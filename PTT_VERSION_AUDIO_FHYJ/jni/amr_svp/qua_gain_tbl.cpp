/* ------------------------------------------------------------------
 * Copyright (C) 1998-2009 PacketVideo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */
/****************************************************************************************
Portions of this file are derived from the following 3GPP standard:

    3GPP TS 26.073
    ANSI-C code for the Adaptive Multi-Rate (AMR) speech codec
    Available from http://www.3gpp.org

(C) 2004, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TTA, TTC)
Permission to distribute, modify and use this file under the standard license
terms listed above has been obtained from the copyright holder.
****************************************************************************************/
/*

 Filename: qua_gain_tbl.cpp

------------------------------------------------------------------------------
 MODULE DESCRIPTION

------------------------------------------------------------------------------
*/

/*----------------------------------------------------------------------------
; INCLUDES
----------------------------------------------------------------------------*/
#include "qua_gain_tbl.h"
#include "qua_gain.h"

/*--------------------------------------------------------------------------*/
#ifdef __cplusplus
extern "C"
{
#endif

    /*----------------------------------------------------------------------------
    ; MACROS
    ; [Define module specific macros here]
    ----------------------------------------------------------------------------*/

    /*----------------------------------------------------------------------------
    ; DEFINES
    ; [Include all pre-processor statements here. Include conditional
    ; compile variables also.]
    ----------------------------------------------------------------------------*/

    /*----------------------------------------------------------------------------
    ; LOCAL FUNCTION DEFINITIONS
    ; [List function prototypes here]
    ----------------------------------------------------------------------------*/

    /*----------------------------------------------------------------------------
    ; LOCAL VARIABLE DEFINITIONS
    ; [Variable declaration - defined here and used outside this module]
    ----------------------------------------------------------------------------*/

    /* The tables contains the following data:
     *
     *    g_pitch        (Q14),
     *    g_fac          (Q12), (g_code = g_code0*g_fac),
     *    qua_ener_MR122 (Q10), (log2(g_fac))
     *    qua_ener       (Q10)  (20*log10(g_fac))
     *
     *    The log2() and log10() values are calculated on the fixed point value
     *    (g_fac Q12) and not on the original floating point value of g_fac
     *    to make the quantizer/MA predictdor use corresponding values.
     */

    /* table used in 'high' rates: MR67 MR74 */

    const Word16 table_gain_highrates[VQ_SIZE_HIGHRATES*4] =
    {

        /*
         * Note: column 4 (qua_ener) contains the original values from IS641
         *       to ensure bit-exactness; however, they are not exactly the
         *       rounded value of (20*log10(g_fac))
         *
         */

        /*g_pit,    g_fac,  qua_ener_MR122, qua_ener */
        577,      662,           -2692,   -16214,
        806,     1836,           -1185,    -7135,
        3109,     1052,           -2008,   -12086,
        4181,     1387,           -1600,    -9629,
        2373,     1425,           -1560,    -9394,
        3248,     1985,           -1070,    -6442,
        1827,     2320,            -840,    -5056,
        941,     3314,            -313,    -1885,
        2351,     2977,            -471,    -2838,
        3616,     2420,            -777,    -4681,
        3451,     3096,            -414,    -2490,
        2955,     4301,              72,      434,
        1848,     4500,             139,      836,
        3884,     5416,             413,     2484,
        1187,     7210,             835,     5030,
        3083,     9000,            1163,     7002,
        7384,      883,           -2267,   -13647,
        5962,     1506,           -1478,    -8900,
        5155,     2134,            -963,    -5800,
        7944,     2009,           -1052,    -6335,
        6507,     2250,            -885,    -5327,
        7670,     2752,            -588,    -3537,
        5952,     3016,            -452,    -2724,
        4898,     3764,            -125,     -751,
        6989,     3588,            -196,    -1177,
        8174,     3978,             -43,     -260,
        6064,     4404,             107,      645,
        7709,     5087,             320,     1928,
        5523,     6021,             569,     3426,
        7769,     7126,             818,     4926,
        6060,     7938,             977,     5885,
        5594,    11487,            1523,     9172,
        10581,     1356,           -1633,    -9831,
        9049,     1597,           -1391,    -8380,
        9794,     2035,           -1033,    -6220,
        8946,     2415,            -780,    -4700,
        10296,     2584,            -681,    -4099,
        9407,     2734,            -597,    -3595,
        8700,     3218,            -356,    -2144,
        9757,     3395,            -277,    -1669,
        10177,     3892,             -75,     -454,
        9170,     4528,             148,      891,
        10152,     5004,             296,     1781,
        9114,     5735,             497,     2993,
        10500,     6266,             628,     3782,
        10110,     7631,             919,     5534,
        8844,     8727,            1117,     6728,
        8956,    12496,            1648,     9921,
        12924,      976,           -2119,   -12753,
        11435,     1755,           -1252,    -7539,
        12138,     2328,            -835,    -5024,
        11388,     2368,            -810,    -4872,
        10700,     3064,            -429,    -2580,
        12332,     2861,            -530,    -3192,
        11722,     3327,            -307,    -1848,
        11270,     3700,            -150,     -904,
        10861,     4413,             110,      663,
        12082,     4533,             150,      902,
        11283,     5205,             354,     2132,
        11960,     6305,             637,     3837,
        11167,     7534,             900,     5420,
        12128,     8329,            1049,     6312,
        10969,    10777,            1429,     8604,
        10300,    17376,            2135,    12853,
        13899,     1681,           -1316,    -7921,
        12580,     2045,           -1026,    -6179,
        13265,     2439,            -766,    -4610,
        14033,     2989,            -465,    -2802,
        13452,     3098,            -413,    -2482,
        12396,     3658,            -167,    -1006,
        13510,     3780,            -119,     -713,
        12880,     4272,              62,      374,
        13533,     4861,             253,     1523,
        12667,     5457,             424,     2552,
        13854,     6106,             590,     3551,
        13031,     6483,             678,     4084,
        13557,     7721,             937,     5639,
        12957,     9311,            1213,     7304,
        13714,    11551,            1532,     9221,
        12591,    15206,            1938,    11667,
        15113,     1540,           -1445,    -8700,
        15072,     2333,            -832,    -5007,
        14527,     2511,            -723,    -4352,
        14692,     3199,            -365,    -2197,
        15382,     3560,            -207,    -1247,
        14133,     3960,             -50,     -300,
        15102,     4236,              50,      298,
        14332,     4824,             242,     1454,
        14846,     5451,             422,     2542,
        15306,     6083,             584,     3518,
        14329,     6888,             768,     4623,
        15060,     7689,             930,     5602,
        14406,     9426,            1231,     7413,
        15387,     9741,            1280,     7706,
        14824,    14271,            1844,    11102,
        13600,    24939,            2669,    16067,
        16396,     1969,           -1082,    -6517,
        16817,     2832,            -545,    -3283,
        15713,     2843,            -539,    -3248,
        16104,     3336,            -303,    -1825,
        16384,     3963,             -49,     -294,
        16940,     4579,             165,      992,
        15711,     4599,             171,     1030,
        16222,     5448,             421,     2537,
        16832,     6382,             655,     3945,
        15745,     7141,             821,     4944,
        16326,     7469,             888,     5343,
        16611,     8624,            1100,     6622,
        17028,    10418,            1379,     8303,
        15905,    11817,            1565,     9423,
        16878,    14690,            1887,    11360,
        16515,    20870,            2406,    14483,
        18142,     2083,            -999,    -6013,
        19401,     3178,            -375,    -2257,
        17508,     3426,            -264,    -1589,
        20054,     4027,             -25,     -151,
        18069,     4249,              54,      326,
        18952,     5066,             314,     1890,
        17711,     5402,             409,     2461,
        19835,     6192,             610,     3676,
        17950,     7014,             795,     4784,
        21318,     7877,             966,     5816,
        17910,     9289,            1210,     7283,
        19144,     9290,            1210,     7284,
        20517,    11381,            1510,     9089,
        18075,    14485,            1866,    11234,
        19999,    17882,            2177,    13108,
        18842,    32764,            3072,    18494
    };


    /* table used in 'low' rates: MR475, MR515, MR59 */

    const Word16 table_gain_lowrates[VQ_SIZE_LOWRATES*4] =
    {
        /*g_pit,    g_fac,  qua_ener_MR122, qua_ener */
        10813,    28753,            2879,    17333,
        20480,     2785,            -570,    -3431,
        18841,     6594,             703,     4235,
        6225,     7413,             876,     5276,
        17203,    10444,            1383,     8325,
        21626,     1269,           -1731,   -10422,
        21135,     4423,             113,      683,
        11304,     1556,           -1430,    -8609,
        19005,    12820,            1686,    10148,
        17367,     2498,            -731,    -4398,
        17858,     4833,             244,     1472,
        9994,     2498,            -731,    -4398,
        17530,     7864,             964,     5802,
        14254,     1884,           -1147,    -6907,
        15892,     3153,            -387,    -2327,
        6717,     1802,           -1213,    -7303,
        18186,    20193,            2357,    14189,
        18022,     3031,            -445,    -2678,
        16711,     5857,             528,     3181,
        8847,     4014,             -30,     -180,
        15892,     8970,            1158,     6972,
        18022,     1392,           -1594,    -9599,
        16711,     4096,               0,        0,
        8192,      655,           -2708,   -16305,
        15237,    13926,            1808,    10884,
        14254,     3112,            -406,    -2444,
        14090,     4669,             193,     1165,
        5406,     2703,            -614,    -3697,
        13434,     6553,             694,     4180,
        12451,      901,           -2237,   -13468,
        12451,     2662,            -637,    -3833,
        3768,      655,           -2708,   -16305,
        14745,    23511,            2582,    15543,
        19169,     2457,            -755,    -4546,
        20152,     5079,             318,     1913,
        6881,     4096,               0,        0,
        20480,     8560,            1089,     6556,
        19660,      737,           -2534,   -15255,
        19005,     4259,              58,      347,
        7864,     2088,            -995,    -5993,
        11468,    12288,            1623,     9771,
        15892,     1474,           -1510,    -9090,
        15728,     4628,             180,     1086,
        9175,     1433,           -1552,    -9341,
        16056,     7004,             793,     4772,
        14827,      737,           -2534,   -15255,
        15073,     2252,            -884,    -5321,
        5079,     1228,           -1780,   -10714,
        13271,    17326,            2131,    12827,
        16547,     2334,            -831,    -5002,
        15073,     5816,             518,     3118,
        3932,     3686,            -156,     -938,
        14254,     8601,            1096,     6598,
        16875,      778,           -2454,   -14774,
        15073,     3809,            -107,     -646,
        6062,      614,           -2804,   -16879,
        9338,     9256,            1204,     7251,
        13271,     1761,           -1247,    -7508,
        13271,     3522,            -223,    -1343,
        2457,     1966,           -1084,    -6529,
        11468,     5529,             443,     2668,
        10485,      737,           -2534,   -15255,
        11632,     3194,            -367,    -2212,
        1474,      778,           -2454,   -14774
    };

    /*--------------------------------------------------------------------------*/
#ifdef __cplusplus
}
#endif

/*
------------------------------------------------------------------------------
 FUNCTION NAME:
------------------------------------------------------------------------------
 INPUT AND OUTPUT DEFINITIONS

 Inputs:
    None

 Outputs:
    None

 Returns:
    None

 Global Variables Used:
    None

 Local Variables Needed:
    None

------------------------------------------------------------------------------
 FUNCTION DESCRIPTION

 None

------------------------------------------------------------------------------
 REQUIREMENTS

 None

------------------------------------------------------------------------------
 REFERENCES

 [1] qua_gain.tab,  UMTS GSM AMR speech codec,
                    R99 - Version 3.2.0, March 2, 2001

------------------------------------------------------------------------------
 PSEUDO-CODE


------------------------------------------------------------------------------
 CAUTION [optional]
 [State any special notes, constraints or cautions for users of this function]

------------------------------------------------------------------------------
*/

/*----------------------------------------------------------------------------
; FUNCTION CODE
----------------------------------------------------------------------------*/







