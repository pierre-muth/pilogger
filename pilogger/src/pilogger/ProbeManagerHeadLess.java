package pilogger;

import java.util.ArrayList;
import java.util.Timer;

import probes.AbstractProbe;

public class ProbeManagerHeadLess implements ProbeManager {
	
	private ArrayList<DataChannel> channelsList = new ArrayList<>();
	
	
	public ProbeManagerHeadLess() {
		Timer timerIP = new Timer();
		timerIP.schedule(new IPfileDescriptor(this), 30000, (60*1000));
		
		Timer timerHTML = new Timer();
		timerHTML.schedule(new HTMLgenerator(this), 10000, (60*1000));
	}
	

	@Override
	public void addProbe(AbstractProbe probe) {

		for (int i = 0; i < probe.getChannels().length; i++) {
			channelsList.add( probe.getChannels()[i] ); 
			
		}
		
	}


	@Override
	public DataChannel[] getChannels() {
		DataChannel[] channels = new DataChannel[channelsList.size()];
		return channelsList.toArray(channels);
	}

	
}
