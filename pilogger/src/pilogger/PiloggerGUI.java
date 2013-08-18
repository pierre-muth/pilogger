package pilogger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import cern.jdve.Chart;
import cern.jdve.Style;
import cern.jdve.data.DefaultDataSource;
import cern.jdve.renderer.PolylineChartRenderer;

public abstract class PiloggerGUI extends JPanel {
	private Font labelFont = new Font("Arial", Font.PLAIN, 8);
	private Color lineChartColor = new Color(255, 255, 255, 150);
	private Style lineStyle = new Style(lineChartColor, lineChartColor);
	private EmptyBorder emptyBorder = new EmptyBorder(0, 0, 0, 0);
	private LineBorder greyBorder = new LineBorder(Color.gray, 1);
	
	/**
	 * Pilogger application GUI. 
	 */
	
	public PiloggerGUI() {
		setBackground(Color.black);
		setLayout(new BorderLayout());
		add(getMainChart(), BorderLayout.CENTER);
		add(getNorthConfigPanel(), BorderLayout.NORTH);
		setPreferredSize(new Dimension(240, 160));
	}
	
	private JPanel northConfigPanel;
	private JPanel getNorthConfigPanel() {
		if (northConfigPanel == null) {
			northConfigPanel = new JPanel(new BorderLayout());
			northConfigPanel.setBackground(Color.black);
			northConfigPanel.add(getScale0Button(), BorderLayout.WEST);
			northConfigPanel.add(getScale1Button(), BorderLayout.EAST);
			northConfigPanel.add(getTimeScaleButton(), BorderLayout.CENTER);
		}
		return northConfigPanel;
	}
	private JButton scale0Button;
	protected JButton getScale0Button() {
		if (scale0Button == null) {
			scale0Button = new JButton("Scale 0");
			scale0Button.setFont(labelFont);
			scale0Button.setBackground(Color.black);
			scale0Button.setForeground(Color.gray);
			scale0Button.setBorder(emptyBorder);
			scale0Button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					getScale0menu().show(scale0Button, 0, 0);
				}
			});
		}
		return scale0Button;
	}
	
	private JButton scale1Button;
	protected JButton getScale1Button() {
		if (scale1Button == null) {
			scale1Button = new JButton("Scale 1");
			scale1Button.setFont(labelFont);
			scale1Button.setBackground(Color.black);
			scale1Button.setForeground(Color.gray);
			scale1Button.setBorder(emptyBorder);
			scale1Button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					getScale1menu().show(scale1Button, 0, 0);
				}
			});
		}
		return scale1Button;
	}
	private JButton timeScaleButton;
	protected JButton getTimeScaleButton() {
		if (timeScaleButton == null) {
			timeScaleButton = new JButton("Time Scale");
			timeScaleButton.setFont(labelFont);
			timeScaleButton.setBackground(Color.black);
			timeScaleButton.setForeground(Color.gray);
			timeScaleButton.setBorder(emptyBorder);
			timeScaleButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					getScaleTimeMenu().show(timeScaleButton, 0, 0);
				}
			});
		}
		return timeScaleButton;
	}
	private JPopupMenu scale0menu;
	protected JPopupMenu getScale0menu() {
		if (scale0menu == null) {
			scale0menu = new JPopupMenu();
			scale0menu.setBackground(Color.black);
			scale0menu.setBorder(greyBorder);
		}
		return scale0menu;
	}
	private JPopupMenu scale1menu;
	protected JPopupMenu getScale1menu() {
		if (scale1menu == null) {
			scale1menu = new JPopupMenu();
			scale1menu.setBackground(Color.black);
			scale1menu.setBorder(greyBorder);
		}
		return scale1menu;
	}
	private JPopupMenu scaleTimeMenu;
	protected JPopupMenu getScaleTimeMenu() {
		if (scaleTimeMenu == null) {
			scaleTimeMenu = new JPopupMenu();
			scaleTimeMenu.setBackground(Color.black);
			scaleTimeMenu.setBorder(greyBorder);
		}
		return scaleTimeMenu;
	}
	private Chart mainChart;
	private Chart getMainChart() {
		if (mainChart == null) {
			mainChart = new Chart();
			mainChart.addYAxis(true, false);
			
			mainChart.getXScale().setLabelFont(labelFont);
			mainChart.getXScale().setLabelForeground(Color.gray);
			mainChart.getXScale().setForegroundColor(Color.gray);
			
			mainChart.getYScale().setLabelFont(labelFont);
			mainChart.getYScale().setLabelForeground(Color.gray);
	        mainChart.getYScale().setForegroundColor(Color.gray);
	        mainChart.getYScale(1).setLabelFont(labelFont);
	        mainChart.getYScale(1).setLabelForeground(Color.gray);
	        mainChart.getYScale(1).setForegroundColor(Color.gray);
	        
	        mainChart.getArea().setBackground(Color.black);
	        mainChart.setBackground(Color.black);
	        mainChart.getArea().setStyle(new Style(Color.black, Color.black));
	        mainChart.getXGrid().setVisible(false);
	        mainChart.getYGrid().setVisible(false);
	        mainChart.setAntiAliasing(false);
	        mainChart.setAntiAliasingText(false);
	        
	        mainChart.addRenderer(0, getRenderer0());
	        mainChart.addRenderer(1, getRenderer1());
	         
//	        mainChart.addInteractor(ChartInteractor.DATA_PICKER);
	        
		}
		
		return mainChart;
	}
	private PolylineChartRenderer renderer0;
	private PolylineChartRenderer getRenderer0() {
		if (renderer0 == null) {
			renderer0 = new PolylineChartRenderer();
			renderer0.setStyles(new Style[] {lineStyle, lineStyle});
			renderer0.setDataSource(getDataSource0());
		}
		return renderer0;
	}
	private DefaultDataSource dataSource0;
	protected DefaultDataSource getDataSource0() {
		if (dataSource0 == null) {
			dataSource0 = new DefaultDataSource();
		} 
		return dataSource0;
	}
	private PolylineChartRenderer renderer1;
	private PolylineChartRenderer getRenderer1() {
		if (renderer1 == null) {
			renderer1 = new PolylineChartRenderer();
			renderer1.setStyles(new Style[] {lineStyle, lineStyle});
			renderer1.setDataSource(getDataSource1());
		}
		
		return renderer1;
	}
	private DefaultDataSource dataSource1;
	protected DefaultDataSource getDataSource1() {
		if (dataSource1 == null) {
			dataSource1 = new DefaultDataSource();
		}
		return dataSource1;
	}
	
	
}
