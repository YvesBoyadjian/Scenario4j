package application.objects;

import application.nodes.SoTargets;
import jscenegraph.database.inventor.nodes.SoGroup;

public interface Target {

    String targetName();

    String getTexturePath();

    int getNbTargets();

    float[] getTarget(int sealIndex, float[] vector);

    float getSize();

    float getRatio();

    float getViewDistance();

    void setGroup(SoGroup group, int instance);

    void resurrect(int instance);

    int getInstance( int index);

    int indexOfInstance(int instance);

    SoTargets getGraphicObject();

    void setGraphicObject(SoTargets graphicObject);

    /*
    returns true if new shot
     */
    boolean setShot(int instance);

    boolean isShot(int instance);
}
