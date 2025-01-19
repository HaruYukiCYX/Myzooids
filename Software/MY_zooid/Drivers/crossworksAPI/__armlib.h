// ARM runtime support.
//
// Copyright (c) 2006 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#ifndef __armlib_H
#define __armlib_H

#ifdef __cplusplus
extern "C" {
#endif
                            
/*****************************************************************************
 * Function    : __ARMLIB_enableIRQ                                          *
 * Description : Enable ARM's global interrupts                              *
 *****************************************************************************/
void __ARMLIB_enableIRQ(void);

/*****************************************************************************
 * Function    : __ARMLIB_disableIRQ                                         *
 * Description : Disable ARM's global interrupts                             *
 *****************************************************************************/
void __ARMLIB_disableIRQ(void);

/*****************************************************************************
 * Function    : __ARMLIB_isrEnableIRQ                                       *
 * Description : Re-enable ARM's global interrupts from within an IRQ ISR    *
 *               NOTE: Call must be accompanied with a call to               *
 *               __ARMLIB_isrDisableIRQ prior to completion of the ISR.      *
 *****************************************************************************/
void __ARMLIB_isrEnableIRQ(void);

/*****************************************************************************
 * Function    : __ARMLIB_isrDisableIRQ                                      *
 * Description : Re-disable ARM's global interrupts from within an IRQ ISR   *
 *****************************************************************************/
void __ARMLIB_isrDisableIRQ(void);

/*****************************************************************************
 * Function    : __ARMLIB_enableFIQ                                          *
 * Description : Enable ARM's global interrupts                              *
 *****************************************************************************/
void __ARMLIB_enableFIQ(void);

/*****************************************************************************
 * Function    : __ARMLIB_disableFIQ                                         *
 * Description : Disable ARM's global interrupts                             *
 *****************************************************************************/
void __ARMLIB_disableFIQ(void);

#ifdef __cplusplus
}
#endif

#endif
