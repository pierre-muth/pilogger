package datachannel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cern.jdve.data.ShiftingDataSet;

/**
 * @author pfreyerm
 */
public class DataChannel {
	public static final String fileDirectory = "/home/pi/projects/pilogger/logs/";
	
	public String channelName;
	public String unit = "";
	private ArrayList<DataChannelListener> dataListenersList = new ArrayList<>();

	private static final int CHART_BUFFER_LENGTH = 300;
	
	private static final int MS_TO_HOUR_POINT = 15000;
	private static final int HOUR_POINTS_TO_DAY_POINT = 20;
	
	
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
	private File logFile;
	private BufferedWriter logFileWriter;
	
	/**
	 *  DataChannel own dataSet of 4 different time scale
	 *  and fire DataReceivedEvent when newData()
	 * @param uniqueChannelName 
	 */
	public DataChannel(String uniqueChannelName) {
		channelName = uniqueChannelName;
		realTimeDataSet = new ShiftingDataSet(channelName+" Real Time", CHART_BUFFER_LENGTH, true);
		
		hourDataSet = new ShiftingDataSet(channelName+" Hour", CHART_BUFFER_LENGTH, true);
		hourMaxDataSet = new ShiftingDataSet(channelName+" Hour max", CHART_BUFFER_LENGTH, true);
		hourMinDataSet = new ShiftingDataSet(channelName+" Hour min", CHART_BUFFER_LENGTH, true);
		
		dayDataSet = new ShiftingDataSet(channelName+" Day", CHART_BUFFER_LENGTH, true);
		dayMaxDataSet = new ShiftingDataSet(channelName+" Day max", CHART_BUFFER_LENGTH, true);
		dayMinDataSet = new ShiftingDataSet(channelName+" Day min", CHART_BUFFER_LENGTH, true);

		monthDataSet = new ShiftingDataSet(channelName+" Month", CHART_BUFFER_LENGTH, true);
		monthMaxDataSet = new ShiftingDataSet(channelName+" Month max", CHART_BUFFER_LENGTH, true);
		monthMinDataSet = new ShiftingDataSet(channelName+" Month min", CHART_BUFFER_LENGTH, true);
		
		yearDataSet = new ShiftingDataSet(channelName+"Year", CHART_BUFFER_LENGTH, true);
		yearMaxDataSet = new ShiftingDataSet(channelName+"Year max", CHART_BUFFER_LENGTH, true);
		yearMinDataSet = new ShiftingDataSet(channelName+"Year min", CHART_BUFFER_LENGTH, true);
		
		
		Path piloggerDir = Paths.get(fileDirectory);
        Path logFilePath = piloggerDir.resolve(channelName+".csv");
		
		try {
			logFileWriter = Files.newBufferedWriter(logFilePath, Charset.defaultCharset(), new OpenOption[] {
				StandardOpenOption.APPEND, StandardOpenOption.CREATE});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Timer t = new Timer();
		averagingTask = new AveragingTask();
		t.schedule(averagingTask, MS_TO_HOUR_POINT, MS_TO_HOUR_POINT);
		
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
	
	private void processAveragedData(AveragedDataPoint averagedDataPoint) {
		hourDataSet.add(averagedDataPoint.time, averagedDataPoint.value);
		hourMaxDataSet.add(averagedDataPoint.time, averagedDataPoint.max);
		hourMinDataSet.add(averagedDataPoint.time, averagedDataPoint.min);
		
		if (logFileWriter != null) {
			try {
				logFileWriter.write(Double.toString(averagedDataPoint.time)
						+", "
						+Double.toString(averagedDataPoint.value)
						+", "
						+Double.toString(averagedDataPoint.max)
						+", "
						+Double.toString(averagedDataPoint.min)
						+"\n");
				logFileWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
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
				AveragedDataPoint averagedDataPoint = new AveragedDataPoint(time, av, min, max);
				DataChannel.this.processAveragedData(averagedDataPoint);
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
		
	}
	
	public class AveragedDataPoint {
		public double time;
		public double value;
		public double min;
		public double max;
		
		public AveragedDataPoint() {
		}
		public AveragedDataPoint(double time, double value, double min, double max) {
			this.time = time;
			this.value = value;
			this.max = max;
			this.min = min;
		}
	}
}
