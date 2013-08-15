package datachannel;

public class DataChannel {
	public String channelName;
	public String unit = "";
	
	public DataChannel(String channelName) {
		this.channelName = channelName;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	

}
