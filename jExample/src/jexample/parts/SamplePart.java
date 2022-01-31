package jexample.parts;

import java.awt.*;
import java.io.IOException;
import java.net.URL;


import com.jogamp.opengl.GL2;
import jscenegraph.coin3d.fxviz.nodes.SoShadowGroup;
import jscenegraph.coin3d.inventor.nodes.*;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderParameterMatrix;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderProgram;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderStateMatrixParameter;
import jscenegraph.database.inventor.SbColor;
import jscenegraph.database.inventor.SbMatrix;
import jscenegraph.database.inventor.SbVec2f;
import jscenegraph.database.inventor.SoDB;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.actions.SoGLRenderAction.TransparencyType;
import jscenegraph.database.inventor.elements.SoModelMatrixElement;
import jscenegraph.database.inventor.elements.SoProjectionMatrixElement;
import jscenegraph.database.inventor.elements.SoViewingMatrixElement;
import jscenegraph.database.inventor.engines.SoElapsedTime;
import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.*;
import jscenegraph.freecad.SoFC;
import jsceneviewerawt.inventor.qt.SoQt;
import jsceneviewerawt.inventor.qt.SoQtCameraController;
import jsceneviewerawt.inventor.qt.viewers.SoQtExaminerViewer;
import jsceneviewerawt.inventor.qt.viewers.SoQtFullViewer;
import org.lwjgl.opengl.GLDebugMessageCallback;

import javax.swing.*;

import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL43C.*;

public class SamplePart {

	public static SoQtExaminerViewer viewer;


	public static void createComposite(Container parent) {
		//parent.setLayout(new GridLayout(1, false));

		SoQt.init("demo");
		//SoFC.init();
		//SoDB.setDelaySensorTimeout(new SbTime(10.0));
		//SoDB.setRealTimeInterval(new SbTime(10.0));
		
		int style = 0;
		
		viewer = new SoQtExaminerViewer(SoQtFullViewer.BuildFlag.BUILD_ALL, SoQtCameraController.Type.BROWSER,parent,style);
	    //viewer.setColorBitDepth (10);
		//viewer.setAntialiasing(true, 16);
		viewer.setHeadlight(true);
		//viewer.getSceneHandler().setTransparencyType(TransparencyType.DELAYED_BLEND);
		//viewer.getSceneHandler().setTransparencyType(TransparencyType.BLEND);
		viewer.getSceneHandler().setTransparencyType(TransparencyType.SORTED_LAYERS_BLEND);		
		
//		viewer.format().debug = true;
//		viewer.format().majorVersion = 3;
//		viewer.format().minorVersion = 0;
//		//viewer.format().profile = GLData.Profile.COMPATIBILITY;
		
	    viewer.buildWidget(style);
	    
	    String fileStr = "jExample/examples_iv/duck.iv";

	    viewer.setSceneGraph(
	    		//SoMaterialBindingExample.createDemoSceneSoMaterialBinding()
	    		//SoMaterialBindingExample.createDemoSceneSoMaterialIndexedBinding()
	    		//IndexedTriangleStrip.createDemoSceneSoIndexedTriangleStrip()
	    		createDemoScenePerformance()
	    		//createDemoSceneSoMaterialShapeBinding()
	    		//SoFaceSetTest.createDemoSceneSoFaceSet()
	    		//SoIndexedFaceSetTest.createDemoSceneSoIndexedFaceSet()
	    		//createDemoSceneTimeWatch()
	    		//TextureCoordinatePlane.computeRoot()
	    		//TextureCoordinates.createScene()
	    		//Text2.createScene()
	    		//Text3.createScene()
	    		//FancyText3.createScene()
	    		//Selection.createScene(viewer)
	    		//Selection.createSceneSelection()
	    		//Manips.createScene()
	    		//(new PickTrackball()).createScene()
	    		//FrolickingWords.createRoot()
	    		//Balance.createScene(viewer)
	    		//Obelisque.makeObeliskFaceSet()
	    		//Drapeau.makePennant()
	    		//Arche.makeArch()
	    		//RotatingSensor.create("C:/eclipseWorkspaces/examples_iv/transparentbluecylinder.iv")
	    		//Canards.create(fileStr/*"C:\\eclipseWorkspaces\\inventor-2.1.5-10.src\\inventor\\apps\\examples\\data\\duck.iv"*/)
	    		//Orbits.main()
	    		//WorldAnimated.main()
	    		//DualWorld.main()
	    		//createDemoSceneTransparentCubes()
	    		);
	    //CameraSensor.attach(viewer);
	    viewer.viewAll();
	}


	static SoNode createDemoScene()
{
    SoSeparator scene = new SoSeparator();
    scene.ref();
//    SoTransformManip manip = new SoTransformManip();
//    scene.addChild (manip);

	SoCube cube = new SoCube();
	cube.height.setValue(0.5f);
	cube.depth.setValue(0.5f);
	cube.width.setValue(0.5f);

    scene.addChild (cube);
    //scene.addChild (new SoCone());
    //scene.addChild (new SoSphere());
    return scene;
}

	static SoNode createDemoSceneRedCone()
	{
		SoSeparator root = new SoSeparator();
		root.ref();

		// material
		SoMaterial material = new SoMaterial();
		material.diffuseColor.setValue(1.0f, 0.0f, 0.0f);
		root.addChild(material);

		root.addChild(new SoCone());
		return root;
	}

	static SoNode createDemoSceneRedConeEngine()
	{
		// create scene root
		  SoSeparator root = new SoSeparator();
		  root.ref();
		 
		  // camera
		  SoPerspectiveCamera camera = new SoPerspectiveCamera();
		  root.addChild(camera);
		 
		  // light
		  root.addChild(new SoDirectionalLight());
		 
		  // material
		  SoMaterial material = new SoMaterial();
		  material.diffuseColor.setValue(1.0f, 0.0f, 0.0f);
		  root.addChild(material);
		 
		  // rotation node
		  SoRotationXYZ rotXYZ = new SoRotationXYZ();
		  rotXYZ.axis.setValue(SoRotationXYZ.Axis.X);
		  root.addChild(rotXYZ);
		 
		  // connect engine to rotation node
		  SoElapsedTime counter = new SoElapsedTime();
		  rotXYZ.angle.connectFrom(counter.timeOut);
		 
		  // cone
		  root.addChild(new SoCone());
		  
		  return root;
	}


static SoSeparator createPlanet(float radius, float distance,
                          float initialAngle, SbColor color)
{
  SoSeparator root = new SoSeparator();

  // material of planet
  SoMaterial material = new SoMaterial();
  material.diffuseColor.setValue(color);
  root.addChild(material);

  // revolution around the Sun
  SoRotationXYZ rotY = new SoRotationXYZ();
  rotY.axis.setValue( SoRotationXYZ.Axis.Y);
  root.addChild(rotY);

  // connect engine to rotation node
  SoElapsedTime counter = new SoElapsedTime();
  counter.speed.setValue(20.f/distance);
  rotY.angle.connectFrom(counter.timeOut);

  // translation from the Sun
  SoTranslation trans = new SoTranslation();
  trans.translation.setValue(distance*(float)(Math.cos(initialAngle)), 0.f,
                              distance*(float)(-Math.sin(initialAngle)));
  root.addChild(trans);

  // planet geometry
  SoSphere sphere = new SoSphere();
  sphere.radius.setValue(radius);
  root.addChild(sphere);

  return root;
}

	
	static SoNode createDemoSceneSolarSystem() {
		  // create scene root
		  SoSeparator root = new SoSeparator();
		  root.ref();


		  // model of the Sun

		  // diffuse material
		  SoMaterial sunMat = new SoMaterial();
		  sunMat.diffuseColor.setValue(1.0f, 1.0f, 0.3f);
		  root.addChild(sunMat);

		  // sphere of radius 10
		  SoSphere sun = new SoSphere();
		  sun.radius.setValue(10.f);
		  root.addChild(sun);


		  // model of the Earth
		  root.addChild(createPlanet(4.0f, 45.f, 0.f,
		                              new SbColor(0.7f, 0.7f, 1.0f)));

		  // model of the Mercury
		  root.addChild(createPlanet(2.0f, 20.f, (float)(4*Math.PI/3),
		                              new SbColor(1.0f, 0.3f, 0.3f)));

		  // model of the Venus
		  root.addChild(createPlanet(3.0f, 30.f, (float)(3*Math.PI/3),
		                              new SbColor(1.0f, 0.6f, 0.0f)));

		  return root;
	}
	

static SoSeparator createPlanetSolarLight(float radius, float distance,
                          float initialAngle, SbColor color)
{
  SoSeparator root = new SoSeparator();

  // material of planet
  // ambient and diffuse color is set to the planet color,
  // specular and emissive colors are left on their default value (0.f, 0.f, 0.f)
  SoMaterial material = new SoMaterial();
  material.ambientColor.setValue(color);
  material.diffuseColor.setValue(color);
  root.addChild(material);

  // revolution around the Sun
  SoRotationXYZ rotY = new SoRotationXYZ();
  rotY.axis.setValue(SoRotationXYZ.Axis.Y);
  root.addChild(rotY);

  // connect engine to rotation node
  SoElapsedTime counter = new SoElapsedTime();
  counter.speed.setValue(20.f/distance);
  rotY.angle.connectFrom(counter.timeOut);

  // translation from the Sun
  SoTranslation trans = new SoTranslation();
  trans.translation.setValue(distance*(float)(Math.cos(initialAngle)), 0.f,
                              distance*(float)(-Math.sin(initialAngle)));
  root.addChild(trans);

  // planet geometry
  SoSphere sphere = new SoSphere();
  sphere.radius.setValue(radius);
  root.addChild(sphere);

  return root;
}


	static SoNode createDemoSceneSolarLight() {
		  // create scene root
		  SoSeparator root = new SoSeparator();
		  root.ref();

		  // environment
		  // ambientColor is left on white (default value)
		  // while we set its intensity on 0.25.
		  SoEnvironment envir = new SoEnvironment();
		  envir.ambientIntensity.setValue(0.25f);
		  root.addChild(envir);


		  // model of the Sun

		  // ambient and diffuse color is set to dark yellow,
		  // emissive on light yellow and specular is left on its default value (black)
		  SoMaterial sunMat = new SoMaterial();
		  sunMat.ambientColor.setValue(0.5f, 0.5f, 0.15f);
		  sunMat.diffuseColor.setValue(0.5f, 0.5f, 0.15f);
		  sunMat.emissiveColor.setValue(0.9f, 0.9f, 0.3f);
		  root.addChild(sunMat);

		  // sphere of radius 10
		  SoSphere sun = new SoSphere();
		  sun.radius.setValue(10.f);
		  root.addChild(sun);

		  // light in the middle of the Sun
		  // all the objects in the scene graph following the light
		  // will be lit by the light
		  SoPointLight sunLight = new SoPointLight();
		  sunLight.location.setValue(0.f, 0.f, 0.f);
		  sunLight.intensity.setValue(0.75f);
		  root.addChild(sunLight);


		  // model of the Earth
		  root.addChild(createPlanetSolarLight(4.0f, 45.f, 0.f,
		                              new SbColor(0.7f, 0.7f, 1.0f)));

		  // model of the Mercury
		  root.addChild(createPlanetSolarLight(2.0f, 20.f, (float)(4*Math.PI/3),
		                              new SbColor(1.0f, 0.3f, 0.3f)));

		  // model of the Venus
		  root.addChild(createPlanetSolarLight(3.0f, 30.f, (float)(3*Math.PI/3),
		                              new SbColor(1.0f, 0.6f, 0.0f)));

		  return root;
	}	
	

// size of the skybox divided by two
private static final float SKY_BOX_SIZE2  = 50;

// coordinates of vertices for sky box
static final float[] P0  = { -SKY_BOX_SIZE2, -SKY_BOX_SIZE2, -SKY_BOX_SIZE2 };
static final float[] P1 ={  SKY_BOX_SIZE2, -SKY_BOX_SIZE2, -SKY_BOX_SIZE2 };
static final float[] P2 ={ -SKY_BOX_SIZE2,  SKY_BOX_SIZE2, -SKY_BOX_SIZE2 };
static final float[] P3 ={  SKY_BOX_SIZE2,  SKY_BOX_SIZE2, -SKY_BOX_SIZE2 };
static final float[] P4 ={ -SKY_BOX_SIZE2, -SKY_BOX_SIZE2,  SKY_BOX_SIZE2 };
static final float[] P5 ={  SKY_BOX_SIZE2, -SKY_BOX_SIZE2,  SKY_BOX_SIZE2 };
static final float[] P6 ={ -SKY_BOX_SIZE2,  SKY_BOX_SIZE2,  SKY_BOX_SIZE2 };
static final float[] P7 ={  SKY_BOX_SIZE2,  SKY_BOX_SIZE2,  SKY_BOX_SIZE2 };


// indices of sky box
static float[][][] skyBoxVertices/*[6][4][3]*/ =
{
  { P0, P1, P2, P3 }, // sky00
  { P6, P4, P2, P0 }, // sky06
  { P5, P4, P7, P6 }, // sky12
  { P5, P7, P1, P3 }, // sky18
  { P2, P3, P6, P7 }, // skyN0
  { P4, P5, P0, P1 }  // skyS0
};



static SoSeparator createSkyBox()
{
  SoSeparator root = new SoSeparator();

  // Set light model to BASE_COLOR. It means,
  // we will render pre-lit scene.
  // All the geometry will receive the color by its diffuse color
  // while lights and other material components have no effect.
  SoLightModel lmodel = new SoLightModel();
  lmodel.model.setValue(SoLightModel.Model.BASE_COLOR);
  root.addChild(lmodel);

  // SoBaseColor is setting just diffuse color of the material.
  // It is often used to speed-up rendering or together with
  // pre-lit scene (lightModel set to BASE_COLOR).
  SoBaseColor baseColor = new SoBaseColor();
  baseColor.rgb.setValue(1.f, 1.f, 1.f);
  root.addChild(baseColor);

  // SoDrawStyle is set to FILLED by default - resulting in filled triangles.
  // We are going to set it to LINES to render it as wire frame.
  SoDrawStyle drawStyle = new SoDrawStyle();
  drawStyle.style.setValue(SoDrawStyle.Style.LINES);
  drawStyle.lineWidth.setValue(2.f);
  root.addChild(drawStyle);

  // Coordinates of vertices
  // They are taken from skyBoxVertices.
  SoCoordinate3 coords = new SoCoordinate3();
  coords.point.setValues(0, 4*6, skyBoxVertices/*[0]*/);
  root.addChild(coords);

  // SoTriangleStripSet node is rendering triangles.
  // Each value in numVertices is determining the number of vertices
  // from SoCoordinate3 that will be used for the particular triangle strip.
  SoTriangleStripSet strip = new SoTriangleStripSet();
  for (int i=0; i<6; i++)
    strip.numVertices.set1Value(i, 4);
  root.addChild(strip);

  return root;
}



static SoSeparator createPlanetWireBox(float radius, float distance,
                          float initialAngle, SbColor color)
{
  SoSeparator root = new SoSeparator();

  // material of planet
  // ambient and diffuse color is set to the planet color,
  // specular and emissive colors are left on their default value (0.f, 0.f, 0.f)
  SoMaterial material = new SoMaterial();
  material.ambientColor.setValue(color);
  material.diffuseColor.setValue(color);
  root.addChild(material);

  // revolution around the Sun
  SoRotationXYZ rotY = new SoRotationXYZ();
  rotY.axis.setValue( SoRotationXYZ.Axis.Y);
  root.addChild(rotY);

  // connect engine to rotation node
  SoElapsedTime counter = new SoElapsedTime();
  counter.speed.setValue(20.f/distance);
  rotY.angle.connectFrom(counter.timeOut);

  // translation from the Sun
  SoTranslation trans = new SoTranslation();
  trans.translation.setValue(distance*(float)(+Math.cos(initialAngle)), 0.f,
                              distance*(float)(-Math.sin(initialAngle)));
  root.addChild(trans);

  // planet geometry
  SoSphere sphere = new SoSphere();
  sphere.radius.setValue(radius);
  root.addChild(sphere);

  return root;
}

	
	
	static SoNode createDemoSceneWireBox() {
		  // create scene root
		  SoSeparator root = new SoSeparator();
		  root.ref();

		  root.addChild(createSkyBox());

		  // environment
		  // ambientColor is left on white (default value)
		  // while we set its intensity on 0.25.
		  SoEnvironment envir = new SoEnvironment();
		  envir.ambientIntensity.setValue(0.25f);
		  root.addChild(envir);


		  // model of the Sun

		  // ambient and diffuse color is set to dark yellow,
		  // emissive on light yellow and specular is left on its default value (black)
		  SoMaterial sunMat = new SoMaterial();
		  sunMat.ambientColor.setValue(0.5f, 0.5f, 0.15f);
		  sunMat.diffuseColor.setValue(0.5f, 0.5f, 0.15f);
		  sunMat.emissiveColor.setValue(0.9f, 0.9f, 0.3f);
		  root.addChild(sunMat);

		  // sphere of radius 10
		  SoSphere sun = new SoSphere();
		  sun.radius.setValue(10.f);
		  root.addChild(sun);

		  // light in the middle of the Sun
		  // all the objects in the scene graph following the light
		  // will be lit by the light
		  SoPointLight sunLight = new SoPointLight();
		  sunLight.location.setValue(0.f, 0.f, 0.f);
		  sunLight.intensity.setValue(0.75f);
		  root.addChild(sunLight);


		  // model of the Earth
		  root.addChild(createPlanetWireBox(4.0f, 45.f, 0.f,
		                              new SbColor(0.7f, 0.7f, 1.0f)));

		  // model of the Mercury
		  root.addChild(createPlanetWireBox(2.0f, 20.f, (float)(4*Math.PI/3),
		                              new SbColor(1.0f, 0.3f, 0.3f)));

		  // model of the Venus
		  root.addChild(createPlanetWireBox(3.0f, 30.f, (float)(3*Math.PI/3),
		                              new SbColor(1.0f, 0.6f, 0.0f)));


		  return root;
	}
	

	// size of the skybox divided by two
	//private static final float SKY_BOX_SIZE2 = 50;

	// skybox vertices
	static float[][] skyBoxVerticesWireBoxIndexed =
	{
	  {-SKY_BOX_SIZE2, -SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
	   {SKY_BOX_SIZE2, -SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
	  {-SKY_BOX_SIZE2,  SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
	   {SKY_BOX_SIZE2,  SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
	  {-SKY_BOX_SIZE2, -SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
	   {SKY_BOX_SIZE2, -SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
	  {-SKY_BOX_SIZE2,  SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
	   {SKY_BOX_SIZE2,  SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
	};

	// skybox indices
	// indices are used to access skyBoxVertices when rendering,
	// -1 is used to terminate triangle strip
	static int skyBoxIndices[] =
	{
	  0, 1, 2, 3, -1,  // sky00
	  6, 4, 2, 0, -1,  // sky06
	  5, 4, 7, 6, -1,  // sky12
	  5, 7, 1, 3, -1,  // sky18
	  2, 3, 6, 7, -1,  // skyN0
	  4, 5, 0, 1, -1,  // skyS0
	};



	static SoSeparator createSkyBoxWireBoxIndexed()
	{
	  SoSeparator root = new SoSeparator();

	  // Set light model to BASE_COLOR. It means,
	  // we will render pre-lit scene.
	  // All the geometry will receive the color by its diffuse color
	  // while lights and other material components have no effect.
	  SoLightModel lmodel = new SoLightModel();
	  lmodel.model.setValue(SoLightModel.Model.BASE_COLOR);
	  root.addChild(lmodel);

	  // SoBaseColor is setting just diffuse color of the material.
	  // It is often used to speed-up rendering or together with
	  // pre-lit scene (lightModel set to BASE_COLOR).
	  SoBaseColor baseColor = new SoBaseColor();
	  baseColor.rgb.setValue(1.f, 1.f, 1.f);
	  root.addChild(baseColor);

	  // SoDrawStyle is set to FILLED by default - resulting in filled triangles.
	  // We are going to set it to LINES to render it as wire frame.
	  SoDrawStyle drawStyle = new SoDrawStyle();
	  drawStyle.style.setValue(SoDrawStyle.Style.LINES);
	  drawStyle.lineWidth.setValue(2.f);
	  root.addChild(drawStyle);

	  // Coordinates of vertices
	  // They are taken from skyBoxVertices.
	  SoCoordinate3 coords = new SoCoordinate3();
	  coords.point.setValues(0, 8, skyBoxVerticesWireBoxIndexed);
	  root.addChild(coords);

	  // SoIndexedTriangleStripSet node is rendering triangles and triangle strips.
	  // Each value in coordIndex is used as the index to SoCoordinate3
	  // for particular vertex.
	  SoIndexedTriangleStripSet strip = new SoIndexedTriangleStripSet();
	  strip.coordIndex.setValues(0, 5*6, skyBoxIndices);
	  root.addChild(strip);

	  return root;
	}



	static SoSeparator createPlanetWireBoxIndexed(float radius, float distance,
	                          float initialAngle, SbColor color)
	{
	  SoSeparator root = new SoSeparator();

	  // material of planet
	  // ambient and diffuse color is set to the planet color,
	  // specular and emissive colors are left on their default value (0.f, 0.f, 0.f)
	  SoMaterial material = new SoMaterial();
	  material.ambientColor.setValue(color);
	  material.diffuseColor.setValue(color);
	  root.addChild(material);

	  // revolution around the Sun
	  SoRotationXYZ rotY = new SoRotationXYZ();
	  rotY.axis.setValue(SoRotationXYZ.Axis.Y);
	  root.addChild(rotY);

	  // connect engine to rotation node
	  SoElapsedTime counter = new SoElapsedTime();
	  counter.speed.setValue(20.f/distance);
	  rotY.angle.connectFrom(counter.timeOut);

	  // translation from the Sun
	  SoTranslation trans = new SoTranslation();
	  trans.translation.setValue(distance*(float)(+Math.cos(initialAngle)), 0.f,
	                              distance*(float)(-Math.sin(initialAngle)));
	  root.addChild(trans);

	  // planet geometry
	  SoSphere sphere = new SoSphere();
	  sphere.radius.setValue(radius);
	  root.addChild(sphere);

	  return root;
	}

	


	
	
	static SoNode createDemoSceneWireBoxIndexed() {
		
		  // create scene root
		  SoSeparator root = new SoSeparator();
		  root.ref();

		  // create skybox
		  root.addChild(createSkyBoxWireBoxIndexed());

		  // environment
		  // ambientColor is left on white (default value)
		  // while we set its intensity on 0.25.
		  SoEnvironment envir = new SoEnvironment();
		  envir.ambientIntensity.setValue(0.25f);
		  root.addChild(envir);


		  // model of the Sun

		  // ambient and diffuse color is set to dark yellow,
		  // emissive on light yellow and specular is left on its default value (black)
		  SoMaterial sunMat = new SoMaterial();
		  sunMat.ambientColor.setValue(0.5f, 0.5f, 0.15f);
		  sunMat.diffuseColor.setValue(0.5f, 0.5f, 0.15f);
		  sunMat.emissiveColor.setValue(0.9f, 0.9f, 0.3f);
		  root.addChild(sunMat);

		  // sphere of radius 10
		  SoSphere sun = new SoSphere();
		  sun.radius.setValue(10.f);
		  root.addChild(sun);

		  // light in the middle of the Sun
		  // all the objects in the scene graph following the light
		  // will be lit by the light
		  SoPointLight sunLight = new SoPointLight();
		  sunLight.location.setValue(0.f, 0.f, 0.f);
		  sunLight.intensity.setValue(0.75f);
		  root.addChild(sunLight);


		  // model of the Earth
		  root.addChild(createPlanetWireBoxIndexed(4.0f, 45.f, 0.f,
		                              new SbColor(0.7f, 0.7f, 1.0f)));

		  // model of the Mercury
		  root.addChild(createPlanetWireBoxIndexed(2.0f, 20.f, (float)(4*Math.PI/3),
		                              new SbColor(1.0f, 0.3f, 0.3f)));

		  // model of the Venus
		  root.addChild(createPlanetWireBoxIndexed(3.0f, 30.f, (float)(3*Math.PI/3),
		                              new SbColor(1.0f, 0.6f, 0.0f)));

		  return root;
	}

	// size of the skybox divided by two
	//#define SKY_BOX_SIZE2 50


	// skybox vertices
	static float skyBoxVertices1[][] =
	{
		{-SKY_BOX_SIZE2, -SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
		{SKY_BOX_SIZE2, -SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
		{-SKY_BOX_SIZE2,  SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
		{SKY_BOX_SIZE2,  SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
		{-SKY_BOX_SIZE2, -SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
		{SKY_BOX_SIZE2, -SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
		{-SKY_BOX_SIZE2,  SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
		{SKY_BOX_SIZE2,  SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
	};


	// skybox indices
	// indices are used to access skyBoxVertices when rendering,
	// -1 is used to terminate triangle strip
	static int skyBoxIndices1[][] =
	{
		{0, 1, 2, 3, -1},  // sky00
		{6, 4, 2, 0, -1},  // sky06
		{5, 4, 7, 6, -1},  // sky12
		{5, 7, 1, 3, -1},  // sky18
		{2, 3, 6, 7, -1},  // skyN0
		{4, 5, 0, 1, -1},  // skyS0
	};


	// indices for texturing coordinates
	static int skyBoxTexCoordIndex[] =
	{
		0, 1, 2, 3, -1,
	};


	static SoSeparator createSkyBox1()
	{
		int i;
		SoSeparator root = new SoSeparator();

		// Set light model to BASE_COLOR. It means,
		// we will render pre-lit scene.
		// All the geometry will receive the color by its diffuse color
		// while lights and other material components have no effect.
		SoLightModel lmodel = new SoLightModel();
		lmodel.model.setValue(SoLightModel.Model.BASE_COLOR);
		root.addChild(lmodel);

		// Coordinates of vertices
		// They are taken from skyBoxVertices.
		SoCoordinate3 coords = new SoCoordinate3();
		coords.point.setValues(0, 8, skyBoxVertices1);
		root.addChild(coords);

		// Texturing coordinates
		// (applied on all 6 sides of the box)
		SoTextureCoordinate2 texCoord = new SoTextureCoordinate2();
		texCoord.point.set1Value(0, new SbVec2f(0, 0));
		texCoord.point.set1Value(1, new SbVec2f(1, 0));
		texCoord.point.set1Value(2, new SbVec2f(0, 1));
		texCoord.point.set1Value(3, new SbVec2f(1, 1));
		root.addChild(texCoord);

		// Textures
		// There is one for each sky box side.
		// DECAL means to replace original object color by the texture (no lighting is applied).
		SoTexture2[] textures = new SoTexture2[6];
		for (i = 0; i<6; i++) {
			textures[i] = new SoTexture2();
			textures[i].model.setValue(SoTexture2.Model.DECAL);
		}
		textures[0].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/sky00.gif");
		textures[1].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/sky06.gif");
		textures[2].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/sky12.gif");
		textures[3].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/sky18.gif");
		textures[4].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/skyN0.gif");
		textures[5].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/skyS0.gif");

		for (i = 0; i<6; i++) {
			root.addChild(textures[i]);

			// SoIndexedTriangleStripSet node is rendering triangles and triangle strips.
			// Each value in coordIndex is used as the index to SoCoordinate3
			// for particular vertex.
			SoIndexedTriangleStripSet strip = new SoIndexedTriangleStripSet();
			strip.coordIndex.setValues(0, 5, skyBoxIndices1[i]);
			strip.textureCoordIndex.setValues(0, 5, skyBoxTexCoordIndex);
			root.addChild(strip);
		}

		return root;
	}



	SoSeparator createPlanet1(float radius, float distance,
		float initialAngle, SbColor color, String textureName)
	{
		SoSeparator root = new SoSeparator();

		// material of planet
		if (textureName != null) {
			SoMaterial material = new SoMaterial();
			material.ambientColor.setValue(new SbColor(1.f, 1.f, 1.f));
			material.diffuseColor.setValue(new SbColor(1.f, 1.f, 1.f));
			root.addChild(material);
		}
		else {
			SoMaterial material = new SoMaterial();
			material.ambientColor.setValue(color);
			material.diffuseColor.setValue(color);
			root.addChild(material);
		}

		// texture of planet
		if (textureName != null) {
			SoTexture2 texture = new SoTexture2();
			texture.filename.setValue(textureName);
			root.addChild(texture);
		}

		// revolution around the Sun
		SoRotationXYZ rotY = new SoRotationXYZ();
		rotY.axis.setValue(SoRotationXYZ.Axis.Y);
		root.addChild(rotY);

		// connect engine to rotation node
		SoElapsedTime counter = new SoElapsedTime();
		counter.speed.setValue(20.f / distance);
		rotY.angle.connectFrom(counter.timeOut);

		// translation from the Sun
		SoTranslation trans = new SoTranslation();
		trans.translation.setValue(distance*(float)(+Math.cos(initialAngle)), 0.f,
			distance*(float)(-Math.sin(initialAngle)));
		root.addChild(trans);

		// planet geometry
		SoSphere sphere = new SoSphere();
		sphere.radius.setValue(radius);
		root.addChild(sphere);

		return root;
	}

	SoNode createDemoSceneSkyBox1() {

		// create scene root
		SoSeparator root = new SoSeparator();
		root.ref();

		// create skybox
		root.addChild(createSkyBox1());

		// environment
		// ambientColor is left on white (default value)
		// while we set its intensity on 0.25.
		SoEnvironment envir = new SoEnvironment();
		envir.ambientIntensity.setValue(0.25f);
		root.addChild(envir);


		// model of the Sun

		// ambient and diffuse color is set to dark yellow,
		// emissive on light yellow and specular is left on its default value (black)
		SoMaterial sunMat = new SoMaterial();
		sunMat.ambientColor.setValue(0.5f, 0.5f, 0.15f);
		sunMat.diffuseColor.setValue(0.5f, 0.5f, 0.15f);
		sunMat.emissiveColor.setValue(0.9f, 0.9f, 0.3f);
		root.addChild(sunMat);

		// sphere of radius 10
		SoSphere sun = new SoSphere();
		sun.radius.setValue(10.f);
		root.addChild(sun);

		// light in the middle of the Sun
		// all the objects in the scene graph following the light
		// will be lit by the light
		SoPointLight sunLight = new SoPointLight();
		sunLight.location.setValue(0.f, 0.f, 0.f);
		sunLight.intensity.setValue(0.75f);
		root.addChild(sunLight);


		// model of the Earth
		root.addChild(createPlanet1(4.0f, 45.f, 0.f,
			new SbColor(0.7f, 0.7f, 1.0f), "G:/eclipseWorkspaces/2-7-SkyBox1/earth.jpg"));

		// model of the Mercury
		root.addChild(createPlanet1(2.0f, 20.f, (float)(4 * Math.PI / 3),
			new SbColor(1.0f, 0.3f, 0.3f), null));

		// model of the Venus
		root.addChild(createPlanet1(3.0f, 30.f, (float)(3 * Math.PI / 3),
			new SbColor(1.0f, 0.6f, 0.0f), "G:/eclipseWorkspaces/2-7-SkyBox1/venus.jpg"));

		return root;
	}

	
// skybox indices
// indices are used to access skyBoxVertices when rendering,
// -1 is used to terminate triangle strip
static int skyBoxIndices2[][] =
{
  {0, 1, 2, 3, -1},  // sky00
  {6, 4, 2, 0, -1},  // sky06
  {5, 4, 7, 6, -1},  // sky12
  {5, 7, 1, 3, -1},  // sky18
  {2, 3, 6, 7, -1},  // skyN0
  {4, 5, 0, 1, -1},  // skyS0
};


// skybox vertices
static float skyBoxVertices2[][] =
{
  {-SKY_BOX_SIZE2, -SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
	  { SKY_BOX_SIZE2, -SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
  {-SKY_BOX_SIZE2,  SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
   {SKY_BOX_SIZE2,  SKY_BOX_SIZE2, -SKY_BOX_SIZE2},
  {-SKY_BOX_SIZE2, -SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
	   { SKY_BOX_SIZE2, -SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
  {-SKY_BOX_SIZE2,  SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
   {SKY_BOX_SIZE2,  SKY_BOX_SIZE2,  SKY_BOX_SIZE2},
};


// indices for texturing coordinates
static int skyBoxTexCoordIndex2[] =
{
  0, 1, 2, 3, -1,
};

	
static SoSeparator createSkyBox2(SoCamera camera)
{
  int i;
  SoSeparator root = new SoSeparator();

  // Translation
  // We are moving sky box in a way that its centre
  // is always in the camera position
  SoTranslation trans = new SoTranslation();
  trans.translation.connectFrom(camera.position);
  root.addChild(trans);

  // Set light model to BASE_COLOR. It means,
  // we will render pre-lit scene.
  // All the geometry will receive the color by its diffuse color
  // while lights and other material components have no effect.
  SoLightModel lmodel = new SoLightModel();
  lmodel.model.setValue(SoLightModel.Model.BASE_COLOR);
  root.addChild(lmodel);

  // Coordinates of vertices
  // They are taken from skyBoxVertices.
  SoCoordinate3 coords = new SoCoordinate3();
  coords.point.setValues(0, 8, skyBoxVertices2);
  root.addChild(coords);

  // Texturing coordinates
  // (applied on all 6 sides of the box)
  SoTextureCoordinate2 texCoord = new SoTextureCoordinate2();
  texCoord.point.set1Value(0, new SbVec2f(0,0));
  texCoord.point.set1Value(1, new SbVec2f(1,0));
  texCoord.point.set1Value(2, new SbVec2f(0,1));
  texCoord.point.set1Value(3, new SbVec2f(1,1));
  root.addChild(texCoord);

  // Textures
  // There is one for each sky box side.
  // DECAL means to replace original object color by the texture (no lighting is applied).
  SoTexture2[] textures = new SoTexture2[6];
  for (i=0; i<6; i++) {
    textures[i] = new SoTexture2();
    textures[i].model.setValue(SoTexture2.Model.DECAL);
  }
  textures[0].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/sky00.gif");
  textures[1].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/sky06.gif");
  textures[2].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/sky12.gif");
  textures[3].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/sky18.gif");
  textures[4].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/skyN0.gif");
  textures[5].filename.setValue("G:/eclipseWorkspaces/2-7-SkyBox1/skyS0.gif");

  for (i=0; i<6; i++) {
    root.addChild(textures[i]);

    // SoIndexedTriangleStripSet node is rendering triangles and triangle strips.
    // Each value in coordIndex is used as the index to SoCoordinate3
    // for particular vertex.
    SoIndexedTriangleStripSet strip = new SoIndexedTriangleStripSet();
    strip.coordIndex.setValues(0, 5, skyBoxIndices2[i]);
    strip.textureCoordIndex.setValues(0, 5, skyBoxTexCoordIndex2);
    root.addChild(strip);
  }

  return root;
}

	
SoSeparator createPlanet2(float radius, float distance,
                          float initialAngle, SbColor color, String textureName)
{
  SoSeparator root = new SoSeparator();

  // material of planet
  if (textureName != null) {
    SoMaterial material = new SoMaterial();
    material.ambientColor.setValue(new SbColor(1.f, 1.f, 1.f));
    material.diffuseColor.setValue(new SbColor(1.f, 1.f, 1.f));
    root.addChild(material);
  } else {
    SoMaterial material = new SoMaterial();
    material.ambientColor.setValue(color);
    material.diffuseColor.setValue(color);
    root.addChild(material);
  }

  // texture of planet
  if (textureName != null) {
    SoTexture2 texture = new SoTexture2();
    texture.filename.setValue(textureName);
    root.addChild(texture);
  }

  // revolution around the Sun
  SoRotationXYZ rotY = new SoRotationXYZ();
  rotY.axis.setValue( SoRotationXYZ.Axis.Y);
  root.addChild(rotY);

  // connect engine to rotation node
  SoElapsedTime counter = new SoElapsedTime();
  counter.speed.setValue(20.f/distance);
  rotY.angle.connectFrom(counter.timeOut);

  // translation from the Sun
  SoTranslation trans = new SoTranslation();
  trans.translation.setValue(distance*(float)(Math.cos(initialAngle)), 0.f,
                              distance*(float)(-Math.sin(initialAngle)));
  root.addChild(trans);

  // planet geometry
  SoSphere sphere = new SoSphere();
  sphere.radius.setValue(radius);
  root.addChild(sphere);

  return root;
}

	
	
	SoNode createDemoSceneSkyBox2() {
	  // create scene root
  SoSeparator root = new SoSeparator();
  root.ref();

  // Camera
  // Although camera can be created automatically by the viewer,
  // we need our own camera because of moving skybox.
  SoPerspectiveCamera camera = new SoPerspectiveCamera();
  root.addChild(camera);

  // create skybox
  root.addChild(createSkyBox2(camera));

  // environment
  // ambientColor is left on white (default value)
  // while we set its intensity on 0.25.
  SoEnvironment envir = new SoEnvironment();
  envir.ambientIntensity.setValue(0.25f);
  root.addChild(envir);


  // model of the Sun

  // ambient and diffuse color is set to dark yellow,
  // emissive on light yellow and specular is left on its default value (black)
  SoMaterial sunMat = new SoMaterial();
  sunMat.ambientColor.setValue(0.5f, 0.5f, 0.15f);
  sunMat.diffuseColor.setValue(0.5f, 0.5f, 0.15f);
  sunMat.emissiveColor.setValue(0.9f, 0.9f, 0.3f);
  root.addChild(sunMat);

  // sphere of radius 10
  SoSphere sun = new SoSphere();
  sun.radius.setValue(10.f);
  root.addChild(sun);

  // light in the middle of the Sun
  // all the objects in the scene graph following the light
  // will be lit by the light
  SoPointLight sunLight = new SoPointLight();
  sunLight.location.setValue(0.f, 0.f, 0.f);
  sunLight.intensity.setValue(0.75f);
  root.addChild(sunLight);


  // model of the Earth
  root.addChild(createPlanet2(4.0f, 45.f, 0.f,
                              new SbColor(0.7f, 0.7f, 1.0f), "G:/eclipseWorkspaces/2-7-SkyBox1/earth.jpg"));

  // model of the Mercury
  root.addChild(createPlanet2(2.0f, 20.f, (float)(4*Math.PI/3),
                              new SbColor(1.0f, 0.3f, 0.3f), null));

  // model of the Venus
  root.addChild(createPlanet2(3.0f, 30.f, (float)(3*Math.PI/3),
                              new SbColor(1.0f, 0.6f, 0.0f), "G:/eclipseWorkspaces/2-7-SkyBox1/venus.jpg"));

	return root;	
	}
	
	static SoNode createDemoScenePerformance() {

	//String fileName = "C:/eclipseWorkspaces/2-6-Performance/AztecCityI.iv"; // default model
		//String fileName = "G:/eclipseWorkspaces/5-1-Tanky1/models/tank.wrl";
		//String fileName = "C:/eclipseWorkspaces/examples_iv/transparentbluecylinder.iv"; // default model
		//String fileName = "G:/eclipseWorkspaces/examples_iv/sphere.iv"; // default model
		//String fileName = "C:/eclipseWorkspaces/examples_iv/text3.iv"; // default model
		//String fileName = "C:/eclipseWorkspaces/examples_iv/texSphereTransf.iv"; // default model
		//String fileName = "C:/eclipseWorkspaces/examples_iv/text3Rusty.iv"; // default model
		//String fileName = "C:/eclipseWorkspaces/examples_iv/bricks.iv"; // default model
		//String fileName = "C:/eclipseWorkspaces/examples_iv/simple2d.iv"; // default model
		//String fileName = "C:/eclipseWorkspaces/examples_iv/simple3D.iv"; // default model
		//String fileName = "C:/eclipseWorkspaces/examples_iv/pc.iv"; // default model
		//String fileName = "C:/eclipseWorkspaces/examples_iv/01.1.Windmill.iv"; // default model
		//String fileName = "C:/eclipseWorkspaces/inventor-2.1.5-10.src/inventor/data/models/chair.iv"; // default model
	//String fileName = "C:/eclipseWorkspaces/inventor-2.1.5-10.src/inventor/data/models/buildings/Barcelona.wrl";
		//String fileName = "C:/eclipseWorkspaces/inventor-2.1.5-10.src/inventor/data/models/buildings/windmill.iv";
		//String fileName = "C:/eclipseWorkspaces/inventor-2.1.5-10.src/inventor/data/models/chess/chessboard.iv";
		String fileName = "jExample/examples_iv/chair.iv";
	//String fileName = "C:/eclipseWorkspaces/inventor-2.1.5-10.src/inventor/data/models/vehicles/spacestation.iv";
	//String fileName = "C:/eclipseWorkspaces/inventor-2.1.5-10.src/inventor/data/models/sgi/logo.iv";
	//String fileName = "C:/eclipseWorkspaces/inventor-2.1.5-10.src/inventor/data/models/CyberHeads/josie.iv";
	//String fileName = "C:/eclipseWorkspaces/inventor-2.1.5-10.src/inventor/data/models/food/apple.iv";
	//String fileName = "C:/eclipseWorkspaces/inventor-2.1.5-10.src/inventor/data/models/scenes/chesschairs.iv";
		//String fileName = "F:/test_oiv/Renderismissingtriangles.iv"; // default model
		//String fileName = "F:/test_oiv/test.iv"; // default model
		//String fileName = "F:/test_oiv/Issue177Renderismissingtrianglesround.iv"; // default model
		//String fileName = "F:/basic_examples/Road/road.iv";
		//String fileName = "F:/basic_examples/Carport/carport.iv";
		// failed String fileName = "examples_iv/ejemplos-ve3d/modelos/escenaPepitoOficina.iv";
		//String fileName = "examples_iv/ejemplos-ve3d/modelos/metralleta.wrl";
		// failed String fileName = "examples_iv/ejemplos-ve3d/mundos-virtuales/forest.iv";
		//String fileName = "examples_iv/shadow-test2.iv";
		//String fileName = "examples_iv/shadow.iv";
		//String fileName = "F:/Partage/shadow.iv"; // FIXME : can cause an error in OpenGL, if current OGL version is 2.1
		//String fileName = "examples_iv/anya1.wrl";
		//String fileName = "examples_iv/export_vrml.wrl";
		//String fileName = "C:/Coin3D/shadowSimple.iv";
		//String fileName = "examples_iv/export_vrml.wrl";
	boolean baseColor = false;

	// create scene root
	SoSeparator root = new SoSeparator();
	//root.ref();

	// base color
	if (baseColor) {
		SoLightModel lm = new SoLightModel();
		lm.model.setValue(SoLightModel.Model.BASE_COLOR);
		lm.setOverride(true);
		root.addChild(lm);
	}

	// load scene from file
	SoFile file = new SoFile();
	file.name.setValue(fileName);
	root.addChild(file);

	return root;
}
	
	SoNode createDemoSceneSoMaterialShapeBinding() {
		
		float[][] rgb_ambient = {
				{ 0.00714286f,0.00169011f,0 },
				{ 0.00746438f,0.00673081f,0.00690282f },
				{ 0.00746438f,0.00673081f,0.00690282f }
		};
		
		float[][] rgb_diffuse = {
				{ 0.314286f,0.0743647f,0 },
				{ 0.0291577f,0.0262922f,0.0269642f },
				{ 0.0291577f,0.0262922f,0.0269642f }
		};
		
		float[][] rgb_specular = {
				{ 1,0.766841f,0 },
				{ 0.641609f,0.976208f,0.979592f },
				{ 0.938776f,0.0550317f,0.0550317f }
		};
		
		float[] shininess = {0.048f,0.062f,0.062f};
		
		SoTranslation trans = new SoTranslation();
		SoSeparator root = new SoSeparator();
		SoMaterial coul = new SoMaterial();
		SoMaterial coul_sphere = new SoMaterial();
		SoMaterialBinding attach =new SoMaterialBinding();
		root.ref();
		
		coul.ambientColor.setValues(0, rgb_ambient);
		coul.diffuseColor.setValues(0, rgb_diffuse);
		coul.specularColor.setValues(0, rgb_specular);
		coul.shininess.setValues(0,shininess);
		
		coul_sphere.ambientColor.setValue( rgb_ambient[1]);
		coul_sphere.diffuseColor.setValue( rgb_diffuse[1]);
		coul_sphere.specularColor.setValue( rgb_specular[1]);
		coul_sphere.shininess.setValue(shininess[1]);
		
		attach.value.setValue(SoMaterialBinding.Binding.PER_PART_INDEXED);
		
		trans.translation.setValue(0, 1, 5);
		
		root.addChild(attach);
		root.addChild(coul);
		root.addChild(new SoCylinder());
		root.addChild(trans);
		root.addChild(coul_sphere);
		root.addChild(new SoSphere());
		
		return root;
	}

	SoNode createDemoSceneSoMaterial() {
		SoSeparator root = new SoSeparator();				
		SoMaterial coul = new SoMaterial();
		root.ref();
		
		coul.ambientColor.setValue(0.01f,0.02f,0.02f);
		coul.diffuseColor.setValue(0.0291577f,0.03f,0.03f);
		coul.specularColor.setValue(0.641609f,0.976208f,0.979592f);
		coul.shininess.setValue(0.061f);
		
		root.addChild(coul);
		SoSphere sphere = new SoSphere();
		sphere.subdivision.setValue(40);
		root.addChild(sphere);
		
		return root;
	}
	
	SoNode createDemoSceneTimeWatch() {
		SoSeparator root = new SoSeparator();
		root.ref();

		SoPerspectiveCamera myCamera = new SoPerspectiveCamera();
		root.addChild(myCamera);
		root.addChild(new SoDirectionalLight());
		SoMaterial myMaterial = new SoMaterial();
		myMaterial.diffuseColor.setValue(1.0f, 0.0f, 0.0f);
		root.addChild(myMaterial);

		SoText3 myText = new SoText3();
		root.addChild(myText);
		myText.string.connectFrom(SoDB.getGlobalField("realTime"));

		return root;
	}

	SoNode createDemoSceneTransparentCubes() {
		SoSeparator root = new SoSeparator();
		root.ref();

		SoSeparator root2 = new SoShadowGroup();
		root.addChild(root2);
		
		SoGroup root3 = new SoGroup();
		root2.addChild(root3);
		
		SoMaterial mat = new SoMaterial();
		mat.diffuseColor.setValue(.5f, .6f, 0.7f);
		mat.transparency.setValue(0.5f);
		
		root3.addChild(mat);
		
		SoCube bigCube;
		
//		bigCube = new SoCube();
//		bigCube.depth.setValue(5.0f);
//		bigCube.height.setValue(5.0f);
//		bigCube.width.setValue(5.0f);
//				
//		root3.addChild(bigCube);
		
		SoIndexedFaceSet faceSet = new SoIndexedFaceSet();
		
		SoVertexProperty vp = new SoVertexProperty();
		
		int nbRepeat = 10000;
		
		float[] xyz = new float[9*nbRepeat];
		for(int i=0;i<nbRepeat;i++) {
		xyz[3+9*i] = 1.0f;
		xyz[4+9*i] = 1.0f;
		xyz[6+9*i] = -1.0f;
		xyz[7+9*i] = 1.0f;
		}
		vp.vertex.setValues(0,xyz);
		
		faceSet.vertexProperty.setValue(vp);
		
		int[] indices = new int[4*nbRepeat];
		for(int i=0;i<nbRepeat;i++) {
		indices[0+4*i] = 0+3*i;
		indices[1+4*i] = 1+3*i;
		indices[2+4*i] = 2+3*i;
		indices[3+4*i] = -1;
		}		
		faceSet.coordIndex.setValues(0, indices);
		
		SoSeparator root4 = new SoSeparator();
		
		SoTranslation transl = new SoTranslation();
		
		transl.translation.setValue(1, 2, 3);
		root4.addChild(transl);
		root4.addChild(faceSet);
		root3.addChild(root4);
		
		bigCube = new SoCube();
		bigCube.depth.setValue(10.0f);
		bigCube.height.setValue(10.0f);
		bigCube.width.setValue(10.0f);
				
		root4 = new SoSeparator();
		root4.addChild(bigCube);
		root3.addChild(root4);
		
		return root;
	}
	
	
	public static void main(String[] args) {

		JFrame frame = new JFrame("VRMLViewer");
		frame.getContentPane().setBackground(new Color(0,true));
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		SwingUtilities.invokeLater(() -> {

			boolean compo = false;

			if(compo) {
				createComposite(frame.getContentPane());
			}
			else {
				SoQt.init("demo");
				//SoDB.setDelaySensorTimeout(new SbTime(1.0/120.0));
				//SoDB.setRealTimeInterval(new SbTime(1.0/120.0));

				int style = 0;

				SoQtExaminerViewer viewer = new SoQtExaminerViewer(SoQtFullViewer.BuildFlag.BUILD_ALL, SoQtCameraController.Type.BROWSER, frame.getContentPane(), style) {
					public void initializeGL(GL2 gl2) {
						super.initializeGL(gl2);

						int error = glGetError();
						glEnable(GL_DEBUG_OUTPUT);
						error = glGetError();
						glDebugMessageCallback(new GLDebugMessageCallback() {
							@Override
							public void invoke(int i, int i1, int i2, int i3, int length, long message, long l1) {
								System.err.println("OpenGL Error : "+ getMessage(length,message));
							}
						}, 0);
						error = glGetError();
						glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
						error = glGetError();

						final int[] vao = new int[1];
						gl2.glGenVertexArrays(1,vao);
						gl2.glBindVertexArray(vao[0]);

						System.out.println("init");
					}
				};
				//viewer.setColorBitDepth (10);
				//viewer.setAntialiasing(true,4);

				viewer.setHeadlight(true);

				viewer.buildWidget(style);

				SoSeparator mainSep = new SoSeparator();

				SoShaderProgram program = new SoShaderProgram();

				SoVertexShader vs = new SoVertexShader();

				vs.sourceType.setValue(SoShaderObject.SourceType.GLSL_PROGRAM);
				vs.sourceProgram.setValue(
					"#version 330 core\n"+
					"layout (location = 0) in vec3 aPos;\n"+
							"uniform mat4 s4j_ModelViewMatrix;\n"+
							"uniform mat4 s4j_ProjectionMatrix;\n"+
					"void main()\n"+
					"{\n"+
					"gl_Position = s4j_ProjectionMatrix * s4j_ModelViewMatrix * vec4(aPos.x, aPos.y, aPos.z, 1.0);\n"+
					"}\n");

				final SoShaderStateMatrixParameter mvs = new SoShaderStateMatrixParameter();
				mvs.name.setValue("s4j_ModelViewMatrix");
				mvs.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.MODELVIEW);

				final SoShaderStateMatrixParameter ps = new SoShaderStateMatrixParameter();
				ps.name.setValue("s4j_ProjectionMatrix");
				ps.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.PROJECTION);

				vs.parameter.set1Value(0, mvs);
				vs.parameter.set1Value(1, ps);

				program.shaderObject.set1Value(0, vs);

				SoFragmentShader fs = new SoFragmentShader();

				fs.sourceType.setValue(SoShaderObject.SourceType.GLSL_PROGRAM);
				fs.sourceProgram.setValue(
						"#version 330 core\n"+
								"out vec4 FragColor;\n"+
						"void main()\n"+
								"{\n"+
								"FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n"+
								"}\n"
				);

				program.shaderObject.set1Value(1, fs);

				mainSep.addChild(program);

				SoTexture2 texture2d = new SoTexture2();

				texture2d.filename.setValue("C:\\Users\\boyadjian\\Pictures\\MountRainierIslandScreenShot.jpg");

				mainSep.addChild(texture2d);

				mainSep.addChild(createDemoScene());

				SoTranslation translation = new SoTranslation();
				translation.translation.setValue(0.3f,0.3f,0.3f);

				mainSep.addChild(translation);

				mainSep.addChild(createDemoScene());

				viewer.setSceneGraph(mainSep/*Orbits.main()*//*Shadows.main()*//*ShadowTest.create()*//*Fog.getScene()*/);
			}
		frame.pack();
		frame.setSize(800,600);
		frame.setVisible(true);
		});


}
}