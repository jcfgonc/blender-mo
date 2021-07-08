package jcfgonc.moea.tools;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * From https://stackoverflow.com/a/14226456
 * 
 * @author daredesm
 *
 */
public class StringClipBoardListener extends Thread implements ClipboardOwner {
	private Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
	private Consumer<String> clipboardConsumer;
	private boolean clipboardOwn;

	public StringClipBoardListener(Consumer<String> clipboardConsumer) {
		this.setDaemon(true);
		this.clipboardConsumer = clipboardConsumer;
		this.clipboardOwn = false;
	}

	@Override
	public void run() {
		Transferable trans = sysClip.getContents(this);
		takeOwnership(trans);
	}

	@Override
	public void lostOwnership(Clipboard c, Transferable t) {
		clipboardOwn = false;
		do {
			try {
				StringClipBoardListener.sleep(100); // waiting e.g for loading huge elements like word's etc.
				Transferable contents = sysClip.getContents(this);
				if (contents != null) {
					processClipboard(contents, c);
				}
				takeOwnership(contents);
			} catch (InterruptedException e) {
		//		e.printStackTrace();
			} catch (UnsupportedFlavorException e) {
		//		e.printStackTrace();
			} catch (IOException e) {
		//		e.printStackTrace();
			} catch (IllegalStateException e) {
		//		e.printStackTrace();
			}
		} while (!clipboardOwn);
	}

	private void takeOwnership(Transferable t) {
		sysClip.setContents(t, this);
		clipboardOwn = true;
	}

	private void processClipboard(Transferable trans, Clipboard c) throws UnsupportedFlavorException, IOException {
		if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			String tempText = (String) trans.getTransferData(DataFlavor.stringFlavor);
			clipboardConsumer.accept(tempText);
		}
	}

}