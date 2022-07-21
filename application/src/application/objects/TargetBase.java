package application.objects;

import application.nodes.SoTargets;
import jscenegraph.database.inventor.nodes.SoGroup;

import java.util.*;

public class TargetBase {
    private final Map<Integer,SoGroup> groups = new HashMap<>();
    private final List<Integer> instances = new ArrayList<>();
    private final Set<Integer> shotInstances = new HashSet<>();
    private SoTargets graphicObject;

    public void setGroup(SoGroup group, int instance) {
        this.groups.put(instance, group);
    }

    public void resurrect(int instance) {
        SoGroup group = groups.get(instance);
        group.removeChild(0);
        shotInstances.remove(instance);
    }

    protected void addInstance(int instance) {
        instances.add(instance);
    }

    public int getInstance( int index) {
        return instances.get(index);
    }

    public int indexOfInstance(int instance) {
        return instances.indexOf(instance);
    }

    public SoTargets getGraphicObject() {
        return graphicObject;
    }

    public void setGraphicObject(SoTargets graphicObject) {
        this.graphicObject = graphicObject;
    }

    public boolean setShot(int instance) {
        return shotInstances.add(instance);
    }

    public boolean isShot(int instance) {
        return shotInstances.contains(instance);
    }
}
