package application.objects;

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
}
