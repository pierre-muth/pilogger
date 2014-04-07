package probes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;

import pilogger.DataChannel;
import pilogger.PiloggerGUI;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Spi;

public class WirelessProbe extends AbstractProbe implements GpioPinListenerDigital{

	public DataChannel outTemperatureChannel = new DataChannel("Outside Temperature", "Outside_Temperature");
	public DataChannel outLightChannel = new DataChannel("Outside Brightness", "Outside_Light");
	public DataChannel outBatteryChannel = new DataChannel("Outside Battery", "Outside_Battery");
	public DataChannel heatingExhaustChannel = new DataChannel("Heating Exhaust", "Heating_Exhaust");
	public DataChannel heatingInflowChannel = new DataChannel("Heating Inflow", "Heating_Inflow");
	public DataChannel heatingReturnChannel = new DataChannel("Heating Return", "Heating_Return");
	public DataChannel cellarTemperatureChannel = new DataChannel("Cellar Temperature", "Cellar_temperature");
	public DataChannel seismoChannel = new DataChannel("Seismometer", "Seismometer");
	public DataChannel seismoDifferentialChannel = new DataChannel("Seismometer Diff", "Seismometer_Diff", true);
	public DataChannel humidChannel = new DataChannel("Relative Humidity", "Rel_Humidity");
	public DataChannel outTemperature2Channel = new DataChannel("Outside Temperature 2", "Outside_Temperature2");
	
	private DataChannel[] channels = new DataChannel[] {
			outTemperatureChannel, 
			outLightChannel, 
			outBatteryChannel, 
			heatingExhaustChannel, 
			heatingInflowChannel, 
			heatingReturnChannel, 
			cellarTemperatureChannel,
			seismoChannel,
			seismoDifferentialChannel,
			humidChannel,
			outTemperature2Channel};

	private GpioController gpio;
	private GpioPinDigitalOutput CE;
	private GpioPinDigitalInput IRQ;
	private Watchdog watchdog;
	private static final int WATCHDOG_DELAY = 20000;
	
	public WirelessProbe(GpioController gpio) {
		this.gpio = gpio;
		initGPIO();
		initNRF24L01();
		IRQ.addListener(this);

		Timer t = new Timer();
		watchdog = new Watchdog();
		t.schedule(watchdog, WATCHDOG_DELAY, WATCHDOG_DELAY);
	}

	@Override
	public DataChannel[] getChannels() {
		return channels;
	}

	private void initGPIO() {
		CE = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);
		IRQ = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);
		CE.low();
	}

	private void initNRF24L01() {

		byte packet[] = new byte[2];

		// write config register : all IRQ, enable 2bytes CRC, Power UP, Primary RX
		packet[0] = 0b00100000;
		packet[1] = 0x0F;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		// read Config Register
		packet[0] = 0b00000000;
		packet[1] = 0x00;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		// write Feature reg, enable dynamic payload
		packet[0] = 0b00111101;
		packet[1] = 0b00000100;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		// write RF setup : 250Kbps
		packet[0] = 0b00100110;
		packet[1] = 0b00100110;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		// write EN_RXADDR : enable RX address pipe 0 & 1 & 2 
		packet[0] = 0b00100010;
		packet[1] = 0b00000111;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		// write DYNPL, enable dynamic payload for pipe0 & 1 & 2
		packet[0] = 0b00111100;
		packet[1] = 0b00000111;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		//Flush RX FIFO
		packet[0] = (byte) 0xE2;
		Spi.wiringPiSPIDataRW(0, packet, 1);

		// write Status Register : clear interupts
		packet[0] = 0b00100111;
		packet[1] = 0x70;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		CE.high();

	}

	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		if (event.getState().isHigh()) return;

		watchdog.reset();
		
		byte packet[] = new byte[2];

		// read R_RX_PL_WID Register
		packet[0] = 0b01100000;
		packet[1] = 0x00;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		if (packet[0] == 0x0E) {
			// if Status byte mean not interrupt append and RX buffer empty
			// reset the nRF module
			initNRF24L01();
		} else {
			int payloadLenght = packet[1];
			byte readPayload[] = new byte[payloadLenght+1];

			//read RX 
			readPayload[0] = 0b01100001;
			Spi.wiringPiSPIDataRW(0, readPayload, payloadLenght+1);

			//Flush RX FIFO
			packet[0] = (byte) 0xE2;
			Spi.wiringPiSPIDataRW(0, packet, 1);

			//clear interupts
			packet[0] = 0b00100111;
			packet[1] = 0x70;
			Spi.wiringPiSPIDataRW(0, packet, 2);

			processPayload(readPayload);
		}
	}

	private void processPayload (byte[] redPayload) {
		//		System.out.println( Utils.bytesToHex(redPayload) );

		if (redPayload[0] == 0x40) { 		// pipe 0 : solar probe
			processSolarProbe(redPayload);
		} else if (redPayload[0] == 0x42) { // pipe 1 : cellar
			processCellarTemp(redPayload);
		} else if (redPayload[0] == 0x44) { // pipe 2 : seismo
			processSeismo(redPayload);
		}

	}

	private void processSeismo(byte[] redPayload) {
		if (redPayload[1] == 'P') {	
			if (redPayload[2] == '2') {
				byte HV = redPayload[3];
				byte LV = redPayload[4];
				ByteBuffer bb = ByteBuffer.allocate(2);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				bb.put(LV);
				bb.put(HV);
				short shortVal = bb.getShort(0);
				seismoChannel.newData(shortVal);
				seismoDifferentialChannel.newData(shortVal);

			}
		}
	}

	private void processCellarTemp(byte[] redPayload) {
		byte reject = (byte) 0xFF;
		if (redPayload[1] == 'T' && redPayload[2] == 'A') {	// Heating Exhaust
			if (redPayload[3] == '2') {
				byte TLV = redPayload[4];
				byte THV = redPayload[5];
				if (TLV != reject && THV != reject) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double temperature = shortVal * 0.0625;
					if (temperature < 80 && temperature > -40)
						heatingExhaustChannel.newData(temperature);
				}
			}
		}
		if (redPayload[6] == 'T' && redPayload[7] == 'B') {	// Heating Return
			if (redPayload[8] == '2') {
				byte TLV = redPayload[9];
				byte THV = redPayload[10];
				if (TLV != reject && THV != reject) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double temperature = shortVal * 0.0625;
					if (temperature < 80 && temperature > -40)
						heatingReturnChannel.newData(temperature);
				}
			}
		}
		if (redPayload[11] == 'T' && redPayload[12] == 'C') { //Heating Inflow
			if (redPayload[13] == '2') {
				byte TLV = redPayload[14];
				byte THV = redPayload[15];
				if (TLV != reject && THV != reject) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double temperature = shortVal * 0.0625;
					if (temperature < 80 && temperature > -40)
						heatingInflowChannel.newData(temperature);
				}

			}
		}
		if (redPayload[16] == 'T' && redPayload[17] == 'D') { // Ambiant temperature
			if (redPayload[18] == '2') {
				byte TLV = redPayload[19];
				byte THV = redPayload[20];
				if (TLV != reject && THV != reject) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double temperature = shortVal * 0.0625;
					if (temperature < 80 && temperature > -40)
						cellarTemperatureChannel.newData(temperature);
				}

			}
		}

	}

	private void processSeismoProbe(byte[] redPayload) {

		if (redPayload[1] == 'P') {	
			if (redPayload[2] == '2') {
				byte pHigh = redPayload[3];
				byte pLow = redPayload[4];
				int i;
				if (pLow < 0) i = 256 + pLow ;
				else i = pLow;
				i+= pHigh*256;

				//				testChannel.newData(i);
			}
		}
	}

	private void processSolarProbe(byte[] redPayload) {
		if (redPayload.length < 11) return;

		// Temperature info
		if (redPayload[1] == 'T') {	
			if (redPayload[2] == '2') {
				byte TLV = redPayload[3];
				byte THV = redPayload[4];

				ByteBuffer bb = ByteBuffer.allocate(2);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				bb.put(TLV);
				bb.put(THV);
				short shortVal = bb.getShort(0);

				double temperature = shortVal * 0.0625;
				if (temperature < 80 && temperature > -45)
					outTemperatureChannel.newData(temperature);

			}
		}
		// Light value
		if (redPayload[5] == 'L') {	
			if (redPayload[6] == '1') {
				int i;
				if (redPayload[7] < 0) i = 256 + redPayload[7] ;
				else i = redPayload[7];
				outLightChannel.newData(i);
			}
		}
		//Battery value
		if (redPayload[8] == 'B') {	
			if (redPayload[9] == '1') {
				int i;
				if (redPayload[10] < 0)	i = 256 + redPayload[10] ;
				else i = redPayload[10];
				outBatteryChannel.newData(i);
			}
		}
		// DHT values
		if (redPayload[11] == 'D') {	
			if (redPayload[12] == '5') {
				byte checksum = (byte) (redPayload[13] + redPayload[14] +redPayload[15] +redPayload[16]);
			    
			    if (checksum != redPayload[17]) {
			    	System.out.println(new Date().toString()+" DHT checksum fail");
			    	return;
			    }
			    
			    //Rel humidity
			    ByteBuffer bb = ByteBuffer.allocate(2);
				bb.order(ByteOrder.BIG_ENDIAN);
				bb.put(redPayload[13]);
				bb.put(redPayload[14]);
				short shortVal = bb.getShort(0);
				double relHumi = ((double)shortVal)/10;
				if (relHumi < 100 && relHumi > 0)
					humidChannel.newData(relHumi);
				
				// Temperature
				bb = ByteBuffer.allocate(2);
				bb.order(ByteOrder.BIG_ENDIAN);
				bb.put(redPayload[15]);
				bb.put(redPayload[16]);
				shortVal = bb.getShort(0);
				double temperature = ((double)shortVal)/10;
				if ( (redPayload[15] & 0x80) == 0x80)
					temperature = -temperature;
				if (temperature < 80 && temperature > -45)
					outTemperature2Channel.newData(temperature);
			    
			}
		}
	}

	@Override
	public JComponent[] getGuiComponents() {
		return new JComponent[] {getResetButton()};
	}

	private JButton resetButton;
	private JButton getResetButton() {
		if (resetButton == null) {
			resetButton = new JButton("Reset nRF24L01+");
			resetButton.setBorder(new LineBorder(Color.gray));
			resetButton.setBackground(Color.black);
			resetButton.setForeground(Color.white);
			resetButton.setFont(PiloggerGUI.labelFont);
			resetButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					initNRF24L01();
				}
			});
		}

		return resetButton;
	}
	private class Watchdog extends TimerTask {
		private long lastInterruptTime = 0;
		
		public Watchdog() {
			reset();
		}
		
		@Override
		public void run() {
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastInterruptTime > WATCHDOG_DELAY) {
				System.out.println(new Date().toString()+": Reseting nRF24L01+ module");
				WirelessProbe.this.initNRF24L01();
			}
		}
		
		public void reset() {
			lastInterruptTime = System.currentTimeMillis();
		}
		
	}
}
