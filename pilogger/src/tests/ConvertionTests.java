package tests;

public class ConvertionTests {
	private int cal_AC1 = 408;
	private int cal_AC2 = -72;
	private int cal_AC3 = -14383;
	private int cal_AC4 = 32741;
	private int cal_AC5 = 32757;
	private int cal_AC6 = 23153;
	private int cal_B1 = 6190;
	private int cal_B2 = 4;
	private int cal_MB = -32768;
	private int cal_MC = -8711;
	private int cal_MD = 2868;
	private int oss = 0;
	
	public ConvertionTests() {
		convertPressureTemp(23843, 27898);
	}
	
	public void convertPressureTemp(int rawPressure, int rawTemperature) {
		double temperature = 0.0;
		double pressure = 0.0;
		double x1 = ((rawTemperature - cal_AC6) * cal_AC5) / 32768;
		double x2 = (cal_MC *2048) / (x1 + cal_MD);
		double b5 = x1 + x2;
		temperature = ((b5 + 8) / 16) / 10.0;
		
		double b6 = b5 - 4000;
		x1 = (cal_B2 * (b6 * b6 / 4096)) / 2048;
		x2 = (cal_AC2 * b6) / 2048;
		double x3 = x1 + x2;
		double b3 = ((cal_AC1 * 4 + x3) * ( Math.pow(2, oss)) +2) / 4;
		x1 = (cal_AC3 * b6) / 8192;
		x2 = (cal_B1 * (b6 * b6 / 4096)) / 65536;
		x3 = ((x1 + x2) + 2) / 4;
		double b4 = (cal_AC4 * (x3 + 32768)) / 32768;
		double b7 = (rawPressure - b3) * (50000 / Math.pow(2, oss));
		if (b7 < 0x80000000) pressure = (b7 * 2) / b4;
		else pressure = (b7 / b4) * 2;
		x1 = (pressure / 256) * (pressure / 256);
		x1 = (x1 * 3038) / 65536;
		x2 = (-7375 * pressure) / 65536;
	    pressure = pressure + ((x1 + x2 + 3791) / 16);
			
	    System.out.println("p= "+pressure+", t= "+temperature);
	    
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ConvertionTests();		
	}

}
