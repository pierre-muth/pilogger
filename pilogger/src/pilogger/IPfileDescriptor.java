package pilogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.TimerTask;

public class IPfileDescriptor extends TimerTask {
	private ProbeManager manager;
	
	public IPfileDescriptor(ProbeManager manager) {
		this.manager = manager;
	}

	@Override
	public void run() {

		try {
			Process p = Runtime.getRuntime().exec("ifconfig");
			BufferedReader reader = new BufferedReader( new InputStreamReader(p.getInputStream()));
			
			Path directory = Paths.get(DataChannel.onlineFileLocalDirectory);
			Path fileIP = directory.resolve("ip.txt");
			BufferedWriter fileWriter = Files.newBufferedWriter(fileIP, Charset.defaultCharset(), new OpenOption[] {StandardOpenOption.CREATE});

			String line = reader.readLine();
			while (line != null) {
				fileWriter.write(line+"\n");
				line = reader.readLine();
			}
			p.destroy();
			reader.close();
			fileWriter.flush();
			fileWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
