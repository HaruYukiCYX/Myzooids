// Rowley CrossWorks, runtime support.
//
//  Copyright (c) 2001-2024 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

// IEEE Std 1003.1-2008 (POSIX.1) extended locales.

#ifndef __xlocale_H
#define __xlocale_H

// The extended locale, or "xlocale" API consists of five core functions:
// newlocale, duplocale, freelocale, querylocale, and uselocale.
// For each of these functions, a NULL locale indicates the current locale.
// We don't implement querylocale

#include "__crossworks.h"

#ifdef __cplusplus
extern "C" {
#endif

#define LC_COLLATE_MASK   (1 << LC_COLLATE)
#define LC_CTYPE_MASK     (1 << LC_CTYPE)
#define LC_MONETARY_MASK  (1 << LC_MONETARY)
#define LC_NUMERIC_MASK   (1 << LC_NUMERIC)
#define LC_TIME_MASK      (1 << LC_TIME)
#define LC_MESSAGES_MASK  0   // Not yet supported (1 << LC_MESSAGES)

#define LC_ALL_MASK        ((int)(~0U))

#define LC_GLOBAL_LOCALE  &__RAL_global_locale

#ifndef __RAL_LOCALE_T_DEFINED
#define __RAL_LOCALE_T_DEFINED
typedef struct __locale_s *locale_t;
#endif


/*! \brief Create a new locale \ingroup Functions \synopsis

  \desc \b \this creates a new locale object or modifies an existing one.
  If the base argument is \b NULL, a new locale object is created.

  \a category_mask specifies the locale categories to be set or modified.
  Values for \a category_mask are constructed by a bitwise-inclusive OR
  of the symbolic constants \b LC_CTYPE_MASK, \b LC_NUMERIC_MASK, \b LC_TIME_MASK,
  \b LC_COLLATE_MASK, \b LC_MONETARY_MASK, and \b LC_MESSAGES_MASK.

  For each category with the corresponding bit set in \a category_mask, the data
  from the locale named by \a locale is used. In the case of modifying an existing
  locale object, the data from the locale named by \a locale replaces the existing
  data within the locale object. If a completely new locale object is created,
  the data for all sections not requested by \a category_mask are taken from the
  default locale.

  The locales \q C and \q POSIX are equivalent and defined for all settings
  of \a category_mask:

  If \a locale is \b NULL, then the \q C locale is used. If \a locale is an empty
  string, \b \this will use the default locale.

  If \a base is \b NULL, the current locale is used. If \a base is \b LC_GLOBAL_LOCALE,
  the global locale is used.

  If \a mask is \b LC_ALL_MASK, \a base is ignored.

  \note POSIX.1-2008 does not specify whether the locale object pointed to by \a base
    is modified or whether it is freed and a new locale object created.

  \task Implementation

  The category mask \b LC_MESSAGES_MASK is not implemented as POSIX messages
  are not implemented.
*/
locale_t newlocale(int category_mask, const char *locale, locale_t base);


/*! \brief Duplicate current locale data \ingroup Functions \synopsis

  \description \b \this duplicates the locale object referenced by \a loc.

  If there is insufficient memory to duplicate \a loc, \b \this returns
  \a NULL and sets \a errno to \a ENOMEM as required by POSIX.1-2008.

  Duplicated locales must be freed with \b freelocale.

  This is different behavior from the GNU glibc implementation which
  makes no mention of setting \b errno on failure.

  \note This extension is derived from BSD, POSIX.1, and glibc.
*/
locale_t duplocale(locale_t loc);


/*! \brief Free a locale \ingroup Functions \synopsis

  \desc \b \this frees the storage associated with \a loc.

  \b \this zero on success, \-1 on error.  
*/  
int freelocale(locale_t loc);


/*! \brief Get locale data \ingroup Functions \synopsis

  \description \b \this returns a pointer to a structure of type \b lconv with the
  corresponding values for the locale \a loc filled in.
*/
struct lconv *localeconv_l(locale_t loc);


/*! \brief Set or query the current locale \ingroup Functions \synopsis

  \description \b \this installs a thread-local locale as returned from \b newlocale or \b duplocale and
  returns the previous thread-local locale. If \a loc is \a NULL then the thread-local locale is unchanged.
*/
locale_t uselocale(locale_t loc);

#ifdef __cplusplus
}
#endif

#endif
