/**
 * 
 */
package application.nodes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.coin3d.inventor.SoOffscreenRenderer;
import jscenegraph.coin3d.inventor.nodes.SoTexture;
import jscenegraph.coin3d.inventor.nodes.SoTexture2;
import jscenegraph.database.inventor.SbVec2s;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SbViewportRegion;
import jscenegraph.database.inventor.nodes.SoCube;
import jscenegraph.database.inventor.nodes.SoMaterial;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoRotation;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.database.inventor.nodes.SoTranslation;
import jscenegraph.port.memorybuffer.MemoryBuffer;

/**
 * 
 */
public class SoPillWithScreen extends SoPill {
	
	/**
	 * Definition of the look
	 */
	private final short LOOK_DEFINITION = 99;
	
	public final SoRotation rotation = new SoRotation();
	
	public final SoTexture2 image = new SoTexture2();
	
	private float orientation;
	
	private BufferedImage bi;

	/**
	 * @param instance
	 * @param pills
	 */
	public SoPillWithScreen(int instance, SoPills pills) {
		super(instance, pills);
		
		SoTranslation screenTranslation = new SoTranslation();
		
		screenTranslation.translation.setValue(0, 1.75f, 0);
		
		addChild(screenTranslation);
		
		addChild(rotation);
		
		SoSeparator screenSeparator = new SoSeparator();
		
		addChild(screenSeparator);
		
	    SoMaterial material = new SoMaterial();
	    material.diffuseColor.setValue(1,1,1);
	    
	    screenSeparator.addChild(material);
		
		screenSeparator.addChild(image);
		
		SoCube screen = new SoCube();
		
		screen.depth.setValue(0.01f);
		screen.height.setValue(0.8f);
		screen.width.setValue(0.8f);
		
		screenSeparator.addChild(screen);
		
		image.enableNotify(false);
		image.image.enableNotify(false);
	}

	public void setImage(BufferedImage image) {
		this.image.image.setValue(image, true);
	}
	
	public float getOrientation() {
		return orientation;
	}
	
	public void setOrientation(float orientation) {
		if (orientation > Math.PI * 2) {
			orientation -= Math.PI * 2;
		}
		if (orientation < - Math.PI * 2) {
			orientation += Math.PI * 2;
		}
		this.orientation = orientation;
		rotation.rotation.setValue(new SbVec3f(0f,1f,0f), orientation);
	}

	/**
	 * look
	 * @param sg
	 */
	public void look(SceneGraphIndexedFaceSetShader sg) {
		
//		if (true) {
//			return;
//		}
//		BufferedImage[] retVal = new BufferedImage[1];
//		if (loadBI()) {
//			setImage(bi);
//		}
		
		SbViewportRegion viewportregion = new SbViewportRegion(LOOK_DEFINITION, LOOK_DEFINITION);
		SoOffscreenRenderer offscreenRenderer = new SoOffscreenRenderer(viewportregion);
		
		SoNode scene = sg.getSceneGraph();
		
//		if (offscreenRenderer.render(scene)) {
////			MemoryBuffer b = MemoryBuffer.allocateFromByteArray(offscreenRenderer.getBuffer());
//////			image.image.setValue(new SbVec2s(LOOK_DEFINITION, LOOK_DEFINITION),3,true,b,true);
//		}
		
		offscreenRenderer.destructor();
	}
	
	private boolean loadBI() {
		if (bi == null) {
			try {
				bi = ImageIO.read(new File("C:\\Users\\yboya\\Pictures\\Top du top reduit\\20090522_162310.jpg"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
}
