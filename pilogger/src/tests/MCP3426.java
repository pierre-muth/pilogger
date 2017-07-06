package tests;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class MCP3426
{
	public static void main(String args[]) throws Exception
	{
		// Create I2C bus
		I2CBus Bus = I2CFactory.getInstance(I2CBus.BUS_1);
		// Get I2C device, MCP3426 I2C address is 0x68(104)
		I2CDevice device = Bus.getDevice(0x68);
		Thread.sleep(300);

		// Select configuration command
		// Continuous conversion mode, channel-1, 16-bit resolution
		device.write((byte)0b0001_1000);
		Thread.sleep(500);

		for (int i = 0; i < 50; i++) {
			// Read 2 bytes of data
			// raw_adc msb, raw_adc lsb
			byte[] data = new byte[2];
			device.read(0x00, data, 0, 2);

			// Convert the data to 16-bits
			ByteBuffer bb = ByteBuffer.allocate(2);
			bb.order(ByteOrder.BIG_ENDIAN);
			bb.put((byte) (data[0] & 0xFF));
			bb.put((byte) (data[1] & 0xFF));
			short raw_adc = bb.getShort(0);

			System.out.printf("Digital Value of Analog Input is : %d %n", raw_adc);
			device.write((byte)0b0001_1000);
			
			Thread.sleep(500);
		}
	}
}