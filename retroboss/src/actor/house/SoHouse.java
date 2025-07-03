/**
 * 
 */
package actor.house;

import java.io.File;

import application.actor.Actor;
import application.actor.ThreeDObject;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.SoFile;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoRotation;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.database.inventor.nodes.SoTranslation;

/**
 * 
 */
public class SoHouse implements Actor {

    SoSeparator root = new SoSeparator();
    SoTranslation position = new SoTranslation();
    SoRotation orientation = new SoRotation();
    SoRotation rotation = new SoRotation();
    SoFile file = new SoFile();
    
    public SoHouse() {
        root.addChild(position);

        orientation.rotation.setValue(new SbVec3f(0,0,1), (float)Math.PI/2);

        root.addChild(orientation);

        rotation.rotation.setValue(new SbVec3f(1,0,0),(float)Math.PI/2);

        root.addChild(rotation);

        File bossFile = new File("ressource/Cottage_FREE.zip");

        if(!bossFile.exists()) {
            bossFile = new File("../ressource/Cottage_FREE.zip");
        }
        if(!bossFile.exists()) {
            bossFile = new File("../../ressource/Cottage_FREE.zip");
        }
        if (bossFile.exists()) {
            file.name.setValue(bossFile.getAbsolutePath());
            root.addChild(file);
        }
    }

	@Override
	public SoNode getNode() {
		return root;
	}

	@Override
	public void setPosition(SbVec3f position) {
        this.position.translation.setValue(position);
	}

	@Override
	public void onIdle(float dt, SceneGraphIndexedFaceSetShader sceneGraph) {
		// nothing to do on idle
		
	}

	@Override
	public SbVec3f getPosition() {
		return position.translation.getValue();
	}

	@Override
	public void kill(SceneGraphIndexedFaceSetShader sceneGraph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shoot(SceneGraphIndexedFaceSetShader sceneGraph) {
		// TODO Auto-generated method stub
		
	}

}
