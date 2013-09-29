package datachannel;

import java.io.BufferedReader;
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
	public static final String logFileDirectory = "/home/pi/projects/pilogger/logs/";
	public static final String onlineFileDirectory = "/home/pi/projects/pilogger/logs/online/";
	
	
	public String channelName;
	private String unit = "";
	
	private String logFileName = "";
	private ArrayList<DataChannelListener> dataListenersList = new ArrayList<>();

	private static final int CHART_BUFFER_LENGTH = 300;
	
	private static final int MS_TO_HOUR_POINT = 15000;
	private static final int HOUR_POINTS_TO_DAY_POINT = 20;
	private static final int DAY_POINTS_TO_MONTH_POINT = 30;
	private static final int MONTH_POINTS_TO_YEAR_POINT = 12;
	
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
	private BufferedWriter logFileWriter;
	
	private double daySum = 0;
	private double dayMin = Double.POSITIVE_INFINITY, dayMax = Double.NEGATIVE_INFINITY;
	private int dayCount = 0;
	
	private double monthSum = 0;
	private double monthMin = Double.POSITIVE_INFINITY, monthMax = Double.NEGATIVE_INFINITY;
	private int monthCount = 0;
	
	private double yearSum = 0;
	private double yearMin = Double.POSITIVE_INFINITY, yearMax = Double.NEGATIVE_INFINITY;
	private int yearCount = 0;
	
	/**
	 *  DataChannel own dataSet of 4 different time scale
	 *  and fire DataReceivedEvent when newData()
	 * @param uniqueChannelName 
	 */
	public DataChannel(String uniqueChannelName, String logFileName) {
		this.channelName = uniqueChannelName;
		this.logFileName = logFileName;
		realTimeDataSet = new ShiftingDataSet(channelName+"Real Time", CHART_BUFFER_LENGTH, true);
		
		hourDataSet = new ShiftingDataSet(channelName+"Hour", CHART_BUFFER_LENGTH, true);
		hourMaxDataSet = new ShiftingDataSet(channelName+" Hour max", CHART_BUFFER_LENGTH, true);
		hourMinDataSet = new ShiftingDataSet(channelName+" Hour min", CHART_BUFFER_LENGTH, true);
		
		dayDataSet = new ShiftingDataSet(channelName+"Day", CHART_BUFFER_LENGTH, true);
		dayMaxDataSet = new ShiftingDataSet(channelName+" Day max", CHART_BUFFER_LENGTH, true);
		dayMinDataSet = new ShiftingDataSet(channelName+" Day min", CHART_BUFFER_LENGTH, true);

		monthDataSet = new ShiftingDataSet(channelName+"Month", CHART_BUFFER_LENGTH, true);
		monthMaxDataSet = new ShiftingDataSet(channelName+" Month max", CHART_BUFFER_LENGTH, true);
		monthMinDataSet = new ShiftingDataSet(channelName+" Month min", CHART_BUFFER_LENGTH, true);
		
		yearDataSet = new ShiftingDataSet(channelName+"Year", CHART_BUFFER_LENGTH, true);
		yearMaxDataSet = new ShiftingDataSet(channelName+"Year max", CHART_BUFFER_LENGTH, true);
		yearMinDataSet = new ShiftingDataSet(channelName+"Year min", CHART_BUFFER_LENGTH, true);
		
		
		Path piloggerDir = Paths.get(logFileDirectory);
        Path logFilePath = piloggerDir.resolve(logFileName+".csv");
        
        try {
			loadLogFile(logFilePath);
		} catch (IOException e1) {
			System.out.println("\n No "+logFileName+".csv found in "+logFileDirectory+".");
		}
        
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
	public String getLogFileName() {
		return logFileName;
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
		
		processNewHourDataForDayDataSets(averagedDataPoint);
		
		logAveragedDataToFile(averagedDataPoint);
	}
	
	private void processNewHourDataForDayDataSets(AveragedDataPoint averagedDataPoint) {
		daySum += averagedDataPoint.value;
		if (averagedDataPoint.value < dayMin) dayMin = averagedDataPoint.value;
		if (averagedDataPoint.value > dayMax) dayMax = averagedDataPoint.value;
		dayCount++;
		if (dayCount > HOUR_POINTS_TO_DAY_POINT) {
			dayDataSet.add(averagedDataPoint.time, daySum/dayCount);
			dayMinDataSet.add(averagedDataPoint.time, dayMin);
			dayMaxDataSet.add(averagedDataPoint.time, dayMax);
			
			averagedDataPoint = new AveragedDataPoint(averagedDataPoint.time, daySum/dayCount, dayMin, dayMax);
			processNewDayData(averagedDataPoint);
			
			daySum = 0;
			dayMin = Double.POSITIVE_INFINITY;
			dayMax = Double.NEGATIVE_INFINITY;
			dayCount = 0;
			
		}
	}
	
	private void processNewDayData(AveragedDataPoint averagedDataPoint) {
		monthSum += averagedDataPoint.value;
		if (averagedDataPoint.value < monthMin) monthMin = averagedDataPoint.value;
		if (averagedDataPoint.value > monthMax) monthMax = averagedDataPoint.value;
		monthCount++;
		if (monthCount > DAY_POINTS_TO_MONTH_POINT) {
			monthDataSet.add(averagedDataPoint.time, monthSum/monthCount);
			monthMinDataSet.add(averagedDataPoint.time, monthMin);
			monthMaxDataSet.add(averagedDataPoint.time, monthMax);
			
			averagedDataPoint = new AveragedDataPoint(averagedDataPoint.time, monthSum/monthCount, monthMin, monthMax);
			processNewMonthData(averagedDataPoint);
			
			monthSum = 0;
			monthMin = Double.POSITIVE_INFINITY;
			monthMax = Double.NEGATIVE_INFINITY;
			monthCount = 0;
			
		}
	}

	private void processNewMonthData(AveragedDataPoint averagedDataPoint) {
		yearSum += averagedDataPoint.value;
		if (averagedDataPoint.value < yearMin) yearMin = averagedDataPoint.value;
		if (averagedDataPoint.value > yearMax) yearMax = averagedDataPoint.value;
		yearCount++;
		if (yearCount > MONTH_POINTS_TO_YEAR_POINT) {
			yearDataSet.add(averagedDataPoint.time, yearSum/yearCount);
			yearMinDataSet.add(averagedDataPoint.time, yearMin);
			yearMaxDataSet.add(averagedDataPoint.time, yearMax);
			
			yearSum = 0;
			yearMin = Double.POSITIVE_INFINITY;
			yearMax = Double.NEGATIVE_INFINITY;
			yearCount = 0;
		}
	}
	
	private void logAveragedDataToFile(AveragedDataPoint averagedDataPoint) {
		if (logFileWriter == null) return;
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
	
	private void loadLogFile(Path logFilePath) throws IOException {
			System.out.print("\n Now loading logged data : "+ logFilePath.getFileName());
		
			BufferedReader logFileReader = Files.newBufferedReader(logFilePath, Charset.defaultCharset());
			String line;
			String[] elements;
			int count = 0;
			
			double time = Double.NaN, value = Double.NaN, min = Double.NaN, max = Double.NaN;
			
			while ((line = logFileReader.readLine()) != null) {
				elements = line.split(", ");
				if (elements.length > 0) time = Double.parseDouble(elements[0]);
				if (elements.length > 1) value = Double.parseDouble(elements[1]);
				if (elements.length > 2) max = Double.parseDouble(elements[2]);
				if (elements.length > 3) min = Double.parseDouble(elements[3]);
			
				AveragedDataPoint averagedDataPoint = new AveragedDataPoint(time, value, min, max);
				processAveragedData(averagedDataPoint);
				count++;
				if (count % 2000 == 0) System.out.print(".");
			}
			System.out.print(" Ok");
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
