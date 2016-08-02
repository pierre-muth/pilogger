/* Copyright (c) 2006 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is confidential property of Nordic Semiconductor. The use,
 * copying, transfer or disclosure of such information is prohibited except by express written
 * agreement with Nordic Semiconductor.
 *
 * $Rev: 1731 $
 *
 */
package hardware;

import pilogger.Utils;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.wiringpi.Spi;

public class NRF24L01 {
	
	public NRF24L01() {
	}

	/* nRF24L01 Instruction Definitions */
	public static final byte WRITE_REG = 	0x20;  			/* Register write command */
	public static final byte RD_RX_PLOAD_W =0x60;  		/* Read RX payload command */
	public static final byte RD_RX_PLOAD =  0x61;  			/* Read RX payload command */
	public static final byte WR_TX_PLOAD =  (byte) 0xA0;  	/* Write TX payload command */
	public static final byte WR_ACK_PLOAD = (byte) 0xA8;  	/* Write ACK payload command */
	public static final byte FLUSH_TX =     (byte) 0xE1;  	/* Flush TX register command */
	public static final byte FLUSH_RX =     (byte) 0xE2;  	/* Flush RX register command */
	public static final byte REUSE_TX_PL =  (byte) 0xE3;  	/* Reuse TX payload command */
	public static final byte LOCK_UNLOCK =	0x50;  			/* Lock/unlcok exclusive features */

	public static final byte NRF_NOP = (byte) 0xFF;  /* No Operation command, used for reading status register */

	/* name  - Register Memory Map - */
	/* nRF24L01 * Register Definitions */
	public static final byte CONFIG =       0x00;  /* nRF24L01 config register */
	public static final byte EN_AA =        0x01;  /* nRF24L01 enable Auto-Acknowledge register */
	public static final byte EN_RXADDR =    0x02;  /* nRF24L01 enable RX addresses register */
	public static final byte SETUP_AW =     0x03;  /* nRF24L01 setup of address width register */
	public static final byte SETUP_RETR =   0x04;  /* nRF24L01 setup of automatic retransmission register */
	public static final byte RF_CH =        0x05;  /* nRF24L01 RF channel register */
	public static final byte RF_SETUP =     0x06;  /* nRF24L01 RF setup register */
	public static final byte STATUS =       0x07;  /* nRF24L01 status register */
	public static final byte OBSERVE_TX =   0x08;  /* nRF24L01 transmit observe register */
	public static final byte CD =           0x09;  /* nRF24L01 carrier detect register */
	public static final byte RX_ADDR_P0 =   0x0A;  /* nRF24L01 receive address data pipe0 */
	public static final byte RX_ADDR_P1 =   0x0B;  /* nRF24L01 receive address data pipe1 */
	public static final byte RX_ADDR_P2 =   0x0C;  /* nRF24L01 receive address data pipe2 */
	public static final byte RX_ADDR_P3 =   0x0D;  /* nRF24L01 receive address data pipe3 */
	public static final byte RX_ADDR_P4 =   0x0E;  /* nRF24L01 receive address data pipe4 */
	public static final byte RX_ADDR_P5 =   0x0F;  /* nRF24L01 receive address data pipe5 */
	public static final byte TX_ADDR =      0x10;  /* nRF24L01 transmit address */
	public static final byte RX_PW_P0 =     0x11;  /* nRF24L01 \# of bytes in rx payload for pipe0 */
	public static final byte RX_PW_P1 =     0x12;  /* nRF24L01 \# of bytes in rx payload for pipe1 */
	public static final byte RX_PW_P2 =     0x13;  /* nRF24L01 \# of bytes in rx payload for pipe2 */
	public static final byte RX_PW_P3 =     0x14;  /* nRF24L01 \# of bytes in rx payload for pipe3 */
	public static final byte RX_PW_P4 =     0x15;  /* nRF24L01 \# of bytes in rx payload for pipe4 */
	public static final byte RX_PW_P5 =     0x16;  /* nRF24L01 \# of bytes in rx payload for pipe5 */
	public static final byte FIFO_STATUS =  0x17;  /* nRF24L01 FIFO status register */
	public static final byte DYNPD =        0x1C;  /* nRF24L01 Dynamic payload setup */
	public static final byte FEATURE =      0x1D;  /* nRF24L01 Exclusive feature setup */


	/* Defines the channel the radio should operate on*/
	public static final byte RF_CHANNEL = 0;

	/* Defines the time it takes for the radio to come up to operational mode */
	public static final int RF_POWER_UP_DELAY = 2;

	/* Defines the payload length the radio should use */
	public static final int RF_PAYLOAD_LENGTH = 32  ;                         

	/* Defines how many retransmitts that should be performed */
	public static final int RF_RETRANSMITS = 7;

	public static final int RF_RETRANS_DELAY = 500;

	public static final byte BIT_0 = 0x01; 
	public static final byte BIT_1 = 0x02; 
	public static final byte BIT_2 = 0x04; 
	public static final byte BIT_3 = 0x08; 
	public static final byte BIT_4 = 0x10; 
	public static final byte BIT_5 = 0x20;
	public static final byte BIT_6 = 0x40; 
	public static final byte BIT_7 = (byte) 0x80; 

	public static final byte MASK_RX_DR =   6;     
	public static final byte MASK_TX_DS =   5;     
	public static final byte MASK_MAX_RT =  4;     
	public static final byte EN_CRC =       3;     
	public static final byte CRCO =         2;     
	public static final byte PWR_UP =       1;     
	public static final byte PRIM_RX =      0;    

	/* @name RF_SETUP register bit definitions */
	public static final byte PLL_LOCK =     4;     
	public static final byte RF_DR =        3;     
	public static final byte RF_PWR1 =      2;    
	public static final byte RF_PWR0 =      1;     
	public static final byte LNA_HCURR =    0;    

	/* STATUS 0x07 */
	/* name STATUS register bit definitions */
	public static final byte RX_DR =        6;     
	public static final byte TX_DS =        5;     
	public static final byte MAX_RT =       4;     
	public static final byte TX_FULL =      0;     
	
	/* FIFO_STATUS 0x17 */
	/* name FIFO_STATUS register bit definitions */
	public static final byte TX_REUSE =     6;     
	public static final byte TX_FIFO_FULL = 5;    
	public static final byte TX_EMPTY =     4;    
	public static final byte RX_FULL =      1;     
	public static final byte RX_EMPTY =     0;     


	public static byte SET_BIT(int position){
		return (byte) (1<<position);
	}
	
	public void hal_nrf_close_pipes()	{
		hal_nrf_write_reg(EN_RXADDR, (byte) 0);
		hal_nrf_write_reg(EN_AA, (byte) 0);
	}

	public void hal_nrf_open_pipes() {
		hal_nrf_write_reg(EN_RXADDR, (byte) ~(BIT_7|BIT_6));
		hal_nrf_write_reg(EN_AA, (byte) ~(BIT_7|BIT_6));
	}

	public void hal_nrf_set_crc_16bits() {
		hal_nrf_write_reg(CONFIG, (byte) (hal_nrf_read_reg(CONFIG) & (BIT_3|BIT_2)));
	}

	public void hal_nrf_set_auto_retr(int retr, int delay) {
	    hal_nrf_write_reg(SETUP_RETR, (byte) ((((delay/250)-1)<<4) | retr));
	}

	public void hal_nrf_set_address_width_5() {
	    hal_nrf_write_reg(SETUP_AW, (byte) 0x03);
	}

	public void hal_nrf_enable_ack_pl() {
	    hal_nrf_write_reg(FEATURE, (byte) (hal_nrf_read_reg(FEATURE) | 0x02));   
	}

	public void hal_nrf_lock_unlock() {
//		CSN.low();
//	    hal_nrf_rw(LOCK_UNLOCK);             
//	    hal_nrf_rw((byte) 0x73);
//	    CSN.high();
	}

	public void hal_nrf_enable_dynamic_pl() {
	    hal_nrf_write_reg(FEATURE, (byte) (hal_nrf_read_reg(FEATURE) | 0x04));   
	}

	public void hal_nrf_setup_dyn_pl() {
	    hal_nrf_write_reg(DYNPD, (byte) 0b00111111); 
	}

	public void hal_nrf_set_operation_mode_RX() {
		hal_nrf_write_reg(CONFIG, (byte) (hal_nrf_read_reg(CONFIG) | (1<<PRIM_RX)));
	}
	
	public void hal_nrf_set_operation_mode_RX_CRC_UP() {
		hal_nrf_write_reg(CONFIG, (byte) 0x0F);
	}
	
	public void hal_nrf_set_operation_mode_TX() {
		hal_nrf_write_reg(CONFIG, (byte) (hal_nrf_read_reg(CONFIG) & ~(1<<PRIM_RX)));
	}

	public void hal_nrf_set_rf_channel(byte channel) {
	    hal_nrf_write_reg(RF_CH, channel);
	}
	
	public void hal_nrf_set_rf_250kbp() {
	    hal_nrf_write_reg(RF_SETUP, (byte) 0b00100110);
	}

	public void hal_nrf_set_power_UP (){
		hal_nrf_write_reg(CONFIG, (byte) (hal_nrf_read_reg(CONFIG) | (1<<PWR_UP)));
	}
	
	public void hal_nrf_set_power_DOWN (){
		hal_nrf_write_reg(CONFIG, (byte) (hal_nrf_read_reg(CONFIG) & ~(1<<PWR_UP)));
	}

	public byte hal_nrf_get_rx_data_source() {
	    return (byte) ((hal_nrf_nop() & (BIT_3|BIT_2|BIT_1)) >> 1);
	}

	public byte hal_nrf_nop() {
	    return hal_nrf_write_reg(NRF_NOP,(byte) 0);
	}

	public byte hal_nrf_read_rx_pl_w() {
	    return hal_nrf_read_reg(RD_RX_PLOAD_W);
	}

	public byte[] hal_nrf_read_RX_PLOAD() {
		byte length;
		length = hal_nrf_read_rx_pl_w();
		
		byte read[] = new byte[length+1];
		read[0] = RD_RX_PLOAD;
		Spi.wiringPiSPIDataRW(0, read, read.length);
		
		byte[] array = new byte[length];
		for (int i = 0; i < array.length; i++) {
			array[i] = read[i+1];
		}
		
	    return array;
	}

	public void hal_nrf_write_ACK_PAYLOAD(byte[] payload, byte pipe) {
		byte[] array = new byte[payload.length+1];
		array[0] = (byte) (WR_ACK_PLOAD + pipe);
		for (int i = 1; i < array.length; i++) {
			array[i] = payload[i-1];
		}
		Spi.wiringPiSPIDataRW(0, array, array.length);
	}
	public byte hal_nrf_read_reg(byte reg) {
		byte[] array = {reg, 0x00};
		Spi.wiringPiSPIDataRW(0, array, 2);
		System.out.println("SPI Register "+reg+" get> " + Utils.byteToHex(array[1]));
		return array[1];
	}
	public byte hal_nrf_write_reg(byte reg, byte value) {
		byte[] array = {(byte) (WRITE_REG + reg), value};
		System.out.print("SPI Register "+Utils.byteToHex(array[0])+" set> " + Utils.byteToHex(array[1]));
		Spi.wiringPiSPIDataRW(0, array, 2);
		System.out.println(" get> " + Utils.bytesToHex(array));
		return array[0];
	}
	public byte hal_nrf_write_command(byte com) {
		System.out.print("SPI Command set> " + Utils.byteToHex(com));
		byte[] array = {com};
		Spi.wiringPiSPIDataRW(0, array, 1);
		System.out.println(" get> " + Utils.bytesToHex(array));
		return array[0];
	}
	
	public byte hal_nrf_get_clear_irq_flags()	{
		byte irq = (byte) (hal_nrf_write_reg(STATUS, (byte) (BIT_6|BIT_5|BIT_4)) & (BIT_6|BIT_5|BIT_4));
		System.out.println("interrupt> "+Utils.byteToHex(irq));
	    return irq;
	}

	public void hal_nrf_flush_tx() {
		hal_nrf_write_command(FLUSH_TX);
	}
	
	public void hal_nrf_flush_rx() {
		hal_nrf_write_command(FLUSH_RX);
	}

	public void initNRF24L01() throws InterruptedException {
//		hal_nrf_close_pipes();               		// First close all radio pipes Pipe 0 and 1 open by default
//		hal_nrf_set_operation_mode_RX();     		// Enter RX mode
//		hal_nrf_open_pipes();        				// Then open pipe0, w/autoack 
//		hal_nrf_set_rf_250kbp();					// 250Kbps data rate
//		hal_nrf_set_rf_channel(RF_CHANNEL);         // Frequenzy = 2400GHz + RF_CHANNEL    
//		hal_nrf_set_crc_16bits();    				// Operates in 16bits CRC mode
//		hal_nrf_set_auto_retr(RF_RETRANSMITS, RF_RETRANS_DELAY); // Enables auto retransmit. 
//		hal_nrf_set_address_width_5();  			// 5 bytes address width
////		hal_nrf_enable_ack_pl();                    // Try to enable ack payload
////		if(hal_nrf_read_reg(FEATURE) == 0x00 && (hal_nrf_read_reg(DYNPD) == 0x00)) { // When the features are locked, the FEATURE and DYNPD are read out 0x00
////			hal_nrf_lock_unlock ();                 // Activate features
////			hal_nrf_enable_ack_pl();                // Enables payload in ack
////		}
//		hal_nrf_enable_dynamic_pl();                // Enables dynamic payload
//		hal_nrf_setup_dyn_pl();               		// Sets up dynamic payload on all data pipes.
//		hal_nrf_flush_rx();
//		hal_nrf_get_clear_irq_flags();
//		hal_nrf_set_power_UP();					    // Power up device
		
		hal_nrf_write_reg(CONFIG,		(byte) 0b00001111);
		Thread.sleep(10);
		hal_nrf_write_reg(FEATURE,		(byte) 0b00000110);
		hal_nrf_write_reg(RF_SETUP,		(byte) 0b00100110);
		hal_nrf_write_reg(SETUP_RETR,	(byte) 0b00110111);
		hal_nrf_write_reg(EN_RXADDR,	(byte) 0b00111111);
		hal_nrf_write_reg(DYNPD,		(byte) 0b00111111);
		hal_nrf_write_command(FLUSH_RX);
		hal_nrf_write_reg(STATUS,		(byte) 0b01110000);
		Thread.sleep(10);
	}
	

}

