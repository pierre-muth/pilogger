package datachannel;

import java.util.ArrayList;

public abstract class AbstractDataChannel {
	private ArrayList<DataChannelListener> dataListenersList = new ArrayList<>();
	
	public void addDataChannelListener(DataChannelListener listener) {
		dataListenersList.add(listener);
	}
	public void removeDataChannelListener(DataChannelListener listener) {
		dataListenersList.remove(listener);
	}
	public void clearDataChannelListener() {
		dataListenersList.clear();
	}
	
	public void fireDataEvent(DataReceivedEvent dataReceivedEvent) {
		for (DataChannelListener dataListener : dataListenersList) {
			dataListener.dataReceived(dataReceivedEvent);
		}
	}
}
