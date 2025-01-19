// Rowley CrossWorks, runtime support.
//
//  Copyright (c) 2001-2024 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#ifndef __stdbool_h
#define __stdbool_h

#ifndef __cplusplus
#if defined(__CROSSWORKS_ARM) || defined(__SES_ARM) || defined(__SES_RISCV)
#define bool _Bool
#else
#define bool int
#endif
#define true 1
#define false 0
#endif
#define __bool_true_false_are_defined 1

#endif

