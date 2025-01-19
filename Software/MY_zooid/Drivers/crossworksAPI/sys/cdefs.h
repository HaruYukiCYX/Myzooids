// Rowley CrossWorks, runtime support.
//
//  Copyright (c) 2001-2024 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#ifndef	_CDEFS_H_
#define	_CDEFS_H_

#if defined(__cplusplus)

#define	__BEGIN_DECLS	extern "C" {
#define	__END_DECLS	};

#else

#define	__BEGIN_DECLS
#define	__END_DECLS

#endif

#define	__P(protos)	protos
#define	__CONCAT1(x,y)	x ## y
#define	__CONCAT(x,y)	__CONCAT1(x,y)
#define	__STRING(x)	#x

#define	__used		__attribute__((__used__))
#endif
