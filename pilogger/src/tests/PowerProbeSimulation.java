package tests;

import javax.swing.JComponent;

import pilogger.DataChannel;
import probes.AbstractProbe;

public class PowerProbeSimulation extends AbstractProbe {
	public DataChannel powerChannel = new DataChannel("inst. power consumption", "power_consumption");

	public PowerProbeSimulation(){
		powerChannel.setUnit("Watt");
		DataSimulationThread dataThread = new DataSimulationThread();
		dataThread.start();
		
	}
	@Override
	public DataChannel[] getChannels() {
		return new DataChannel[] {powerChannel};
	}

	@Override
	public JComponent[] getGuiComponents() {
		return null;
	}
	
	private class DataSimulationThread extends Thread {
		private int dataCount;
		public DataSimulationThread() {
		}

		@Override
		public void run() {
			while (true) {
				powerChannel.newData(250 + Math.random()*3 + (Math.cos(dataCount/10) *200 ));
				dataCount++;
				try {
					sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}

		}
	}

}
