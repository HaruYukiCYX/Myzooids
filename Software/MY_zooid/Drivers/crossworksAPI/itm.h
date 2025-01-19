//
// Copyright (c) 2012 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#ifndef __itm_h
#define __itm_h

#ifdef __cplusplus
extern "C" {
#endif

/*! \brief Check if an ITM channel is enabled \ingroup Functions \synopsis

 \desc \this returns 1 if the given ITM channel is enabled otherwise it returns 0.

 \a n is the ITM channel number from 0 to 31.

*/

int ITM_channel_enabled(int n);

/*! \brief Send a byte to an ITM channel \ingroup Functions \synopsis

 \desc \this sends the byte \a b to the ITM channel \a n.

 \a n is the ITM channel number from 0 to 31.

*/

void ITM_send_byte(int n, unsigned char b);

/*! \brief Send a half word to an ITM channel \ingroup Functions \synopsis

 \desc \this sends the half word \a s to the ITM channel \a n.

 \a n is the ITM channel number from 0 to 31.

*/

void ITM_send_half_word(int n, unsigned short s);

/*! \brief Send a word to an ITM channel \ingroup Functions \synopsis

 \desc \this sends the word \a w to the ITM channel \a n.

 \a n is the ITM channel number from 0 to 31.

*/

void ITM_send_word(int n, unsigned w);

/*! \brief Send the program counter of the caller to an ITM channel \ingroup Functions \synopsis

 \desc \this sends the program counter of the caller to the ITM channel \a n.

 \a n is the ITM channel number from 0 to 31.

*/

void ITM_send_pc(int n);

/*! \brief The base address of the ITM peripheral \ingroup Variables \synopsis

  \desc \this is the base address of the ITM peripheral. It must be
  assigned for ITM on V7A/V7R architectures. It is not required for V7M architectures.
*/
extern volatile unsigned *ITM_base;

#ifdef __cplusplus
}
#endif

#endif
