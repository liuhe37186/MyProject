/*****************************************************************************/
/* BroadVoice(R)16 (BV16) Fixed-Point ANSI-C Source Code                     */
/* Revision Date: November 13, 2009                                          */
/* Version 1.1                                                               */
/*****************************************************************************/

/*****************************************************************************/
/* Copyright 2000-2009 Broadcom Corporation                                  */
/*                                                                           */
/* This software is provided under the GNU Lesser General Public License,    */
/* version 2.1, as published by the Free Software Foundation ("LGPL").       */
/* This program is distributed in the hope that it will be useful, but       */
/* WITHOUT ANY SUPPORT OR WARRANTY; without even the implied warranty of     */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the LGPL for     */
/* more details.  A copy of the LGPL is available at                         */
/* http://www.broadcom.com/licenses/LGPLv2.1.php,                            */
/* or by writing to the Free Software Foundation, Inc.,                      */
/* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.                 */
/*****************************************************************************/


/*****************************************************************************
  ptdec.c : Common Fixed-Point Library: 

  $Log: ptdec.c,v $
  Revision 1.1.1.1  2013/02/28 10:40:56  zzhan
  no message

  Revision 1.1.1.1  2011/09/27 09:15:18  zzhan
  no message

******************************************************************************/

#include "typedef.h"
#include "bvcommon.h"

void pp3dec(
            Word16   idx,
            Word16   *b)
{
   Word16   *fp;
   Word16   i;
   fp = pp9cb+idx*9;
   for (i=0;i<3;i++) 
      b[i] = *fp++;
}
