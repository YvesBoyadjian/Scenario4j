/**
 * 
 */
package application.targets;

import java.util.Random;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

import application.objects.Target;
import application.objects.TargetBase;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.database.inventor.SbBox3f;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.fields.SoMFVec3f;

/**
 * @author Yves Boyadjian
 *
 */
public class Seals extends TargetBase implements Target {

	public static final String SEAL_NAME = "Seal";

	final int THREE_HUNDRED_THOUSAND = 300000;
	int NB_SEAL_BIRTHS = THREE_HUNDRED_THOUSAND;
	
	final static int SEED_SEAL_PLACEMENT = 48;
	
	SceneGraphIndexedFaceSetShader sg;
	 
	int nbSeals = 0;
	
	SoMFVec3f sealCoords = new SoMFVec3f();
	
	public Seals( SceneGraphIndexedFaceSetShader sg ) {
		this.sg = sg;
	}

	public int getNbSeals() {
		
		if(nbSeals == 0) {
			compute();
		}
		
		return nbSeals;
	}

	public float[] getSeal(int sealIndex, float[] vector) {
		
		if(nbSeals == 0) {
			compute();
		}
		
		SbVec3f oneSealCoords = sealCoords.getValueAt(sealIndex);
		vector[0] = oneSealCoords.getX();
		vector[1] = oneSealCoords.getY();
		vector[2] = oneSealCoords.getZ();
		return vector;
	}
	
	private void compute() {

		RandomGenerator randomPlacementSeals = new SplittableRandom(SEED_SEAL_PLACEMENT);
		
		int[] indices = new int[4];
		
		float zWater =  - 150 + sg.getzTranslation() - sg.CUBE_DEPTH /2;
		
		float[] xyz = new float[3];
		int start;

		for( int i = 0; i < NB_SEAL_BIRTHS; i++) {
			float x = getRandomX(randomPlacementSeals);
			float y = getRandomY(randomPlacementSeals);
			float z = sg.getInternalZ(x,y,indices,true) + sg.getzTranslation();
			
			boolean isNearWater = Math.abs(z - zWater) < 5;
			boolean isAboveWater = z > zWater;
			
			float z1 = sg.getInternalZ(x+0.5f,y,indices,true) + sg.getzTranslation();
			float z2 = sg.getInternalZ(x-0.5f,y,indices,true) + sg.getzTranslation();
			float z3 = sg.getInternalZ(x,y+0.5f,indices,true) + sg.getzTranslation();
			float z4 = sg.getInternalZ(x,y-0.5f,indices,true) + sg.getzTranslation();
			float d1 = Math.abs(z-z1);
			float d2 = Math.abs(z-z2);
			float d3 = Math.abs(z-z3);
			float d4 = Math.abs(z-z4);
			float dzMax = 0.25f;
			boolean isNotTooSteep = (d1<dzMax) && (d2<dzMax) && (d3<dzMax) && (d4<dzMax); 
			
			if( isNearWater && isAboveWater && isNotTooSteep ) {
				xyz[0] = x;
				xyz[1] = y;
				xyz[2] = z + 0.3f;
				start = nbSeals;
				if (sealCoords.getNum() <= start) {
					sealCoords.setNum(1000+start);
				}
				sealCoords.setValues(start, xyz);

				addInstance(i);
				nbSeals++;
			}
		}
		sealCoords.setNum(nbSeals);
	}

	float getRandomX(RandomGenerator randomPlacementTrees) {
		SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
		float xMin = sceneBox.getBounds()[0];
		float xMax = sceneBox.getBounds()[3];
		return xMin + (xMax - xMin) * randomPlacementTrees.nextFloat();
	}
	
	float getRandomY(RandomGenerator randomPlacementTrees) {
		SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
		float yMin = sceneBox.getBounds()[1];
		float yMax = sceneBox.getBounds()[4];
		return yMin + (yMax - yMin) * randomPlacementTrees.nextFloat();
	}

	@Override
	public String targetName() {
		return SEAL_NAME;
	}

	@Override
	public String getTexturePath() {
		return "ressource/robbe-3080459.jpg";
	}

	@Override
	public int getNbTargets() {
		return getNbSeals();
	}

	@Override
	public float[] getTarget(int sealIndex, float[] vector) {
		return getSeal(sealIndex,vector);
	}

	@Override
	public float getSize() {
		return 1.5f;
	}

	@Override
	public float getRatio() {
		return 1920.0f / 1280.0f;
	}

	@Override
	public float getViewDistance() {
		return 2000;
	}
}
