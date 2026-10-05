package com.tangluobo.rdp4j.clipboard;

import java.io.File;
import java.util.List;

/**
 * The single seam between RDP clipboard redirection and the local system
 * clipboard.
 *
 * <p>This exists so that the rest of the package stays free of
 * {@code java.awt.datatransfer}. Text and HTML are served by the JavaFX
 * clipboard; the file-list path keeps AWT's {@code Transferable}, because
 * JavaFX has no delayed rendering, no way to enumerate a clipboard's native
 * formats, and no {@code ClipboardOwner} equivalent — all three of which the
 * RDP file-transfer flow depends on.</p>
 *
 * <p>Every method is safe to call from any thread: implementations marshal to
 * the UI thread internally. Reads block the calling thread, so callers must not
 * invoke them while holding a platform clipboard lock.</p>
 */
public interface SystemClipboardAdapter {

	/**
	 * One consistent read of the local clipboard.
	 *
	 * @param text      clipboard text, or null when it holds none
	 * @param files     clipboard file list, empty when it holds none
	 * @param ownedByUs true when this channel published the current contents,
	 *                  used to avoid announcing remote content back to the same
	 *                  server
	 */
	record LocalContents(String text, List<File> files, boolean ownedByUs) {

		public static LocalContents empty() {
			return new LocalContents(null, List.of(), false);
		}

		public boolean hasText() {
			return text != null && !text.isEmpty();
		}

		public boolean hasFiles() {
			return files != null && !files.isEmpty();
		}
	}

	/**
	 * A published file list whose entries are filled in later, once the RDP
	 * streams have finished downloading. This is the delayed-rendering handle:
	 * a paste blocks until {@link #complete} runs.
	 */
	interface DeferredFileHandle {

		/** Publish the downloaded files, releasing any blocked paste. */
		void complete(List<File> files);

		/** Give up; releases blocked pastes with an empty list. */
		void cancel();

		/** True once {@link #complete} or {@link #cancel} has run. */
		boolean isReady();
	}

	/** Read the local clipboard once. */
	LocalContents read();

	/** Replace the local clipboard with the given text. */
	void writeText(String text);

	/**
	 * Announce a file list immediately, deferring the paths until the download
	 * finishes.
	 *
	 * @return handle used to complete or cancel the transfer
	 */
	DeferredFileHandle writeDeferredFileList();

	/** Replace the local clipboard with an already-downloaded file list. */
	void writeFileList(List<File> files);

	/** True when this channel is still the clipboard owner for what it wrote. */
	boolean ownsClipboard();

	/** Drop any published content and release resources. */
	void dispose();
}
