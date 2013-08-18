package tests;

import javax.swing.JFrame;

import pilogger.PiloggerGUI;

public class PiloggerGuiTest extends PiloggerGUI{
	
	public PiloggerGuiTest() {
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame("Pilogger TEST");
        f.getContentPane().add(new PiloggerGuiTest());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
	}

}
