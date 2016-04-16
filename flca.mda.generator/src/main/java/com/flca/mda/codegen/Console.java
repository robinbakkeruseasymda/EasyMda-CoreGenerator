package com.flca.mda.codegen;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console {

	private static MessageConsoleStream consoleStream = null;

	public static MessageConsoleStream getStream() {
		if (consoleStream == null) {
			MessageConsole console = new MessageConsole("easyMDA Console", null);
			console.activate();
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(
					new IConsole[] { console });
			consoleStream = console.newMessageStream();
		}
		return consoleStream;
	}
}
