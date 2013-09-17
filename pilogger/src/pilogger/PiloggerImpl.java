package pilogger;

import java.io.IOException;

import tests.BMP085probeSimulation;
import tests.GeigerProbeSimulation;

import cern.jdve.data.DefaultDataSet;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class PiloggerImpl extends PiloggerGUI{
	private BMP085probe bmp085Probe;
    private GeigerProbe geigerCounter;
    private SystemProbe systemProbe;
    
    private ProbeManager probeManager = new ProbeManager(this);

    /**
     * Implementation of the Pilogger application GUI
     * Initialize links and Probes
     */
    public PiloggerImpl(boolean simulation) {
    	if (simulation) {
    		probeManager.addProbe(new BMP085probeSimulation());
    		probeManager.addProbe(new GeigerProbeSimulation());
    		probeManager.addProbe(new SystemProbe());
    	} else {
    		initI2CandBMP085probe();
    		initComAndGeigerProbe(); 
    		initSystemProbe();
    	}
    	
    } 

    private void initComAndGeigerProbe() {
    	try {
    		final Serial serial = SerialFactory.createInstance();
    		geigerCounter = new GeigerProbe(serial);
    		probeManager.addProbe(geigerCounter);
    	} catch (SerialPortException e) {
    		e.printStackTrace();
    	}
    }
	private void initI2CandBMP085probe() {
    	try {
			final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_0);
			bmp085Probe = new BMP085probe(bus);
			probeManager.addProbe(bmp085Probe);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	private void initSystemProbe() {
		systemProbe = new SystemProbe();
		probeManager.addProbe(systemProbe);
	}
	
}
