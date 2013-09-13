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
	public static final String fileDirectory = "/home/pi/project/pilogger/logs/";
	
	public String channelName;
	public String unit = "";
	private ArrayList<DataChannelListener> dataListenersList = new ArrayList<>();

	private static final int BUFFER_LENGTH = 300;
	
	// 30 sec
	private static final int DAY_AVERAGING_TIME = 30000;
	
	
	
	public ShiftingDataSet realTimeDataSet;
	
	public ShiftingDataSet hourDataSet;
	public ShiftingDataSet hourMaxDataSet;
	public ShiftingDataSet hourMinDataSet;
	
	public ShiftingDataSet dayDataSet;
	public ShiftingDataSet dayMaxDataSet;
	public ShiftingDataSet dayMinDataSet;

	public ShiftingDataSet monthDataSet;
	public ShiftingDataSet monthMaxDataSet;
	public ShiftingDataSet monthMinDataSet;
	
	public ShiftingDataSet yearDataSet;
	public ShiftingDataSet yearMaxDataSet;
	public ShiftingDataSet yearMinDataSet;
	
	private AveragingTask averagingTask;
	
	/**
	 *  DataChannel own dataSet of 4 different time scale
	 *  and fire DataReceivedEvent when newData()
	 * @param uniqueChannelName 
	 */
	public DataChannel(String uniqueChannelName) {
		channelName = uniqueChannelName;
		realTimeDataSet = new ShiftingDataSet(channelName+" Real Time", BUFFER_LENGTH, true);
		
		hourDataSet = new ShiftingDataSet(channelName+" Hour", BUFFER_LENGTH, true);
		hourMaxDataSet = new ShiftingDataSet(channelName+" Hour max", BUFFER_LENGTH, true);
		hourMinDataSet = new ShiftingDataSet(channelName+" Hour min", BUFFER_LENGTH, true);
		
		dayDataSet = new ShiftingDataSet(channelName+" Day", BUFFER_LENGTH, true);
		dayMaxDataSet = new ShiftingDataSet(channelName+" Day max", BUFFER_LENGTH, true);
		dayMinDataSet = new ShiftingDataSet(channelName+" Day min", BUFFER_LENGTH, true);

		monthDataSet = new ShiftingDataSet(channelName+" Month", BUFFER_LENGTH, true);
		monthMaxDataSet = new ShiftingDataSet(channelName+" Month max", BUFFER_LENGTH, true);
		monthMinDataSet = new ShiftingDataSet(channelName+" Month min", BUFFER_LENGTH, true);
		
		yearDataSet = new ShiftingDataSet(channelName+"Year", BUFFER_LENGTH, true);
		yearMaxDataSet = new ShiftingDataSet(channelName+"Year max", BUFFER_LENGTH, true);
		yearMinDataSet = new ShiftingDataSet(channelName+"Year min", BUFFER_LENGTH, true);
		
		Timer t = new Timer();
		averagingTask = new AveragingTask();
		t.schedule(averagingTask, DAY_AVERAGING_TIME, DAY_AVERAGING_TIME);
		
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
		double time = System.currentTimeMillis();
		realTimeDataSet.add(time, data);
		averagingTask.addPoint(data);
	}
	
	private class AveragingTask extends TimerTask{
		private double sum, count;
		private double min, max;
		
		public AveragingTask() {
			initVariables();
		}
		
		@Override
		public void run() {

			if (count > 0) {
				double time = System.currentTimeMillis();
				double av = sum/count;
				hourDataSet.add(time, av);
				hourMaxDataSet.add(time, max);
				hourMinDataSet.add(time, min);
			}
			
			initVariables();
		}
		
		public void addPoint(double dataPoint) {
			sum += dataPoint;
			count++;
			if (dataPoint < min) min = dataPoint;
			if (dataPoint > max) max = dataPoint;
		}
		
		private void initVariables () {
			sum = 0;
			count = 0;
			min = Double.POSITIVE_INFINITY;
			max = Double.NEGATIVE_INFINITY;
		}
		
		private void savePoint(double time, double data) {
			
		}
		
	}
}
