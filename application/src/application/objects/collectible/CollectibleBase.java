package application.objects.collectible;

import application.nodes.SoCollectibles;
import application.nodes.SoTargets;
import jscenegraph.database.inventor.nodes.SoGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectibleBase {
    private final Map<Integer, SoGroup> groups = new HashMap<>();
    private final List<Integer> instances = new ArrayList<>();
    private SoCollectibles graphicObject;

    public void setGroup(SoGroup group, int instance) {
        this.groups.put(instance, group);
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

    public SoCollectibles getGraphicObject() {
        return graphicObject;
    }

    public void setGraphicObject(SoCollectibles graphicObject) {
        this.graphicObject = graphicObject;
    }
}
