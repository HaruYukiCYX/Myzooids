#include <sys/stat.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>


#undef errno
extern int errno;

int _write(int file, char *ptr, int len) {
    // ???????write??
    return len;
}

int _close(int file) {
    // ???????close??
    return -1;
}

int _fstat(int file, struct stat *st) {
    // ???????fstat??
    st->st_mode = S_IFCHR;
    return 0;
}

int _isatty(int file) {
    // ???????isatty??
    return 1;
}

int _lseek(int file, int ptr, int dir) {
    // ???????lseek??
    return 0;
}

int _read(int file, char *ptr, int len) {
    // ???????read??
    return 0;
}

caddr_t _sbrk(int incr) {
    extern char end; // ??????
    static char *heap_end;
    char *prev_heap_end;

    if (heap_end == 0) {
        heap_end = &end;
    }
    prev_heap_end = heap_end;
    heap_end += incr;

    return (caddr_t) prev_heap_end;
}

void _exit(int status) {
    // ???????exit??
    while (1) {}
}

int _kill(int pid, int sig) {
    // ???????kill??
    errno = EINVAL;
    return -1;
}

int _getpid(void) {
    // ???????getpid??
    return 1;
}
