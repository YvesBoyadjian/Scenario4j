package application.nodes;

import application.objects.Target;
import application.objects.collectible.Collectible;
import jscenegraph.coin3d.inventor.SbBSPTree;
import jscenegraph.coin3d.inventor.lists.SbListInt;
import jscenegraph.database.inventor.SbSphere;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SbViewportRegion;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.actions.SoGetBoundingBoxAction;
import jscenegraph.database.inventor.elements.SoCacheElement;
import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSeparator;

import java.util.*;

public class SoCollectibles extends SoSeparator {

    private Collectible collectible;
    private SbVec3f referencePoint;
    private SbVec3f cameraDirection;

    private final SbBSPTree bspTree = new SbBSPTree();

    private final SbSphere nearSphere = new SbSphere();

    private final SbListInt nearIDS = new SbListInt();

    private final Set<Integer> actualChildren = new HashSet<>();

    private final Set<Integer> nearChildren = new HashSet<>();

    private final List<SoCollectible> collectibles = new ArrayList<>();

    private float nearestCollectibleDistance = 99;

    public SoCollectibles(Collectible collectible) {
        super();
        renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
        this.collectible = collectible;
        this.collectible.setGraphicObject(this);
    }

    public void
    GLRenderBelowPath(SoGLRenderAction action)

    ////////////////////////////////////////////////////////////////////////
    {
        update_children_list();

        SoState state = action.getState();

        // never cache this node
        SoCacheElement.invalidate(state);

        super.GLRenderBelowPath(action);
    }

    public void setReferencePoint(SbVec3f referencePoint) {
        this.referencePoint = referencePoint;
    }

    public void setCameraDirection(SbVec3f cameraDirection) {
        this.cameraDirection = cameraDirection;
    }

    // Adds a child as last one in group.
    public void addChild(SoNode child) {

        if ( child instanceof SoCollectible) {
            child.ref();
            bspTree.addPoint(((SoCollectible)child).getCoordinates(),child);
            collectibles.add((SoCollectible)child);
        }
        else {
            super.addChild(child);
        }
    }

    void update_children_list() {

        nearSphere.setValue(referencePoint.operator_add(cameraDirection.operator_mul(collectible.getViewDistance()*0.8f)), collectible.getViewDistance());

        nearIDS.truncate(0);
        bspTree.findPoints(nearSphere,nearIDS);

        nearChildren.clear();

        nearestCollectibleDistance = 99;

        int nbIDS = nearIDS.size();
        for( int i=0;i<nbIDS;i++) {
            int id = nearIDS.get(i);
            if( !actualChildren.contains(id)) {
                SoCollectible child = (SoCollectible)bspTree.getUserData(id);
                addCollectible(child, id);
            }
            nearChildren.add(id);
        }

        final Set<Integer> actualChildrenSaved = new HashSet<>();
        actualChildrenSaved.addAll(actualChildren);
        for( int id : actualChildrenSaved) {
            if(actualChildren.contains(id) && !nearChildren.contains(id)) {
                SoCollectible child = (SoCollectible)bspTree.getUserData(id);
                removeCollectible(child,id);
            }
        }
        for(int id : actualChildren) {
            SoCollectible child = (SoCollectible)bspTree.getUserData(id);
            float distance = referencePoint.operator_minus(child.getCoordinates()).length();
            nearestCollectibleDistance = Math.min(nearestCollectibleDistance,distance);
        }
        //System.out.println(nearestCollectibleDistance);
    }

    void addCollectible(SoCollectible target, int id) {
        actualChildren.add(id);
        super.addChild(target);
        target.unref();
    }

    void removeCollectible(SoCollectible target, int id) {
        actualChildren.remove(id);
        target.ref();
        super.removeChild(target);
    }

    public Collectible getCollectible() {
        return collectible;
    }

    public SoCollectible getCollectibleChildFromInstance(int instance) {
        int index = collectible.indexOfInstance(instance);
        return collectibles.get(index);
    }

    public Collection<SoCollectible> getNearChildren() {
        Collection<SoCollectible> nearChildren = new ArrayList<>();
        for(int id : actualChildren) {
            SoCollectible child = (SoCollectible)bspTree.getUserData(id);
            nearChildren.add(child);
        }
        return nearChildren;
    }

    public float getNearestCollectibleDistance() {
        return nearestCollectibleDistance;
    }
}
