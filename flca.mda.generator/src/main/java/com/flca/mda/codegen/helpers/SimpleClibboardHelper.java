package com.flca.mda.codegen.helpers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import flca.mda.codegen.helpers.FileHelper;

public class SimpleClibboardHelper implements ClipboardOwner {

	public void save(String aString) {
		StringSelection stringSelection = new StringSelection(aString);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(stringSelection, stringSelection);		
	    
	   String fname = FileHelper.appendSeperator(System.getProperty("java.io.tmpdir")) + "clipboard.txt";
	    try {
			FileHelper.saveFile(fname, aString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// public String restore()
	// {
	// Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	// clipboard.getContents(this);
	// }

	public void lostOwnership(Clipboard arg0, Transferable arg1) {
	}

}
