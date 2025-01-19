/*****************************************************************************
 * Copyright (c) 2006 Rowley Associates Limited.                             *
 *                                                                           *
 * This file may be distributed under the terms of the License Agreement     *
 * provided with this software.                                              *
 *                                                                           *
 * THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE   *
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. *
 *****************************************************************************/

#ifndef libmem_loader_h
#define libmem_loader_h

/*! \brief Start up a LIBMEM loader that uses direct RPC (remote procedure calls).
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param comm_buffer_start
 *    A pointer to the start of an area of RAM that can be used by the host to
 *    store data passed to the remotely called libmem functions.
 *
 *  \param comm_buffer_end
 *    A pointer to the last byte of the of an area of RAM that can be used by
 *    the host to store data passed to the remotely called libmem functions.
 *
 *  \return The last error result returned from a LIBMEM function or \a LIBMEM_STATUS_SUCCESS if there has been no error.
 *
 *  This function starts up a LIBMEM loader that uses direct remote procedure
 *  calls of the LIBMEM library.
 *
 *  A communication buffer is required to store the parameters passed
 *  to the LIBMEM functions, this buffer is specified using the
 *  \a comm_buffer_start and \a comm_buffer_end parameters. The
 *  buffer must be at least 8 bytes in length, however you will find
 *  the bigger the buffer is, the better the loader performance will be
 *  because fewer RPC calls will be required.
 *
 *  Example:
 *
 *  \code
 *  static unsigned char buffer[256];
 *
 *  int main(void)
 *  {
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    const int flash1_max_geometry_regions = 4;
 *    libmem_driver_handle_t flash1_handle;
 *    libmem_geometry_t flash1_geometry[flash1_max_geometry_regions];
 *    libmem_flash_info_t flash1_info;
 *    int res;
 *
 *    // Register FLASH driver.
 *    res = libmem_register_cfi_driver(&flash1_handle, 
 *                                     flash1_start,
 *                                     flash1_geometry,
 *                                     flash1_max_geometry_regions, 
 *                                     &flash1_info);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      {
 *        // Run the loader 
 *        libmem_rpc_loader_start(buffer, buffer + sizeof(buffer) - 1);
 *      }
 *
 *    libmem_rpc_loader_exit(res, NULL);
 *
 *    return 0;
 *  }
 *  \endcode
 *
 *  Parameters are passed to an RPC loader by initialising the CPU
 *  registers prior to starting the loader. On entry, the register R0
 *  contains the LIBMEM loader flags which can be any of the following:
 *
 *  \item \a LIBMEM_RPC_LOADER_FLAG_PARAM - If this flag is set then R1 holds
 *    the optional loader parameter specified using CrossStudio's 
 *    "Target | Loader Parameter" project property.
 *
 *  \item \a LIBMEM_RPC_LOADER_FLAG_PRESERVE_STATE - If this flag is set then
 *    the loader should attempt to preserve any existing target
 *    state. This is typically set when a loader is used to modify
 *    memory while a target program is running which would happen
 *    when a software breakpoint is set in ROM during a debug session.
 *    If this is functionality is not required then this flag can be ignored.
 *
 *  Note that older versions of LIBMEM loader required that you always link 
 *  certain LIBMEM functions such as \a libmem_write() and  \a libmem_erase()
 *  into the loader using the "Linker | Keep Symbols" project property. This
 *  is now only required if you want the loader to be compatible with versions
 *  of CrossWorks prior to version 3.
 *
 */
int libmem_rpc_loader_start(void *comm_buffer_start, void *comm_buffer_end);

/*! \brief Start up a LIBMEM loader that uses direct RPC (remote procedure calls) with additional options flags.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param comm_buffer_start
 *    A pointer to the start of an area of RAM that can be used by the host to
 *    store data passed to the remotely called libmem functions.
 *
 *  \param comm_buffer_end
 *    A pointer to the last byte of the of an area of RAM that can be used by
 *    the host to store data passed to the remotely called libmem functions.
 *
 *  \param options
 *    This parameter is usually 0, but can be used to enable different loader
 *    behaviour by specifying a combination of the following loader options:
 *
 *    \item \a LIBMEM_RPC_LOADER_OPTION_HOST_WRITE - This option flag enables host
 *      write loader mode. This mode is intended for devices with small
 *      amounts RAM. When enabled it guarantees that all write operations will
 *      be the size of, and aligned to, the communication buffer. This allows
 *      a lot of the complexity involved in carrying out a memory write
 *      operation to be avoided, thereby saving code size at the cost of
 *      download performance.
 *
 *    \item \a LIBMEM_RPC_LOADER_OPTION_HOST_ERASE - This option flag enables host
 *      erase loader mode. This mode is intended for devices with small
 *      amounts RAM. When enabled it guarantees that all erase operations will
 *      be the size of, and aligned to, the device's geometry. This allows a
 *      lot of the complexity involved in carrying out a memory erase
 *      operation to be avoided, thereby saving code size at the cost of
 *      download performance.
 *
 *  \return The last error result returned from a LIBMEM function or \a LIBMEM_STATUS_SUCCESS if there has been no error.
 *
 *  This function is the same as \a libmem_rpc_loader_start except that it
 *  allows a loader option parameter to be specified.
 *
 *  Example:
 *
 *  \code
 *  static unsigned char buffer[256];
 *
 *  int main(void)
 *  {
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    const int flash1_max_geometry_regions = 4;
 *    libmem_driver_handle_t flash1_handle;
 *    libmem_geometry_t flash1_geometry[flash1_max_geometry_regions];
 *    libmem_flash_info_t flash1_info;
 *    int res;
 *
 *    // Register FLASH driver.
 *    res = libmem_register_cfi_driver(&flash1_handle, 
 *                                     flash1_start,
 *                                     flash1_geometry,
 *                                     flash1_max_geometry_regions, 
 *                                     &flash1_info);
 *
 *    if (res == LIBMEM_STATUS_SUCCESS)
 *      {
 *        // Run the loader 
 *        libmem_rpc_loader_start_ex(buffer,
 *                                   buffer + sizeof(buffer) - 1,
 *                                   LIBMEM_LOADER_OPTION_HOST_WRITE | 
 *                                   LIBMEM_LOADER_OPTION_HOST_ERASE);
 *      }
 *
 *    libmem_rpc_loader_exit(res, NULL);
 *
 *    return 0;
 *  }
 *  \endcode
 *
 */
int libmem_rpc_loader_start_ex(void *comm_buffer_start, void *comm_buffer_end, uint32_t options);

/*! \brief Exit an RPC loader and return the exit status to the host.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param result
 *    A LIBMEM status result.
 *
 *  \param error
 *    Pointer to optional error string or NULL if not required.
 *
 *  This function provides a way of signalling to the host that
 *  the loader program has completed and also allows the loader
 *  to return an exit code and optional error string.
 *  Note that this function should only be used in conjunction with
 *  libmem_rpc_loader_start() and that any code located after
 *  the call to libmem_rpc_loader_exit() has been made will not be
 *  executed.
 *
 *  The \a error parameter can be used to describe an error not
 *  covered by the LIBMEM status results. To use it, set \a result
 *  to LIBMEM_STATUS_ERROR and \a error to the error string to
 *  be displayed.
 *
 *  The following example demonstrates how to return user defined
 *  error messages from the loader and how code can be executed
 *  after the loader server has terminated prior to the
 *  loader program exiting:
 *
 *  \code
 *  static unsigned char buffer[256];
 *
 *  int initialise()
 *  {
 *    ... initialisation code ...
 *  }
 *
 *  int deinitialise()
 *  {
 *    ... deinitialisation code ...
 *  }
 *
 *  int main(void)
 *  {
 *    uint8_t *flash1_start = (uint8_t *)0x10000000;
 *    const int flash1_max_geometry_regions = 4;
 *    libmem_driver_handle_t flash1_handle;
 *    libmem_geometry_t flash1_geometry[flash1_max_geometry_regions];
 *    libmem_flash_info_t flash1_info;
 *    int res;
 *    const char *error = 0;
 *
 *    if (initialise())
 *      {
 *        // Register FLASH driver.
 *        res = libmem_register_cfi_driver(&flash1_handle, 
 *                                         flash1_start,
 *                                         flash1_geometry,
 *                                         flash1_max_geometry_regions, 
 *                                         &flash1_info);
 *
 *        if (res == LIBMEM_STATUS_SUCCESS)
 *          {
 *            // Run the loader 
 *            libmem_rpc_loader_start(buffer, buffer + sizeof(buffer) - 1);
 *          }
 *     }
 *   else
 *     {
 *       res = LIBMEM_STATUS_ERROR;
 *       error = "cannot initialise loader";
 *     }
 *
 *   if (!deinitialise() && res == LIBMEM_STATUS_SUCCESS)
 *     {
 *       res = LIBMEM_STATUS_ERROR;
 *       error = "cannot deinitialise loader";
 *     }
 *
 *    libmem_rpc_loader_exit(res, NULL);
 *
 *    return 0;
 *  }
 *  \endcode
 *
 */
void libmem_rpc_loader_exit(int result, const char *error);

/*! \brief LIBMEM loader interface version number.
 *
 *  \ingroup Macros
 *
 *  \synopsis \description
 *
 *  The LIBMEM loader interface version number.
 */
#define LIBMEM_LOADER_VERSION_NUMBER 3

/*! \brief Indicates whether the loader parameter has been set.
 *
 *  \ingroup Macros
 *
 *  \synopsis \description
 *
 *  LIBMEM loader flag used to indicate whether the loader parameter
 *  has been set.
 *
 *  If this flag is set in R0 on entry to an RPC loader then R1 holds the
 *  optional loader parameter specified using CrossStudio's 
 *  "Target | Loader Parameter" project property.
 */
#define LIBMEM_RPC_LOADER_FLAG_PARAM (1 << 0)

/*! \brief Indicates that a loader should preserve target state.
 *
 *  \ingroup Macros
 *
 *  \synopsis \description
 *
 *  LIBMEM loader flag used to indicate that a loader should preserve target
 *  state.
 *
 *  If this flag is set in R0 on entry to an RPC loader then the loader should
 *  attempt to preserve any existing target state. This is typically set when a 
 *  loader is used to modify memory while a target program is running which 
 *  would happen when a software breakpoint is set in ROM during a debug
 *  session. If this is functionality is not required then this flag can be ignored.
 */
#define LIBMEM_RPC_LOADER_FLAG_PRESERVE_STATE (1 << 31)

/*! \brief Enables host write loader mode.
 *
 *  \ingroup Macros
 *
 *  \synopsis \description
 *
 *  LIBMEM loader option that enables host write mode when used in the 
 *  \em options parameter of \a libmem_rpc_loader_start_ex.
 */
#define LIBMEM_RPC_LOADER_OPTION_HOST_WRITE (1 << 31)

/*! \brief Enables host erase loader mode.
 *
 *  \ingroup Macros
 *
 *  \synopsis \description
 *
 *  LIBMEM loader option that enables host erase mode when used in the 
 *  \em options parameter of \a libmem_rpc_loader_start_ex.
 */
#define LIBMEM_RPC_LOADER_OPTION_HOST_ERASE (1 << 30)

/*! \brief Magic number used to identify LIBMEM loader.
 *
 *  \ingroup Macros
 *
 *  \synopsis \description
 *
 *  Defines the magic number used by host to identify the loader as a LIBMEM
 *  loader.
 */
#define LIBMEM_RPC_LOADER_MAGIC_NUMBER 0x76E9C416


#endif

