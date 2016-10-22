package net.relinc.libraries.staticClasses;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class SPLogger {

	public static Logger logger = Logger.getLogger("SPLogger");
	static{
		try {
			FileHandler handler = new FileHandler(SPSettings.applicationSupportDirectory + "/RELFX/Log/" + "SPLog.%u.%g.txt", 1024 * 1024, 10, true);
			handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
			logger.setLevel(Level.ALL);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
