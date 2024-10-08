/*
 *
 *  Copyright (C) 2000 Silicon Graphics, Inc.  All Rights Reserved. 
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  Further, this software is distributed without any warranty that it is
 *  free of the rightful claim of any third person regarding infringement
 *  or the like.  Any license provided herein, whether implied or
 *  otherwise, applies only to this software file.  Patent licenses, if
 *  any, provided herein do not apply to combinations of this program with
 *  other software, or any other product whatsoever.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact information: Silicon Graphics, Inc., 1600 Amphitheatre Pkwy,
 *  Mountain View, CA  94043, or:
 * 
 *  http://www.sgi.com 
 * 
 *  For further information regarding this notice, see: 
 * 
 *  http://oss.sgi.com/projects/GenInfo/NoticeExplan/
 *
 */


/*
 * Copyright (C) 1990,91   Silicon Graphics, Inc.
 *
 _______________________________________________________________________
 ______________  S I L I C O N   G R A P H I C S   I N C .  ____________
 |
 |   $Revision: 1.1.1.1 $
 |
 |   Description:
 |      This file defines the SoDrawStyleElement class.
 |
 |   Author(s)          : Paul S. Strauss
 |
 ______________  S I L I C O N   G R A P H I C S   I N C .  ____________
 _______________________________________________________________________
 */

package jscenegraph.database.inventor.elements;

import jscenegraph.database.inventor.misc.SoState;


///////////////////////////////////////////////////////////////////////////////
///
///  \class SoDrawStyleElement
///  \ingroup Elements
///
///  Element that stores the current draw style.
///
//////////////////////////////////////////////////////////////////////////////

/**
 * @author Yves Boyadjian
 *
 */
public class SoDrawStyleElement extends SoInt32Element {

    //! These are the available draw styles:
    public enum Style {
        FILLED,                 //!< Filled regions
        LINES,                  //!< Outlined regions
        POINTS,                 //!< Points
        INVISIBLE;               //!< Nothing!

        private static Style[] values = Style.values();

        public static Style fromValue(int value) {
        	return values[value];
        }
        public int getValue() {
        	return ordinal();
        }
    };

	  public static void
	   initClass(final Class<? extends SoElement> javaClass)
	   {
		  SoElement.initClass(javaClass);
	   }

	  ////////////////////////////////////////////////////////////////////////
//
// Description:
//    Initializes element
//
// Use: public

public void
init(SoState state)
//
////////////////////////////////////////////////////////////////////////
{
    data = getDefault().getValue();
}
///////////////////////////////////////////////////////////////////////
//
// Description
// set the current draw style in the state
//
public static void
set(SoState state, Style style)
{
    SoInt32Element.set(classStackIndexMap.get(SoDrawStyleElement.class), state, (int)style.getValue()); 
    SoShapeStyleElement.setDrawStyle(state,(int)style.getValue());
}

	  
	  
    //! Returns current draw style from the state
	  public static Style        get(SoState state)
        { return Style.fromValue(SoInt32Element.get(classStackIndexMap.get(SoDrawStyleElement.class), state)); }

    //! Returns the default draw style
    public static Style        getDefault()            { return Style.FILLED; }


}
