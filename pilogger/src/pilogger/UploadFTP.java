package pilogger;

import java.awt.AWTException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class UploadFTP extends TimerTask{
	private static final String HOSTNAME = "ftpperso.free.fr";
	private static final String LOGIN = "***";
	private static final String PWD = "***";

	public static synchronized boolean store(File localFile) {
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect(HOSTNAME);
			
			if (! FTPReply.isPositiveCompletion(  ftp.getReplyCode() )) {
				ftp.disconnect();
				return false;
			}

			ftp.login(LOGIN, PWD);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();

			InputStream input = new FileInputStream(localFile);
			ftp.storeFile("pilogger/" + localFile.getName(), input);
			ftp.logout();
			ftp.disconnect();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if(ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {
					// do nothing
				}
			}
		}

	}
	
	@Override
	public void run() {
//		try {
//			ScreenCapture.capture(ProbeManager.onlineFileLocalDirectory);
//		} catch (IOException | AWTException e1) {
//			System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+
//					": Fail to capture screen");
//		}
		
		File directory = new File(ProbeManager.onlineFileLocalDirectory);
		File[] files = directory.listFiles();
		int i = 0;
		try {
			System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+": Start "+files.length+" FTP Uploads ");
			
			for (i = 0; i < files.length; i++) {
				if ( !files[i].isDirectory() ) {
					boolean success = UploadFTP.store(files[i]);
					if (!success) System.out.println( new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+
								": Fail "+ files[i].getName());
				}
				Thread.sleep(1000);
			}
			
			System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+": Ended FTP Uploads ");
			
		} catch (InterruptedException e) {
			System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+
					": Fail uploads at "+i+"/"+ files.length+" : "+files[i].getName());
		}


	}



}
