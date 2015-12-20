package pilogger;

public class Utils {
	final static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 3];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 3] = hexArray[v >>> 4];
			hexChars[j * 3 + 1] = hexArray[v & 0x0F];
			hexChars[j * 3 + 2] = ' ';
		}
		return new String(hexChars);
	}
	
	public static String byteToHex(byte aByte) {
		int v = aByte & 0xFF;
		return new String ( new char[] {hexArray[v >>> 4], hexArray[v & 0x0F]} );
	}
}
