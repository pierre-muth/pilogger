package datachannel;

/**
 * Probe add listener to all provided DataChannels
 * and should provide an array of these DataChannels
 */
public abstract class AbstractProbe {
	
	abstract public DataChannel[] getChannels();

	public void addDataListenerToChannels(DataChannelListener l) {
		for (int i = 0; i < getChannels().length; i++) {
			getChannels()[i].addDataChannelListener(l);
		}
	}
}
