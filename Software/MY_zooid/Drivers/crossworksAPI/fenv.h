// Rowley CrossWorks, runtime support.
//
//  Copyright (c) 2001-2024 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#ifndef _FENV_H
#define _FENV_H

#ifdef __cplusplus
extern "C" {
#endif

typedef unsigned long fenv_t;
typedef unsigned long fexcept_t;

#define FE_DIVBYZERO   0x08
#define FE_INEXACT     0x01
#define FE_INVALID     0x10
#define FE_OVERFLOW    0x04
#define FE_UNDERFLOW   0x02

#define FE_ALL_EXCEPT \
  (FE_DIVBYZERO  | \
   FE_INEXACT    | \
   FE_INVALID    | \
   FE_OVERFLOW   | \
   FE_UNDERFLOW)

#define FE_DOWNWARD   0x3
#define FE_TONEAREST  0x0
#define FE_TOWARDZERO 0x1
#define FE_UPWARD     0x2

#define FE_DFL_ENV ((const fenv_t *) 0)

int  feclearexcept(int);
int  fegetexceptflag(fexcept_t *, int);
int  feraiseexcept(int);
int  fesetexceptflag(const fexcept_t *, int);
int  fetestexcept(int);
int  fegetround(void);
int  fesetround(int);
int  fegetenv(fenv_t *);
int  feholdexcept(fenv_t *);
int  fesetenv(const fenv_t *);
int  feupdateenv(const fenv_t *);

#ifdef __cplusplus
}
#endif

#endif
