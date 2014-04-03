package probes;

import java.io.IOException;

import javax.swing.JComponent;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import datachannel.DataChannel;

public class HMC5983probe extends AbstractProbe {
	public static final int HMC5983_I2C_ADDR   = 0x1E;
	private I2CDevice hmc5983device;
	private DataChannel magXChannel = new DataChannel("Magnetic field X", "Magnetic_field_x");
	private DataChannel magYChannel = new DataChannel("Magnetic field Y", "Magnetic_field_y");
	private DataChannel magZChannel = new DataChannel("Magnetic field Z", "Magnetic_field_z");

	// HMC5983 Registers
	public static final int CONFIG_A           = 0x00; // R/W
	public static final int CONFIG_B           = 0x01; // R/W
	public static final int MODE	           = 0x02; // R/W
	public static final int DATA_X_H	       = 0x03; // R
	public static final int DATA_X_L	       = 0x04; // R
	public static final int DATA_Y_H	       = 0x05; // R
	public static final int DATA_Y_L	       = 0x06; // R
	public static final int DATA_Z_H	       = 0x07; // R
	public static final int DATA_Z_L	       = 0x08; // R
	public static final int STATUS		       = 0x09; // R
	public static final int ID_A		       = 0x0A; // R
	public static final int ID_B		       = 0x0B; // R
	public static final int ID_C		       = 0x0C; // R
	public static final int TEMP_H		       = 0x31; // R
	public static final int TEMP_L		       = 0x32; // R
	
	
	/**
	 * HMC5983 magnetic probe on I2C bus
	 * @param bus Pi4J I2CBus object
	 * @throws IOException
	 */
	public HMC5983probe(I2CBus bus) throws IOException {
		hmc5983device = bus.getDevice(HMC5983_I2C_ADDR);
		
		
	}
	
	@Override
	public DataChannel[] getChannels() {
		return new DataChannel[] {magXChannel, magYChannel, magZChannel};
	}

	@Override
	public JComponent[] getGuiComponents() {
		// TODO Auto-generated method stub
		return null;
	}

}
