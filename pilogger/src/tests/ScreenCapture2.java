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


public class ScreenCapture2 extends JPanel{
	public static final int S_HEIGHT = 240;
	public static final int S_WIDTH  = 320;
	
	
	public static final int[][] thMap44 = {{1, 9, 3, 11},{13, 5, 15, 7},{4, 12, 2, 10},{16, 8, 14, 6}};
	public static final int[][] thMap88 = {{	
	 0, 48, 12, 60,  3, 51, 15, 63},{
    32, 16, 44, 28, 35, 19, 47, 31},{
     8, 56,  4, 52, 11, 59,  7, 55},{
    40, 24, 36, 20, 43, 27, 39, 23},{
     2, 50, 14, 62,  1, 49, 13, 61},{
    34, 18, 46, 30, 33, 17, 45, 29},{
    10, 58,  6, 54,  9, 57,  5, 53},{
    42, 26, 38, 22, 41, 25, 37, 21}}; 
	public static final int[][] thMap1616 = {
		{   0,192, 48,240, 12,204, 60,252,  3,195, 51,243, 15,207, 63,255 },
		{ 128, 64,176,112,140, 76,188,124,131, 67,179,115,143, 79,191,127 },
		{  32,224, 16,208, 44,236, 28,220, 35,227, 19,211, 47,239, 31,223 },
		{ 160, 96,144, 80,172,108,156, 92,163, 99,147, 83,175,111,159, 95 },
		{   8,200, 56,248,  4,196, 52,244, 11,203, 59,251,  7,199, 55,247 },
		{ 136, 72,184,120,132, 68,180,116,139, 75,187,123,135, 71,183,119 },
		{  40,232, 24,216, 36,228, 20,212, 43,235, 27,219, 39,231, 23,215 },
		{ 168,104,152, 88,164,100,148, 84,171,107,155, 91,167,103,151, 87 },
		{   2,194, 50,242, 14,206, 62,254,  1,193, 49,241, 13,205, 61,253 },
		{ 130, 66,178,114,142, 78,190,126,129, 65,177,113,141, 77,189,125 },
		{  34,226, 18,210, 46,238, 30,222, 33,225, 17,209, 45,237, 29,221 },
		{ 162, 98,146, 82,174,110,158, 94,161, 97,145, 81,173,109,157, 93 },
		{  10,202, 58,250,  6,198, 54,246,  9,201, 57,249,  5,197, 53,245 },
		{ 138, 74,186,122,134, 70,182,118,137, 73,185,121,133, 69,181,117 },
		{  42,234, 26,218, 38,230, 22,214, 41,233, 25,217, 37,229, 21,213 },
		{ 170,106,154, 90,166,102,150, 86,169,105,153, 89,165,101,149, 85 }
	};
	
	JLabel labelComputed;
	JLabel labelOriginal;
	
	Robot robot;
	private Timer timer;

	static int[] pixList = new int[S_HEIGHT * S_WIDTH ];
	static int[][] pixArray2D = new int[S_WIDTH][S_HEIGHT];
	static int[][] pixArray2Ddest = new int[S_WIDTH][S_HEIGHT];
	
	public ScreenCapture2() {
		initGUI();
	}

	private void initGUI() {
		labelComputed = new JLabel();
		labelComputed.setPreferredSize(new Dimension(S_WIDTH, S_HEIGHT));
		labelComputed.setOpaque(true);
		labelOriginal = new JLabel();
		labelOriginal.setPreferredSize(new Dimension(S_WIDTH, S_HEIGHT));
		labelOriginal.setOpaque(true);
		add(labelComputed, BorderLayout.CENTER);
		add(labelOriginal, BorderLayout.EAST);

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		CaptureTaskDither task = new CaptureTaskDither();

		timer = new Timer();
		timer.schedule(task, 500, 50);
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
	                nleft=x>0; nright=x<width-1;
	               
//	                oldpixel = pixArray2D[x][y];
//	                
//	                if (oldpixel<85) newpixel = 0;
//	                else if (oldpixel >=85 && oldpixel < 170) newpixel = 128;
//	                else newpixel = 255;
//	                pixArray2D[x][y] = newpixel;
//	                error = oldpixel-newpixel;
//	                
//	                if (nright) 		pixArray2D[x+1][y]	+= 7*error/16;
//	                if (nleft&&nbottom) pixArray2D[x-1][y+1]+= 3*error/16;
//	                if (nbottom) 		pixArray2D[x][y+1] 	+= 5*error/16;   
//	                if (nright&&nbottom)pixArray2D[x+1][y+1]+=   error/16;
//	                
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

			//----
			
			monoImageOrdered.getData().getPixels(0, 0, S_WIDTH, S_HEIGHT, pixList);
			
			for (int i = 0; i < pixList.length; i++) {
				pixArray2D[i%S_WIDTH][i/S_WIDTH] = (int) ((pixList[i]));
			}
			
			width = S_WIDTH;
	        height = S_HEIGHT;
	        double th = 0;
	        for (int y=0; y<height; y++) {
	            for (int x=0; x<width; x++) {
	               
//	                oldpixel = (int) (oldpixel * 0.6667);
//	                th = thMap1616[x%16][y%16];
//	                th = (th/256)*(256/3);
//	                oldpixel = (int) (oldpixel * 0.667);
//	                th = (th/16)*(256/3); 
	              
	            	oldpixel = pixArray2D[x][y];
	                th = thMap44[x%4][y%4];
	                th -= 8;
	                th *= 5;
	                oldpixel += th;
	                
	                if (oldpixel<86) newpixel = 0;
	                else if (oldpixel >=86 && oldpixel < 170) newpixel = 128;
	                else newpixel = 255;
	                pixArray2D[x][y] = newpixel;
	            }
	        }
			for (int i = 0; i < pixList.length; i++) {
				pixList[i]  = pixArray2D[i%S_WIDTH][i/S_WIDTH];
			}
			
			
			wr = monoImageOrdered.getData().createCompatibleWritableRaster();
			wr.setPixels(0, 0, S_WIDTH, S_HEIGHT, pixList);

			monoImageOrdered.setData(wr);

			//---
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					labelComputed.setIcon(new ImageIcon(monoImageFloyd));
					labelOriginal.setIcon(new ImageIcon(monoImageOrdered));
				}
			});
		}
	}

	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("FrameDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new ScreenCapture2(), BorderLayout.CENTER );
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
