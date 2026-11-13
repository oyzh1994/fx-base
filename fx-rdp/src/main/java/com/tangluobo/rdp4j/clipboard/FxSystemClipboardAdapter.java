package com.tangluobo.rdp4j.clipboard;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.util.FXUtil;
import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
		try {
			if (com.sun.jna.Platform.isMac()) {
				MacPasteboard.NativeContents contents = MacPasteboard.read();
				if (contents == null) {
					return LocalContents.empty();
				}
				return localContents(contents.text(), contents.files());
			}
			return callOnFxThread(this::readOnFxThread);
		} catch (RuntimeException e) {
			JulLog.debug("Clipboard read failed: " + e.getMessage());
			return LocalContents.empty();
		}
	}

	private LocalContents readOnFxThread() {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		String text = clipboard.hasString() ? clipboard.getString() : null;
		Transferable contents;
		try {
			contents = java.awt.Toolkit.getDefaultToolkit()
					.getSystemClipboard().getContents(owner);
		} catch (RuntimeException e) {
			contents = null;
		}
		return localContents(text, readFiles(contents));
	}

	private LocalContents localContents(String text, List<File> files) {
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

	private List<File> readFiles(Transferable contents) {
		try {
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
		FXUtil.runLater(() -> {
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
		FXUtil.runLater(() -> {
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
		FXUtil.runLater(() -> {
			setAwtContents(new FileListTransferable(files));
			publishedText = null;
			ownsClipboard = true;
		});
	}

	private void setAwtContents(Transferable transferable) {
		callOnAwtThread(() -> {
			java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, owner);
			return null;
		});
	}

	@Override
	public boolean ownsClipboard() {
		return ownsClipboard;
	}

	@Override
	public long changeCount() {
		return MacPasteboard.changeCount();
	}

	@Override
	public void dispose() {
		ownsClipboard = false;
		publishedText = null;
	}

	// ------------------------------------------------------------------
	// Thread marshalling
	// ------------------------------------------------------------------

//	private void runOnFxThread(Runnable action) {
//		if (Platform.isFxApplicationThread()) {
//			action.run();
//		} else {
//			Platform.runLater(action);
//		}
//	}

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

	private <T> T callOnAwtThread(java.util.function.Supplier<T> action) {
		if (java.awt.EventQueue.isDispatchThread()) {
			return action.get();
		}
		FutureTask<T> task = new FutureTask<>(action::get);
		java.awt.EventQueue.invokeLater(task);
		try {
			return task.get(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted reading the AWT clipboard", e);
		} catch (ExecutionException | TimeoutException e) {
			task.cancel(true);
			throw new IllegalStateException("Unable to read the AWT clipboard", e);
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

	private static final class MacPasteboard {
		private record NativeContents(String text, List<File> files) {
		}

		private static final Function MESSAGE;
		private static final Pointer GENERAL_PASTEBOARD;
		private static final Pointer CHANGE_COUNT;
		private static final Pointer PASTEBOARD_ITEMS;
		private static final Pointer COUNT;
		private static final Pointer OBJECT_AT_INDEX;
		private static final Pointer STRING_FOR_TYPE;
		private static final Pointer UTF8_STRING;
		private static final Pointer NSSTRING_CLASS;
		private static final Pointer STRING_WITH_UTF8;
		private static final Pointer NSURL_CLASS;
		private static final Pointer URL_WITH_STRING;
		private static final Pointer FILE_PATH_URL;
		private static final Pointer PATH;

		static {
			Function message = null;
			Pointer pasteboard = null;
			Pointer changeCount = null;
			Pointer pasteboardItems = null;
			Pointer count = null;
			Pointer objectAtIndex = null;
			Pointer stringForType = null;
			Pointer utf8String = null;
			Pointer nsStringClass = null;
			Pointer stringWithUtf8 = null;
			Pointer nsUrlClass = null;
			Pointer urlWithString = null;
			Pointer filePathUrl = null;
			Pointer path = null;
			if (com.sun.jna.Platform.isMac()) {
				try {
					NativeLibrary objc = NativeLibrary.getInstance("objc");
					Function getClass = objc.getFunction("objc_getClass");
					Function selector = objc.getFunction("sel_registerName");
					message = objc.getFunction("objc_msgSend");
					Pointer pasteboardClass = getClass.invokePointer(new Object[]{"NSPasteboard"});
					Pointer generalSelector = selector.invokePointer(new Object[]{"generalPasteboard"});
					pasteboard = message.invokePointer(new Object[]{pasteboardClass, generalSelector});
					changeCount = selector.invokePointer(new Object[]{"changeCount"});
					pasteboardItems = selector.invokePointer(new Object[]{"pasteboardItems"});
					count = selector.invokePointer(new Object[]{"count"});
					objectAtIndex = selector.invokePointer(new Object[]{"objectAtIndex:"});
					stringForType = selector.invokePointer(new Object[]{"stringForType:"});
					utf8String = selector.invokePointer(new Object[]{"UTF8String"});
					nsStringClass = getClass.invokePointer(new Object[]{"NSString"});
					stringWithUtf8 = selector.invokePointer(new Object[]{"stringWithUTF8String:"});
					nsUrlClass = getClass.invokePointer(new Object[]{"NSURL"});
					urlWithString = selector.invokePointer(new Object[]{"URLWithString:"});
					filePathUrl = selector.invokePointer(new Object[]{"filePathURL"});
					path = selector.invokePointer(new Object[]{"path"});
				} catch (Throwable error) {
					JulLog.debug("Native pasteboard unavailable: " + error.getMessage());
					message = null;
					pasteboard = null;
					changeCount = null;
					pasteboardItems = null;
					count = null;
					objectAtIndex = null;
					stringForType = null;
					utf8String = null;
					nsStringClass = null;
					stringWithUtf8 = null;
					nsUrlClass = null;
					urlWithString = null;
					filePathUrl = null;
					path = null;
				}
			}
			MESSAGE = message;
			GENERAL_PASTEBOARD = pasteboard;
			CHANGE_COUNT = changeCount;
			PASTEBOARD_ITEMS = pasteboardItems;
			COUNT = count;
			OBJECT_AT_INDEX = objectAtIndex;
			STRING_FOR_TYPE = stringForType;
			UTF8_STRING = utf8String;
			NSSTRING_CLASS = nsStringClass;
			STRING_WITH_UTF8 = stringWithUtf8;
			NSURL_CLASS = nsUrlClass;
			URL_WITH_STRING = urlWithString;
			FILE_PATH_URL = filePathUrl;
			PATH = path;
		}

		static long changeCount() {
			if (MESSAGE == null || GENERAL_PASTEBOARD == null || CHANGE_COUNT == null) {
				return -1L;
			}
			try {
				return MESSAGE.invokeLong(new Object[]{GENERAL_PASTEBOARD, CHANGE_COUNT});
			} catch (Throwable error) {
				return -1L;
			}
		}

		static NativeContents read() {
			if (MESSAGE == null || GENERAL_PASTEBOARD == null || PASTEBOARD_ITEMS == null) {
				return null;
			}
			try {
				Pointer items = MESSAGE.invokePointer(new Object[]{GENERAL_PASTEBOARD, PASTEBOARD_ITEMS});
				if (items == null) {
					return new NativeContents(null, List.of());
				}
				long count = MESSAGE.invokeLong(new Object[]{items, COUNT});
				String text = null;
				List<File> files = new ArrayList<>();
				for (long i = 0; i < count; i++) {
					Pointer item = MESSAGE.invokePointer(
							new Object[]{items, OBJECT_AT_INDEX, new com.sun.jna.NativeLong(i)});
					if (item == null) {
						continue;
					}
					if (text == null) {
						text = stringForType(item, "public.utf8-plain-text");
					}
					File file = resolveFileUrl(stringForType(item, "public.file-url"));
					if (file != null && !files.contains(file)) {
						files.add(file);
					}
				}
				return new NativeContents(text, files);
			} catch (Throwable error) {
				JulLog.debug("Native pasteboard read failed: " + error.getMessage());
				return null;
			}
		}

		private static String stringForType(Pointer item, String type) {
			Pointer typeString = nativeString(type);
			if (typeString == null) {
				return null;
			}
			Pointer value = MESSAGE.invokePointer(
					new Object[]{item, STRING_FOR_TYPE, typeString});
			return javaString(value);
		}

		private static File resolveFileUrl(String value) {
			if (value == null || value.isEmpty()) {
				return null;
			}
			try {
				Pointer urlString = nativeString(value);
				Pointer url = MESSAGE.invokePointer(
						new Object[]{NSURL_CLASS, URL_WITH_STRING, urlString});
				Pointer fileUrl = MESSAGE.invokePointer(new Object[]{url, FILE_PATH_URL});
				Pointer pathString = MESSAGE.invokePointer(new Object[]{fileUrl, PATH});
				String path = javaString(pathString);
				return path == null || path.isEmpty() ? null : new File(path);
			} catch (Throwable error) {
				return null;
			}
		}

		private static Pointer nativeString(String value) {
			return MESSAGE.invokePointer(
					new Object[]{NSSTRING_CLASS, STRING_WITH_UTF8, value});
		}

		private static String javaString(Pointer value) {
			if (value == null) {
				return null;
			}
			Pointer utf8 = MESSAGE.invokePointer(new Object[]{value, UTF8_STRING});
			return utf8 == null ? null : utf8.getString(0);
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
