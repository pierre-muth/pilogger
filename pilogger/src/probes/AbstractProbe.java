package probes;

import javax.swing.JComponent;

import datachannel.DataChannel;

/**
 * Probe add listener to all provided DataChannels
 * and should provide an array of these DataChannels
 */
public abstract class AbstractProbe {
	abstract public DataChannel[] getChannels();
	abstract public JComponent[] getGuiComponents();
}
