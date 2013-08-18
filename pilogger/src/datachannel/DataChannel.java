package datachannel;

import java.util.ArrayList;
import java.util.Date;

import cern.jdve.data.DefaultDataSet;

/**
 * @author pfreyerm
 */
public class DataChannel {
	public String channelName;
	public String unit = "";
	private ArrayList<DataChannelListener> dataListenersList = new ArrayList<>();
	//TODO consider ShiftingDataSet
	private DefaultDataSet realTimeDataSet;
	private DefaultDataSet dayDataSet;
	private DefaultDataSet monthDataSet;
	private DefaultDataSet yearDataSet;

	/**
	 *  DataChannel own dataSet of 4 different time scale
	 *  and fire DataReceivedEvent when newData()
	 * @param uniqueChannelName 
	 */
	public DataChannel(String uniqueChannelName) {
		channelName = uniqueChannelName;
		realTimeDataSet = new DefaultDataSet(channelName+" Real Time");
		dayDataSet = new DefaultDataSet(channelName+" Day");
		monthDataSet = new DefaultDataSet(channelName+" Month");
		yearDataSet = new DefaultDataSet(channelName+"Year");
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public void addDataChannelListener(DataChannelListener listener) {
		dataListenersList.add(listener);
	}
	public void removeDataChannelListener(DataChannelListener listener) {
		dataListenersList.remove(listener);
	}
	public void clearDataChannelListener() {
		dataListenersList.clear();
	}
	
	public void newData(double data) {
		processNewData(data);
		DataReceivedEvent event = new DataReceivedEvent(data);
		fireDataEvent(event);
	}

	public DefaultDataSet getRealTimeDataSet() {
		return realTimeDataSet;
	}

	public DefaultDataSet getDayDataSet() {
		return dayDataSet;
	}

	public DefaultDataSet getMonthDataSet() {
		return monthDataSet;
	}

	public DefaultDataSet getYearDataSet() {
		return yearDataSet;
	}
	protected void fireDataEvent(DataReceivedEvent dataReceivedEvent) {
		for (DataChannelListener dataListener : dataListenersList) {
			dataReceivedEvent.channel = this;
			dataListener.dataReceived(dataReceivedEvent);
		}
	}
	
	private void processNewData(double data) {
		// TODO compute time and update dataset
		double time = System.currentTimeMillis();
		getRealTimeDataSet().add(time, data);
	}
	

}
