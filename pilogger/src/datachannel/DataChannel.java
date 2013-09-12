package datachannel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cern.jdve.data.DataSet;
import cern.jdve.data.DefaultDataSet;
import cern.jdve.data.ShiftingDataSet;

/**
 * @author pfreyerm
 */
public class DataChannel {
	public String channelName;
	public String unit = "";
	private ArrayList<DataChannelListener> dataListenersList = new ArrayList<>();

	private static final int REALTIME_BUFFER_LENGTH = 3600;
	private static final int DAY_BUFFER_LENGTH = 2880;
	
	private static final int DAY_AVERAGING_TIME = 30000;
	
	
	public ShiftingDataSet realTimeDataSet;
	
	public ShiftingDataSet dayDataSet;
	public ShiftingDataSet dayMaxDataSet;
	public ShiftingDataSet dayMinDataSet;
	private DataSet[] dayDataSets = {dayDataSet, dayMaxDataSet, dayMinDataSet};	

	public DefaultDataSet monthDataSet;
	public DefaultDataSet monthMaxDataSet;
	public DefaultDataSet monthMinDataSet;
	private DataSet[] monthDataSets = {monthDataSet, monthMaxDataSet, monthMinDataSet};
	
	public DefaultDataSet yearDataSet;
	public DefaultDataSet yearMaxDataSet;
	public DefaultDataSet yearMinDataSet;
	private DataSet[] yearDataSets = {yearDataSet, yearMaxDataSet, yearMinDataSet};
	
	/**
	 *  DataChannel own dataSet of 4 different time scale
	 *  and fire DataReceivedEvent when newData()
	 * @param uniqueChannelName 
	 */
	public DataChannel(String uniqueChannelName) {
		channelName = uniqueChannelName;
		realTimeDataSet = new ShiftingDataSet(channelName+" Real Time", REALTIME_BUFFER_LENGTH, true);
		
		dayDataSet = new ShiftingDataSet(channelName+" Day", DAY_BUFFER_LENGTH, true);
		dayMaxDataSet = new ShiftingDataSet(channelName+" Day max", DAY_BUFFER_LENGTH, true);
		dayMinDataSet = new ShiftingDataSet(channelName+" Day min", DAY_BUFFER_LENGTH, true);

		monthDataSet = new DefaultDataSet(channelName+" Month");
		monthMaxDataSet = new DefaultDataSet(channelName+" Month max");
		monthMinDataSet = new DefaultDataSet(channelName+" Month min");
		
		yearDataSet = new DefaultDataSet(channelName+"Year");
		yearMaxDataSet = new DefaultDataSet(channelName+"Year max");
		yearMinDataSet = new DefaultDataSet(channelName+"Year min");
		
		Timer t = new Timer();
		AveragingTask dayTask = new AveragingTask(dayDataSets);
		t.schedule(dayTask, 2000, DAY_AVERAGING_TIME);
		
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

	protected void fireDataEvent(DataReceivedEvent dataReceivedEvent) {
		for (DataChannelListener dataListener : dataListenersList) {
			dataReceivedEvent.channel = this;
			dataListener.dataReceived(dataReceivedEvent);
		}
	}
	
	private void processNewData(double data) {
		// TODO compute time and update dataset real time
		// send data point to averaging task
		
		double time = System.currentTimeMillis();
		realTimeDataSet.add(time, data);
	}
	
	private class AveragingTask extends TimerTask{
		public double sum, count;
		private DataSet avergeDataSet;
		private DataSet maxDataSet;
		private DataSet minDataSet;
		
		//TODO don't need to pass datasets as argument.
		
		public AveragingTask(DataSet[] dataSets) {
			this.avergeDataSet = dataSets[0];
			this.maxDataSet = dataSets[1];
			this.minDataSet = dataSets[2];
		}
		
		@Override
		public void run() {
			System.out.println("DayAveraging: "+this);
			
			
		}
		
	}
}
