package pilogger;

import javax.swing.JFrame;

public class PiloggerLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame("Pilogger");
        f.getContentPane().add(new PiloggerImpl());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
	}

}
