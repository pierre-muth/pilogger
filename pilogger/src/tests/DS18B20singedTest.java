package tests;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DS18B20singedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Short s1, s2;
		s1 = (short) ((0b11111110 << 8) + 0b01101111);
		s2 = (short) ((0b00000000 << 8) + 0b10000001);
		double t1 = s1 *0.0625;
		double t2 = s2 *0.0625;
		
		System.out.println("s1 : "+s1+", t1: "+t1+", s2: "+s2+", t2: "+t2);
		
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put((byte) 0b11111110);
		bb.put((byte) 0b01101111);
		short shortVal = bb.getShort(0);
		
		System.out.println(shortVal*0.0625);
		
	}

}
