package pilogger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import cern.jdve.data.DataSet;
import cern.jdve.data.ShiftingDataSet;

/**
 * @author pfreyerm
 */
public class DataChannel {
	public static final String logFileDirectory = "/home/pi/projects/pilogger/logs/";

	private Path logFilePath;

	public String channelName;
	private String unit = "";

	private String logFileName = "";

	private static final int CHART_BUFFER_LENGTH = 600;

	private static final int MS_TO_HOUR_POINT = 15000;
	private static final int HOUR_POINTS_TO_DAY_POINT = 10;
	private static final int DAY_POINTS_TO_MONTH_POINT = 18;
	private static final int MONTH_POINTS_TO_YEAR_POINT = 6;

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

	private double daySum = 0; 
	private long dayTimeSum = 0;
	private double dayMin = Double.POSITIVE_INFINITY, dayMax = Double.NEGATIVE_INFINITY;
	private int dayCount = 0;

	private double monthSum = 0; 
	private long monthTimeSum = 0;
	private double monthMin = Double.POSITIVE_INFINITY, monthMax = Double.NEGATIVE_INFINITY;
	private int monthCount = 0;

	private double yearSum = 0;
	private long yearTimeSum = 0;
	private double yearMin = Double.POSITIVE_INFINITY, yearMax = Double.NEGATIVE_INFINITY;
	private int yearCount = 0;

	private double previousData = Double.NaN;

	private JButton blinkButton;
	private JButton reloadButton;
	
	private AtomicBoolean isRecording = new AtomicBoolean(true);
	private AtomicBoolean isFileLoading = new AtomicBoolean(false);
	private boolean isDifferential = false;

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
		logFilePath = piloggerDir.resolve(logFileName+".csv");

		loadLogFile(logFilePath);

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
		
		processNewData(data);
		
		Blinker b = new Blinker();
		b.start();
		
	}

	public JComponent getChannelButton() {
		if (blinkButton == null) {
			blinkButton = new JButton();
			blinkButton.setBorder(new LineBorder(Color.gray));
			blinkButton.setBackground(Color.black);
			blinkButton.setPreferredSize(new Dimension(8, 8));
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

	private void processNewData(double data) {
		long time = System.currentTimeMillis();
		realTimeDataSet.add(time, data);
		if (isRecording.get()) 
			averagingTask.addPoint(time, data);
	}

	private void processAveragedData(AveragedDataPoint averagedDataPoint, boolean fromFile) {
		hourDataSet.add(averagedDataPoint.time, averagedDataPoint.value);
		hourMaxDataSet.add(averagedDataPoint.time, averagedDataPoint.max);
		hourMinDataSet.add(averagedDataPoint.time, averagedDataPoint.min);

		processNewHourDataForDayDataSets(averagedDataPoint, fromFile);
		if(!fromFile) {
			if ( ! LogFile.store(logFilePath, averagedDataPoint) ) {
				System.out.println("Problem with file "+logFileName+".csv");
			}
		}
	}

	private void processNewHourDataForDayDataSets(AveragedDataPoint averagedDataPoint, boolean fromFile) {
		daySum += averagedDataPoint.value;
		dayTimeSum += averagedDataPoint.time;
		if (averagedDataPoint.min < dayMin) dayMin = averagedDataPoint.min;
		if (averagedDataPoint.max > dayMax) dayMax = averagedDataPoint.max;
		dayCount++;
		if (dayCount > HOUR_POINTS_TO_DAY_POINT) {
			long averageTime = dayTimeSum/dayCount;
			double averageValue = daySum/dayCount;

			dayDataSet.add(averageTime, averageValue);
			dayMinDataSet.add(averageTime, dayMin);
			dayMaxDataSet.add(averageTime, dayMax);

			averagedDataPoint = new AveragedDataPoint(averageTime, averageValue, dayMin, dayMax);
			processNewDayData(averagedDataPoint, fromFile);
			
			if (!fromFile) {
				writeOnlineRealTimeData();
				writeOnlineHourData();
				writeOnlineDayData();
			}

			daySum = 0;
			dayTimeSum = 0;
			dayMin = Double.POSITIVE_INFINITY;
			dayMax = Double.NEGATIVE_INFINITY;
			dayCount = 0;

		}
	}

	private void processNewDayData(AveragedDataPoint averagedDataPoint, boolean fromFile) {
		monthSum += averagedDataPoint.value;
		monthTimeSum += averagedDataPoint.time;
		if (averagedDataPoint.min < monthMin) monthMin = averagedDataPoint.min;
		if (averagedDataPoint.max > monthMax) monthMax = averagedDataPoint.max;
		monthCount++;
		if (monthCount > DAY_POINTS_TO_MONTH_POINT) {
			long averageTime = monthTimeSum/monthCount;
			double averageValue = monthSum/monthCount;

			monthDataSet.add(averageTime, averageValue);
			monthMinDataSet.add(averageTime, monthMin);
			monthMaxDataSet.add(averageTime, monthMax);

			averagedDataPoint = new AveragedDataPoint(averageTime, averageValue, monthMin, monthMax);
			processNewMonthData(averagedDataPoint, fromFile);

			if (!fromFile) {
				writeOnlineMonthData();
			}
			
			monthSum = 0;
			monthTimeSum = 0;
			monthMin = Double.POSITIVE_INFINITY;
			monthMax = Double.NEGATIVE_INFINITY;
			monthCount = 0;

		}
	}

	private void processNewMonthData(AveragedDataPoint averagedDataPoint, boolean fromFile) {
		yearSum += averagedDataPoint.value;
		yearTimeSum += averagedDataPoint.time;
		if (averagedDataPoint.min < yearMin) yearMin = averagedDataPoint.min;
		if (averagedDataPoint.max > yearMax) yearMax = averagedDataPoint.max;
		yearCount++;
		if (yearCount > MONTH_POINTS_TO_YEAR_POINT) {
			long averageTime = yearTimeSum/yearCount;
			double averageValue = yearSum/yearCount;

			yearDataSet.add(averageTime, averageValue);
			yearMinDataSet.add(averageTime, yearMin);
			yearMaxDataSet.add(averageTime, yearMax);
			
			if (!fromFile) {
				writeOnlineYearData();
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
		
		FileLoader loader = new FileLoader(logFilePath);
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
		writeOnlineDataSet(realTimeDataSet, null, null, "Realtime");		
	}

	private void writeOnlineHourData() {
		writeOnlineDataSet(hourDataSet, hourMinDataSet, hourMaxDataSet, "Hour");		
	}

	private void writeOnlineDayData() {
		writeOnlineDataSet(dayDataSet, dayMinDataSet, dayMaxDataSet, "Day");		
	}

	private void writeOnlineMonthData() {
		writeOnlineDataSet(monthDataSet, monthMinDataSet, monthMaxDataSet, "Month");
	}

	private void writeOnlineYearData() {
		writeOnlineDataSet(yearDataSet, yearMinDataSet, yearMaxDataSet, "Year");		
	}

	private class AveragingTask extends TimerTask{
		private double sum;
		private double min, max;
		private long timeSum, count;

		public AveragingTask() {
			initVariables();
		}

		@Override
		public void run() {
			if (count > 0) {
				long time = timeSum/count;
				double av = sum/count;
				AveragedDataPoint averagedDataPoint = new AveragedDataPoint(time, av, min, max);
				DataChannel.this.processAveragedData(averagedDataPoint, false);
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

	private class FileLoader extends Thread {
		private Path logFilePath;
		
		public FileLoader(Path logFilePath) {
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
				long time = 0;
				double value = Double.NaN, min = Double.NaN, max = Double.NaN;

				while ((line = logFileReader.readLine()) != null) {
					elements = line.split(", ");
					if (elements.length > 0) time = (long) Double.parseDouble(elements[0]);
					if (elements.length > 1) value = Double.parseDouble(elements[1]);
					if (elements.length > 2) max = Double.parseDouble(elements[2]);
					if (elements.length > 3) min = Double.parseDouble(elements[3]);

					AveragedDataPoint averagedDataPoint = new AveragedDataPoint(time, value, min, max);
					processAveragedData(averagedDataPoint, true);
					//sleep(1); // to not overload the system
				}

				logFileReader.close();
				logFileReader = null;
				line = null;
				elements = null;
				System.out.println(channelName+" Loaded.");
				
			} catch (NumberFormatException e) {
				System.out.println(channelName+" seems corrupted");
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
				public void run() { DataChannel.this.getChannelButton().setBackground(Color.black);	}
			});

		}
	}
}
