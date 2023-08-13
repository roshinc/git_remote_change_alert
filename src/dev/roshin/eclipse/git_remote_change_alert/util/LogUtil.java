package dev.roshin.eclipse.git_remote_change_alert.util;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import dev.roshin.eclipse.git_remote_change_alert.Activator;

/**
 * The logs will be written to the Eclipse log file, which you can typically
 * find in the .metadata/.log file in your workspace directory. You can also
 * view logs from within Eclipse in the Error Log view.
 */
public class LogUtil {

	protected static void log(int severity, String message, Throwable exception) {
		ILog log = Platform.getLog(Platform.getBundle(Activator.PLUGIN_ID));
		log.log(new Status(severity, Activator.PLUGIN_ID, message, exception));
	}

	public static void logError(String message, Throwable exception) {
		log(Status.ERROR, message, exception);
	}

	public static void logInfo(String message) {
		log(Status.INFO, message, null);
	}

	public static void logWarning(String message) {
		log(Status.WARNING, message, null);
	}
}
