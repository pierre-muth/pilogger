package tests;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import datachannel.AbstractProbe;
import datachannel.DataChannel;
import datachannel.DataReceivedEvent;


public class BMP085probeSimulation extends AbstractProbe{
	public DataChannel pressureChannel = new DataChannel("Atmospheric Pressure", "Atmospheric_Pressure");
	public DataChannel temperatureChannel = new DataChannel("Room Temperature", "Room_Temperature");

	/**
	 * BMP085 Pressure and Temperature probe on I2C bus
	 * @param bus Pi4J I2CBus object
	 * @throws IOException
	 */
	public BMP085probeSimulation() {
		DataSimulationThread dataThread = new DataSimulationThread();
		dataThread.start();
	}

	@Override
	public DataChannel[] getChannels() {
		return new DataChannel[]{pressureChannel, temperatureChannel};
	}

	private class DataSimulationThread extends Thread {
		public static final int OVER_SAMPLING = 5;
		private double temperatureSum;
		private double pressureSum;
		private int dataCount;

		public DataSimulationThread() {
		}

		@Override
		public void run() {
			int rawTemperature;
			int msb, lsb, xlsb;
			int rawPressure;
			while (true) {

				temperatureSum += 25 + (Math.random()*2);
				pressureSum += 95500 + (Math.random()*10);
				dataCount++;

				if (dataCount >= OVER_SAMPLING) {
					pressureChannel.newData(pressureSum/OVER_SAMPLING);
					temperatureChannel.newData(temperatureSum/OVER_SAMPLING);
					temperatureSum = 0;
					pressureSum = 0;
					dataCount = 0;
				}

				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}

		}
	}

}
