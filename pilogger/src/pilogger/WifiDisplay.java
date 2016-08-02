package pilogger;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class WifiDisplay implements Runnable {
	public static final String CAPTURE_FILENAME = "pilogger320x240.png";
	public static final int WIDTH = 320;
	public static final int HEIGHT = 240;
	private JPanel gui;
	private ServerSocket serverServer;
	private byte[] byteToSend;
	private int[] pixList;

	public WifiDisplay(JPanel gui) throws IOException {
		this.gui = gui;
		serverServer = new ServerSocket(9999);
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
					if (line.contains("LCD")) {
						System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+
								" Wifi LCD requested an image");
						outputStream.write(capture());
						break;
					}
				}

				clientSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private byte[] capture() {
		//capture the jpanel		
		final BufferedImage sourceImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = sourceImage.createGraphics();
		gui.paint(g);
		g.dispose();
		byte[] pixels = ((DataBufferByte) sourceImage.getRaster().getDataBuffer()).getData();
		
		
		File outputfile = new File(ProbeManager.onlineFileLocalDirectory+CAPTURE_FILENAME);
	    try {
			ImageIO.write(sourceImage, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pixList = new int[sourceImage.getWidth()*sourceImage.getHeight()];
		for (int i = 0; i < pixList.length; i++) {
			pixList[i] = pixels[i] < 0 ? 256 + pixels[i] : pixels[i];
		}
		
		//get dithered image
		int[] pixDithered = getDitheredMonochrom();

		//generate image with pixel bit in bytes
		byte[] pixBytes = new byte[(WIDTH/8) * HEIGHT ];

		int mask = 0x01;
		int x, y;
		for (int i = 0; i < pixBytes.length; i++) {
			for (int j = 0; j < 8; j++) {
				mask = 0b10000000 >>> j;
				x = ((i%(WIDTH/8)*8 ) +j)  ;
				y = i / (WIDTH/8);
				if ( pixDithered[x+(y*WIDTH)] == 0 ) {
					pixBytes[i] = (byte) (pixBytes[i] | mask);
				}
			}
		}

		byteToSend = new byte[(((WIDTH/8)+2)*HEIGHT)+2];
		int line = 0;
		int col = 0;
		for (int i = 0; i < byteToSend.length; i++) {
			if (i == 0) {
				byteToSend[i] = (byte) 0x80;
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
	
	public int[] getDitheredMonochrom() {
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


}
