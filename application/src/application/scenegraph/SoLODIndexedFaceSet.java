/**
 * 
 */
package application.scenegraph;

import application.scenegraph.douglas.IndexedFaceSetParameters;
import jscenegraph.coin3d.inventor.nodes.SoVertexProperty;
import jscenegraph.database.inventor.SbBox3f;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.actions.SoAction;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.elements.SoGLCacheContextElement;
import jscenegraph.database.inventor.nodes.SoGroup;
import jscenegraph.database.inventor.nodes.SoIndexedFaceSet;

/**
 * @author Yves Boyadjian
 *
 */
public class SoLODIndexedFaceSet extends /*SoIndexedFaceSet*/SoGroup {
	
	public static enum Type {
		TRUNK,
		FOLIAGE
	}

	public static enum LoadState {
		CLEARED,
		LOAD_FAR,
		LOAD_NEAR
	}
	
	private final Type type;

	private final SbVec3f referencePoint;

	private final SbVec3f referencePoint2;

	public float[] maxDistance;
	
	private final SbBox3f box = new SbBox3f();
	
	private final SbVec3f center = new SbVec3f();
	
	private final SbVec3f dummy = new SbVec3f(); //SINGLE_THREAD
	
	private final DouglasChunk chunk;
	
	private LoadState loadedFar = LoadState.CLEARED;
	private LoadState[] loadedNear = new LoadState[DouglasChunk.NUM_NEAR_FOLIAGE];//LoadState.CLEARED;

	private final int[] counting;

	private final SoIndexedFaceSet sonFar;

	private final SoIndexedFaceSet[] sonsNear = new SoIndexedFaceSet[DouglasChunk.NUM_NEAR_FOLIAGE];
	
	public SoLODIndexedFaceSet(SbVec3f referencePoint,SbVec3f referencePoint2, DouglasChunk chunk, Type type, final int[] counting,SbBox3f finalBox, SbVec3f finalCenter) {
		this.referencePoint = referencePoint;
		this.referencePoint2 = referencePoint2;
		this.chunk = chunk;
		this.type = type;
		this.counting = counting;
		enableNotify(false); // In order not to invalidate shaders

		sonFar = new SoIndexedFaceSet() {

		public void computeBBox (SoAction action, SbBox3f box, SbVec3f center){

			box.copyFrom(finalBox);
			center.copyFrom(finalCenter);
			//super.computeBBox(action, box, center);
		}

		public void GLRender(SoGLRenderAction action) {
			if (isAllNearLoaded()) {
				return;
			}
			super.GLRender(action);
		}
	};

	if (type == SoLODIndexedFaceSet.Type.FOLIAGE) {
		for (int i=0;i<DouglasChunk.NUM_NEAR_FOLIAGE; i++) {

			loadedNear[i] = LoadState.CLEARED;
			sonsNear[i] = new SoIndexedFaceSet() {

				public void computeBBox (SoAction action, SbBox3f box, SbVec3f center){

					box.copyFrom(finalBox);
					center.copyFrom(finalCenter);
					//super.computeBBox(action, box, center);
				}
			};
			sonsNear[i].enableNotify(false);
			addChild(sonsNear[i]);
		}
	}

		sonFar.enableNotify(false);
		addChild(sonFar);
	}
	
	public void GLRender(SoGLRenderAction action)
	{		
		sonFar.getBBox(action, box, center);

		if( box.intersect(referencePoint2)) {
			if(!load(true)) // Near mode
			super.GLRender(action);
		}
		else {

			SbVec3f closestPoint = new SbVec3f(box.getClosestExternalPoint(referencePoint));

			SbVec3f closestPoint2 = new SbVec3f(box.getClosestExternalPoint(referencePoint2));

			if( closestPoint2.operator_minus(referencePoint2,dummy).length() <= 200 ) {
				if(!load(true))
				super.GLRender(action);
			}
			else if( closestPoint2.operator_minus(referencePoint2,dummy).length() <= maxDistance[0] ) {
				if(!load(false))
				super.GLRender(action);				
			}
			else {
				clearAll();
				//chunk.clear();
			}
		}
		  // don't auto cache LOD nodes.
		  SoGLCacheContextElement.shouldAutoCache(action.getState(),
		                                           SoGLCacheContextElement.AutoCache.DONT_AUTO_CACHE.getValue());
	}

	/**
	 *
	 * @param near
	 * @return false if must render
	 */
	private boolean load(boolean near) {
		switch(type) {
		case FOLIAGE:
			return loadFoliage(near);
		case TRUNK:
			return loadTrunk();
		default:
			return false;
		
		}
	}

	/**
	 *
	 * @return false if must render
	 */
	public boolean loadTrunk() {
		if(loadedFar == LoadState.CLEARED && counting[0] < 1 /*&& counting[1] < 50*/) {
			counting[0]++;
			counting[1]++;
			loadedFar = LoadState.LOAD_FAR;
		SoIndexedFaceSet indexedFaceSetT = sonFar;
		
		//boolean wasNotify = indexedFaceSetT.coordIndex.enableNotify(false); // In order not to recompute shaders
		indexedFaceSetT.coordIndex.setValuesPointer(chunk.douglasIndicesT);
		//indexedFaceSetT.coordIndex.enableNotify(wasNotify);
		
		SoVertexProperty vertexProperty = new SoVertexProperty();
		
		vertexProperty.vertex.setValuesPointer(chunk.douglasVerticesT);
		
		vertexProperty.normalBinding.setValue(SoVertexProperty.Binding.PER_VERTEX_INDEXED);
		
		vertexProperty.normal.setValuesPointer(chunk.douglasNormalsT);
		
		vertexProperty.materialBinding.setValue(SoVertexProperty.Binding.PER_VERTEX_INDEXED);
		
		vertexProperty.orderedRGBA.setValues(0, chunk.douglasColorsT);
		
		//wasNotify = indexedFaceSetT.vertexProperty.enableNotify(false);
		indexedFaceSetT.vertexProperty.setValue(vertexProperty);
		//indexedFaceSetT.vertexProperty.enableNotify(wasNotify); // In order not to recompute shaders
			return true; // must not render the first time
		}
		return loadedFar == LoadState.CLEARED; // must not render if cleared
	}

	private boolean isAllNearLoaded() {
		for (int i=0; i<DouglasChunk.NUM_NEAR_FOLIAGE;i++) {
			if (loadedNear[i] != LoadState.LOAD_NEAR) {
				return false;
			}
		}
		return true;
	}

	private int firstNearIndexToLoad() {
		for (int i=0; i<DouglasChunk.NUM_NEAR_FOLIAGE;i++) {
			if (loadedNear[i] != LoadState.LOAD_NEAR) {
				return i;
			}
		}
		return -1;
	}

	/**
	 *
	 * @param near
	 * @return false if must render
	 */
	public boolean loadFoliage(boolean near) {

		final LoadState initiallyWanted = near ? LoadState.LOAD_NEAR : LoadState.LOAD_FAR;
		//final LoadState originalLoadedFar = loadedFar;
		//final LoadState originalLoadedNear = loadedNear[0];

		// Loading must be done
		if (
				(
				((initiallyWanted == LoadState.LOAD_FAR) && (loadedFar != initiallyWanted))
				|| ((initiallyWanted == LoadState.LOAD_NEAR) && !isAllNearLoaded())
				)
				&& counting[0] < 1 /*&& counting[1] < 50*/) {
			counting[0]++;
			counting[1]++;

			final int nearIndexToLoad = firstNearIndexToLoad();

			IndexedFaceSetParameters foliageParameters = (initiallyWanted == LoadState.LOAD_NEAR) ? chunk.getFoliageNearParameters(nearIndexToLoad) : chunk.getFoliageFarParameters();

			LoadState oneToLoad = initiallyWanted;

			if (initiallyWanted == LoadState.LOAD_NEAR && foliageParameters == null) { // Near not still computed, we fall on far
				oneToLoad = LoadState.LOAD_FAR;
				if (loadedFar == oneToLoad) {
					return false; // nothing to do, we can draw far at first time
				}
				foliageParameters = chunk.getFoliageFarParameters();
			}
			else if (initiallyWanted == LoadState.LOAD_FAR /*&& loadedNear[0] == LoadState.LOAD_NEAR*/){ // Clear near if far is initially wanted
					clearNear(); // We must clear near if far is wanted and near is loaded
			}

			final SoIndexedFaceSet indexedFaceSetF = (oneToLoad == LoadState.LOAD_FAR) ? sonFar : sonsNear[nearIndexToLoad];

			//boolean wasNotify = indexedFaceSetF.coordIndex.enableNotify(false); // In order not to recompute shaders
			indexedFaceSetF.coordIndex.setValuesPointer(/*chunk.douglasIndicesF*/foliageParameters.coordIndices());
			//indexedFaceSetF.coordIndex.enableNotify(wasNotify);

			SoVertexProperty vertexProperty = new SoVertexProperty();

			vertexProperty.vertex.setValuesPointer(/*chunk.douglasVerticesF*/foliageParameters.vertices(),foliageParameters.keepOwnership());

			vertexProperty.normalBinding.setValue(SoVertexProperty.Binding.PER_VERTEX_INDEXED);

			vertexProperty.normal.setValuesPointer(/*chunk.douglasNormalsF*/foliageParameters.normals(),foliageParameters.keepOwnership());

			boolean withColors = true;
			if(withColors) {
				vertexProperty.texCoord.setValuesPointer(/*chunk.douglasTexCoordsF*/foliageParameters.textureCoords(),foliageParameters.keepOwnership());
				vertexProperty.materialBinding.setValue(SoVertexProperty.Binding.PER_VERTEX_INDEXED);
				vertexProperty.orderedRGBA.setValuesPointer(/*chunk.douglasColorsF*/foliageParameters.colorsRGBA());
			}
			else {
				vertexProperty.orderedRGBA.setValue(DouglasChunk.TREE_FOLIAGE_AVERAGE_MULTIPLIER/*SbColor(1,0.0f,0.0f)*/.getPackedValue());
			}

			//wasNotify = indexedFaceSetF.vertexProperty.enableNotify(false);
			indexedFaceSetF.vertexProperty.setValue(vertexProperty);
			//indexedFaceSetF.vertexProperty.enableNotify(wasNotify); // In order not to recompute shaders
			foliageParameters.markConsumed();

			// if was not cleared, we must immediately draw in order not to have flickering
			boolean mustDraw = true;//(originalLoaded != LoadState.CLEARED);

			switch(oneToLoad) {

				case LOAD_FAR:
					loadedFar = oneToLoad;
					break;
				case LOAD_NEAR:
					loadedNear[nearIndexToLoad] = oneToLoad;
					if (isAllNearLoaded()) {
						//clearFar();
					}
					break;
			}

//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				throw new RuntimeException(e);
//			}
			return !mustDraw; // don't want to draw after an update
		}
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
		return /*originalLoaded == LoadState.CLEARED*/false; // must not render if cleared
	}
	public void clearNear() {

		// No near for trunk
		if (type == Type.TRUNK) {
			return;
		}

		for (int nearIndex=0; nearIndex<DouglasChunk.NUM_NEAR_FOLIAGE; nearIndex++) {
			if (loadedNear[nearIndex] != LoadState.CLEARED) {
				counting[1]--;
				loadedNear[nearIndex] = LoadState.CLEARED;
				boolean wasEnabled = sonsNear[0].vertexProperty.enableNotify(false);
				sonsNear[nearIndex].vertexProperty.setValue(null/*recursiveChunk.getVertexProperty()*/);
				sonsNear[nearIndex].vertexProperty.enableNotify(wasEnabled);

				//coordIndex.setValuesPointer(recursiveChunk.getDecimatedCoordIndices());
				//wasEnabled = coordIndex.enableNotify(false); // In order not to recompute shaders
				sonsNear[nearIndex].coordIndex.setNum(0); // Notification MUST be enabled for this, or else there is a memory leak
				//coordIndex.enableNotify(wasEnabled);
			}
		}
	}
	public void clearFar() {
		if(loadedFar != LoadState.CLEARED) {
			counting[1]--;
			loadedFar = LoadState.CLEARED;
			boolean wasEnabled = sonFar.vertexProperty.enableNotify(false);
			sonFar.vertexProperty.setValue(null/*recursiveChunk.getVertexProperty()*/);
			sonFar.vertexProperty.enableNotify(wasEnabled);

			//coordIndex.setValuesPointer(recursiveChunk.getDecimatedCoordIndices());
			//wasEnabled = coordIndex.enableNotify(false); // In order not to recompute shaders
			sonFar.coordIndex.setNum(0); // Notification MUST be enabled for this, or else there is a memory leak
			//coordIndex.enableNotify(wasEnabled);
		}
	}

	public void clearAll() {
		clearFar();
		clearNear();
	}
}
