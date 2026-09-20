package com.tangluobo.rdp4j.clipboard;

import cn.oyzh.common.log.JulLog;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The one place in this package that touches AWT's clipboard.
 *
 * <p>Text goes through the JavaFX clipboard ({@link ClipboardContent}), which is
 * the JavaFX-native path. The file list stays on
 * {@code java.awt.datatransfer}: JavaFX can neither publish a lazily-rendered
 * payload nor express {@code CF_HDROP}, both of which "paste a file copied from
 * the remote desktop" requires. That dependency is confined to this file.</p>
 *
 * <p>Note the Windows virtual-file path
 * ({@link WindowsVirtualFileClipboard}) is pure JNA/OLE and is untouched by
 * this class.</p>
 */
public final class FxSystemClipboardAdapter implements SystemClipboardAdapter {

	/** Bounded wait for a clipboard round trip onto the JavaFX thread. */
	private static final long FX_TIMEOUT_SECONDS = 5L;

	private final Owner owner = new Owner();

	/** True while this channel is the publisher of the current clipboard. */
	private volatile boolean ownsClipboard;

	/**
	 * The text this channel last published. JavaFX fires no ownership callback,
	 * so comparing the live text against this is the only way to notice that
	 * another application has taken the clipboard over.
	 */
	private volatile String publishedText;

	@Override
	public LocalContents read() {
		String text = readText();
		List<File> files = readFiles();
		boolean owned = ownsClipboard;
		if (owned && publishedText != null && !publishedText.equals(text)) {
			// The clipboard text is no longer ours. Without this check the flag
			// would stay set forever (nothing clears it for a JavaFX write) and
			// local copies would never be announced to the server again.
			ownsClipboard = false;
			publishedText = null;
			owned = false;
		}
		return new LocalContents(text, files, owned);
	}

	private String readText() {
		try {
			return callOnFxThread(() -> {
				Clipboard clipboard = Clipboard.getSystemClipboard();
				return clipboard.hasString() ? clipboard.getString() : null;
			});
		} catch (RuntimeException e) {
			JulLog.debug("Clipboard text read failed: " + e.getMessage());
			return null;
		}
	}

	private List<File> readFiles() {
		try {
			Transferable contents = java.awt.Toolkit.getDefaultToolkit()
					.getSystemClipboard().getContents(owner);
			if (contents == null || !contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return List.of();
			}
			Object data = contents.getTransferData(DataFlavor.javaFileListFlavor);
			if (data instanceof List<?> list) {
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) list;
				return files;
			}
		} catch (UnsupportedFlavorException | IOException e) {
			JulLog.debug("Clipboard file read failed: " + e.getMessage());
		} catch (RuntimeException e) {
			// IllegalStateException when the platform clipboard is unavailable,
			// e.g. before the toolkit starts.
			JulLog.debug("Clipboard file read unavailable: " + e.getMessage());
		}
		return List.of();
	}

	@Override
	public void writeText(String text) {
		String value = text == null ? "" : text;
		runOnFxThread(() -> {
			ClipboardContent content = new ClipboardContent();
			content.putString(value);
			Clipboard.getSystemClipboard().setContent(content);
			publishedText = value;
			ownsClipboard = true;
		});
	}

	@Override
	public DeferredFileHandle writeDeferredFileList() {
		DeferredFileListTransferable transfer = new DeferredFileListTransferable();
		runOnFxThread(() -> {
			try {
				setAwtContents(transfer);
				publishedText = null;
				ownsClipboard = true;
			} catch (RuntimeException e) {
				transfer.cancel();
				throw e;
			}
		});
		return transfer;
	}

	@Override
	public void writeFileList(List<File> files) {
		runOnFxThread(() -> {
			setAwtContents(new FileListTransferable(files));
			publishedText = null;
			ownsClipboard = true;
		});
	}

	private void setAwtContents(Transferable transferable) {
		java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, owner);
	}

	@Override
	public boolean ownsClipboard() {
		return ownsClipboard;
	}

	@Override
	public void dispose() {
		ownsClipboard = false;
		publishedText = null;
	}

	// ------------------------------------------------------------------
	// Thread marshalling
	// ------------------------------------------------------------------

	private void runOnFxThread(Runnable action) {
		if (Platform.isFxApplicationThread()) {
			action.run();
		} else {
			Platform.runLater(action);
		}
	}

	/**
	 * Runs a blocking read on the JavaFX thread. Reads originate on the RDP
	 * protocol thread, so the cost is one JavaFX pulse -- the same round trip the
	 * old AWT {@code getContents} made to the platform.
	 */
	private <T> T callOnFxThread(java.util.function.Supplier<T> action) {
		if (Platform.isFxApplicationThread()) {
			return action.get();
		}
		FutureTask<T> task = new FutureTask<>(action::get);
		Platform.runLater(task);
		try {
			return task.get(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted reading the clipboard", e);
		} catch (ExecutionException | TimeoutException e) {
			throw new IllegalStateException("Unable to read the clipboard", e);
		}
	}

	// ------------------------------------------------------------------
	// AWT transferables: the isolated file-list path
	// ------------------------------------------------------------------

	/**
	 * Clears the ownership flag when another application takes the clipboard.
	 * JavaFX has no ownership concept, so this is the only signal available.
	 */
	private final class Owner implements ClipboardOwner {
		@Override
		public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
			ownsClipboard = false;
			JulLog.debug("Lost clipboard ownership");
		}
	}

	/** A file list that is already fully known. */
	private static final class FileListTransferable implements Transferable {

		private final List<File> files;

		FileListTransferable(List<File> files) {
			this.files = files == null ? List.of() : List.copyOf(files);
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.javaFileListFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return files;
		}
	}

	/**
	 * Publishes the file-list flavor immediately while delaying the actual paths
	 * until their RDP file-content streams have finished downloading. The
	 * consuming application's paste blocks in {@link #getTransferData} until
	 * {@link #complete} runs.
	 */
	private static final class DeferredFileListTransferable implements Transferable, DeferredFileHandle {

		private final CountDownLatch ready = new CountDownLatch(1);
		private volatile List<File> files;

		@Override
		public void complete(List<File> completedFiles) {
			if (ready.getCount() != 0) {
				files = completedFiles == null ? List.of() : List.copyOf(completedFiles);
				ready.countDown();
			}
		}

		@Override
		public void cancel() {
			complete(List.of());
		}

		@Override
		public boolean isReady() {
			return ready.getCount() == 0;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.javaFileListFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			try {
				ready.await();
			} catch (InterruptedException error) {
				Thread.currentThread().interrupt();
				throw new IOException("Interrupted while waiting for remote clipboard files", error);
			}
			return files;
		}
	}
}
