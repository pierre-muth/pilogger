package pilogger;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JMenuItem;

import datachannel.AbstractProbe;
import datachannel.DataChannel;
import datachannel.DataChannelListener;
import datachannel.DataReceivedEvent;

public class ProbeManager implements DataChannelListener, ActionListener {
	private PiloggerGUI gui;
	private HashMap<JMenuItem, DataChannel> scale0channelMap = new HashMap<>();
	private HashMap<JMenuItem, DataChannel> scale1channelMap = new HashMap<>();
	private HashMap<TimeScale, String> timeScaleText = new HashMap<>();
	private DataChannel scale0selectedChannel;
	private DataChannel scale1selectedChannel;
	private TimeScale timeScaleSelected = TimeScale.DAY;
	private Font labelFont = new Font("Arial", Font.PLAIN, 8);
	/**
	 * Manage the probes by generating Gui according to 
	 * the channels provided. Switch accordingly the dataset
	 * displayed in chart.
	 * @param gui Pilogger Gui.
	 */
	public ProbeManager(PiloggerGUI gui) {
		this.gui = gui;
		initTimeScaleMenu();
	}
	public void addProbe(AbstractProbe probe) {
		for (int i = 0; i < probe.getChannels().length; i++) {
			DataChannel channel = probe.getChannels()[i]; 
			channel.addDataChannelListener(this);
			JMenuItem item0 = new JMenuItem(channel.channelName);
			JMenuItem item1 = new JMenuItem(channel.channelName);
			gui.getScale0menu().add(item0);
			gui.getScale1menu().add(item1);
			scale0channelMap.put(item0, channel);
			scale1channelMap.put(item1, channel);
			item0.addActionListener(this);
			item1.addActionListener(this);
			item0.setBackground(Color.black);
			item1.setBackground(Color.black);
			item0.setForeground(Color.white);
			item1.setForeground(Color.white);			
			item0.setFont(labelFont);
			item1.setFont(labelFont);
			
			// first default selection
			if (scale0selectedChannel == null)
				scale0selectedChannel = channel;
			else if (scale1selectedChannel == null) {
				scale1selectedChannel = channel;
				resetDisplayedDataset();
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JMenuItem item = (JMenuItem)arg0.getSource();
		if (scale0channelMap.containsKey(item)) {
			scale0selectedChannel = scale0channelMap.get(item);
		}
		if (scale1channelMap.containsKey(item)) {
			scale1selectedChannel = scale1channelMap.get(item);
		}
		resetDisplayedDataset();
	}

	@Override
	public void dataReceived(DataReceivedEvent dataReceivedEvent) {
		
		// TODO Save to raw .csv file
		
	}
	private void resetDisplayedDataset() {
		if (gui.getDataSource0().getDataSets().length >0)
			gui.getDataSource0().removeDataSet(0);
		if (gui.getDataSource1().getDataSets().length >0)
			gui.getDataSource1().removeDataSet(0);
		
		switch (timeScaleSelected) {
		case REALTIME:
			gui.getDataSource0().addDataSet(0, scale0selectedChannel.getRealTimeDataSet());
			gui.getDataSource1().addDataSet(0, scale1selectedChannel.getRealTimeDataSet());
			break;
			
		case DAY:
			gui.getDataSource0().addDataSet(0, scale0selectedChannel.getDayDataSet());			
			gui.getDataSource1().addDataSet(0, scale1selectedChannel.getDayDataSet());			
			break;
			
		case MONTH:
			gui.getDataSource0().addDataSet(0, scale0selectedChannel.getMonthDataSet());			
			gui.getDataSource1().addDataSet(0, scale1selectedChannel.getMonthDataSet());			
			break;
			
		case YEAR:
			gui.getDataSource0().addDataSet(0, scale0selectedChannel.getYearDataSet());			
			gui.getDataSource1().addDataSet(0, scale1selectedChannel.getYearDataSet());			
			break;

		default:
			gui.getDataSource0().addDataSet(0, scale0selectedChannel.getDayDataSet());			
			gui.getDataSource1().addDataSet(0, scale1selectedChannel.getDayDataSet());
			break;
		}
		
		gui.getScale0Button().setText(scale0selectedChannel.channelName);
		gui.getScale1Button().setText(scale1selectedChannel.channelName);
		gui.getTimeScaleButton().setText(timeScaleText.get(timeScaleSelected));
	}	
	private void initTimeScaleMenu() {
		timeScaleText.put(TimeScale.REALTIME, "Real Time");
		timeScaleText.put(TimeScale.DAY, "Day");
		timeScaleText.put(TimeScale.MONTH, "Month");
		timeScaleText.put(TimeScale.YEAR, "Year");
		gui.getScaleTimeMenu().add(getRealTimeMItem());
		gui.getScaleTimeMenu().add(getDayTimeMItem());
		gui.getScaleTimeMenu().add(getMonthTimeMItem());
		gui.getScaleTimeMenu().add(getYearTimeMItem());
	}
	
	private JMenuItem realTimeMItem;
	private JMenuItem getRealTimeMItem() {
		if (realTimeMItem == null) {
			realTimeMItem = new JMenuItem(timeScaleText.get(TimeScale.REALTIME));
			realTimeMItem.setBackground(Color.black);
			realTimeMItem.setForeground(Color.white);
			realTimeMItem.setFont(labelFont);
			realTimeMItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timeScaleSelected = TimeScale.REALTIME;
					resetDisplayedDataset();
				}
			});
		}
		return realTimeMItem;
	}
	private JMenuItem dayTimeMItem;
	private JMenuItem getDayTimeMItem() {
		if (dayTimeMItem == null) {
			dayTimeMItem = new JMenuItem(timeScaleText.get(TimeScale.DAY));
			dayTimeMItem.setBackground(Color.black);
			dayTimeMItem.setForeground(Color.white);
			dayTimeMItem.setFont(labelFont);
			dayTimeMItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timeScaleSelected = TimeScale.DAY;
					resetDisplayedDataset();
				}
			});
		}
		return dayTimeMItem;
	}
	private JMenuItem monthTimeMItem;
	private JMenuItem getMonthTimeMItem() {
		if (monthTimeMItem == null) {
			monthTimeMItem = new JMenuItem(timeScaleText.get(TimeScale.MONTH));
			monthTimeMItem.setBackground(Color.black);
			monthTimeMItem.setForeground(Color.white);
			monthTimeMItem.setFont(labelFont);
			monthTimeMItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timeScaleSelected = TimeScale.MONTH;
					resetDisplayedDataset();
				}
			});
		}
		return monthTimeMItem;
	}
	private JMenuItem yearTimeMItem;
	private JMenuItem getYearTimeMItem() {
		if (yearTimeMItem == null) {
			yearTimeMItem = new JMenuItem(timeScaleText.get(TimeScale.YEAR));
			yearTimeMItem.setBackground(Color.black);
			yearTimeMItem.setForeground(Color.white);
			yearTimeMItem.setFont(labelFont);
			yearTimeMItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timeScaleSelected = TimeScale.YEAR;
					resetDisplayedDataset();
				}
			});
		}
		return yearTimeMItem;
	}
}
