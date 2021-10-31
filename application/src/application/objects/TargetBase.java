package application.objects;

import jscenegraph.database.inventor.nodes.SoGroup;

import java.util.HashMap;
import java.util.Map;

public class TargetBase {
    private final Map<Integer,SoGroup> groups = new HashMap<>();

    public void setGroup(SoGroup group, int instance) {
        this.groups.put(instance, group);
    }

    public void resurrect(int instance) {
        SoGroup group = groups.get(instance);
        group.removeChild(0);
    }
}
