// Rowley CrossWorks, runtime support.
//
//  Copyright (c) 2001-2024 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#ifndef _SYS_TIMES_H
#define _SYS_TIMES_H

#include <time.h>

#ifdef __cplusplus
extern "C" {
#endif

struct tms
{
  clock_t tms_utime; // user CPU time
  clock_t tms_stime; // system CPU time
  clock_t tms_cutime; // user CPU time of terminated child processes
  clock_t tms_cstime; // system CPU time of terminated child processes
};

clock_t times(struct tms *);

#ifdef __cplusplus
}
#endif

#endif
