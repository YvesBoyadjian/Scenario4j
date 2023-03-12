package jscenegraph.coin3d.inventor.nodes;

import jscenegraph.database.inventor.engines.SoCalculator;
import jscenegraph.database.inventor.engines.SoTimeCounter;
import jscenegraph.database.inventor.sensors.SoOneShotSensor;
import jscenegraph.database.inventor.sensors.SoSensor;
import jscenegraph.port.Destroyable;

public class SoBlinkerP implements Destroyable {
    public
    SoBlinkerP(SoBlinker master) {

    this.master = master;
    }

    static void whichChildCB(Object closure, SoSensor sensor) {
        SoBlinkerP thisp = (SoBlinkerP) closure;
        thisp.counter.reset.setValue((short)thisp.whichvalue);

        // if sensor/blinker isn't enabled, we need to manually set the whichChild field
        if (!thisp.counter.on.getValue()) {
            boolean old = thisp.master.whichChild.enableNotify(false);
            thisp.master.whichChild.setValue(thisp.whichvalue);
            thisp.master.whichChild.enableNotify(old);
        }
    }
    private SoBlinker master;
    int whichvalue;
    SoTimeCounter counter;
    SoCalculator calculator;
    SoOneShotSensor whichChildSensor;

    @Override
    public void destructor() {
        master = null;
        counter = null;
        calculator = null;
        whichChildSensor = null;
    }
}
