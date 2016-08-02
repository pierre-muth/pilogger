package pilogger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import cern.jdve.data.DataSet;
import cern.jdve.data.DefaultDataSet;
import cern.jdve.data.ShiftingDataSet;

/**
 * @author pfreyerm
 */
public class DataChannel {
	public static final String logFileDirectory = "/home/pi/projects/pilogger/logs/";
	public static final String logFileDirectorySimulation = "c:\\pilogger\\logs\\online\\";

	private Path logFilePath;
	
	private boolean uploadOnLine = true;

	public String channelName;
	private String unit = "";

	private String logFileName = "";

	private static final int CHART_BUFFER_LENGTH = 400;

	private static final int MS_TO_HOUR_POINT = 15000;
	private static final int HOUR_POINTS_TO_DAY_POINT = 15;
	private static final int DAY_POINTS_TO_MONTH_POINT = 30;
	private static final int MONTH_POINTS_TO_YEAR_POINT = 12;
	
	public static final String REALTIME_SUFIX = "Realtime";
	public static final String HOUR_SUFIX = "Hour";
	public static final String DAY_SUFIX = "Day";
	public static final String MONTH_SUFIX = "Month";
	public static final String YEAR_SUFIX = "Year";
	public static final String LONGRANGE_SUFIX = "Longrange";

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
	public DefaultDataSet longRangeDataSet;
	public DefaultDataSet longRangeMaxDataSet;
	public DefaultDataSet longRangeMinDataSet;

	private AveragingTask averagingTask;

	private double daySum = 0; 
	private double dayTimeSum = 0;
	private double dayMin = Double.POSITIVE_INFINITY, dayMax = Double.NEGATIVE_INFINITY;
	private int dayCount = 0;

	private double monthSum = 0; 
	private double monthTimeSum = 0;
	private double monthMin = Double.POSITIVE_INFINITY, monthMax = Double.NEGATIVE_INFINITY;
	private int monthCount = 0;

	private double yearSum = 0;
	private double yearTimeSum = 0;
	private double yearMin = Double.POSITIVE_INFINITY, yearMax = Double.NEGATIVE_INFINITY;
	private int yearCount = 0;

	private double longRangeSum = 0;
	private double longRangeTimeSum = 0;
	private double longRangeMin = Double.POSITIVE_INFINITY, longRangeMax = Double.NEGATIVE_INFINITY;
	private int longRangeCount = 0;
	private int dayOfLastLongRangePoint = -1;
	
	private double previousData = Double.NaN;
	
	private double dataRangeMax = Double.POSITIVE_INFINITY;
	private double dataRangeMin = Double.NEGATIVE_INFINITY;

	private JButton blinkButton;
	private JButton reloadButton;
	private ChannelPanel channelPanel;
	
	private AtomicBoolean isRecording = new AtomicBoolean(true);
	private AtomicBoolean isFileLoading = new AtomicBoolean(false);
	private boolean isDifferential = false;
	
	private AveragedDataPoint lastAveragedDataPoint;

	/**
	 *  DataChannel own dataSet of 6 different time scale
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

		longRangeDataSet = new DefaultDataSet(channelName+"All");
		longRangeMaxDataSet = new DefaultDataSet(channelName+"All max");
		longRangeMinDataSet = new DefaultDataSet(channelName+"All min");

		Path piloggerDir;
		if (PiloggerLauncher.simulation) piloggerDir = Paths.get(logFileDirectorySimulation);
		else piloggerDir = Paths.get(logFileDirectory);
		
		logFilePath = piloggerDir.resolve(this.logFileName+".csv");

		loadDataSetLogFile();

		Timer t = new Timer();
		averagingTask = new AveragingTask();
		t.schedule(averagingTask, MS_TO_HOUR_POINT, MS_TO_HOUR_POINT);

	}
	
	public DataChannel(String uniqueChannelName, String logFileName, boolean isDifferencial) {
		this(uniqueChannelName, logFileName);
		this.isDifferential = isDifferencial;
	}
	
	public void setDifferentialMode(boolean isDiff) {
		this.isDifferential = isDiff;
	}
	
	public void setDataRange(double min, double max) {
		this.dataRangeMin = min;
		this.dataRangeMax = max;
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

	public void newData(double data) {
		if (isFileLoading.get()) return;
		
		if (isDifferential) {
			if (Double.isNaN(previousData)) {
				previousData = data;
				data = 0;
			} else {
				double substract = data - previousData;
				previousData = data;
				data = substract;
			}
		}
		
		if (data >= dataRangeMin && data <= dataRangeMax) {
			processNewData(data);
			if (channelPanel != null){		// could be null if we haven't put on the wifi panel
				channelPanel.setValue(data);
			}
		}
		
		Blinker b = new Blinker();
		b.start();
		
		
	}
	
	public ChannelPanel getChannelPanel(){
		if (channelPanel == null){
			channelPanel = new ChannelPanel(this);
		}
		return channelPanel;
	}

	public JComponent getChannelButton() {
		if (blinkButton == null) {
			blinkButton = new JButton();
			blinkButton.setBorder(new LineBorder(Color.gray));
			blinkButton.setBackground(Color.black);
			blinkButton.setPreferredSize(new Dimension(8, 6));
			blinkButton.setToolTipText(channelName);
			blinkButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setRecording(! isRecording.get()); 
				}
			});
		}
		return blinkButton;
	}
	
	public JButton getReloadButton() {
		if (reloadButton == null) {
			reloadButton = new JButton();
			reloadButton.setBorder(new LineBorder(Color.gray));
			reloadButton.setBackground(Color.black);
			reloadButton.setForeground(Color.white);
			reloadButton.setPreferredSize(new Dimension(150, 12));
			reloadButton.setFont(PiloggerGUI.labelFont);
			reloadButton.setToolTipText("Reload from file");
			reloadButton.setText(channelName);
			reloadButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					loadLogFile(logFilePath);
				}
			});
		}
		return reloadButton;
	}
	
	public void setRecording(boolean rec) {
		isRecording.set(rec);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (isRecording.get()) getChannelButton().setBorder(new LineBorder(Color.gray));
				else getChannelButton().setBorder(new LineBorder(Color.darkGray));
			}
		});
	}

	public AveragedDataPoint getLastAveragedDataPoint() {
		return lastAveragedDataPoint;
	}
	
	private void processNewData(double data) {
		long time = System.currentTimeMillis();
		realTimeDataSet.add(time, data);
		if (isRecording.get()) 
			averagingTask.addPoint(time, data);
	}

	private void processAveragedData(AveragedDataPoint averagedDataPoint, boolean isFromFile, boolean isNewData) {
		hourDataSet.add(averagedDataPoint.time, averagedDataPoint.value);
		hourMaxDataSet.add(averagedDataPoint.time, averagedDataPoint.max);
		hourMinDataSet.add(averagedDataPoint.time, averagedDataPoint.min);
		
		lastAveragedDataPoint = averagedDataPoint;
		
		boolean rejectPoint = false;
		if (averagedDataPoint.min < dataRangeMin || averagedDataPoint.min > dataRangeMax) rejectPoint = true;
		if (averagedDataPoint.max < dataRangeMin || averagedDataPoint.max > dataRangeMax) rejectPoint = true;
		if (averagedDataPoint.value < dataRangeMin || averagedDataPoint.value > dataRangeMax) rejectPoint = true;
		
		if (!rejectPoint) {
			processNewHourDataForDayDataSets(averagedDataPoint, isFromFile);
			processDataForLongRange(averagedDataPoint, isFromFile);
		}
		
		if(!isFromFile && isNewData) {
			if ( ! LogFile.store(logFilePath, averagedDataPoint) ) {
				System.out.println("Problem with file "+logFileName+".csv");
			}
			
//			if (uploadOnLine) {
//				UploadMySQL.storeInstantValue(channelName, averagedDataPoint);
//			}
			
			
//			if ( !UploadMySQL.storeValueAndDeleteLast(logFileName, HOUR_SUFIX, averagedDataPoint) ) {
//				System.out.println("Problem uploading "+logFileName+HOUR_SUFIX+" to MySQL");
//			} 
		}
	}
	
	private void processDataForLongRange(AveragedDataPoint averagedDataPoint, boolean isFromFile) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(averagedDataPoint.time);
		int today = c.get(Calendar.DAY_OF_YEAR);
		if (dayOfLastLongRangePoint == -1) dayOfLastLongRangePoint = today;
		
		longRangeSum += averagedDataPoint.value;
		longRangeTimeSum += averagedDataPoint.time;
		if (averagedDataPoint.min < longRangeMin) longRangeMin = averagedDataPoint.min;
		if (averagedDataPoint.max > longRangeMax) longRangeMax = averagedDataPoint.max;
		longRangeCount++;
		
		if (dayOfLastLongRangePoint != today) {
			long averageTime = (long) (longRangeTimeSum/longRangeCount);
			double averageValue = longRangeSum/longRangeCount;

			longRangeDataSet.add(averageTime, averageValue);
			longRangeMinDataSet.add(averageTime, longRangeMin);
			longRangeMaxDataSet.add(averageTime, longRangeMax);
			
//			if ( !UploadMySQL.storeValueAndDeleteLast(logFileName, LONGRANGE_SUFIX, averagedDataPoint) ) {
//				System.out.println("Problem uploading "+logFileName+LONGRANGE_SUFIX+" to MySQL");
//			}
			
			longRangeSum = 0;
			longRangeTimeSum = 0;
			longRangeMin = Double.POSITIVE_INFINITY;
			longRangeMax = Double.NEGATIVE_INFINITY;
			longRangeCount = 0;
			dayOfLastLongRangePoint = today;
		}
		
	}
	
	private void processNewHourDataForDayDataSets(AveragedDataPoint averagedDataPoint, boolean isFromFile) {
		daySum += averagedDataPoint.value;
		dayTimeSum += averagedDataPoint.time;
		if (averagedDataPoint.min < dayMin) dayMin = averagedDataPoint.min;
		if (averagedDataPoint.max > dayMax) dayMax = averagedDataPoint.max;
		dayCount++;
		if (dayCount > HOUR_POINTS_TO_DAY_POINT) {
			long averageTime = (long) (dayTimeSum/dayCount);
			double averageValue = daySum/dayCount;

			dayDataSet.add(averageTime, averageValue);
			dayMinDataSet.add(averageTime, dayMin);
			dayMaxDataSet.add(averageTime, dayMax);

			averagedDataPoint = new AveragedDataPoint(averageTime, averageValue, dayMin, dayMax);
			processNewDayData(averagedDataPoint, isFromFile);
			
			if (!isFromFile) {
				writeOnlineRealTimeData();
				writeOnlineHourData();
				writeOnlineDayData();
				
//				if ( !UploadMySQL.storeValueAndDeleteLast(logFileName, DAY_SUFIX, averagedDataPoint) ) {
//					System.out.println("Problem uploading "+logFileName+DAY_SUFIX+" to MySQL");
//				}
			}

			daySum = 0;
			dayTimeSum = 0;
			dayMin = Double.POSITIVE_INFINITY;
			dayMax = Double.NEGATIVE_INFINITY;
			dayCount = 0;

		}
	}

	private void processNewDayData(AveragedDataPoint averagedDataPoint, boolean isFromFile) {
		monthSum += averagedDataPoint.value;
		monthTimeSum += averagedDataPoint.time;
		if (averagedDataPoint.min < monthMin) monthMin = averagedDataPoint.min;
		if (averagedDataPoint.max > monthMax) monthMax = averagedDataPoint.max;
		monthCount++;
		if (monthCount > DAY_POINTS_TO_MONTH_POINT) {
			long averageTime = (long) (monthTimeSum/monthCount);
			double averageValue = monthSum/monthCount;

			monthDataSet.add(averageTime, averageValue);
			monthMinDataSet.add(averageTime, monthMin);
			monthMaxDataSet.add(averageTime, monthMax);

			averagedDataPoint = new AveragedDataPoint(averageTime, averageValue, monthMin, monthMax);
			processNewMonthData(averagedDataPoint, isFromFile);

			if (!isFromFile) {
				writeOnlineMonthData();
				writeOnlineLongRangeData();
				
//				if ( !UploadMySQL.storeValueAndDeleteLast(logFileName, MONTH_SUFIX, averagedDataPoint) ) {
//					System.out.println("Problem uploading "+logFileName+MONTH_SUFIX+" to MySQL");
//				}
			}
			
			monthSum = 0;
			monthTimeSum = 0;
			monthMin = Double.POSITIVE_INFINITY;
			monthMax = Double.NEGATIVE_INFINITY;
			monthCount = 0;

		}
	}

	private void processNewMonthData(AveragedDataPoint averagedDataPoint, boolean isFromFile) {
		yearSum += averagedDataPoint.value;
		yearTimeSum += averagedDataPoint.time;
		if (averagedDataPoint.min < yearMin) yearMin = averagedDataPoint.min;
		if (averagedDataPoint.max > yearMax) yearMax = averagedDataPoint.max;
		yearCount++;
		if (yearCount > MONTH_POINTS_TO_YEAR_POINT) {
			long averageTime = (long) (yearTimeSum/yearCount);
			double averageValue = yearSum/yearCount;

			yearDataSet.add(averageTime, averageValue);
			yearMinDataSet.add(averageTime, yearMin);
			yearMaxDataSet.add(averageTime, yearMax);
			
			if (!isFromFile) {
				writeOnlineYearData();
//				if ( !UploadMySQL.storeValueAndDeleteLast(logFileName, YEAR_SUFIX, averagedDataPoint) ) {
//					System.out.println("Problem uploading "+logFileName+YEAR_SUFIX+" to MySQL");
//				}
			}

			yearSum = 0;
			yearTimeSum = 0;
			yearMin = Double.POSITIVE_INFINITY;
			yearMax = Double.NEGATIVE_INFINITY;
			yearCount = 0;
		}
	}
	
	private void loadLogFile(Path logFilePath) {
		if (isFileLoading.get()) return;
		
		setRecording(false);
		resetDataSets();
		
		LogFileLoader loader = new LogFileLoader(logFilePath);
		loader.setPriority(Thread.MIN_PRIORITY);
		loader.start();
	}
	
	private void loadDataSetLogFile() {
		if (isFileLoading.get()) return;
		
		setRecording(false);
		resetDataSets();
		
		DataSetLogFileLoader loader = new DataSetLogFileLoader();
		loader.setPriority(Thread.MIN_PRIORITY);
		loader.start();
		
	}
	
	private void resetDataSets(){
		daySum = 0; 
		dayTimeSum = 0;
		dayMin = Double.POSITIVE_INFINITY;
		dayMax = Double.NEGATIVE_INFINITY;
		dayCount = 0;

		monthSum = 0; 
		monthTimeSum = 0;
		monthMin = Double.POSITIVE_INFINITY;
		monthMax = Double.NEGATIVE_INFINITY;
		monthCount = 0;

		yearSum = 0;
		yearTimeSum = 0;
		yearMin = Double.POSITIVE_INFINITY;
		yearMax = Double.NEGATIVE_INFINITY;
		yearCount = 0;

		previousData = Double.NaN;

		longRangeSum = 0;
		longRangeTimeSum = 0;
		longRangeMin = Double.POSITIVE_INFINITY;
		longRangeMax = Double.NEGATIVE_INFINITY;
		longRangeCount = 0;
		dayOfLastLongRangePoint = -1;
		
		realTimeDataSet.clear();
		hourDataSet.clear();
		hourMaxDataSet.clear();
		hourMinDataSet.clear();
		dayDataSet.clear();
		dayMaxDataSet.clear();
		dayMinDataSet.clear();
		monthDataSet.clear();
		monthMaxDataSet.clear();
		monthMinDataSet.clear();
		yearDataSet.clear();
		yearMaxDataSet.clear();
		yearMinDataSet.clear();
		longRangeDataSet.clear();
		longRangeMinDataSet.clear();
		longRangeMaxDataSet.clear();
	}
	
	private void writeOnlineDataSet(DataSet dataset, DataSet minDataset, DataSet maxDataset, String timeScale) {
		Path onlineFilePath = Paths.get(ProbeManager.onlineFileLocalDirectory).resolve(logFileName+timeScale+".csv");

		try {
			BufferedWriter onlineFileWriter = Files.newBufferedWriter(onlineFilePath, Charset.defaultCharset(), new OpenOption[] {
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE});

			onlineFileWriter.write("Time, "+channelName+", Min, Max\n");

			String time, value, min, max;
			for (int i = 0; i < dataset.getDataCount(); i++) {
				time = Double.toString(dataset.getX(i));
				value = Double.toString(dataset.getY(i));
				if (minDataset != null) min = Double.toString(minDataset.getY(i));
				else min = "";
				if (maxDataset != null) max = Double.toString(maxDataset.getY(i));
				else max = "";
				onlineFileWriter.write(time+", "+value+", "+min+", "+max+"\n");
			}

			onlineFileWriter.flush();
			onlineFileWriter.close();

		} catch (Exception e) {
			System.out.println(new Date().toString()+": Fail writing "+ timeScale +" "+channelName);
		} 
	}
	
	private void writeOnlineRealTimeData() {
		writeOnlineDataSet(realTimeDataSet, null, null, REALTIME_SUFIX);		
	}

	private void writeOnlineHourData() {
		writeOnlineDataSet(hourDataSet, hourMinDataSet, hourMaxDataSet, HOUR_SUFIX);		
	}

	private void writeOnlineDayData() {
		writeOnlineDataSet(dayDataSet, dayMinDataSet, dayMaxDataSet, DAY_SUFIX);		
	}

	private void writeOnlineMonthData() {
		writeOnlineDataSet(monthDataSet, monthMinDataSet, monthMaxDataSet, MONTH_SUFIX);
	}

	private void writeOnlineYearData() {
		writeOnlineDataSet(yearDataSet, yearMinDataSet, yearMaxDataSet, YEAR_SUFIX);		
	}
	
	private void writeOnlineLongRangeData() {
		writeOnlineDataSet(longRangeDataSet, longRangeMinDataSet, longRangeMaxDataSet, LONGRANGE_SUFIX);		
	}

	private class AveragingTask extends TimerTask{
		private double sum;
		private double lastAverage = Double.NaN;
		private double min, max;
		private long timeSum, count;

		public AveragingTask() {
			initVariables();
		}

		@Override
		public void run() {
			if (!isRecording.get() || isFileLoading.get()) return;
			
			if (count > 0) {
				long time = timeSum/count;
				double av = sum/count;
				AveragedDataPoint averagedDataPoint = new AveragedDataPoint(time, av, min, max);
				DataChannel.this.processAveragedData(averagedDataPoint, false, true);
				lastAverage = av;
			} else if (!Double.isNaN(lastAverage)){
				timeSum = System.currentTimeMillis();
				AveragedDataPoint averagedDataPoint = new AveragedDataPoint(timeSum, lastAverage, lastAverage, lastAverage);
				DataChannel.this.processAveragedData(averagedDataPoint, false, false);
			}

			initVariables();
		}

		public void addPoint(long time, double dataPoint) {
			sum += dataPoint;
			timeSum += time;
			count++;
			if (dataPoint < min) min = dataPoint;
			if (dataPoint > max) max = dataPoint;
		}

		private void initVariables () {
			sum = 0;
			timeSum = 0;
			count = 0;
			min = Double.POSITIVE_INFINITY;
			max = Double.NEGATIVE_INFINITY;
		}

	}
	
	private class DataSetLogFileLoader extends Thread {
		
		@Override
		public void run() {
			DataChannel.this.setRecording(false);
			DataChannel.this.isFileLoading.set(true);
			
			loadDataSetLogFile(DataChannel.this.realTimeDataSet,
					null,
					null, REALTIME_SUFIX);
			
			loadDataSetLogFile(DataChannel.this.hourDataSet,
					DataChannel.this.hourMinDataSet,
					DataChannel.this.hourMaxDataSet, HOUR_SUFIX);
			
			loadDataSetLogFile(DataChannel.this.dayDataSet,
					DataChannel.this.dayMinDataSet,
					DataChannel.this.dayMaxDataSet, DAY_SUFIX);
			
			loadDataSetLogFile(DataChannel.this.monthDataSet,
					DataChannel.this.monthMinDataSet,
					DataChannel.this.monthMaxDataSet, MONTH_SUFIX);
			
			loadDataSetLogFile(DataChannel.this.yearDataSet,
					DataChannel.this.yearMinDataSet,
					DataChannel.this.yearMaxDataSet, YEAR_SUFIX);
			
			loadDataSetLogFile(DataChannel.this.longRangeDataSet,
					DataChannel.this.longRangeMinDataSet,
					DataChannel.this.longRangeMaxDataSet, LONGRANGE_SUFIX);
			
			
			DataChannel.this.isFileLoading.set(false);
			DataChannel.this.setRecording(true);
		}
		
		private void loadDataSetLogFile(DataSet dataset, DataSet minDataset, DataSet maxDataset, String timeScale) {
			Path datasetFilePath = Paths.get(ProbeManager.onlineFileLocalDirectory).resolve(logFileName+timeScale+".csv");
			
			try {
				BufferedReader logFileReader = Files.newBufferedReader(datasetFilePath, Charset.defaultCharset());
				String line;
				String[] elements;
				double time = 0;
				double value = Double.NaN, min = Double.NaN, max = Double.NaN;
				boolean realtimeData = timeScale.contains(REALTIME_SUFIX);

//				UploadMySQL.emptyTable(logFileName, timeScale);
				
				logFileReader.readLine(); //header
				while ((line = logFileReader.readLine()) != null) {
					time = 0; value = 0; min = 0; max = 0;
					elements = line.split(", ");
					if (elements.length >= 2) {
						try {
							time = Double.parseDouble(elements[0]);
							value = Double.parseDouble(elements[1]);
							dataset.add(time, value);
						} catch (NumberFormatException e) {
							System.out.println(channelName+" "+timeScale+" corrupted point");
						}
					} 
					if (elements.length == 4 && !realtimeData) {
						try {
							min = Double.parseDouble(elements[2]);
							minDataset.add(time, min);
							max = Double.parseDouble(elements[3]);
							maxDataset.add(time, max);
						} catch (NumberFormatException e) {
							System.out.println(channelName+" "+timeScale+" corrupted point");
						}
					}
//					if (!realtimeData)
//						UploadMySQL.storeValue(logFileName, timeScale, new AveragedDataPoint((long) time, value, min, max));
				}

				logFileReader.close();
				logFileReader = null;
				line = null;
				elements = null;
				System.out.println(channelName+" "+timeScale+" Loaded.");

			} catch (IOException e) {
				System.out.println(channelName+" not found");
			}

		}
		
	}

	private class LogFileLoader extends Thread {
		private Path logFilePath;
		
		public LogFileLoader(Path logFilePath) {
			this.logFilePath = logFilePath;
		}
		
		@Override
		public void run() {
			DataChannel.this.setRecording(false);
			DataChannel.this.isFileLoading.set(true);
			
			try {
				BufferedReader logFileReader = Files.newBufferedReader(this.logFilePath, Charset.defaultCharset());
				String line;
				String[] elements;
				double time = 0;
				double value = Double.NaN, min = Double.NaN, max = Double.NaN;

				while ((line = logFileReader.readLine()) != null) {
					elements = line.split(", ");
					if (elements.length == 4) {
						try {
							time = Double.parseDouble(elements[0]);
							value = Double.parseDouble(elements[1]);
							max = Double.parseDouble(elements[2]);
							min = Double.parseDouble(elements[3]);

							AveragedDataPoint averagedDataPoint = new AveragedDataPoint((long) time, value, min, max);
							processAveragedData(averagedDataPoint, true, false);

						} catch (NumberFormatException e) {
							System.out.println(channelName+" corrupted point");
						}
					}

//					sleep(50); // to not overload the system
				}

				logFileReader.close();
				logFileReader = null;
				line = null;
				elements = null;
				System.out.println(channelName+" Loaded.");

			} catch (IOException e) {
				System.out.println(channelName+" not yet created");
			}

			DataChannel.this.isFileLoading.set(false);
			DataChannel.this.setRecording(true);
		}
	}
	
	private class Blinker extends Thread {
		private static final int DELAY = 100;

		@Override
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {	DataChannel.this.getChannelButton().setBackground(Color.white); }
			});
			
			try {
				sleep(DELAY);
			} catch (InterruptedException e) { e.printStackTrace(); }

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {	DataChannel.this.getChannelButton().setBackground(Color.gray); }
			});
			
			try {
				sleep(DELAY);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() { DataChannel.this.getChannelButton().setBackground(Color.black);	}
			});

		}
	}
}
