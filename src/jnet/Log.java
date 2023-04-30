package jnet;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;


/**
 * Utilities to handle logging and output. All messages logged with {@code Log::stdlog} are written to the
 * file {@code Log.STDLOG_FILE}.
 *
 * @author Jonathan Uhler
 */
public class Log {

	/** The standard log file. */
	public static final String STDLOG_FILE = "Log.log";

	/** Debug logging level. */
	public static final int DEBUG = 0;
	/** Informational logging level. */
	public static final int INFO = 1;
	/** Warning logging level. */
	public static final int WARN = 2;
	/** Error logging level. */
	public static final int ERROR = 3;
	/** Fatal error logging level. */
	public static final int FATAL = 4;

	private static final String[] levelToString = {"DEBUG", "INFO", "WARN", "ERROR", "FATAL"};


	private Log() { }


	/**
	 * Displays a graphical message.
	 *
	 * @param title the window title for the displayed message.
	 * @param message the message to display.
	 *
	 * @see javax.swing.JOptionPane
	 */
	public static void gfxmsg(String title, Object message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
	}


	/**
	 * Generates a log message with specific formatting. This method does not print or write to any
	 * buffer. If {@code level} is out of bounds, the level will be omitted. This method will
	 * always generate a formatted output and will never result in error.
	 * <p>
	 * The logging format is as follows:
	 * <br>
	 * {@code "LEVEL (LOCATION)  MESSAGE"}
	 * <br>
	 * or
	 * <br>
	 * {@code "Log.format (LOCATION)  MESSAGE"}
	 * <br>
	 * If the level was out of bounds.
	 *
	 * @param level the logging level of the message, which should be an integer in the interval 
	 *        {@code [0, Log.levelToString.length]}.
	 * @param location the location where the error occured (e.g. a class or method name).
	 * @param message the error message.
	 *
	 * @return the formatted message.
	 */
	public static String format(int level, String location, String message) {
		// Check if the level is known. If not, replace where the level string would be with "Log.stdout"
		if (level < Log.DEBUG || level > Log.FATAL)
			return "Log.format (" + location + ")  " + message;
		else
			return levelToString[level] + " (" + location + ")  " + message;
	}
	

	/**
	 * Formats and prints information to the standard output.
	 *
	 * @param level the verbosity level of the message.
	 * @param location the location the message originated from.
	 * @param message the message to print.
	 *
	 * @see format
	 */
	public static void stdout(int level, String location, String message) {
		System.out.println(Log.format(level, location, message));
	}


	/**
	 * Formats and writes information to the standard log file. The formatted message will also be printed
	 * by default if the logging level is greater than {@code Log.INFO}. This behavior can be overridden by
	 * calling {@code Log.stdlog(int, String, String, boolean)} with {@code false} for the last argument.
	 *
	 * @param level the verbosity level of the message.
	 * @param location the location the message originated from.
	 * @param message the message to print.
	 *
	 * @see stdlog(int, String, String, boolean)
	 * @see format
	 */
	public static void stdlog(int level, String location, String message) {
		boolean print = (level == Log.WARN || level == Log.ERROR || level == Log.FATAL);
		Log.stdlog(level, location, message, print);
	}


	/**
	 * Formats and writes information to the standard log file.
	 *
	 * @param level the verbosity level of the message.
	 * @param location the location the message originated from.
	 * @param message the message to print.
	 * @param print whether to also print the formatted message to the standard output.
	 *
	 * @see stdlog(int, String, String)
	 * @see format
	 */
	public static void stdlog(int level, String location, String message, boolean print) {
		// Print message to stdout if requested
		if (print)
			Log.stdout(level, location, message);

		// Write to the log file
		if (Log.STDLOG_FILE != null) {
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(Log.STDLOG_FILE),
																 StandardCharsets.UTF_8,
																 StandardOpenOption.APPEND,
																 StandardOpenOption.CREATE)) {
				writer.write(Log.format(level, location, message) + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			Log.stdout(level, location, "(STDLOG_FILE was null) " + message);
		}

		// If the message was a fatal error, assume there is no way to recover and close to program to
		// prevent further errors or damage
		if (level == Log.FATAL)
			System.exit(Log.FATAL);
	}
	
}
