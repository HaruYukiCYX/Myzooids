// ARM runtime support - Comm Port Server definitions.
//
// Copyright (c) 2001, 2002 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#define __CPS_TAG_POKE                  0x00
#define __CPS_TAG_PEEK                  0x01
#define __CPS_TAG_MEMSET                0x02
#define __CPS_TAG_ERASE                 0x03
#define __CPS_TAG_ERASE_ALL             0x04
#define __CPS_TAG_TERMINATE             0x05
#define __CPS_TAG_VERIFY                0x06
#define __CPS_TAG_SET_PARAM             0x07
#define __CPS_TAG_CRC32                 0x08
#define __CPS_TAG_CALL                  0x09
#define __CPS_TAG_TERMINATE_NO_RESPONSE 0x0A

#define __CPS_CTRL_TAG_MASK             0x0F
#define __CPS_CTRL_LENGTH_OFFSET        0x04

