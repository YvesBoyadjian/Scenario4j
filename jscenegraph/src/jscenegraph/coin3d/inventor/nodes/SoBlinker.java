/**************************************************************************\
 * Copyright (c) Kongsberg Oil & Gas Technologies AS
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 \**************************************************************************/

/*!
  \class SoBlinker SoBlinker.h Inventor/nodes/SoBlinker.h
  \brief The SoBlinker class is a cycling switch node.

  \ingroup coin_nodes

  This switch node cycles its children SoBlinker::speed number of
  times per second. If the node has only one child, it will be cycled
  on and off. Cycling can be turned off using the SoBlinker::on field,
  and the node then behaves like a normal SoSwitch node.

  <b>FILE FORMAT/DEFAULTS:</b>
  \code
    Blinker {
        whichChild -1
        speed 1
        on TRUE
    }
  \endcode
*/

// *************************************************************************

package jscenegraph.coin3d.inventor.nodes;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SoType;
import jscenegraph.database.inventor.engines.SoCalculator;
import jscenegraph.database.inventor.engines.SoTimeCounter;
import jscenegraph.database.inventor.fields.SoFieldData;
import jscenegraph.database.inventor.fields.SoSFBool;
import jscenegraph.database.inventor.fields.SoSFFloat;
import jscenegraph.database.inventor.misc.SoNotList;
import jscenegraph.database.inventor.nodes.SoSubNode;
import jscenegraph.database.inventor.nodes.SoSwitch;
import jscenegraph.database.inventor.sensors.SoOneShotSensor;
import jscenegraph.port.Destroyable;

/**
 * @author Yves Boyadjian
 *
 */
public class SoBlinker extends SoSwitch {

	private final SoSubNode nodeHeader = SoSubNode.SO_NODE_HEADER(SoBlinker.class,this);
	   
	   public                                                                     
	    static SoType       getClassTypeId()        /* Returns class type id */   
	                                    { return SoSubNode.getClassTypeId(SoBlinker.class);  }                   
	  public  SoType      getTypeId()      /* Returns type id      */
	  {
		  return nodeHeader.getClassTypeId();
	  }
	  public                                                                  
	    SoFieldData   getFieldData()  {
		  return nodeHeader.getFieldData();
	  }
	  public  static SoFieldData[] getFieldDataPtr()                              
	        { return SoSubNode.getFieldDataPtr(SoBlinker.class); }    	  	
	
	  public final SoSFFloat speed = new SoSFFloat();
	  public final SoSFBool on = new SoSFBool();

	private SoBlinkerP pimpl;

	  /*!
	  Constructor.
	*/
	public SoBlinker()
	{
	  pimpl = new SoBlinkerP(this);

	  pimpl.calculator = new SoCalculator();
	  pimpl.calculator.ref();
	  pimpl.calculator.a.connectFrom(this.on);
	  pimpl.calculator.b.connectFrom(this.speed);
	  pimpl.calculator.expression.setValue(new SoCalculator.Expression() {
											   @Override
											   public void run(float[] abcdefgh, SbVec3f[] ABCDEFGH, float[][] oaobocod, SbVec3f[] oAoBoCoD) {
													float a = abcdefgh[0];
													float b = abcdefgh[1];
													float[] oa = oaobocod[0];
												   oa[0] = ((b > 0) && (a != 0)) ? 1.0f : 0.0f;
											   }
										   }
	  ); // "oa = ((b > 0) && (a != 0)) ? 1.0 : 0.0;");

	  pimpl.counter = new SoTimeCounter();
	  pimpl.counter.ref();
	  pimpl.counter.min.setValue((short)SO_SWITCH_NONE);
	  pimpl.counter.max.setValue((short)SO_SWITCH_NONE);
	  pimpl.counter.frequency.connectFrom(this.speed);
	  pimpl.counter.on.connectFrom(pimpl.calculator.oa);
	  pimpl.whichChildSensor =
	    new SoOneShotSensor(SoBlinkerP::whichChildCB, this.pimpl);
	  pimpl.whichChildSensor.setPriority(1);
	  pimpl.whichvalue = SO_SWITCH_NONE;


	  nodeHeader.SO_NODE_INTERNAL_CONSTRUCTOR(SoBlinker.class);

	  nodeHeader.SO_NODE_ADD_FIELD(speed,"speed", (1.0f));
	  nodeHeader.SO_NODE_ADD_FIELD(on,"on", (true));
	  
	  this.whichChild.connectFrom(pimpl.counter.output, true);
	}

	/*!
	  Destructor.
	*/
	public void destructor()
	{
	  Destroyable.delete( pimpl.whichChildSensor );
	  pimpl.counter.unref();
	  pimpl.calculator.unref();
	  Destroyable.delete( pimpl );
	}

	/*!
	  \copybrief SoBase::initClass(void)
	*/
	public static void initClass()
	{
	  //SO_NODE_INTERNAL_INIT_CLASS(SoBlinker, SO_FROM_INVENTOR_1);
		SoSubNode.SO__NODE_INIT_CLASS(SoBlinker.class, "Blinker", SoSwitch.class);
	}

// Documented in superclass. Overridden to detect "external" changes
// (i.e. not caused by the internal timer engine).
	public void
	notify(SoNotList nl)
	{
		// See if the whichChild field was "manually" set.
		if (nl.getFirstRec().getBase() == this &&
			nl.getLastField() == this.whichChild) {
		// delay whichChild reset with the one shot sensor (to enable
		// children to be added before the reset is actually done)

		// disable connection while reading whichChild to get the actual value set
		boolean old = this.whichChild.isConnectionEnabled();
		this.whichChild.enableConnection(false);
		this.pimpl.whichvalue = this.whichChild.getValue();
		this.whichChild.enableConnection(old);
		this.pimpl.whichChildSensor.schedule();
	}

		// Check if a child was added or removed.
		int lastchildidx = this.getNumChildren() - 1;

		if (this.pimpl.counter.max.getValue() != lastchildidx) {
		// Wrap to avoid recursive invocation.
		this.pimpl.counter.enableNotify(false);

		// Note that if we have one child, the counting should go from -1
		// to 0 (so the child is toggled on and off).
		this.pimpl.counter.min.setValue((short)(lastchildidx > 0 ? 0 : SO_SWITCH_NONE));
		this.pimpl.counter.max.setValue((short)(lastchildidx >= 0 ? lastchildidx : SO_SWITCH_NONE));

		// To avoid SoSwitch getting an out-of-range whichChild value, in
		// case whichChild was at the end.
		if (lastchildidx < this.whichChild.getValue()) {
			this.pimpl.counter.reset.setValue((short)lastchildidx);
			this.whichChild.setDirty(true); // Force evaluate() on the field.
		}
		this.pimpl.counter.enableNotify(true);
	}

		super.notify(nl);
	}


}
