#ifndef _DEBUG_H_
#define _DEBUG_H_


#ifndef NDEBUG
#include <stdio.h>
#define debug_printf(...) fprintf(stderr, __VA_ARGS__)
#else
#define debug_printf(...)
#endif

#endif
