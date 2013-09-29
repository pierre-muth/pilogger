package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class TestFTP {
	
	private FTPClient ftp;

	public TestFTP() {
		ftp = new FTPClient();
		try {
			ftp.connect("ftpperso.free.fr");
			
			if (! FTPReply.isPositiveCompletion(  ftp.getReplyCode() )) {
				ftp.disconnect();
			}
			
			ftp.login("muth.inc", "gx88sodw");
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();
			
			InputStream input = new FileInputStream(new File("/home/pi/projects/pilogger/logs/Outside_Battery.csv"));
			ftp.storeFile("pilogger/" + "Outside_Battery2.csv", input);
			
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new TestFTP();
		
	}

}
