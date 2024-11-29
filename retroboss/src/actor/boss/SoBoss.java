package actor.boss;

import application.actor.Actor;
import application.objects.Hero;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.database.inventor.SbRotation;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.*;

import java.io.File;

public class SoBoss implements Actor {

    SoSeparator root = new SoSeparator();
    SoTranslation position = new SoTranslation();
    SoRotation orientation = new SoRotation();
    SoRotation rotation = new SoRotation();
    SoFile file = new SoFile();

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
        Hero hero = sceneGraph.getHero();
        SbVec3f heroPosition = hero.getPosition();

        SbVec3f delta = heroPosition.operator_minus(position.translation.getValue());

        double desiredAngle = Math.atan2(delta.y(), delta.x()) + Math.PI/2;

        float previousAngle = getOrientation();

        double deltaAngle = (desiredAngle - previousAngle + 4*Math.PI) % (2*Math.PI);

        if (deltaAngle > Math.PI) {
            deltaAngle = deltaAngle - 2*Math.PI;
        }

        setOrientation(previousAngle+Math.min(1d/100d, Math.abs(deltaAngle))*Math.signum(deltaAngle));
    }
}
