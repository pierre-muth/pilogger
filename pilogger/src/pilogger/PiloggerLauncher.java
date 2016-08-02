package pilogger;

import javax.swing.JFrame;

public class PiloggerLauncher {
	public static boolean simulation = false; 

	/**
	 * Main Pilogger Launcher
	 * @param args sim : simulation
	 */
	public static void main(String[] args) {
		simulation = args.length > 0;
		JFrame f = new JFrame("Pilogger");
		PiloggerGUI gui = new PiloggerGUI(); 
        f.getContentPane().add(gui);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (!simulation){
        	f.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        	f.setUndecorated(true);
        }
        f.setVisible(true);
        f.pack();
        f.addKeyListener(gui);
        new PiloggerImpl(gui);
	}

}
