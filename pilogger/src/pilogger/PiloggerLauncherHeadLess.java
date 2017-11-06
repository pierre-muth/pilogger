package pilogger;

import javax.swing.JFrame;

public class PiloggerLauncherHeadLess {
	public static boolean simulation = false; 
	public static String onlineFileLocalDirectory = "/home/pi/pilogger/logs/online/";
	public static String fileLocalDirectory = "/home/pi/pilogger/logs/online/";

	/**
	 * Main Pilogger Launcher
	 * @param args sim : simulation
	 */
	public static void main(String[] args) {
		simulation = args.length > 0;
        new PiloggerImpl();
	}

}
