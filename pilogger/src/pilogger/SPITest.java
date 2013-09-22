package pilogger;
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
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Spi;

public class SPITest {

	// SPI operations
	public static byte WRITE_CMD = 0x40;
	public static byte READ_CMD  = 0x41;

	@SuppressWarnings("unused")
	public static void main(String args[]) throws InterruptedException {

		final GpioController gpio = GpioFactory.getInstance();
		final GpioPinDigitalOutput CE = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);

		// 
		// This SPI example is using the WiringPi native library 
		//
		// Please note the following command are required to enable the SPI driver on
		// your Raspberry Pi:
		// >  sudo modprobe spi_bcm2708
		// >  sudo chown `id -u`.`id -g` /dev/spidev0.*
		//
		// see this blog post for additional details on SPI and WiringPi
		// https://projects.drogon.net/understanding-spi-on-the-raspberry-pi/

		System.out.println("<--Pi4J--> SPI test program ");

		// setup SPI for communication
		int fd = Spi.wiringPiSPISetup(0, 10000000);;
		if (fd <= -1) {
			System.out.println(" ==>> SPI SETUP FAILED");
			return;
		} else {
			System.out.println(" ==>> SPI SETUP SUCCES");
		}

		byte packet[] = new byte[7];
		
		CE.low();

		packet[0] = 0b00100000;
		packet[1] = 0x0F;
		Spi.wiringPiSPIDataRW(0, packet, 2);
		System.out.println("[RX] " + bytesToHex(packet));

		packet[0] = 0b00000000;
		packet[1] = 0x00;
		Spi.wiringPiSPIDataRW(0, packet, 2);
		System.out.println("[RX] " + bytesToHex(packet));

		packet[0] = 0b00110001;
		packet[1] = 0x07;
		Spi.wiringPiSPIDataRW(0, packet, 2);
		System.out.println("[RX] " + bytesToHex(packet));

		Thread.sleep(1000);
		CE.high();
		while(true) {
			Thread.sleep(600);
			packet[0] = 0b01100001;
			Spi.wiringPiSPIDataRW(0, packet, 7);
			System.out.println("[RX] " + bytesToHex(packet));
		}
	}

	public static void write(byte register, int data){

		// send test ASCII message
		byte packet[] = new byte[3];
		packet[0] = WRITE_CMD;  // address byte
		packet[1] = register;  // register byte
		packet[2] = (byte)data;  // data byte

		System.out.println("-----------------------------------------------");
		System.out.println("[TX] " + bytesToHex(packet));
		Spi.wiringPiSPIDataRW(0, packet, 3);        
		System.out.println("[RX] " + bytesToHex(packet));
		System.out.println("-----------------------------------------------");
	}

	public static void read(byte register){

		// send test ASCII message
		byte packet[] = new byte[3];
		packet[0] = READ_CMD;    // address byte
		packet[1] = register;    // register byte
		packet[2] = 0b00000000;  // data byte

		System.out.println("-----------------------------------------------");
		System.out.println("[TX] " + bytesToHex(packet));
		Spi.wiringPiSPIDataRW(0, packet, 3);        
		System.out.println("[RX] " + bytesToHex(packet));
		System.out.println("-----------------------------------------------");
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
