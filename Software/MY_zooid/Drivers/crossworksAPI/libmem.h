// Copyright (c) 2006 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#ifndef libmem_h
#define libmem_h

#ifdef __cplusplus
extern "C" {
#endif

#include <stddef.h>
#include <stdint.h>

/*! \brief Successful operation return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating success.
 */
#define LIBMEM_STATUS_SUCCESS                    (1)

/*! \brief Non-specific error return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating a non-specific
 *  error.
 */
#define LIBMEM_STATUS_ERROR                      (0)

/*! \brief Timeout error return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that the operation
 *  has timed out.
 */
#define LIBMEM_STATUS_TIMEOUT                    (-1)

/*! \brief Memory locked return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that the operation
 *  could not be completed because the memory is locked.
 */
#define LIBMEM_STATUS_LOCKED                     (-2)

/*! \brief Not implemented return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that the operation
 *  being carried out has not been implemented in the LIBMEM driver.
 */
#define LIBMEM_STATUS_NOT_IMPLEMENTED            (-3)

/*! \brief No room for geometry information return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that there is not enough room
 *  to store all the geometry region information.
 */
#define LIBMEM_STATUS_GEOMETRY_REGION_OVERFLOW   (-4)

/*! \brief No driver for memory range return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that no driver has
 *  been installed for the region of memory being used.
 */
#define LIBMEM_STATUS_NO_DRIVER                  (-5)

/*! \brief Error reading CFI information return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that an error has
 *  been detected reading out the CFI information.
 */
#define LIBMEM_STATUS_CFI_ERROR                  (-6)

/*! \brief Invalid range return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that an invalid
 *  address range has been passed to the function.
 */
#define LIBMEM_STATUS_INVALID_RANGE              (-7)

/*! \brief Invalid parameter return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that an invalid
 *  parameter has been passed to the function.
 */
#define LIBMEM_STATUS_INVALID_PARAMETER          (-8)

/*! \brief Invalid or unsupported device width return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that an invalid
 *  or unsupported device width has been passed to the function.
 */
#define LIBMEM_STATUS_INVALID_WIDTH              (-9)

/*! \brief Invalid or mismatched device return code \ingroup Return codes \synopsis \description
 *
 *  Status result returned from LIBMEM functions indicating that the driver
 *  has determined that the expected and actual device IDs do not match.
 */
#define LIBMEM_STATUS_INVALID_DEVICE             (-10)


/*! \brief Invalid CFI command set  \ingroup Command set macros \synopsis \description
 *
 *  A definition representing an invalid CFI command set number.
 */
#define LIBMEM_CFI_CMDSET_NONE                   (0x0000)

/*! \brief Intel extended command set \ingroup Command set macros \synopsis \description
 *
 *  A definition representing the CFI command set number for the Intel extended
 *  command set.
 */
#define LIBMEM_CFI_CMDSET_INTEL_EXTENDED         (0x0001)

/*! \brief AMD standard command set \ingroup Command set macros \synopsis \description
 *
 *  A definition representing the CFI command set number for the AMD standard
 *  command set.
 */
#define LIBMEM_CFI_CMDSET_AMD_STANDARD           (0x0002)

/*! \brief Intel standard command set \ingroup Command set macros \synopsis \description
 *
 *  A definition representing the CFI command set number for the Intel standard
 *  command set.
 */
#define LIBMEM_CFI_CMDSET_INTEL_STANDARD         (0x0003)

/*! \brief AMD standard command set \ingroup Command set macros \synopsis \description
 *
 *  A definition representing the CFI command set number for the AMD extended
 *  command set.
 */
#define LIBMEM_CFI_CMDSET_AMD_EXTENDED           (0x0004)

/*! \brief Winbond standard command set \ingroup Command set macros \synopsis \description
 *
 *  A definition representing the CFI command set number for the Winbond standard
 *  command set.
 */
#define LIBMEM_CFI_CMDSET_WINBOND_STANDARD       (0x0006)

/*! \brief Mitsubishi standard command set \ingroup Command set macros \synopsis \description
 *
 *  A definition representing the CFI command set number for the Mitsubishi standard
 *  command set.
 */
#define LIBMEM_CFI_CMDSET_MITSUBISHI_STANDARD    (0x0100)

/*! \brief Mitsubishi extended command set \ingroup Command set macros \synopsis \description
 *
 *  A definition representing the CFI command set number for the Mitsubishi extended
 *  command set.
 */
#define LIBMEM_CFI_CMDSET_MITSUBISHI_EXTENDED    (0x0101)

/*! \brief SST page write command set \ingroup Command set macros \synopsis \description
 *
 *  A definition representing the CFI command set number for the SST page write
 *  command set.
 */
#define LIBMEM_CFI_CMDSET_SST_PAGE_WRITE         (0x0102)

/*! \brief Reserved command set \ingroup Command set macros \synopsis \description
 *
 *  A definition representing the reserved CFI command set number.
 */
#define LIBMEM_CFI_CMDSET_RESERVED               (0xFFFF)

/*! \brief LIBMEM interface version number \ingroup Macros \synopsis \description
 *
 *  The LIBMEM interface version number.
 */
#define LIBMEM_VERSION_NUMBER 4

/*! \brief Determine whether an address is within an address range \ingroup Utility macros \synopsis \description
 *
 *  \b \this is used to determine whether an address is within an address range.
 *
 *  \param address
 *    The address to check.
 *
 *  \param startAddress
 *    The start address of the address range.
 *
 *  \param endAddress
 *    The end address of the address range.
 *
 *  \return
 *    Non-zero if address is within address range.
 */
#define LIBMEM_ADDRESS_IN_RANGE(address, startAddress, endAddress) ((address >= startAddress) && (address <= endAddress))

/*! \brief Determine whether an address range is within another address range \ingroup Utility macros \synopsis \description
 *
 *  \b \this is used to determine whether address range 1 is within address range 2.
 *
 *  \param r1StartAddress
 *    The start address of address range 1.
 *
 *  \param r1EndAddress
 *    The end address of address range 1.
 *
 *  \param r2StartAddress
 *    The start address of address range 2.
 *
 *  \param r2EndAddress
 *    The end address of address range 2.
 *
 *  \return
 *    Non-zero if address range 1 is within address range 2.
 */
#define LIBMEM_RANGE_WITHIN_RANGE(r1StartAddress, r1EndAddress, r2StartAddress, r2EndAddress) (LIBMEM_ADDRESS_IN_RANGE(r1StartAddress, r2StartAddress, r2EndAddress) && LIBMEM_ADDRESS_IN_RANGE(r1EndAddress, r2StartAddress, r2EndAddress))

/*! \brief Determine whether an address range overlaps another address range \ingroup Utility macros \synopsis \description
 *
 *  \b \this is used to determine whether address range 1 overlaps address range 2.
 *
 *  \param r1StartAddress
 *    The start address of address range 1.
 *
 *  \param r1EndAddress
 *    The end address of address range 1.
 *
 *  \param r2StartAddress
 *    The start address of address range 2.
 *
 *  \param r2EndAddress
 *    The end address of address range 2.
 *
 *  \return
 *    Non-zero if address range 1 overlaps address range 2.
 */
#define LIBMEM_RANGE_OVERLAPS_RANGE(r1StartAddress, r1EndAddress, r2StartAddress, r2EndAddress) (LIBMEM_ADDRESS_IN_RANGE(r1StartAddress, r2StartAddress, r2EndAddress) || LIBMEM_ADDRESS_IN_RANGE(r1EndAddress, r2StartAddress, r2EndAddress))

/*! \brief Determine whether an address range overlaps another address range or vice versa \ingroup Utility macros \synopsis \description
 *
 *  \b \this is used to determine whether address range 1 overlaps address range 2 or vice versa.
 *
 *  \param r1StartAddress
 *    The start address of address range 1.
 *
 *  \param r1EndAddress
 *    The end address of address range 1.
 *
 *  \param r2StartAddress
 *    The start address of address range 2.
 *
 *  \param r2EndAddress
 *    The end address of address range 2.
 *
 *  \return
 *    Non-zero if address range 1 overlaps address range 2 or address range 2 overlaps address range 1.
 */
#define LIBMEM_RANGE_OCCLUDES_RANGE(r1StartAddress, r1EndAddress, r2StartAddress, r2EndAddress) (LIBMEM_RANGE_OVERLAPS_RANGE(r1StartAddress, r1EndAddress, r2StartAddress, r2EndAddress) || LIBMEM_RANGE_OVERLAPS_RANGE(r2StartAddress, r2EndAddress, r1StartAddress, r1EndAddress))

/*! \brief Determine whether an address is aligned to a specified width \ingroup Utility macros \synopsis \description
 *
 *  \b \this is used to determine whether an address is aligned to a specified width.
 *
 *  \param address
 *    The address to check alignment of.
 *
 *  \param width
 *    The alignment width.
 *
 *  \return
 *     Non-zero if address is aligned.
 */
#define LIBMEM_ADDRESS_IS_ALIGNED(address, width) \
   ((((uint32_t)address) & ((width) - 1)) == 0)

/*! \brief Return an address aligned to a specified width \ingroup Utility macros \synopsis \description
 *
 *  \b \this returns an address aligned to a specified width.
 *
 *  \param address
 *    The address to align.
 *
 *  \param width
 *    The alignment width.
 *
 *  \return
 *    The aligned address.
 */
#define LIBMEM_ALIGNED_ADDRESS(address, width) \
   ((uint8_t *)(((uint32_t)address) & (~((width) - 1))))

/*! \brief Option to disable paged write data pre-loading \ingroup Driver helper macros \synopsis \description
 *
 *  \b \this disables paged write data pre-loading.
 *
 *  This option can be passed to \a libmem_driver_paged_write_init to disable
 *  pre-loads to the page buffer when switching to a new page. The pre-load
 *  is required if you want the driver to support arbitrary writes without
 *  corrupting existing data, however it may not be supported by all hardware.
 */
#define LIBMEM_DRIVER_PAGED_WRITE_OPTION_DISABLE_PAGE_PRELOAD (1 << 0)

/*! \brief Option to disable direct writes bypassing page buffer \ingroup Driver helper macros \synopsis \description
 *
 *  \b \this disables direct writes bypassing page buffer.
 *
 *  This option can be passed to \a libmem_driver_paged_write_init to stop
 *  the libmem_driver_paged_write function carrying out direct writes bypassing
 *  the page buffer when it is possible to do so. This should be used if the
 *  source data for the write page function must be in RAM.
 */
#define LIBMEM_DRIVER_PAGED_WRITE_OPTION_DISABLE_DIRECT_WRITES (1 << 1)

/*! A type definition of struct \a _libmem_driver_handle_t \synopsis \description
 *
 *  \b \this is a type definition of struct \a _libmem_driver_handle_t.
 */
typedef struct _libmem_driver_handle_t libmem_driver_handle_t;

/*! A type definition of struct \a _libmem_driver_functions_t \synopsis \description
 *
 *  \b \this is a type definition of struct \a _libmem_driver_functions_t.
 */
typedef struct _libmem_driver_functions_t libmem_driver_functions_t;

/*! A type definition of struct \a _libmem_ext_driver_functions_t \synopsis \description
 *
 *  \b \this is a type definition of struct \a _libmem_ext_driver_functions_t.
 */
typedef struct _libmem_ext_driver_functions_t libmem_ext_driver_functions_t;

/*! A type definition of struct \a _libmem_driver_paged_write_ctrlblk_t \synopsis \description
 *
 *  \b \this is a type definition of struct \a _libmem_driver_paged_write_ctrlblk_t.
 */
typedef struct _libmem_driver_paged_write_ctrlblk_t libmem_driver_paged_write_ctrlblk_t;

/*! A type definition of struct \a _libmem_geometry_t \synopsis \description
 *
 *  \b \this is a type definition of struct \a _libmem_geometry_t.
 */
typedef struct _libmem_geometry_t libmem_geometry_t;

/*! A type definition of struct \a _libmem_flash_info_t \synopsis \description
 *
 *  \b \this is a type definition of struct \a _libmem_flash_info_t.
 */
typedef struct _libmem_flash_info_t libmem_flash_info_t;

/*! A type definition of struct \a _libmem_sector_info_t \synopsis \description
 *
 *  \b \this is a type definition of struct \a _libmem_sector_info_t.
 */
typedef struct _libmem_sector_info_t libmem_sector_info_t;

/*! \brief A function pointer to a LIBMEM driver's write function \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a LIBMEM driver's write function.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 * 
 *  \param dest
 *    A pointer to the memory address in memory range handled by driver
 *    to write data to.
 *
 *  \param src
 *    A pointer to the memory address to read data from.
 *
 *  \param size
 *    The number of bytes to write.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  The driver's \a write function copies data from the memory address pointed
 *  to by \a src to the memory address handled by the LIBMEM driver pointed to
 *  by \a dest.
 *
 *  If this operation is not required the function should return 
 *  \a LIBMEM_STATUS_SUCCESS.
 */
typedef int (*libmem_driver_write_fn_t)(libmem_driver_handle_t *h, uint8_t *dest, const uint8_t *src, size_t size);

/*! \brief A function pointer to a LIBMEM driver's fill function \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a LIBMEM driver's fill function.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 * 
 *  \param dest
 *    A pointer to the memory address in memory range handled by driver
 *    to write data to.
 *
 *  \param c
 *    The data byte to write.
 *
 *  \param size
 *    The number of bytes to write.
 *  
 *  \return
 *    The LIBMEM status result.
 *
 *  The driver's \a fill function writes \a size bytes of value \c to the memory
 *  address handled by the LIBMEM driver pointed to by \a dest.
 *
 *  If this operation is not required the function should return 
 *  \a LIBMEM_STATUS_SUCCESS.
 */
typedef int (*libmem_driver_fill_fn_t)(libmem_driver_handle_t *h, uint8_t *dest, uint8_t c, size_t size);

/*! \brief A function pointer to a LIBMEM driver's erase function \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a LIBMEM driver's erase function.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param start
 *    A pointer to the initial memory address in memory range handled by driver
 *    to erase.
 *
 *  \param size
 *    The number of bytes to erase.
 *
 *  \param erase_start
 *    A pointer to a location in memory to store a pointer to the start of
 *    the memory range that has actually been erased or NULL if not required.
 *
 *  \param erase_size
 *    A pointer to a location in memory to store the size in bytes of the
 *    memory range that has actually been erased or NULL if not required.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  The driver's \a erase function should erase \a size bytes of the memory
 *  range handled by the LIBMEM driver pointed to by \a start.
 *
 *  There is no specific module or chip erase driver entry point, it is up to
 *  the driver to decide how best to erase the memory based on the supplied
 *  address range. If the application needs to know what memory was actually
 *  erased it can use the \a erase_start and \a erase_size parameters.
 *
 *  If this operation is not required the function should return 
 *  \a LIBMEM_STATUS_SUCCESS and if the \a erase_start or \a erase_size parameters
 *  are supplied they should be assigned with the values of \a start and \a size.
 */
typedef int (*libmem_driver_erase_fn_t)(libmem_driver_handle_t *h, uint8_t *start, size_t size, uint8_t **erase_start, size_t *erase_size);

/*! \brief A function pointer to a LIBMEM driver's lock function \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a LIBMEM driver's lock function.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param start
 *    A pointer to the initial memory address in memory range handled by driver
 *    to lock.
 *
 *  \param size
 *    The number of bytes to lock.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  The driver's \a lock function should lock \a size bytes of the memory
 *  range handled by the LIBMEM driver pointed to by \a start.
 *
 *  If this operation is not required the function should return 
 *  \a LIBMEM_STATUS_SUCCESS.
 *
 */                      
typedef int (*libmem_driver_lock_fn_t)(libmem_driver_handle_t *h, uint8_t *start, size_t size);

/*! \brief A function pointer to a LIBMEM driver's unlock function \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a LIBMEM driver's unlock function.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param start
 *    A pointer to the initial memory address in memory range handled by driver
 *    to unlock.
 *
 *  \param size
 *    The number of bytes to unlock.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  The driver's \a unlock function should unlock \a size bytes of the memory
 *  range handled by the LIBMEM driver pointed to by \a start.
 *
 *  If this operation is not required the function should return 
 *  \a LIBMEM_STATUS_SUCCESS.
 *
 */
typedef int (*libmem_driver_unlock_fn_t)(libmem_driver_handle_t *h, uint8_t *start, size_t size);

/*! \brief A function pointer to a LIBMEM driver's flush function \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a LIBMEM driver's flush function.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  The driver's \a flush function should complete any outstanding memory
 *  operations (if any) and return the memory to read mode.
 *
 *  If this operation is not required the function should return 
 *  \a LIBMEM_STATUS_SUCCESS.
 *
 */
typedef int (*libmem_driver_flush_fn_t)(libmem_driver_handle_t *h);

/*! \brief A function pointer to a LIBMEM driver's inrange extended function \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a LIBMEM driver's inrange extended function.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param dest
 *    A pointer to then memory location being tested.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  The driver's \a inrange function is an optional extended function.
 *  It has been provided to allow the driver to indicate if it handles
 *  a more complex memory range than the single range described by the
 *  \a start and \a size \a libmem_driver_handle_t fields, for example
 *  if the memory has been aliased over a number of memory ranges.
 * 
 *  The function should return non-zero if the address pointed to by
 *  \a dest is handled by the driver.
 */
typedef int (*libmem_driver_inrange_fn_t)(libmem_driver_handle_t *h, const uint8_t *dest);

/*! \brief A function pointer to a LIBMEM driver's read extended function \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a LIBMEM driver's read extended function.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param dest
 *    A pointer to the initial memory address to write data to.
 *
 *  \param src
 *    A pointer to the initial memory address in the memory range
 *    handled by the driver to read data from.
 *
 *  \param size
 *    The number of bytes to write.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  The driver's \a read function is an optional extended function.
 *  It has been provided to allow you to write a driver for memory
 *  that is not memory mapped.
 *
 *  Typically memory read operations will be direct memory mapped
 *  operations however implementing a driver's \a read function
 *  allows you to access non-memory mapped memory through the
 *  LIBMEM interface.
 */
typedef int (*libmem_driver_read_fn_t)(libmem_driver_handle_t *h, uint8_t *dest, const uint8_t *src, size_t size);

/*! \brief A function pointer to a LIBMEM driver's crc32 extended function \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a LIBMEM driver's crc32 extended function.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param start
 *    A pointer to the start of the address range.
 *
 *  \param size
 *    The size of the address range in bytes.
 *
 *  \param crc
 *    The initial CRC-32 value.
 *
 *  \return
 *    The computed CRC-32 value.
 *
 *  The driver's \a crc function is an optional extended function.
 *  It has been provided to allow you to write a driver for memory
 *  that is not memory mapped.
 *
 *  Typically memory read operations will be direct memory mapped 
 *  operations however implementing a driver's \a crc function
 *  allows you to carry out a crc32 operation on non-memory mapped
 *  memory through the LIBMEM interface.
 */     
typedef uint32_t (*libmem_driver_crc32_fn_t)(libmem_driver_handle_t *h, const uint8_t *start, size_t size, uint32_t crc);

/*! \brief A function pointer to a function handling a libmem_foreach_driver call \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a function handling a libmem_foreach_driver call.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \return
 *    The LIBMEM status result. If any value other than LIBMEM_STATUS_SUCCESS 
 *    is returned from this function the \a libmem_foreach_driver function will terminate and return the response.
 *     
 */
typedef int (*libmem_foreach_driver_fn_t)(libmem_driver_handle_t *h);

/*! \brief A function pointer to a function implementing a paged write operation \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a function implementing a paged write operation.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param dest
 *    A pointer to the start address of the page to write to.
 * 
 *  \param src 
 *    A pointer to the address to copy the page data from.
 *
 *  \return
 *    The LIBMEM status result. If any value other than LIBMEM_STATUS_SUCCESS
 *    is returned from this function the \a libmem_driver_paged_write or
 *    \a libmem_driver_paged_write_fill functions will terminate and return 
 *    the response.
 */
typedef int (*libmem_driver_page_write_fn_t)(libmem_driver_handle_t *h, uint8_t *dest, const uint8_t *src);

/*! \brief A function pointer to a function handling a libmem_foreach_sector or libmem_foreach_sector_in_range call \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a function pointer to a function handling a libmem_foreach_sector or libmem_foreach_sector_in_range call.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param sector_info
 *    A pointer to the sector information.
 *
 *  \return
 *    The LIBMEM status result. If any value other than LIBMEM_STATUS_SUCCESS
 *    is returned from this function the \a libmem_foreach_sector or
 *    \a libmem_foreach_sector_in_range functions will terminate and return 
 *    the response.
 */
typedef int (*libmem_foreach_sector_fn_t)(libmem_driver_handle_t *h, libmem_sector_info_t *sector_info);

/*! \brief A pointer to a function returning the current timer tick count \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a pointer to a function returning the current timer tick count.
 *
 *  \return
 *    The current timer tick count.
 */
typedef uint32_t (*libmem_get_ticks_fn_t)(void);

/*! \brief A pointer to a function to be called each time LIBMEM iterates a busy loop \ingroup Function pointers \synopsis \description
 *
 *  \b \this is a pointer to a function to be called each time LIBMEM iterates a busy loop.
 *
 */
typedef void (*libmem_busy_handler_fn_t)(void);

/*! \brief Structure describing a geometry region \ingroup Data types \synopsis \description
 *
 *  \b \this describes a geometry region.
 *
 *  A geometry description can be made up of one or more geometry regions. A geometry
 *  region is a collection of equal-size sectors.
 *
 *  \table{1.5in,*}
 *  || Member             | Description
 *  || count              | The number of equal-sized sectors in the geometry region
 *  || size               | The size of the sector
 *  \endtable
 *
 */
struct _libmem_geometry_t
{
  unsigned int count;
  size_t       size;
};


/*! \brief Structure containing information about a specific FLASH chip \ingroup Data types \synopsis \description
 *
 *  \b \this is a structure containing information about a specific FLASH chip.
 *
 *  \table{1.5in,*}
 *  || Member                     | Description
 *  || write_timeout_ticks        | The maximum number of ticks it should take for a write operation to complete
 *  || multi_write_timeout_ticks  | The maximum number of ticks it should take for a multi-byte write operation to complete
 *  || erase_sector_timeout_ticks | The maximum number of ticks it should take for a sector erase operation to complete
 *  || erase_chip_timeout_ticks   | The maximum number of ticks it should take for a chip erase operation to complete
 *  || max_multi_program_bytes    | The maximum number of bytes that can be programmed in a multi-program operation
 *  || primary_cmdset             | The FLASH chip's primary CFI command set
 *  || width                      | The operating width of the FLASH chip in bytes
 *  || pairing                    | Non-zero if using a paired FLASH configuration
 *  \endtable
 *
 */
struct _libmem_flash_info_t
{
  uint32_t write_timeout_ticks;
  uint32_t multi_write_timeout_ticks;
  uint32_t erase_sector_timeout_ticks;
  uint32_t erase_chip_timeout_ticks;
  uint32_t max_multi_program_bytes;
  uint16_t primary_cmdset;
  uint8_t  width;
  uint8_t  pairing;
};


/*! \brief Structure describing a sector \ingroup Data types \synopsis \description
 *
 *  \b \this is a structure describing a sector.
 *
 *  \table{1.5in,*}
 *  || Member             | Description
 *  || number             | The sector number (sectors in a geometry are numbered in order from zero)
 *  || start              | The start address of the sector
 *  || size               | The size of the sector
 *  \endtable
 */
struct _libmem_sector_info_t
{
  int      number;
  uint8_t *start;
  size_t   size;
};


/*! \brief Structure containing pointers to a LIBMEM driver's functions \ingroup Data types \synopsis \description
 *
 *  \b \this is a structure containing pointers to a LIBMEM driver's functions.
 *
 *  \table{1.5in,*}
 *  || Member             | Description
 *  || write              | A pointer to a LIBMEM driver's write function
 *  || fill               | A pointer to a LIBMEM driver's fill function
 *  || erase              | A pointer to a LIBMEM driver's erase function
 *  || lock               | A pointer to a LIBMEM driver's lock function
 *  || unlock             | A pointer to a LIBMEM driver's unlock function
 *  || flush              | A pointer to a LIBMEM driver's flush function
 *  \endtable
 */
struct _libmem_driver_functions_t
{
  libmem_driver_write_fn_t   write;
  libmem_driver_fill_fn_t    fill;
  libmem_driver_erase_fn_t   erase;
  libmem_driver_lock_fn_t    lock;
  libmem_driver_unlock_fn_t  unlock;
  libmem_driver_flush_fn_t   flush;
};


/*! \brief Structure containing pointers to a LIBMEM driver's extended functions \ingroup Data types \synopsis \description
 *
 *  \b \this is a structure containing pointers to a LIBMEM driver's extended functions.
 *
 *  \table{1.5in,*}
 *  || Member             | Description
 *  || inrange            | A pointer to a LIBMEM driver's \b inrange function
 *  || read               | A pointer to a LIBMEM driver's \b read function
 *  || crc32              | A pointer to a LIBMEM driver's \b crc32 function
 *  \endtable
 */
struct _libmem_ext_driver_functions_t
{
  libmem_driver_inrange_fn_t inrange;
  libmem_driver_read_fn_t    read;
  libmem_driver_crc32_fn_t   crc32;
};


/*! \brief LIBMEM driver handle structure \ingroup Data types \synopsis \description
 *
 *  \b \this contains information on a particular driver's entry point
 *  functions, the address range the driver is responsible for and optionally
 *  the geometry and device specific information of the memory.
 *
 *  \table{1.5in,*}
 *  || Member               | Description
 *  || next                 | The next LIBMEM driver in list of drivers
 *  || driver_functions     | A pointer to the structure describing the LIBMEM driver's functions
 *  || ext_driver_functions | A pointer to the structure describing the LIBMEM driver's extended functions
 *  || start                | A pointer to the start of the address range handled by the LIBMEM driver
 *  || size                 | The size of address range handled by the LIBMEM driver in bytes
 *  || geometry             | A pointer to a null-terminated geometry description list
 *  || flash_info           | A pointer to the FLASH information structure
 *  || driver_data          | A data word available for storing driver information
 *  || user_data            | A data word available for storing user information
 *  \endtable
 */
struct _libmem_driver_handle_t
{
  libmem_driver_handle_t              *next;
  const libmem_driver_functions_t     *driver_functions;
  const libmem_ext_driver_functions_t *ext_driver_functions;
  uint8_t                             *start;
  size_t                               size;
  const libmem_geometry_t             *geometry;
  const libmem_flash_info_t           *flash_info;
  uint32_t                             driver_data;
  uint32_t                             user_data;
};


/*! \brief Paged write control block \ingroup Data types \synopsis \description
 *
 *  \b \this is a structure describing the paged write helper functions control bock.
 *
 */
struct _libmem_driver_paged_write_ctrlblk_t
{
  uint8_t                      *page_buffer;
  size_t                        page_size;
  uint32_t                      page_mask;
  libmem_driver_page_write_fn_t page_write_fn;
  uint32_t                      page_write_src_alignment;
  uint32_t                      options;
  uint8_t                      *current_page;
};


/*! \brief Pointer to the first registered LIBMEM driver \ingroup Static data \synopsis \description
 *
 *  \b \this is a pointer to the first registered LIBMEM driver.
 */
extern libmem_driver_handle_t *libmem_drivers;

/*! \brief Pointer to a function that returns the current timer tick count \ingroup Static data \synopsis \description
 *
 *  \b \this is a pointer to a function that returns the current timer tick count.
 */
extern libmem_get_ticks_fn_t libmem_get_ticks_fn;

/*! \brief Pointer to a function that should be called each time LIBMEM iterates a busy loop \ingroup Static data \synopsis \description
 *
 *  \b \this is a pointer to a function that should be called each time LIBMEM iterates a busy loop.
 */
extern libmem_busy_handler_fn_t libmem_busy_handler_fn;

/*! \brief How fast the tick increments \ingroup Static data \synopsis \description
 *
 *  \b \this is the amount the value returned by the \a libmem_get_ticks_fn function
 *  increments each second.
 */
extern uint32_t libmem_ticks_per_second;

/*! \brief Enable LIBMEM operation timeouts \ingroup Functions \synopsis \description
 *
 *  \b \this enables LIBMEM operation timeouts.
 *
 *  \param get_ticks_fn
 *    A pointer to a function that returns an incrementing tick count.
 *
 *  \param ticks_per_second
 *    The amount the value returned by the \a get_ticks_fn increments 
 *    per second.
 *
 *  In order for operations to timeout the LIBMEM library needs
 *  a function that can supply a timer tick count and also needs to know
 *  the frequency the timer increments.
 *
 *  This function should be called prior to registering LIBMEM drivers
 *  as the \a ticks_per_second parameter can be used to pre-compute
 *  timeout periods when the driver is registered.
 *
 */
void libmem_enable_timeouts(libmem_get_ticks_fn_t get_ticks_fn, uint32_t ticks_per_second);

/*! \brief Specify busy loop function \ingroup Functions \synopsis \description
 *
 *  \b \this specifies a handler function that should be called each time LIBMEM
 *  iterates a busy loop.
 *
 *  \param busy_handler_fn
 *    A pointer to a busy handler function.
 *
 *  \return
 *    A pointer to the existing busy handler or NULL if there isn't one.
 *
 *  This function allows a user defined function to be called each time LIBMEM
 *  iterates a busy loop. The typical use of this is to keep watchdogs
 *  alive while LIBMEM is carrying out blocking operations.
 */
libmem_busy_handler_fn_t libmem_set_busy_handler(libmem_busy_handler_fn_t busy_handler_fn);

/*! \brief Write a block of data using a LIBMEM driver \ingroup Functions \synopsis \description
 *
 *  \b \this writes a block of data using a LIBMEM driver.
 *
 *  \param dest
 *    A pointer to the address to write the block of data.
 *
 *  \param src
 *    A pointer to the address to copy the block of data from.
 *
 *  \param size
 *    The size of the block of data to copy in bytes.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function locates the LIBMEM driver for the address pointed to
 *  by \a start and then calls the LIBMEM driver's \a write function.
 *
 *  Note that the address range being written to cannot span multiple
 *  LIBMEM drivers.
 *
 *  Example:
 *  \code
 *    const unsigned char buffer[8] = { 1, 2, 3, 4, 5, 6, 7, 8 };
 *    int res;
 *
 *    res = libmem_write((uint8_t *)0x10000000, buffer, sizeof(buffer));
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_write : success\n");
 *    else
 *      printf("libmem_write : failed (%d)\n", res);
 *  \endcode
 */
int libmem_write(uint8_t *dest, const uint8_t *src, size_t size);

/*! \brief Fill memory with a specific data value using a LIBMEM driver \ingroup Functions \synopsis \description
 *
 *  \b \this fills memory with a specific data value using a LIBMEM driver.
 *
 *  \param dest
 *    A pointer to the address to write the data.
 *
 *  \param c
 *    The data value to fill the memory with.
 *
 *  \param size
 *    The number of bytes to write.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function locates the LIBMEM driver for the address pointed to by \a dest
 *  and then calls the LIBMEM driver's \a fill function.
 *
 *  Note that the address range being written to cannot span multiple
 *  LIBMEM drivers.
 *
 *  Example:
 *  \code
 *    int res;
 *
 *    res = libmem_fill((uint8_t *)0x10000000, 0xCC, 64);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_fill : success\n");
 *    else
 *      printf("libmem_fill : failed (%d)\n", res);
 *  \endcode
 */
int libmem_fill(uint8_t *dest, uint8_t c, size_t size);

/*! \brief Read a block of data using a LIBMEM driver \ingroup Functions \synopsis \description
 *
 *  \b \this reads a block of data using a LIBMEM driver.
 *
 *  \param dest
 *    A pointer to the address to write the block of data.
 *
 *  \param src
 *    A pointer to the address to copy the block of data from.
 *
 *  \param size 
 *    The size of the block of data to copy in bytes.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function locates the LIBMEM driver for the address pointed to
 *  by \a src and then calls the LIBMEM driver's \a read extended function if
 *  it has been implemented. If the \a read function has not been implemented
 *  then the memory will be read directly using memcpy.
 *  The intention for this function is to allow you to use the LIBMEM
 *  library for memory that doesn't appear on the address bus by providing
 *  a virtual address range for the device.
 *
 *  Note that if the LIBMEM driver's read function is used, the address range 
 *  being read cannot span multiple LIBMEM drivers.
 *
 *  Example:
 *  \code
 *    uint8_t buffer[64];
 *    int res;
 *
 *    res = libmem_read(buffer, (uint8_t *)0x10000000, sizeof(buffer));
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_read : success\n");
 *    else
 *      printf("libmem_read : failed (%d)\n", res);
 *  \endcode
 */
int libmem_read(uint8_t *dest, const uint8_t *src, size_t size);

/*! \brief Erase a block of memory using a LIBMEM driver \ingroup Functions \synopsis \description
 *
 *  \b \this erases a block of memory using a LIBMEM driver.
 *
 *  \param start 
 *    A pointer to the start address of the memory range to erase.
 *
 *  \param size 
 *    The size of the memory range to erase in bytes.
 *
 *  \param erase_start 
 *    A pointer to a location in memory to store a pointer to the start of
 *    the memory range that has actually been erased or NULL if not required.
 *
 *  \param erase_size
 *    A pointer to a location in memory to store the size in bytes of the
 *    memory range that has actually been erased or NULL if not required.
 *
 *  \return 
 *    The LIBMEM status result.
 *
 *  This function locates the LIBMEM driver for the address pointed to 
 *  by \a start and then calls the LIBMEM driver's \a erase function.
 *
 *  Note that the address range being erased cannot span multiple LIBMEM
 *  drivers.
 *
 *  Example:
 *  \code
 *    uint8_t *erase_start;
 *    size_t erase_size;
 *    int res;
 *
 *    res = libmem_erase((uint8_t *)0x10000000, 1024, &erase_start, &erase_size);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_erase : success (erased %08X - 0x%08X)\n", erase_start, erase_start + erase_size - 1);
 *    else
 *      printf("libmem_erase : failed (%d)\n", res);
 *  \endcode
 */
int libmem_erase(uint8_t *start, size_t size, uint8_t **erase_start, size_t *erase_size);

/*! \brief Erase all memory using LIBMEM drivers \ingroup Functions \synopsis \description
 *
 *  \b \this erases all memory using LIBMEM drivers.
 *
 *  \return 
 *    The LIBMEM status result.
 *
 *  This function iterates through all registered LIBMEM drivers calling each
 *  driver's \a erase function specifying the drivers entire memory range as its
 *  parameters.
 *
 *  The function will terminate if any of the driver's \a erase functions return
 *  a result other than \a LIBMEM_STATUS_SUCCESS.
 *
 *  Example:
 *  \code
 *    int res;
 *
 *    res = libmem_erase_all();
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_erase_all : success\n");
 *    else
 *      printf("libmem_erase_all : failed (%d)\n", res);
 *  \endcode
 */
int libmem_erase_all(void);

/*! \brief Lock a block of memory using a LIBMEM driver \ingroup Functions \synopsis \description
 *
 *  \b \this locks a block of memory using a LIBMEM driver.
 *
 *  \param start 
 *    A pointer to the start address of the memory range to lock.
 *
 *  \param size 
 *    The size of the memory range to lock in bytes.
 *
 *  \return 
 *    The LIBMEM status result.
 *
 *  This function locates the LIBMEM driver for the address pointed to 
 *  by \a start and then calls the LIBMEM driver's \a lock function.
 *
 *  Example:
 *  \code
 *    int res;
 *
 *    res = libmem_lock((uint8_t *)0x10000000, 1024);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_lock : success\n");
 *    else
 *      printf("libmem_lock : failed (%d)\n", res);
 *  \endcode
 */
int libmem_lock(uint8_t *start, size_t size);

/*! \brief Lock all memory using LIBMEM drivers \ingroup Functions \synopsis \description
 *
 *  \b \this locks all memory using LIBMEM drivers.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function iterates through all registered LIBMEM drivers calling each
 *  driver's \a lock function specifying the drivers entire memory range as its
 *  parameters.
 *
 *  The function will terminate if any of the driver's \a lock functions return
 *  a result other than \a LIBMEM_STATUS_SUCCESS.
 *
 *  Example:
 *  \code
 *    int res;
 *
 *    res = libmem_lock_all();
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_lock_all : success\n");
 *    else
 *      printf("libmem_lock_all : failed (%d)\n", res);
 *  \endcode
 */
int libmem_lock_all(void);

/*! \brief Unlock a block of memory using a LIBMEM driver \ingroup Functions \synopsis \description
 *
 *  \b \this unlocks a block of memory using a LIBMEM driver.
 *
 *  \param start
 *    A pointer to the start address of the memory range to unlock.
 *
 *  \param size
 *    The size of the memory range to unlock in bytes.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function locates the LIBMEM driver for the address pointed to
 *  by \a start and then calls the LIBMEM driver's \a unlock function.
 *
 *  Example:
 *  \code
 *    int res;
 *
 *    res = libmem_unlock((uint8_t *)0x10000000, 1024);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_unlock : success\n");
 *    else
 *      printf("libmem_unlock : failed (%d)\n", res);
 *  \endcode
 */
int libmem_unlock(uint8_t *start, size_t size);

/*! \brief Unlock all memory using LIBMEM drivers \ingroup Functions \synopsis \description
 *
 *  \b \this unlocks all memory using LIBMEM drivers.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function iterates through all registered LIBMEM drivers calling each
 *  driver's \a unlock function specifying the drivers entire memory range as its
 *  parameters.
 *
 *  The function will terminate if any of the driver's \a unlock functions return
 *  a result other than \a LIBMEM_STATUS_SUCCESS.
 *
 *  Example:
 *  \code
 *    int res;
 *
 *    res = libmem_unlock_all();
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_unlock_all : success\n");
 *    else
 *      printf("libmem_unlock_all : failed (%d)\n", res);
 *  \endcode
 */
int libmem_unlock_all(void);

/*! \brief Flush any outstanding memory operations and return memory to read mode if applicable \ingroup Functions \synopsis \description
 *
 *  \b \this flushes any outstanding memory operations and returns memory to read mode if applicable.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  LIBMEM drivers do not necessarily carry out operations immediately or they may 
 *  leave the memory in an unreadable state for performance reasons. You should
 *  call \a libmem_flush once you have finished carrying out memory operations in order
 *  to complete all outstanding transactions and return the memory to a readable
 *  state.
 *
 *  Example:
 *  \code
 *    int res;
 *
 *    res = libmem_flush();
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_flush : success\n");
 *    else
 *      printf("libmem_flush : failed (%d)\n", res);
 *  \endcode
 */
int libmem_flush(void);

/*! \brief Compute CRC-32 checksum \ingroup Functions \synopsis \description
 *
 *  \b \this computes the CRC-32 checksum of an address range using a LIBMEM driver.
 *
 *  \param start
 *    A pointer to the start of the address range.
 *
 *  \param size
 *    The size of the address range in bytes.
 *
 *  \param crc
 *    The initial CRC-32 value.
 *
 *  \return
 *    The computed CRC-32 value.
 *
 *  This function locates the LIBMEM driver for the address pointed to
 *  by \a start, then calls the LIBMEM driver's \a crc32 extended 
 *  function if it has one and returns the result. If the driver
 *  hasn't implemented the \a crc32 extended function then the
 *  \a libmem_crc32_direct function is called which accesses the memory directly.
 *  The intention for this function is to allow you to use the LIBMEM
 *  library for memory that doesn't appear on the address bus by providing
 *  a virtual address range for the device.
 *
 *  Example:
 *  \code
 *    uint32_t crc = 0xFFFFFFFF;
 *
 *    crc = libmem_crc32((uint8_t *)0x10000000, 1024, crc);
 *  \endcode
 *   
 */
uint32_t libmem_crc32(const uint8_t *start, size_t size, uint32_t crc);

/*! \brief Register a LIBMEM driver instance \ingroup Functions \synopsis \description
 *
 *  \b \this registers a LIBMEM driver instance.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver being registered.
 *
 *  \param start
 *    A Pointer to the start of the address range handled by the LIBMEM driver.
 *
 *  \param size
 *    The size of the address range handled by the LIBMEM driver in bytes.
 *
 *  \param geometry
 *    A pointer to a null-terminated geometry description list or NULL if not required.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure or NULL if not required.
 *
 *  \param driver_functions
 *    A pointer to the structure describing the LIBMEM driver's functions.
 *
 *  \param ext_driver_functions
 *    A pointer to the structure describing the LIBMEM driver's extended functions, or NULL if not required.
 *
 *  This function adds a LIBMEM driver to the list of LIBMEM drivers currently
 *  installed. This function is not normally called directly by an application,
 *  it is typically called by a LIBMEM driver's own register function.
 */
void libmem_register_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info, const libmem_driver_functions_t *driver_functions, const libmem_ext_driver_functions_t *ext_driver_functions);

/*! \brief Iterate through all sectors of a driver \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function for iterating through all sectors handled by a LIBMEM driver.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param fn
 *    The function to call for each sector.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function iterates through all the sectors handled by a single LIBMEM
 *  driver and calls a \a libmem_foreach_sector_fn_t function for each.
 *  If any of the calls return a response other than LIBMEM_STATUS_SUCCESS this function
 *  will terminate and return the response.
 *
 */
int libmem_foreach_sector(libmem_driver_handle_t *h, libmem_foreach_sector_fn_t fn);

/*! \brief  Iterate through subset of sectors of a driver \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function for iterating through all sectors handled by a driver that are within a specific address range.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param range_start
 *    A pointer to the start of the address range.
 *
 *  \param range_size
 *    The size of the address range in bytes.
 *
 *  \param fn
 *    The function to call for each sector.
 *
 *  \param actual_range_start
 *    A pointer to the start of the first sector that is within the address range.
 *
 *  \param actual_range_size
 *    The combined size of all the sectors that are within the address range.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function iterates through all the sectors handled by a single LIBMEM
 *  driver and calls a \a libmem_foreach_sector_fn_t function for each if it
 *  is within the specified address range.
 *  If any of the calls return a response other than LIBMEM_STATUS_SUCCESS this function 
 *  will terminate and return the response.
 */
int libmem_foreach_sector_in_range(libmem_driver_handle_t *h, uint8_t *range_start, size_t range_size, libmem_foreach_sector_fn_t fn, uint8_t **actual_range_start, size_t *actual_range_size);

/*! \brief A helper function for iterating through all sectors in a specified geometry that are within a specific address range \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function for iterating through all sectors in a specified geometry that are within a specific address range.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param geometry
 *    A pointer to the NULL terminated geometry description.
 *
 *  \param range_start
 *    A pointer to the start of the address range.
 *
 *  \param range_size
 *    The size of the address range in bytes.
 *
 *  \param fn
 *    The function to call for each sector.
 *
 *  \param actual_range_start
 *    A pointer to the start of the first sector that is within the address range.
 *
 *  \param actual_range_size
 *    The combined size of all the sectors that are within the address range.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function iterates through all the sectors in the specified geometry 
 *  and calls a \a libmem_foreach_sector_fn_t function for each if it
 *  is within the specified address range.
 *  If any of the calls return a response other than LIBMEM_STATUS_SUCCESS this function 
 *  will terminate and return the response.
 *  This function is essentially the same as \a libmem_foreach_sector_in_range except
 *  it allows a different geometry to be specified to that associated with the driver.
 */
int libmem_foreach_sector_in_range_ex(libmem_driver_handle_t *h, const libmem_geometry_t *geometry, uint8_t *range_start, size_t range_size, libmem_foreach_sector_fn_t fn, uint8_t **actual_range_start, size_t *actual_range_size);

/*! \brief A helper function that returns the sector number of an address within a specified geometry \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function that returns the sector number of an address within a specified geometry.
 *
 *  \param start
 *    A pointer to the start address of the geometry desribed by \a geometry.
 *
 *  \param geometry
 *    A pointer to the NULL terminated geometry description.
 *
 *  \param p
 *    A pointer to the address to determine the sector number of.
 *
 *  \return
 *    The sector number or -1 if the address is not located within the described geometry.
 */
int libmem_get_sector_number(uint8_t *start, const libmem_geometry_t *geometry, const uint8_t *p);

/*! \brief A helper function that returns the number of sectors described by a geometry description \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function that returns the number of sectors described by a geometry description.
 *
 *  \param geometry
 *    A pointer to the NULL terminated geometry description.
 *
 *  \return
 *    The number of sectors.
 */
int libmem_get_number_of_sectors(const libmem_geometry_t *geometry);

/*! \brief A helper function that returns the number of geometry regions described by a geometry description \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function that returns the number of geometry regions described by a geometry description.
 *
 *  \param geometry
 *    A pointer to the NULL terminated geometry description.
 *
 *  \return
 *    The number of geometry regions.
 */
int libmem_get_number_of_regions(const libmem_geometry_t *geometry);

/*! \brief A helper function that returns the sector information for an address within a specified geometry \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function that returns the sector information for an address within a specified geometry.
 *
 *  \param start
 *    A pointer to the start address of the geometry desribed by \a geometry.
 *
 *  \param geometry
 *    A pointer to the NULL terminated geometry description.
 *
 *  \param p
 *    A pointer to the address to determine the sector information of.
 *
 *  \param info
 *    A pointer to the \a libmem_sector_info_t structure to write the sector information to.
 *
 *  \return
 *    The LIBMEM status result.
 */
int libmem_get_sector_info(uint8_t *start, const libmem_geometry_t *geometry, const uint8_t *p, libmem_sector_info_t *info);

/*! \brief A helper function that returns the sector size for an address within a specified geometry \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function that returns the sector size for an address within a specified geometry.
 *
 *  \param start
 *    A pointer to the start address of the geometry desribed by \a geometry.
 *
 *  \param geometry
 *    A pointer to the NULL terminated geometry description.
 *
 *  \param p
 *    A pointer to the address to determine the sector information of.
 *
 *  \return
 *    The size of the sector or 0 if the sector cannot be found.
 */
size_t libmem_get_sector_size(uint8_t *start, const libmem_geometry_t *geometry, const uint8_t *p);

/*! \brief A helper function that returns the size of the address range described by a geometry description \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function that returns the size of the address range described by a geometry description.
 *
 *  \param geometry
 *    A pointer to the NULL terminated geometry description.
 *
 *  \return
 *    The size of the address range described the by geometry description in bytes.
 */
size_t libmem_get_geometry_size(const libmem_geometry_t *geometry);

/*! \brief Helper function that returns the current timer tick count \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function that returns the current timer tick count.
 *
 *  \return
 *    The current timer tick count as returned by the \a libmem_get_ticks_fn
 *    function or 0 if this function has not been defined.
 */
uint32_t libmem_get_ticks(void);

/*! \brief Compute CRC-32 checksum of an address range \ingroup Functions \synopsis \description
 *
 *  \b \this computes the CRC-32 checksum of an address range.
 *
 *  \param start
 *    A pointer to the start of the address range.
 *
 *  \param size
 *    The size of the address range in bytes.
 *
 *  \param crc
 *    The initial CRC-32 value.
 *
 *  \return
 *    The computed CRC-32 value.
 *
 *  This function computes a CRC-32 checksum on a block of data using the
 *  standard CRC-32 polynomial (0x04C11DB7). Note that this implementation
 *  doesn't reflect the input or the output and the result is not inverted.
 *
 *  Example:
 *  \code
 *    uint32_t crc = 0xFFFFFFFF;
 *
 *    crc = libmem_crc32_direct((uint8_t *)0x10000000, 1024, crc);
 *  \endcode
 */
uint32_t libmem_crc32_direct(const uint8_t *start, size_t size, uint32_t crc);

/*! \brief A driver helper function that initializes the paged write control bock \ingroup Driver helper functions \synopsis \description
 *
 *  \b \this is a driver helper function that initializes the paged write control bock.
 *
 *  \param paged_write_ctrlblk
 *    A pointer to the paged write control block.
 *
 *  \param page_buffer
 *    A pointer to the page buffer to use for paged write operations.
 *
 *  \param page_size
 *    The page size, this value must be equal to the size of the buffer pointed to by \a page_buffer.
 *
 *  \param page_write_fn
 *    A pointer to a function that carries out the page write operation.
 *
 *  \param page_write_src_alignment
 *    The byte alignment of source data required by the page write function when
 *    bypassing the page buffer. Set this to zero if the write function only
 *    supports writes directly from the page buffer.
 *
 *  \param options
 *    Paged write configuration options. 
 *
 *  \return
 *    The LIBMEM status result.
 *
 */
int libmem_driver_paged_write_init(libmem_driver_paged_write_ctrlblk_t *paged_write_ctrlblk, uint8_t *page_buffer, size_t page_size, libmem_driver_page_write_fn_t page_write_fn, uint32_t page_write_src_alignment, uint32_t options);

/*! \brief A driver helper function that implements a paged write operation \ingroup Driver helper functions \synopsis \description
 *
 *  \b \this is a driver helper function that implements a paged write operation.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param dest
 *    A pointer to the address to write the block of data.
 *
 *  \param src
 *    A pointer to the address to copy the block of data from.
 *
 *  \param size
 *    The size of the block of data to copy in bytes.
 *
 *  \param paged_write_ctrlblk
 *    A pointer to the paged write control block.
 *
 *  \return
 *    The LIBMEM status result.
 */
int libmem_driver_paged_write(libmem_driver_handle_t *h, uint8_t *dest, const uint8_t *src, size_t size, libmem_driver_paged_write_ctrlblk_t *paged_write_ctrlblk);

/*! \brief A driver helper function that implements a paged write fill operation \ingroup Driver helper functions \synopsis \description
 *
 *  \b \this is a driver helper function that implements a paged write fill operation.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param dest
 *    A pointer to the address to write the block of data.
 *
 *  \param c
 *    The data value to fill the memory with.
 *
 *  \param size
 *    The number of bytes to write.
 *
 *  \param paged_write_ctrlblk
 *    A pointer to the paged write control block.
 *
 *  \return
 *    The LIBMEM status result.
 */
int libmem_driver_paged_write_fill(libmem_driver_handle_t *h, uint8_t *dest, uint8_t c, size_t size, libmem_driver_paged_write_ctrlblk_t *paged_write_ctrlblk);

/*! \brief A driver helper function that implements a paged write flush operation \ingroup Driver helper functions \synopsis \description
 *
 *  \b \this is a driver helper function that implements a paged write flush operation.
 *
 *  \param h
 *    A pointer to the handle of the LIBMEM driver.
 *
 *  \param paged_write_ctrlblk
 *    A pointer to the paged write control block.
 *
 *  \return
 *    The LIBMEM status result.
 */
int libmem_driver_paged_write_flush(libmem_driver_handle_t *h, libmem_driver_paged_write_ctrlblk_t *paged_write_ctrlblk);

/*! \def LIBMEM_LIGHT
 *
 *  This definition should be defined when using the "light" version of LIBMEM.
 *
 *  When it is defined only the last registered driver will be used, no driver
 *  searching or address range checks will be carried out.
 */

#ifdef LIBMEM_LIGHT

#define libmem_get_driver(p1) libmem_drivers
#define libmem_foreach_driver(p1) p1(libmem_drivers)
#define LIBMEM_INLINE

#else /* LIBMEM_LIGHT */

/*! \brief Look up driver for address \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function that returns the handle of a LIBMEM driver
 *  that is responsible for a specific memory location.
 *    
 *  \param p
 *    A pointer to the memory location to get the driver for.
 * 
 *  \return The LIBMEM driver handle or NULL if no driver could be found.
 * 
 */
libmem_driver_handle_t *libmem_get_driver(const uint8_t *p);

/*! \brief Iterate through all drivers \ingroup Functions \synopsis \description
 *
 *  \b \this iterates through all the registered LIBMEM drivers and calls
 *  \a fn for each. If any of the calls return a response other than
 *  \tt LIBMEM_STATUS_SUCCESS this function will terminate
 *  and return that response.
 *
 *  \param fn
 *    The function to call for each driver.
 *
 *  \return
 *    The LIBMEM status result.
 *
 */
int libmem_foreach_driver(libmem_foreach_driver_fn_t fn);

/*! \brief Inline definition \ingroup Configuration macros \synopsis \description
 *
 *  This definition contains the \tt inline keyword if function inlining should be used.
 *  This definition is empty for the LIBMEM_LIGHT build.
 */
#define LIBMEM_INLINE inline

#endif /* LIBMEM_LIGHT */

/*! \brief A helper function that locates the driver for a specific address and then returns the sector size for that address using the driver's geometry \ingroup Functions \synopsis \description
 *
 *  \b \this is a helper function that locates the driver for a specific address and then returns the sector size for that address using the driver's geometry.
 *
 *  \param p
 *    A pointer to the address to determine the sector information of.
 *
 *  \return
 *    The size of the sector or 0 if the sector cannot be found.
 */
size_t libmem_get_driver_sector_size(const uint8_t *p);

/*! \brief Return a FLASH memory device's common flash interface (CFI) information \ingroup Functions \synopsis \description
 *
 *  \b \this returns a FLASH memory device's common flash interface (CFI) information.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    A pointer to the memory location to store the size (in bytes) of the FLASH memory.
 *
 *  \param geometry
 *    A pointer to the memory location to store the geometry description or NULL if not
 *    required.
 *
 *  \param max_geometry_regions
 *    The maximum number of geometry regions that can be stored at the memory pointed
 *    to by \a geometry. The geometry description is NULL terminated so 
 *    \a max_geometry_regions must be at least two regions in size in order to store one
 *    geometry region and one terminator entry.
 *
 *  \param flash_info
 *    A pointer to the memory location to store the remaining FLASH information, or NULL
 *    if not required.
 *
 *  \return
 *    The LIBMEM status result.
 * 
 *  This function attempts to return the FLASH device's type, size, geometry and other
 *  FLASH information from only a pointer to the first address the FLASH memory is
 *  located at. It uses the common flash memory interface (CFI) to obtain this
 *  information and therefore only works on FLASH devices that fully support this
 *  interface.
 *
 *  Example:
 *  \code
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_flash_info_t flash1_info;
 *    const int flash1_max_geometry_regions = 4;
 *    libmem_geometry_t flash1_geometry[flash1_max_geometry_regions];
 *    size_t flash1_size;
 *    int res;
 *
 *    res = libmem_cfi_get_info(flash1_start,
 *                              &flash1_size,
 *                              flash1_geometry,
 *                              flash1_max_geometry_regions,
 *                              &flash1_info);
 * 
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_cfi_get_info : success\n");
 *    else
 *      printf("libmem_cfi_get_info : failed (%d)\n", res);
 *  \endcode
 */
int libmem_cfi_get_info(uint8_t *start, size_t *size, libmem_geometry_t *geometry, int max_geometry_regions, libmem_flash_info_t *flash_info);

/*! \brief Register a FLASH driver based on detected CFI information \ingroup Generic FLASH drivers \synopsis \description
 *
 *  \b \this registers a FLASH driver based on detected CFI information.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param geometry
 *    A pointer to the memory location to store the geometry description.
 *
 *  \param max_geometry_regions
 *    The maximum number of geometry regions that can be stored at the memory pointed
 *    to by \a geometry. The geometry description is NULL terminated so 
 *    \a max_geometry_regions must be at least two regions in size in order to store one
 *    geometry region and one terminator entry.
 *
 *  \param flash_info
 *    A pointer to the memory location to store the remaining FLASH information.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function calls \a libmem_cfi_get_info to detect the type and geometry of the
 *  the FLASH pointed to by \a start and then, if the FLASH memory is supported, 
 *  registers an appropriate LIBMEM driver for the FLASH.
 *
 *  Use of this function requires all supported CFI LIBMEM drivers to be linked in,
 *  therefore if memory is at a premium you should register only the LIBMEM
 *  FLASH driver you require instead of using this function.
 *
 *  Example:
 *  \code
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_flash_info_t flash1_info;
 *    const int flash1_max_geometry_regions = 4;
 *    libmem_geometry_t flash1_geometry[flash1_max_geometry_regions];
 *    libmem_driver_handle_t flash1_handle;
 *    int res;
 *
 *    res = libmem_register_cfi_driver(&flash1_handle,
 *                                     flash1_start,
 *                                     flash1_geometry,
 *                                     flash1_max_geometry_regions,
 *                                     &flash1_info);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_cfi_driver : success\n");
 *    else
 *      printf("libmem_register_cfi_driver : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_cfi_driver(libmem_driver_handle_t *h, uint8_t *start, libmem_geometry_t *geometry, int max_geometry_regions, libmem_flash_info_t *flash_info);

/*! \brief Register an 8-bit CFI command set 1 (Intel Extended) LIBMEM driver \ingroup Generic FLASH drivers \synopsis \description
 *
 *  \b \this registers an 8-bit CFI command set 1 (Intel Extended) LIBMEM driver.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the FLASH memory.
 *
 *  \param geometry
 *    A NULL terminated description of the FLASH's geometry.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure or NULL if not required.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_geometry_t flash1_geometry[] =
 *      {
 *        { 8,  0x00002000 }, // 8 x 8KB sectors
 *        { 31, 0x00010000 }, // 31 x 64KB sectors
 *        { 0, 0 },           // NULL terminator
 *      };
 *    int res;
 *
 *    res = libmem_register_cfi_0001_8_driver(&flash1_handle,
 *                                            flash1_start,
 *                                            libmem_get_geometry_size(flash1_geometry),
 *                                            flash1_geometry,
 *                                            0);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_cfi_0001_8_driver : success\n");
 *    else
 *      printf("libmem_register_cfi_0001_8_driver : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_cfi_0001_8_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info);

/*! \brief Register a 16-bit CFI command set 1 (Intel Extended) LIBMEM driver \ingroup Generic FLASH drivers \synopsis \description
 *
 *  \b \this registers a 16-bit CFI command set 1 (Intel Extended) LIBMEM driver.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the FLASH memory.
 *
 *  \param geometry
 *    A NULL terminated description of the FLASH's geometry.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure or NULL if not required.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_geometry_t flash1_geometry[] =
 *      {
 *        { 8,  0x00002000 }, // 8 x 8KB sectors
 *        { 31, 0x00010000 }, // 31 x 64KB sectors
 *        { 0, 0 },           // NULL terminator
 *      };
 *    int res;
 *
 *    res = libmem_register_cfi_0001_16_driver(&flash1_handle,
 *                                             flash1_start,
 *                                             libmem_get_geometry_size(flash1_geometry),
 *                                             flash1_geometry,
 *                                             0);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_cfi_0001_16_driver : success\n");
 *    else
 *      printf("libmem_register_cfi_0001_16_driver : failed (%d)\n", res);
 *  \endcode
 */           
int libmem_register_cfi_0001_16_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info);

/*! \brief Register an 8 bit CFI command set 2 (AMD Standard) LIBMEM driver \ingroup Generic FLASH drivers \synopsis \description
 *
 *  \b \this registers an 8 bit CFI command set 2 (AMD Standard) LIBMEM driver.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the FLASH memory.
 *
 *  \param geometry
 *    A NULL terminated description of the FLASH's geometry.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure or NULL if not required.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_geometry_t flash1_geometry[] =
 *      {
 *        { 8,  0x00002000 }, // 8 x 8KB sectors
 *        { 31, 0x00010000 }, // 31 x 64KB sectors
 *        { 0, 0 },           // NULL terminator
 *      };
 *    int res;
 *
 *    res = libmem_register_cfi_0002_8_driver(&flash1_handle,
 *                                             flash1_start,
 *                                             libmem_get_geometry_size(flash1_geometry),
 *                                             flash1_geometry,
 *                                             0);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_cfi_0002_8_driver : success\n");
 *    else
 *      printf("libmem_register_cfi_0002_8_driver : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_cfi_0002_8_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info);

/*! \brief Register a 16-bit CFI command set 2 (AMD Standard) LIBMEM driver \ingroup Generic FLASH drivers \synopsis \description
 *
 *  \b \this registers a 16-bit CFI command set 2 (AMD Standard) LIBMEM driver.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the FLASH memory.
 *
 *  \param geometry
 *    A NULL terminated description of the FLASH's geometry.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure or NULL if not required.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_geometry_t flash1_geometry[] =
 *      {
 *        { 8,  0x00002000 }, // 8 x 8KB sectors
 *        { 31, 0x00010000 }, // 31 x 64KB sectors
 *        { 0, 0 },           // NULL terminator
 *      };
 *    int res;
 *
 *    res = libmem_register_cfi_0002_16_driver(&flash1_handle,
 *                                             flash1_start,
 *                                             libmem_get_geometry_size(flash1_geometry),
 *                                             flash1_geometry,
 *                                             0);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_cfi_0002_16_driver : success\n");
 *    else
 *      printf("libmem_register_cfi_0002_16_driver : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_cfi_0002_16_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info);

/*! \brief Register an 8-bit CFI command set 3 (Intel Standard) LIBMEM driver \ingroup Generic FLASH drivers \synopsis \description
 *
 *  \b \this registers an 8-bit CFI command set 3 (Intel Standard) LIBMEM driver.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the FLASH memory.
 *
 *  \param geometry
 *    A NULL terminated description of the FLASH's geometry.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure or NULL if not required.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_geometry_t flash1_geometry[] =
 *      {
 *        { 8,  0x00002000 }, // 8 x 8KB sectors
 *        { 31, 0x00010000 }, // 31 x 64KB sectors
 *        { 0, 0 },           // NULL terminator
 *      };
 *    int res;
 *
 *    res = libmem_register_cfi_0003_8_driver(&flash1_handle,
 *                                            flash1_start,
 *                                            libmem_get_geometry_size(flash1_geometry),
 *                                            flash1_geometry,
 *                                            0);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_cfi_0003_8_driver : success\n");
 *    else
 *      printf("libmem_register_cfi_0003_8_driver : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_cfi_0003_8_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info);

/*! \brief Register a 16-bit CFI command set 3 (Intel Standard) LIBMEM driver \ingroup Generic FLASH drivers \synopsis \description
 *
 *  \b \this registers a 16-bit CFI command set 3 (Intel Standard) LIBMEM driver.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the FLASH memory.
 *
 *  \param geometry
 *    A NULL terminated description of the FLASH's geometry.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure or NULL if not required.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_geometry_t flash1_geometry[] =
 *      {
 *        { 8,  0x00002000 }, // 8 x 8KB sectors
 *        { 31, 0x00010000 }, // 31 x 64KB sectors
 *        { 0, 0 },           // NULL terminator
 *      };
 *    int res;
 *
 *    res = libmem_register_cfi_0003_16_driver(&flash1_handle,
 *                                             flash1_start,
 *                                             libmem_get_geometry_size(flash1_geometry),
 *                                             flash1_geometry,
 *                                             0);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_cfi_0003_16_driver : success\n");
 *    else
 *      printf("libmem_register_cfi_0003_16_driver : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_cfi_0003_16_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info);

/*! \brief Register a combined multi-width CFI command set 1 and 3 (Intel) LIBMEM driver \ingroup Generic FLASH drivers \synopsis \description
 *
 *  \b \this registers a combined multi-width CFI command set 1 and 3 (Intel) LIBMEM driver.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the FLASH memory.
 *
 *  \param geometry
 *    A NULL terminated description of the FLASH's geometry.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function registers a combined multi-width CFI command set 1 and 3 (Intel)
 *  LIBMEM driver. The advantage of this driver over the individual single
 *  width and command set drivers is that one driver will support a range
 *  of Intel FLASH chips, the disadvantage is that of increased code size
 *  and reduced performance.
 *
 *  Example:
 *  \code
 *    const int flash1_max_geometry_regions = 4;
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_geometry_t flash1_geometry[flash1_max_geometry_regions];
 *    libmem_flash_info_t flash1_info;
 *    size_t flash1_size;
 *    int res;
 *
 *    // Detect the type, size and geometry of the Intel FLASH.
 *    res = libmem_cfi_get_info(flash1_start,
 *                              &flash1_size,
 *                              flash1_geometry,
 *                              flash1_max_geometry_regions,
 *                              &flash1_info);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      {
 *        // Register the driver
 *        res = libmem_register_cfi_intel_driver(&flash1_handle,
 *                                               flash1_start,
 *                                               flash1_size,
 *                                               flash1_geometry,
 *                                               &flash1_info);
 *
 *        if (res == LIBMEM_STATUS_SUCCESS)
 *          printf("libmem_register_cfi_intel_driver : success\n");
 *        else
 *          printf("libmem_register_cfi_intel_driver : failed (%d)\n", res);
 *
 *      }
 *    else
 *      printf("libmem_cfi_get_info : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_cfi_intel_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info);

/*! \brief Register a multi-width CFI command set 2 (AMD) LIBMEM driver \ingroup Generic FLASH drivers \synopsis \description
 *
 *  \b \this registers a multi-width CFI command set 2 (AMD) LIBMEM driver.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the FLASH memory.
 *
 *  \param geometry
 *    A NULL terminated description of the FLASH's geometry.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  This function registers a multi-width CFI command set 2 (AMD)
 *  LIBMEM driver. The advantage of this driver over the individual single
 *  width and command set drivers is that one driver will support a range
 *  of FLASH chips, the disadvantage is that of increased code size
 *  and reduced performance.
 *
 *  Example:
 *  \code
 *    const int flash1_max_geometry_regions = 4;
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_geometry_t flash1_geometry[flash1_max_geometry_regions];
 *    libmem_flash_info_t flash1_info;
 *    size_t flash1_size;
 *    int res;
 *
 *    // Detect the type, size and geometry of the Intel FLASH.
 *    res = libmem_cfi_get_info(flash1_start,
 *                              &flash1_size,
 *                              flash1_geometry,
 *                              flash1_max_geometry_regions,
 *                              &flash1_info);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      {
 *        // Register the driver
 *        res = libmem_register_cfi_amd_driver(&flash1_handle,
 *                                               flash1_start,
 *                                               flash1_size,
 *                                               flash1_geometry,
 *                                               &flash1_info);
 *
 *        if (res == LIBMEM_STATUS_SUCCESS)
 *          printf("libmem_register_cfi_amd_driver : success\n");
 *        else
 *          printf("libmem_register_cfi_amd_driver : failed (%d)\n", res);
 *
 *      }
 *    else
 *      printf("libmem_cfi_get_info : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_cfi_amd_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info);

/*! \brief Register a driver for an ST M28W320CB FLASH chip \ingroup FLASH drivers \synopsis \description
 *
 *  \b \this registers a driver for an ST M28W320CB FLASH chip.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    int res;
 *
 *    res = libmem_register_st_m28w320cb_driver(&flash1_handle, flash1_start);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_st_m28w320cb_driver : success\n");
 *    else
 *      printf("libmem_register_st_m28w320cb_driver : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_st_m28w320cb_driver(libmem_driver_handle_t *h, uint8_t *start);

/*! \brief Register a driver for an ST M28W320CT FLASH chip \ingroup FLASH drivers \synopsis \description
 *
 *  \b \this registers a driver for an ST M28W320CT FLASH chip.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    int res;
 *
 *    res = libmem_register_st_m28w320ct_driver(&flash1_handle, flash1_start);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_st_m28w320ct_driver : success\n");
 *    else
 *      printf("libmem_register_st_m28w320ct_driver : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_st_m28w320ct_driver(libmem_driver_handle_t *h, uint8_t *start);

/*! \brief Register a driver for a 16-bit SST39xFx00A FLASH chip \ingroup FLASH drivers \synopsis \description
 *
 *  \b \this registers a driver for a 16-bit SST39xFx00A FLASH chip.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the FLASH memory.
 *
 *  \param geometry
 *    A NULL terminated description of the FLASH's geometry.
 *
 *  \param flash_info
 *    A pointer to the FLASH information structure or NULL if not required.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t flash1_handle;
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    libmem_flash_info_t flash1_info;
 *    const int flash1_max_geometry_regions = 4;
 *    libmem_geometry_t flash1_geometry[flash1_max_geometry_regions];
 *    size_t size;
 *    int res;
 *   
 *    // Get CFI FLASH information and geometry 
 *    res = libmem_cfi_get_info(flash1_start, &size, flash1_geometry, flash1_max_geometry_regions, &flash1_info);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      {
 *        res = libmem_register_sst39xFx00A_16_driver(&flash1_handle, flash1_start, size, flash1_geometry, &flash1_info)
 *
 *        if (res == LIBMEM_STATUS_SUCCESS)
 *          printf("libmem_register_sst39xFx00A_16_driver : success\n");
 *        else
 *          printf("libmem_register_sst39xFx00A_16_driver : failed (%d)\n", res); 
 *      }
 *    else
 *      printf("libmem_cfi_get_info : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_sst39xFx00A_16_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size, const libmem_geometry_t *geometry, const libmem_flash_info_t *flash_info);

/*! \brief Register a driver for an AMD Am29Fxxx FLASH chip \ingroup FLASH drivers \synopsis \description
 *
 *  \b \this registers a driver for an AMD Am29Fxxx FLASH chip.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \param size
 *    The size of the address range handled by the LIBMEM driver in bytes.
 *
 *  \param geometry
 *    A pointer to a null-terminated geometry description list for the device.
 *
 *  \param device_id
 *    The device ID of the device.  The expected device ID is checked against the
 *    device ID read from the FLASH.  If the device IDs differ this function
 *    return LIBMEM_STATUS_INVALID_DEVICE.
 *
 *  \return
 *    The LIBMEM status result.
 *
 */
int libmem_register_am29fxxx_driver(libmem_driver_handle_t *h, uint8_t *start, unsigned size, const libmem_geometry_t *geometry, unsigned device_id);

/*! \brief Register a driver for an AMD Am29F200B FLASH chip \ingroup FLASH drivers \synopsis \description
 *
 *  \b \this registers a driver for an AMD Am29F200B FLASH chip.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \return
 *    The LIBMEM status result.
 *
 */
int libmem_register_am29f200b_driver(libmem_driver_handle_t *h, uint8_t *start);

/*! \brief Register a driver for an AMD Am29F200T FLASH chip \ingroup FLASH drivers \synopsis \description
 *
 *  \b \this registers a driver for an AMD Am29F200T FLASH chip.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \return
 *    The LIBMEM status result.
 *
 */
int libmem_register_am29f200t_driver(libmem_driver_handle_t *h, uint8_t *start);

/*! \brief Register a driver for an AMD Am29F400BB FLASH chip \ingroup FLASH drivers \synopsis \description
 *
 *  \b \this registers a driver for an AMD Am29F400BB FLASH chip.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \return
 *    The LIBMEM status result.
 *
 */
int libmem_register_am29f400bb_driver(libmem_driver_handle_t *h, uint8_t *start);

/*! \brief Register a driver for an AMD Am29F400BT FLASH chip \ingroup FLASH drivers \synopsis \description
 *
 *  \b \this registers a driver for an AMD Am29F400BT FLASH chip.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \return
 *    The LIBMEM status result.
 *
 */
int libmem_register_am29f400bt_driver(libmem_driver_handle_t *h, uint8_t *start);

/*! \brief Register a driver for an AMD Am29LV010B FLASH chip \ingroup FLASH drivers \synopsis \description
 *
 *  \b \this registers a driver for an AMD Am29LV010B FLASH chip.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the FLASH memory.
 *
 *  \return
 *    The LIBMEM status result.
 *
 */
int libmem_register_am29lv010b_driver(libmem_driver_handle_t *h, uint8_t *start);

/*! \brief Register a simple driver that directly accesses RAM \ingroup RAM drivers \synopsis \description
 *
 *  \b \this registers a simple driver that directly accesses RAM.
 *
 *  \param h
 *    A pointer to the LIBMEM handle structure to use for this LIBMEM driver.
 *
 *  \param start
 *    The start address of the RAM.
 *
 *  \param size
 *    The size of the RAM.
 *
 *  \return
 *    The LIBMEM status result.
 *
 *  Example:
 *  \code
 *    libmem_driver_handle_t ram1_handle;
 *    uint8_t *ram1_start = (uint8_t *)0x10000000;
 *    const size_t ram1_size = 1024;
 *    int res;
 *
 *    res = libmem_register_ram_driver(&ram_handle, ram1_start, ram1_size);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      printf("libmem_register_ram_driver : success\n");
 *    else
 *      printf("libmem_register_ram_driver : failed (%d)\n", res);
 *  \endcode
 */
int libmem_register_ram_driver(libmem_driver_handle_t *h, uint8_t *start, size_t size);

/*! \brief Convert kilobytes to bytes \ingroup Utility macros \synopsis \description
 *
 *  \b \this converts kilobytes to bytes, e.g. LIBMEM_KB(10) = 10*1024.
 */
#define LIBMEM_KB(X) ((X)*1024)

/*! \brief Convert megabytes to bytes \ingroup Utility macros \synopsis \description
 *
 *  \b \this converts megabytes to bytes, e.g. LIBMEM_MB(10) = 10*1024*1024.
 */
#define LIBMEM_MB(X) (LIBMEM_KB(X)*1024)

#ifdef __cplusplus
}
#endif
 
#endif

