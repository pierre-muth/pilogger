package tests;

import com.pi4j.io.serial.SerialPortException;

import datachannel.AbstractProbe;
import datachannel.DataChannel;

public class GeigerProbeSimulation extends AbstractProbe {
	public static final int GEIGER_SERIAL_SPEED = 9600;
	public DataChannel geigerChannel = new DataChannel("Backgound Radiation");
	private String inbuf = "";
	 
	/**
	 * Geiger Counter connected to the serial port
	 * @param serial Serial port com Pi4J object
	 * @throws SerialPortException
	 */
	public GeigerProbeSimulation() {
		DataSimulationThread dataThread = new DataSimulationThread();
		dataThread.start();
	}

	

	@Override
	public DataChannel[] getChannels() {
		return new DataChannel[] {geigerChannel};
	}
	
	private class DataSimulationThread extends Thread {

		public DataSimulationThread() {
		}

		@Override
		public void run() {
			while (true) {
				geigerChannel.newData(Math.random()*0.3);
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}

		}
	}

	
}
