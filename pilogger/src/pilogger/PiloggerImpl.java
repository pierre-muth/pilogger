package pilogger;

import java.awt.Font;
import java.io.IOException;

import cern.jdve.data.DefaultDataSet;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

import datachannel.DataChannelListener;
import datachannel.DataReceivedEvent;

public class PiloggerImpl extends PiloggerGUI implements DataChannelListener{
	private BMP085channels bmp085Probe;
    private GeigerChannel geigerCounter;
    private int dataCountTemperatureIn = 0;
    private int dataCountPressure = 0;

    public PiloggerImpl() {
    	initDataSets();
    	initCom();
        initI2C();
    }

	@Override
	public void dataReceived(DataReceivedEvent dataReceivedEvent) {
		if (dataReceivedEvent.channel == bmp085Probe.pressureChannel) {
    		if (getDataSetPressure().getDataCount() > 20000) {
    			getDataSetPressure().remove(0, 10);
    		}
    		getDataSetPressure().add(dataCountPressure, dataReceivedEvent.dataValue);
    		dataCountPressure++;
    	}
    	if (dataReceivedEvent.channel == bmp085Probe.temperatureChannel) {
    		if (getDataSetTemperatureIn().getDataCount() > 20000) {
    			getDataSetTemperatureIn().remove(0, 10);
    		}
    		getDataSetTemperatureIn().add(dataCountTemperatureIn, dataReceivedEvent.dataValue);
    		dataCountTemperatureIn++;
    	}
    	if (dataReceivedEvent.channel == geigerCounter.geigerChannel) {
//    		dataset.add(dataset.getDataCount(), dataReceivedEvent.dataValue);
    	}
	}
	
	private void initDataSets() {
		getHourPdataSource().addDataSet(getDataSetPressure());
		getHourT1dataSource().addDataSet(getDataSetTemperatureIn());
	}
	
	private void initI2C() {
    	try {
			final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_0);
			bmp085Probe = new BMP085channels(bus);
			bmp085Probe.addDataChannelListener(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	private void initCom() {
    	try {
			final Serial serial = SerialFactory.createInstance();
			geigerCounter = new GeigerChannel(serial);
			geigerCounter.addDataChannelListener(this);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
    }
	private DefaultDataSet dataSetTemperatureIn;
    private DefaultDataSet getDataSetTemperatureIn() {
    	if (dataSetTemperatureIn == null) {
    		dataSetTemperatureIn = new DefaultDataSet("Home Temperature");
    	}
    	return dataSetTemperatureIn;
    }
    private DefaultDataSet dataSetPressure;
    private DefaultDataSet getDataSetPressure() {
    	if (dataSetPressure == null) {
    		dataSetPressure = new DefaultDataSet("Barometric Pressure");
    	}
    	return dataSetPressure;
    }
    private DefaultDataSet dataSetGeiger;
    private DefaultDataSet getDataSetGeiger() {
    	if (dataSetGeiger == null) {
    		dataSetGeiger = new DefaultDataSet("Background Radiation");
    	}
    	return dataSetGeiger;
    }
    
}
