package pilogger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LogFile {
	
	public static synchronized boolean store(Path logFilePath, AveragedDataPoint averagedDataPoint) {
		BufferedWriter logFileWriter;
		try {
			logFileWriter = Files.newBufferedWriter(logFilePath, Charset.defaultCharset(), new OpenOption[] {
				StandardOpenOption.APPEND, StandardOpenOption.CREATE});
			
			logFileWriter.write(Long.toString(averagedDataPoint.time)
					+", "
							+Double.toString(averagedDataPoint.value)
							+", "
							+Double.toString(averagedDataPoint.max)
							+", "
							+Double.toString(averagedDataPoint.min)
							+"\n");
			logFileWriter.flush();
			logFileWriter.close();
			return true;
		} catch (IOException e) {
			return false;
		} 
	}
}
