/**
* @file bmp085_interface.c
*
* Interface functions for the BMP085 pressure and temperature sensor.
*/

#include "bmp085_interface.h"

static char A1[2], A2[2], A3[2], A4[2], A5[2], A6[2], B1[2], B2[2], MB[2], MC[2], MD[2];

static int32_t B5;

//Macros to retreive barometer config values.
#define A1_s  (int32_t)(((int16_t*)A1)[0])
#define A1_u (uint32_t)(((int16_t*)A1)[0])
#define A2_s  (int32_t)(((int16_t*)A2)[0])
#define A2_u (uint32_t)(((int16_t*)A2)[0])
#define A3_s  (int32_t)(((int16_t*)A3)[0])
#define A3_u (uint32_t)(((int16_t*)A3)[0])

#define A4_s  (int32_t)(((uint16_t*)A4)[0])
#define A4_u (uint32_t)(((uint16_t*)A4)[0])
#define A5_s  (int32_t)(((uint16_t*)A5)[0])
#define A5_u (uint32_t)(((uint16_t*)A5)[0])
#define A6_s  (int32_t)(((uint16_t*)A6)[0])
#define A6_u (uint32_t)(((uint16_t*)A6)[0])

#define B1_s  (int32_t)(((int16_t*)B1)[0])
#define B1_u (uint32_t)(((int16_t*)B1)[0])
#define B2_s  (int32_t)(((int16_t*)B2)[0])
#define B2_u (uint32_t)(((int16_t*)B2)[0])

#define MB_s  (int32_t)(((int16_t*)MB)[0])
#define MB_u (uint32_t)(((int16_t*)MB)[0])
#define MC_s  (int32_t)(((int16_t*)MC)[0])
#define MC_u (uint32_t)(((int16_t*)MC)[0])
#define MD_s  (int32_t)(((int16_t*)MD)[0])
#define MD_u (uint32_t)(((int16_t*)MD)[0])

/**
 * Reads cofiguration data from the barometer.
 */
void bmp_Init()
{
    char out = BAR_CON_ADDR;

    twi_Write(BAR_ADDR, &out, 1);

    twi_Read(BAR_ADDR, A1, 2);
    swap(A1[0], A1[1]);

    twi_Read(BAR_ADDR, A2, 2);
    swap(A2[0], A2[1]);

    twi_Read(BAR_ADDR, A3, 2);
    swap(A3[0], A3[1]);

    twi_Read(BAR_ADDR, A4, 2);
    swap(A4[0], A4[1]);

    twi_Read(BAR_ADDR, A5, 2);
    swap(A5[0], A5[1]);

    twi_Read(BAR_ADDR, A6, 2);
    swap(A6[0], A6[1]);

    twi_Read(BAR_ADDR, B1, 2);
    swap(B1[0], B1[1]);

    twi_Read(BAR_ADDR, B2, 2);
    swap(B2[0], B2[1]);

    twi_Read(BAR_ADDR, MB, 2);
    swap(MB[0], MB[1]);

    twi_Read(BAR_ADDR, MC, 2);
    swap(MC[0], MC[1]);

    twi_Read(BAR_ADDR, MD, 2);
    swap(MD[0], MD[1]);
}

/**
 * Sends a temperature request to the barometer.
 */
void bmp_Request_Temperature()
{
    char out[2] = {BAR_REQ_ADDR, BAR_TEMP_REQ};

    twi_Write(BAR_ADDR, out, 2);
}

/**
 * Sends a pressure request to the barometer.
 * This must be done after a full temperature request
 */
void bmp_Request_Pressure()
{
    char out[2] = {BAR_REQ_ADDR, BAR_PRES_REQ};

    twi_Write(BAR_ADDR, out, 2);
}

/**
 * Reads the temperature register in the barometer.
 * See the bmp085 datasheet for an explaination of the math.
 *
 * @return The temperature in 0.1 degrees C.
 */
int16_t bmp_Read_Temperature()
{
    char in[2];
    char out = BAR_RES_ADDR;

    twi_Write(BAR_ADDR, &out, 1);
    twi_Read(BAR_ADDR, in, 2);

    int32_t UT = ((int32_t)in[0] << 8) + (int32_t)in[1];

    int32_t X1 = (UT - A6_s) * A5_s / 32768;
    int32_t X2 = MC_s * 2048 / (X1 + MD_s);
    B5 = X1 + X2;
    return (B5 + 8) / 0x10;
}

/**
 * Reads the pressure register in the barometer.
 * See the bmp085 datasheet for an explaination of the math.
 *
 * @return The pressure in Pa.
 */
int32_t bmp_Read_Pressure()
{
    char in[2];
    char out = BAR_RES_ADDR;

    twi_Write(BAR_ADDR, &out, 1);
    twi_Read(BAR_ADDR, in, 2);

    int32_t UP = ((int32_t)in[0] << 8) + (int32_t)in[1];

    int32_t B6 = B5 - 4000;
    int32_t X1 = (B2_s * (B6 * B6 / 4096)) / 2048;
    int32_t X2 = A2_s * B6 / 2048;
    int32_t X3 = X1 + X2;
    int32_t B3 = ((A1_s * 4 + X3) + 2) / 4;

    X1 = A3_s * B6 / 8192;
    X2 = (B1_s * (B6 * B6 / 4096)) / 65536;
    X3 = ((X1 + X2) + 2) / 4;

    uint32_t B4 = (A4_u * (uint32_t)(X3 + 32768)) / 32768;
    uint32_t B7 = ((uint32_t)UP - B3) * 50000;

    int32_t p = (B7 < 0x80000000) ? ((B7 * 2) / B4) : ((B7 / B4) * 2);

    X1 = (p / 256) * (p / 256);

    X1 = (X1 * 3038) / 65536;
    X2 = (-7357 * p) / 65536;
    p = p + (X1 + X2 + 3791) / 16;

    return p;
}