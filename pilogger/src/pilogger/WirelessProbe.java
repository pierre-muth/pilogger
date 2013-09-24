package pilogger;

import java.util.Timer;
import java.util.TimerTask;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Spi;

import datachannel.AbstractProbe;
import datachannel.DataChannel;

public class WirelessProbe extends AbstractProbe implements GpioPinListenerDigital{

	public DataChannel outTemperatureChannel = new DataChannel("Outside Temperature", "Outside_Temperature");
	public DataChannel outLightChannel = new DataChannel("Outside Light", "Outside_Light");
	public DataChannel outBatteryChannel = new DataChannel("Outside Battery", "Outside_Battery");
	private DataChannel[] channels = new DataChannel[] {outTemperatureChannel, outLightChannel, outBatteryChannel};

	private GpioController gpio;
	private GpioPinDigitalOutput CE;
	private GpioPinDigitalInput IRQ;

	public WirelessProbe(GpioController gpio) {
		this.gpio = gpio;
		initGPIO();
		initNRF24L01();
		IRQ.addListener(this);

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

		// write DYNPL, enable dynamic payload for pipe0 & 1
		packet[0] = 0b00111100;
		packet[1] = 0b00000011;
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

		byte packet[] = new byte[2];

		// read R_RX_PL_WID Register
		packet[0] = 0b01100000;
		packet[1] = 0x00;
		Spi.wiringPiSPIDataRW(0, packet, 2);

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

	private void processPayload (byte[] redPayload) {
		System.out.println( Utils.bytesToHex(redPayload) );
		if (redPayload.length < 10) return;

		if (redPayload[1] == 'T') {	// Temperature info
			if (redPayload[2] == '2') {
				byte TLV = redPayload[3];
				byte THV = redPayload[4];

				THV = (byte) (THV & 0b10000111);
				int temperature = THV*255;
				int i;
				if (TLV < 0)
					i = 255 + TLV ;
				else
					i = TLV;
				
				temperature += i;
				outTemperatureChannel.newData(temperature * 0.0625);

			}
		}

		if (redPayload[5] == 'L') {	// Light value
			if (redPayload[6] == '1') {
				outLightChannel.newData(redPayload[7]);
			}
		}
		
		if (redPayload[8] == 'B') {	//Battery value
			if (redPayload[9] == '1') {
				outBatteryChannel.newData(redPayload[10]);
			}
		}

		//		System.out.println( Utils.bytesToHex(readPayload) );
	}
}
