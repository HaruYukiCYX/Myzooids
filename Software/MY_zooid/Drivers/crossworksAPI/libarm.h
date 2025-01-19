// ARM Runtime Support.
//
// Copyright (c) 2006 Rowley Associates Limited.
//
// This file may be distributed under the terms of the License Agreement
// provided with this software.
//
// THIS FILE IS PROVIDED AS IS WITH NO WARRANTY OF ANY KIND, INCLUDING THE
// WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

#ifndef libarm_h
#define libarm_h

#ifdef __cplusplus
extern "C" {
#endif

#include <stdlib.h>

/*! \brief Enables or disables IRQ interrupts.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param enable 
 *    If non-zero IRQ interrupts will be enabled, otherwise they will be
 *    disabled.
 *
 *  \return The IRQ enable state prior to enabling the IRQ interrupt.
 * 
 *  This function enables or disables IRQ interrupts. It modifies the CPSR
 *  register's control field and therefore will only work when the CPU is
 *  executing in a privileged operating mode.
 *
 *  \task Example
 *
 *  \code
 *  // Disable IRQ interrupts if enabled
 *  int en = libarm_set_irq(0); 
 *  \hdots
 *  // Restore IRQ interrupts
 *  libarm_set_irq(en);
 *  \endcode
 */
int libarm_set_irq(int enable);

/*! \brief Enables or disables FIQ interrupts.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param enable 
 *    If non-zero FIQ interrupts will be enabled, otherwise they will be
 *    disabled.
 *
 *  \return The FIQ enable state prior to enabling the FIQ interrupt.
 *
 *  This function enables or disables FIQ interrupts. It modifies the CPSR
 *  register's control field and therefore will only work when the CPU is
 *  executing in a privileged operating mode.
 *
 *  \task Example
 *
 *  \code
 *  // Enable FIQ interrupts
 *  libarm_set_fiq(1); 
 *  \hdots
 *  // Disable FIQ interrupts
 *  libarm_set_fiq(0);
 *  \endcode
 */
int libarm_set_fiq(int enable);

/*! \brief Enable IRQ interrupts.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  This function enables IRQ interrupts by clearing the I bit in the
 *  CPSR register.
 *
 *  Note that this function modifies the CPSR register's control field and
 *  therefore will only work when the CPU is executing in a privileged
 *  operating mode.
 *
 *  Example:
 *
 *  \code
 *  // Enable IRQ interrupts
 *  libarm_enable_irq();
 *  \endcode
 */
void libarm_enable_irq(void);

/*! \brief Disable IRQ interrupts.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  This function disables IRQ interrupts by setting the I bit in the
 *  CPSR register.
 *
 *  Note that this function modifies the CPSR register's control field and
 *  therefore will only work when the CPU is executing in a privileged
 *  operating mode.
 *
 *  \task Example
 *
 *  \code
 *  // Disable IRQ interrupts
 *  libarm_disable_irq();
 *  \endcode
 */
void libarm_disable_irq(void);

/*! \brief Enable FIQ interrupts.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  This function enables FIQ interrupts by clearing the F bit in the
 *  CPSR register.
 *
 *  Note that this function modifies the CPSR register's control field and
 *  therefore will only work when the CPU is executing in a privileged
 *  operating mode.
 *
 *  \task Example
 *
 *  \code
 *  // Enable FIQ interrupts
 *  libarm_enable_fiq();
 *  \endcode
 */
void libarm_enable_fiq(void);

/*! \brief Disable FIQ interrupts.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  This function disables FIQ interrupts by setting the F bit in the
 *  CPSR register.
 *
 *  Note that this function modifies the CPSR register's control field and
 *  therefore will only work when the CPU is executing in a privileged
 *  operating mode.
 *
 *  \task Example
 *
 *  \code
 *  // Disable FIQ interrupts
 *  libarm_disable_fiq();
 *  \endcode
 */
void libarm_disable_fiq(void);

/*! \brief Enable IRQ and FIQ interrupts.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \return The IRQ and FIQ enable state prior to enabling the IRQ and FIQ interrupts.
 *
 *  This function enables both IRQ and FIQ interrupts.
 *
 *  Note that this function modifies the CPSR register's control field and
 *  therefore will only work when the CPU is executing in a privileged
 *  operating mode.
 *
 *  \task Example
 *
 *  \code
 *  // Enable IRQ and FIQ interrupts
 *  libarm_enable_irq_fiq();
 *  \endcode
 */
void libarm_enable_irq_fiq(void);

/*! \brief Disables IRQ and FIQ interrupts and return the previous enable state.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \return The IRQ and FIQ enable state prior to disabling the IRQ and FIQ interrupts.
 *
 *  This function disables both IRQ and FIQ interrupts, it also returns the 
 *  previous IRQ and FIQ enable state so that it can be restored using
 *  \a libarm_restore_irq_fiq.
 *
 *  Note that this function modifies the CPSR register's control field and
 *  therefore will only work when the CPU is executing in a privileged
 *  operating mode.
 *
 *  \task Example
 * 
 *  \code
 *  int s;
 *
 *  // Disable IRQ and FIQ interrupts
 *  s = libarm_disable_irq_fiq();
 *  \hdots
 *  // Restore IRQ and FIQ interrupts
 *  libarm_restore_irq_fiq(s);
 *  \endcode
 *
 */
int libarm_disable_irq_fiq(void);

/*! \brief Restores the IRQ and FIQ interrupt enable state.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param disable_irq_fiq_return
 *    The value returned from \a libarm_disable_irq_fiq.
 * 
 *  This function restores the IRQ and FIQ enable state to the state it
 *  was in before a call to \a libarm_disable_irq_fiq.
 *
 *  Note that this function modifies the CPSR register's control field and
 *  therefore will only work when the CPU is executing in a privileged
 *  operating mode.
 *
 *  \task Example
 * 
 *  \code
 *  int s;
 *
 *  // Disable IRQ and FIQ interrupts
 *  s = libarm_disable_irq_fiq();
 *  \hdots
 *  // Restore IRQ and FIQ interrupts
 *  libarm_restore_irq_fiq(s);
 *  \endcode
 */
void libarm_restore_irq_fiq(int disable_irq_fiq_return);

/*! \brief Re-enable ARM's global interrupts from within an IRQ interrupt service routine.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  ARM IRQ interrupts are automatically disabled on entry to an interrupt
 *  handler and subsequently re-enabled on exit.
 *  You can use \a libarm_isr_enable_irq to re-enable interrupts from within
 *  an interrupt handler so that higher-priority interrupts may interrupt the
 *  current interrupt handler.
 *
 *  This call must be accompanied with a call to \a libarm_isr_disable_irq 
 *  prior to completion of the interrupt service routine.
 *
 *  Note that this function should only be called from within an IRQ interrupt 
 *  handler and that calling this function changes the operating mode, and
 *  therefore the stack, so if it is being called from a C function you should
 *  not use any automatic variables within that function.
 */
void libarm_isr_enable_irq(void);

/*! \brief Re-disable ARM's global interrupts from within an IRQ interrupt service routine.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  A call to \a libarm_isr_enable_irq must have been made prior to calling
 *  this function.
 *
 *  Note that this call should only be made from within an IRQ interrupt 
 *  handler.
 */
void libarm_isr_disable_irq(void);

/*! \brief Set the value of the CPSR.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param cpsr
 *    The value the CPSR should be set to.
 *
 *  This function sets the value of all fields of the CPSR (Current Program
 *  Status Register).
 */
void libarm_set_cpsr(unsigned long cpsr);

/*! \brief Get the value of the CPSR.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \return The value of the CPSR.
 *
 * This function returns the value of the CPSR (Current Program Status
 * Register).
 */
unsigned long libarm_get_cpsr(void);

/*! \brief Write a word of data to the host over JTAG using the ARM debug comms channel.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param data
 *    The data to write to the debug comms channel.
 *
 *  The ARM's debug comms channel is usually used by debuggers so writing to
 *  this port with a debugger attached can cause unpredictable results.
 */
void libarm_dcc_write(unsigned long data);

/*! \brief Read a word of data from the host over JTAG using the ARM's debug comms channel.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \return The data read from the debug comms channel.
 *
 *  The ARM's debug comms channel is usually used by debuggers so reading
 *  from this port with a debugger attached can cause unpredictable results.
 */
unsigned long libarm_dcc_read(void);

/*! \brief Serve commands from the ARM's debug communication channel.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  CrossWorks uses the ARM's debug communication channel to carry operations
 *  such as memory access, to do this a simple client server protocol is
 *  run over the channel. This function runs the debug communications channel
 *  server, it returns when the host terminates the server.
 */
void libarm_run_dcc_port_server(void);

/*! \brief Create a flat mapped level 1 translation table.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param translation_table
 *    A pointer to the start of the translation table.
 *
 *  This function creates a flat mapped (i.e. virtual addresses == 
 *  physical addresses) level 1 MMU translation table at the location
 *  pointed to by \a translation_table (the translation table is 16BKytes
 *  in size).
 *
 *  Note that this function only initialises the translation table, it
 *  doesn't set the translation table base register.
 *
 */
void libarm_mmu_flat_initialise_level_1_table(void *translation_table);

/*! \brief Create a level 2 small page table for an address range.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param translation_table
 *    A pointer to the start of the translation table.
 *
 *  \param start
 *    A pointer to the start address of the address range.
 *
 *  \param size
 *    The size of the address range in bytes.
 *
 *  \param coarse_page_tables
 *    A pointer to the start address of the coarse page tables.
 *
 *  This function creates a level 2 small page table for the specified
 *  address range, it requires a level 1 translation table to be createdi
 *  using \a libarm_mmu_flat_initialise_level_1_table prior to calling.
 */
void libarm_mmu_flat_initialise_level_2_small_page_table(void *translation_table, void *start, size_t size, void *coarse_page_tables);

/*! \brief Mark region of memory described by level 1 section descriptors as cacheable.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 * 
 *  \param translation_table
 *    A pointer to the start of the translation table.
 *
 *  \param start
 *    A pointer to the start of the cacheable region.
 *
 *  \param size
 *    The size of the cacheable region in bytes.
 *
 *  This function marks a region of memory described by level 1 section 
 *  descriptors as cacheable, it requires a level 1 translation table to be
 *  created using \a libarm_mmu_flat_initialise_level_1_table prior to calling.
 */
void libarm_mmu_flat_set_level_1_cacheable_region(void *translation_table, void *start, size_t size);

/*! \brief Mark region of memory described by level 2 small page table descriptors as cacheable.
 *
 *  \ingroup Functions
 *
 *  \synopsis \description
 *
 *  \param translation_table
 *    A pointer to the start of the translation table.
 *
 *  \param start
 *    A pointer to the start address of the cacheable region.
 *
 *  \param size
 *    The size of the cacheable region in bytes.
 *
 *  This function marks a region of memory described by level 2 small page table
 *  descriptors as cacheable, it requires a level 2 small page table table to be
 *  created using \a libarm_mmu_flat_initialise_level_2_small_page_table prior
 *  to calling.
 */
void libarm_mmu_flat_set_level_2_small_page_cacheable_region(void *translation_table, void *start, size_t size);

#ifdef __cplusplus
}
#endif

#endif
