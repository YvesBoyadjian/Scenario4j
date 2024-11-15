package actor.boss;

import application.actor.Actor;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.*;

import java.io.File;

public class SoBoss implements Actor {

    SoSeparator root = new SoSeparator();
    SoTranslation position = new SoTranslation();
    SoRotation rotation = new SoRotation();
    SoFile file = new SoFile();

    public SoBoss() {
        root.addChild(position);

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
}
