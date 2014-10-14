package probes;

import java.lang.management.ManagementFactory;

import javax.swing.JComponent;

import pilogger.DataChannel;

public class SystemProbe extends AbstractProbe {

	private DataChannel loadChannel = new DataChannel("System Load", "System_Load");
	private DataChannel[] channels = new DataChannel[]{loadChannel};

	public SystemProbe() {
		SystemInfoReaderThread systemInfoReaderThread = new SystemInfoReaderThread();
		systemInfoReaderThread.start();
	}

	@Override
	public DataChannel[] getChannels() {
		return channels;
	}

	private class SystemInfoReaderThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					loadChannel.newData( ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() );
					sleep(3000);
				} catch (Exception e) {
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
