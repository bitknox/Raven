package dk.itu.raven.util;

public class Logger {
	public enum LogLevel {
		NONE(0),
		ERROR(1),
		WARNING(2),
		INFO(3),
		DEBUG(4);
		int level;

		private LogLevel(int level) {
			this.level = level;
		}
	}
	static boolean debug = false;
	static LogLevel logLevel = LogLevel.NONE;

	private static boolean hasSufficentLevel(LogLevel level) {
		return level.level <= logLevel.level && logLevel != LogLevel.NONE;
	}

	public static void log(Object o, LogLevel level) {
		if(debug && hasSufficentLevel(level)) System.out.println(o.toString());
	}
	public static void log(LogLevel level) {
		if(debug && hasSufficentLevel(level)) System.out.println("");
	}

	public static void log(Exception e, LogLevel level) {
		if(debug && hasSufficentLevel(level)) e.printStackTrace();
	}

	public static void setDebug(boolean debug) {
		Logger.debug = debug;
	}

	public static boolean getDebug() {
		return debug;
	}

	public static void setLogLevel(LogLevel level) {
		logLevel = level;
	}

	public static LogLevel getLogLevel() {
		return logLevel;
	}
}