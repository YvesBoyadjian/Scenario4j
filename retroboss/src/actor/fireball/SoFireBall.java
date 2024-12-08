package actor.fireball;

import application.actor.Actor;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.database.inventor.nodes.SoSphere;
import jscenegraph.database.inventor.nodes.SoTranslation;

public class SoFireBall implements Actor {

    SoSeparator root = new SoSeparator();
    SoTranslation position = new SoTranslation();
    SbVec3f speed;
    SoSphere fireBall = new SoSphere();

    public SoFireBall(SbVec3f speed, SbVec3f initialPosition) {
        root.addChild(position);
        root.addChild(fireBall);
        fireBall.radius.setValue(0.2f);
        setPosition(initialPosition);
        this.speed = speed;
    }
    @Override
    public SoNode getNode() {
        return root;
    }

    @Override
    public void setPosition(SbVec3f position) {
        this.position.translation.setValue(position);
    }

    public SbVec3f getPosition() {
        return position.translation.getValue();
    }

    @Override
    public void onIdle(float dt, SceneGraphIndexedFaceSetShader sceneGraph) {
        setPosition(getPosition().operator_add(speed.operator_mul(dt)));
    }
}
