package pilogger;

import java.util.TimerTask;

public class HTMLgenerator extends TimerTask {
	private ProbeManager manager;
	
	public HTMLgenerator(ProbeManager manager) {
		this.manager = manager;
	}

	@Override
	public void run() {

		System.out.println("PiLogger recording...");
		// TODO generate dynamically HTML files
		
	}

}
