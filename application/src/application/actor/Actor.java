package application.actor;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.SoNode;

public interface Actor {
    SoNode getNode();
    void setPosition(SbVec3f position);
    void onIdle(float dt, SceneGraphIndexedFaceSetShader sceneGraph);
}
