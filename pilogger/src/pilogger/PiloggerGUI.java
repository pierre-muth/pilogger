package pilogger;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import cern.jdve.Chart;
import cern.jdve.Style;
import cern.jdve.data.DefaultDataSource;
import cern.jdve.renderer.DiffAreaChartRenderer;
import cern.jdve.renderer.PolylineChartRenderer;
import cern.jdve.scale.TimeStepsDefinition;

public class PiloggerGUI extends JPanel {
	public static Font labelFont = new Font("Dialog", Font.PLAIN, 9);
	public static final String DATE_PATERN = "yyyy.MM.dd HH:mm:ss";
	private Color line0ChartColor = new Color(255, 255, 255, 255);
	private Color line1ChartColor = new Color(128, 128, 128, 255);
	private Color areaChartColor  = new Color(55, 55, 55, 100);
	private Style line0Style = new Style(new BasicStroke(1.0f), line0ChartColor, line0ChartColor);
	private Style line1Style = new Style(new BasicStroke(1.0f), line1ChartColor, line1ChartColor);
	private Style areaStyle  = new Style(new BasicStroke(0.0f), areaChartColor, areaChartColor);
	private EmptyBorder emptyBorder = new EmptyBorder(0, 0, 0, 0);
	private LineBorder greyBorder = new LineBorder(Color.gray, 1);
	private static final String CARD_CHART = "Chart";
	private static final String CARD_CONF = "Config";
	
	/**
	 * Pilogger application GUI. 
	 */
	
	public PiloggerGUI() {
		setBackground(Color.black);
		setBorder(emptyBorder);
		setLayout(getCardLayout());
		add(getChartCard(), CARD_CHART);
		add(getConfCard(), CARD_CONF);
		setPreferredSize(new Dimension(320, 240));
	}
	
	private CardLayout cardLayout;
	private CardLayout getCardLayout() {
		if (cardLayout == null) {
			cardLayout = new CardLayout();
		}
		return cardLayout;
	}
	
	private JPanel chartCard;
	private JPanel getChartCard() {
		if (chartCard == null) {
			chartCard = new JPanel();
			chartCard.setBackground(Color.black);
			chartCard.setLayout(new BorderLayout(0, 0));
			chartCard.add(getMainChart(), BorderLayout.CENTER);
			chartCard.add(getNorthConfigPanel(), BorderLayout.NORTH);
			chartCard.add(getLedPanel(), BorderLayout.SOUTH);
		}
		return chartCard;
	}
	
	private JPanel ledPanel;
	protected JPanel getLedPanel() {
		if (ledPanel == null) {
			ledPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
			ledPanel.setBackground(Color.black);
		}
		return ledPanel;
	}
	
	private JPanel confCard;
	private JPanel getConfCard() {
		if (confCard == null) {
			confCard = new JPanel(new BorderLayout());
			confCard.setBackground(Color.black);
			confCard.add(getConfBackButton(), BorderLayout.NORTH);
			confCard.add(getProbeCustomPanel(), BorderLayout.CENTER);
			confCard.add(getChannelReloadPanel(), BorderLayout.SOUTH);
		}
		return confCard;
	}
	
	private JPanel probeCustomPanel;
	protected JPanel getProbeCustomPanel() {
		if (probeCustomPanel == null) {
			probeCustomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			probeCustomPanel.setBackground(Color.black);
			probeCustomPanel.setBorder(getCustomPanelBorder());
		}
		return probeCustomPanel;
	}
	
	private TitledBorder customPanelBorder;
	private TitledBorder getCustomPanelBorder() {
		if (customPanelBorder == null) {
			customPanelBorder = new TitledBorder("Probes utilities");
			customPanelBorder.setTitleColor(Color.white);
			customPanelBorder.setTitleFont(PiloggerGUI.labelFont);
			customPanelBorder.setBorder(new LineBorder(Color.gray));
		}
		return customPanelBorder;
	}
	
	private JPanel channelReloadPanel;
	protected JPanel getChannelReloadPanel() {
		if (channelReloadPanel == null) {
			channelReloadPanel = new JPanel(new GridLayout(10, 2));
			channelReloadPanel.setBackground(Color.black);
			channelReloadPanel.setBorder(getReloadPanelBorder());
		}
		return channelReloadPanel;
	}
	
	private TitledBorder reloadPanelBorder;
	private TitledBorder getReloadPanelBorder() {
		if (reloadPanelBorder == null) {
			reloadPanelBorder = new TitledBorder("Reload from log file");
			reloadPanelBorder.setTitleColor(Color.white);
			reloadPanelBorder.setTitleFont(PiloggerGUI.labelFont);
			reloadPanelBorder.setBorder(new LineBorder(Color.gray));
		}
		return reloadPanelBorder;
	}
	
	private JButton confBackButton;
	private JButton getConfBackButton() {
		if (confBackButton == null) {
			confBackButton = new JButton("Back");
			confBackButton.setBorder(new LineBorder(Color.gray));
			confBackButton.setBackground(Color.black);
			confBackButton.setForeground(Color.white);
			confBackButton.setPreferredSize(new Dimension(30, 12));
			confBackButton.setFont(PiloggerGUI.labelFont);
			confBackButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					getCardLayout().show(PiloggerGUI.this, CARD_CHART);
				}
			});
		}
		return confBackButton;
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
			scale0Button.setFocusable(false);
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
			scale1Button.setFocusable(false);
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
			timeScaleButton.setFocusable(false);
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
			scaleTimeMenu.add(getConfigItem());
		}
		return scaleTimeMenu;
	}
	
	private JMenuItem configItem;
	private JMenuItem getConfigItem() {
		if (configItem == null) {
			configItem = new JMenuItem(CARD_CONF);
			configItem.setBackground(Color.black);
			configItem.setForeground(Color.white);
			configItem.setFont(PiloggerGUI.labelFont);
			configItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getCardLayout().show(PiloggerGUI.this, CARD_CONF);
				}
			});
		}
		return configItem;
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
			mainChart.getYScale().setLabelForeground(Color.white);
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
	        
	        TimeStepsDefinition stepsDefinition = new TimeStepsDefinition();
	        mainChart.getXScale().setStepsDefinition(stepsDefinition);
	        
	        mainChart.addRenderer(1, getAreaRenderer1());
	        mainChart.addRenderer(0, getAreaRenderer0());
	        mainChart.addRenderer(1, getLineRenderer1());
	        mainChart.addRenderer(0, getLineRenderer0());
	         
//	        mainChart.addInteractor(ChartInteractor.DATA_PICKER);
	        
		}
		
		return mainChart;
	}
	private PolylineChartRenderer lineRenderer0;
	private PolylineChartRenderer getLineRenderer0() {
		if (lineRenderer0 == null) {
			lineRenderer0 = new PolylineChartRenderer();
			lineRenderer0.setStyles(new Style[] {line0Style, line0Style});
			lineRenderer0.setDataSource(getLineDataSource0());
		}
		return lineRenderer0;
	}
	private PolylineChartRenderer lineRenderer1;
	private PolylineChartRenderer getLineRenderer1() {
		if (lineRenderer1 == null) {
			lineRenderer1 = new PolylineChartRenderer();
			lineRenderer1.setStyles(new Style[] {line1Style, line1Style});
			lineRenderer1.setDataSource(getLineDataSource1());
		}
		
		return lineRenderer1;
	}
	private DiffAreaChartRenderer areaRenderer0;
	private DiffAreaChartRenderer getAreaRenderer0() {
		if (areaRenderer0 == null) {
			areaRenderer0 = new DiffAreaChartRenderer();
			areaRenderer0.setStyles(new Style[] {areaStyle, areaStyle});
			areaRenderer0.setDataSource(getAreaDataSource0());
		}
		
		return areaRenderer0;
	}
	private DiffAreaChartRenderer areaRenderer1;
	private DiffAreaChartRenderer getAreaRenderer1() {
		if (areaRenderer1 == null) {
			areaRenderer1 = new DiffAreaChartRenderer();
			areaRenderer1.setStyles(new Style[] {areaStyle, areaStyle});
			areaRenderer1.setDataSource(getAreaDataSource1());
		}
		
		return areaRenderer1;
	}

	private DefaultDataSource lineDataSource0;
	protected DefaultDataSource getLineDataSource0() {
		if (lineDataSource0 == null) {
			lineDataSource0 = new DefaultDataSource();
		} 
		return lineDataSource0;
	}
	private DefaultDataSource lineDataSource1;
	protected DefaultDataSource getLineDataSource1() {
		if (lineDataSource1 == null) {
			lineDataSource1 = new DefaultDataSource();
		}
		return lineDataSource1;
	}
	private DefaultDataSource areaDataSource0;
	protected DefaultDataSource getAreaDataSource0() {
		if (areaDataSource0 == null) {
			areaDataSource0 = new DefaultDataSource();
		} 
		return areaDataSource0;
	}
	private DefaultDataSource areaDataSource1;
	protected DefaultDataSource getAreaDataSource1() {
		if (areaDataSource1 == null) {
			areaDataSource1 = new DefaultDataSource();
		}
		return areaDataSource1;
	}
	
}
