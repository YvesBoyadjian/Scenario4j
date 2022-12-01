package application.nodes;

import application.objects.enemy.EnemyFamily;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
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

    private SceneGraphIndexedFaceSetShader sg;

    private EnemyFamily enemies;

    private SbVec3f referencePoint;
    //private SbVec3f cameraDirection;
    private final SbBSPTree bspTree = new SbBSPTree();
    private final SbSphere nearSphere = new SbSphere();
    private final Set<Integer> actualChildrenInstances = new HashSet<>();
    private final Map<Integer,SoPill> instancesToCollectibles = new HashMap<>();
    private float nearestCollectibleDistance = 99;

    private final SbVec3fSingle dummy = new SbVec3fSingle();

    private final SbVec3fSingle dummy2 = new SbVec3fSingle();

    private final SbVec3fSingle feetToCenter = new SbVec3fSingle(0,0,1.75f/2);

    private long nanoTime = 0;

    private final int[] indices = new int[8];

    public SoEnemies(EnemyFamily enemies, SceneGraphIndexedFaceSetShader sg) {
        super();
        renderCaching.setValue(CacheEnabled.OFF);
        this.enemies = enemies;
        this.sg = sg;
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

        final Set<Integer> nearChildrenInstances = new HashSet<>();
        nearChildrenInstances.clear();

        nearestCollectibleDistance = 99;

        final int nbIDS = nearIDS.size();
        for( int i=0;i<nbIDS;i++) {
            final int instance = (Integer)bspTree.getUserData(nearIDS.get(i));
            if( !actualChildrenInstances.contains(instance)) {
                int index = enemies.indexOfInstance(instance);
                if (index != -1 && !enemies.isKilled(index)) {

                    SoPill pill = new SoPill(instance);
                    pill.ref();

                    //SoTranslation collectibleTranslation = new SoTranslation();
                    //collectibleTranslation.enableNotify(false); // Will change often

                    final SbVec3f collectiblePosition = new SbVec3f();
                    final float[] vector = new float[3];
                    collectiblePosition.setValue(enemies.getEnemy(index, vector));
                    pill.position.translation.setValue(collectiblePosition);

                    //collectibleSeparator.addChild(collectibleTranslation);

                    //SoNode collectibleNode = enemies.getNode();

                    //collectibleSeparator.addChild(collectibleNode);

                    addEnemy(pill, instance);
                    pill.unref();
                }
            }
            nearChildrenInstances.add(instance);
        }

        final Set<Integer> actualChildrenSaved = new HashSet<>();
        actualChildrenSaved.addAll(actualChildrenInstances);
        for( int instance : actualChildrenSaved) {
            if(actualChildrenInstances.contains(instance) && !nearChildrenInstances.contains(instance)) {
                SoPill child = instancesToCollectibles.get(instance);
                removeEnemy(child,instance);
            }
        }
        for(int instance : actualChildrenInstances) {
            SoPill child = instancesToCollectibles.get(instance);
            float distance = referencePoint.operator_minus(child.getCoordinates()).length();
            nearestCollectibleDistance = Math.min(nearestCollectibleDistance,distance);
        }
        //System.out.println(nearestCollectibleDistance);
    }
    void addEnemy(SoPill target, int instance) {
        actualChildrenInstances.add(instance);
        super.addChild(target);
        instancesToCollectibles.put(instance,target);
//        target.unref();
        //System.out.println("Add enemy "+actualChildren.size());
    }
    void removeEnemy(SoPill target, int instance) {
        actualChildrenInstances.remove(instance);
        instancesToCollectibles.remove(instance);
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

        for(Integer instance : actualChildrenInstances) {
            SoPill pill = instancesToCollectibles.get(instance);
            SbVec3f pillPosition = pill.position.translation.getValue();
            SbVec3f direction = referencePoint.operator_minus(pillPosition.operator_minus(feetToCenter,dummy2), dummy);
            final float distance = direction.length(); 
            if ( distance <= 0.8f || distance > 80) {
                continue;
            }
            direction.normalize();
            pill.position.translation.setValue(pillPosition.operator_add(direction.operator_mul(deltaT*speed)));
            pillPosition.setZ(sg.getInternalZ(pillPosition.getX(),pillPosition.getY(),indices,true)+sg.getzTranslation() + 1.75f/2 - 0.03f);
        }
    }

    public void kill(SoPill target) {
        final int instance = target.getInstance();
        removeEnemy(target,instance);
        enemies.kill(instance);
    }
}
