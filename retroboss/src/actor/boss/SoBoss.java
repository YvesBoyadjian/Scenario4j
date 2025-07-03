package actor.boss;

import actor.fireball.SoFireBall;
import application.actor.Actor;
import application.actor.SoActorFile;
import application.objects.Hero;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.database.inventor.SbRotation;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoBoss implements Actor {

    SoSeparator root = new SoSeparator();
    SoTranslation position = new SoTranslation();
    SoRotation orientation = new SoRotation();
    SoRotation rotation = new SoRotation();
    SoFile file = new SoActorFile(this);
    double time = 0;
    double timeFireBallThrown = 0;
    int fireBallNumber = 0;
    Map<String,SoFireBall> fireballs = new HashMap<>();
    
    private float life = 1;
    
    private boolean shot;
    
    double shotDz = 0;
    
    private boolean killed;

    public SoBoss() {
        root.addChild(position);

        root.addChild(orientation);

        rotation.rotation.setValue(new SbVec3f(1,0,0),(float)Math.PI);

        root.addChild(rotation);

        File bossFile = new File("ressource/halo_5_noble_armor_thingy/scene.gltf");

        if(!bossFile.exists()) {
            bossFile = new File("../ressource/halo_5_noble_armor_thingy/scene.gltf");
        }
        if(!bossFile.exists()) {
            bossFile = new File("../../ressource/halo_5_noble_armor_thingy/scene.gltf");
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

    public void setOrientation(double angle) {
        this.orientation.rotation.setValue(new SbVec3f(0,0,1), (float)angle);
    }

    public float getOrientation() {
        SbVec3f axis = new SbVec3f(0,0,1);
        float angle = orientation.rotation.getValue(axis);
        return axis.getZ() > 0 ? angle : -angle;
    }

    @Override
    public void onIdle(float dt, SceneGraphIndexedFaceSetShader sceneGraph) {
    	
		if(killed) {
			return;
		}
    	
    		if(shot) {
    			shotDz += dt;
    			setPosition(getPosition().operator_add(new SbVec3f(0,0, - dt)));
    			if (shotDz > 10) {
    				sceneGraph.removeActor("boss");
    				return;
    			}
    		}

        time+=dt;

        Hero hero = sceneGraph.getHero();
        SbVec3f heroPosition = hero.getPosition();

        SbVec3f delta = heroPosition.operator_minus(position.translation.getValue());
        
        if (delta.length() < 60) {

        double desiredAngle = Math.atan2(delta.y(), delta.x()) + Math.PI/2;

        float previousAngle = getOrientation();

        double deltaAngle = (desiredAngle - previousAngle + 4*Math.PI) % (2*Math.PI);

        if (deltaAngle > Math.PI) {
            deltaAngle = deltaAngle - 2*Math.PI;
        }

        setOrientation(previousAngle+Math.min(dt/10f, Math.abs(deltaAngle))*Math.signum(deltaAngle));

        if(time - timeFireBallThrown > 3d && Math.abs(deltaAngle)<0.1f) {
            timeFireBallThrown = time;
            fireBallNumber++;

            SbVec3f initialPosition = position.translation.getValue().operator_add(new SbVec3f(0,0,5.5f));
            SbVec3f finalPosition = heroPosition.operator_minus(new SbVec3f(0,0,0.05f));

            delta = finalPosition.operator_minus(initialPosition);

            delta.normalize();
            SbVec3f speed = delta.operator_mul(5f);
            SoFireBall fireball = new SoFireBall(speed, initialPosition);
            String fireballKey = "FireBall" + fireBallNumber; 
            fireballs.put(fireballKey, fireball);
            sceneGraph.addActor("FireBall" + fireBallNumber, fireball);
        }
        }
        for (String fireballKey : new HashSet<>(fireballs.keySet())) {
        		SoFireBall fireball = fireballs.get(fireballKey);
        		float distance = fireball.getPosition().operator_minus(getPosition()).length();
        		if (distance > 99) {
        			fireballs.remove(fireballKey);
        			sceneGraph.removeActor(fireballKey);
        		}
        }
    }

	@Override
	public SbVec3f getPosition() {
		return position.translation.getValue();
	}

	@Override
	public void kill(SceneGraphIndexedFaceSetShader sceneGraph) {
		killed = true;
        for (String fireballKey : new HashSet<>(fireballs.keySet())) {
    		SoFireBall fireball = fireballs.get(fireballKey);
    			fireballs.remove(fireballKey);
    			sceneGraph.removeActor(fireballKey);
    }
	}

	@Override
	public void shoot(SceneGraphIndexedFaceSetShader sceneGraph) {
		life -= 0.1f;
		
		if (life <= 0) {
			shot = true;
		}
	}
}
