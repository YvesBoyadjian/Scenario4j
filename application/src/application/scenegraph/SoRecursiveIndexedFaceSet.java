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
import jscenegraph.database.inventor.nodes.SoIndexedFaceSet;

/**
 * @author Yves Boyadjian
 *
 */
public class SoRecursiveIndexedFaceSet extends SoIndexedFaceSet {
	
	RecursiveChunk recursiveChunk;
	
	boolean cleared = true;
	
	public static int nbDoLoad;

	public SoRecursiveIndexedFaceSet(RecursiveChunk recursiveChunk) {
		this.recursiveChunk = recursiveChunk;
		enableNotify(false); // In order not to invalidate shaders
	}
	
	private void doLoad(IndexedFaceSetParameters parameters) {
		nbDoLoad++;

		SoVertexProperty vertexProperty = new SoVertexProperty();
		vertexProperty.vertex.setValuesPointer(parameters.vertices()/*recursiveChunk.getDecimatedVertices()*//*,recursiveChunk.getDecimatedVerticesBuffer()*/);
	    vertexProperty.normalBinding.setValue(SoVertexProperty.Binding.PER_VERTEX_INDEXED);
	    vertexProperty.normal.setValuesPointer(/*0,*/ parameters.normals()/*recursiveChunk.getDecimatedNormals()*//*,recursiveChunk.getDecimatedNormalsBuffer()*/);
	    vertexProperty.texCoord.setValuesPointer(/*0,*/ parameters.textureCoords()/*recursiveChunk.getDecimatedTexCoords()*//*,recursiveChunk.getDecimatedTexCoordsBuffer()*/);

	    boolean wasEnabled = this.vertexProperty.enableNotify(false);
		this.vertexProperty.setValue(vertexProperty);
		this.vertexProperty.enableNotify(wasEnabled);

		wasEnabled = coordIndex.enableNotify(false);
	    coordIndex.setValuesPointer(/*0,*/ parameters.coordIndices()/*recursiveChunk.getDecimatedCoordIndices()*/);
	    coordIndex.enableNotify(wasEnabled);

		parameters.markConsumed();
	}

	/**
	 * Returns true is there was action
	 * @return
	 */
	public boolean clear() {
		if(!cleared) {
		    boolean wasEnabled = this.vertexProperty.enableNotify(false);
			vertexProperty.setValue(null/*recursiveChunk.getVertexProperty()*/);
			this.vertexProperty.enableNotify(wasEnabled);
			
			//coordIndex.setValuesPointer(recursiveChunk.getDecimatedCoordIndices());
			//wasEnabled = coordIndex.enableNotify(false); // Don't want to invalidate shaders
			coordIndex.setNum(0); // Notification MUST be enabled for this, or else there is a memory leak
		    //coordIndex.enableNotify(wasEnabled);
		    
			recursiveChunk.clear();		
			cleared = true;
			return true;
		}
		return false;
	}
	
	public void computeBBox(SoAction action, SbBox3f box, SbVec3f center) {
		box.copyFrom(recursiveChunk.sceneBox);
		center.copyFrom(recursiveChunk.sceneCenter);
	}

	public void GLRender(SoGLRenderAction action)
	{
		if(cleared && nbDoLoad<0) {

			IndexedFaceSetParameters parameters = recursiveChunk.getDecimatedParameters();
			if (parameters != null) {
				doLoad(parameters);
				cleared = false;
			}
//		long delta = stop - start;
//		
//		if(delta > 10e6) {
//			System.out.println("SoTouchLOD2 " + delta/1e6 +" ms");
//		}
//		int i=0;
		}
		//long start = System.nanoTime();
		if (!cleared) {
			super.GLRender(action);
		}
		//long stop = System.nanoTime();
	}
}
