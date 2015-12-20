package probes;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;

import pilogger.DataChannel;
import pilogger.PiloggerGUI;
import pilogger.Utils;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Spi;

public class WirelessProbe extends AbstractProbe implements GpioPinListenerDigital{
	private static final double MIN_TEMPERATURE = -39, MAX_TEMPERATURE = 65;
	private static final double MIN_WATT = 2, MAX_WATT = 6000;
	private static final double MIN_BATTERY = 1, MAX_BATTERY = 4;
	private static final double MIN_LIGHT = 1, MAX_LIGHT = 1023;

	public DataChannel outTemperatureChannel = new DataChannel("Outside Temperature", "Outside_Temperature");
	public DataChannel outLightChannel = new DataChannel("Outside Brightness", "Outside_Light");
	public DataChannel outBatteryChannel = new DataChannel("Outside Battery", "Outside_Battery");
	public DataChannel heatingExhaustChannel = new DataChannel("Heating Exhaust", "Heating_Exhaust");
	public DataChannel hotWaterOutChannel = new DataChannel("Hot water out", "Hot_Water_Out");
	public DataChannel coldWaterInChannel = new DataChannel("Cold water in", "Cold_Water_In");
	public DataChannel heatingReturnChannel = new DataChannel("Heating Return", "Heating_Return");
	public DataChannel cellarTemperatureChannel = new DataChannel("Cellar Temperature", "Cellar_temperature");
	public DataChannel powerConsumptionChannel = new DataChannel("inst. power consumption", "inst_power_cons");
	public DataChannel upstairsTemperatureChannel = new DataChannel("Upstairs Temperature", "Upstairs_Temperature");
	public DataChannel upstairsBatteryChannel = new DataChannel("Upstairs Battery", "Upstairs_Battery");
	
	private DataChannel[] channels = new DataChannel[] {
			outTemperatureChannel, 
			outLightChannel, 
			outBatteryChannel, 
			heatingExhaustChannel, 
			hotWaterOutChannel, 
			coldWaterInChannel,
			heatingReturnChannel, 
			cellarTemperatureChannel,
			powerConsumptionChannel,
			upstairsTemperatureChannel,
			upstairsBatteryChannel
			};

	private GpioController gpio;
	private GpioPinDigitalOutput CE;
	private GpioPinDigitalInput IRQ;
	private Watchdog watchdog;
	private static final int WATCHDOG_DELAY = 20000;
	
	public WirelessProbe(GpioController gpio) {
		initDataChannels();
		
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

	private void initDataChannels() {
		outTemperatureChannel.setDataRange(-20.0, 50.0);
		outLightChannel.setDataRange(MIN_LIGHT, MAX_LIGHT);
		outBatteryChannel.setDataRange(MIN_BATTERY, MAX_BATTERY);
		heatingExhaustChannel.setDataRange(6.0, MAX_TEMPERATURE);
		hotWaterOutChannel.setDataRange(6.0, MAX_TEMPERATURE);
		coldWaterInChannel.setDataRange(MIN_TEMPERATURE, 45.0);
		heatingReturnChannel.setDataRange(6.0, MAX_TEMPERATURE);
		cellarTemperatureChannel.setDataRange(MIN_TEMPERATURE, 45.0);
		powerConsumptionChannel.setDataRange(MIN_WATT, MAX_WATT);
		upstairsTemperatureChannel.setDataRange(-20.0, 50.0);
		upstairsBatteryChannel.setDataRange(MIN_BATTERY, MAX_BATTERY);
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

		// write EN_RXADDR : enable RX address pipe 5:0 
		packet[0] = 0b00100010;
		packet[1] = 0b00111111;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		// write DYNPL, enable dynamic payload for pipe 5:0
		packet[0] = 0b00111100;
		packet[1] = 0b00111111;
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
		if (getDebugOutCheckBox().isSelected()) 
			outputForDebug(redPayload);
		
		if (redPayload[0] == 0x40) { 		// pipe 0 : solar probe
//			processSolarProbe(redPayload);
		} else if (redPayload[0] == 0x42) { // pipe 1 : cellar
			processCellarTemp(redPayload);
		} else if (redPayload[0] == 0x44) { // pipe 2 : Instant power
			processPower(redPayload);
		} else if (redPayload[0] == 0x46) { // pipe 3 : Outside low power temperature probe
			processOutProbe(redPayload);
		} else if (redPayload[0] == 0x48) { // pipe 4 : low power temperature probe upstair
			processUpstairsProbe(redPayload);
		}

	}
	
	private void outputForDebug(byte[] redPayload) {
		StringBuffer sb = new StringBuffer();
		sb.append(Utils.byteToHex(redPayload[0]));
		sb.append(">");
		int dataLenght = 0;
		
		sb.append(" ");

		for (int i = 1; i < redPayload.length; i++) {
			if (dataLenght > 0) {
				sb.append(Utils.byteToHex(redPayload[i]));
				dataLenght--;
				if (dataLenght == 0)
					sb.append(" ");
			} else {
				if (Character.isDigit((char)redPayload[i])) {
					sb.append(": ");
					dataLenght = Integer.parseInt( ""+((char)redPayload[i]) );
				} else {
					sb.append((char)redPayload[i]);
				}
			}
		}
		
		System.out.println( sb.toString() );
	}
	
	private void processUpstairsProbe(byte[] redPayload) {
		byte reject = (byte) 0xFF;
		if (redPayload[1] == 'T') {
			if (redPayload[2] == '2') {
				byte TLV = redPayload[3];
				byte THV = redPayload[4];
				if (!(TLV == reject && THV == reject)) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double temperature = shortVal / 256.0;
					if (temperature < MAX_TEMPERATURE && temperature > MIN_TEMPERATURE)
						upstairsTemperatureChannel.newData(temperature);
				}
			}
		}
		if (redPayload[5] == 'B') {
			if (redPayload[6] == '2') {
				byte THV = redPayload[7];
				byte TLV = redPayload[8];
				if (!(TLV == reject && THV == reject)) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double voltage = (1024.0/shortVal)*1.024;
					if (voltage < 5.1 && voltage > 0.9)
						upstairsBatteryChannel.newData(voltage);
				}
			}
		}
	}
	
	private void processOutProbe(byte[] redPayload) {
		byte reject = (byte) 0xFF;
		if (redPayload[1] == 'T') {
			if (redPayload[2] == '2') {
				byte TLV = redPayload[3];
				byte THV = redPayload[4];
				if (!(TLV == reject && THV == reject)) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double temperature = shortVal / 256.0;
					if (temperature < MAX_TEMPERATURE && temperature > MIN_TEMPERATURE)
						outTemperatureChannel.newData(temperature);
				}
			}
		}
		if (redPayload[5] == 'B') {
			if (redPayload[6] == '2') {
				byte THV = redPayload[7];
				byte TLV = redPayload[8];
				if (!(TLV == reject && THV == reject)) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double voltage = (1024.0/shortVal)*1.024;
					if (voltage < 5.1 && voltage > 0.9)
						outBatteryChannel.newData(voltage);
				}
			}
		}
		if (redPayload[9] == 'L') {
			if (redPayload[10] == '2') {
				byte THV = redPayload[11];
				byte TLV = redPayload[12];
				if (!(TLV == reject && THV == reject)) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double voltage = shortVal;
					if (voltage < 1024 && voltage >= 0)
						outLightChannel.newData(voltage);
				}
			}
		}
	}

	private void processPower(byte[] redPayload) {
		if (redPayload[1] == 'W') {
			if (redPayload[2] == '2') {
				byte HV = redPayload[3];
				byte LV = redPayload[4];
				ByteBuffer bb = ByteBuffer.allocate(2);
				bb.order(ByteOrder.BIG_ENDIAN);
				bb.put(LV);
				bb.put(HV);
				short watt = bb.getShort(0);
				
				if (watt > MIN_WATT && watt < MAX_WATT) {
					powerConsumptionChannel.newData(watt);
				}
			}
		}
		
		if (redPayload[5] == 'R') {
			if (redPayload[6] == '4') {
				byte r0 = redPayload[7];
				byte r1 = redPayload[8];
				byte r2 = redPayload[9];
				byte r3 = redPayload[10];
				ByteBuffer bb = ByteBuffer.allocate(4);
				bb.order(ByteOrder.BIG_ENDIAN);
				bb.put(r3);
				bb.put(r2);
				bb.put(r1);
				bb.put(r0);
				int time = bb.getInt(0);
				
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
					if (temperature < MAX_TEMPERATURE && temperature > MIN_TEMPERATURE)
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
					if (temperature < MAX_TEMPERATURE && temperature > MIN_TEMPERATURE)
						heatingReturnChannel.newData(temperature);
				}
			}
		}
		if (redPayload[11] == 'T' && redPayload[12] == 'C') { //Hot water pipe
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
					if (temperature < MAX_TEMPERATURE && temperature > MIN_TEMPERATURE)
						hotWaterOutChannel.newData(temperature);
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
					if (temperature < MAX_TEMPERATURE && temperature > MIN_TEMPERATURE)
						cellarTemperatureChannel.newData(temperature);
				}

			}
		}
		if (redPayload[21] == 'T' && redPayload[22] == 'E') { // Cold Water pipe
			if (redPayload[23] == '2') {
				byte TLV = redPayload[24];
				byte THV = redPayload[25];
				if (TLV != reject && THV != reject) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(TLV);
					bb.put(THV);
					short shortVal = bb.getShort(0);

					double temperature = shortVal * 0.0625;
					if (temperature < MAX_TEMPERATURE && temperature > MIN_TEMPERATURE)
						coldWaterInChannel.newData(temperature);
				}

			}
		}

	}

//	private void processSeismoProbe(byte[] redPayload) {
//
//		if (redPayload[1] == 'P') {	
//			if (redPayload[2] == '2') {
//				byte pHigh = redPayload[3];
//				byte pLow = redPayload[4];
//				int i;
//				if (pLow < 0) i = 256 + pLow ;
//				else i = pLow;
//				i+= pHigh*256;
//
//				testChannel.newData(i);
//			}
//		}
//	}

//	private void processSolarProbe(byte[] redPayload) {
//		if (redPayload.length < 11) return;
//
//		// Temperature info
//		if (redPayload[1] == 'T') {	
//			if (redPayload[2] == '2') {
//				byte TLV = redPayload[3];
//				byte THV = redPayload[4];
//
//				ByteBuffer bb = ByteBuffer.allocate(2);
//				bb.order(ByteOrder.LITTLE_ENDIAN);
//				bb.put(TLV);
//				bb.put(THV);
//				short shortVal = bb.getShort(0);
//
//				double temperature = shortVal * 0.0625;
//				if (temperature < 80 && temperature > -45)
//					outTemperatureChannel.newData(temperature);
//
//			}
//		}
//		// Light value
//		if (redPayload[5] == 'L') {	
//			if (redPayload[6] == '1') {
//				int i;
//				if (redPayload[7] < 0) i = 256 + redPayload[7] ;
//				else i = redPayload[7];
//				outLightChannel.newData(i);
//			}
//		}
//		//Battery value
//		if (redPayload[8] == 'B') {	
//			if (redPayload[9] == '1') {
//				int i;
//				if (redPayload[10] < 0)	i = 256 + redPayload[10] ;
//				else i = redPayload[10];
//				double v = i;
//				v = (v/256)*5;	// in Volt
//				outBatteryChannel.newData(v);
//			}
//		}
//		// DHT values
//		if (redPayload[11] == 'D') {	
//			if (redPayload[12] == '5') {
//				byte checksum = (byte) (redPayload[13] + redPayload[14] +redPayload[15] +redPayload[16]);
//			    
//			    if (checksum != redPayload[17]) {
//			    	System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+": DHT checksum fail");
//			    	return;
//			    }
//			    
//			    //Rel humidity
//			    ByteBuffer bb = ByteBuffer.allocate(2);
//				bb.order(ByteOrder.BIG_ENDIAN);
//				bb.put(redPayload[13]);
//				bb.put(redPayload[14]);
//				short shortVal = bb.getShort(0);
//				double relHumi = ((double)shortVal)/10;
//				if (relHumi < 100 && relHumi > 0)
//					humidChannel.newData(relHumi);
//				
//				// Temperature
//				bb = ByteBuffer.allocate(2);
//				bb.order(ByteOrder.BIG_ENDIAN);
//				bb.put(redPayload[15]);
//				bb.put(redPayload[16]);
//				shortVal = bb.getShort(0);
//				double temperature = ((double)shortVal)/10;
//				if ( (redPayload[15] & 0x80) == 0x80)
//					temperature = -temperature;
//				if (temperature < 80 && temperature > -45)
//					outTemperature2Channel.newData(temperature);
//			    
//			}
//		}
//	}

	@Override
	public JComponent[] getGuiComponents() {
		return new JComponent[] {getResetButton(), getDebugOutCheckBox()};
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
	
	private JCheckBox debugOutCheckBox;
	private JCheckBox getDebugOutCheckBox() {
		if (debugOutCheckBox == null) {
			debugOutCheckBox = new JCheckBox("Debug Output", false);
			debugOutCheckBox.setBorder(new LineBorder(Color.gray));
			debugOutCheckBox.setBackground(Color.black);
			debugOutCheckBox.setForeground(Color.white);
			debugOutCheckBox.setFont(PiloggerGUI.labelFont);
		}
		return debugOutCheckBox;
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
				System.out.println(new SimpleDateFormat(PiloggerGUI.DATE_PATERN).format(new Date())+": Reseting nRF24L01+ module");
				WirelessProbe.this.initNRF24L01();
			}
		}
		
		public void reset() {
			lastInterruptTime = System.currentTimeMillis();
		}
		
	}
}
