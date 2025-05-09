/**
 * 
 */
package jscenegraph.coin3d.fxviz.nodes;

import com.jogamp.opengl.GL2;

import jscenegraph.coin3d.glue.cc_glglue;
import jscenegraph.coin3d.inventor.misc.SoShaderGenerator;
import jscenegraph.coin3d.inventor.nodes.SoCoordinate3;
import jscenegraph.coin3d.inventor.nodes.SoFragmentShader;
import jscenegraph.coin3d.inventor.nodes.SoSceneTexture2;
import jscenegraph.coin3d.inventor.nodes.SoShaderObject;
import jscenegraph.coin3d.inventor.nodes.SoTextureUnit;
import jscenegraph.coin3d.inventor.nodes.SoTransparencyType;
import jscenegraph.coin3d.inventor.nodes.SoVertexShader;
import jscenegraph.coin3d.misc.SoGL;
import jscenegraph.coin3d.misc.Tidbits;
import jscenegraph.coin3d.shaders.inventor.nodes.*;
import jscenegraph.database.inventor.SbBox3f;
import jscenegraph.database.inventor.SbColor;
import jscenegraph.database.inventor.SbMatrix;
import jscenegraph.database.inventor.SbVec2f;
import jscenegraph.database.inventor.SbVec2s;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SbVec4f;
import jscenegraph.database.inventor.SbXfBox3f;
import jscenegraph.database.inventor.SoFullPath;
import jscenegraph.database.inventor.SoPath;
import jscenegraph.database.inventor.actions.SoAction;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.elements.SoColorPacker;
import jscenegraph.database.inventor.elements.SoGLCacheContextElement;
import jscenegraph.database.inventor.elements.SoLazyElement;
import jscenegraph.database.inventor.elements.SoMaterialBindingElement;
import jscenegraph.database.inventor.elements.SoNormalElement;
import jscenegraph.database.inventor.elements.SoOverrideElement;
import jscenegraph.database.inventor.elements.SoTextureOverrideElement;
import jscenegraph.database.inventor.elements.SoTextureQualityElement;
import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.SoCallback;
import jscenegraph.database.inventor.nodes.SoCamera;
import jscenegraph.database.inventor.nodes.SoDirectionalLight;
import jscenegraph.database.inventor.nodes.SoFaceSet;
import jscenegraph.database.inventor.nodes.SoLight;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoOrthographicCamera;
import jscenegraph.database.inventor.nodes.SoPerspectiveCamera;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.database.inventor.nodes.SoShapeHints;
import jscenegraph.port.Destroyable;
import jscenegraph.port.FloatArray;
import jscenegraph.port.SbVec2fArray;

/**
 * @author Yves Boyadjian
 *
 */
public class SoShadowLightCache implements Destroyable {

	  final SbMatrix matrix = new SbMatrix();
  final SbMatrix nearmatrix = new SbMatrix();

	  SoPath path; //ptr
	  SoLight light; //ptr
	  SoSceneTexture2 depthmap; //ptr

      SoSceneTexture2 neardepthmap; //ptr

	  SoNode depthmapscene; //ptr
      SoNode neardepthmapscene; //ptr

	  SoSceneTexture2 gaussmap; //ptr

	  SoCamera camera; //ptr
      SoCamera nearCamera;

	  float farval;
	  float nearval;

  float farvalnear;
  float nearvalnear;

  int texunit;

	  int neartexunit; // for near shadow

	  int lightid;
	  float precision; // YB : Precision used to compute ShadowLightCache

	  SoSeparator bboxnode; //ptr
	  SoShaderProgram vsm_program; //ptr
	  SoShaderParameter1i shadowmapid; //ptr
  SoShaderParameter1i nearshadowmapid; //ptr

	  SoShaderParameter1f vsm_farval; //ptr
	  SoShaderParameter1f vsm_nearval; //ptr

  SoShaderParameter1f vsm_farvalnear; //ptr
  SoShaderParameter1f vsm_nearvalnear; //ptr

  SoShaderParameter1f vsm_nearflag;

  SoShaderParameter1f fragment_farval; //ptr
	  SoShaderParameter1f fragment_nearval; //ptr

  SoShaderParameter1f fragment_farvalnear; //ptr
  SoShaderParameter1f fragment_nearvalnear; //ptr


  SoShaderParameter4f fragment_lightplane; //ptr
  SoShaderParameter4f fragment_lightnearplane;

	  final SoShaderGenerator vsm_vertex_generator = new SoShaderGenerator();
	  final SoShaderGenerator vsm_fragment_generator = new SoShaderGenerator();
	  SoShaderParameter1f maxshadowdistance; //ptr

	  final SoColorPacker colorpacker = new SoColorPacker();
	  final SbColor color = new SbColor();

	  SoState destructorState;
	  
  public SoShadowLightCache(SoState state,
                     SoPath path,
                     SoShadowGroup sg,
                     SoNode scene,
                     SoNode bboxscene,
                     int gausskernelsize,
                     float gaussstandarddeviation)
  {
    cc_glglue glue = SoGL.cc_glglue_instance(SoGLCacheContextElement.get(state));

    int maxsize = 2048*8;
    int maxtexsize = 2048*8;

    // Testing for maximum proxy texture size doesn't seem to work, so
    // we just have to hardcode the maximum size to 2048 for now.  We
    // still use the proxy texture test in case the maximum size is
    // something smaller than 2048 though.  pederb, 2007-05-03

    // glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE_EXT, &maxsize);
    // glGetIntegerv(GL_MAX_TEXTURE_SIZE, &maxtexsize);
    // if (maxtexsize < maxsize) maxsize = maxtexsize;

    int internalformat = GL2.GL_RGBA16F/*_ARB*/;
    int format = GL2.GL_RGBA;
    int type = GL2.GL_FLOAT;

    while (!SoGL.coin_glglue_is_texture_size_legal(glue, maxsize, maxsize, 0, internalformat, format, type, true) && maxsize != 0 ) {
      maxsize >>= 1;
    }
    
    if(maxsize == 0) { // Can happen on CentOS 7 in VirtualBox    	
    	GL2 gl2 = state.getGL2();
    	
    	final int[] maxsizep = new int[1];
        gl2.glGetIntegerv(GL2.GL_MAX_RENDERBUFFER_SIZE, maxsizep);
    	final int[] maxtexsizep = new int[1];
        gl2.glGetIntegerv(GL2.GL_MAX_TEXTURE_SIZE, maxtexsizep);
        
        maxsize = maxsizep[0];
        maxtexsize = maxtexsizep[0];
        
        if (maxtexsize < maxsize) maxsize = maxtexsize;    	
    }
    
    // YB : no more need to have a power of two size in modern opengl
    precision = sg.precision.getValue();

    int TEXSIZE = /*Tidbits.coin_geq_power_of_two(*/(int) Math.min(precision * maxtexsize,Math.min(maxsize, maxtexsize))/*)*/;

    this.lightid = -1;
    this.vsm_program = null;
    this.vsm_farval = null;
    this.vsm_nearval = null;

    this.vsm_farvalnear = null;
    this.vsm_nearvalnear = null;
    this.vsm_nearflag = null;

    this.gaussmap = null;
    this.texunit = -1;
    this.neartexunit = -1;
    this.bboxnode = new SoSeparator();
    this.bboxnode.ref();

    this.shadowmapid = new SoShaderParameter1i();
    this.shadowmapid.ref();

    this.nearshadowmapid = new SoShaderParameter1i();
    this.nearshadowmapid.ref();

    this.fragment_farval = new SoShaderParameter1f();
    this.fragment_farval.ref();

    this.fragment_farvalnear = new SoShaderParameter1f();
    this.fragment_farvalnear.ref();

    this.fragment_nearval = new SoShaderParameter1f();
    this.fragment_nearval.ref();

    this.fragment_nearvalnear = new SoShaderParameter1f();
    this.fragment_nearvalnear.ref();

    this.fragment_lightplane = new SoShaderParameter4f();
    this.fragment_lightplane.ref();

    this.fragment_lightnearplane = new SoShaderParameter4f();
    this.fragment_lightnearplane.ref();

    this.maxshadowdistance = new SoShaderParameter1f();
    this.maxshadowdistance.ref();

    this.path = path.copy();
    this.path.ref();
    assert((SoFullPath.cast(path)).getTail().isOfType(SoLight.getClassTypeId()));

    this.light = (SoLight)(SoFullPath.cast(path)).getTail();
    this.light.ref();

    this.createVSMProgram();

    this.depthmap = new SoSceneTexture2();
    this.depthmap.ref();
    this.depthmap.transparencyFunction.setValue(SoSceneTexture2.TransparencyFunction.NONE);
    this.depthmap.size.setValue( new SbVec2s((short)TEXSIZE, (short)TEXSIZE));
    this.depthmap.wrapS.setValue( SoSceneTexture2.Wrap.CLAMP_TO_BORDER);
    this.depthmap.wrapT.setValue( SoSceneTexture2.Wrap.CLAMP_TO_BORDER);

    this.neardepthmap = new SoSceneTexture2();
    this.neardepthmap.ref();
    this.neardepthmap.transparencyFunction.setValue(SoSceneTexture2.TransparencyFunction.NONE);
    this.neardepthmap.size.setValue( new SbVec2s((short)TEXSIZE, (short)TEXSIZE));
    this.neardepthmap.wrapS.setValue( SoSceneTexture2.Wrap.CLAMP_TO_BORDER);
    this.neardepthmap.wrapT.setValue( SoSceneTexture2.Wrap.CLAMP_TO_BORDER);

    if (this.vsm_program != null) {
      this.depthmap.type.setValue( SoSceneTexture2.Type./*RGBA32F*/DEPTH32);
      this.depthmap.backgroundColor.setValue( new SbVec4f(1.0f, 1.0f, 1.0f, 1.0f));

      this.neardepthmap.type.setValue( SoSceneTexture2.Type./*RGBA32F*/DEPTH32);
      this.neardepthmap.backgroundColor.setValue( new SbVec4f(1.0f, 1.0f, 1.0f, 1.0f));
    }
    else {
      this.depthmap.type.setValue( SoSceneTexture2.Type.DEPTH);

      this.neardepthmap.type.setValue( SoSceneTexture2.Type.DEPTH);
    }
    SoTransparencyType tt = new SoTransparencyType();
    tt.value.setValue( SoTransparencyType.Type.NONE);

    this.depthmap.sceneTransparencyType.setValue(tt);

    tt = new SoTransparencyType();
    tt.value.setValue( SoTransparencyType.Type.NONE);

    this.neardepthmap.sceneTransparencyType.setValue(tt);

    if (this.light.isOfType(SoDirectionalLight.getClassTypeId())) {
      this.camera = new SoOrthographicCamera();
      this.nearCamera = new SoOrthographicCamera();
    }
    else {
      this.camera = new SoPerspectiveCamera();
      this.nearCamera = new SoPerspectiveCamera();
    }
    this.camera.ref();
    this.camera.viewportMapping.setValue( SoCamera.ViewportMapping.LEAVE_ALONE);

    this.nearCamera.ref();
    this.nearCamera.viewportMapping.setValue( SoCamera.ViewportMapping.LEAVE_ALONE);

    SoSeparator sep = new SoSeparator();
    sep.addChild(this.camera);

    SoSeparator nearSep = new SoSeparator();
    nearSep.addChild(this.nearCamera);

    SoCallback cb = new SoCallback();
    cb.setCallback(SoShadowLightCache::shadowmap_glcallback, this);

    sep.addChild(cb);
    nearSep.addChild(cb);

    if (this.vsm_program != null) {
      sep.addChild(this.vsm_program);
      nearSep.addChild(this.vsm_program);
    }

    if (scene.isOfType(SoShadowGroup.getClassTypeId())) {
      SoShadowGroup g = (SoShadowGroup) scene;
      for (int i = 0; i < g.getNumChildren(); i++) {
        sep.addChild(g.getChild(i));
        nearSep.addChild(g.getChild(i));
      }
    }
    else {
      sep.addChild(scene);
      nearSep.addChild(scene);
    }

    if (bboxscene.isOfType(SoShadowGroup.getClassTypeId())) {
      SoShadowGroup g = (SoShadowGroup) bboxscene;
      for (int i = 0; i < g.getNumChildren(); i++) {
        this.bboxnode.addChild(g.getChild(i));
      }
    }
    else {
      this.bboxnode.addChild(bboxscene);
    }

    cb = new SoCallback();
    cb.setCallback(SoShadowLightCache::shadowmap_post_glcallback, this);
    sep.addChild(cb);
    nearSep.addChild(cb);

    this.depthmap.scene.setValue( sep);
    this.neardepthmap.scene.setValue( nearSep);

    this.depthmapscene = sep;
    this.depthmapscene.ref();

    this.neardepthmapscene = nearSep;
    this.neardepthmapscene.ref();

    this.matrix.copyFrom( SbMatrix.identity());
    this.nearmatrix.copyFrom( SbMatrix.identity());

    if (gausskernelsize > 0) {
      this.gaussmap = new SoSceneTexture2();
      this.gaussmap.ref();
      this.gaussmap.transparencyFunction.setValue( SoSceneTexture2.TransparencyFunction.NONE);
      this.gaussmap.size.setValue( new SbVec2s((short)TEXSIZE, (short)TEXSIZE));
      this.gaussmap.wrapS.setValue( SoSceneTexture2.Wrap.CLAMP_TO_BORDER);
      this.gaussmap.wrapT.setValue( SoSceneTexture2.Wrap.CLAMP_TO_BORDER);

      this.gaussmap.type.setValue( SoSceneTexture2.Type.RGBA32F);
      this.gaussmap.backgroundColor.setValue( new SbVec4f(1.0f, 1.0f, 1.0f, 1.0f));

      SoShaderProgram shader = this.createGaussFilter(TEXSIZE, gausskernelsize, gaussstandarddeviation);
      this.gaussmap.scene.setValue( this.createGaussSG(shader, this.depthmap));
    }
  }
	  

	  public void destructor() {
		    if (this.depthmapscene != null) this.depthmapscene.unref();
        if (this.neardepthmapscene != null) this.neardepthmapscene.unref();
		    if (this.bboxnode != null) this.bboxnode.unref();
		    if (this.maxshadowdistance != null) this.maxshadowdistance.unref();
		    if (this.vsm_program != null) this.vsm_program.unref();
		    if (this.vsm_farval != null) this.vsm_farval.unref();
		    if (this.vsm_nearval != null) this.vsm_nearval.unref();
        if (this.vsm_farvalnear != null) this.vsm_farvalnear.unref();
        if (this.vsm_nearvalnear != null) this.vsm_nearvalnear.unref();
        if (this.vsm_nearflag != null) this.vsm_nearflag.unref();
		    if (this.fragment_farval != null) this.fragment_farval.unref();
        if (this.fragment_farvalnear != null) this.fragment_farvalnear.unref();
		    if (this.shadowmapid != null) this.shadowmapid.unref();
        if (this.nearshadowmapid != null) this.nearshadowmapid.unref();
		    if (this.fragment_nearval != null) this.fragment_nearval.unref();
        if (this.fragment_nearvalnear != null) this.fragment_nearvalnear.unref();
		    if (this.fragment_lightplane != null) this.fragment_lightplane.unref();
        if (this.fragment_lightnearplane != null) this.fragment_lightnearplane.unref();
		    if (this.light != null) this.light.unref();
		    if (this.path != null) this.path.unref();
		    if (this.gaussmap != null) this.gaussmap.unref();
		    if (this.depthmap != null) {
              this.depthmap.destructorState = destructorState;
		      this.depthmap.unref();
              this.depthmap = null;
            }

		    if (this.neardepthmap != null) {
              this.neardepthmap.destructorState = destructorState;
		      this.neardepthmap.unref();
              this.neardepthmap = null;
            }

		    if (this.camera != null) this.camera.unref();
		  }

		  public float getPrecision() {
    return precision;
          }

public void
createVSMProgram()
{
  SoShaderProgram program = new SoShaderProgram();
  program.ref();

  SoVertexShader vshader = new SoVertexShader();
  SoFragmentShader fshader = new SoFragmentShader();

  program.shaderObject.set1Value(0, vshader);
  program.shaderObject.set1Value(1, fshader);

  SoShaderGenerator vgen = this.vsm_vertex_generator;
  SoShaderGenerator fgen = this.vsm_fragment_generator;

  vgen.reset(false);
  vgen.setVersion("#version 400 core"); // YB : necessary for Intel Graphics HD 630

  vgen.addDeclaration("uniform mat4 s4j_ModelViewMatrix;",false);
  vgen.addDeclaration("uniform mat4 s4j_ProjectionMatrix;",false);
  //vgen.addDeclaration("uniform mat3 s4j_NormalMatrix;",false);

  boolean dirlight = this.light.isOfType(SoDirectionalLight.getClassTypeId());

  vgen.addDeclaration("layout (location = 0) in vec3 s4j_Vertex;",false);

  vgen.addDeclaration("out vec3 light_vec;", false);
  vgen.addMainStatement("light_vec = (s4j_ModelViewMatrix * vec4(s4j_Vertex,1.0)).xyz;\n"+
                        "gl_Position = s4j_ProjectionMatrix * s4j_ModelViewMatrix * vec4(s4j_Vertex,1.0);//ftransform();");

  vshader.sourceProgram.setValue( vgen.getShaderProgram());
  vshader.sourceType.setValue( SoShaderObject.SourceType.GLSL_PROGRAM);



  final SoShaderStateMatrixParameter mvs = new SoShaderStateMatrixParameter();
  mvs.name.setValue("s4j_ModelViewMatrix");
  mvs.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.MODELVIEW);

  final SoShaderStateMatrixParameter ps = new SoShaderStateMatrixParameter();
  ps.name.setValue("s4j_ProjectionMatrix");
  ps.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.PROJECTION);

//  final SoShaderStateMatrixParameter ns = new SoShaderStateMatrixParameter();
//  ns.name.setValue("s4j_NormalMatrix");
//  ns.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.MODELVIEW);
//  ns.matrixTransform.setValue(SoShaderStateMatrixParameter.MatrixTransform.INVERSE_TRANSPOSE_3);

  vshader.parameter.set1Value(vshader.parameter.getNum(), mvs);
  vshader.parameter.set1Value(vshader.parameter.getNum(), ps);
  //vshader.parameter.set1Value(vshader.parameter.getNum(), ns);


  fgen.reset(false);
  fgen.setVersion("#version 400 core"); // YB : necessary for Intel Graphics HD 630

  fgen.addDeclaration("layout(location = 0) out float s4j_FragColor;",false);
  
//#ifdef DISTRIBUTE_FACTOR
  String str;
  str = "const float DISTRIBUTE_FACTOR = "+SoShadowGroupP.DISTRIBUTE_FACTOR+";\n";
  fgen.addDeclaration(str, false);
//#endif
  fgen.addDeclaration("in vec3 light_vec;", false);
  fgen.addDeclaration("uniform float farval;", false);
  fgen.addDeclaration("uniform float nearval;", false);
  fgen.addDeclaration("uniform float farvalnear;", false);
  fgen.addDeclaration("uniform float nearvalnear;", false);
  fgen.addDeclaration("uniform float nearflag;",false);
  if (!dirlight)  {
    fgen.addMainStatement("float lfar = (length(light_vec) - nearval) / (farval-nearval);\n");
    fgen.addMainStatement("float lnear = (length(light_vec) - nearvalnear) / (farvalnear-nearvalnear);\n");
  }
  else {
    fgen.addMainStatement("float lfar = (-light_vec.z - nearval) / (farval-nearval);\n");
    fgen.addMainStatement("float lnear = (-light_vec.z - nearvalnear) / (farvalnear-nearvalnear);\n");
  }
  fgen.addMainStatement( "float l = lfar*(1-nearflag)+ nearflag*lnear;\n");
  fgen.addMainStatement("s4j_FragColor = l * 2.0 - 1.0;\n");
//  fgen.addMainStatement(
////#ifdef DISTRIBUTE_FACTOR
//                        "vec2 m = vec2(l, l*l);\n"+
//                        "vec2 f = fract(m * DISTRIBUTE_FACTOR);\n"+
//
////#ifdef USE_NEGATIVE
//                        "s4j_FragColor.rg = (m - (f / DISTRIBUTE_FACTOR)) * 2.0 - vec2(1.0, 1.0);\n"+
//                        "s4j_FragColor.ba = f * 2.0 - vec2(1.0, 1.0);\n"
////#else // USE_NEGATIVE
////                        "gl_FragColor.rg = m - (f / DISTRIBUTE_FACTOR);\n"
////                        "gl_FragColor.ba = f;\n"
////#endif // ! USE_NEGATIVE
////#else // DISTRIBUTE_FACTOR
////#ifdef USE_NEGATIVE
////                        "gl_FragColor = vec4(l*2.0 - 1.0, l*l*2.0 - 1.0, 0.0, 0.0);"
////#else // USE_NEGATIVE
////                        "gl_FragColor = vec4(l, l*l, 0.0, 0.0);"
////#endif // !USE_NEGATIVE
////#endif // !DISTRIBUTE_FACTOR
//                        );
  fshader.sourceProgram.setValue( fgen.getShaderProgram());
  fshader.sourceType.setValue( SoShaderObject.SourceType.GLSL_PROGRAM);

  this.vsm_program = program;
  this.vsm_program.ref();

  this.vsm_farval = new SoShaderParameter1f();
  this.vsm_farval.ref();
  this.vsm_farval.name.setValue("farval");

  this.vsm_farvalnear = new SoShaderParameter1f();
  this.vsm_farvalnear.ref();
  this.vsm_farvalnear.name.setValue("farvalnear");

  this.vsm_nearval = new SoShaderParameter1f();
  this.vsm_nearval.ref();
  this.vsm_nearval.name.setValue( "nearval");

  this.vsm_nearvalnear = new SoShaderParameter1f();
  this.vsm_nearvalnear.ref();
  this.vsm_nearvalnear.name.setValue( "nearvalnear");

  this.vsm_nearflag = new SoShaderParameter1f();
  this.vsm_nearflag.ref();
  this.vsm_nearflag.name.setValue( "nearflag");

  fshader.parameter.set1Value(0, this.vsm_farval);
  fshader.parameter.set1Value(1, this.vsm_nearval);

  fshader.parameter.set1Value(2, this.vsm_farvalnear);
  fshader.parameter.set1Value(3, this.vsm_nearvalnear);

  fshader.parameter.set1Value(4, this.vsm_nearflag);
}

private final SbXfBox3f xbox = new SbXfBox3f(); // SINGLE_THREAD
  private final SbXfBox3f nearXbox = new SbXfBox3f(); // SINGLE_THREAD

public SbBox3f toCameraSpace(final SbXfBox3f  worldbox, final SbMatrix mat)
{
  SoCamera cam = this.camera;
  mat.constructor();//final SbMatrix mat = new SbMatrix();
  xbox.copyFrom(worldbox);
  mat.setTranslate( cam.position.getValue().operator_minus());
  xbox.transform(mat);
  mat.copyFrom( cam.orientation.getValue().getMatrix().inverse(mat)); // java port
  xbox.transform(mat);
  return xbox.project();
}

  public SbBox3f toNearCameraSpace(final SbXfBox3f  worldbox, final SbMatrix mat)
  {
    SoCamera cam = this.nearCamera;
    mat.constructor();//final SbMatrix mat = new SbMatrix();
    nearXbox.copyFrom(worldbox);
    mat.setTranslate( cam.position.getValue().operator_minus());
    nearXbox.transform(mat);
    // mat can be used as dummy
    mat.copyFrom( cam.orientation.getValue().getMatrix().inverse(mat)); // java port
    nearXbox.transform(mat);
    return nearXbox.project();
  }

public static void
shadowmap_glcallback(Object closure, SoAction action)
{
  if (action.isOfType(SoGLRenderAction.getClassTypeId())) {
    SoState state = action.getState();
    SoLazyElement.setLightModel(state, SoLazyElement.LightModel.BASE_COLOR.getValue());
    SoTextureQualityElement.set(state, 0.0f);
    SoMaterialBindingElement.set(state,/* null,*/ SoMaterialBindingElement.Binding.OVERALL);
    SoNormalElement.set(state, null, 0, null/*, false*/,0,null); //YB


    SoOverrideElement.setNormalVectorOverride(state, null, true);
    SoOverrideElement.setMaterialBindingOverride(state, null, true);
    SoOverrideElement.setLightModelOverride(state, null, true);
    SoTextureOverrideElement.setQualityOverride(state, true);
  }
}


public static void
shadowmap_post_glcallback(Object closure, SoAction action)
{
  if (action.isOfType(SoGLRenderAction.getClassTypeId())) {
    // for debugging the shadow map
    //((SoShadowLightCache)closure).dumpBitmap("D:/shadowj.rgb");
    // nothing to do yet
  }
}


public SoShaderProgram 
createGaussFilter( int texsize, int size, float gaussstandarddeviation)
{
  SoVertexShader vshader = new SoVertexShader();
  SoFragmentShader fshader = new SoFragmentShader();
  SoShaderProgram program = new SoShaderProgram();

  SoShaderParameterArray2f offset = new SoShaderParameterArray2f();
  offset.name.setValue("offset");
  SoShaderParameterArray1f kernel = new SoShaderParameterArray1f();
  kernel.name.setValue("kernelvalue");
  SoShaderParameter1i baseimage = new SoShaderParameter1i();
  baseimage.name.setValue("baseimage");
  baseimage.value.setValue(0);

  int kernelsize = size*size;

  offset.value.setNum(kernelsize);
  kernel.value.setNum(kernelsize);

  final SoShaderGenerator fgen = new SoShaderGenerator();
  String str;

  str = "const int KernelSize = "+kernelsize+";";
  fgen.addDeclaration(str, false);
  fgen.addDeclaration("uniform vec2 offset[KernelSize];", false);
  fgen.addDeclaration("uniform float kernelvalue[KernelSize];", false);
  fgen.addDeclaration("uniform sampler2D baseimage;", false);

  fgen.addMainStatement(
                        "int i;\n"+
                        "vec4 sum = vec4(0.0);\n"+
                        "for (i = 0; i < KernelSize; i++) {\n"+
                        "  vec4 tmp = texture2D(baseimage, gl_TexCoord[0].st + offset[i]);\n"+
                        "  sum += tmp * kernelvalue[i];\n"+
                        "}\n"+
                        "gl_FragColor = sum;\n"
                        );

  double sigma = (double) gaussstandarddeviation;
  int center = size / 2;
  float dt = 1.0f / (float)(texsize);

  SbVec2fArray offsetptr = offset.value.startEditing();
  FloatArray kernelptr = kernel.value.startEditing();

  int c = 0;
  for (int y = 0; y < size; y++) {
    int dy = Math.abs(y - center);
    for (int x = 0; x < size; x++) {
      int dx = Math.abs(x - center);

      kernelptr.set(c, (float) ((1.0 /  (2.0 * Math.PI * sigma * sigma)) * Math.exp(- (double)(dx*dx + dy*dy) / (2.0 * sigma * sigma))));
      offsetptr.setO(c, new SbVec2f((float)(x-center) * dt, (float)(y-center)*dt));
      c++;

    }
  }
  offset.value.finishEditing();
  kernel.value.finishEditing();

  program.shaderObject.set1Value(0, vshader);
  program.shaderObject.set1Value(1, fshader);

  fshader.sourceProgram.setValue( fgen.getShaderProgram());
  fshader.sourceType.setValue(SoShaderObject.SourceType.GLSL_PROGRAM);

  fshader.parameter.set1Value(0, offset);
  fshader.parameter.set1Value(1, kernel);
  fshader.parameter.set1Value(2, baseimage);

  final SoShaderGenerator vgen = new SoShaderGenerator();
  vgen.addMainStatement("gl_TexCoord[0] = gl_Vertex;\n");
  vgen.addMainStatement("gl_Position = ftransform();");

  vshader.sourceProgram.setValue( vgen.getShaderProgram());
  vshader.sourceType.setValue(SoShaderObject.SourceType.GLSL_PROGRAM);

  fgen.destructor(); // java port
  vgen.destructor();
  return program;
}

public SoSeparator
createGaussSG(SoShaderProgram program, SoSceneTexture2 tex)
{
  SoSeparator sep = new SoSeparator();
  SoOrthographicCamera camera = new SoOrthographicCamera();
  SoShapeHints sh = new SoShapeHints();

  float verts[][] = {
    {0.0f, 0.0f, 0.0f},
    {1.0f, 0.0f, 0.0f},
    {1.0f, 1.0f, 0.0f},
    {0.0f, 1.0f, 0.0f}

  };

  sh.vertexOrdering.setValue(SoShapeHints.VertexOrdering.COUNTERCLOCKWISE);
  sh.faceType.setValue(SoShapeHints.FaceType.CONVEX);
  sh.shapeType.setValue(SoShapeHints.ShapeType.SOLID);

  sep.addChild(sh);

  camera.position.setValue(new SbVec3f(0.5f, 0.5f, 2.0f));
  camera.height.setValue(1.0f);
  camera.aspectRatio.setValue(1.0f);
  camera.viewportMapping.setValue(SoCamera.ViewportMapping.LEAVE_ALONE);

  sep.addChild(camera);
  SoTextureUnit unit = new SoTextureUnit();
  unit.unit.setValue(0);
  sep.addChild(unit);

  sep.addChild(tex);
  sep.addChild(program);

  SoCoordinate3 coord = new SoCoordinate3();
  sep.addChild(coord);

  coord.point.setValues(0,4,verts);

  SoFaceSet fs = new SoFaceSet();
  fs.numVertices.setValue(4);
  sep.addChild(fs);

  return sep;
}

int dumpBitmap(String filename) {
    int width;
    int height;
    int comp;
    
    GL2 gl2 = new GL2() {};

    int[] vp = new int[4];
    gl2.glGetIntegerv(GL2.GL_VIEWPORT, vp, 0);
    width = vp[2];
    height = vp[3];
    comp = 1;

    byte[] bytes = new byte[width*height*comp];
    gl2.glFlush();
    gl2.glReadPixels(0,0, (short)width, (short)height, GL2.GL_RED, GL2.GL_UNSIGNED_BYTE, bytes);
    
    int blacks=0;
    int size = width*height*comp;
    for(int i=0;i<size;i++) {
    	if(bytes[i]!= -1) {
    		blacks++;
    	}
    }

//    int x, y, c;
//    unsigned char * tmpbuf;
//    unsigned char buf[500];
//
//    FILE * fp = fopen(filename, "wb");
//    if (!fp) {
//      return 0;
//    }
//
//    write_short(fp, 0x01da); /* imagic */
//    write_short(fp, 0x0001); /* raw (no rle yet) */
//
//    if (comp == 1)
//      write_short(fp, 0x0002); /* 2 dimensions (heightmap) */
//    else
//      write_short(fp, 0x0003); /* 3 dimensions */
//
//    write_short(fp, (unsigned short) width);
//    write_short(fp, (unsigned short) height);
//    write_short(fp, (unsigned short) comp);
//
//    memset(buf, 0, 500);
//    buf[7] = 255; /* set maximum pixel value to 255 */
//    strcpy((char *)buf+8, "http://www.coin3d.org");
//    fwrite(buf, 1, 500, fp);
//
//    tmpbuf = (unsigned char *) malloc(width);
//
//    for (c = 0; c < comp; c++) {
//      for (y = 0; y < height; y++) {
//        for (x = 0; x < width; x++) {
//          tmpbuf[x] = bytes[x * comp + y * comp * width + c];
//        }
//        fwrite(tmpbuf, 1, width, fp);
//      }
//    }
//    free(tmpbuf);
//    fclose(fp);
//    delete[] bytes;
    return 1;
  }

}
