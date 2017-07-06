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

import pilogger.Utils;
import hardware.NRF24L01;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Spi;

public class SPITest implements GpioPinListenerDigital, Runnable{

	private static int rx_id = 0;
	private NRF24L01 nRF; 
	
	public SPITest() throws InterruptedException {
		byte packet[] = new byte[32];
		
		
		System.out.println("nRF24l01 SPI test program ");

		// setup SPI for communication
		int fd = Spi.wiringPiSPISetup(0, 10000000);
		if (fd <= -1) {
			System.out.println(" ==>> SPI SETUP FAILED");
			return;
		} else {
			System.out.println(" ==>> SPI SETUP SUCCES");
		}

		GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalOutput CE = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);
//		final GpioPinDigitalOutput CSN = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10);
		GpioPinDigitalInput IRQ = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);
		
		CE.low();
//		CSN.high();
		
		// init nRF
		nRF = new NRF24L01();
		nRF.initNRF24L01();		
		
		
		Thread.sleep(100);
		CE.high(); // enable reception
		
//		System.out.println("irq: "+nRF.hal_nrf_read_rx_pl_w());
		
		// nRF listening
		IRQ.addListener(this);
		
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(10000);
				nRF.hal_nrf_get_clear_irq_flags();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}  
	
	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		byte packet[] = new byte[32];
		if (event.getState().isHigh()) return;
		
		packet = nRF.hal_nrf_read_RX_PLOAD();
		nRF.hal_nrf_flush_rx();
		nRF.hal_nrf_flush_tx();
		nRF.hal_nrf_get_clear_irq_flags();
		
		System.out.println(rx_id++ +">"+Utils.bytesToHex(packet));
		clearArray(packet);
		
		byte[] payload = {(byte) 0xDE, (byte) 0xBE, (byte) 0xFF};
		nRF.hal_nrf_write_ACK_PAYLOAD(payload, (byte) 0x00);
		
//		// read R_RX_PL_WID Register
//		packet[0] = 0b01100000;
//		packet[1] = 0x00;
//		Spi.wiringPiSPIDataRW(0, packet, 2);
//		System.out.println("[RX] " + bytesToHex(packet));
//		
//		//read RX 
//		int payloadLenght = packet[1];
//		packet[0] = 0b01100001;
//		Spi.wiringPiSPIDataRW(0, packet, payloadLenght+1);
//		System.out.println("[RX] " + bytesToHex(packet));
//
//		//clear status
//		packet[0] = 0b00100111;
//		packet[1] = 0x70;
//		Spi.wiringPiSPIDataRW(0, packet, 2);
	}

	public static void main(String args[]) throws InterruptedException {
		new Thread( new SPITest() ).start();
	}

	private static void clearArray(byte[] bytes) {
		for ( int j = 0; j < bytes.length; j++ ) {
			bytes[j] = 0;
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
