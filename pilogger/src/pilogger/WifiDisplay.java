package pilogger;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

import probes.AbstractProbe;

public class WifiDisplay extends AbstractProbe implements Runnable {
	public static final String CAPTURE_FILENAME = "pilogger320x240.png";
	public static final String DATE_PATERN = "EEEE d MMMM 'at' HH:mm:ss";
	public static final int WIDTH = 320;
	public static final int HEIGHT = 240;
	public DataChannel wifiLCD01VCCChannel = new DataChannel("Wifi LCD01 VCC", "LCD1_VCC");
	private WifiDisplayGUI gui;
	private ServerSocket serverServer;
	private byte[] byteToSend;
	private int[] pixList;
	private int alternate = 0;		// Sharp memory LCD Com invertion

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
	
	public static final int[][] thMap44 = {{1, 9, 3, 11},{13, 5, 15, 7},{4, 12, 2, 10},{16, 8, 14, 6}};

	public WifiDisplay(WifiDisplayGUI gui) throws IOException {
		this.gui = gui;
		serverServer = new ServerSocket(9999);
		
		wifiLCD01VCCChannel.setUnit("mV");
		
		// for debug
		if (PiloggerLauncher.simulation) {
			Timer t = new Timer();
			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					capture(false);
				}
			};
			t.schedule(tt, 1000, 5000);
		}
	}
	
	@Override
	public void run() {

		String line;
		BufferedReader inputStream;
		PrintStream outputStream;
		Socket clientSocket = null;

		try {
			while(true) {
				clientSocket = serverServer.accept();
				inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outputStream = new PrintStream(clientSocket.getOutputStream());

				while (true) {
					line = inputStream.readLine();
					if (line != null && line.contains("LCD")) {
						System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+
								" Wifi LCD requested an image: "+line);
						outputStream.write(capture(isNightTime()));
						alternate = alternate == 0 ? 1 : 0;
						String[] split = line.split(" ");
						if (split.length > 0) {
							int vcc = Integer.parseInt( split[1] );
							wifiLCD01VCCChannel.newData(vcc);
						}
						break;
					}
				}

				clientSocket.close();
			}
		} catch (IOException e) {
			System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+
					" Error WifiDisplay: ");
			e.printStackTrace();
		}

	}
	
	private boolean isNightTime() {
		int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		return ( hourOfDay >= 2 && hourOfDay < 4 );
	}

	private byte[] capture(boolean inverted) {
		// set the date of capture
		gui.getJlWifiTime().setText(new SimpleDateFormat(WifiDisplay.DATE_PATERN).format(new Date()));
		//capture the jpanel		
		final BufferedImage sourceImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = sourceImage.createGraphics();
		gui.paint(g);
		g.dispose();
		byte[] pixels = ((DataBufferByte) sourceImage.getRaster().getDataBuffer()).getData();

		// singed byte to 'unsigned' int
		pixList = new int[sourceImage.getWidth()*sourceImage.getHeight()];
		for (int i = 0; i < pixList.length; i++) {
			pixList[i] = pixels[i] < 0 ? 256 + pixels[i] : pixels[i];
		}

		//get dithered image
//		int[] pixDithered = getDitheredMonochromErrDiff();
		int[] pixDithered = getDitheredMonochromeOrdered();
		
		// save to file
		File outputfile = new File(ProbeManager.onlineFileLocalDirectory+CAPTURE_FILENAME);
		try {
			BufferedImage destImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster wr = destImage.getData().createCompatibleWritableRaster();
			wr.setPixels(0, 0, WIDTH, HEIGHT, pixDithered);
			destImage.setData(wr);
			ImageIO.write(destImage, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//generate image with pixel bit in bytes
		byte[] pixBytes = new byte[(WIDTH/8) * HEIGHT ];

		int mask = 0x01;
		int x, y;
		for (int i = 0; i < pixBytes.length; i++) {
			for (int j = 0; j < 8; j++) {
				mask = 0b10000000 >>> j;
				x = ((i%(WIDTH/8)*8 ) +j)  ;
				y = i / (WIDTH/8);
				if ( (pixDithered[x+(y*WIDTH)] == 0 && !inverted) || (pixDithered[x+(y*WIDTH)] > 0 && inverted) )
					pixBytes[i] = (byte) (pixBytes[i] | mask);
						
			}
		}

		byteToSend = new byte[(((WIDTH/8)+2)*HEIGHT)+2];
		int line = 0;
		int col = 0;
		for (int i = 0; i < byteToSend.length; i++) {
			if (i == 0) {
				byteToSend[i] = (byte) (0x80 + (alternate*0x40));
			} else if (i%42 == 0) {
				byteToSend[i] = (byte) 0x00;  //dummy byte
				line++;
				col = 0;
			} else if (i%42 == 1) {
				byteToSend[i] = Utils.reverseBitOrder( (byte) ((i/42)+1) );
			} else {
				byteToSend[i] =  pixBytes[(line*(WIDTH/8)) + col];
				col++;
			}
		}

		return byteToSend;
	}

	public int[] getDitheredMonochromeOrdered() {
		int[] pixDithered = new int[pixList.length];
		int pixel, x, y;
		
		for (int i=0; i<pixList.length; i++) {
			x = i%WIDTH;
			y = i/WIDTH;
			if (pixList[i]<85) pixDithered[i] = 0;
			if (pixList[i]>=85 && pixList[i]<170) pixDithered[i] = 255 * ((x+(y%2))%2);
			if (pixList[i]>= 170) pixDithered[i] = 255;
			
		}
		return pixDithered;
	}

	public int[] getDitheredMonochromErrDiff() {
		int pixelWithError, pixelDithered, error;
		boolean notLeft, notRight, notBottom;
		int[] pixDithered = new int[pixList.length];
		int min = 255, max = 0;
		double gain = 1;

		// search min-max
		for (int i = 0; i < pixList.length; i++) {
			if (pixList[i] > max) max = pixList[i];
			if (pixList[i] < min) min = pixList[i];
		}

		//limiting
		max = max<32 ? 32 : max;
		min = min>224 ? 224 : min;

		// calculate gain
		gain = 255.0/(max-min);		

		// normalise min-max to 0 - 255
		for (int i = 0; i < pixList.length; i++) {
			pixList[i] = (int) ( (pixList[i] - min)*gain) ;
			if(pixList[i]>255) pixList[i] = 255;
			if(pixList[i]<0) pixList[i] = 0;
		}

		//dithering
		for (int pixCount = 0; pixCount < pixList.length; pixCount++) {

			// are we on a corner ?
			notLeft = pixCount%WIDTH!=0;
			notBottom = pixCount < WIDTH*(HEIGHT-1);
			notRight = (pixCount+1)%WIDTH!=0;

			//error was propagated in the existing  pixDithered[] array
			pixelWithError = pixDithered[pixCount] + pixList[pixCount];

			// black or white
			if (pixelWithError < 128) pixelDithered = 0;
			else pixelDithered = 255;

			// set the actual pixel
			pixDithered[pixCount] = pixelDithered;

			// get the error of the aproximation
			error = pixelWithError - pixelDithered;

			// propagate error
			if (notRight) pixDithered[pixCount+1] += 7*error/16;
			if (notLeft && notBottom) pixDithered[pixCount+(WIDTH-1)] += 3*error/16;
			if (notBottom) pixDithered[pixCount+(WIDTH)] += 5*error/16;
			if (notRight && notBottom) pixDithered[pixCount+(WIDTH+1)] += 1*error/16;
		}

		return pixDithered;
	}

	@Override
	public DataChannel[] getChannels() {
		return new DataChannel[] {wifiLCD01VCCChannel};
	}

	@Override
	public JComponent[] getGuiComponents() {
		return null;
	}


}
