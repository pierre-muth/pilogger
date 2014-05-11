package probes;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.swing.JComponent;

import pilogger.DataChannel;

import com.pi4j.system.SystemInfo;

public class SystemProbe extends AbstractProbe {

	private DataChannel memoryChannel = new DataChannel("System Memory", "System_Memory");
	private DataChannel cpuTempChannel = new DataChannel("CPU temperature", "CPU_temp");
	private DataChannel loadChannel = new DataChannel("System Load", "System_Load");
	private DataChannel[] channels = new DataChannel[]{memoryChannel, loadChannel, cpuTempChannel};

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
					memoryChannel.newData( SystemInfo.getMemoryUsed() );
					cpuTempChannel.newData( SystemInfo.getCpuTemperature() );
					loadChannel.newData( ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() );
					sleep(5000);
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
