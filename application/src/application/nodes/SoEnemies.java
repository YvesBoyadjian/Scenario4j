package application.nodes;

import application.objects.enemy.EnemyFamily;
import jscenegraph.coin3d.inventor.SbBSPTree;
import jscenegraph.coin3d.inventor.lists.SbListInt;
import jscenegraph.database.inventor.SbSphere;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SbVec3fSingle;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.elements.SoCacheElement;
import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.database.inventor.nodes.SoTranslation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoEnemies extends SoSeparator {

    private EnemyFamily enemies;

    private SbVec3f referencePoint;
    //private SbVec3f cameraDirection;
    private final SbBSPTree bspTree = new SbBSPTree();
    private final SbSphere nearSphere = new SbSphere();
    private final Set<Integer> actualChildren = new HashSet<>();
    private final Map<Integer,SoPill> idxToCollectibles = new HashMap<>();
    private float nearestCollectibleDistance = 99;

    private final SbVec3fSingle dummy = new SbVec3fSingle();

    private final SbVec3fSingle dummy2 = new SbVec3fSingle();

    private final SbVec3fSingle feetToCenter = new SbVec3fSingle(0,0,1.75f/2);

    private long nanoTime = 0;

    public SoEnemies(EnemyFamily enemies) {
        super();
        renderCaching.setValue(CacheEnabled.OFF);
        this.enemies = enemies;
    }
    public void setReferencePoint(SbVec3f referencePoint) {
        this.referencePoint = referencePoint;
    }
    public void
    GLRenderBelowPath(SoGLRenderAction action)

    ////////////////////////////////////////////////////////////////////////
    {
        update_children_list();

        update_children_position();

        SoState state = action.getState();

        // never cache this node
        SoCacheElement.invalidate(state);

        super.GLRenderBelowPath(action);
    }

    /*
    register the member
     */
    public void addMember(SbVec3f enemyPosition, int instance) {
        bspTree.addPoint(enemyPosition,instance);
    }

    void update_children_list() {

        nearSphere.setValue(referencePoint, 250);

        final SbListInt nearIDS = new SbListInt();
        nearIDS.truncate(0);
        bspTree.findPoints(nearSphere,nearIDS);

        final Set<Integer> nearChildren = new HashSet<>();
        nearChildren.clear();

        nearestCollectibleDistance = 99;

        int nbIDS = nearIDS.size();
        for( int i=0;i<nbIDS;i++) {
            int id = nearIDS.get(i);
            if( !actualChildren.contains(id)) {
                int instance = (Integer)bspTree.getUserData(id);

                SoPill pill = new SoPill();
                pill.ref();

                //SoTranslation collectibleTranslation = new SoTranslation();
                //collectibleTranslation.enableNotify(false); // Will change often

                final SbVec3f collectiblePosition = new SbVec3f();
                int index = enemies.indexOfInstance(instance);
                final float[] vector = new float[3];
                collectiblePosition.setValue(enemies.getEnemy(index, vector));
                pill.position.translation.setValue(collectiblePosition);

                //collectibleSeparator.addChild(collectibleTranslation);

                //SoNode collectibleNode = enemies.getNode();

                //collectibleSeparator.addChild(collectibleNode);

                addEnemy(pill, id);
                pill.unref();
            }
            nearChildren.add(id);
        }

        final Set<Integer> actualChildrenSaved = new HashSet<>();
        actualChildrenSaved.addAll(actualChildren);
        for( int id : actualChildrenSaved) {
            if(actualChildren.contains(id) && !nearChildren.contains(id)) {
                SoPill child = idxToCollectibles.get(id);
                removeEnemy(child,id);
            }
        }
        for(int id : actualChildren) {
            SoPill child = idxToCollectibles.get(id);
            float distance = referencePoint.operator_minus(child.getCoordinates()).length();
            nearestCollectibleDistance = Math.min(nearestCollectibleDistance,distance);
        }
        //System.out.println(nearestCollectibleDistance);
    }
    void addEnemy(SoPill target, int id) {
        actualChildren.add(id);
        super.addChild(target);
        idxToCollectibles.put(id,target);
//        target.unref();
        //System.out.println("Add enemy "+actualChildren.size());
    }
    void removeEnemy(SoPill target, int id) {
        actualChildren.remove(id);
        idxToCollectibles.remove(id);
//        target.ref();
        super.removeChild(target);
    }

    private void update_children_position() {

        long now = System.nanoTime();
        float deltaT = (now - nanoTime)*1.0e-9f;
        float speed = 1.5f;
        if (nanoTime == 0) {
            nanoTime = now;
            return;
        }
        nanoTime = now;

        for(Integer id : actualChildren) {
            SoPill pill = idxToCollectibles.get(id);
            SbVec3f pillPosition = pill.position.translation.getValue();
            SbVec3f direction = referencePoint.operator_minus(pillPosition.operator_minus(feetToCenter,dummy2), dummy);
            if (direction.length() <= 0.8f) {
                continue;
            }
            direction.normalize();
            pill.position.translation.setValue(pillPosition.operator_add(direction.operator_mul(deltaT*speed)));
        }
    }
}
