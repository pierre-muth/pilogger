package pilogger;

import javax.swing.JFrame;

public class PiloggerLauncher {

	/**
	 * Main Pilogger Launcher
	 * @param args not used
	 */
	public static void main(String[] args) {
		boolean simulation = args.length > 0;
		JFrame f = new JFrame("Pilogger");
        f.getContentPane().add(new PiloggerImpl(simulation));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        f.setUndecorated(true);
        f.setVisible(true);
        f.pack();
	}

}
