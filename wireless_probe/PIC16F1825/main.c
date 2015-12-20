/* 
 * File:   main.c
 * Author: pfreyerm
 *
 * Created on November 27, 2014, 3:23 PM
 */
#include <xc.h>         /* XC8 General Include File */
#include <stdio.h>
#include <stdlib.h>

#pragma config FOSC = INTOSC    // Oscillator Selection (INTOSC oscillator: I/O function on CLKIN pin)
#pragma config WDTE = SWDTEN    // Watchdog Timer Enable by soft
#pragma config MCLRE = OFF      // MCLR Pin Function Select 
#pragma config BOREN = OFF      // Brown-out Reset Enable (Brown-out Reset disabled)
#pragma config PLLEN = OFF      // PLL Enable (4x PLL disabled)
  
#define _XTAL_FREQ  (8000000UL)

// nRF commands
#define W_TX_PAYLOAD    0b10100000
#define W_CONFIG        0b00100000
#define R_CONFIG        0b00000000
#define W_TX_ADDR       0b00110000
#define W_SETUP_RETR    0b00100100
#define W_RF_SETUP      0b00100110
#define W_SATUS         0b00100111
#define R_SATUS         0b00000111
#define FLUSH_TX        0b11100001
#define W_FEATURE       0b00111101
#define W_DYNPD         0b00111100

//MAX312723 cmmands
#define W_CONFIG_MAX    0x80

// nFR24L01 control pins
#define CE_nRF  RC2
#define CSN_nRF RC3
#define CE_TempProbe  RC5

// solar brightness
#define Solar_Pin = RA2

// Pins
#define LED RC4

//proto
void nRF_spiRW(char command, char * data, int dataLength);
void tempProbe_spiRW(char command, char* data, int dataLength);
void sendData();

//global var
unsigned char payload[32];        //Payload for nRF24L01+
unsigned char result[8];

  
void main() {
    OSCCONbits.IRCF = 0b1110;   // 8MHz
    WDTCONbits.WDTPS = 0b01100; // watchdog for 4sec sleep

    ANSELC = 0b00000000;    // no analog on port C
    ANSELA = 0b00000100;    // A2 analog input

    TRISC4 = 0;             // LED output
    TRISC2 = 0;             // SPI nFR CE command pin
    TRISC3 = 0;             // SPI nFR CSN command pin
    TRISC5 = 0;             // SPI MAX CE command pin
    TRISC0 = 0;             // SPI CLK as output
    TRISC1 = 1;             // SPI SDI as input

    TRISA4 = 0;             // SPI SDO as output
    TRISA2 = 1;             // analog Solar input

    SDOSEL = 1;             // SPI SDO on pin4: RA4
    SSSEL = 1;              // SS1 on RA3
    CKP = 1;                // SPI clk phase
    CKE = 0;                // SPI clk default low
    SSPM0 = 0;              // SPI clock Fosc/64
    SSPM1 = 1;              // "
    SSPM2 = 0;              // "
    SSPM3 = 0;              // "
    SSPEN = 1;              // Enable SPI

    unsigned char i = 0;

    CSN_nRF = 1;
    CE_TempProbe = 0;

    __delay_ms(1);
    // configure TempProbe for 1 shot conversion
    payload[0] = 0b00010111;
    tempProbe_spiRW(0x80, payload, 1);

    while(1){
        // start voltage reference
        FVRCONbits.ADFVR = 0b01;    // Internal 1.024v ref
        FVRCONbits.FVREN = 1;                  // Enable FVR module

        // Get temperature and relaunch measurement
        payload[0] = 0b00000000;
        tempProbe_spiRW(0x01, payload, 1); // read temp LSB
        result[0] = payload[0];
        payload[0] = 0b00000000;
        tempProbe_spiRW(0x02, payload, 1); // read temp MSB
        result[1] = payload[0];
        payload[0] = 0b00010111;
        tempProbe_spiRW(0x80, payload, 1); // configure TempProbe for 1 shot conversion

        // get battery voltage
        while(!FVRCONbits.FVRRDY) {}; // Wait for FVR to be stable
        ADCON1bits.ADFM = 1;        // Right justify result
        ADCON0bits.CHS = 0b11111;   // FVR is ADC input
        ADCON1bits.ADPREF = 0b00;   // Positive ref is Vdd (default)
        ADCON1bits.ADNREF = 0b00;   // Negative ref is GND (default)
        ADCON1bits.ADCS = 0b010;     // 4us conversion
        ADCON0bits.ADON = 1;        // Turn on ADC module

        __delay_us(20);
        ADCON0bits.GO_nDONE = 1;    // Start a conversion
        while (ADCON0bits.GO_nDONE) {} ;// Wait for it to be completed

        result[2] = ADRESH;         // Store the result in adc_val
        result[3] = ADRESL;

//        // get brighness
//        ADCON0bits.CHS = 0b00010;   // A2 is ADC input
//        ADCON1bits.ADPREF = 0b11;   // Positive ref is FVR
//        ADCON1bits.ADNREF = 0b00;   // Negative ref is GND (default)
//
//        __delay_us(20);
//        ADCON0bits.GO_nDONE = 1;    // Start a conversion
//        while (ADCON0bits.GO_nDONE) {} ;// Wait for it to be completed
//
//        result[4] = ADRESH;         // Store the result in adc_val
//        result[5] = ADRESL;

        // adc, fvr off
        FVREN = 0;                  // disable FVR module
        ADCON0bits.ADON = 0;        // disable ADC

        LED = 1;
        sendData();
        LED = 0;

        // watch dog enable to wake-up after 4sec
        SWDTEN = 1;
        SLEEP();
        NOP();
    }
}

void tempProbe_spiRW(char command, char * data, int dataLength){
    CKP = 1;                // SPI clk phase
    CKE = 0;                // SPI clk default low

    CE_TempProbe = 1;
    SSPBUF = command;
    while (BF == 0){;}
    TXREG = SSPBUF;
    while (TRMT == 0){;}

    for(unsigned int i=0; i<dataLength; i++){
        SSPBUF = data[i];
        while (BF == 0){;}
        data[i] = SSPBUF;
        TXREG = data[i];
        while (TRMT == 0){;}
    }
    CE_TempProbe = 0;
}

void nRF_spiRW(char command, char * data, int dataLength){
    CKP = 0;                // SPI clk phase
    CKE = 1;                // SPI clk default low

    CSN_nRF = 0;
    SSPBUF = command;
    while (BF == 0){;}
    TXREG = SSPBUF;
    while (TRMT == 0){;}

    for(unsigned int i=0; i<dataLength; i++){
        SSPBUF = data[i];
        while (BF == 0){;}
        data[i] = SSPBUF;
        TXREG = data[i];
        while (TRMT == 0){;}
    }
    CSN_nRF = 1;
}

void sendData(){
    // nFR24L01+ re-init
    payload[0] = 0b00001110;
    nRF_spiRW(W_CONFIG, payload, 1);  // Power up
    __delay_ms(5);

    payload[0] = 0b00000100;
    nRF_spiRW(W_FEATURE, payload, 1);  // write FEATURE enable dynamic payload

    payload[0] = 0b00111111;
    nRF_spiRW(W_DYNPD, payload, 1);  // enable dynamic payload for all pipes

    payload[0] = 0xC5;
    payload[1] = 0xC2;
    payload[2] = 0xC2;
    payload[3] = 0xC2;
    payload[4] = 0xC2;
    nRF_spiRW(W_TX_ADDR, payload, 5);  // TX address

    payload[0] = 0b00100110;
    nRF_spiRW(W_RF_SETUP, payload, 1);  // 250Kbps

    payload[0] = 0b00011111;
    nRF_spiRW(W_SETUP_RETR, payload, 1);  // retransmit up to 15 times every 500us

    payload[0] = 0b01111110;
    nRF_spiRW(W_SATUS, payload, 1);  // clear MAX_TR flag

    nRF_spiRW(FLUSH_TX, payload, 0);       // Flush TX fifo

    // Send over the air
    payload[0] = 'T';
    payload[1] = '2';
    payload[2] = result[0];
    payload[3] = result[1];
    payload[4] = 'B';
    payload[5] = '2';
    payload[6] = result[2];
    payload[7] = result[3];
//    payload[8] = 'L';
//    payload[9] = '2';
//    payload[10] = result[4];
//    payload[11] = result[5];

    nRF_spiRW(W_TX_PAYLOAD, payload, 8);

    // sending signal
    CE_nRF = 1;
    __delay_us(50);
    CE_nRF = 0;
    __delay_ms(1);
    payload[0] = 0b00001100;
    nRF_spiRW(W_CONFIG, payload, 1);  // Power down
    __delay_ms(1);

}
