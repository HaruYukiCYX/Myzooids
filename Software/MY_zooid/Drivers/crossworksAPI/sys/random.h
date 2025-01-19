// Rowley CrossWorks, runtime support.
//
//  Copyright (c) 2001-2024 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#ifndef _RANDOM_H
#define _RANDOM_H

#ifdef __cplusplus
extern "C" {
#endif

#include <sys/types.h>

int getentropy(void* __buffer, size_t __buffer_size);

ssize_t getrandom(void* __buffer, size_t __buffer_size, unsigned int __flags);

#ifdef __cplusplus
}
#endif

#endif
