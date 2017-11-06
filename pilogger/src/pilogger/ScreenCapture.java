package pilogger;

import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ScreenCapture extends TimerTask{
	public static final String CAPTURE_FILENAME = "pilogger320x240.png";
	public static final int WIDTH = 320;
	public static final int HEIGHT = 240;
	private static JPanel GUI;
	
	public ScreenCapture(JPanel gui) {
		this.GUI = gui;
	}
	
	public static void capture() throws IOException, AWTException {
		if (GUI == null) return;
		
		final BufferedImage sourceImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = sourceImage.createGraphics();
		GUI.paint(g);
		g.dispose();
		File outputfile = new File(ProbeManagerSwing.onlineFileLocalDirectory+CAPTURE_FILENAME);
	    ImageIO.write(sourceImage, "png", outputfile);
	}

	@Override
	public void run() {
		try {
			ScreenCapture.capture();
		} catch (IOException | AWTException e) {
			System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+
					": Fail to capture screen");
		}
		
	}
	

}
