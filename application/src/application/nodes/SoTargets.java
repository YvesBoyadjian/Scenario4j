/**
 * 
 */
package application.nodes;

import application.objects.Target;
import jscenegraph.coin3d.inventor.SbBSPTree;
import jscenegraph.coin3d.inventor.VRMLnodes.SoVRMLBillboard;
import jscenegraph.coin3d.inventor.lists.SbListInt;
import jscenegraph.database.inventor.SbSphere;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.elements.SoCacheElement;
import jscenegraph.database.inventor.misc.SoChildList;
import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.*;
import org.lwjgl.system.CallbackI;

import java.util.*;

/**
 * @author Yves Boyadjian
 *
 */
public class SoTargets extends SoSeparator {

	private static final float TWO_KILOMETERS = 2000;

	private Target target;
	private SbVec3f referencePoint;
	private SbVec3f cameraDirection;

	private final SbBSPTree bspTree = new SbBSPTree();
	private final SbSphere nearSphere = new SbSphere();
	private final SbListInt nearIDS = new SbListInt();

	private final Set<Integer> actualChildren = new HashSet<>();
	private final Set<Integer> nearChildren = new HashSet<>();

	private final Map<Integer,SoTarget> idxToTargets = new HashMap<>();

//	private final List<SoTarget> targets = new ArrayList<>();

	public SoTargets(Target target) {
		super();
		renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
		this.target = target;
		this.target.setGraphicObject(this);
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

	// Adds a child as last one in group.
	/*
	public void addChild(SoNode child) {

		if ( child instanceof SoTarget) {
			child.ref();
			bspTree.addPoint(((SoTarget)child).getCoordinates(),child);
			targets.add((SoTarget)child);
		}
		else {
			super.addChild(child);
		}
	}
*/
	void update_children_list() {

		nearSphere.setValue(referencePoint.operator_add(cameraDirection.operator_mul(target.getViewDistance()*0.8f)), target.getViewDistance());

		nearIDS.truncate(0);
		bspTree.findPoints(nearSphere,nearIDS);

		nearChildren.clear();

		int nbIDS = nearIDS.size();
		for( int i=0;i<nbIDS;i++) {
			int id = nearIDS.get(i);
			if( !actualChildren.contains(id)) {
				int instance = (Integer)bspTree.getUserData(id);
//				SoTarget child = (SoTarget)bspTree.getUserData(id);
				SoTarget targetSeparator = new SoTarget(instance);
				targetSeparator.ref();
				//sealSeparator.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);

				SoTranslation targetTranslation = new SoTranslation();
				targetTranslation.enableNotify(false); // Will change often

				final SbVec3f targetPosition = new SbVec3f();
				int index = target.indexOfInstance(instance);
				final float[] vector = new float[3];
				targetPosition.setValue(target.getTarget(index, vector));
				targetPosition.setZ(targetPosition.getZ() + 0.3f);

				targetTranslation.translation.setValue(targetPosition);

				targetSeparator.addChild(targetTranslation);

				SoVRMLBillboard billboard = new SoVRMLBillboard();
				//billboard.axisOfRotation.setValue(0, 1, 0);
				if (target.isShot(instance)) {
					SoMaterial c = new SoMaterial();
					c.diffuseColor.setValue(1,0,0);
					billboard.addChild(c);
					target.setGroup(billboard,instance);
				}

				SoCube targetCube = new SoCube();
				targetCube.height.setValue(target.getSize());
				targetCube.width.setValue(target.getRatio() * targetCube.height.getValue());
				targetCube.depth.setValue(0.1f);

				billboard.addChild(targetCube);

				targetSeparator.addChild(billboard);
				addTarget(targetSeparator, id);
				targetSeparator.unref();
			}
			nearChildren.add(id);
		}

		final Set<Integer> actualChildrenSaved = new HashSet<>();
		actualChildrenSaved.addAll(actualChildren);
		for( int id : actualChildrenSaved) {
			if(actualChildren.contains(id) && !nearChildren.contains(id)) {
				//SoTarget child = (SoTarget)bspTree.getUserData(id);
				SoTarget child = idxToTargets.get(id);
				removeTarget(child,id);
			}
		}

	}

	void addTarget(SoTarget target, int id) {
		actualChildren.add(id);
		super.addChild(target);
		idxToTargets.put(id,target);
		//target.unref();
	}

	void removeTarget(SoTarget target, int id) {
		actualChildren.remove(id);
		idxToTargets.remove(id);
		//target.ref();
		super.removeChild(target);
	}

	public Target getTarget() {
		return target;
	}

	public SoTarget getTargetChildFromInstance(int instance) {
		int index = target.indexOfInstance(instance);
		return idxToTargets.get(index);
	}

	public Collection<SoTarget> getNearChildren() {
		Collection<SoTarget> nearChildren = new ArrayList<>();
		for(int id : actualChildren) {
			//SoTarget child = (SoTarget)bspTree.getUserData(id);
			SoTarget child = idxToTargets.get(id);
			nearChildren.add(child);
		}
		return nearChildren;
	}
}
