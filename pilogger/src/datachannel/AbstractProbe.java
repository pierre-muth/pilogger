package datachannel;

import javax.swing.JComponent;

/**
 * Probe add listener to all provided DataChannels
 * and should provide an array of these DataChannels
 */
public abstract class AbstractProbe {
	abstract public DataChannel[] getChannels();
	abstract public JComponent[] getGuiComponents();
}
