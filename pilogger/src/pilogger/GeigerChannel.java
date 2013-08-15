package pilogger;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialPortException;

import datachannel.AbstractDataChannel;
import datachannel.DataChannel;
import datachannel.DataReceivedEvent;

public class GeigerChannel extends AbstractDataChannel implements SerialDataListener {
	public static final int GEIGER_SERIAL_SPEED = 9600;
	public DataChannel geigerChannel = new DataChannel("geiger");
	private String inbuf = "";
	 
	public GeigerChannel(Serial serial) throws SerialPortException {
		// open the default serial port provided on the GPIO header
		serial.open(Serial.DEFAULT_COM_PORT, GEIGER_SERIAL_SPEED);
		serial.addListener(this);
	}

	@Override
	public void dataReceived(SerialDataEvent serialDataEvent) {
		String input = serialDataEvent.getData();
		inbuf = inbuf.concat(input);
    	if (input.endsWith("\n")) {
    		String[] blocks = inbuf.split(", ");
    		if (blocks.length > 5) {
    			double dose = Double.valueOf(blocks[5]);
    			DataReceivedEvent geigerEvent = new DataReceivedEvent(dose, geigerChannel);
    			fireDataEvent(geigerEvent);
    		}
    		inbuf = "";
    	}
	}
	
	
	
}
