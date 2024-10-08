/**
 * 
 */
package application.scenegraph;

import java.util.function.Supplier;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.SoCamera;
import jscenegraph.database.inventor.nodes.SoNode;

/**
 * @author Yves Boyadjian
 *
 */
public interface SceneGraph extends HeightProvider {

	SoNode getSceneGraph();

	float getCenterY();

	float getCenterX();
	
	void setPosition(float x, float y);

	void setSunPosition(SbVec3f sunPosition);
	
	void preDestroy();

	void setCamera(Supplier<SoCamera> camera);

	void idle();
}
