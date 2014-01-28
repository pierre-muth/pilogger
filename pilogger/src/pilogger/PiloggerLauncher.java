package pilogger;

import javax.swing.JFrame;

public class PiloggerLauncher {

	/**
	 * Main Pilogger Launcher
	 * @param args sim : simulation
	 */
	public static void main(String[] args) {
		boolean simulation = args.length > 0;
		JFrame f = new JFrame("Pilogger");
		PiloggerGUI gui = new PiloggerGUI(); 
        f.getContentPane().add(gui);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        f.setUndecorated(true);
        f.setVisible(true);
        f.pack();
        new PiloggerImpl(simulation, gui);
	}

}
