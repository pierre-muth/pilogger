package pilogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import dbwConnection.DbwConnection;

public class UploadMySQL extends TimerTask {
	private static final String location = "***";
	private static final String username = "***";
	private static final String pwd = "***";
	private static final String host = "***";
	private static final String dataBase = "***";
	private static final String tableInstant = "pilogger";
	public static final String MySQL_DATE_PATERN = "yyyy-MM-dd HH:mm:ss";  //2016-01-13 13:20:01
	private static DbwConnection conn = null;

	public static boolean DEBUG = false;

	private ProbeManagerSwing manager;

	public UploadMySQL(ProbeManagerSwing manager) {
		this.manager = manager;
	}

	@Override
	public void run() {
		DataChannel[] channels = manager.getChannels();

		for (int i = 0; i < channels.length; i++) {
			if (channels[i].getLastAveragedDataPoint() != null) {
				storeInstantValue(channels[i].channelName, channels[i].getLastAveragedDataPoint());
			}
			if (DEBUG)
				System.out.println("upload MySQL: "+(i+1)+"/"+(channels.length)+": "+channels[i].channelName);

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static synchronized void storeInstantValue(final String channelName, AveragedDataPoint dataPoint) {
		final double value = dataPoint.value;
		long time = dataPoint.time;
		final String timeString = new SimpleDateFormat(MySQL_DATE_PATERN).format( new Date(time) );

		try {
			conn = new DbwConnection(location, username, pwd, host, dataBase);
			conn.executeQuery("UPDATE "+tableInstant+" SET value='"+value+"', time='"+timeString+"' WHERE channel_name='"+channelName+"';");
			conn.close();
		} catch (Exception e) {
			System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+
					" Error on Instant updload: "+channelName);
			if (DEBUG)
				System.out.println(e);
		} finally {
			conn.close();
		}

	}

	public static synchronized boolean storeValueAndDeleteLast(String tableName, String timeScale, AveragedDataPoint dataPoint) {
		String tableCompleteName = tableName+timeScale;
		double value = dataPoint.value;
		double min = dataPoint.min;
		double max = dataPoint.max;
		long time = dataPoint.time;
		String timeString = new SimpleDateFormat(MySQL_DATE_PATERN).format( new Date(time) );
		String queryAdd = "INSERT INTO "+tableCompleteName+"(time, value, min, max) VALUES ('"+
				timeString+"', '"+value+"', '"+min+"', '"+max+"');";
		String queryDeleteLast = "DELETE FROM "+tableCompleteName+" ORDER BY time ASC LIMIT 1;";
		try {
			DbwConnection conn = new DbwConnection(location, username, pwd, host, dataBase);
			conn.executeQuery(queryAdd);
			conn.executeQuery(queryDeleteLast);
			conn.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static synchronized boolean storeValue(String tableName, String timeScale, AveragedDataPoint dataPoint) {
		String tableCompleteName = tableName+timeScale;
		double value = dataPoint.value;
		double min = dataPoint.min;
		double max = dataPoint.max;
		long time = dataPoint.time;
		String timeString = new SimpleDateFormat(MySQL_DATE_PATERN).format( new Date(time) );
		String queryAdd = "INSERT INTO "+tableCompleteName+"(time, value, min, max) VALUES ('"+
				timeString+"', '"+value+"', '"+min+"', '"+max+"');";
		try {
			DbwConnection conn = new DbwConnection(location, username, pwd, host, dataBase);
			conn.executeQuery(queryAdd);
			conn.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static synchronized boolean ceateTable(String tableName) {
		String queryCreate = "CREATE TABLE "+tableName+"("+
				"time DATETIME NOT NULL ,"+
				"value DOUBLE NOT NULL ,"+
				"min DOUBLE NOT NULL ,"+
				"max DOUBLE NOT NULL"+
				") ENGINE = MYISAM DEFAULT CHARSET = latin1 COLLATE = latin1_general_ci;" ;

		try {
			DbwConnection conn = new DbwConnection(location, username, pwd, host, dataBase);
			conn.executeQuery(queryCreate);
			conn.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean emptyTable(String tableName, String timeScale) {
		String queryEmpty = "TRUNCATE "+tableName+timeScale+";" ;

		try {
			DbwConnection conn = new DbwConnection(location, username, pwd, host, dataBase);
			conn.executeQuery(queryEmpty);
			conn.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	/*


		TRUNCATE 

		DELETE FROM table ORDER BY id ASC LIMIT 4, 18446744073709551615;

		DELETE
		FROM  'Atmospheric_PressureRealtime' 
		ORDER BY  'Atmospheric_PressureRealtime'.'time' ASC 
		LIMIT 1

		 INSERT INTO `muth_inc`.`Atmospheric_PressureRealtime` (`time`, `value`, `min`, `max`) 
		 VALUES ('2016-01-10 13:05:04', '22', '21', '23'), 
			 ('2016-01-11 13:05:15', '44', '43', '45'), 
			 ('2016-01-03 13:05:25', '23', '22', '24'), 
			 ('2016-01-26 13:05:38', '55', '54', '56'), 
			 ('2016-01-01 13:05:47', '11', '11', '11'); 


		CREATE TABLE  `muth_inc`.`test` (
		`time` DATETIME NOT NULL ,
		 `value` DOUBLE NOT NULL ,
		 `min` DOUBLE NOT NULL ,
		 `max` DOUBLE NOT NULL
		) ENGINE = MYISAM DEFAULT CHARSET = latin1 COLLATE = latin1_general_ci;

		INSERT INTO  `muth_inc`.`test` 
		SELECT * 
		FROM  `muth_inc`.`Atmospheric_PressureRealtime` ;
	 */

	public static void main(String[] args) {
		//		String query = "DELETE FROM Atmospheric_PressureRealtime ORDER BY time ASC LIMIT 1;";
		//		String query = "INSERT INTO Atmospheric_PressureRealtime (time, value, min, max) VALUES ('2016-01-10 13:05:04', '22', '21', '23')	;";

		String rootName = "Upstairs_Battery";

		//		UploadMySQL.ceateTable(rootName+DataChannel.REALTIME_SUFIX);
		//		UploadMySQL.ceateTable(rootName+DataChannel.HOUR_SUFIX);
		//		UploadMySQL.ceateTable(rootName+DataChannel.DAY_SUFIX);
		//		UploadMySQL.ceateTable(rootName+DataChannel.MONTH_SUFIX);
		//		UploadMySQL.ceateTable(rootName+DataChannel.YEAR_SUFIX);
		//		UploadMySQL.ceateTable(rootName+DataChannel.LONGRANGE_SUFIX);


	}

}
