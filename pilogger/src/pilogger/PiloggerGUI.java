package pilogger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import cern.jdve.Chart;
import cern.jdve.ChartInteractor;
import cern.jdve.Style;
import cern.jdve.data.DefaultDataSource;
import cern.jdve.graphic.RenderingHint;
import cern.jdve.renderer.PolylineChartRenderer;

public abstract class PiloggerGUI extends JTabbedPane {
	private Font labelFont = new Font("Arial", Font.PLAIN, 8);
	private Color lineChartColor = new Color(255, 255, 255, 128);
	private Style lineStyle = new Style(lineChartColor, lineChartColor);
	
	public PiloggerGUI() {
		setBackground(Color.black);

		addTab("Hour", getHourPanel());
		setTabComponentAt(0, getHourTabTitle());
		setBackgroundAt(0, Color.black);
		
	}
	
	private JPanel hourPanel;
	private JPanel getHourPanel() {
		if (hourPanel == null) {
			hourPanel = new JPanel(new BorderLayout());
			hourPanel.add(getT1PHourChart(), BorderLayout.CENTER);
			hourPanel.setBackground(Color.black);
			hourPanel.setOpaque(true);
		}
		return hourPanel;
	}
	private JLabel hourTabTitle;
	private JLabel getHourTabTitle() {
		if (hourTabTitle == null) {
			hourTabTitle = new JLabel("Hour");
			hourTabTitle.setFont(labelFont);
			hourTabTitle.setForeground(Color.gray);
			hourTabTitle.setBackground(Color.black);
			hourTabTitle.setOpaque(true);
		}
		return hourTabTitle;
	}
	
	private Chart hourT1PChart;
	private Chart getT1PHourChart() {
		if (hourT1PChart == null) {
			hourT1PChart = new Chart();
			hourT1PChart.addYAxis(true, false);
			
			hourT1PChart.getXScale().setLabelFont(labelFont);
			hourT1PChart.getXScale().setLabelForeground(Color.gray);
			hourT1PChart.getXScale().setForegroundColor(Color.gray);
			
			hourT1PChart.getYScale().setLabelFont(labelFont);
			hourT1PChart.getYScale().setLabelForeground(Color.gray);
	        hourT1PChart.getYScale().setForegroundColor(Color.gray);
	        hourT1PChart.getYScale(1).setLabelFont(labelFont);
	        hourT1PChart.getYScale(1).setLabelForeground(Color.gray);
	        hourT1PChart.getYScale(1).setForegroundColor(Color.gray);
	        
	        hourT1PChart.getArea().setBackground(Color.black);
	        hourT1PChart.setBackground(Color.black);
	        hourT1PChart.getArea().setStyle(new Style(Color.black, Color.black));
	        hourT1PChart.getXGrid().setVisible(false);
	        hourT1PChart.getYGrid().setVisible(false);
	        hourT1PChart.setAntiAliasing(false);
	        hourT1PChart.setAntiAliasingText(false);
	        
	        hourT1PChart.addRenderer(0, getHourT1renderer());
	        hourT1PChart.addRenderer(1, getHourPrenderer());
	        
	        hourT1PChart.addInteractor(ChartInteractor.DATA_PICKER);
	        
		}
		
		return hourT1PChart;
	}
	private PolylineChartRenderer hourT1renderer;
	private PolylineChartRenderer getHourT1renderer() {
		if (hourT1renderer == null) {
			hourT1renderer = new PolylineChartRenderer();
			hourT1renderer.setStyles(new Style[] {lineStyle, lineStyle});
			hourT1renderer.setDataSource(getHourT1dataSource());
		}
		return hourT1renderer;
	}
	private DefaultDataSource hourT1dataSource;
	protected DefaultDataSource getHourT1dataSource() {
		if (hourT1dataSource == null) {
			hourT1dataSource = new DefaultDataSource();
		}
		return hourT1dataSource;
	}
	private PolylineChartRenderer hourPrenderer;
	private PolylineChartRenderer getHourPrenderer() {
		if (hourPrenderer == null) {
			hourPrenderer = new PolylineChartRenderer();
			hourPrenderer.setStyles(new Style[] {lineStyle, lineStyle});
			hourPrenderer.setDataSource(getHourPdataSource());
		}
		
		return hourPrenderer;
	}
	private DefaultDataSource hourPdataSource;
	protected DefaultDataSource getHourPdataSource() {
		if (hourPdataSource == null) {
			hourPdataSource = new DefaultDataSource();
		}
		return hourPdataSource;
	}
	
	
}
