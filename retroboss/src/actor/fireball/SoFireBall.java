package actor.fireball;

import application.actor.Actor;
import application.objects.Hero;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.*;

public class SoFireBall implements Actor {

    SoSeparator root = new SoSeparator();
    SoTranslation position = new SoTranslation();
    SbVec3f speed;
    SoMaterial fireBallColor = new SoMaterial();
    SoSphere fireBall = new SoSphere();
    
    boolean dead;

    public SoFireBall(SbVec3f speed, SbVec3f initialPosition) {
        root.addChild(position);
        fireBallColor.diffuseColor.setValue(0,0,0);
        fireBallColor.ambientColor.setValue(0,0,0);
        fireBallColor.specularColor.setValue(0,0,0);
        fireBallColor.emissiveColor.setValue(1,1,0);
        root.addChild(fireBallColor);
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
    	
    		if (dead) {
    			return;
    		}
        setPosition(getPosition().operator_add(speed.operator_mul(dt)));

        Hero hero = sceneGraph.getHero();
        SbVec3f heroPosition = hero.getPosition();
        
        SbVec3f delta = heroPosition.operator_minus(getPosition());
        if (delta.length() < 0.5f) {
            hero.life -= dt/10.0f;
            hero.hurting = true;
            if (hero.life < 0) {
                hero.life = 0;
            }
        }
    }
	@Override
	public void kill() {
		dead = true;
		
	}
}
