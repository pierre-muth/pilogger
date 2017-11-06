package pilogger;

import probes.AbstractProbe;

public interface ProbeManager {
	public void addProbe(final AbstractProbe probe);
	public DataChannel[] getChannels();
}
