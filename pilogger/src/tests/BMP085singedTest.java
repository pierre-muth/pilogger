package tests;

public class BMP085singedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Short s1, s2;
		s1 = (short) ((0b11111110 << 8) + 0b01101111);
		s2 = (short) ((0b00000001 << 8) + 0b10010001);
		double t1 = s1 *0.0625;
		double t2 = s2 *0.0625;
		
		System.out.println("s1 : "+s1+", t1: "+t1+", s2: "+s2+", t2: "+t2);
		
		
		
	}

}
