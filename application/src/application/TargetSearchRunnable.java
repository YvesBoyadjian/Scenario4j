/**
 * 
 */
package application;

import application.nodes.SoEnemies;
import application.nodes.SoPill;
import application.nodes.SoTarget;
import application.nodes.SoTargets;
import application.objects.Target;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.viewer.glfw.SoQtWalkViewer;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SbViewportRegion;
import jscenegraph.database.inventor.SoPath;
import jscenegraph.database.inventor.SoPickedPoint;
import jscenegraph.database.inventor.actions.SoRayPickAction;
import jscenegraph.database.inventor.nodes.SoCube;
import jscenegraph.database.inventor.nodes.SoGroup;
import jscenegraph.database.inventor.nodes.SoMaterial;
import jscenegraph.database.inventor.nodes.SoNode;

import javax.swing.*;

/**
 * @author Yves Boyadjian
 *
 */
public class TargetSearchRunnable implements Runnable {
	
	SoQtWalkViewer v;
	SbViewportRegion vr;
	SoNode sg;
	SceneGraphIndexedFaceSetShader main;
	
	public TargetSearchRunnable(SoQtWalkViewer v, SbViewportRegion vr, SoNode sg, SceneGraphIndexedFaceSetShader main) {
		this.v = v;
		this.vr = vr;
		this.sg = sg;
		this.main = main;
	}

	@Override
	public void run() {
		Thread pickThread = new Thread() {
			public void run() {
				SoRayPickAction fireAction = new SoRayPickAction(vr);
				//fireAction.setRay(new SbVec3f(0.0f,0.0f,0.0f), new SbVec3f(0.0f,0.0f,-1.0f),0.1f,1000f);
				fireAction.setPoint(vr.getViewportSizePixels().operator_div(2));
				fireAction.setRadius(2.0f);
				fireAction.apply(sg);
				SoPickedPoint pp = fireAction.getPickedPoint();
				if( pp == null) {
					fireAction.destructor();
					return;
				}
				SwingUtilities.invokeLater(()-> {
					SoPath pat = pp.getPath();
					if( pat != null) {
						SoNode n = pat.getTail();
						final int len = pat.getLength();
						if( n.isOfType(SoCube.getClassTypeId())) {
							if( len > 3) {
								SoNode cube_parent = pat.getNode(len-2);
								SoNode maybe_targets = pat.getNode(len-4);
								if(cube_parent.isOfType(SoGroup.getClassTypeId()) &&
								maybe_targets instanceof SoTargets) {
									// hit one
									v.addOneShotIdleListener((viewer1)->{
										SoGroup g = (SoGroup)cube_parent;

										SoTargets targets = (SoTargets) maybe_targets;
										Target t = targets.getTarget();
										SoTarget targetNode = (SoTarget) pat.getNode(len-3);

										final int instance = targetNode.getInstance();
										if(!main.isShot(t,instance)) {
											Target targetFamily = targets.getTarget();
											targetFamily.setShot(instance);

											SoMaterial c = new SoMaterial();
											c.diffuseColor.setValue(1, 0, 0);
											g.enableNotify(false);
											g.insertChild(c, 0);
											g.enableNotify(true);
											t.setGroup(g,targetNode.getInstance());

											main.shootTarget(t, targetNode.getInstance());
											main.showTarget(t);

											main.getHero().life += 0.1f;
											if (main.getHero().life > 1.0f) {
												main.getHero().life = 1.0f;
											}

											SbVec3f pickedPoint = pp.getPoint();
											SbVec3f hero = main.getPosition();
											float distance = hero.operator_minus(pickedPoint).length();
											if(distance > 120) {
												String[] message = new String[1];
												message[0] = "NICE SHOT!";
												main.displayTemporaryMessage(message, 3);
											}
										}
									});
								}
							}
							//System.out.println(pp.getPath().getTail().getClass());
						}
						else if(len >= 4) {
							SoNode maybePill = pat.getNode(len-3);
							SoNode maybeEnemies = pat.getNode(len-4);
							if (maybePill instanceof SoPill && maybeEnemies instanceof SoEnemies) {
								SoPill pill = (SoPill)maybePill;
								SoEnemies enemies = (SoEnemies) maybeEnemies;
								enemies.kill(pill);
							}
						}
					}
					fireAction.destructor();
				});
			}
		};
		//pickThread.start();
		pickThread.run(); // No need now to do multithreading
	}

}
