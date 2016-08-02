package tests;

import java.io.IOException;

import javax.swing.JComponent;

import pilogger.DataChannel;
import probes.AbstractProbe;


public class BMP085probeSimulation extends AbstractProbe{
	public DataChannel pressureChannel = new DataChannel("Atmospheric Pressure", "Atmospheric_Pressure");
	public DataChannel temperatureChannel = new DataChannel("Room Temperature", "Room_Temperature");

	/**
	 * BMP085 Pressure and Temperature probe on I2C bus
	 * @param bus Pi4J I2CBus object
	 * @throws IOException
	 */
	public BMP085probeSimulation() {
		pressureChannel.setUnit("Pa");
		temperatureChannel.setUnit("°C");
		DataSimulationThread dataThread = new DataSimulationThread();
		dataThread.start();
	}

	@Override
	public DataChannel[] getChannels() {
		return new DataChannel[]{pressureChannel, temperatureChannel};
	}

	private class DataSimulationThread extends Thread {
		private double temperature;
		private double pressure;
		private int dataCount;

		public DataSimulationThread() {
		}

		@Override
		public void run() {
			while (true) {

				temperature = 5 + (Math.random()*2) + (Math.sin(dataCount/(Math.PI*10)) *25.5);
				pressure = 95500 + (Math.random()*100) + (Math.cos(dataCount/(Math.PI*18)) *200);
				dataCount++;

				pressureChannel.newData(pressure);
				temperatureChannel.newData(temperature);
				temperature = 0;
				pressure = 0;

				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
		}
	}

	@Override
	public JComponent[] getGuiComponents() {
		return null;
	}

}
