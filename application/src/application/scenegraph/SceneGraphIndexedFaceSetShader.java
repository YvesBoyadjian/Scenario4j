/**
 * 
 */
package application.scenegraph;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.*;

import application.MainGLFW;
import application.nodes.SoTarget;
import application.nodes.SoTargets;
import application.objects.Target;
import application.scenario.Scenario;
import application.viewer.glfw.SoQtWalkViewer;
import com.jogamp.opengl.GL2;

import application.nodes.SoNoSpecularDirectionalLight;
import application.objects.DouglasFir;
import jscenegraph.coin3d.fxviz.nodes.SoShadowDirectionalLight;
import jscenegraph.coin3d.fxviz.nodes.SoShadowGroup;
import jscenegraph.coin3d.fxviz.nodes.SoVolumetricShadowGroup;
import jscenegraph.coin3d.inventor.SbBSPTree;
import jscenegraph.coin3d.inventor.VRMLnodes.SoVRMLBillboard;
import jscenegraph.coin3d.inventor.lists.SbListInt;
import jscenegraph.coin3d.inventor.nodes.*;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderProgram;
import jscenegraph.database.inventor.*;
import jscenegraph.database.inventor.actions.SoAction;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.actions.SoGetMatrixAction;
import jscenegraph.database.inventor.actions.SoSearchAction;
import jscenegraph.database.inventor.misc.SoNotList;
import jscenegraph.database.inventor.nodes.*;
import jscenegraph.port.Ctx;
import jscenegraph.port.memorybuffer.MemoryBuffer;
import jsceneviewerglfw.inventor.qt.viewers.SoQtConstrainedViewer;
import org.lwjgl.system.CallbackI;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DMatrix3C;
import org.ode4j.ode.*;

import static application.MainGLFW.MAX_PROGRESS;
import static application.MainGLFW.SCENE_POSITION;

/**
 * @author Yves Boyadjian
 *
 */
public class SceneGraphIndexedFaceSetShader implements SceneGraph {

	// Put it to false, if you do not want to save mri files
	private static final boolean SAVE_CHUNK_MRI = false;

	public static final boolean AIM = false;
	
	private static final float ZMAX = 4392.473f;
	
	private static final float ZMIN = 523.63275f;
	
	private static final int I_START = 2500;
	
	//private static final int MAX_I = 14000;//9000;
	
	private static final int MAX_J = 9000;
	
	static final float ALPINE_HEIGHT = 2000f;
	
	private static final float SUN_REAL_DISTANCE = 150e9f;
	
	private static final float SUN_RADIUS = 7e8f;
	
	public static final float SUN_FAKE_DISTANCE = 1e7f;
	
	public static final float WATER_HORIZON = 1e5f;
	
	public static final float WATER_BRIGHTNESS = 0.4f;
	
	private static final SbColor SUN_COLOR = new SbColor(1f, 0.95f, 0.8f);

	private static final float SUN_INTENSITY = 1.0f;
	
	public static final SbColor SKY_COLOR = new SbColor(0.3f, 0.3f, 0.5f);
	
	private static final float SKY_INTENSITY = 0.2f; 
	
	private static final Color STONE = new Color(139,141,122); //http://www.colourlovers.com/color/8B8D7A/stone_gray

	private static final Color TRAIL = new Color(200,150,50); // Color of trail
	
	private static final float GRASS_LUMINOSITY = 0.6f;
	
	public static boolean FLY = false;
	
	static final float CUBE_DEPTH = 2000;
	
	static final int LEVEL_OF_DETAIL = 600;
	
	static final int LEVEL_OF_DETAIL_SHADOW = 3000;
	
	static final int DOUGLAS_DISTANCE = 7000;
	
	static final int DOUGLAS_DISTANCE_SHADOW = 3000;
	
	static final boolean WITH_DOUGLAS = true;

	static public final float ORACLE_X = 314.9f;//317.56f;

	static public final float ORACLE_Y = 169.77f;//137.62f;

	static public final float ORACLE_Z = 1250.24f;//1248.5f;

	static public final float STARTING_X = 250.5f;

	static public final float STARTING_Y = 303.5f;

	static public final float STARTING_Z = 1256f;

	static public final float LOADING_HEIGHMAP_TIME_PERCENTAGE = 40;

	static public final float LOADING_NORMALS_TIME_PERCENTAGE = 20;

	private final float[] douglas_distance_trunk = new float[1];

	private final float[] douglas_distance_foliage = new float[1];

	private final float[] douglas_distance_shadow_trunk = new float[1];

	private final float[] douglas_distance_shadow_foliage = new float[1];

	private SoSeparator sep = new SoSeparator() {
		public void ref() {
			super.ref();
		}
		
		public void unref() {
			super.unref();
		}
	};
	
	private SoTranslation transl = new SoTranslation();
	
	private float zTranslation;

	private ChunkArray chunks;
	
	private float centerX;
	
	private float centerY;
	
	private SoShadowDirectionalLight[] sun = new SoShadowDirectionalLight[4];
	//private SoDirectionalLight sun;
	
	private SoDirectionalLight[] sky;

	private SoSeparator sunSep = new SoSeparator();
	
	private SoTranslation sunTransl = new SoTranslation();
	
	private SoSphere sunView;
	
	private SoSphere skyView;
	
	private SbRotation r1 = new SbRotation();
	private SbRotation r2 = new SbRotation();
	private SbRotation r3 = new SbRotation();
	private SbRotation r4 = new SbRotation();
	
	float delta_y;
	float delta_x;
	int h;
	int w;
	int full_island_w;
	
	float total_height_meter;
	float total_width_meter;
	
	int jstart;

	SoEnvironment environment = new SoEnvironment();

	SoVolumetricShadowGroup shadowGroup;
	SoNode shadowTree;
	SoNode chunkTree;
	SoCamera camera;

	SoTexture2 gigaTexture;
	
	float current_x;
	
	float current_y;
	
	float current_z;
	
	SoGroup douglasTreesF;
	SoGroup douglasTreesT;
	
	final SbVec3f douglasTreesRefPoint = new SbVec3f();
	
	SoGroup douglasTreesST;
	SoGroup douglasTreesSF;
	
	final SbVec3f douglasTreesSRefPoint = new SbVec3f();
	
	SoTouchLODMaster master;
	SoTouchLODMaster masterS;
	
	//final SbBSPTree sealsBSPTree = new SbBSPTree();
	
	final SbVec3f targetsRefPoint = new SbVec3f();

	final SbVec3f cameraDirection = new SbVec3f();

	final SbBSPTree treesBSPTree = new SbBSPTree();

	Raster rw;
	Raster re;
	int overlap;

	final float[] fArray = new float[1];

	float delta = 0;

	final SoSwitch fpsSwitch = new SoSwitch();

	final SoText2 fpsDisplay = new SoText2();

	final SoText2 targetDisplay = new SoText2();

	final SoText2 messageDisplay = new SoText2();

	final SoText2 offscreenTargetDisplay = new SoText2();

	final SoSwitch oracleSwitch = new SoSwitch();

	final SoSwitch oracleSwitchShadow = new SoSwitch();

	final SoSwitch oracleSpeechSwitch = new SoSwitch();

	final SoSwitch oracleSpeechSwitchShadow = new SoSwitch();

	final SoSwitch aimSwitch = new SoSwitch();

	final SoText3 oracleSpeech = new SoText3();

	final SoRotation oracleSpeechRotation = new SoRotation();

	SoTranslation speechTranslation = new SoTranslation();

	private int max_i;

	private DSpace space;

	final SoGroup targetsGroup = new SoGroup();

	final List<Target> targets = new ArrayList<>();

	private final Set<String> shotTargets = new HashSet<>();
	private final List<Integer> shotTargetsIndices = new ArrayList<>();
	private final List<Integer> shotTargetsInstances = new ArrayList<>();

	final SoSeparator planksSeparator = new SoSeparator();

	final List<SbVec3f> planksTranslations = new ArrayList<>();

	final List<SbRotation> planksRotations = new ArrayList<>();

	final List<DBox> planks = new ArrayList<>();

	final Set<Long> trails = new HashSet<>();

	final SbBSPTree trailsBSPTree = new SbBSPTree();

	List<Long> sorted_trails;

	boolean trailsDirty = false;
	boolean CBRunning = false;

	final Collection<Runnable> idleCallbacks = new ArrayList<>();

	DBody heroBody;

	DBody heroFeetBody;

	Scenario scenario;

	boolean searchForSea;

	final SbBSPTree beachBSPTree = new SbBSPTree();

	boolean wasDisplayingTrailDistance;

	public SceneGraphIndexedFaceSetShader(
			Raster rw,
			Raster re,
			int overlap,
			float zTranslation,
			int max_i,
			long[] trails,
			final JProgressBar progressBar) {
		super();
		this.rw = rw;
		this.re = re;
		this.overlap = overlap;
		this.zTranslation = zTranslation;
		this.max_i = max_i;
		
		int hImageW = rw.getHeight();
		int wImageW = rw.getWidth();
		
		int hImageE = re.getHeight();
		int wImageE = re.getWidth();
		
		h = Math.min(hImageW, MAX_J);// 8112
		full_island_w = wImageW+wImageE-I_START-overlap;// 13711
		w = Math.min(full_island_w, /*MAX_I*/max_i);
		
		chunks = new ChunkArray(w,h,full_island_w);
		
		float West_Bounding_Coordinate = -122.00018518518522f;
	      float East_Bounding_Coordinate= -121.74981481481484f;
	      float North_Bounding_Coordinate= 47.000185185185195f;
	      float South_Bounding_Coordinate= 46.749814814814826f;
	      
	      float delta_y_deg = (North_Bounding_Coordinate - South_Bounding_Coordinate)/wImageW;
	      float delta_x_deg = (East_Bounding_Coordinate - West_Bounding_Coordinate)/hImageW;
	      
	      float earth_radius = 6371e3f;
	      float earth_circumference = earth_radius * 2 * (float)Math.PI;
	      
	      delta_y = earth_circumference * delta_y_deg / 360.0f; // 3.432
	      delta_x = earth_circumference * delta_x_deg / 360.0f * (float)Math.cos(South_Bounding_Coordinate*Math.PI/180);// 2.35
	      
	      float width = delta_x * w;// 32241
	      float height = delta_y * h;// 27840
	      
	      centerX = width/2;// 16120
	      centerY = height/2;// 13920
		
		//int nbVertices = w*h; // 111 million vertices
		
		chunks.initArrays();
		
		//float[] vertices = new float[nbVertices*3];
		//float[] normals = new float[nbVertices*3];
		//int[] colors = new int[nbVertices];

		//int nbCoordIndices = (w-1)*(h-1)*5;
		//int[] coordIndices = new int[nbCoordIndices];
		
		Color snow = new Color(1.0f,1.0f,1.0f);

		
		Color colorStone = STONE;
		int redStone = colorStone.getRed()*colorStone.getRed()/255;
		int greenStone = colorStone.getGreen()*colorStone.getGreen()/255;
		int blueStone = colorStone.getBlue()*colorStone.getBlue()/255;
		
		final int rgbaStone = (colorStone.getAlpha() << 0) | (redStone << 24)| (greenStone << 16)|(blueStone<<8); 
		
		
		int nb = 0;
		for(int j=hImageW/4;j<hImageW*3/4;j++) {
			float zw = rw.getPixel(wImageW-1, j, fArray)[0];
			float ze = re.getPixel(overlap-1, j, fArray)[0];
		
			delta += (ze - zw);
			nb++;
		}
		delta /= nb;
		
		//SbBox3f sceneBox = new SbBox3f();
		//SbVec3f sceneCenter = new SbVec3f();
		SbVec3f ptV = new SbVec3f();
		
		float zmin = Float.MAX_VALUE;
		float zmax = -Float.MAX_VALUE;
		//Random random = new Random();
		
		chunks.initXY(delta_x,delta_y);
		
		jstart = (int)(Math.ceil((float)(h-1)/(Chunk.CHUNK_WIDTH-1)))*(Chunk.CHUNK_WIDTH-1) - h + 1;
		
		boolean need_to_save_colors = false;
		boolean load_z_and_color_was_successfull = chunks.loadZAndColors(); 
		if( ! load_z_and_color_was_successfull )
		{
		
			int red_,green_,blue_;
			
			float[] xyz = new float[3];

			float sharp = 0.06f;
			float sharp2 = 0.05f;

			float zWater =  - 150 + getzTranslation() - CUBE_DEPTH /2;

			for(int i=0;i<w;i++) {
				for(int j=0; j<h;j++) {
					int index = i*h+j;
					//chunks.verticesPut(index*3+0, i * delta_x);
					//chunks.verticesPut(index*3+1, (h - j -1) * delta_y);
					int i0 = Math.max(i-1,0);
					int i1 = Math.min(i+1,w-1);
					int j0 = Math.max(j-1,0);
					int j1 = Math.min(j+1,h-1);

					float zi0 = ((i0+I_START) >= wImageW ? re.getPixel(i0+I_START-wImageW+overlap, j, fArray)[0] - delta : rw.getPixel(i0+I_START, j, fArray)[0]);
					float zi1 = ((i1+I_START) >= wImageW ? re.getPixel(i1+I_START-wImageW+overlap, j, fArray)[0] - delta : rw.getPixel(i1+I_START, j, fArray)[0]);
					float zj0 = ((i+I_START) >= wImageW ? re.getPixel(i+I_START-wImageW+overlap, j0, fArray)[0] - delta : rw.getPixel(i+I_START, j0, fArray)[0]);
					float zj1 = ((i+I_START) >= wImageW ? re.getPixel(i+I_START-wImageW+overlap, j1, fArray)[0] - delta : rw.getPixel(i+I_START, j1, fArray)[0]);

					float zi0j0 = ((i0+I_START) >= wImageW ? re.getPixel(i0+I_START-wImageW+overlap, j0, fArray)[0] - delta : rw.getPixel(i0+I_START, j0, fArray)[0]);
					float zi1j1 = ((i1+I_START) >= wImageW ? re.getPixel(i1+I_START-wImageW+overlap, j1, fArray)[0] - delta : rw.getPixel(i1+I_START, j1, fArray)[0]);
					float zi1j0 = ((i1+I_START) >= wImageW ? re.getPixel(i1+I_START-wImageW+overlap, j0, fArray)[0] - delta : rw.getPixel(i1+I_START, j0, fArray)[0]);
					float zi0j1 = ((i0+I_START) >= wImageW ? re.getPixel(i0+I_START-wImageW+overlap, j1, fArray)[0] - delta : rw.getPixel(i0+I_START, j1, fArray)[0]);

					float zc = ((i+I_START) >= wImageW ? re.getPixel(i+I_START-wImageW+overlap, j, fArray)[0] - delta : rw.getPixel(i+I_START, j, fArray)[0]);

					float z = zc*(1 + 4*sharp + 4*sharp2) - sharp *( zi0 + zi1 + zj0 + zj1 ) - sharp2 * ( zi0j0 + zi1j1 + zi1j0 + zi0j1 );

					if(
							Math.abs(zi0j0)> 1e30 ||
									Math.abs(zi1j1)> 1e30 ||
									Math.abs(zi1j0)> 1e30 ||
									Math.abs(zi0j1)> 1e30 ||
							Math.abs(zi0)> 1e30 ||
									Math.abs(zi1)> 1e30 ||
									Math.abs(zj0)> 1e30 ||
									Math.abs(zj1)> 1e30 ||
									Math.abs(zc)> 1e30 ||
									i == 0 || j == 0 || i == w-1 || j == h-1 ) {
						z= ZMIN;
					}
					else {
						zmin = Math.min(zmin, z);
						zmax = Math.max(zmax, z);
					}
					chunks.verticesPut(index, z);

					chunks.verticesGet(index,xyz);
					//xyz[2] -= zTranslation;
					ptV.setValue(xyz);
					//sceneBox.extendBy(ptV);

					// On the beach ?
					if( Math.abs(ptV.getZ() - zWater) < 1) {
						beachBSPTree.addPoint(ptV,null);
					}
					
					//Color color = snow;
					
					if(z < ALPINE_HEIGHT + 400 * (random.nextDouble()-0.3)) {
						//color = new Color((float)random.nextDouble()*GRASS_LUMINOSITY, 1.0f*GRASS_LUMINOSITY, (float)random.nextDouble()*0.75f*GRASS_LUMINOSITY);
						
						red_ = (int)(255.99f * (float)random.nextDouble()*GRASS_LUMINOSITY);
						green_ = (int)(255.99f *  1.0f*GRASS_LUMINOSITY);
						blue_ = (int)(255.99f * (float)random.nextDouble()*/*0.75f*/0.7f*GRASS_LUMINOSITY); // Adapted to 6500 K display
					}
					else {					
						red_ = snow.getRed();
						green_ = snow.getGreen();
						blue_ = snow.getBlue();					
					}
					
					int red = /*color.getRed()*color.getRed()*/red_*red_/255;
					int green = /*color.getGreen()*color.getGreen()*/green_*green_/255;
					int blue = /*color.getBlue()*color.getBlue()*/blue_*blue_/255;
					int alpha = 255;//color.getAlpha();
					
					chunks.colorsPut(index, red, green, blue, alpha);
				}
				progressBar.setValue((int)((long)MAX_PROGRESS*i*LOADING_HEIGHMAP_TIME_PERCENTAGE/100/w));
			}

			if(SAVE_CHUNK_MRI) {
				chunks.saveZ();
			}
			need_to_save_colors = true;
		}
		
		rw = null;
		re = null;

		//sceneCenter.setValue(sceneBox.getCenter());
		
		boolean load_normals_and_stone_was_successfull = chunks.loadNormalsAndStones(); 
		
		if( ! load_normals_and_stone_was_successfull )
		{
			float[] xyz = new float[3];
			
			SbVec3f p0 = new SbVec3f();
			SbVec3f p1 = new SbVec3f();
			SbVec3f p2 = new SbVec3f();
			SbVec3f p3 = new SbVec3f();
			SbVec3f v0 = new SbVec3f();
			SbVec3f v1 = new SbVec3f();
			SbVec3f n = new SbVec3f();
			for(int i=1;i<w-1;i++) {
				for(int j=1; j<h-1;j++) {
					int index = i*h+j;
					int index0 = (i-1)*h+j;
					int index1 = i*h+j+1;
					int index2 = (i+1)*h+j;
					int index3 = i*h+j-1;
					p0.setValue(chunks.verticesGet(index0,xyz));
					p1.setValue(chunks.verticesGet(index1,xyz));
					p2.setValue(chunks.verticesGet(index2,xyz));
					p3.setValue(chunks.verticesGet(index3,xyz));
					v0.setValue(p0.operator_minus_equal(p2));
					v1.setValue(p1.operator_minus_equal(p3));
					n.setValue(v0.operator_cross_equal(v1));
					n.normalize();
					chunks.normalsPut(index, n.getX(), n.getY(), n.getZ());
		
					if((n.getZ()<0.45 && chunks.verticesGet(index,xyz)[2] < ALPINE_HEIGHT) || n.getZ()<0.35) {
						chunks.colorsPut(index, redStone, greenStone, blueStone, 255);
					}
					if(n.getZ()<0.55) {
						chunks.stonePut(index);				
					}
				}
				progressBar.setValue((int)((long)MAX_PROGRESS*LOADING_HEIGHMAP_TIME_PERCENTAGE/100 + (long)MAX_PROGRESS*i*LOADING_NORMALS_TIME_PERCENTAGE/100/w));
			}
			if ( need_to_save_colors ) {
				if(SAVE_CHUNK_MRI) {
					chunks.saveColors();
				}
			}
			if(SAVE_CHUNK_MRI) {
				chunks.saveNormalsAndStones();
			}
		}
		
		chunks.initIndexedFaceSets();

		sep.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);

		sep.addChild(new SoPerspectiveCamera());

	    SoCallback callback = new SoCallback();
	    
	    callback.setCallback(action -> {
	    	if(action instanceof SoGLRenderAction) {
	    		SoGLRenderAction glRenderAction = (SoGLRenderAction)action;
	    		GL2 gl2 = Ctx.get(glRenderAction.getCacheContext());
	    		//gl2.glEnable(GL2.GL_FRAMEBUFFER_SRGB);
	    		gl2.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
	    	}
	    });
	    
	    sep.addChild(callback);
	    
	    //sep.addChild(new SoAbort());
	    
	    environment.ambientColor.setValue(0, 0, 0);
	    environment.ambientIntensity.setValue(0);
	    environment.fogType.setValue(SoEnvironment.FogType.FOG);
	    environment.fogColor.setValue(SKY_COLOR.darker());
	    environment.fogVisibility.setValue(5e4f);
	    
	    sep.addChild(environment);

	    SoComplexity complexity = new SoComplexity();
	    complexity.textureQuality.setValue(0.9f);
	    sep.addChild(complexity);
	    
	    SoDepthBuffer depthBuffer1 = new SoDepthBuffer();
	    depthBuffer1.clamp.setValue(true);
	    sep.addChild(depthBuffer1);	    
	    
	    sunView = new SoSphere();
	    sunView.radius.setValue(SUN_RADIUS/SUN_REAL_DISTANCE*SUN_FAKE_DISTANCE);
	    
	    SoSeparator skySep = new SoSeparator();
	    
	    SoMaterial skyColor = new SoMaterial();
	    skyColor.diffuseColor.setValue(SceneGraphIndexedFaceSetShader.SKY_COLOR.darker());
	    
	    skySep.addChild(skyColor);
	    
	    skyView = new SoSphere();
	    skyView.radius.setValue(WATER_HORIZON);
	    
	    SoDepthBuffer skyDepthBuffer = new SoDepthBuffer();
	    skyDepthBuffer.test.setValue(false);
	    //skyDepthBuffer.write.setValue(false);
	    skyDepthBuffer.clamp.setValue(true);
	    
	    skySep.addChild(skyDepthBuffer);
	    
	    skySep.addChild(skyView);
	    
	    SoMaterial sunMat = new SoMaterial();
	    sunMat.emissiveColor.setValue(SUN_COLOR);
	    sunMat.diffuseColor.setValue(1,1,1); // In order to see the sun white
	    sunMat.ambientColor.setValue(0, 0, 0);
	    
	    sunSep.addChild(sunMat);
	    
	    sunSep.addChild(transl);
	    
	    sunSep.addChild(sunTransl);
	    
	    SoDepthBuffer depthBuffer = new SoDepthBuffer();
	    depthBuffer.test.setValue(false);
	    depthBuffer.write.setValue(false);
	    //depthBuffer.clamp.setValue(true);
	    
	    sunSep.addChild(depthBuffer);
	    
	    SoShaderProgram programSun = new SoShaderProgram();
	    
	    SoVertexShader vertexShaderSun = new SoVertexShader();
	    
	    //vertexShader.sourceProgram.setValue("../../MountRainierIsland/application/src/shaders/phongShading.vert");

		String behindVertexPath = "../../MountRainierIsland/application/src/shaders/behind_vertex.glsl";

		File behindVertexFile = new File(behindVertexPath);

		if(!behindVertexFile.exists()) {
			behindVertexPath = "../MountRainierIsland/application/src/shaders/behind_vertex.glsl";
		}

		behindVertexFile = new File(behindVertexPath);

		if(!behindVertexFile.exists()) {
			behindVertexPath = "application/src/shaders/behind_vertex.glsl";
		}

	    vertexShaderSun.sourceProgram.setValue(behindVertexPath);
	    
	    programSun.shaderObject.set1Value(0, vertexShaderSun);
	    sunSep.addChild(programSun);
	    
	    sunSep.addChild(sunView);
	    
	    sky = new SoDirectionalLight[4];
	    sky[0] = new SoNoSpecularDirectionalLight();
	    sky[0].color.setValue(SKY_COLOR);
	    sky[0].intensity.setValue(SKY_INTENSITY);
	    sky[0].direction.setValue(0, 1, -1);
	    sky[1] = new SoNoSpecularDirectionalLight();
	    sky[1].color.setValue(SKY_COLOR);
	    sky[1].intensity.setValue(SKY_INTENSITY);
	    sky[1].direction.setValue(0, -1, -1);
	    sky[2] = new SoNoSpecularDirectionalLight();
	    sky[2].color.setValue(SKY_COLOR);
	    sky[2].intensity.setValue(SKY_INTENSITY);
	    sky[2].direction.setValue(1, 0, -1);
	    sky[3] = new SoNoSpecularDirectionalLight();
	    sky[3].color.setValue(SKY_COLOR);
	    sky[3].intensity.setValue(SKY_INTENSITY);
	    sky[3].direction.setValue(-1, 0, -1);
	    
	    sep.addChild(sky[0]);
	    sep.addChild(sky[1]);
	    sep.addChild(sky[2]);
	    sep.addChild(sky[3]);
	    
	    //shadowGroup = new SoSeparator();
//	    shadowGroup = new SoShadowGroup() {
		shadowGroup = new SoVolumetricShadowGroup() {
			public void ref() {
				super.ref();
			}
			
			public void unref() {
				super.unref();
			}
		};
		shadowGroup.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
		shadowGroup.isActive.setValue(true);
	    shadowGroup.quality.setValue(1.0f);
	    shadowGroup.precision.setValue(0.01f);
	    shadowGroup.intensity.setValue(1.0f);
//	    shadowGroup.visibilityNearRadius.setValue(5000);
//	    shadowGroup.visibilityRadius.setValue(10000);
//	    shadowGroup.visibilityFlag.setValue(SoShadowGroup.VisibilityFlag.ABSOLUTE_RADIUS);
	    shadowGroup.visibilityFlag.setValue(SoShadowGroup.VisibilityFlag.PROJECTED_BBOX_DEPTH_FACTOR);
	    shadowGroup.threshold.setValue(0.9f);
	    shadowGroup.epsilon.setValue(3.0e-6f);
	    shadowGroup.smoothBorder.setValue(1.0f);
	    
	for(int is=0;is<4;is++) {
	    sun[is] = new SoShadowDirectionalLight();
	    //sun = new SoDirectionalLight();
	    sun[is].color.setValue(new SbColor(SUN_COLOR.operator_mul(SUN_INTENSITY)));
	    
	    sun[is].maxShadowDistance.setValue(2e4f);
	    //sun[is].bboxCenter.setValue(10000, 0, 0);
	    sun[is].bboxSize.setValue(5000+is*3000, 5000+is*3000, 1000);
	    
	    sun[is].intensity.setValue(1.0F/4.0f);
	    
	    shadowGroup.addChild(sun[is]);
	    sun[is].enableNotify(false); // In order not to recompute shaders
	}

	skySep.addChild(transl);

	shadowGroup.addChild(skySep);
	    
	shadowGroup.addChild(sunSep); // Sun must be drawn after sky
    
	    SoSeparator landSep = new SoSeparator();
	    //landSep.renderCulling.setValue(SoSeparator.CacheEnabled.ON);
	    
	    SoShapeHints shapeHints = new SoShapeHints();
	    shapeHints.shapeType.setValue(SoShapeHints.ShapeType.SOLID);
	    shapeHints.vertexOrdering.setValue(SoShapeHints.VertexOrdering.CLOCKWISE);
	    landSep.addChild(shapeHints);
	    
	    SoPickStyle ps = new SoPickStyle();
	    ps.style.setValue(SoPickStyle.Style.UNPICKABLE);
	    
	    landSep.addChild(ps);
	    
	    SoShaderProgram program = new SoShaderProgram();
	    
	    SoVertexShader vertexShader = new SoVertexShader();
	    
	    //vertexShader.sourceProgram.setValue("../../MountRainierIsland/application/src/shaders/phongShading.vert");
	    vertexShader.sourceProgram.setValue("../../MountRainierIsland/application/src/shaders/perpixel_vertex.glsl");
	    
	    program.shaderObject.set1Value(0, vertexShader);
	    
	    SoFragmentShader fragmentShader = new SoFragmentShader();
	    
	    //fragmentShader.sourceProgram.setValue("../../MountRainierIsland/application/src/shaders/phongShading.frag");
	    fragmentShader.sourceProgram.setValue("../../MountRainierIsland/application/src/shaders/perpixel_fragment.glsl");
	    
	    program.shaderObject.set1Value(1, fragmentShader);
	    
	    //landSep.addChild(program);
	    
	    SoMaterial mat = new SoMaterial();
	    mat.ambientColor.setValue(0, 0, 0); // no ambient
	    mat.specularColor.setValue(0.3f,0.3f,0.3f);
	    mat.shininess.setValue(0.15f);
	    
	    landSep.addChild(mat);
	    
	    landSep.addChild(transl);
	    
	    OverallTexture ot = chunks.getOverallTexture();

	    gigaTexture = ot.getTexture();

	    landSep.addChild(gigaTexture);
	    
	    RecursiveChunk rc = chunks.getRecursiveChunk(progressBar);
	    
	    master = new SoTouchLODMaster("viewer");

		master.setLodFactor(LEVEL_OF_DETAIL);
	    
	    landSep.addChild(master);
	    
	    chunkTree = rc.getGroup(master,true);
	    landSep.addChild(chunkTree);
	    
		shadowGroup.addChild(landSep);

		// ___________________________________________________________ Douglas trees

		for(long i =0; i<trails.length;i++) {
			long code = trails[(int)i];

			int iv = (int) code;
			int jv = (int)(code >>> 32);
			addTrail(iv,jv);
		}

	    SoSeparator douglasSep = new SoSeparator();
	    douglasSep.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
	    
	    shapeHints = new SoShapeHints();
	    shapeHints.shapeType.setValue(SoShapeHints.ShapeType.SOLID);
	    shapeHints.vertexOrdering.setValue(SoShapeHints.VertexOrdering.COUNTERCLOCKWISE);
	    douglasSep.addChild(shapeHints);
	    
	    douglasSep.addChild(ps);
	    
	    SoTexture2 douglasTexture = new SoTexture2();
	    
	    //douglasTexture.filename.setValue("ressource/texture-2058269_Steve_Wittmann_thethreedguy.jpg");
	    //douglasTexture.filename.setValue("ressource/texture-2058270_Steve_Wittmann_thethreedguy.jpg");

		String douglasPath1 = "ressource/texture-2058270_Steve_Wittmann_thethreedguy.jpg";

		final int[] wi1 = new int[1];
		final int[] hi1 = new int[1];

		MemoryBuffer b1 = loadTexture(douglasPath1,wi1,hi1);

//		//int min1 = Math.min(wi1[0],hi1[0]);
//		int max1 = Math.max(wi1[0],hi1[0]);
//
//		String douglasPath2 = "ressource/texture-2058269_Steve_Wittmann_thethreedguy.jpg";
//
//		final int[] wi2 = new int[1];
//		final int[] hi2 = new int[1];
//
//		MemoryBuffer b2 = loadTexture(douglasPath2,wi2,hi2);
//
//		//int min2 = Math.min(wi2[0],hi2[0]);
//		int max2 = Math.max(wi2[0],hi2[0]);
//
//		//int ratio = 8;
//
//		int nb_tiles_on_side = 6;
//
//		//int min1min2 = Math.min(min1,min2)*ratio/10;
//		int max1max2 = Math.max(max1,max2);
//
////		int dw1 = wi1[0] - min1min2;
////		int dh1 = hi1[0] - min1min2;
////		int dw2 = wi2[0] - min1min2;
////		int dh2 = hi2[0] - min1min2;
//
//		//int min3 = min1min2 * (10*10/ratio);
//		int max3 = max1max2*nb_tiles_on_side;
//
//		int nb_images = nb_tiles_on_side*nb_tiles_on_side;//(10*10/ratio)*(10*10/ratio);
//
//		int nbPixels3 = max3*max3;//min3*min3;
//
//		MemoryBuffer b3 = MemoryBuffer.allocateBytes(nbPixels3*3);
//
//		Random r = new Random(37);
//
//		boolean[] im = new boolean[nb_images];
//		int[] dx = new int[nb_images];
//		int[] dy = new int[nb_images];
//		for( int i=0; i< nb_images; i++) {
//			im[i] = r.nextBoolean();
//			//dx[i] = im[i] ? (int)((dw1+1)*r.nextDouble()*.9) : (int)((dw2+1)*r.nextDouble());
//			//dy[i] = im[i] ? (int)((dh1+1)*r.nextDouble()) : (int)((dh2+1)*r.nextDouble());
//			dx[i] = im[i] ? (int)((max1max2 - wi1[0])*r.nextDouble()) : (int)((max1max2 - wi2[0])*r.nextDouble());
//			dy[i] = im[i] ? (int)((max1max2 - hi1[0])*r.nextDouble()) : (int)((max1max2 - hi2[0])*r.nextDouble());
//		}
//
//		for(int i3=0; i3<max3;i3++) {
//			for(int j3=0; j3<max3;j3++) {
//
//				int ii = i3/max1max2;
//				int jj = j3/max1max2;
//
//				int no_im = ii + nb_tiles_on_side*jj;
//
//				boolean imb = im[no_im];
//
//				int i1 = (i3%max1max2 + dx[no_im])%wi1[0];
//				int j1 = (j3%max1max2 + dy[no_im])%hi1[0];
//
//				int i2 = (i3%max1max2 + dx[no_im])%wi2[0];
//				int j2 = (j3%max1max2 + dy[no_im])%hi2[0];
//
//				int index1 = 3*(i1 + j1*wi1[0]);
//				int index2 = 3*(i2 + j2*wi2[0]);
//
//				int index3 = 3*(j3*max3+i3);
//				b3.setByte(index3,imb ? b1.getByte(index1) : b2.getByte(index2));
//				b3.setByte(index3+1, imb ? b1.getByte(index1+1) : b2.getByte(index2+1));
//				b3.setByte(index3+2, imb ? b1.getByte(index1+2) : b2.getByte(index2+2));
//			}
//		}

		SbVec2s s1 = new SbVec2s((short)wi1[0],(short)hi1[0]);
//		SbVec2s s3 = new SbVec2s((short)max3,(short)max3);

		int nc = 3;

		//douglasTexture.image.setValue(s3, nc, b3);
		if (null!=b1) {
			douglasTexture.image.setValue(s1, nc, false, b1);
		}
	    douglasSep.addChild(transl);

	    douglas_distance_trunk[0] = DOUGLAS_DISTANCE/2;
	    
		douglasTreesT = getDouglasTreesT(douglasTreesRefPoint,douglas_distance_trunk,progressBar);
		
	    SoSeparator douglasSepF = new SoSeparator();
	    douglasSepF.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
	    
		douglasSep.addChild(douglasTreesT);

		douglas_distance_foliage[0] = DOUGLAS_DISTANCE;
		
		douglasTreesF = getDouglasTreesF(douglasTreesRefPoint,douglas_distance_foliage,true,progressBar);
		
	    douglasSepF.addChild(douglasTexture);
	    
		douglasSepF.addChild(douglasTreesF);
		
		douglasSep.addChild(douglasSepF);
		
		if(WITH_DOUGLAS)
			shadowGroup.addChild(douglasSep);

		// _______________________________________________________________________ Targets

		Seals seals_ = new Seals(this);
		addTarget(seals_);

		BigFoots bigfoots_ = new BigFoots(this);
		addTarget(bigfoots_);

		MountainGoats goats_ = new MountainGoats(this);
		addTarget(goats_);

		HoaryMarmots marmots_ = new HoaryMarmots(this);
		addTarget(marmots_);

		GroundSquirrels squirrels_ = new GroundSquirrels(this);
		addTarget(squirrels_);

		Owls spottedOwlFront_ = new Owls(this,53) {

			@Override
			public String targetName() {
				return Owls.SPOTTED_OWL_NAME;
			}

			@Override
			public String getTexturePath() {
				return "ressource/SPOW-front_web.jpg";
			}

			@Override
			public float getRatio() {
				return 218.0f/292.0f;
			}
		};
		addTarget(spottedOwlFront_);

		Owls spottedOwlBack_ = new Owls(this,54) {

			@Override
			public String targetName() {
				return Owls.SPOTTED_OWL_NAME;
			}

			@Override
			public String getTexturePath() {
				return "ressource/SPOW-back_web.jpg";
			}

			@Override
			public float getRatio() {
				return 209.0f/329.0f;
			}
		};
		addTarget(spottedOwlBack_);

		Owls barredOwlFront_ = new Owls(this,55) {

			@Override
			public String targetName() {
				return Owls.BARRED_OWL_NAME;
			}

			@Override
			public String getTexturePath() {
				return "ressource/BDOW-front_web.jpg";
			}

			@Override
			public float getRatio() {
				return 217.0f/289.0f;
			}
		};
		addTarget(barredOwlFront_);

		Owls barredOwlBack_ = new Owls(this,56) {

			@Override
			public String targetName() {
				return Owls.BARRED_OWL_NAME;
			}

			@Override
			public String getTexturePath() {
				return "ressource/BDOW-back_web.jpg";
			}

			@Override
			public float getRatio() {
				return 222.0f/330.0f;
			}
		};
		addTarget(barredOwlBack_);

		for( Target target : targets) {

			SoTargets targetsSeparator = new SoTargets(target) {
				public void notify(SoNotList list) {
					super.notify(list);
				}
			};
			targetsSeparator.setReferencePoint(targetsRefPoint);
			targetsSeparator.setCameraDirection(cameraDirection);

			SoBaseColor targetsColor = new SoBaseColor();
			targetsColor.rgb.setValue(1,1,1);
			targetsSeparator.addChild(targetsColor);

		//sealsSeparator.renderCaching.setValue(SoSeparator.CacheEnabled.ON);
		
		//SoTranslation sealsTranslation = new SoTranslation();
		
		//sealsTranslation.translation.setValue(0, 0, - getzTranslation());

			targetsSeparator.addChild(transl);
	    
		//sealsSeparator.addChild(sealsTranslation);
		
			SoTexture2 targetTexture = new SoTexture2();

			String texturePath = target.getTexturePath();

			File textureFile = new File(texturePath);

			if(!textureFile.exists()) {
				texturePath = "application/"+texturePath;
			}

			targetTexture.filename.setValue(texturePath);

			targetsSeparator.addChild(targetTexture);
		
			final float[] vector = new float[3];
		
			final SbVec3f targetPosition = new SbVec3f();

			for (int instance = 0; instance < target.getNbTargets(); instance++) {
				SoTarget targetSeparator = new SoTarget(instance);
				//sealSeparator.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);

				SoTranslation targetTranslation = new SoTranslation();

				targetPosition.setValue(target.getTarget(instance, vector));
				targetPosition.setZ(targetPosition.getZ() + 0.3f);

				targetTranslation.translation.setValue(targetPosition);

				targetSeparator.addChild(targetTranslation);

				SoVRMLBillboard billboard = new SoVRMLBillboard();
				//billboard.axisOfRotation.setValue(0, 1, 0);

				SoCube targetCube = new SoCube();
				targetCube.height.setValue(target.getSize());
				targetCube.width.setValue(target.getRatio() * targetCube.height.getValue());
				targetCube.depth.setValue(0.1f);

				billboard.addChild(targetCube);

				targetSeparator.addChild(billboard);

				targetSeparator.setReferencePoint(targetsRefPoint);
				targetsSeparator.addChild(targetSeparator);
			}

			targetsGroup.addChild(targetsSeparator);

		}
		shadowGroup.addChild(targetsGroup);

		addWater(shadowGroup,185 + zTranslation, 0.0f, true,false);
		addWater(shadowGroup,175 + zTranslation, 0.2f, true,false);
		addWater(shadowGroup,165 + zTranslation, 0.4f, true,false);
		addWater(shadowGroup,160 + zTranslation, 0.4f, true,false);
		addWater(shadowGroup,155 + zTranslation, 0.6f, true,false);
		addWater(shadowGroup,150 + zTranslation, 0.7f, true,false);		

		SoNode oracleSeparator = buildOracle(false);

		oracleSwitch.addChild(oracleSeparator);
		shadowGroup.addChild(oracleSwitch);


		SoMaterial plankMat = new SoMaterial();
		plankMat.diffuseColor.setValue(0.7f,0.5f,0.1f);
		plankMat.ambientColor.setValue(0, 0, 0); // no ambient
		plankMat.specularColor.setValue(0.3f,0.3f,0.3f);
		plankMat.shininess.setValue(0.25f);

		planksSeparator.addChild(plankMat);

		//planksSeparator.addChild(ot.getTexture());

		shadowGroup.addChild(planksSeparator);

		sep.addChild(shadowGroup);

		// _______________________________________________ shadows
		
		SoSeparator castingShadowScene = new SoSeparator();
		castingShadowScene.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
		
		addWaterShadow(castingShadowScene, 150 + zTranslation,0.0f, false,false);
		//addWater(castingShadowScene,150 + zTranslation,0.0f, false,true);
		
	    SoSeparator shadowLandSep = new SoSeparator();
	    
	    shapeHints = new SoShapeHints();
	    shapeHints.shapeType.setValue(SoShapeHints.ShapeType.SOLID);
	    shapeHints.vertexOrdering.setValue(SoShapeHints.VertexOrdering.CLOCKWISE);
	    shadowLandSep.addChild(shapeHints);
	    
	    SoMaterial shadowMat = new SoMaterial();
	    shadowMat.ambientColor.setValue(0, 0, 0); // no ambient
	    
	    shadowLandSep.addChild(shadowMat);
	    
	    shadowLandSep.addChild(transl);
	    
	    //RecursiveChunk rcS = chunks.getRecursiveChunk();
	    
	    masterS = new SoTouchLODMaster("shadow");

		masterS.setLodFactor(LEVEL_OF_DETAIL_SHADOW);
	    
	    shadowLandSep.addChild(masterS);
	    
	    shadowTree = rc.getShadowGroup(masterS,false);
	    shadowLandSep.addChild(shadowTree);
	    
	    //shadowLandSep.addChild(chunks.getShadowGroup());
		
		castingShadowScene.addChild(shadowLandSep);
		
	    SoSeparator douglasSepS = new SoSeparator();
	    douglasSepS.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
	    
	    shapeHints = new SoShapeHints();
	    shapeHints.shapeType.setValue(SoShapeHints.ShapeType.SOLID);
	    shapeHints.vertexOrdering.setValue(SoShapeHints.VertexOrdering.COUNTERCLOCKWISE);
	    douglasSepS.addChild(shapeHints);
	    
	    douglasSepS.addChild(transl);

		douglas_distance_shadow_trunk[0] = DOUGLAS_DISTANCE_SHADOW/2;
	    
		douglasTreesST = getDouglasTreesT(douglasTreesSRefPoint,douglas_distance_shadow_trunk,progressBar);
		
		douglasSepS.addChild(douglasTreesST);

		douglas_distance_shadow_foliage[0] = DOUGLAS_DISTANCE_SHADOW;
		
		douglasTreesSF = getDouglasTreesF(douglasTreesSRefPoint,douglas_distance_shadow_foliage, false,progressBar);
		
		douglasSepS.addChild(douglasTreesSF);
		
		if(WITH_DOUGLAS)
			castingShadowScene.addChild(douglasSepS);

		oracleSwitchShadow.addChild(buildOracle(true));

		castingShadowScene.addChild(oracleSwitchShadow);

		castingShadowScene.addChild(planksSeparator);

		// slowdown too much the rendering
		//castingShadowScene.addChild(targetsGroup);
		
		sun[0].shadowMapScene.setValue(castingShadowScene);
		sun[1].shadowMapScene.setValue(castingShadowScene);
		sun[2].shadowMapScene.setValue(castingShadowScene);
		sun[3].shadowMapScene.setValue(castingShadowScene);
		
		//sep.ref();
		//forest = null; // for garbage collection : no, we need forest

		SoDepthBuffer db = new SoDepthBuffer();

		db.test.setValue(false);

		sep.addChild(db);

		// ________________________________________________________________ Display of FPS
		SoSeparator billboardSeparator = new SoSeparator();

		SoOrthographicCamera billboardCamera = new SoOrthographicCamera();

		billboardSeparator.addChild(billboardCamera);

		SoTranslation textTransl = new SoTranslation();
		textTransl.translation.setValue(1,0.9f,0);

		billboardSeparator.addChild(textTransl);

		SoFont font = new SoFont();
		font.size.setValue(40.0f);

		billboardSeparator.addChild(font);

		SoBaseColor color = new SoBaseColor();

		color.rgb.setValue(0,1,0);

		billboardSeparator.addChild(color);

		//fpsDisplay.string.setValue("60.0 FPS");
		fpsDisplay.enableNotify(false);

		billboardSeparator.addChild(fpsDisplay);

		fpsSwitch.addChild(billboardSeparator);
		fpsSwitch.whichChild.setValue(SoSwitch.SO_SWITCH_NONE);
		sep.addChild(fpsSwitch);

		// ____________________________________________________ Display of shot targets
		SoSeparator targetSeparator = new SoSeparator();

		SoOrthographicCamera billboardTargetCamera = new SoOrthographicCamera();

		targetSeparator.addChild(billboardTargetCamera);

		textTransl = new SoTranslation();
		textTransl.translation.setValue(-1,0.9f,0);

		targetSeparator.addChild(textTransl);

//		SoFont font = new SoFont();
//		font.size.setValue(40.0f);

		targetSeparator.addChild(font);

		color = new SoBaseColor();

		color.rgb.setValue(1,0,0);

		targetSeparator.addChild(color);

		targetDisplay.string.setValue("");

		targetSeparator.addChild(targetDisplay);

		sep.addChild(targetSeparator);

		// ____________________________________________________ Display of message
		SoSeparator messageSeparator = new SoSeparator();

		SoOrthographicCamera billboardMessageCamera = new SoOrthographicCamera();

		messageSeparator.addChild(billboardMessageCamera);

		textTransl = new SoTranslation();
		textTransl.translation.setValue(0, -0.86f, 0);

		messageSeparator.addChild(textTransl);

		messageSeparator.addChild(font);

		color = new SoBaseColor();

		color.rgb.setValue(1,1,0);

		messageSeparator.addChild(color);

		messageDisplay.justification.setValue(SoText2.Justification.CENTER);

		messageSeparator.addChild(messageDisplay);

		sep.addChild(messageSeparator);

		// _____________________________________________________ ViewFinder
		SoSeparator viewFinderSeparator = new SoSeparator();

		SoOrthographicCamera billboardFinderCamera = new SoOrthographicCamera();

		aimSwitch.addChild(billboardFinderCamera);

		color = new SoBaseColor();

		color.rgb.setValue(0.5f,0,0);

		aimSwitch.addChild(color);

		SoMarkerSet markerSet = new SoMarkerSet();

		markerSet.markerIndex.setValue(SoMarkerSet.MarkerType.CIRCLE_LINE_9_9.getValue());

		SoVertexProperty vertexProperty = new SoVertexProperty();
		vertexProperty.vertex.setValue(new SbVec3f());

		markerSet.vertexProperty.setValue(vertexProperty);

		aimSwitch.addChild(markerSet);

		viewFinderSeparator.addChild(aimSwitch);

		sep.addChild(viewFinderSeparator);

		// _____________________________________________________ OffscreenTargets

		SoSeparator offscreenTargetSeparator = new SoSeparator();

		SoOrthographicCamera offscreenTargetCamera = new SoOrthographicCamera();

		offscreenTargetSeparator.addChild(offscreenTargetCamera);

		textTransl = new SoTranslation();
		textTransl.translation.setValue(0,0.9f,0);

		offscreenTargetSeparator.addChild(textTransl);

//		SoFont font = new SoFont();
//		font.size.setValue(40.0f);

		offscreenTargetSeparator.addChild(font);

		color = new SoBaseColor();

		color.rgb.setValue(1,1,0);

		offscreenTargetSeparator.addChild(color);


		//offscreenTargetDisplay.string.setValue("60.0 FPS");
		offscreenTargetDisplay.enableNotify(false);
		offscreenTargetDisplay.justification.setValue(SoText2.Justification.CENTER);

		offscreenTargetSeparator.addChild(offscreenTargetDisplay);

		sep.addChild(offscreenTargetSeparator);
	}

	private SoNode buildOracle(boolean shadow) {

		SoSeparator oracleSeparator = new SoSeparator();

		SoTranslation oraclePosition = new SoTranslation();

		oraclePosition.translation.setValue(ORACLE_X,ORACLE_Y,ORACLE_Z-zTranslation - 0.74f);

		oracleSeparator.addChild(oraclePosition);

		//oracleSeparator.addChild(transl);

		SoRotation oracleRot = new SoRotation();
		oracleRot.rotation.setValue(new SbVec3f(1,0,0),(float)Math.PI/2);

		oracleSeparator.addChild(oracleRot);

		SoComplexity complexity = new SoComplexity();
		complexity.value.setValue(1); // Complexity must not exceed one

		oracleSeparator.addChild(complexity);

		SoMaterial material = new SoMaterial();

		material.diffuseColor.setValue(1,0,0);

		oracleSeparator.addChild(material);

		SoCylinder oracle = new SoCylinder();

		oracle.height.setValue(1.75f - 0.8f);
		oracle.radius.setValue(0.4f);
		oracle.parts.setValue(SoCylinder.Part.SIDES);

		oracleSeparator.addChild(oracle);

		SoTranslation headPos = new SoTranslation();

		headPos.translation.setValue(0,1.75f/2 - 0.4f,0);

		oracleSeparator.addChild(headPos);

		SoSphere oracleHead = new SoSphere();
		oracleHead.radius.setValue(0.4f);
		oracleHead.subdivision.setValue(32);

		oracleSeparator.addChild(oracleHead);

		SoTranslation footPos = new SoTranslation();

		footPos.translation.setValue(0,(-1.75f/2 + 0.4f)*2,0);

		oracleSeparator.addChild(footPos);

		oracleSeparator.addChild(oracleHead);

		oracleSeparator.addChild( shadow ? oracleSpeechSwitchShadow : oracleSpeechSwitch);

		SoGroup speechGroup = new SoGroup();

		if(shadow) {
			oracleSpeechSwitchShadow.addChild(speechGroup);
		}
		else {
			oracleSpeechSwitch.addChild(speechGroup);
		}

		SoFont font = new SoFont();

		font.size.setValue(0.2f);

		speechGroup.addChild(font);

		SoMaterial materialFont = new SoMaterial();

		materialFont.emissiveColor.setValue(1,1,0);
		materialFont.diffuseColor.setValue(1,1,0);

		speechGroup.addChild(materialFont);

		speechGroup.addChild(oracleSpeechRotation);

		speechGroup.addChild(speechTranslation);

		speechGroup.addChild(oracleSpeech);

		return oracleSeparator;
	}

	private void hideOracleIfTooFar() {
		double squareDist = Math.pow(current_x - ORACLE_X,2) + Math.pow(current_y-ORACLE_Y,2);
		boolean isShown = oracleSwitch.whichChild.getValue() == SoSwitch.SO_SWITCH_ALL;
		boolean speechShown = oracleSpeechSwitch.whichChild.getValue() == SoSwitch.SO_SWITCH_ALL;
		double hysteresis = speechShown ? 50 : 0;
		boolean mustShow = squareDist < 200*200;
		boolean mustShowSpeech = squareDist < 20*20 + hysteresis;

		if( mustShow != isShown) {
			oracleSwitch.whichChild.setValue(mustShow ? SoSwitch.SO_SWITCH_ALL : SoSwitch.SO_SWITCH_NONE);
			oracleSwitchShadow.whichChild.setValue(mustShow ? SoSwitch.SO_SWITCH_ALL : SoSwitch.SO_SWITCH_NONE);
		}

		if(mustShowSpeech != speechShown) {
			oracleSpeechSwitch.whichChild.setValue(mustShowSpeech ? SoSwitch.SO_SWITCH_ALL : SoSwitch.SO_SWITCH_NONE);
			oracleSpeechSwitchShadow.whichChild.setValue(mustShowSpeech ? SoSwitch.SO_SWITCH_ALL : SoSwitch.SO_SWITCH_NONE);
		}
	}

	public float getZ(int i, int j) {
		int wImageW = rw.getWidth();

		int index = i*h+j;
		//chunks.verticesPut(index*3+0, i * delta_x);
		//chunks.verticesPut(index*3+1, (h - j -1) * delta_y);
		float z = ((i+I_START) >= wImageW ? re.getPixel(i+I_START-wImageW+overlap, j, fArray)[0] - delta : rw.getPixel(i+I_START, j, fArray)[0]);
		if( Math.abs(z)> 1e30 || i == 0 || j == 0 || i == w-1 || j == h-1 ) {
			z= ZMIN;
		}
		return z;
	}

	/**
	 * For garbage collecting
	 */
	public void clearRasters() {
		re = null;
		rw = null;
	}

	public int getNbI() {
		return w;
	}

	public long getNbJ() {
		return h;
	}

	public double getWidth() {
		return  delta_x * (w - 1);
	}

	public double getHeight() {
		return  delta_y * (h - 1);
	}

	public double getExtraDY() {
		return jstart * delta_y;
	}

	public void addWater(SoGroup group, float z, float transparency, boolean shining, boolean small) {
		
	    SoSeparator waterSeparator = new SoSeparator();
	    
		SoCube water = new SoCube();
		
		water.depth.setValue(CUBE_DEPTH);
		water.height.setValue(small ? WATER_HORIZON : WATER_HORIZON*2);
		water.width.setValue(small ? WATER_HORIZON : WATER_HORIZON*2);
		
	    SoMaterial waterMat = new SoMaterial();
	    waterMat.diffuseColor.setValue(/*0.1f*/0.12f*WATER_BRIGHTNESS,0.5f*WATER_BRIGHTNESS,/*0.6f*/0.55f*WATER_BRIGHTNESS); // For 6500 K display
	    waterMat.ambientColor.setValue(0, 0, 0);
	    waterMat.transparency.setValue(transparency);
	    if(shining) {
	    	waterMat.specularColor.setValue(1.0f, 1.0f, 1.0f);
	    	waterMat.shininess.setValue(0.5f);
	    }
	    
	    waterSeparator.addChild(waterMat);
	    
	    SoTranslation waterTranslation = new SoTranslation();
	    
	    waterTranslation.translation.setValue( /*14000*/- 4000 + WATER_HORIZON/2, /*-8000*/0, - /*transl.translation.getValue().getZ()*/z);	    
	    
	    waterSeparator.addChild(waterTranslation);
	    
	    waterSeparator.addChild(water);
	    
	    group.addChild(waterSeparator);
	    
	}
	
	public void addWaterShadow(SoGroup group, float z, float transparency, boolean shining, boolean small) {
		
	    SoSeparator waterSeparator = new SoSeparator();
	    
		SoCubeWithoutTop water = new SoCubeWithoutTop();
		
		water.depth.setValue(CUBE_DEPTH);
		water.height.setValue(small ? WATER_HORIZON : WATER_HORIZON*2);
		water.width.setValue(small ? WATER_HORIZON : WATER_HORIZON*2);
		
	    SoMaterial waterMat = new SoMaterial();
	    waterMat.diffuseColor.setValue(0.12f*WATER_BRIGHTNESS,0.5f*WATER_BRIGHTNESS,0.55f*WATER_BRIGHTNESS); // For 6500 K display
	    waterMat.ambientColor.setValue(0, 0, 0);
	    waterMat.transparency.setValue(transparency);
	    if(shining) {
	    	waterMat.specularColor.setValue(1.0f, 1.0f, 1.0f);
	    	waterMat.shininess.setValue(0.5f);
	    }
	    
	    waterSeparator.addChild(waterMat);
	    
	    SoTranslation waterTranslation = new SoTranslation();
	    
	    waterTranslation.translation.setValue( /*14000*/- 4000 + WATER_HORIZON/2, /*-8000*/0, - /*transl.translation.getValue().getZ()*/z);	    
	    
	    waterSeparator.addChild(waterTranslation);
	    
	    waterSeparator.addChild(water);
	    
	    group.addChild(waterSeparator);
	    
	}
	
	public SoNode getSceneGraph() {
		return sep;
	}

	@Override
	public float getCenterY() {
		return centerY;
	}

	@Override
	public float getCenterX() {
		return centerX;
	}

	@Override
	public void setPosition(float x, float y) {
		transl.translation.setValue(-x,-y,-zTranslation);
	}

	@Override
	public void setSunPosition(SbVec3f sunPosition) {
		SbVec3f dir = sunPosition.operator_minus();
		
		SbVec3f p1 = null;
		float a = dir.getX();
		float b = dir.getY();
		float c = dir.getZ();
		
		float aa = Math.abs(a);
		float ba = Math.abs(b);
		float ca = Math.abs(c);
		
		int max = 0;
		if( ba > aa) {
			max = 1;
			if(ca > ba) {
				max = 2;
			}
		}
		else {
			if( ca > aa) {
				max = 2;
			}
		}
		if(max == 2) {
			p1 = new SbVec3f(1,1,-(a+b)/c);
		}
		else if(max == 1) {
			p1 = new SbVec3f(1,-(a+c)/b,1);
		}
		else if( max == 0) {
			p1 = new SbVec3f(-(c+b)/a,1,1);
		}
		p1.normalize();
		SbVec3f p2 = dir.cross(p1);
		
		SbVec3f p3 = p1.operator_add(p2.operator_mul(0.5f)); p3.normalize();
		
		SbVec3f p4 = dir.cross(p3);
		
		float angle = SUN_RADIUS / SUN_REAL_DISTANCE * 0.8f;
		
	    r1.setValue(p3, angle);
	    r2.setValue(p4, angle);
	    r3.setValue(p3, -angle);
	    r4.setValue(p4, -angle);
	    
		
		sun[1].direction.setValue(r1.multVec(dir));
		sun[2].direction.setValue(r2.multVec(dir));
		sun[0].direction.setValue(r3.multVec(dir));
		sun[3].direction.setValue(r4.multVec(dir));
		
		boolean wasEnabled = sunTransl.translation.enableNotify(false); // In order not to invalidate shaders
		sunTransl.translation.setValue(sunPosition.operator_mul(SUN_FAKE_DISTANCE));
		sunTransl.translation.enableNotify(wasEnabled);
		//inverseSunTransl.translation.setValue(sunPosition.operator_mul(SUN_FAKE_DISTANCE).operator_minus());
		
		float sunElevationAngle = (float)Math.atan2(sunPosition.getZ(), Math.sqrt(Math.pow(sunPosition.getX(),2.0f)+Math.pow(sunPosition.getY(),2.0f)));
		float sinus = (float)Math.sin(sunElevationAngle);
		for(int is=0;is<4;is++) {	    		    
		    sun[is].maxShadowDistance.setValue(1e4f + (1 - sinus)*1e5f);
		    sun[is].bboxSize.setValue(5000+is*3000 + (1 - sinus)*10000, 5000+is*3000 + (1 - sinus)*10000, 2000);
		    sun[is].nearBboxSize.setValue(80+is*10 + (1-sinus)*100,80+is*10+(1-sinus)*100,300);
		}
	}

	private final SbVec3f dummy = new SbVec3fSingle();

	private final Map<Integer,DGeom> geoms = new HashMap<>();

	private final Set<Integer> nearGeoms = new HashSet<>();
	
	public float getZ(float x, float y, float z) {
		
		current_x = x;
		current_y = y;
		
		if(FLY) {
			current_z = z;
			//setBBoxCenter();
			return current_z;
		}
		float newZ = getGroundZ();
		
		current_z = newZ;

		updateNearGeoms();

		//setBBoxCenter();
		return current_z;
	}

	public void updateNearGeoms() {

		if( null == forest) {
			return;
		}
		if( null == space) {
			return;
		}

		SbVec3f trans = transl.translation.getValue();
		dummy.setValue(current_x - trans.getX(), current_y - trans.getY(), current_z - trans.getZ());

		SbSphere sphere = new SbSphere(dummy,99);
		SbListInt points = new SbListInt();
		treesBSPTree.findPoints(sphere,points);
		float trunk_width_coef = DouglasFir.trunk_diameter_angle_degree*(float)Math.PI/180.0f;
		nearGeoms.clear();
		for(int i=0; i< points.getLength();i++) {
			int bsp_index = points.operator_square_bracket(i);
			if (bsp_index == -1) {
				continue;
			}
			int tree_index = (int) treesBSPTree.getUserData(bsp_index);
			if (!geoms.containsKey(tree_index)) {
				float xd = forest.xArray[tree_index];
				float yd = forest.yArray[tree_index];
				float zd = forest.zArray[tree_index];
				float height = forest.heightArray[tree_index];
				float width = height * trunk_width_coef;

				DGeom box = OdeHelper.createCapsule(space, width, height);
				box.setPosition(xd + trans.getX(), yd + trans.getY(), zd + trans.getZ()+height/2);
				geoms.put(tree_index,box);
			}
			nearGeoms.add(tree_index);
		}
		Set<Map.Entry<Integer,DGeom>> entrySet = new HashSet<>();
		entrySet.addAll(geoms.entrySet()); // To avoid ConcurrentModificationException
		for( Map.Entry entry : entrySet) {
			if(!nearGeoms.contains(entry.getKey())) {
				DGeom g = (DGeom)entry.getValue();
				g.destroy();
				//space.remove(g);
				geoms.remove(entry.getKey());
			}
		}
	}

	public float getGroundZ() {

		float newZ = getInternalZ(current_x,current_y, new int[4]);

		if( newZ < - 150 - zTranslation + CUBE_DEPTH/2 -1.5f) {
			newZ = - 150 - zTranslation + CUBE_DEPTH/2 -1.5f;
		}

		return newZ;
	}
	
	private void setBBoxCenter() {
		  SbVec3f world_camera_direction = camera.orientation.getValue().multVec(new SbVec3f(0,0,-1)); 
		  
		  world_camera_direction.normalize();
		  
		for(int is=0;is<4;is++) {
		    sun[is].bboxCenter.setValue(
		    		current_x+2000*world_camera_direction.getX(),
		    		current_y+2000*world_camera_direction.getY(), /*current_z*/getGroundZ());
		    sun[is].nearBboxCenter.setValue(
		    		current_x+40*world_camera_direction.getX(),
					current_y+40*world_camera_direction.getY(),
					getGroundZ()
			);
		}		
		
		float xTransl = - transl.translation.getValue().getX();
		
		float yTransl = - transl.translation.getValue().getY();
		
		float zTransl = - transl.translation.getValue().getZ();
		
		float trees_x = current_x + xTransl+douglas_distance_foliage[0]*0.4f/*3000*/*world_camera_direction.getX();
		float trees_y = current_y + yTransl+douglas_distance_foliage[0]*0.4f/*3000*/*world_camera_direction.getY();
		
		douglasTreesRefPoint.setValue(trees_x,trees_y,current_z + zTransl);
		
		for(SoNode nodeForX : douglasTreesT.getChildren()) {
			SoLODGroup sepForX = (SoLODGroup) nodeForX;
			sepForX.referencePoint.setValue(trees_x,trees_y,current_z + zTransl);
//			for( SoNode node : sepForX.getChildren()) {
//			SoLODIndexedFaceSet lifs = (SoLODIndexedFaceSet) node;
//			lifs.referencePoint.setValue(
//					/*current_x + xTransl+3000*world_camera_direction.getX()*/trees_x,
//					/*current_y + yTransl+3000*world_camera_direction.getY()*/trees_y,
//					current_z + zTransl);
//			}
		}
				
		for(SoNode nodeForX : douglasTreesF.getChildren()) {
			SoLODGroup sepForX = (SoLODGroup) nodeForX;
			sepForX.referencePoint.setValue(trees_x,trees_y,current_z + zTransl);
//			for( SoNode node : sepForX.getChildren()) {
//			SoLODIndexedFaceSet lifs = (SoLODIndexedFaceSet) node;
//			lifs.referencePoint.setValue(
//					/*current_x + xTransl+3000*world_camera_direction.getX()*/trees_x,
//					/*current_y + yTransl+3000*world_camera_direction.getY()*/trees_y,
//					current_z + zTransl);
//			}
		}
				
		float treesS_x = current_x + xTransl+douglas_distance_shadow_foliage[0]*0.3f/*1000*/*world_camera_direction.getX();
		float treesS_y = current_y + yTransl+douglas_distance_shadow_foliage[0]*0.3f/*1000*/*world_camera_direction.getY();
		
		douglasTreesSRefPoint.setValue(treesS_x,treesS_y,current_z + zTransl);
		
		for (SoNode nodeForX : douglasTreesST.getChildren()) {
			SoLODGroup sepForX = (SoLODGroup) nodeForX;
			sepForX.referencePoint.setValue(treesS_x,treesS_y,current_z + zTransl);
//			for( SoNode node : sepForX.getChildren()) {
//			SoLODIndexedFaceSet lifs = (SoLODIndexedFaceSet) node;
//			lifs.referencePoint.setValue(
//					/*current_x + xTransl+1000*world_camera_direction.getX()*/treesS_x, 
//					/*current_y + yTransl+1000*world_camera_direction.getY()*/treesS_y, 
//					current_z + zTransl);
//			}
		}
				
		for (SoNode nodeForX : douglasTreesSF.getChildren()) {
			SoLODGroup sepForX = (SoLODGroup) nodeForX;
			sepForX.referencePoint.setValue(treesS_x,treesS_y,current_z + zTransl);
//			for( SoNode node : sepForX.getChildren()) {
//			SoLODIndexedFaceSet lifs = (SoLODIndexedFaceSet) node;
//			lifs.referencePoint.setValue(
//					/*current_x + xTransl+1000*world_camera_direction.getX()*/treesS_x, 
//					/*current_y + yTransl+1000*world_camera_direction.getY()*/treesS_y, 
//					current_z + zTransl);
//			}
		}
		
		float targets_x = current_x + xTransl/*+SoTarget.MAX_VIEW_DISTANCE*world_camera_direction.getX()*0.8f*/;
		float targets_y = current_y + yTransl/*+SoTarget.MAX_VIEW_DISTANCE*world_camera_direction.getY()*0.8f*/;

		cameraDirection.setValue(world_camera_direction);
		cameraDirection.setZ(0);
		
		targetsRefPoint.setValue(targets_x,targets_y,current_z + zTransl);
	}
	
	public int[] getIndexes(float x, float y, int[] indices) {
		float ifloat = (x - transl.translation.getValue().getX())/delta_x;
		float jfloat = (delta_y*(h-1) -(y - transl.translation.getValue().getY() - jstart * delta_y))/delta_y;
		
		int i = Math.round(ifloat);
		//int j = Math.round((y - transl.translation.getValue().getY() - 3298)/delta_y);
		int j = Math.round(jfloat);
		
		if(i <0 || i>w-1 || j<0 || j>h-1) {
			return null;
		}
		i = Math.max(0,i);
		j = Math.max(0,j);
		i = Math.min(w-1,i);
		j = Math.min(h-1,j);
		
		int imin = (int)Math.floor(ifloat);
		int imax = (int)Math.ceil(ifloat);
		int jmin = (int)Math.floor(jfloat);
		int jmax = (int)Math.ceil(jfloat);
		
		imin = Math.max(0,imin);
		jmin = Math.max(0,jmin);
		imin = Math.min(w-1,imin);
		jmin = Math.min(h-1,jmin);
		
		imax = Math.max(0,imax);
		jmax = Math.max(0,jmax);
		imax = Math.min(w-1,imax);
		jmax = Math.min(h-1,jmax);
		
		int index0 = imin*h + jmin;
		int index1 = imax*h + jmin;
		int index2 = imax*h + jmax;
		int index3 = imin*h + jmax;
		
		if(indices == null) {
			indices = new int[4];
		}
		indices[0] = index0;
		indices[1] = index1;
		indices[2] = index2;
		indices[3] = index3;
				
		return indices;
	}

	public float getIFloat(float x) {
		float ifloat = (x - transl.translation.getValue().getX())/delta_x;
		return ifloat;
	}

	public float getJFloat(float y) {
		float jfloat = (delta_y*(h-1) -(y - transl.translation.getValue().getY() - jstart * delta_y))/delta_y;
		return jfloat;
	}
	
	public float getInternalZ(float x, float y, int[] indices) {
		
		float ifloat = getIFloat(x);// (x - transl.translation.getValue().getX())/delta_x;
		float jfloat = getJFloat(y);// (delta_y*(h-1) -(y - transl.translation.getValue().getY() - jstart * delta_y))/delta_y;
		
		int i = Math.round(ifloat);
		//int j = Math.round((y - transl.translation.getValue().getY() - 3298)/delta_y);
		int j = Math.round(jfloat);
		
		if(i <0 || i>w-1 || j<0 || j>h-1) {
			return ZMIN - zTranslation;
		}
		i = Math.max(0,i);
		j = Math.max(0,j);
		i = Math.min(w-1,i);
		j = Math.min(h-1,j);
		
		int imin = (int)Math.floor(ifloat);
		int imax = (int)Math.ceil(ifloat);
		int jmin = (int)Math.floor(jfloat);
		int jmax = (int)Math.ceil(jfloat);
		/*
		imin = Math.max(0,imin);
		jmin = Math.max(0,jmin);
		imin = Math.min(w-1,imin);
		jmin = Math.min(h-1,jmin);
		
		imax = Math.max(0,imax);
		jmax = Math.max(0,jmax);
		imax = Math.min(w-1,imax);
		jmax = Math.min(h-1,jmax);
		
		int index0 = imin*h + jmin;
		int index1 = imax*h + jmin;
		int index2 = imax*h + jmax;
		int index3 = imin*h + jmax;
		*/
		/*int[] indices = */;
		if(getIndexes(x,y,indices) == null) {
			return ZMIN - zTranslation;			
		}
		int index0 = indices[0]; // imin, jmin
		int index1 = indices[1]; // imax, jmin
		int index2 = indices[2]; // imax, jmax
		int index3 = indices[3]; // imin, jmax
		
		float z0 = chunks.verticesGetZ(index0) - zTranslation;
		float z1 = chunks.verticesGetZ(index1) - zTranslation;
		float z2 = chunks.verticesGetZ(index2) - zTranslation;
		float z3 = chunks.verticesGetZ(index3) - zTranslation;
		
		float alpha = ifloat - imin;
		float beta = jfloat - jmin;
		
		//int index = i*h+ h - j -1;
		int index = i*h+ j;
		
		//z = chunks.verticesGet(index*3+2) - zTranslation;

		float za = 0;

		if(alpha + beta < 1) { // imin, jmin
			za = z0 + (z1 - z0)*alpha + (z3 - z0)*beta;
		}
		else { // imax, jmax
			za = z2 + (z3 - z2)*(1-alpha) + (z1 - z2)*(1-beta);
		}

		float zb = 0;

		if(alpha + (1-beta) < 1) { // imin, jmax
			zb = z3 + (z2 - z3)*alpha + (z0 - z3)*(1-beta);
		}
		else { // imax, jmin
			zb = z1 + (z0 - z1)*(1-alpha) + (z2 - z1)*beta;
		}

		float z = Math.min(za,zb);

		//z/= 2.0f;
		
//		float xx = chunks.verticesGet(index*3+0);
//		float yy = chunks.verticesGet(index*3+1);
//		
//		System.out.println("i = "+i+" j = "+j);
//		
//		System.out.println("y ="+(y - transl.translation.getValue().getY())+ " yy = "+yy);
//		
//		float delta = yy - (y - transl.translation.getValue().getY());
//		
//		System.out.println("delta = " +delta);
		return z;
	}
	
	public boolean isStone(float x, float y) {
		int[] indices = getIndexes(x, y, null);
		if(indices == null) {
			return true;
		}
		boolean stone0 = chunks.isStone(indices[0]); 
		boolean stone1 = chunks.isStone(indices[1]); 
		boolean stone2 = chunks.isStone(indices[2]); 
		boolean stone3 = chunks.isStone(indices[3]);
		
		return stone0 || stone1 || stone2 || stone3;
	}
	
	DouglasForest forest;
	
	private void computeDouglas(final JProgressBar progressBar) {
		
		forest = new DouglasForest(this);
		
		int nbDouglas = forest.compute(progressBar);
		
		forest.buildDouglasChunks();
		
		forest.fillDouglasChunks();
		
		forest.computeDouglas();

		final SbVec3fSingle treePoint = new SbVec3fSingle();

		int progressBarInitialValue = progressBar.getValue();

		for( int i=0; i< forest.NB_DOUGLAS_SEEDS; i++) {
			if( 0 == i%999 ) {
				progressBar.setValue((int)(progressBarInitialValue + (MAX_PROGRESS - progressBarInitialValue)*(long)i/forest.NB_DOUGLAS_SEEDS));
			}

			float x = forest.xArray[i];
			float y = forest.yArray[i];
			float z = forest.zArray[i];

			if(Float.isNaN(x)) {
				continue;
			}
			treePoint.setValue(x,y,z);
			treesBSPTree.addPoint(treePoint,i);
		}
		progressBar.setValue(MAX_PROGRESS);
	}
		
	SoGroup getDouglasTreesT(SbVec3f refPoint, final float[] distance,final JProgressBar progressBar) {
		
		if( forest == null) {
			computeDouglas(progressBar);
		}
		
		return forest.getDouglasTreesT(refPoint, distance);			
	}	
	
	SoGroup getDouglasTreesF(SbVec3f refPoint, final float[] distance, boolean withColors,final JProgressBar progressBar) {
		
		if( forest == null) {
			computeDouglas(progressBar);
		}
		
		return forest.getDouglasTreesF(refPoint, distance, withColors);			
	}	
	
	static Random random = new Random(42);
	
	@Override
	public void preDestroy() {
		
		shadowGroup.removeAllChildren();
		
		for(int i=0; i<4; i++) {
			sun[i].on.setValue(false);
		}
		
		SbViewportRegion region = new SbViewportRegion();

		SoGLRenderAction render = new SoGLRenderAction(region);
		render.apply(shadowGroup);
		
		render.destructor();
	}

	@Override
	public void setCamera(SoCamera camera) {
		this.camera = camera;
	    master.setCamera(camera);	
	    masterS.setCamera(camera);
		RecursiveChunk.setCamera(chunkTree, camera);
		RecursiveChunk.setCamera(shadowTree, camera);		
	}

	@Override
	public void idle() {
		setBBoxCenter();
		hideOracleIfTooFar();

		runIdleCB();

		if(trailsDirty && !CBRunning) {

			CBRunning = true;
			trailsDirty = false;

			Thread t = new Thread() {
				@Override
				public void run() {

					final OverallTexture ot = chunks.getOverallTexture();

					addIdleCB(()-> {
						final SoTexture2 newGigaTexture = ot.getTexture();
						newGigaTexture.ref();
						final SbVec2s s = new SbVec2s();
						final int[] nc = new int[1];
						final boolean[] srgb = new boolean[1];
						MemoryBuffer mb = newGigaTexture.image.getValue(s,nc,srgb);
						gigaTexture.image.setValue(s,nc[0],srgb[0],mb, true);
						newGigaTexture.unref();
					});
				}
			};
			t.start();
		}
	}

	synchronized void addIdleCB(Runnable r) {
		idleCallbacks.add(r);
	}

	synchronized void runIdleCB() {
		for( var r : idleCallbacks) {
			CBRunning = false;
			r.run();
		}
		idleCallbacks.clear();
	}

	public float getzTranslation() {
		return zTranslation;
	}

	public ChunkArray getChunks() {
		return chunks;
	}

	public SoVolumetricShadowGroup getShadowGroup() {
		return shadowGroup;
	}

	public SoEnvironment getEnvironment() {
		return environment;
	}

	public void enableNotifySun() {
		for( int i=0;i<4;i++) {
			sun[i].enableNotify(true);
		}
	}

	public void disableNotifySun() {
		for( int i=0;i<4;i++) {
			sun[i].enableNotify(false);
		}
	}

	public float getLevelOfDetail() {
		return LEVEL_OF_DETAIL / master.getLodFactor();
	}

	public float getLevelOfDetailShadow() {
		return LEVEL_OF_DETAIL_SHADOW / masterS.getLodFactor();
	}

	public void setLevelOfDetail(float levelOfDetail) {
		float lodFactor = LEVEL_OF_DETAIL / levelOfDetail;
		master.setLodFactor(lodFactor);
	}

	public void setLevelOfDetailShadow(float levelOfDetailShadow) {
		float lodFactor = LEVEL_OF_DETAIL_SHADOW / levelOfDetailShadow;
		masterS.setLodFactor(lodFactor);
	}

	public void setTreeDistance(float treeDistance) {
		douglas_distance_trunk[0] = treeDistance/2;
		douglas_distance_foliage[0] = treeDistance;
	}

	public void setTreeShadowDistance(float treeShadowDistance) {
		douglas_distance_shadow_trunk[0] = treeShadowDistance/2;
		douglas_distance_shadow_foliage[0] = treeShadowDistance;
	}

	public float getTreeDistance() {
		return douglas_distance_foliage[0];
	}

	public float getTreeShadowDistance() {
		return douglas_distance_shadow_foliage[0];
	}

	public void setFPS(float fps) {
		if( fpsSwitch.whichChild.getValue() == SoSwitch.SO_SWITCH_NONE) {
			return; // FPS disabled
		}

		String str = String.format("%.1f fps", fps);
		//if (!Objects.equals(fpsDisplay.string.getValueAt(0),str)) { // Better foster frame regularity
			fpsDisplay.string.setValue(str);
		//}
	}

	public void enableFPS(boolean enable) {
		fpsSwitch.whichChild.setValue(enable ? SoSwitch.SO_SWITCH_ALL : SoSwitch.SO_SWITCH_NONE);
	}

	public boolean isFPSEnabled() {
		return fpsSwitch.whichChild.getValue() == SoSwitch.SO_SWITCH_ALL;
	}

	public int getMaxI() {
		return max_i;
	}

	public void setMaxI(int max_i) {
		this.max_i = max_i;
	}

	public void setSpace(DSpace space) {
		this.space = space;
	}

	public void addTarget(Target target) {
		targets.add(target);
	}

	public void shootTarget(Target t, int instance) {
		registerShot(targets.indexOf(t),instance);
		doShootTarget(t,instance);
	}

	public void registerShot(int index, int instance) {
		shotTargetsIndices.add(index);
		shotTargetsInstances.add(instance);
	}

	public boolean isShot(Target t, int instance) {
		int index = targets.indexOf(t);
		int nbShots = shotTargetsIndices.size();
		for(int i=0; i<nbShots; i++) {
			if(shotTargetsIndices.get(i) == index) {
				if(shotTargetsInstances.get(i) == instance) {
					return true;
				}
			}
		}
		return false;
	}

	public void doShootTarget(Target t, int instance) {

		shotTargets.add(t.targetName());

		String[] targets = new String[shotTargets.size()];
		int i=0;
		for(String name : shotTargets) {
			targets[i] = name;
			i++;
		}
		targetDisplay.string.setValues(0,targets);
	}

	public void loadShots(Properties saveGameProperties) {
		int i=0;
		String keyIndex = "targetShotIndex";
		String keyInstance = "targetShotInstance";
		while(saveGameProperties.containsKey(keyIndex+i)) {
			int index = Integer.valueOf(saveGameProperties.getProperty(keyIndex+i));
			int instance = Integer.valueOf(saveGameProperties.getProperty(keyInstance+i));

			SoTargets targetsNode = (SoTargets) targetsGroup.getChild(index);

			SoTarget target = targetsNode.getTargetChild(instance);

			SoVRMLBillboard billboard = (SoVRMLBillboard) target.getChild(1);

			SoMaterial c = new SoMaterial();
			c.diffuseColor.setValue(1, 0, 0);
			//billboard.enableNotify(false);
			billboard.insertChild(c, 0);
			//billboard.enableNotify(true);

			targets.get(index).setGroup(billboard,instance);
			shootTarget(targets.get(index),instance);
			i++;
		}
	}

	public void talk(String[] whatToSay) {
		oracleSpeech.justification.setValue(SoText3.Justification.CENTER);

		float angle = (float)Math.atan2(current_y - ORACLE_Y, current_x - ORACLE_X) + (float)Math.PI/2;

		oracleSpeechRotation.rotation.setValue(new SbVec3f(0,1,0),angle);

		speechTranslation.translation.setValue(0,0.2f + 1.75f/2,0.5f);

		oracleSpeech.string.setNum(0);
		oracleSpeech.string.setValues(0,whatToSay);
	}

	public boolean haveShot(String targetName) {
		return shotTargets.contains(targetName);
	}

	public void setMessage(String message) {
		if(messageDisplay.string.getNum() != 1 || !Objects.equals(messageDisplay.string.getValueAt(0),message)) {
			messageDisplay.string.setValue(message);
		}
	}

	public void aim(boolean aim) {
		if( AIM ) {
			aimSwitch.whichChild.setValue(aim ? SoSwitch.SO_SWITCH_ALL : SoSwitch.SO_SWITCH_NONE);
		}
	}

	public void addPlank(SoQtConstrainedViewer viewer, SbVec3f translation, SbRotation rotation) {
		final SoSeparator plankSeparator = new SoSeparator();
		final SoCube plank = new SoCube();
		plank.setName("plank");
		plank.width.setValue(3.5f); // X
		plank.height.setValue(20.0f); // Y
		plank.depth.setValue(0.1f); // Z
		final SoTranslation plankLayerTranslation = new SoTranslation();
		plankLayerTranslation.translation.setValue(translation);
		plankSeparator.addChild(plankLayerTranslation);
		final SoRotation plankRotation = new SoRotation();
		plankRotation.rotation.setValue(rotation);
		plankSeparator.addChild(plankRotation);
		final SoTranslation plankFrontTranslation = new SoTranslation();
		plankFrontTranslation.translation.setValue(0.0f,plank.height.getValue()/2.0f + 2.0f,0.13f - 1.75f -0.05f);
		plankSeparator.addChild(plankFrontTranslation);
		plankSeparator.addChild(plank);
		planksSeparator.addChild(plankSeparator);

		planksTranslations.add(translation);
		planksRotations.add(rotation);

		DBox box = OdeHelper.createBox(space,3.5,20.0,0.1);
		planks.add(box);
	}

	public void movePlank(SoQtConstrainedViewer viewer, SbVec3f translation, SbRotation rotation) {
		final SoSeparator plankSeparator = (SoSeparator) planksSeparator.getChild(planksSeparator.getNumChildren()-1);
		final SoTranslation plankLayerTranslation = (SoTranslation) plankSeparator.getChild(0);
		final SoRotation plankRotation = (SoRotation) plankSeparator.getChild(1);

		planksTranslations.get(planks.size()-1).setValue(translation);

		float[] q0q1q2q3 = rotation.getValue();

		planksRotations.get(planks.size()-1).setValue(q0q1q2q3);

		plankLayerTranslation.translation.setValue(translation);
		plankRotation.rotation.setValue(rotation);

		SoSearchAction sa = new SoSearchAction();

		final SoCube plank = (SoCube) plankSeparator.getChild(3);
		sa.setNode(plank);
		sa.apply(getSceneGraph());

		SoPath path = sa.getPath();

		SoGetMatrixAction getMatrixAction = new SoGetMatrixAction(viewer.getSceneHandler().getViewportRegion());
		getMatrixAction.apply(path);
		SbMatrix matrix = getMatrixAction.getMatrix();

		DBox box = planks.get(planks.size()-1);

		final SbVec3f mat_translation = new SbVec3f();
		final SbRotation mat_rotation = new SbRotation();
		final SbVec3f mat_scaleFactor = new SbVec3f();
		final SbRotation mat_scaleOrientation = new SbRotation();
		final SbVec3f mat_center = new SbVec3f();
		matrix.getTransform(mat_translation, mat_rotation,mat_scaleFactor,mat_scaleOrientation,mat_center);

		double x=mat_translation.x(),y=mat_translation.y(),z=mat_translation.z();

		box.setPosition(x,y,z);

		SbMatrix rot_matrix = rotation.getMatrix();

		double d = rot_matrix.getValueAt(0),
				e = rot_matrix.getValueAt(1),
				f = rot_matrix.getValueAt(2),
				g = rot_matrix.getValueAt(3),
				h = rot_matrix.getValueAt(4),
				i = rot_matrix.getValueAt(5),
				j = rot_matrix.getValueAt(6),
				k = rot_matrix.getValueAt(7),
				l = rot_matrix.getValueAt(8),
				m = rot_matrix.getValueAt(9),
				n = rot_matrix.getValueAt(10),
				o = rot_matrix.getValueAt(11);
		DMatrix3C rot = new DMatrix3(d,e,f,g,h,i,j,k,l,m,n,o);

		box.setRotation(rot.reTranspose());
	}

	public void removePlank(SoNode sep) {
		int index = planksSeparator.findChild(sep);
		if(index != -1) {
			planksSeparator.removeChild(index);
			DBox box = planks.remove(index-1);
			box.destroy();
			planksRotations.remove(index-1);
			planksTranslations.remove(index-1);
		}
	}

	public SbVec3f getPosition() {
		return new SbVec3f(current_x,current_y,current_z);
	}

	public SbVec3f getTranslation() {
		return transl.translation.getValue();
	}

	public void storePlanks(Properties planksProperties) {
		int nbPlanks = planks.size();

		float[] q0q1q2q3 = new float[4];

		for( int plankIndex=0; plankIndex < nbPlanks; plankIndex++) {
			String key = "plankTranslX"+plankIndex;
			String value = Float.toString(planksTranslations.get(plankIndex).getX());
			planksProperties.put(key,value);

			key = "plankTranslY"+plankIndex;
			value = Float.toString(planksTranslations.get(plankIndex).getY());
			planksProperties.put(key,value);

			key = "plankTranslZ"+plankIndex;
			value = Float.toString(planksTranslations.get(plankIndex).getZ());
			planksProperties.put(key,value);

			planksRotations.get(plankIndex).getValue(q0q1q2q3);

			key = "plankRotQ0"+plankIndex;
			value = Float.toString(q0q1q2q3[0]);
			planksProperties.put(key,value);

			key = "plankRotQ1"+plankIndex;
			value = Float.toString(q0q1q2q3[1]);
			planksProperties.put(key,value);

			key = "plankRotQ2"+plankIndex;
			value = Float.toString(q0q1q2q3[2]);
			planksProperties.put(key,value);

			key = "plankRotQ3"+plankIndex;
			value = Float.toString(q0q1q2q3[3]);
			planksProperties.put(key,value);
		}
	}

	public void loadPlanks(SoQtConstrainedViewer viewer, Properties plankProperties) {
		int plankIndex = 0;
		String key;
		while(plankProperties.containsKey((key="plankTranslX"+plankIndex))) {
			float x = Float.valueOf(plankProperties.getProperty(key,"0"));
			key = "plankTranslY"+plankIndex;
			float y = Float.valueOf(plankProperties.getProperty(key,"0"));
			key = "plankTranslZ"+plankIndex;
			float z = Float.valueOf(plankProperties.getProperty(key,"0"));
			key = "plankRotQ0"+plankIndex;
			float q0 = Float.valueOf(plankProperties.getProperty(key,"0"));
			key = "plankRotQ1"+plankIndex;
			float q1 = Float.valueOf(plankProperties.getProperty(key,"0"));
			key = "plankRotQ2"+plankIndex;
			float q2 = Float.valueOf(plankProperties.getProperty(key,"0"));
			key = "plankRotQ3"+plankIndex;
			float q3 = Float.valueOf(plankProperties.getProperty(key,"1"));

			SbVec3f translation = new SbVec3f(x,y,z);
			SbRotation rotation = new SbRotation(q0,q1,q2,q3);

			addPlank(viewer,translation,rotation);
			movePlank(viewer,translation,rotation);
			
			plankIndex++;
		}
	}

	public void addTrail(int i, int j) {
		int index = i * h + j;
		float normal = chunks.normalsGet(index*3+2);
		if(Math.abs(normal)<0.96) {
			return;
		}
		long code = (((long)j) << 32) | i;
		if(trails.add(code)) {
			if (!chunks.isStone(index)) {
				int red = TRAIL.getRed();
				int green = TRAIL.getGreen();
				int blue = TRAIL.getBlue();
				int alpha = 255;
				chunks.colorsPut(index, red, green, blue, alpha);
				trailsDirty = true;
			}
			float[] xyz = chunks.verticesGet(index,null);
			SbVec3f pt = new SbVec3f(xyz);
			Object data = null;
			trailsBSPTree.addPoint(pt,data);
		}
	}

	public Stream<Long> getTrails() {
		sorted_trails = new ArrayList<>(trails);
		java.util.Collections.sort(sorted_trails);
		return sorted_trails.stream();
	}

	public long getTrailsSize() {
		return trails.size();
	}

	final SbSphere nearSphere = new SbSphere(new SbVec3f(), 2.1f);

	public boolean isNearTrails(SbVec3f point) {
		nearSphere.setValue(point, 2.1f);
		final SbListInt points = new SbListInt();
		trailsBSPTree.findPoints(nearSphere, points);
		return points.size() > 0;
	}

	public void saveShots(Properties properties) {
		int nbShots = shotTargetsIndices.size();
		for(int i=0; i<nbShots; i++) {
			properties.put("targetShotIndex"+i, String.valueOf(shotTargetsIndices.get(i)));
			properties.put("targetShotInstance"+i, String.valueOf(shotTargetsInstances.get(i)));
		}
	}

	public void setBody(DBody body) {
		heroBody = body;
	}

	public void setBallBody(DBody ballBody) {
		heroFeetBody = ballBody;
	}

	public void stopBody() {
		if(null != heroBody) {
			heroBody.setLinearVel(0,0,0);
		}
		if(null != heroFeetBody) {
			heroFeetBody.setLinearVel(0,0,0);
			heroFeetBody.setAngularVel(0,0,0);
		}
	}

	public void setHeroPosition(float x, float y, float z) {
		camera.position.setValue(x,y,z - SCENE_POSITION.getZ());
		camera.orientation.setValue(new SbVec3f(0, 1, 0), -(float) Math.PI / 2.0f);

		SbVec3f cameraPositionValue = camera.position.getValue();

		final float above_ground = //4.5f; // Necessary for planks
				0.2f; // Necessary when respawning on water

		heroBody.setPosition(cameraPositionValue.getX(), cameraPositionValue.getY(), cameraPositionValue.getZ() - /*1.75f / 2*/0.4f + 0.13f + above_ground);
		heroBody.setLinearVel(0,0,0);

		heroFeetBody.setPosition(
				heroBody.getPosition().get0()/*cameraPositionValue.getX()*/,
				heroBody.getPosition().get1()/*cameraPositionValue.getY()*/,
				heroBody.getPosition().get2() /*cameraPositionValue.getZ() - 0.4f + 0.13f + above_ground*/ - 1.75f+ 2*0.4f);

		heroFeetBody.setLinearVel(0,0,0);
		heroFeetBody.setAngularVel(0,0,0);
	}

	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	public void resetScenario(SoQtWalkViewer viewer) {
		scenario.start(0,viewer);
	}

	public void resurrectTheAnimals() {

		int nb = shotTargetsIndices.size();
		for( int i = 0; i< nb; i++) {
			int indice = shotTargetsIndices.get(i);
			int instance = shotTargetsInstances.get(i);
			Target t = targets.get(indice);
			t.resurrect(instance);
		}

		shotTargetsIndices.clear();
		shotTargetsInstances.clear();
		shotTargets.clear();

		targetDisplay.string.setNum(0);
	}

	public float getDistanceFromOracle() {
		return (float)Math.sqrt(
				(current_x - ORACLE_X)*(current_x - ORACLE_X) +
						(current_y - ORACLE_Y)*(current_y - ORACLE_Y) +
						(current_z - ORACLE_Z + zTranslation + 0.74f)*(current_z - ORACLE_Z + zTranslation + 0.74f)
		);
	}

	public float getDistanceFromTrail() {
		SbVec3f trans = transl.translation.getValue();

		SbVec3f hero = new SbVec3f(current_x - trans.getX(),current_y - trans.getY(),current_z - trans.getZ());
		int closest = trailsBSPTree.findClosest(hero);

		float distance = Float.MAX_VALUE;

		if( -1 != closest ) {
			SbVec3f closestPoint = trailsBSPTree.getPoint(closest);
			distance = (float)Math.sqrt(
					(current_x - trans.getX() - closestPoint.getX())*(current_x - trans.getX() - closestPoint.getX()) +
							(current_y - trans.getY() - closestPoint.getY())*(current_y - trans.getY() - closestPoint.getY()) +
							(current_z - trans.getZ() - closestPoint.getZ())*(current_z - trans.getZ() - closestPoint.getZ())
			);
		}
		return distance;
	}

	public float getDistanceFromBeach() {
		SbVec3f trans = transl.translation.getValue();

		SbVec3f hero = new SbVec3f(current_x - trans.getX(),current_y - trans.getY(),current_z - trans.getZ());
		int closest = beachBSPTree.findClosest(hero);

		float distance = Float.MAX_VALUE;

		if(-1 != closest) {
			SbVec3f closestPoint = beachBSPTree.getPoint(closest);
			distance = (float)Math.sqrt(
					(current_x - trans.getX() - closestPoint.getX())*(current_x - trans.getX() - closestPoint.getX()) +
							(current_y - trans.getY() - closestPoint.getY())*(current_y - trans.getY() - closestPoint.getY()) +
							(current_z - trans.getZ() - closestPoint.getZ())*(current_z - trans.getZ() - closestPoint.getZ())
			);
		}
		return distance;
	}

	public void displayObjectives(SoQtWalkViewer viewer) {

		String string1 = "Oracle distance: "+(int)getDistanceFromOracle()+ " m";

		float distanceFromTrail = getDistanceFromTrail();

		String string2 = "Trail distance: "+(int)distanceFromTrail+ " m";
		offscreenTargetDisplay.string.set1Value(0,string1);

		int numStrings = 1;

		if( (!wasDisplayingTrailDistance && distanceFromTrail > 4) ||
				(wasDisplayingTrailDistance && distanceFromTrail > 2)) {
			offscreenTargetDisplay.string.set1Value(1, string2);
			numStrings++;
			wasDisplayingTrailDistance = true;
		}
		else {
			wasDisplayingTrailDistance = false;
		}

		if(searchForSea) {
			float distanceFromSea = getDistanceFromBeach();
			if(distanceFromSea > 20) {
				String string3 = "Beach distance: "+(int)distanceFromSea+" m";
				offscreenTargetDisplay.string.set1Value(numStrings,string3);
				numStrings++;
			}
		}

		offscreenTargetDisplay.string.setNum(numStrings);
	}

	MemoryBuffer loadTexture(String douglasPath, final int[] wi, final int[] hi) {
		File f = new File(douglasPath);
		if(!f.exists()) {
			f = new File("application/"+douglasPath);
		}

		MemoryBuffer buf = null;

		try {
			InputStream is = new FileInputStream(f);
			BufferedImage image = ImageIO.read(is);

			if(image != null) {
				wi[0] = image.getWidth();
				hi[0] = image.getHeight();

				int nc = 3;

				int nbPixels = wi[0]*hi[0];

				MemoryBuffer bytesRGB = MemoryBuffer.allocateBytes(nbPixels*3);
				int j=0;
				for(int i=0; i< nbPixels;i++) {
					int x = i%wi[0];
					int y = hi[0] - i/wi[0] -1;
					int rgb = image.getRGB(x, y);

					float r = (float)Math.pow(((rgb & 0x00FF0000) >>> 16)/255.0f,2.2f)*/*4f*/5f; // Adapted to 6500 K display
					float g = (float)Math.pow(((rgb & 0x0000FF00) >>> 8)/255.0f,2.2f);
					float b = (float)Math.pow(((rgb & 0x000000FF) >>> 0)/255.0f,2.2f)*/*1.5f*/1.47f; // Adapted to 6500 K display
					r = Math.min(r,1);
					g = Math.min(g,1);
					b = Math.min(b,1);
					bytesRGB.setByte(j, (byte)(r*255.0f)) ; j++;
					bytesRGB.setByte(j, (byte)(g*255.0f)); j++;
					bytesRGB.setByte(j, (byte)(b*255.0f)); j++;
				}
				buf = bytesRGB;
			}
			is.close();
		} catch (IOException e) {
		}
		return buf;
	}

	public void setSearchForSea(boolean searchForSea) {
		this.searchForSea = searchForSea;
	}
}
