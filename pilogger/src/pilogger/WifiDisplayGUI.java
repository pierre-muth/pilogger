package pilogger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class WifiDisplayGUI extends JPanel{

	public WifiDisplayGUI() {
		setLayout( new GridBagLayout() );
		setBackground(Color.gray);
		setMinimumSize(new Dimension(320, 240));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 0, 1, 0);
		add(getJpWifiHeader(), c);
	}

	private JPanel jpWifiHeader;
	private JPanel getJpWifiHeader() {
		if (jpWifiHeader == null) {
			jpWifiHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			jpWifiHeader.setBackground(Color.black);
			jpWifiHeader.add(getWifiBackButton());
			jpWifiHeader.add(getJlWifiTime());
		}
		return jpWifiHeader;
	}
	private JButton wifiBackButton;
	public JButton getWifiBackButton() {
		if (wifiBackButton == null) {
			wifiBackButton = new JButton("24h             Last update: ");
			wifiBackButton.setBackground(Color.black);
			wifiBackButton.setForeground(Color.white);
			wifiBackButton.setBorder(new EmptyBorder(0, 0, 0, 0));
			wifiBackButton.setFont(PiloggerGUI.labelFont);
		}
		return wifiBackButton;
	}
	private JLabel jlWifiTime;
	public JLabel getJlWifiTime() {
		if (jlWifiTime == null) {
			jlWifiTime = new JLabel();
			jlWifiTime.setForeground(Color.white);
			jlWifiTime.setBackground(Color.black);
			jlWifiTime.setFont(ChannelPanel.titleFont);
			jlWifiTime.setText(" - ");
		}
		return jlWifiTime;
	}
}
