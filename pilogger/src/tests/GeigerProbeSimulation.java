package tests;

import javax.swing.JComponent;

import pilogger.DataChannel;
import probes.AbstractProbe;

import com.pi4j.io.serial.SerialPortException;

public class GeigerProbeSimulation extends AbstractProbe {
	public DataChannel geigerChannel = new DataChannel("Backgound Radiation", "Backgound_Radiation");
	 
	/**
	 * Geiger Counter connected to the serial port
	 * @param serial Serial port com Pi4J object
	 * @throws SerialPortException
	 */
	public GeigerProbeSimulation() {
		geigerChannel.setUnit("µSv/h");
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
					sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}

		}
	}

	@Override
	public JComponent[] getGuiComponents() {
		// TODO Auto-generated method stub
		return null;
	}

}
