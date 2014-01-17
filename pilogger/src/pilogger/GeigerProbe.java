package pilogger;

import javax.swing.JComponent;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialPortException;

import datachannel.AbstractProbe;
import datachannel.DataChannel;
import datachannel.DataReceivedEvent;

public class GeigerProbe extends AbstractProbe implements SerialDataListener {
	public static final int GEIGER_SERIAL_SPEED = 9600;
	public DataChannel geigerChannel = new DataChannel("Backgound Radiation", "Backgound_Radiation");
	private String inbuf = "";
	 
	/**
	 * Geiger Counter connected to the serial port
	 * @param serial Serial port com Pi4J object
	 * @throws SerialPortException
	 */
	public GeigerProbe(Serial serial) throws SerialPortException {
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
    			geigerChannel.newData(dose);
    		}
    		inbuf = "";
    	}
	}

	@Override
	public DataChannel[] getChannels() {
		return new DataChannel[] {geigerChannel};
	}

	@Override
	public JComponent[] getGuiComponents() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
