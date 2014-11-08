package tests;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class ScreenCapture3 extends JPanel{
	public static final int S_HEIGHT = 120;
	public static final int S_WIDTH  = 160;
	
	JLabel labelComputed;
	Robot robot;
	private Timer timer;

	static int[] pixList = new int[S_HEIGHT * S_WIDTH ];
	static int[][] pixArray2D = new int[S_WIDTH][S_HEIGHT];
	static int[][] pixArray2Ddest = new int[S_WIDTH][S_HEIGHT];
	
	public ScreenCapture3() {
		initGUI();
	}

	private void initGUI() {
		labelComputed = new JLabel();
		labelComputed.setPreferredSize(new Dimension(S_WIDTH, S_HEIGHT));
		labelComputed.setOpaque(true);
		add(labelComputed, BorderLayout.CENTER);

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		CaptureTaskDither task = new CaptureTaskDither();

		timer = new Timer();
		timer.schedule(task, 500, 1000);
	}

	private class CaptureTaskDither extends TimerTask {

		@Override
		public void run() {
			Rectangle screenRectangle = new Rectangle(
					MouseInfo.getPointerInfo().getLocation().x - (S_WIDTH/2)
					, MouseInfo.getPointerInfo().getLocation().y - (S_HEIGHT/2),
					S_WIDTH, S_HEIGHT);
			final BufferedImage sourceImage = robot.createScreenCapture(screenRectangle);
			final BufferedImage monoImageFloyd = new BufferedImage(S_WIDTH, S_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
			final BufferedImage monoImageOrdered = new BufferedImage(S_WIDTH, S_HEIGHT, BufferedImage.TYPE_BYTE_GRAY); 
			
			Graphics2D g2d = monoImageFloyd.createGraphics();
			g2d.drawImage(sourceImage,0, 0, S_WIDTH, S_HEIGHT, null); 
			g2d.dispose();
			
			g2d = monoImageOrdered.createGraphics();
			g2d.drawImage(sourceImage,0, 0, S_WIDTH, S_HEIGHT, null); 
			g2d.dispose();
			
			monoImageFloyd.getData().getPixels(0, 0, S_WIDTH, S_HEIGHT, pixList);
			
			for (int i = 0; i < pixList.length; i++) {
				pixArray2D[i%S_WIDTH][i/S_WIDTH] = (int) ((pixList[i]));
				pixArray2Ddest[i%S_WIDTH][i/S_WIDTH] = 0;
			}
			
			int width = S_WIDTH;
	        int height = S_HEIGHT;
	        int oldpixel, newpixel, error;
	        boolean nbottom, nleft, nright;
	        
	        for (int y=0; y<height; y++) {
	            nbottom=y<height-1;
	            for (int x=0; x<width; x++) {
	                nleft = x>0; 
	                nright = x<width-1;
	               
	                oldpixel = pixArray2Ddest[x][y] + pixArray2D[x][y];
	                
	                if (oldpixel<85) 
	                	newpixel = 0;
	                else if (oldpixel >=85 && oldpixel < 170) 
	                	newpixel = 128;
	                else 
	                	newpixel = 255;
	                
	                pixArray2Ddest[x][y] = newpixel;
	                error = oldpixel-newpixel;
	                
	                if (nright) 		pixArray2Ddest[x+1][y]	+= 7*error/16;
	                if (nleft&&nbottom) pixArray2Ddest[x-1][y+1]+= 3*error/16;
	                if (nbottom) 		pixArray2Ddest[x][y+1] 	+= 5*error/16;   
	                if (nright&&nbottom)pixArray2Ddest[x+1][y+1]+=   error/16;
	                
	            }
	        }
			
			for (int i = 0; i < pixList.length; i++) {
				pixList[i]  = pixArray2Ddest[i%S_WIDTH][i/S_WIDTH];
			}
			
			
			WritableRaster wr = monoImageFloyd.getData().createCompatibleWritableRaster();
			wr.setPixels(0, 0, S_WIDTH, S_HEIGHT, pixList);

			monoImageFloyd.setData(wr);
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					labelComputed.setIcon(new ImageIcon(monoImageFloyd));
				}
			});
		}
	}

	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("FrameDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new ScreenCapture3(), BorderLayout.CENTER );
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
