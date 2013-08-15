package datachannel;

public class DataReceivedEvent {
	public double dataValue;
	public DataChannel channel;
	
	public DataReceivedEvent(double dataValue, DataChannel channel) {
		this.dataValue = dataValue;
		this.channel = channel;
	}

}
