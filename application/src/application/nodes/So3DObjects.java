package application.nodes;

import application.objects.collectible.Collectible;
import jscenegraph.coin3d.inventor.SbBSPTree;
import jscenegraph.coin3d.inventor.lists.SbListInt;
import jscenegraph.database.inventor.SbSphere;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.elements.SoCacheElement;
import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.database.inventor.nodes.SoTranslation;

import java.util.*;

public class So3DObjects extends SoSeparator {

    private Collectible collectible;
    private SbVec3f referencePoint;
    private SbVec3f cameraDirection;

    private final SbBSPTree bspTree = new SbBSPTree();
    private final SbSphere nearSphere = new SbSphere();
    private final SbListInt nearIDS = new SbListInt();

    private final Set<Integer> actualChildren = new HashSet<>();
    private final Set<Integer> nearChildren = new HashSet<>();

    private final Map<Integer, So3DObject> idxToCollectibles = new HashMap<>();

    private float nearestCollectibleDistance = 99;

    public So3DObjects(Collectible collectible) {
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

    /*
    register the member
     */
    public void addMember(SbVec3f collectiblePosition, int instance) {
        bspTree.addPoint(collectiblePosition,instance);
    }

    void update_children_list() {

        nearSphere.setValue(referencePoint.operator_add(cameraDirection.operator_mul(collectible.getViewDistance()*0.0f)), collectible.getViewDistance());

        nearIDS.truncate(0);
        bspTree.findPoints(nearSphere,nearIDS);

        nearChildren.clear();

        nearestCollectibleDistance = 99;

        int nbIDS = nearIDS.size();
        for( int i=0;i<nbIDS;i++) {
            int id = nearIDS.get(i);
            if( !actualChildren.contains(id)) {
                int instance = (Integer)bspTree.getUserData(id);

                So3DObject collectibleSeparator = new So3DObject(instance);
                collectibleSeparator.ref();

                SoTranslation collectibleTranslation = new SoTranslation();
                collectibleTranslation.enableNotify(false); // Will change often

                final SbVec3f collectiblePosition = new SbVec3f();
                int index = collectible.indexOfInstance(instance);
                final float[] vector = new float[3];
                collectiblePosition.setValue(collectible.getCollectible(index, vector));
                collectibleTranslation.translation.setValue(collectiblePosition);

                collectibleSeparator.addChild(collectibleTranslation);

                SoNode collectibleNode = collectible.getNode();

                collectibleSeparator.addChild(collectibleNode);

                addCollectible(collectibleSeparator, id);
                collectibleSeparator.unref();
            }
            nearChildren.add(id);
        }

        final Set<Integer> actualChildrenSaved = new HashSet<>();
        actualChildrenSaved.addAll(actualChildren);
        for( int id : actualChildrenSaved) {
            if(actualChildren.contains(id) && !nearChildren.contains(id)) {
                So3DObject child = idxToCollectibles.get(id);
                removeCollectible(child,id);
            }
        }
        for(int id : actualChildren) {
            So3DObject child = idxToCollectibles.get(id);
            float distance = referencePoint.operator_minus(child.getCoordinates()).length();
            nearestCollectibleDistance = Math.min(nearestCollectibleDistance,distance);
        }
        //System.out.println(nearestCollectibleDistance);
    }

    void addCollectible(So3DObject target, int id) {
        actualChildren.add(id);
        super.addChild(target);
        idxToCollectibles.put(id,target);
//        target.unref();
    }

    void removeCollectible(So3DObject target, int id) {
        actualChildren.remove(id);
        idxToCollectibles.remove(id);
//        target.ref();
        super.removeChild(target);
    }

    public Collectible getCollectible() {
        return collectible;
    }

    public Collection<So3DObject> getNearChildren() {
        Collection<So3DObject> nearChildren = new ArrayList<>();
        for(int id : actualChildren) {
            So3DObject child = idxToCollectibles.get(id);
            nearChildren.add(child);
        }
        return nearChildren;
    }

    public float getNearestCollectibleDistance() {
        return nearestCollectibleDistance;
    }
}
