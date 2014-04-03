package tests;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class HMC5983test {
	public static final int HMC5983_I2C_ADDR   = 0x1E;
	private I2CDevice hmc5983device;

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

	public HMC5983test() throws IOException {
		// get I2C bus instance
		final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		hmc5983device = bus.getDevice(HMC5983_I2C_ADDR);

		
		
		for (int i = 0; i < 13; i++) {
			System.out.print(byteToHex( (byte) hmc5983device.read(i) ) +" ");
		}
		System.out.print("\n");

		hmc5983device.write(CONFIG_A, (byte)0b11110000 );
		hmc5983device.write(CONFIG_B, (byte)0b00100000 );
		hmc5983device.write(MODE	, (byte)0b00000001 );

		for (int i = 0; i < 13; i++) {
			System.out.print(byteToHex( (byte) hmc5983device.read(i) ) +" ");
		}
		System.out.print("\n");
		
		while (true) {
			
			ByteBuffer bb = ByteBuffer.allocate(6);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 3; i < 9; i++) {
				bb.put((byte) hmc5983device.read(i));
			}			
			short xval = bb.getShort(0);
			short yval = bb.getShort(1);
			short zval = bb.getShort(2);
			
			System.out.println("X:"+xval+" Y:"+yval+" Z:"+zval);
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			hmc5983device.write(MODE	, (byte)0b00000001 );
			

		}        

	}

	public static String byteToHex(byte b) {
		final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		char[] hexChars = new char[2];
		int v;
		v = b & 0xFF;
		hexChars[0] = hexArray[v >>> 4];
		hexChars[1] = hexArray[v & 0x0F];
		return new String(hexChars);
	}    

	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}    

	public static void main(String[] args) throws IOException {
		new HMC5983test();
	}

}


//package tests;
//
//import java.io.IOException;
//
//import com.pi4j.io.i2c.I2CBus;
//import com.pi4j.io.i2c.I2CDevice;
//import com.pi4j.io.i2c.I2CFactory;



//
//public class HMC5983test {
//	public static final int HMC5983_I2C_ADDR   = 0x1E;
//	private I2CDevice hmc5983device;
//	
//	// HMC5983 Registers
//	public static final int CONFIG_A           = 0x00; // R/W
//	public static final int CONFIG_B           = 0x01; // R/W
//	public static final int MODE	           = 0x02; // R/W
//	public static final int DATA_X_H	       = 0x03; // R
//	public static final int DATA_X_L	       = 0x04; // R
//	public static final int DATA_Y_H	       = 0x05; // R
//	public static final int DATA_Y_L	       = 0x06; // R
//	public static final int DATA_Z_H	       = 0x07; // R
//	public static final int DATA_Z_L	       = 0x08; // R
//	public static final int STATUS		       = 0x09; // R
//	public static final int ID_A		       = 0x0A; // R
//	public static final int ID_B		       = 0x0B; // R
//	public static final int ID_C		       = 0x0C; // R
//	public static final int TEMP_H		       = 0x31; // R
//	public static final int TEMP_L		       = 0x32; // R
//	
//	public HMC5983test() throws IOException {
//		 // get I2C bus instance
//        final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
//        hmc5983device = bus.getDevice(HMC5983_I2C_ADDR);
//        
//        
//        for (int i = 0; i < 12; i++) {
//        	System.out.println(hmc5983device.read(i));
//		}
//        
//        hmc5983device.write(CONFIG_A, (byte)0b11101000 );
//        hmc5983device.write(CONFIG_B, (byte)0b00000000 );
//        hmc5983device.write(MODE	, (byte)0b00000000 );
//        
//        for (int i = 0; i < 10; i++) {
//        	System.out.println(hmc5983device.read(DATA_X_L));
//        	System.out.println(hmc5983device.read(DATA_X_H));
//        	System.out.println(hmc5983device.read(DATA_Y_L));
//        	System.out.println(hmc5983device.read(DATA_Y_H));
//        	System.out.println(hmc5983device.read(DATA_Z_L));
//        	System.out.println(hmc5983device.read(DATA_Z_H));
//        	try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//        
//        
//        
//	}
//	
//	
//	
//	
//	
//	public static void main(String[] args) throws IOException {
//		new HMC5983test();
//	}
//
//}
