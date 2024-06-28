/*
 *
 *  Copyright (C) 2000 Silicon Graphics, Inc.  All Rights Reserved. 
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  Further, this software is distributed without any warranty that it is
 *  free of the rightful claim of any third person regarding infringement
 *  or the like.  Any license provided herein, whether implied or
 *  otherwise, applies only to this software file.  Patent licenses, if
 *  any, provided herein do not apply to combinations of this program with
 *  other software, or any other product whatsoever.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact information: Silicon Graphics, Inc., 1600 Amphitheatre Pkwy,
 *  Mountain View, CA  94043, or:
 * 
 *  http://www.sgi.com 
 * 
 *  For further information regarding this notice, see: 
 * 
 *  http://oss.sgi.com/projects/GenInfo/NoticeExplan/
 *
 */


/*
 * Copyright (C) 1990,91   Silicon Graphics, Inc.
 *
 _______________________________________________________________________
 ______________  S I L I C O N   G R A P H I C S   I N C .  ____________
 |
 |   $Revision $
 |
 |   Description:
 |      This file defines the SoFile node class.
 |
 |   Author(s)          : Paul S. Strauss
 |
 ______________  S I L I C O N   G R A P H I C S   I N C .  ____________
 _______________________________________________________________________
 */

package jscenegraph.database.inventor.nodes;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import de.javagl.obj.*;
import jscenegraph.coin3d.inventor.misc.SoGLImage;
import jscenegraph.coin3d.inventor.nodes.SoTexture;
import jscenegraph.coin3d.inventor.nodes.SoTexture2;
import jscenegraph.coin3d.inventor.nodes.SoVertexProperty;
import jscenegraph.database.inventor.*;
import jscenegraph.database.inventor.actions.SoAction;
import jscenegraph.database.inventor.actions.SoCallbackAction;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.actions.SoGetBoundingBoxAction;
import jscenegraph.database.inventor.actions.SoGetMatrixAction;
import jscenegraph.database.inventor.actions.SoHandleEventAction;
import jscenegraph.database.inventor.actions.SoPickAction;
import jscenegraph.database.inventor.actions.SoSearchAction;
import jscenegraph.database.inventor.errors.SoReadError;
import jscenegraph.database.inventor.fields.SoFieldData;
import jscenegraph.database.inventor.fields.SoSFString;
import jscenegraph.database.inventor.misc.SoChildList;
import jscenegraph.database.inventor.sensors.SoFieldSensor;
import jscenegraph.database.inventor.sensors.SoSensor;
import jscenegraph.database.inventor.sensors.SoSensorCB;
import jscenegraph.port.memorybuffer.MemoryBuffer;
import org.lwjgl.assimp.AIFileIO;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;
import org.lwjglx.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipError;

/**
 * @author Yves Boyadjian
 *
 */

////////////////////////////////////////////////////////////////////////////////
//! Node that reads children from a named file.
/*!
\class SoFile
\ingroup Nodes
This node represents a subgraph that was read from a named input file.
When an SoFile node is written out, just the field containing the
name of the file is written; no children are written out. When an
SoFile is encountered during reading, reading continues from the
named file, and all nodes read from the file are added as hidden
children of the file node.


Whenever the \b name  field changes, any existing children are removed
and the contents of the new file is read in. The file node remembers
what directory the last file was read from and will read the new file
from the same directory after checking the standard list of
directories (see SoInput), assuming the field isn't set to an
absolute path name.


The children of an SoFile node are hidden; there is no way of
accessing or editing them. If you wish to edit the contents of an
SoFile node, you can modify the contents of the named file and
then "touch" the \b name  field (see SoField). Alternatively,
you can use the
copyChildren() method
to get a editable copy of the file node's children. Note that this
does not affect the original file on disk, however.

\par File Format/Default
\par
\code
File {
  name <Undefined file>
}
\endcode

\par Action Behavior
\par
SoGLRenderAction, SoCallbackAction, SoGetBoundingBoxAction, SoGetMatrixAction, SoHandleEventAction
<BR> Traverses its children just as SoGroup does. 
\par
SoRayPickAction
<BR> Traverses its hidden children, but, if intersections are found, generates paths that end at the SoFile node. 
\par
SoWriteAction
<BR> Writes just the \b name  field and no children. 

\par See Also
\par
SoInput, SoPath
*/
////////////////////////////////////////////////////////////////////////////////

public class SoFile extends SoNode {

	private final SoSubNode nodeHeader = SoSubNode.SO_NODE_HEADER(SoFile.class,this);
	   
	   public                                                                     
	    static SoType       getClassTypeId()        /* Returns class type id */   
	                                    { return SoSubNode.getClassTypeId(SoFile.class);  }                   
	  public  SoType      getTypeId()      /* Returns type id      */
	  {
		  return nodeHeader.getClassTypeId();
	  }
	  public                                                                  
	    SoFieldData   getFieldData()  {
		  return nodeHeader.getFieldData();
	  }
	  public  static SoFieldData[] getFieldDataPtr()                              
	        { return SoSubNode.getFieldDataPtr(SoFile.class); }    
	  
	  static boolean searchok = false;

  public
    //! \name Fields
    //@{

    //! Name of file from which to read children.
    final SoSFString          name = new SoSFString();           

  private
    SoChildList         children;

    //! These keep the image and filename fields in sync.
    private SoFieldSensor      nameChangedSensor;
    
    //! Creates a file node with default settings.
    public SoFile() {
    	children = new SoChildList(this);
        nodeHeader.SO_NODE_CONSTRUCTOR(/*SoFile.class*/);
        nodeHeader.SO_NODE_ADD_FIELD(name,"name", ("<Undefined file>"));

        // Set up sensors to read in the file when the filename field
        // changes.
        // Sensors are used instead of field to field connections or raw
        // notification so that the fields can still be attached to/from
        // other fields.
        nameChangedSensor = new SoFieldSensor( new SoSensorCB() {

			@Override
			public void run(Object data, SoSensor sensor) {
				nameChangedCB(data,sensor);
			}
        	
        }
        		, this);
        nameChangedSensor.setPriority(0);
        nameChangedSensor.attach(name);

        readOK = true;
        isBuiltIn = true;
    	
    }

    public void destructor() {
    	nameChangedSensor.destructor();
    	super.destructor();
    }
    

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Reads into instance of SoFile. Returns FALSE on error.
//
// Use: protected

public boolean readInstance(SoInput in, short flags)
//
////////////////////////////////////////////////////////////////////////
{
    // Detach sensor temporarily
    nameChangedSensor.detach();

    // Read field info as usual.
    if (! super.readInstance(in, flags))
        readOK = false;

    // If file name is default, there's a problem, since the default
    // file name is not a valid one
    else if (name.isDefault()) {
        SoReadError.post(in, "\"name\" field of SoFile node was never set");
        readOK = false;
    }
    else {
        // Call nameChangedCB to read in children.  There is a really
        // cool bug that occurs if we let the sensor do this for us.
        // The sensor is called right after notification, in
        // processImmediateQueue.  It would then call nameChanged,
        // which calls SoDB::read, which sets up the directory search
        // path.  If there is another File node in that directory
        // search path, its name field will be set, but, since we are
        // already in the middle of a processImmediateQueue, its field
        // sensor isn't called right away.  The SoDB::read returns,
        // removing the directory it added to the search path,
        // nameChanged returns, and THEN the field sensor for the
        // inner File node goes off.  But, by then it is too late--
        // the directory search path no longer contains the directory
        // of the containing File node.
        nameChangedCB(this, null);
    }

    // Reattach sensor
    nameChangedSensor.attach(name);

    return readOK;
}

private static Path findFileWithExtension(Path parentPath, String extension, boolean anyParent) {
        Path fileName = parentPath.getFileName();
        if(
        fileName != null
        && (fileName.toString().endsWith(extension))
        && ( parentPath.getParent().getFileName() == null
        || fileName.toString().startsWith(parentPath.getParent().getFileName().toString()) || anyParent)) {
            return parentPath;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parentPath)) {
            for (Path file: stream) {
                Path found = findFileWithExtension(file, extension, anyParent);
                if( found != null ) {
                    return found;
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            //System.err.println(x);
        }
        return null;
}
    
////////////////////////////////////////////////////////////////////////
//
//Description:
//Callback that reads in the file when the name field changes.
//
//Use: static, internal
    
    private static void         nameChangedCB(Object data, SoSensor sensor) {
    SoFile f = (SoFile )data;

    f.children.truncate(0);

    String filename = f.name.getValue();

    Path filePath = null;

    try {
        filePath = Paths.get(filename);
    } catch( InvalidPathException e) {
        f.readOK = false;
        return;
    }

    // _______________________________________ Test if it is a zip file
    boolean zip = true;
        FileSystem fs = null;

    try {
        Path zipfile = filePath;
        //ZipFileSystemProvider provider = new ZipFileSystemProvider();
        //Map<String,?> env = Collections.emptyMap();
        //fs = provider.newFileSystem(zipfile,env);
        fs = FileSystems.newFileSystem(zipfile,(ClassLoader)null);
    }
    catch (IOException e) {
        zip = false;
    }
    catch (ProviderNotFoundException e) {
        zip = false;
    }
    catch (UnsupportedOperationException e) {
        zip = false;
    }
    catch (ZipError e) {
        zip = false;
    }
    catch(FileSystemNotFoundException e) {
        zip = false;
    }

    Path ivOrwrl = null;
    if ( fs != null) {
        for (Path root : fs.getRootDirectories()) {
            ivOrwrl = findFileWithExtension(root,".iv",false);
            if (ivOrwrl != null) {
                break;
            }
            ivOrwrl = findFileWithExtension(root,".wrl",false);
            if (ivOrwrl != null) {
                break;
            }
        }
    }

    // Open file
    f.readOK = true;

    final SoInput in = new SoInput();

    boolean found = (ivOrwrl != null) ? in.openFile(ivOrwrl,true) : in.openFile(filename, true);

    if (! /*in.openFile(filename, true)*/found) {
        f.readOK = false;
        SoReadError.post(in, "Can't open included file \""+filename+"\" in File node");
    }

    if (f.readOK) {
        final SoNode[]  node = new SoNode[1];

        // Read children from opened file.

        while (true) {
            if (SoDB.read(in, node)) {
                if (node[0] != null)
                    f.children.append(node[0]);
                else
                    break;
            }
            else {
                f.readOK = false;
                break; // Coin3D
            }
        }
        in.closeFile();
    }
    // Note: if there is an error reading one of the children, the
    // other children will still be added properly...    	
    in.destructor(); //java port

        if (!f.readOK && f.children.getLength() == 0) {
            // try another format
            Path gltf = null;
            Path obj = null;
            if(zip) {
                if ( fs != null) {
                    for (Path root : fs.getRootDirectories()) {
                        gltf = findFileWithExtension(root,".gltf",true);
                        if (gltf != null) {
                            readGLTF(f, gltf);
                            return;
                        }
                    }
                    for (Path root : fs.getRootDirectories()) {
                        obj = findFileWithExtension(root,".obj",true);
                        if (obj != null) {
                            readOBJ(f, obj);
                            return;
                        }
                    }
                }
            }
            else {
                gltf = findFileWithExtension(filePath,".gltf",true);
                obj = findFileWithExtension(filePath,".obj",true);
            }
            if (gltf != null) {
                readGLTF(f, gltf);
            }
            if (obj != null) {
                readOBJ(f, obj);
            }
        }
    }

    private boolean                readOK;         //!< FALSE on read error.
	  

    private static void readGLTF(SoFile f, Path gltf) {

//        AIScene scene = Assimp.aiImportFile(gltf.toFile().toString(),0);

//        URI gltfURI = gltf.toUri();
        GltfModelReader reader = new GltfModelReader();
        try {
            //GltfModel gltfModel = reader.read(gltfURI);
            GltfModel gltfModel = reader.read(gltf);
            List<MaterialModel> materialModels = gltfModel.getMaterialModels();
            for(MaterialModel materialModel : materialModels) {
                if (materialModel instanceof MaterialModelV2) {
                    MaterialModelV2 materialModelV2 = (MaterialModelV2) materialModel;
                    TextureModel baseColorTexture = materialModelV2.getBaseColorTexture();
                    if (baseColorTexture != null) {
                        ImageModel imageModel = baseColorTexture.getImageModel();
                        String imageURI = imageModel.getUri();

                        Path rootDir = gltf.getParent();
                        Path imagePath = rootDir.resolve(imageURI);
                        SoTexture2 texture = textureFromPath(imagePath, false);

                        f.children.append(texture);
                        System.out.println("");
                    }
                }
            }
            List<SceneModel> sceneModels = gltfModel.getSceneModels();
            for (SceneModel sceneModel : sceneModels) {
                List<NodeModel> nodeModels = sceneModel.getNodeModels();
                for (NodeModel nodeModel : nodeModels) {
                    f.children.append(recursiveAddNode(f,nodeModel));
                }
            }

            List<MeshModel> meshModels = gltfModel.getMeshModels();
            List<TextureModel> textureModels = gltfModel.getTextureModels();
            System.out.println(meshModels);
            System.out.println(textureModels);
            f.readOK = true;
        } catch (IOException e) {
            e.printStackTrace();
            f.readOK = false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            f.readOK = false;
        }
    }

    private static SoTexture2 textureFromPath(Path imagePath, boolean obj) throws IOException {

        BufferedImage textureBufferedImage = ImageIO.read(imagePath.toUri().toURL());
        SoTexture2 texture = new SoTexture2();
        //texture.filename.setValue(imagePath.toString());
        int w = textureBufferedImage.getWidth();
        int h = textureBufferedImage.getHeight();
        SbVec2s s = new SbVec2s((short)w,(short)h);
        int nc = 3;
        int numPixels = (int)s.getX()*s.getY();
        int numBytes = numPixels*3;
        MemoryBuffer b = MemoryBuffer.allocateBytes(numBytes);
        int j=0;
        for(int i=0; i< numPixels;i++) {
            int x = i%w;
            int y = obj ? h - i/w -1 : i/w;
            int rgb = textureBufferedImage.getRGB(x, y);
            b.setByte(j, (byte)((rgb & 0x00FF0000) >>> 16)) ; j++;
            b.setByte(j, (byte)((rgb & 0x0000FF00) >>> 8)); j++;
            b.setByte(j, (byte)((rgb & 0x000000FF) >>> 0)); j++;
        }

        texture.image.setValue(s,nc,true,b);

        return texture;
    }

    private static void readOBJ(SoFile f, Path objPath) {
/*
        AIScene scene = Assimp.aiImportFile(objPath.toFile().toString(), Assimp.aiProcess_Triangulate);
        int numMeshes = scene.mNumMeshes();
        int numTextures = scene.mNumTextures();
        int numMaterials = scene.mNumMaterials();
        System.out.println(numMeshes);
        System.out.println(numTextures);
        System.out.println(scene.toString());
*/
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(objPath);
            Obj obj = ObjReader.read(inputStream);
            Obj renderableObj = ObjUtils.convertToRenderable(obj);

            IntBuffer indicesIntBuffer = ObjData.getFaceVertexIndices(renderableObj);
            FloatBuffer positionsFloatBuffer = ObjData.getVertices(renderableObj);
            FloatBuffer texCoordsFloatBuffer = ObjData.getTexCoords(renderableObj, 2);
            FloatBuffer normalsFloatBuffer = ObjData.getNormals(renderableObj);

            List<String> materials = renderableObj.getMtlFileNames();
            if (!materials.isEmpty()) {
                String materialName = materials.get(0);
                Path materialPath = objPath.getParent().resolve(materialName);
                List<Mtl> mtls = MtlReader.read(Files.newInputStream(materialPath));
                if (!mtls.isEmpty()) {
                    Mtl mtl = mtls.get(0);
                    String imageName = mtl.getMapKd();
                    if (imageName != null) {
                        if (imageName.indexOf(" ") != -1) {
                            imageName = imageName.substring(imageName.indexOf(" ") + 1);
                        }
                        System.out.println("Texture File : " + imageName);
                        Path imagePath = objPath.getParent().resolve(imageName);
                        SoTexture2 texture = textureFromPath(imagePath, true);
                        f.children.append(texture);
                    }
                }
            }

            SoGroup group = new SoSeparator();

            SoIndexedFaceSet indexedFaceSet = new SoIndexedFaceSet();

            float[] positionsArray = new float[positionsFloatBuffer.remaining()];
            positionsFloatBuffer.get(positionsArray);

            float[] normalsArray = new float[normalsFloatBuffer.remaining()];
            normalsFloatBuffer.get(normalsArray);

            float[] texCoordsArray = new float[texCoordsFloatBuffer.remaining()];
            texCoordsFloatBuffer.get(texCoordsArray);

            SoVertexProperty vertexProperty = new SoVertexProperty();
            vertexProperty.vertex.setValuesPointer(positionsArray);
            vertexProperty.normal.setValuesPointer(normalsArray);
            vertexProperty.texCoord.setValues(0,texCoordsArray);

            indexedFaceSet.vertexProperty.setValue(vertexProperty);

            int numIndices = indicesIntBuffer.remaining();
            int numTriangles = numIndices/3;
            int[] indicesArray = new int[numTriangles*4];
            for(int triangle = 0 ; triangle < numTriangles; triangle++) {
                indicesArray[triangle*4] = indicesIntBuffer.get(triangle*3);
                indicesArray[triangle*4+1] = indicesIntBuffer.get(triangle*3+1);
                indicesArray[triangle*4+2] = indicesIntBuffer.get(triangle*3+2);
                indicesArray[triangle*4+3] = -1;
            }

            indexedFaceSet.coordIndex.setValuesPointer(indicesArray);
            group.addChild(indexedFaceSet);

            f.children.append(group);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static SoGroup recursiveAddNode(SoFile f, NodeModel nodeModel) {
        SoGroup group = new SoSeparator();

        float[] matrixArray = nodeModel.getMatrix();
        if (matrixArray != null) {
            SbMatrix matrix = new SbMatrix();
            matrix.setValue(matrixArray);
            //matrix.setIdentity();
            SoMatrixTransform matrixTransform = new SoMatrixTransform();
            matrixTransform.matrix.setValue(matrix);
            group.addChild(matrixTransform);
        }

        List<MeshModel> meshModels = nodeModel.getMeshModels();
        for (MeshModel meshModel : meshModels) {
            List<MeshPrimitiveModel> primitiveModels = meshModel.getMeshPrimitiveModels();
            for (MeshPrimitiveModel primitiveModel : primitiveModels) {
                AccessorModel indicesModel = primitiveModel.getIndices();
                Map<String, AccessorModel> attributes = primitiveModel.getAttributes();
                AccessorModel positionsModel = attributes.get("POSITION");
                AccessorModel normalsModel = attributes.get("NORMAL");
                AccessorModel texCoordsModel = attributes.get("TEXCOORD_0");

                if (indicesModel != null && positionsModel != null && normalsModel != null && texCoordsModel != null) {
                    Class indiceDataType =  indicesModel.getComponentDataType();
                    Class positionDataType = positionsModel.getComponentDataType();
                    Class normalDataType = normalsModel.getComponentDataType();
                    Class texCoordDataType = texCoordsModel.getComponentDataType();

                    AccessorData indicesData = indicesModel.getAccessorData();
                    AccessorData positionsData = positionsModel.getAccessorData();
                    AccessorData normalsData = normalsModel.getAccessorData();
                    AccessorData texCoordsData = texCoordsModel.getAccessorData();

                    if (
                            indicesData != null &&
                                    positionsData != null &&
                                    normalsData != null &&
                                    texCoordsData != null &&
                                    (Objects.equals(indiceDataType, int.class) || Objects.equals(indiceDataType, short.class)) &&
                                    Objects.equals(positionDataType, float.class) &&
                                    Objects.equals(normalDataType, float.class) &&
                                    Objects.equals(texCoordDataType, float.class)) {
                        ByteBuffer indicesBuffer = indicesData.createByteBuffer();
                        ByteBuffer positionsBuffer = positionsData.createByteBuffer();
                        ByteBuffer normalsBuffer = normalsData.createByteBuffer();
                        ByteBuffer texCoordsBuffer = texCoordsData.createByteBuffer();
                        if (indicesBuffer != null && positionsBuffer != null && normalsBuffer != null && texCoordsBuffer != null) {

                            FloatBuffer positionsFloatBuffer = positionsBuffer.asFloatBuffer();
                            FloatBuffer normalsFloatBuffer = normalsBuffer.asFloatBuffer();
                            FloatBuffer texCoordsFloatBuffer = texCoordsBuffer.asFloatBuffer();

                            int[] indicesArray = null;
                            if(Objects.equals(indiceDataType, int.class)) {
                                IntBuffer indicesIntBuffer = indicesBuffer.asIntBuffer();

                                int numIndices = indicesIntBuffer.remaining();
                                int numTriangles = numIndices / 3;
                                indicesArray = new int[numTriangles * 4];
                                for (int triangle = 0; triangle < numTriangles; triangle++) {
                                    indicesArray[triangle * 4] = indicesIntBuffer.get(triangle * 3);
                                    indicesArray[triangle * 4 + 1] = indicesIntBuffer.get(triangle * 3 + 1);
                                    indicesArray[triangle * 4 + 2] = indicesIntBuffer.get(triangle * 3 + 2);
                                    indicesArray[triangle * 4 + 3] = -1;
                                }
                            }
                            else if (Objects.equals(indiceDataType, short.class)) {
                                ShortBuffer indicesIntBuffer = indicesBuffer.asShortBuffer();

                                int numIndices = indicesIntBuffer.remaining();
                                int numTriangles = numIndices / 3;
                                indicesArray = new int[numTriangles * 4];
                                for (int triangle = 0; triangle < numTriangles; triangle++) {
                                    indicesArray[triangle * 4] = shortToInt(indicesIntBuffer.get(triangle * 3));
                                    indicesArray[triangle * 4 + 1] = shortToInt(indicesIntBuffer.get(triangle * 3 + 1));
                                    indicesArray[triangle * 4 + 2] = shortToInt(indicesIntBuffer.get(triangle * 3 + 2));
                                    indicesArray[triangle * 4 + 3] = -1;
                                }
                            }

                            float[] positionsArray = new float[positionsFloatBuffer.remaining()];
                            positionsFloatBuffer.get(positionsArray);

                            float[] normalsArray = new float[normalsFloatBuffer.remaining()];
                            normalsFloatBuffer.get(normalsArray);

                            float[] texCoordsArray = new float[texCoordsFloatBuffer.remaining()];
                            texCoordsFloatBuffer.get(texCoordsArray);

                            SoVertexProperty vertexProperty = new SoVertexProperty();
                            vertexProperty.vertex.setValuesPointer(positionsArray);
                            vertexProperty.normal.setValuesPointer(normalsArray);
                            vertexProperty.texCoord.setValues(0,texCoordsArray);

                            SoIndexedFaceSet indexedFaceSet = new SoIndexedFaceSet();
                            indexedFaceSet.vertexProperty.setValue(vertexProperty);
                            indexedFaceSet.coordIndex.setValuesPointer(indicesArray);
                            group.addChild(indexedFaceSet);
                        }
                    }
                }
            }
        }

        List<NodeModel> childrens = nodeModel.getChildren();
        for (NodeModel child : childrens) {
            group.addChild(recursiveAddNode(f, child));
        }
        return group;
    }

    private static int shortToInt(short shortValue) {
        return Short.toUnsignedInt(shortValue);
    }

    private static ByteBuffer pathToByteBuffer(Path path) {
        long fileLength = path.toFile().length();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer((int)fileLength);

        try {
            byte[] data = Files.readAllBytes(path);
            byteBuffer.put(ByteBuffer.wrap(data));
            byteBuffer.flip();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return byteBuffer;
    }

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Returns pointer to children.
//
// Use: internal

public SoChildList getChildren()
//
////////////////////////////////////////////////////////////////////////
{
    return (SoChildList ) children;
}

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Implements typical traversal.
//
// Use: extender public

    final int[]         numIndices = new int[1];
    final int[][]   indices = new int[1][];

    public void SoFile_doAction(SoAction action)
//
////////////////////////////////////////////////////////////////////////
{
    numIndices[0] = 0;
    indices[0] = null;

    if (action.getPathCode(numIndices, indices) == SoAction.PathCode.IN_PATH)
        getChildren().traverse(action, 0, indices[0][numIndices[0] - 1]);

    else
        getChildren().traverse(action);
}

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Does the callback action
//
// Use: extender

public void callback(SoCallbackAction action)
//
////////////////////////////////////////////////////////////////////////
{
    SoFile_doAction(action);
}

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Does the GL render action
//
// Use: extender

public void GLRender(SoGLRenderAction action)

////////////////////////////////////////////////////////////////////////
{
    SoFile_doAction(action);
}

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Does the get bounding box action.  This takes care of averaging
//    the centers of all children to get a combined center.
//
// Use: extender

public void getBoundingBox(SoGetBoundingBoxAction action)
//
////////////////////////////////////////////////////////////////////////
{
    final SbVec3f     totalCenter = new SbVec3f(0,0,0);
    int         numCenters = 0;
    final int[]         numIndices = new int[1];
    final int[][]   indices = new int[1][];
    int         lastChild;

    if (action.getPathCode(numIndices, indices) == SoAction.PathCode.IN_PATH)
        lastChild = indices[0][numIndices[0] - 1];
    else
        lastChild = getChildren().getLength() - 1;

    for (int i = 0; i <= lastChild; i++) {
        getChildren().traverse(action, i, i);
        if (action.isCenterSet()) {
            totalCenter.operator_add_equal(action.getCenter());
            numCenters++;
            action.resetCenter();
        }
    }
    // Now, set the center to be the average:
    if (numCenters != 0)
        action.setCenter(totalCenter.operator_div(numCenters), false);
}

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Does the handle event thing
//
// Use: extender

public void handleEvent(SoHandleEventAction action)
//
////////////////////////////////////////////////////////////////////////
{
    SoFile_doAction(action);
}

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Pick.
//
// Use: extender

public void pick(SoPickAction action)
//
////////////////////////////////////////////////////////////////////////
{
    SoFile_doAction(action);
}

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Implements get matrix action.
//
// Use: extender

public void getMatrix(SoGetMatrixAction action)
//
////////////////////////////////////////////////////////////////////////
{
    final int[]         numIndices = new int[1];
    final  int[][]   indices = new int[1][];

    // Only need to compute matrix if group is a node in middle of
    // current path chain or is off path chain (since the only way
    // this could be called if it is off the chain is if the group is
    // under a group that affects the chain).

    switch (action.getPathCode(numIndices, indices)) {

      case NO_PATH:
        break;

      case IN_PATH:
          getChildren().traverse(action, 0, indices[0][numIndices[0] - 1]);
        break;

      case BELOW_PATH:
        break;

      case OFF_PATH:
          getChildren().traverse(action);
        break;
    }
}

public void search(SoSearchAction action)
{
  super.search(action); // always include this node in the search

  // only search children if the user has requested it
  if (searchok) SoFile_doAction((SoAction)action);
}
    
    
////////////////////////////////////////////////////////////////////////
//
// Description:
//    This initializes the SoFile class.
//
// Use: internal

public static void initClass()
//
////////////////////////////////////////////////////////////////////////
{
    SO__NODE_INIT_CLASS(SoFile.class, "File", SoNode.class);
}

}
