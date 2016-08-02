package pilogger;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import cern.jdve.Chart;
import cern.jdve.Style;
import cern.jdve.data.DefaultDataSet;
import cern.jdve.data.DefaultDataSource;
import cern.jdve.renderer.AreaChartRenderer;
import cern.jdve.renderer.PolylineChartRenderer;
import cern.jdve.scale.TimeStepsDefinition;

public class ChannelPanel extends JPanel {
	private DataChannel channel;
	private Color lineColor = new Color(255, 255, 255);
	private Color filltColor = new Color(128, 128, 128);
	private Style lineStyle = new Style(new BasicStroke(1.0f), lineColor, filltColor);
	public static Font valueFont = new Font("Dialog", Font.BOLD, 30);
	public static Font titleFont = new Font("Dialog", Font.PLAIN, 11);
	public static Font minmaxFont = new Font("Dialog", Font.PLAIN, 15);
	private final NumberFormat formatter = NumberFormat.getNumberInstance();
	private DefaultDataSet dataSet;

	public ChannelPanel(DataChannel channel){
		this.channel = channel;
		if (PiloggerLauncher.simulation){
			dataSet = channel.realTimeDataSet;
		} else {
			dataSet = channel.dayDataSet;
		}
		setBackground(Color.black);
		setLayout(new GridLayout(1, 2, 2, 1));
		add(getChartSimple());
		add(getInfoPanel());
		formatter.setGroupingUsed(false);
		formatter.setMaximumFractionDigits(2);
	}

	public void setValue(final double value) {
		if (value >= 1000) formatter.setMaximumFractionDigits(0);
		else formatter.setMaximumFractionDigits(2);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				getvalueLabel().setText(formatter.format(value));
				getminLabel().setText(formatter.format(dataSet.getYRange().getMin()));
				getmaxLabel().setText(formatter.format(dataSet.getYRange().getMax()));
			}
		});
	}

	private JPanel infoPanel;
	private JPanel getInfoPanel() {
		if (infoPanel == null){

			infoPanel = new JPanel(new BorderLayout());
			infoPanel.setBackground(Color.black);
			
			JPanel jpCenter = new JPanel(new BorderLayout());
			jpCenter.setBackground(Color.black);
			jpCenter.add(gettitleLabel(), BorderLayout.WEST);
			jpCenter.add(getunitLabel(), BorderLayout.CENTER);
			
			infoPanel.add(jpCenter, BorderLayout.NORTH);
			
			JPanel jpSouth = new JPanel(new BorderLayout());
			jpSouth.setBackground(Color.black);
			jpSouth.add(getvalueLabel(), BorderLayout.CENTER);
			
			JPanel jpMinMax = new JPanel(new GridLayout(2, 1));
			jpMinMax.setBackground(Color.black);
			jpMinMax.add(getmaxLabel());
			jpMinMax.add(getminLabel());
			
			jpSouth.add(jpMinMax, BorderLayout.EAST);
			
			infoPanel.add(jpSouth, BorderLayout.CENTER);
			
		}
		return infoPanel;
	}
	
	

	private JLabel value;
	private JLabel getvalueLabel() {
		if (value == null){
			value = new JLabel("-");
			value.setFont(valueFont);
			value.setBackground(Color.black);
			value.setForeground(Color.white);
			value.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return value;
	}

	private JLabel min;
	private JLabel getminLabel() {
		if (min == null){
			min = new JLabel("min");
			min.setFont(minmaxFont);
			min.setBackground(Color.black);
			min.setForeground(Color.white);
			min.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return min;
	}

	private JLabel max;
	private JLabel getmaxLabel() {
		if (max == null){
			max = new JLabel("max");
			max.setFont(minmaxFont);
			max.setBackground(Color.black);
			max.setForeground(Color.white);
			max.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return max;
	}

	private JLabel title;
	private JLabel gettitleLabel() {
		if (title == null){
			title = new JLabel(channel.channelName);
			title.setFont(titleFont);
			title.setBackground(Color.black);
			title.setForeground(Color.white);
			title.setHorizontalAlignment(SwingConstants.LEFT);
		}
		return title;
	}

	private JLabel unit;
	private JLabel getunitLabel() {
		if (unit == null){
			unit = new JLabel(channel.getUnit());
			unit.setFont(titleFont);
			unit.setBackground(Color.black);
			unit.setForeground(Color.white);
			unit.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return unit;
	}

	private Chart chartSimple;
	private Chart getChartSimple(){
		if (chartSimple == null){
			chartSimple = new Chart();

			chartSimple.getXScale().setVisible(false);
			chartSimple.getYScale().setVisible(false);
			chartSimple.getXScale().setAxisVisible(false);
			chartSimple.getYScale().setAxisVisible(false);

			chartSimple.getArea().setBackground(Color.black);
			chartSimple.setBackground(Color.black);
			chartSimple.getArea().setStyle(new Style(Color.black, Color.black));
			chartSimple.getXGrid().setVisible(false);
			chartSimple.getYGrid().setVisible(false);

			TimeStepsDefinition stepsDefinition = new TimeStepsDefinition();
			chartSimple.getXScale().setStepsDefinition(stepsDefinition);

			chartSimple.addRenderer(getAeraRenderer());			
		}
		return chartSimple;
	}

	private AreaChartRenderer aeraRenderer;
	private PolylineChartRenderer getAeraRenderer() {
		if (aeraRenderer == null) {
			aeraRenderer = new AreaChartRenderer();
			aeraRenderer.setStyles(new Style[] {lineStyle, lineStyle});
			aeraRenderer.setDataSource(getLineDataSource());
		}
		return aeraRenderer;
	}
	
	private PolylineChartRenderer lineRenderer;
	private PolylineChartRenderer getLineRenderer() {
		if (lineRenderer == null) {
			lineRenderer = new PolylineChartRenderer();
			lineRenderer.setStyles(new Style[] {lineStyle, lineStyle});
			lineRenderer.setDataSource(getLineDataSource());
		}
		return lineRenderer;
	}

	private DefaultDataSource lineDataSource;
	private DefaultDataSource getLineDataSource() {
		if (lineDataSource == null) {
			lineDataSource = new DefaultDataSource();
			lineDataSource.addDataSet(dataSet);
		} 
		return lineDataSource;
	}

}
