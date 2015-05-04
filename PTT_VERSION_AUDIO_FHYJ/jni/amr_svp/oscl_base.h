/* ------------------------------------------------------------------
 * Copyright (C) 2009 Martin Storsjo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */

#ifndef OSCL_BASE_H
#define OSCL_BASE_H

//#include "stdint.h"

#ifndef STDINT_H
#define STDINT_H

#define uint8_t unsigned char
#define int16_t short
#define int64_t __int64
typedef signed long int32_t;
#define int8_t char
#define uint32_t unsigned int
#define uint64_t unsigned __int64
#define uint16_t unsigned short

#endif /* STDINT_H */



typedef int8_t int8;
typedef uint8_t uint8;
typedef int16_t int16;
typedef uint16_t uint16;
typedef int32_t int32;
typedef uint32_t uint32;
typedef int64_t int64;
typedef uint64_t uint64;

/*
typedef signed char int8;
typedef unsigned char uint8;
typedef short int int16;
typedef unsigned short int uint16;
typedef int int32;
typedef unsigned int uint32;
typedef long long int int64;
typedef unsigned long long int uint64;

*/
#define OSCL_IMPORT_REF
#define OSCL_EXPORT_REF
#define OSCL_UNUSED_ARG(x) (void)(x)

#endif
