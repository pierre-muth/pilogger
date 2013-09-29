package tests;
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  WiringPiSPIExample.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2013 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Spi;

public class SPITest {

	private static int rx_id = 0;

	public static void main(String args[]) throws InterruptedException {
		final byte packet[] = new byte[32];
		final GpioController gpio = GpioFactory.getInstance();
		final GpioPinDigitalOutput CE = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);
		final GpioPinDigitalInput IRQ = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);

		System.out.println("<--Pi4J--> SPI test program ");

		// setup SPI for communication
		int fd = Spi.wiringPiSPISetup(0, 10000000);
		if (fd <= -1) {
			System.out.println(" ==>> SPI SETUP FAILED");
			return;
		} else {
			System.out.println(" ==>> SPI SETUP SUCCES");
		}

		
		CE.low();		// disable radio
		Thread.sleep(100);
		
		// write config register : all IRQ, enable 2bytes CRC, Power UP, Primary RX
		packet[0] = 0b00100000;
		packet[1] = 0x0F;
		Spi.wiringPiSPIDataRW(0, packet, 2);
		System.out.println("[RX] " + bytesToHex(packet));

		// read Config Register
		packet[0] = 0b00000000;
		packet[1] = 0x00;
		Spi.wiringPiSPIDataRW(0, packet, 2);
		System.out.println("[RX] " + bytesToHex(packet));
		
		// write Feature reg, enable dynamic payload
		packet[0] = 0b00111101;
		packet[1] = 0b00000100;
		Spi.wiringPiSPIDataRW(0, packet, 2);
		System.out.println("[RX] " + bytesToHex(packet));

//		// Set RX payload lenght to 6 bytes
//		packet[0] = 0b00110001;
//		packet[1] = 0x06;
//		Spi.wiringPiSPIDataRW(0, packet, 2);
//		System.out.println("[RX] " + bytesToHex(packet));
		
		// write DYNPL, enable dynamic payload for pipe0 & 1
		packet[0] = 0b00111100;
		packet[1] = 0b00000011;
		Spi.wiringPiSPIDataRW(0, packet, 2);
		System.out.println("[RX] " + bytesToHex(packet));
		
		//Flush RX FIFO
		packet[0] = (byte) 0xE2;
		Spi.wiringPiSPIDataRW(0, packet, 1);
		System.out.println("[RX] " + bytesToHex(packet));
		
		// write Status Register : clear interupts
		packet[0] = 0b00100111;
		packet[1] = 0x70;
		Spi.wiringPiSPIDataRW(0, packet, 2);

		Thread.sleep(100);
		CE.high();
		
		IRQ.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				// display pin state on console
//				System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
				if (event.getState().isHigh()) return;
				
				// read R_RX_PL_WID Register
				packet[0] = 0b01100000;
				packet[1] = 0x00;
				Spi.wiringPiSPIDataRW(0, packet, 2);
				System.out.println("[RX] " + bytesToHex(packet));
				
				int payloadLenght = packet[1];

				//read RX 
				packet[0] = 0b01100001;
				Spi.wiringPiSPIDataRW(0, packet, payloadLenght+1);
				System.out.println("[RX] " + bytesToHex(packet));
				System.out.println(rx_id++ +" " + new String(packet));

				//clear status
				packet[0] = 0b00100111;
				packet[1] = 0x70;
				Spi.wiringPiSPIDataRW(0, packet, 2);
			}

		});
		
		while(true) {
			Thread.sleep(1000);
//			Spi.wiringPiSPIDataRW(0, packet, 1);
//			System.out.println("[RX] " + bytesToHex(packet));
			
//			if ((packet[0] & 0x0F) == 0) {
//				packet[0] = 0b01100001;
//				Spi.wiringPiSPIDataRW(0, packet, 7);
//				System.out.println("[RX] " + bytesToHex(packet));
//				System.out.println(rx_id++ +" " + new String(packet));
//				
//				//clear status
//				packet[0] = 0b00100111;
//				packet[1] = 0x70;
//				Spi.wiringPiSPIDataRW(0, packet, 2);
//				
//				Spi.wiringPiSPIDataRW(0, packet, 1);
//				System.out.println("[RX] " + bytesToHex(packet));
//			}
		}
	}


	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}    
}
