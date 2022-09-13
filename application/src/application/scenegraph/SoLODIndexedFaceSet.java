/**
 * 
 */
package application.scenegraph;

import application.scenegraph.douglas.IndexedFaceSetParameters;
import jscenegraph.coin3d.inventor.nodes.SoVertexProperty;
import jscenegraph.database.inventor.SbBox3f;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.elements.SoGLCacheContextElement;
import jscenegraph.database.inventor.nodes.SoIndexedFaceSet;

/**
 * @author Yves Boyadjian
 *
 */
public class SoLODIndexedFaceSet extends SoIndexedFaceSet {
	
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
	
	private LoadState loaded = LoadState.CLEARED;
	
	private final int[] counting;
	
	public SoLODIndexedFaceSet(SbVec3f referencePoint,SbVec3f referencePoint2, DouglasChunk chunk, Type type, final int[] counting) {
		this.referencePoint = referencePoint;
		this.referencePoint2 = referencePoint2;
		this.chunk = chunk;
		this.type = type;
		this.counting = counting;
		enableNotify(false); // In order not to invalidate shaders
	}
	
	public void GLRender(SoGLRenderAction action)
	{		
		getBBox(action, box, center);

		if( box.intersect(referencePoint2)) {
			load(true); // Near mode
			super.GLRender(action);
		}
		else {

			SbVec3f closestPoint = new SbVec3f(box.getClosestExternalPoint(referencePoint));

			SbVec3f closestPoint2 = new SbVec3f(box.getClosestExternalPoint(referencePoint2));

			if( closestPoint2.operator_minus(referencePoint2,dummy).length() <= 2500 ) {
				load(true);
				super.GLRender(action);
			}
			else if( closestPoint2.operator_minus(referencePoint2,dummy).length() <= maxDistance[0] ) {
				load(false);
				super.GLRender(action);				
			}
			else {
				clear();
				chunk.clear();
			}
		}
		  // don't auto cache LOD nodes.
		  SoGLCacheContextElement.shouldAutoCache(action.getState(),
		                                           SoGLCacheContextElement.AutoCache.DONT_AUTO_CACHE.getValue());
	}		
	
	private void load(boolean near) {
		switch(type) {
		case FOLIAGE:
			loadFoliage(near);
			break;
		case TRUNK:
			loadTrunk();
			break;
		default:
			break;
		
		}
	}
	
	public void loadTrunk() {
		if(loaded == LoadState.CLEARED && counting[0] < 2 /*&& counting[1] < 50*/) {
			counting[0]++;
			counting[1]++;
			loaded = LoadState.LOAD_FAR;
		SoLODIndexedFaceSet indexedFaceSetT = this;
		
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
		}
	}
	
	public void loadFoliage(boolean near) {

		LoadState wanted = near ? LoadState.LOAD_NEAR : LoadState.LOAD_FAR;

		if(loaded != wanted && counting[0] < 2 /*&& counting[1] < 50*/) {
			clear();
			counting[0]++;
			counting[1]++;
			loaded = wanted;
		SoLODIndexedFaceSet indexedFaceSetF = this;

			IndexedFaceSetParameters farFoliageParameters = near ? chunk.getFoliageNearParameters() : chunk.getFoliageFarParameters();
		
		//boolean wasNotify = indexedFaceSetF.coordIndex.enableNotify(false); // In order not to recompute shaders
		indexedFaceSetF.coordIndex.setValuesPointer(/*chunk.douglasIndicesF*/farFoliageParameters.coordIndices());
		//indexedFaceSetF.coordIndex.enableNotify(wasNotify);
		
		SoVertexProperty vertexProperty = new SoVertexProperty();
		
		vertexProperty.vertex.setValuesPointer(/*chunk.douglasVerticesF*/farFoliageParameters.vertices());
		
		vertexProperty.normalBinding.setValue(SoVertexProperty.Binding.PER_VERTEX_INDEXED);
		
		vertexProperty.normal.setValuesPointer(/*chunk.douglasNormalsF*/farFoliageParameters.normals());
		
		boolean withColors = true;
		if(withColors) {
			vertexProperty.texCoord.setValuesPointer(/*chunk.douglasTexCoordsF*/farFoliageParameters.textureCoords());
			vertexProperty.materialBinding.setValue(SoVertexProperty.Binding.PER_VERTEX_INDEXED);
			vertexProperty.orderedRGBA.setValuesPointer(/*chunk.douglasColorsF*/farFoliageParameters.colorsRGBA());
		}
		else {
			vertexProperty.orderedRGBA.setValue(DouglasChunk.TREE_FOLIAGE_AVERAGE_MULTIPLIER/*SbColor(1,0.0f,0.0f)*/.getPackedValue());
		}
		
		//wasNotify = indexedFaceSetF.vertexProperty.enableNotify(false);
		indexedFaceSetF.vertexProperty.setValue(vertexProperty);
		//indexedFaceSetF.vertexProperty.enableNotify(wasNotify); // In order not to recompute shaders
		}		
	}
	public void clear() {
		if(loaded != LoadState.CLEARED) {
			counting[1]--;
			loaded = LoadState.CLEARED;
		    boolean wasEnabled = this.vertexProperty.enableNotify(false);
			vertexProperty.setValue(null/*recursiveChunk.getVertexProperty()*/);
			this.vertexProperty.enableNotify(wasEnabled);
			
			//coordIndex.setValuesPointer(recursiveChunk.getDecimatedCoordIndices());
			//wasEnabled = coordIndex.enableNotify(false); // In order not to recompute shaders
			coordIndex.setNum(0); // Notification MUST be enabled for this, or else there is a memory leak
		    //coordIndex.enableNotify(wasEnabled);
		}
	}
}
