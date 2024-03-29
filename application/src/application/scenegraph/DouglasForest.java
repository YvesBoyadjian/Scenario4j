/**
 * 
 */
package application.scenegraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.MainGLFW;
import application.objects.DouglasFir;
import application.scenegraph.douglas.DouglasParameters;
import jscenegraph.coin3d.inventor.nodes.SoVertexProperty;
import jscenegraph.database.inventor.SbBox3f;
import jscenegraph.database.inventor.SbColor;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.actions.SoAction;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.nodes.SoGroup;
import jscenegraph.database.inventor.nodes.SoIndexedFaceSet;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.database.inventor.nodes.SoShapeHints;

import javax.swing.*;

/**
 * @author Yves Boyadjian
 *
 */
public class DouglasForest {

	final static float DOUGLAS_CHUNK_WIDTH = 1000;

	final static int FOUR_MILLION = 4000000;
	
	private final int NB_DOUGLAS_SEEDS = FOUR_MILLION;
	
	final static int SEED_PLACEMENT_TREES = 42;
	final static int SEED_HEIGHT_TREES = 43;
	final static int SEED_ANGLE_TREES = 44;
	final static int SEED_WIDTH_TOP_TREES = 45;
	final static int SEED_WIDTH_BOTTOM_TREES = 46;
	final static int SEED_COLOR_MULTIPLIER = 47;
	final static int SEED_LEAN_ANGLE_TREES = 48;
	final static int SEED_LEAN_DIRECTION_ANGLE_TREES = 49;
	final static int SEED_BASE_TREES = 50;

	private List<DouglasParameters> douglasParams = new ArrayList<>();
/*
	private float[] xArray = new float[NB_DOUGLAS_SEEDS];
	private float[] yArray = new float[NB_DOUGLAS_SEEDS];
	private float[] zArray = new float[NB_DOUGLAS_SEEDS];
	private float[] heightArray = new float[NB_DOUGLAS_SEEDS];
	private float[] angleDegree1 = new float[NB_DOUGLAS_SEEDS];
	private float[] randomTopTree = new float[NB_DOUGLAS_SEEDS];
	private float[] randomBottomTree = new float[NB_DOUGLAS_SEEDS];
	private float[] randomBaseTree = new float[NB_DOUGLAS_SEEDS];
	private int[] randomColorMultiplierTree = new int[NB_DOUGLAS_SEEDS];
	private float[] randomLeanAngleTree = new float[NB_DOUGLAS_SEEDS];
	private float[] randomLeanDirectionAngleTree = new float[NB_DOUGLAS_SEEDS];
*/
	SceneGraphIndexedFaceSetShader sg;
	
	private List<List<DouglasChunk>> douglasChunks = new ArrayList<>(); // X, then Y
	private List<SbBox3f> xLimits = new ArrayList<>();
	
	private int nbDouglas = 0;

	public final ExecutorService es = Executors.newSingleThreadExecutor();
	
	public DouglasForest( SceneGraphIndexedFaceSetShader sg ) {
		this.sg = sg;
	}

	public int getNumIndexDouglas() {
		return nbDouglas/*NB_DOUGLAS_SEEDS*/;
	}

	public float getX(int tree_index) {
		DouglasParameters params = douglasParams.get(tree_index);
		if(params == null) {
			return Float.NaN;
		}
		return params.x;
	}

	public float getY(int tree_index) {
		return douglasParams.get(tree_index).y;
	}

	public float getZ(int tree_index) {
		return douglasParams.get(tree_index).z;
	}

	public float getHeight(int tree_index) {
		return douglasParams.get(tree_index).height;
	}

	public float getAngleDegree1(int tree_index) {
		return douglasParams.get(tree_index).angleDegree1;
	}

	public float getRandomTopTree(int tree_index) {
		return douglasParams.get(tree_index).topTree;
	}

	public float getRandomBottomTree(int tree_index) {
		return douglasParams.get(tree_index).bottomTree;
	}

	public float getRandomBaseTree(int tree_index) {
		return douglasParams.get(tree_index).baseTree;
	}

	public int getRandomColorMultiplierTree(int tree_index) {
		return douglasParams.get(tree_index).colorMultiplier;
	}

	public float getRandomLeanAngleTree(int tree_index) {
		return douglasParams.get(tree_index).leanAngle;
	}

	public float getRandomLeanDirectionAngleTree(int tree_index) {
		return douglasParams.get(tree_index).leanDirectionAngle;
	}

	public int compute(final JProgressBar progressBar) {
		
		//if( ! loadDouglas()) {
		
			Random randomPlacementTrees = new Random(SEED_PLACEMENT_TREES);		
			Random randomHeightTrees = new Random(SEED_HEIGHT_TREES);		
			Random randomAngleTrees = new Random(SEED_ANGLE_TREES);
			Random randomTopTrees  = new Random(SEED_WIDTH_TOP_TREES);
			Random randomBottomTrees  = new Random(SEED_WIDTH_BOTTOM_TREES);
			Random randomColorMultiplier  = new Random(SEED_COLOR_MULTIPLIER);
			Random randomLeanAngle = new Random(SEED_LEAN_ANGLE_TREES);
			Random randomLeanDirectionAngle = new Random(SEED_LEAN_DIRECTION_ANGLE_TREES);
			Random randomBaseTrees = new Random(SEED_BASE_TREES);
			
			int[] indices = new int[4];
			
			final SbColor dummyColor = new SbColor();
			
			float averageRed = DouglasChunk.TREE_FOLIAGE_AVERAGE_MULTIPLIER.getX();
			float averageGreen = DouglasChunk.TREE_FOLIAGE_AVERAGE_MULTIPLIER.getY();
			float averageBlue = DouglasChunk.TREE_FOLIAGE_AVERAGE_MULTIPLIER.getZ();
			
			final float nan = Float.NaN;

			final SbVec3f xyz = new SbVec3f();

			final int progressBarInitialValue = progressBar != null? progressBar.getValue():0;
			
			for( int i = 0; i < NB_DOUGLAS_SEEDS; i++) {

				if(0 == i%999 && progressBar != null) {
					progressBar.setValue((int)((long)progressBarInitialValue+((long)MainGLFW.MAX_PROGRESS*.9-progressBarInitialValue)*i/NB_DOUGLAS_SEEDS));
				}

				float x = getRandomX(randomPlacementTrees);
				float y = getRandomY(randomPlacementTrees);
				float z = sg.getInternalZ(x,y,indices,true) + sg.getzTranslation();

				xyz.setX(x);
				xyz.setY(y);
				xyz.setZ(z);
				
				boolean isAboveWater = z > - 150 + sg.getzTranslation() - sg.CUBE_DEPTH /2;
				boolean isUnderSnowLevel = z < sg.ALPINE_HEIGHT;
				boolean isStone = sg.isStone(x,y);
				boolean isNearTrails = sg.isNearTrails(xyz);

				final float randomHeight = DouglasFir.getHeight(randomHeightTrees);
				final float randomAngle = randomAngleTrees.nextFloat();
				final float randomTop = randomTopTrees.nextFloat();
				final float randomBottom = randomBottomTrees.nextFloat();
				final float randomLeanAngle_ = randomLeanAngle.nextFloat();
				final float randomLeanDirection = randomLeanDirectionAngle.nextFloat();
				final float randomColorMultiplier1 = randomColorMultiplier.nextFloat();
				final float randomColorMultiplier2 = randomColorMultiplier.nextFloat();
				final float randomColorMultiplier3 = randomColorMultiplier.nextFloat();

				if( isAboveWater && isUnderSnowLevel && !isStone && !isNearTrails ) {

					final DouglasParameters params = new DouglasParameters();
					douglasParams.add(params);
					
					float height = randomHeight;//DouglasFir.getHeight(randomHeightTrees);

					params.x = x;
					params.y = y;
					params.z = z;
					params.height = height;
					float angleDegree = 120.0f * randomAngle;//randomAngleTrees.nextFloat();
					params.angleDegree1 = angleDegree;
					float width = height * 0.707f / 50.0f;
					float widthTop = width */*2.5f*/5f * (float)Math.pow(randomTop/*randomTopTrees.nextFloat()*/,2.0f);
					params.topTree = widthTop;
					float foliageWidth = (height+ randomBottom/*randomBottomTrees.nextFloat()*/*12.0f) * 0.1f;
					params.bottomTree = foliageWidth;

					params.baseTree = 1.0f + randomBaseTrees.nextFloat()*height/10;
					
					float leanAngleTree = randomLeanAngle_;//randomLeanAngle.nextFloat();
					params.leanAngle = (float)(Math.pow(leanAngleTree, 5)*Math.PI/2/10);
					
					float leanAngleDirectionTree = randomLeanDirection;//randomLeanDirectionAngle.nextFloat();
					params.leanDirectionAngle = (float)(leanAngleDirectionTree * Math.PI * 2);
					
					float deltaR = randomColorMultiplier1/*randomColorMultiplier.nextFloat()*/ - 0.5f;
					float deltaG = randomColorMultiplier2/*randomColorMultiplier.nextFloat()*/ - 0.5f;
					float deltaB = randomColorMultiplier3/*randomColorMultiplier.nextFloat()*/ - 0.5f;
					
					int power = 5;
					deltaR = (float)Math.pow(deltaR*2.0, power)/2.0f;
					deltaG = (float)Math.pow(deltaG*2.0, power)/2.0f;
					deltaB = (float)Math.pow(deltaB*2.0, power)/2.0f;
					
					float r = (averageRed + deltaR)/3.0f;
					float g = averageGreen + deltaG/1.2f + deltaR;
					float b = (averageBlue + deltaB)/1.4f;
					
					float tree_contrast = 1.1f;
					
					r *= tree_contrast;
					g *= tree_contrast;
					b *= tree_contrast;
					
					r = Math.max(r,0);
					g = Math.max(g,0);
					b = Math.max(b,0);
					r = Math.min(r,1);
					g = Math.min(g,1);
					b = Math.min(b,1);
					dummyColor.setX(r);
					dummyColor.setY(g);
					dummyColor.setZ(b);
					
					params.colorMultiplier = dummyColor.getPackedValue();
					
					nbDouglas++;
				}
				else {
					//xArray[i] = nan;
				}
			}
			//saveDouglas(); no increase of performance
//		}

		System.out.println("numDouglas = "+ nbDouglas);
		return nbDouglas;
	}
/*
	private void saveDouglas() {
		File file = new File("douglas_forest.mri");
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			
			byte[] nbda = new byte[Integer.BYTES];
			ByteBuffer bbnbda = ByteBuffer.wrap(nbda);
			bbnbda.asIntBuffer().put(nbDouglas);
			fos.write(nbda);
			
			byte[] ba = new byte[xArray.length*Float.BYTES];
			ByteBuffer bb = ByteBuffer.wrap(ba);
			
			bb.asFloatBuffer().put(xArray);
			fos.write(ba);
			
			bb.asFloatBuffer().put(yArray);
			fos.write(ba);
			
			bb.asFloatBuffer().put(zArray);
			fos.write(ba);
			
			bb.asFloatBuffer().put(heightArray);
			fos.write(ba);
			
			bb.asFloatBuffer().put(angleDegree1);
			fos.write(ba);
			
			bb.asFloatBuffer().put(randomTopTree);
			fos.write(ba);
			
			bb.asFloatBuffer().put(randomBottomTree);
			fos.write(ba);

			bb.asFloatBuffer().put(randomBaseTree);
			fos.write(ba);

			bb.asFloatBuffer().put(randomLeanAngleTree);
			fos.write(ba);
			
			bb.asFloatBuffer().put(randomLeanDirectionAngleTree);
			fos.write(ba);
			
			bb.asIntBuffer().put(randomColorMultiplierTree);
			fos.write(ba);			
			
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
*/
	/*
	private boolean loadDouglas() {
		File file = new File( "douglas_forest.mri" );
		
		if( !file.exists()) {
			return false;
		}
		if( !file.isFile()) {
			return false;
		}
		if ( file.length() != getDouglasForestFileLength()) {
			return false;
		}
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			
			int nbb = (int)file.length();									
			byte[] buffer = new byte[nbb];
			fileInputStream.read(buffer);
			
			ByteBuffer bbnbda = ByteBuffer.wrap(buffer,0,Integer.BYTES);
			nbDouglas = bbnbda.asIntBuffer().get();
			
			ByteBuffer bb = ByteBuffer.wrap(buffer,Integer.BYTES,(int)file.length()-Integer.BYTES);
			
			FloatBuffer fb = bb.asFloatBuffer();			
			
			fb.get(xArray);
			fb.get(yArray);
			fb.get(zArray);
			fb.get(heightArray);
			fb.get(angleDegree1);
			fb.get(randomTopTree);
			fb.get(randomBottomTree);
			fb.get(randomBaseTree);
			fb.get(randomLeanAngleTree);
			fb.get(randomLeanDirectionAngleTree);
			
			bb.position(Integer.BYTES * xArray.length * 9);
			IntBuffer ib = bb.asIntBuffer();			
			ib.get(randomColorMultiplierTree);
			
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		return true;
	}
*/
	private long getDouglasForestFileLength() {
		return NB_DOUGLAS_SEEDS * Float.BYTES * 8 + Integer.BYTES;
	}

	float getRandomX(Random randomPlacementTrees) {
		SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
		float xMin = sceneBox.getBounds()[0];
		float xMax = sceneBox.getBounds()[3];
		return xMin + (xMax - xMin) * randomPlacementTrees.nextFloat();
	}
	
	float getRandomY(Random randomPlacementTrees) {
		SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
		float yMin = sceneBox.getBounds()[1];
		float yMax = sceneBox.getBounds()[4];
		return yMin + (yMax - yMin) * randomPlacementTrees.nextFloat();
	}

	public void buildDouglasChunks() {
		SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
		float xMin = sceneBox.getBounds()[0];
		float xMax = sceneBox.getBounds()[3];		
		float yMin = sceneBox.getBounds()[1];
		float yMax = sceneBox.getBounds()[4];
		
		float delta_x = xMax - xMin;
		float delta_y = yMax - yMin;
		
//		float width_x = delta_x / 15;
//		float width_y = delta_y / 15;
		
		float min_width = DOUGLAS_CHUNK_WIDTH; //Math.min(width_x,width_y);

		System.out.println("DouglasChunk width : "+min_width);
		
		int nb_x = (int)Math.round(Math.ceil(delta_x / min_width));
		int nb_y = (int)Math.round(Math.ceil(delta_y / min_width));

		System.out.println("nb_x = "+nb_x);
		System.out.println("nb_y = "+nb_y);
		
		for(int i=0;i<nb_x;i++) {
			SbBox3f xLimitsBBox = new SbBox3f();
			xLimits.add(xLimitsBBox);
			List<DouglasChunk> listForX = new ArrayList<>();
			douglasChunks.add(listForX);
			for(int j=0; j< nb_y;j++) {
				float chunkMinX = xMin + i*min_width;
				float chunkMaxX = xMin + (i+1)*min_width;
				float chunkMinY = yMin + j*min_width;
				float chunkMaxY = yMin + (j+1)*min_width;
				SbBox3f chunkBB = new SbBox3f();
				chunkBB.setBounds(chunkMinX, chunkMinY, 0, chunkMaxX, chunkMaxY, 0);
				listForX.add(new DouglasChunk(this,chunkBB));
				if(xLimitsBBox != null) {
					xLimitsBBox.extendBy(chunkBB);
					xLimitsBBox = null;
				}
			}
		}
	}

	public void fillDouglasChunks() {
		
		final SbVec3f xy = new SbVec3f();
		
		for(int tree=0; tree</*nbDouglas*/getNumIndexDouglas();tree++) {

			DouglasParameters params = douglasParams.get(tree);
			if(params == null) {
				continue;
			}

			float x = params.x;
			float y = params.y;

			xy.setValue(x, y, 0.0f);
			
			int nbChunksX = douglasChunks.size(); 
			for(int i=0;i<nbChunksX;i++) {
				List<DouglasChunk> listForX = douglasChunks.get(i);
				int nbChunksY = listForX.size();
				if(x >= xLimits.get(i).getMin().getX() && x <= xLimits.get(i).getMax().getX()) {
					for(int j=0;j<nbChunksY;j++) {
						DouglasChunk chunk = listForX.get(j);
						if( chunk.boundingBox.intersect(xy)) {
							chunk.addTree(/*x,y,zArray[tree],heightArray[tree],angleDegree1[tree],randomTopTree[tree],randomBottomTree[tree]*/tree);
						}
					}
				}
			}
			
		}
	}

	public void computeDouglas() {
		int nbChunksX = douglasChunks.size(); 
		for(int i=0;i<nbChunksX;i++) {
			List<DouglasChunk> listForX = douglasChunks.get(i);
			listForX.parallelStream().forEach((dc)->dc.computeDouglas());
		}
//		for( DouglasChunk chunk : douglasChunks ) {
//			chunk.computeDouglas();
//		}
	}

	public SoGroup getDouglasTreesT(SbVec3f refPoint,SbVec3f refPoint2, final float[] distance,final Counter douglasLoadCount) {

		SoGroup separator = new SoGroup();
		
		for( List<DouglasChunk> chunkListForX : douglasChunks ) {
			SoLODGroup separatorForX = new SoLODGroup();
			separatorForX.maxDistance = distance;
			for( DouglasChunk chunk : chunkListForX ) {
				
				final SbBox3f finalBox = new SbBox3f();
				final SbVec3f finalCenter = new SbVec3f();
				
				
				if(chunk.xMin != Float.MAX_VALUE) {
					SbVec3f min = new SbVec3f(chunk.xMin,chunk.yMin,chunk.zMin);
					SbVec3f max = new SbVec3f(chunk.xMax,chunk.yMax,chunk.zMax);
					finalBox.extendBy(min);
					finalBox.extendBy(max);
					SbVec3f centerV = new SbVec3f(
							(chunk.xMin+chunk.xMax)/2,
							(chunk.yMin+chunk.yMax)/2,
							(chunk.zMin+chunk.zMax)/2);
					finalCenter.setValue(centerV);
				}
				
				SoLODIndexedFaceSet indexedFaceSetT = new SoLODIndexedFaceSet(refPoint,refPoint2,chunk,SoLODIndexedFaceSet.Type.TRUNK, douglasLoadCount,finalBox,finalCenter);
				
				indexedFaceSetT.loadTrunk();
				
				indexedFaceSetT.maxDistance = distance;
				
				if(!finalBox.isEmpty()) {
					separatorForX.addChild(indexedFaceSetT);	
					separatorForX.box.extendBy(finalBox);
				}
			}		
			separator.addChild(separatorForX);
		}
		
		return separator;
	}

	public SoGroup getDouglasTreesF(SbVec3f refPoint,SbVec3f refPoint2, final float[] distance, boolean withColors,final Counter douglasLoadCount) {

		SoGroup separator = new SoGroup();

//		SoShapeHints shapeHints = new SoShapeHints();
//
//		separator.addChild(shapeHints);
		
		for( List<DouglasChunk> chunkListForX : douglasChunks ) {
			SoLODGroup separatorForX = new SoLODGroup();
			separatorForX.maxDistance = distance;
			for( DouglasChunk chunk : chunkListForX ) {
				
					final SbBox3f finalBox = new SbBox3f();
					final SbVec3f finalCenter = new SbVec3f();
					
					if(chunk.xMin != Float.MAX_VALUE) {
						SbVec3f min = new SbVec3f(chunk.xMin,chunk.yMin,chunk.zMin);
						SbVec3f max = new SbVec3f(chunk.xMax,chunk.yMax,chunk.zMax);
						finalBox.extendBy(min);
						finalBox.extendBy(max);
						SbVec3f centerV = new SbVec3f(
								(chunk.xMin+chunk.xMax)/2,
								(chunk.yMin+chunk.yMax)/2,
								(chunk.zMin+chunk.zMax)/2);
						finalCenter.setValue(centerV);
					}
					
				SoLODIndexedFaceSet indexedFaceSetF = new SoLODIndexedFaceSet(refPoint,refPoint2, chunk,SoLODIndexedFaceSet.Type.FOLIAGE, douglasLoadCount,finalBox,finalCenter);
				
				//indexedFaceSetF.loadFoliage();
				
				indexedFaceSetF.maxDistance = distance;
				
				if(!finalBox.isEmpty()) {
					separatorForX.addChild(indexedFaceSetF);
					separatorForX.box.extendBy(finalBox);
				}
			}
			separator.addChild(separatorForX);
		}
		
		return separator;
	}

}
