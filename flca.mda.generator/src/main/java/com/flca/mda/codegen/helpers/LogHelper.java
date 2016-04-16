package com.flca.mda.codegen.helpers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import flca.mda.codegen.helpers.FileHelper;
import flca.mda.codegen.helpers.ShellUtils;

public class LogHelper {
	public static String CODEGEN_CONSOLE = "CODEGEN_CONSOLE";

	private static Logger logger = LoggerFactory.getLogger(LogHelper.class);

	private static int sWarnCount = 0;
	private static int sErrorCount = 0;
	private static int sFileCount = 0;
	private static int sLineCount = 0;

	private static StringBuffer sBuffer;

	public static void initialize() {
		sBuffer = new StringBuffer();
		sErrorCount = 0;
		sWarnCount = 0;
		sLineCount = 0;
		sFileCount = 0;
		info("------------------------------------------------\n");
	}

	public static void updateLinesGenerated(String generatedSource) {
		int count = getLineCount(generatedSource);
		if (count > 0) {
			sLineCount += count;
			sFileCount += 1;
			new SimpleClibboardHelper().save(generatedSource);
		}
	}

	public static void info(String aMsg) {
		logger.info(aMsg);
	}

	/**
	 * this writes a message to the eclipse console and the logfile
	 * 
	 * @param aMsg
	 */
	public static void console(String aMsg) {
		logger.info(aMsg);
		writeToConsole(aMsg);
	}

	public static void warning(String aMsg) {
		logger.warn(aMsg);
	}

	public static void debug(String aMsg) {
		logger.debug(aMsg);
	}

	public static void error(String aMsg) {
		logger.error(aMsg, true);
	}

	public static void error(String aMsg, boolean toConsole) {
		logger.info(aMsg);
		if (toConsole)
			writeToConsole(aMsg);
	}

	public static void error(String aMsg, Throwable ex) {
		String fout = ex.getMessage();
		if (ex instanceof InvocationTargetException) {
			fout = ((InvocationTargetException) ex).getCause().getMessage();
		}
		logger.error(aMsg + " " + fout);
		writeToConsole(aMsg + " " + fout);
		logErrorStacktrace(ex);
	}

	private static void logErrorStacktrace(Throwable ex) {
		OutputStream os = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(os);
		ex.printStackTrace(writer);
		writer.flush();
		logger.error("\n" + os.toString(), false);
		try {
			os.close();
		} catch (IOException e) {
			System.out.println("error closing stacktracefile " + e);
		}
	}

	public static String getResults() {
		String msg = "\n\ngenerated " + sFileCount + " files with " + sLineCount + " lines\n";
		msg += sErrorCount + " errors  and " + sWarnCount + " warnings encountered";
		info(msg);
		return msg;
	}

	public static boolean isDebugEnabled() {
		return true;
	}

	public static void writeLogfile(String aFilename) {
		try {
			FileHelper.saveFile(new File(aFilename), sBuffer.toString());
		} catch (Exception e) {
			writeToConsole("error creating logfile " + aFilename + " : " + e);
		}
	}

	private static void writeToConsole(String msg) {
		System.out.println("console> " + msg);

		if (!ShellUtils.isJunitTest()) {
			MessageConsole myConsole = findConsole(CODEGEN_CONSOLE);
			MessageConsoleStream out = myConsole.newMessageStream();
			out.println(msg);
		}
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	private static final Pattern errorPattern = Pattern.compile("(.*)( ERROR )(.*)");

	public static int getLogbackErrorCount() {
		int result = 0;

		File logfile = getLogbackFile();
		if (logfile != null) {
			try {
				String loglines = FileHelper.readFile(logfile);
				String lines[] = loglines.split("\n");
				for (String line : lines) {
					Matcher errmatch = errorPattern.matcher(line);
					if (errmatch.find())
						result++;
				}
			} catch (Exception e) {
				LogHelper.error("error reading logfile " + e);
			}
		}
		return result;
	}

	private static File getLogbackFile() {
		File result = null;
		String fname = getLogbackFilename();

		if (fname != null) {
			result = new File(fname);
			if (result.exists()) {
				return result;
			} else {
				return null;
			}
		}
		return null;
	}

	private static String getLogbackFilename() {
		String result = null;
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
			for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
				Appender<ILoggingEvent> appender = index.next();
				if (appender instanceof FileAppender<?>) {
					FileAppender<?> fap = (FileAppender<?>) appender;
					result = fap.getFile();
					break;
				}
			}
		}

		return result;
	}
	
	private static int getLineCount(String src) {
		StringTokenizer tokens = new StringTokenizer(src, "/n");
		int result = 0;
		while (tokens.hasMoreTokens()) {
			tokens.nextToken();
			result++;
		}
		return result;
	}
}
