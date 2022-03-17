package application.objects;

import application.nodes.SoTargets;
import jscenegraph.database.inventor.nodes.SoGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetBase {
    private final Map<Integer,SoGroup> groups = new HashMap<>();
    private final List<Integer> instances = new ArrayList<>();
    private SoTargets graphicObject;

    public void setGroup(SoGroup group, int instance) {
        this.groups.put(instance, group);
    }

    public void resurrect(int instance) {
        SoGroup group = groups.get(instance);
        group.removeChild(0);
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
}
