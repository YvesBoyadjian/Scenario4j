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
import java.util.concurrent.*;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.*;

import application.RasterProvider;
import application.nodes.*;
import application.objects.Hero;
import application.objects.Target;
import application.objects.collectible.BootsFamily;
import application.objects.collectible.Collectible;
import application.objects.enemy.EnemyFamily;
import application.scenario.Scenario;
import application.targets.*;
import application.viewer.glfw.SoQtWalkViewer;
import com.jogamp.opengl.GL2;

import application.objects.DouglasFir;
import jscenegraph.coin3d.fxviz.nodes.SoShadowDirectionalLight;
import jscenegraph.coin3d.fxviz.nodes.SoShadowGroup;
import jscenegraph.coin3d.fxviz.nodes.SoVolumetricShadowGroup;
import jscenegraph.coin3d.inventor.SbBSPTree;
import jscenegraph.coin3d.inventor.VRMLnodes.SoVRMLBillboard;
import jscenegraph.coin3d.inventor.lists.SbListInt;
import jscenegraph.coin3d.inventor.nodes.*;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderProgram;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderStateMatrixParameter;
import jscenegraph.database.inventor.*;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.actions.SoGetMatrixAction;
import jscenegraph.database.inventor.actions.SoSearchAction;
import jscenegraph.database.inventor.elements.SoProjectionMatrixElement;
import jscenegraph.database.inventor.elements.SoViewingMatrixElement;
import jscenegraph.database.inventor.misc.SoNotList;
import jscenegraph.database.inventor.nodes.*;
import jscenegraph.port.Ctx;
import jscenegraph.port.memorybuffer.MemoryBuffer;
import jsceneviewerglfw.inventor.qt.viewers.SoQtConstrainedViewer;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DMatrix3C;
import org.ode4j.ode.*;

import static application.MainGLFW.*;

/**
 * @author Yves Boyadjian
 *
 */
public class SceneGraphIndexedFaceSetShader implements SceneGraph {

	// Put it to false, if you do not want to save mri files
	private static final boolean SAVE_CHUNK_MRI = false;

	public static final boolean AIM = true;

	public static final float ZMAX = 4392.473f;

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
	
	public static final SbColor SKY_COLOR = new SbColor(0.3f, 0.3f, 0.65f);
	
	private static final float SKY_INTENSITY = 0.3f;
	
	private static final Color STONE = new Color(139,141,122); //http://www.colourlovers.com/color/8B8D7A/stone_gray

	private static final Color SNAIL_TRAIL = new Color(200,194,151); //https://www.colourlovers.com/color/C8C297/Snail_Trail

	private static final Color TRAIL = new Color(200,150,80); // Color of trail

	private static final float GRASS_LUMINOSITY = 0.6f;
	
	public static boolean FLY = false;
	
	static public final float CUBE_DEPTH = 2000;
	
	static final int LEVEL_OF_DETAIL = 600;
	
	static final int LEVEL_OF_DETAIL_SHADOW = 3000;
	
	static final int DOUGLAS_DISTANCE = 7000;
	
	static final int DOUGLAS_DISTANCE_SHADOW = 3000;
	
	static final boolean WITH_DOUGLAS = true;

	static public final float ORACLE_X = 314.9f;//317.56f;

	static public final float ORACLE_Y = 169.77f;//137.62f;

	static public final float ORACLE_Z = 1250.24f;//1248.5f;

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
	
	private final SoTranslation transl = new SoTranslation();
	
	private float zTranslation;

	private ChunkArray chunks;
	
	private float centerX;
	
	private float centerY;
	
	private SoShadowDirectionalLight[] sunLight = new SoShadowDirectionalLight[4];
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
	//SoNode shadowTree;
	SoNode chunkTree;
	SoCamera camera;

	SoTexture2 gigaTexture;
	
	float current_x;
	
	float current_y;
	
	float current_z;
	
	SoGroup douglasTreesF;
	SoGroup douglasTreesT;
	
	final SbVec3f douglasTreesRefPoint = new SbVec3f();
	final SbVec3f douglasTreesRefPoint2 = new SbVec3f();

	SoGroup douglasTreesST;
	SoGroup douglasTreesSF;
	
	final SbVec3f douglasTreesSRefPoint = new SbVec3f();
	final SbVec3f douglasTreesSRefPoint2 = new SbVec3f();

	SoTouchLODMaster master;
	//SoTouchLODMaster masterS;
	
	//final SbBSPTree sealsBSPTree = new SbBSPTree();
	
	final SbVec3f targetsRefPoint = new SbVec3f();

	final SbVec3f cameraDirection = new SbVec3f();

	final SbBSPTree treesBSPTree = new SbBSPTree();

	private Future<GeomsResult> geomsResultFuture;

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

	final SoText2 temporaryMessageDisplay = new SoText2();

	long temporaryMessageStopNanoTime;

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

	final List<Target> targetFamilies = new ArrayList<>();

	private final Set<String> shotTargets = new HashSet<>();
	private final List<Integer> shotTargetsIndices = new ArrayList<>();
	private final List<Integer> shotTargetsInstances = new ArrayList<>();

	final SoGroup collectibleGroup = new SoGroup();

	final List<Collectible> collectibleFamilies = new ArrayList<>();

	final SoSeparator planksSeparator = new SoSeparator();

	final List<SbVec3f> planksTranslations = new ArrayList<>();

	final List<SbRotation> planksRotations = new ArrayList<>();

	final List<DBox> planks = new ArrayList<>();

	final Set<Long> trails = new HashSet<>();

	final SbBSPTree trailsBSPTree = new SbBSPTree();

	private Future<Float> distanceFromTrailFuture;

	List<Long> sorted_trails;

	boolean trailsDirty = false;
	boolean CBRunning = false;

	final Collection<Runnable> idleCallbacks = new ArrayList<>();

	SoEnemies enemiesSeparator;

	//final SbBSPTree enemiesBSPTree = new SbBSPTree();

	final EnemyFamily enemyFamily = new EnemyFamily();

	Hero hero;
	//DBody heroBody;

	//DBody heroFeetBody;

	Scenario scenario;

	boolean searchForSea;

	final SbBSPTree beachBSPTree = new SbBSPTree();

	boolean wasDisplayingTrailDistance;

	boolean haveBoots;

    BootsFamily boots;

	boolean softShadows = true;

	float distanceFromSea = 1e6f;

	private Future<Float> distanceFromSeaFuture;

	private final ExecutorService es = Executors.newSingleThreadExecutor();

	private Future<SoSeparator> enemiesSeparatorFuture;

	private float overallContrast = 1.6f;

	private final Map<String,SbVec4f> objectives = new HashMap<>();

	private final Map<String,SbVec3f> threeDObjectives = new HashMap<>();

	private SoGroup objectivesGroup;

	private SoMarkerSet objectiveMarkerSet;

    public SceneGraphIndexedFaceSetShader(
			RasterProvider rwp,
			RasterProvider rep,
			int overlap,
			float zTranslation,
			int max_i,
			long[] trails,
			final JProgressBar progressBar) {
		super();
		this.rw = rwp.provide();
		this.overlap = overlap;
		this.zTranslation = zTranslation;
		this.max_i = max_i;
		
		int hImageW = rw.getHeight(); // 8112
		int wImageW = rw.getWidth(); // 8112

		h = Math.min(hImageW, MAX_J);// 8112
		full_island_w = 2*wImageW-I_START-overlap;// 13711

		if (max_i-1+I_START >= wImageW) {
			this.re = rep.provide();
			int hImageE = re.getHeight(); // 8112
			int wImageE = re.getWidth(); // 8112
			full_island_w = wImageW+wImageE-I_START-overlap;// 13711
		}
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
		
		if (re != null) {
			int nb = 0;
			for (int j = hImageW / 4; j < hImageW * 3 / 4; j++) {
				float zw = rw.getPixel(wImageW - 1, j, fArray)[0];
				float ze = re.getPixel(overlap - 1, j, fArray)[0];

				delta += (ze - zw);
				nb++;
			}
			delta /= nb;
		}
		
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
					if( Math.abs(ptV.getZ() - zWater) < 2.0f) {
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

		// ___________________________________ pre load enemies
		computeEnemies();

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

		SoCallback objectivesCallback = new SoCallback();
		objectivesCallback.setCallback(action->{
			if(action instanceof SoGLRenderAction) {
				SoGLRenderAction glRenderAction = (SoGLRenderAction)action;
				updateObjectives(glRenderAction.getViewportRegion().getViewportAspectRatio());
			}
		});

		sep.addChild(objectivesCallback);
	    
	    //sep.addChild(new SoAbort());
	    
	    environment.ambientColor.setValue(0, 0, 0);
	    environment.ambientIntensity.setValue(0);
	    environment.fogType.setValue(SoEnvironment.FogType.FOG);
	    environment.fogColor.setValue(new SbColor(SKY_COLOR.darker().operator_mul(overallContrast)));
	    environment.fogVisibility.setValue(4.5e4f/*5e4f*/);
	    
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
	    SoFragmentShader fragmentShaderSun = new SoFragmentShader();
	    
	    //vertexShader.sourceProgram.setValue("../../MountRainierIsland/application/src/shaders/phongShading.vert");

		String behindVertexPath = "../../MountRainierIsland/application/src/shaders/behind_vertex.glsl";
		String behindFragmentPath = "../../MountRainierIsland/application/src/shaders/behind_fragment.glsl";

		File behindVertexFile = new File(behindVertexPath);
		File behindFragmentFile = new File(behindFragmentPath);

		if(!behindVertexFile.exists()) {
			behindVertexPath = "../MountRainierIsland/application/src/shaders/behind_vertex.glsl";
			behindFragmentPath = "../MountRainierIsland/application/src/shaders/behind_fragment.glsl";
		}

		behindVertexFile = new File(behindVertexPath);
		behindFragmentFile = new File(behindFragmentPath);

		if(!behindVertexFile.exists()) {
			behindVertexPath = "application/src/shaders/behind_vertex.glsl";
			behindFragmentPath = "application/src/shaders/behind_fragment.glsl";
		}

	    vertexShaderSun.sourceProgram.setValue(behindVertexPath);
		fragmentShaderSun.sourceProgram.setValue(behindFragmentPath);

		final SoShaderStateMatrixParameter mvs = new SoShaderStateMatrixParameter();
		mvs.name.setValue("s4j_ModelViewMatrix");
		mvs.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.MODELVIEW);

		final SoShaderStateMatrixParameter prs = new SoShaderStateMatrixParameter();
		prs.name.setValue("s4j_ProjectionMatrix");
		prs.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.PROJECTION);

		final SoShaderStateMatrixParameter ns = new SoShaderStateMatrixParameter();
		ns.name.setValue("s4j_NormalMatrix");
		ns.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.MODELVIEW);
		ns.matrixTransform.setValue(SoShaderStateMatrixParameter.MatrixTransform.INVERSE_TRANSPOSE_3);

		vertexShaderSun.parameter.set1Value(vertexShaderSun.parameter.getNum(), mvs);
		vertexShaderSun.parameter.set1Value(vertexShaderSun.parameter.getNum(), prs);
		//vertexShaderSun.parameter.set1Value(vertexShaderSun.parameter.getNum(), ns);

	    programSun.shaderObject.set1Value(0, vertexShaderSun);
		programSun.shaderObject.set1Value(1, fragmentShaderSun);

	    sunSep.addChild(programSun);
	    
	    sunSep.addChild(sunView);
	    
	    sky = new SoDirectionalLight[4];
	    sky[0] = new SoNoSpecularDirectionalLight();
	    sky[0].color.setValue(SKY_COLOR);
	    sky[0].intensity.setValue(SKY_INTENSITY*overallContrast);
	    sky[0].direction.setValue(0, 1, -1);
	    sky[1] = new SoNoSpecularDirectionalLight();
	    sky[1].color.setValue(SKY_COLOR);
	    sky[1].intensity.setValue(SKY_INTENSITY*overallContrast);
	    sky[1].direction.setValue(0, -1, -1);
	    sky[2] = new SoNoSpecularDirectionalLight();
	    sky[2].color.setValue(SKY_COLOR);
	    sky[2].intensity.setValue(SKY_INTENSITY*overallContrast);
	    sky[2].direction.setValue(1, 0, -1);
	    sky[3] = new SoNoSpecularDirectionalLight();
	    sky[3].color.setValue(SKY_COLOR);
	    sky[3].intensity.setValue(SKY_INTENSITY*overallContrast);
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
	    sunLight[is] = new SoShadowDirectionalLight();
	    //sun = new SoDirectionalLight();
	    sunLight[is].color.setValue(new SbColor(SUN_COLOR.operator_mul(SUN_INTENSITY)));
	    
	    sunLight[is].maxShadowDistance.setValue(2e4f);
	    //sun[is].bboxCenter.setValue(10000, 0, 0);
	    sunLight[is].bboxSize.setValue(5000+is*3000, 5000+is*3000, 1000);

		if (is==0) {
			sunLight[0].intensity.setValue((softShadows ? 1.0f / 4.0f : 1.0f) * overallContrast);
		}
		else {
			sunLight[is].intensity.setValue(1.0F / 4.0f * overallContrast);
		}
	    
	    shadowGroup.addChild(sunLight[is]);
	    sunLight[is].enableNotify(false); // In order not to recompute shaders
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

		final Counter douglasLoadCount = new Counter();

	    master = new SoTouchLODMaster("viewer",douglasLoadCount);

		master.setLodFactor(LEVEL_OF_DETAIL);
	    
	    landSep.addChild(master);
	    
	    chunkTree = rc.getGroup(master,true);
	    landSep.addChild(chunkTree);
	    
		shadowGroup.addChild(landSep);

		// ___________________________________________________________ Douglas trees

		SoCallback douglasLoadCountCallback = new SoCallback();

		sep.addChild(douglasLoadCountCallback);

		douglasLoadCountCallback.setCallback((action)->{
			douglasLoadCount.reset();
		});


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
	    
		douglasTreesT = getDouglasTreesT(douglasTreesRefPoint,douglasTreesRefPoint2,douglas_distance_trunk,progressBar,douglasLoadCount);
		
	    SoSeparator douglasSepF = new SoSeparator();
	    douglasSepF.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
	    
		douglasSep.addChild(douglasTreesT);

		douglas_distance_foliage[0] = DOUGLAS_DISTANCE;
		
		douglasTreesF = getDouglasTreesF(douglasTreesRefPoint,douglasTreesRefPoint2,douglas_distance_foliage,true,progressBar,douglasLoadCount);
		
	    douglasSepF.addChild(douglasTexture);
	    
		douglasSepF.addChild(douglasTreesF);
		
		douglasSep.addChild(douglasSepF);
		
		if(WITH_DOUGLAS)
			shadowGroup.addChild(douglasSep);

		// ____________________________________________________________ Targets

		Target seals_ = new Seals(this);
		addTargetFamily(seals_);

		Target bigfoots_ = new BigFoots(this);
		addTargetFamily(bigfoots_);

		Target goats_ = new MountainGoats(this);
		addTargetFamily(goats_);

		Target marmots_ = new HoaryMarmots(this);
		addTargetFamily(marmots_);

		Target squirrels_ = new GroundSquirrels(this);
		addTargetFamily(squirrels_);

		Target spottedOwlFront_ = new Owls(this,53) {

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
		addTargetFamily(spottedOwlFront_);

		Target spottedOwlBack_ = new Owls(this,54) {

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
		addTargetFamily(spottedOwlBack_);

		Target barredOwlFront_ = new Owls(this,55) {

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
		addTargetFamily(barredOwlFront_);

		Target barredOwlBack_ = new Owls(this,56) {

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
		addTargetFamily(barredOwlBack_);

		for( Target targetFamily : targetFamilies) {

			SoTargets targetsSeparator = new SoTargets(targetFamily) {
				public void notify(SoNotList list) {
					super.notify(list);
				}
			};
			targetsSeparator.setReferencePoint(targetsRefPoint);
			targetsSeparator.setCameraDirection(cameraDirection);

			SoBaseColor targetsColor = new SoBaseColor();
			targetsColor.rgb.setValue(1,1,1);
			targetsSeparator.addChild(targetsColor);

			targetsSeparator.addChild(transl);
	    
			SoTexture2 targetTexture = new SoTexture2();

			String texturePath = targetFamily.getTexturePath();

			File textureFile = new File(texturePath);

			if(!textureFile.exists()) {
				texturePath = "application/"+texturePath;
			}

			targetTexture.filename.setValue(texturePath);

			targetsSeparator.addChild(targetTexture);
		
			final float[] vector = new float[3];

			final int nbTargets = targetFamily.getNbTargets();

			for (int index = 0; index < nbTargets; index++) {
				int instance = targetFamily.getInstance(index);

				final SbVec3f targetPosition = new SbVec3f();
				targetPosition.setValue(targetFamily.getTarget(index, vector));
				targetPosition.setZ(targetPosition.getZ() + 0.3f);

				targetsSeparator.addMember(targetPosition,instance);

				//SoTarget targetSeparator = new SoTarget(instance);
				//sealSeparator.renderCaching.setValue(SoSeparator.CacheEnabled.OFF);

				//SoTranslation targetTranslation = new SoTranslation();
				//targetTranslation.enableNotify(false); // Will change often


				//targetTranslation.translation.setValue(targetPosition);

				//targetSeparator.addChild(targetTranslation);

				//SoVRMLBillboard billboard = new SoVRMLBillboard();
				//billboard.axisOfRotation.setValue(0, 1, 0);

				//SoCube targetCube = new SoCube();
				//targetCube.height.setValue(targetFamily.getSize());
				//targetCube.width.setValue(targetFamily.getRatio() * targetCube.height.getValue());
				//targetCube.depth.setValue(0.1f);

				//billboard.addChild(targetCube);

				//targetSeparator.addChild(billboard);

				//targetsSeparator.addChild(targetSeparator);
			}

			targetsGroup.addChild(targetsSeparator);
		}
		shadowGroup.addChild(targetsGroup);

		// End targets

		// ___________________________________________________ Collectibles

		boots = new BootsFamily(this, 57);
		boots.setSpin(!haveBoots);
		collectibleFamilies.add(boots);

		for( Collectible collectibleFamily : collectibleFamilies) {

			SoCollectibles collectiblesSeparator = new SoCollectibles(collectibleFamily);

			collectiblesSeparator.setReferencePoint(targetsRefPoint);
			collectiblesSeparator.setCameraDirection(cameraDirection);

			collectiblesSeparator.addChild(transl);

//			SoNode collectibleNode = collectibleFamily.getNode();

			final float[] vector = new float[3];

			final int nbCollectibles = collectibleFamily.getNbCollectibles();

			for (int index = 0; index < nbCollectibles; index++) {
				int instance = collectibleFamily.getInstance(index);

				final SbVec3f collectiblePosition = new SbVec3f();
				collectiblePosition.setValue(collectibleFamily.getCollectible(index, vector));

				collectiblesSeparator.addMember(collectiblePosition,instance);

//				SoCollectible collectibleSeparator = new SoCollectible(instance);

//				SoTranslation collectibleTranslation = new SoTranslation();
//				collectibleTranslation.enableNotify(false); // Will change often

//				collectibleTranslation.translation.setValue(collectiblePosition);

//				collectibleSeparator.addChild(collectibleTranslation);

//				collectibleSeparator.addChild(collectibleNode);

//				collectiblesSeparator.addChild(collectibleSeparator);
			}

			collectibleGroup.addChild(collectiblesSeparator);
		}
		shadowGroup.addChild(collectibleGroup);

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

		// _______________________________________________ Enemies

		SoSeparator mainEnemySep = null;
		try {
			mainEnemySep = enemiesSeparatorFuture.get(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}

		shadowGroup.addChild(mainEnemySep);

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
	    
	    //masterS = new SoTouchLODMaster("shadow");

		//masterS.setLodFactor(LEVEL_OF_DETAIL_SHADOW);
	    
	    //shadowLandSep.addChild(masterS);
	    
	    //shadowTree = rc.getShadowGroup(masterS,false);
	    shadowLandSep.addChild(/*shadowTree*/chunkTree);
	    
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
	    
		douglasTreesST = getDouglasTreesT(douglasTreesSRefPoint,douglasTreesSRefPoint2, douglas_distance_shadow_trunk,progressBar,douglasLoadCount);
		
		douglasSepS.addChild(douglasTreesST);

		douglas_distance_shadow_foliage[0] = DOUGLAS_DISTANCE_SHADOW;
		
		douglasTreesSF = getDouglasTreesF(douglasTreesSRefPoint,douglasTreesSRefPoint2, douglas_distance_shadow_foliage, false,progressBar,douglasLoadCount);
		
		douglasSepS.addChild(douglasTreesSF);
		
		if(WITH_DOUGLAS)
			castingShadowScene.addChild(douglasSepS);

		oracleSwitchShadow.addChild(buildOracle(true));

		castingShadowScene.addChild(oracleSwitchShadow);

		castingShadowScene.addChild(planksSeparator);

		// slowdown too much the rendering
		//castingShadowScene.addChild(targetsGroup);

		castingShadowScene.addChild(collectibleGroup);

		sunLight[0].shadowMapScene.setValue(castingShadowScene);
		sunLight[1].shadowMapScene.setValue(castingShadowScene);
		sunLight[2].shadowMapScene.setValue(castingShadowScene);
		sunLight[3].shadowMapScene.setValue(castingShadowScene);
		
		//sep.ref();
		//forest = null; // for garbage collection : no, we need forest

		// ____________________________________________________________ Shader for screen display
		SoShaderProgram onScreenShaderProgram = new SoShaderProgram();

		SoVertexShader vertexShaderScreen = new SoVertexShader();

		vertexShaderScreen.sourceType.setValue(SoShaderObject.SourceType.GLSL_PROGRAM);
		vertexShaderScreen.sourceProgram.setValue(
				"#version 400 core\n" +
						"layout (location = 0) in vec3 s4j_Vertex;\n" +
						"layout (location = 1) in vec3 s4j_Normal;\n" +
						"layout (location = 2) in vec2 s4j_MultiTexCoord0;\n"+
						"layout (location = 3) in vec4 s4j_Color;\n" +
						"\n" +
						"uniform mat4 s4j_ModelViewMatrix;\n" +
						"uniform mat4 s4j_ProjectionMatrix;\n" +
						"uniform mat3 s4j_NormalMatrix;\n" +
						"uniform vec4 s4j_ColorUniform;\n" +
						"uniform bool s4j_PerVertexColor;\n" +
						"uniform vec4 s4j_FrontLightModelProduct_sceneColor;\n" +
						"\n" +
						"out vec2 texCoord;\n" +
						"out vec4 frontColor;\n" +
						"\n" +
						"void main(void)\n" +
						"{\n" +
						"    gl_Position = s4j_ProjectionMatrix * s4j_ModelViewMatrix * vec4(s4j_Vertex, 1.0);\n" +
						"    vec4 diffuCol;\n" +
						"    //diffuCol = s4j_FrontLightModelProduct_sceneColor;\n" +
						"    diffuCol = s4j_ColorUniform; if(s4j_PerVertexColor) diffuCol = s4j_Color;\n" +
						"\n" +
						"texCoord = s4j_MultiTexCoord0;\n"+
						"    frontColor = diffuCol;\n" +
						"}\n"
		);

		SoFragmentShader fragmentShaderScreen = new SoFragmentShader();

		fragmentShaderScreen.sourceType.setValue(SoShaderObject.SourceType.GLSL_PROGRAM);
		fragmentShaderScreen.sourceProgram.setValue("#version 400 core\n"+
				"struct FrontMaterial {\n" +
				"    vec4 specular;\n" +
				"    vec4 ambient;\n" +
				"    float shininess;\n" +
				"};\n" +
				"uniform FrontMaterial s4j_FrontMaterial;\n" +
				"layout(location = 0) out vec4 s4j_FragColor;\n"+
				"in vec2 texCoord;\n" +
				"in vec4 frontColor;\n" +
				"uniform sampler2D textureMap0;\n" +
				"\n" +
				"uniform int coin_texunit0_model;\n"+
				"uniform int coin_light_model;\n"+
				"\n" +
				"void main(void) {\n"+
				"vec4 mydiffuse = frontColor;\n" +
				"vec4 texcolor = (coin_texunit0_model != 0) ? texture2D(textureMap0, texCoord) : vec4(1.0);\n" +
				"if ( texcolor.r == 0.0 ) { mydiffuse.a = 0; discard;}\n" +
				"\n" +
				"  vec3 color;\n"+
				"color = mydiffuse.rgb * texcolor.rgb;\n"+
				"s4j_FragColor = vec4(color, mydiffuse.a);\n"+
				"}\n"
		);

		final SoShaderStateMatrixParameter smvs = new SoShaderStateMatrixParameter();
		smvs.name.setValue("s4j_ModelViewMatrix");
		smvs.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.MODELVIEW);

		final SoShaderStateMatrixParameter sprs = new SoShaderStateMatrixParameter();
		sprs.name.setValue("s4j_ProjectionMatrix");
		sprs.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.PROJECTION);

//		final SoShaderStateMatrixParameter ns = new SoShaderStateMatrixParameter();
//		ns.name.setValue("s4j_NormalMatrix");
//		ns.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.MODELVIEW);
//		ns.matrixTransform.setValue(SoShaderStateMatrixParameter.MatrixTransform.INVERSE_TRANSPOSE_3);

		vertexShaderScreen.parameter.set1Value(vertexShaderScreen.parameter.getNum(), smvs);
		vertexShaderScreen.parameter.set1Value(vertexShaderScreen.parameter.getNum(), sprs);
		//vertexShaderSun.parameter.set1Value(vertexShaderSun.parameter.getNum(), ns);

		onScreenShaderProgram.shaderObject.set1Value(0, vertexShaderScreen);
		onScreenShaderProgram.shaderObject.set1Value(1, fragmentShaderScreen);

		sep.addChild(onScreenShaderProgram);

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

		SoSeparator temporaryMessageSeparator = new SoSeparator();

		temporaryMessageSeparator.addChild(billboardMessageCamera);

		SoFont bigFont = new SoFont();
		bigFont.size.setValue(80.0f);

		temporaryMessageSeparator.addChild(bigFont);

		temporaryMessageSeparator.addChild(color);

		SoTranslation centerTextTransl = new SoTranslation();
		centerTextTransl.translation.setValue(0, -0.06f, 0);

		temporaryMessageSeparator.addChild(centerTextTransl);

		temporaryMessageDisplay.justification.setValue(SoText2.Justification.CENTER);

		temporaryMessageSeparator.addChild(temporaryMessageDisplay);

		sep.addChild(temporaryMessageSeparator);

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

		//______________________________________________________ Objectives

		addObjective("Oracle",getOracleCoordinates());

		SoSeparator objectivesSeparator = new SoSeparator();

		SoOrthographicCamera objectivesCamera = new SoOrthographicCamera();
		objectivesSeparator.addChild(objectivesCamera);

		SoBaseColor objectivesColor = new SoBaseColor();
		objectivesColor.rgb.setValue(1,0,1);
		objectivesSeparator.addChild(objectivesColor);

		objectiveMarkerSet = new SoMarkerSet();
		objectiveMarkerSet.markerIndex.setValue(SoMarkerSet.MarkerType.TRIANGLE_FILLED_9_9.getValue());

		vertexProperty = new SoVertexProperty();
		vertexProperty.vertex.setValue(new SbVec3f());

		objectiveMarkerSet.vertexProperty.setValue(vertexProperty);
		objectiveMarkerSet.ref();

		objectivesGroup = new SoGroup();
		objectivesGroup.enableNotify(false); // No need to notify

		objectivesSeparator.addChild(objectivesGroup);

		sep.addChild(objectivesSeparator);

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

		// ________________________________________ Life Bar

		SoSeparator lifeBarSeparator = new SoSeparator();

		SoOrthographicCamera offscreenLifeBarCamera = new SoOrthographicCamera();

		lifeBarSeparator.addChild(offscreenLifeBarCamera);

		SoCallback lifeBarCallback = new SoCallback();

		lifeBarSeparator.addChild(lifeBarCallback);

		SoIndexedFaceSet lifeBar = new SoIndexedFaceSet();

		float[] xyz = new float[4*3];

		SoVertexProperty lifeBarVertexProperty = new SoVertexProperty();

		lifeBarVertexProperty.vertex.setValues(0,xyz);

		lifeBar.vertexProperty.setValue(lifeBarVertexProperty);

		final float[] currentViewportAspectRatio = new float[1];
		final float[] currentHeroLife = new float[1];

		lifeBarCallback.setCallback(action->{
			if (action.isOfType(SoGLRenderAction.getClassTypeId())) {
				SoGLRenderAction glRenderAction = (SoGLRenderAction) action;
				float viewportAspectRatio = glRenderAction.getViewportRegion().getViewportAspectRatio();
				if(currentViewportAspectRatio[0] != viewportAspectRatio
				||
				currentHeroLife[0] != hero.life) {
					currentViewportAspectRatio[0] = viewportAspectRatio;
					currentHeroLife[0] = hero.life;

					if (viewportAspectRatio > 16.0/9.0) {
						viewportAspectRatio = 16.0f/9.0f;
					}

					xyz[0] = 0.9f*viewportAspectRatio;
					xyz[1] = -0.95f;

					xyz[3] = 0.95f*viewportAspectRatio;
					xyz[4] = -0.95f;

					xyz[6] = 0.95f*viewportAspectRatio;
					xyz[7] = -0.95f + 0.95f*hero.life;

					xyz[9] = 0.9f*viewportAspectRatio;
					xyz[10] = -0.95f + 0.95f*hero.life;

					lifeBarVertexProperty.vertex.setValues(0, xyz);

					lifeBar.vertexProperty.setValue(lifeBarVertexProperty);
				}
			}
		});

		int[] lifeBarIndices = new int[5];
		lifeBarIndices[1] = 1;
		lifeBarIndices[2] = 2;
		lifeBarIndices[3] = 3;
		lifeBarIndices[4] = -1;

		lifeBar.coordIndex.setValues(0, lifeBarIndices);

		lifeBarSeparator.addChild(lifeBar);

		sep.addChild(lifeBarSeparator);

		onContrastChange();
	}

	private SoNode buildOracle(boolean shadow) {

		SoPill oracleSeparator = new SoPill(-1);//SoSeparator();

		oracleSeparator.position.translation.setValue(ORACLE_X,ORACLE_Y,ORACLE_Z-zTranslation - 0.74f);
		oracleSeparator.material.diffuseColor.setValue(1,0,0);

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
		if(!transl.translation.getValue().isNull()) {
			throw new IllegalStateException();
		}
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
	    
		
		sunLight[1].direction.setValue(r1.multVec(dir));
		sunLight[2].direction.setValue(r2.multVec(dir));
		sunLight[0].direction.setValue(r3.multVec(dir));
		sunLight[3].direction.setValue(r4.multVec(dir));
		
		boolean wasEnabled = sunTransl.translation.enableNotify(false); // In order not to invalidate shaders
		sunTransl.translation.setValue(sunPosition.operator_mul(SUN_FAKE_DISTANCE));
		sunTransl.translation.enableNotify(wasEnabled);
		//inverseSunTransl.translation.setValue(sunPosition.operator_mul(SUN_FAKE_DISTANCE).operator_minus());
		
		float sunElevationAngle = (float)Math.atan2(sunPosition.getZ(), Math.sqrt(Math.pow(sunPosition.getX(),2.0f)+Math.pow(sunPosition.getY(),2.0f)));
		float sinus = (float)Math.sin(sunElevationAngle);
		for(int is=0;is<4;is++) {	    		    
		    sunLight[is].maxShadowDistance.setValue(1e4f + (1 - sinus)*1e5f);
		    sunLight[is].bboxSize.setValue(5000+is*3000 + (1 - sinus)*10000, 5000+is*3000 + (1 - sinus)*10000, 2000);
		    sunLight[is].nearBboxSize.setValue(80+is*10 + (1-sinus)*100,80+is*10+(1-sinus)*100,300);
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

		if (geomsResultFuture == null) {
			final float curx = current_x;
			final float cury = current_y;
			final float curz = current_z;
			geomsResultFuture = es.submit(()->{
				return updateNearGeoms(curx,cury,curz);
			});
		}
		if (geomsResultFuture.isDone()) {

			GeomsResult geomsResult = null;//updateNearGeoms(current_x, current_y, current_z);
			try {
				geomsResult = geomsResultFuture.get();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
			if (geomsResult != null) {
				consumeGeomsResult(geomsResult);
			}
			geomsResultFuture = null;
		}

		//setBBoxCenter();
		return current_z;
	}

    public EnemyFamily getEnemies() {
		return enemyFamily;
    }

	public void loadEnemiesKills(Properties saveGameProperties) {
		String killedEnemies = saveGameProperties.getProperty(KILLED_ENEMIES);
		if (killedEnemies == null || killedEnemies.isBlank()) {
			return;
		}
		String[] enemiesKilledInstances = killedEnemies.split(",");
		for(String enemyKilledInstance : enemiesKilledInstances) {
			if (enemyKilledInstance.isBlank()) {
				continue;
			}
			int instance = Integer.valueOf(enemyKilledInstance);
			enemyFamily.kill(instance);
		}
	}

	public Hero getHero() {
		return hero;
	}

	private static class GeomsResult {
		private final Set<Integer> geomsToAdd = new HashSet<>();
		private final Set<Integer> geomsToRemove = new HashSet<>();
	}
	public GeomsResult updateNearGeoms(final float curx, final float cury, final float curz) {

		if( null == forest) {
			return null;
		}
		if( null == space) {
			return null;
		}

		GeomsResult geomsResult = new GeomsResult();

		SbVec3f trans = transl.translation.getValue();
		dummy.setValue(curx - trans.getX(), cury - trans.getY(), curz - trans.getZ());

		SbSphere sphere = new SbSphere(dummy,40);
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
//				float xd = forest.getX(tree_index);
//				float yd = forest.getY(tree_index);
//				float zd = forest.getZ(tree_index);
//				float height = forest.getHeight(tree_index);
//				float width = height * trunk_width_coef;
//
//				DGeom box = OdeHelper.createCapsule(space, width, height);
//				box.setPosition(xd + trans.getX(), yd + trans.getY(), zd + trans.getZ()+height/2);
//				geoms.put(tree_index,box);
				geomsResult.geomsToAdd.add(tree_index);
			}
			nearGeoms.add(tree_index);
		}
		for( Integer entry : geoms.keySet()) {
			if(!nearGeoms.contains(entry)) {
//				DGeom g = (DGeom)entry.getValue();
//				g.destroy();
//				//space.remove(g);
//				geoms.remove(entry.getKey());
				geomsResult.geomsToRemove.add(entry);
			}
		}

		return geomsResult;
	}

	private void consumeGeomsResult(GeomsResult result) {
		float trunk_width_coef = DouglasFir.trunk_diameter_angle_degree*(float)Math.PI/180.0f;
		SbVec3f trans = transl.translation.getValue();
		for( Integer entry : result.geomsToAdd) {
			int tree_index = entry;
			float xd = forest.getX(tree_index);
			float yd = forest.getY(tree_index);
			float zd = forest.getZ(tree_index);
			float height = forest.getHeight(tree_index);
			float width = height * trunk_width_coef;

			DGeom box = OdeHelper.createCapsule(space, width, height);
			box.setPosition(xd + trans.getX(), yd + trans.getY(), zd + trans.getZ()+height/2);
			geoms.put(tree_index,box);
		}

		for( Integer entry : result.geomsToRemove) {
//			if(!nearGeoms.contains(entry.getKey())) {
				DGeom g = (DGeom)geoms.get(entry);
				g.destroy();
				//space.remove(g);
				geoms.remove(entry);
//			}
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
		    sunLight[is].bboxCenter.setValue(
		    		current_x+2000*world_camera_direction.getX(),
		    		current_y+2000*world_camera_direction.getY(), /*current_z*/getGroundZ());
		    sunLight[is].nearBboxCenter.setValue(
		    		current_x+40*world_camera_direction.getX(),
					current_y+40*world_camera_direction.getY(),
					getGroundZ()
			);
		}		
		
		final float xTransl = - transl.translation.getValue().getX();
		final float yTransl = - transl.translation.getValue().getY();
		final float zTransl = - transl.translation.getValue().getZ();
		
		float trees_x = current_x + xTransl+douglas_distance_foliage[0]*0.4f/*3000*/*world_camera_direction.getX();
		float trees_y = current_y + yTransl+douglas_distance_foliage[0]*0.4f/*3000*/*world_camera_direction.getY();
		
		douglasTreesRefPoint.setValue(trees_x,trees_y,current_z + zTransl);
		douglasTreesRefPoint2.setValue(current_x + xTransl,current_y + yTransl,current_z + zTransl);
		
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
			if (nodeForX instanceof SoLODGroup) {
				SoLODGroup sepForX = (SoLODGroup) nodeForX;
				sepForX.referencePoint.setValue(trees_x, trees_y, current_z + zTransl);
//			for( SoNode node : sepForX.getChildren()) {
//			SoLODIndexedFaceSet lifs = (SoLODIndexedFaceSet) node;
//			lifs.referencePoint.setValue(
//					/*current_x + xTransl+3000*world_camera_direction.getX()*/trees_x,
//					/*current_y + yTransl+3000*world_camera_direction.getY()*/trees_y,
//					current_z + zTransl);
//			}
			}
		}
				
		float treesS_x = current_x + xTransl+douglas_distance_shadow_foliage[0]*0.3f/*1000*/*world_camera_direction.getX();
		float treesS_y = current_y + yTransl+douglas_distance_shadow_foliage[0]*0.3f/*1000*/*world_camera_direction.getY();
		
		douglasTreesSRefPoint.setValue(treesS_x,treesS_y,current_z + zTransl);
		douglasTreesSRefPoint2.setValue(current_x + xTransl,current_y + yTransl,current_z + zTransl);

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
			if (nodeForX instanceof SoLODGroup) {
				SoLODGroup sepForX = (SoLODGroup) nodeForX;
				sepForX.referencePoint.setValue(treesS_x, treesS_y, current_z + zTransl);
//			for( SoNode node : sepForX.getChildren()) {
//			SoLODIndexedFaceSet lifs = (SoLODIndexedFaceSet) node;
//			lifs.referencePoint.setValue(
//					/*current_x + xTransl+1000*world_camera_direction.getX()*/treesS_x, 
//					/*current_y + yTransl+1000*world_camera_direction.getY()*/treesS_y, 
//					current_z + zTransl);
//			}
			}
		}
		
		float targets_x = current_x + xTransl/*+SoTarget.MAX_VIEW_DISTANCE*world_camera_direction.getX()*0.8f*/;
		float targets_y = current_y + yTransl/*+SoTarget.MAX_VIEW_DISTANCE*world_camera_direction.getY()*0.8f*/;

		cameraDirection.setValue(world_camera_direction);
		cameraDirection.setZ(0);
		
		targetsRefPoint.setValue(targets_x,targets_y,current_z + zTransl);
	}

	public int[] getIndexes(float x, float y, int[] indices) {
		return getIndexes(x,y,indices,false);
	}
	public int[] getIndexes(float x, float y, int[] indices, boolean noTransl) {
		float ifloat = (x - (noTransl ? 0 : transl.translation.getValue().getX()))/delta_x;
		float jfloat = (delta_y*(h-1) -(y - (noTransl ? 0 : transl.translation.getValue().getY()) - jstart * delta_y))/delta_y;
		
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

	public float getIFloatNoTransl(float x) {
		float ifloat = x/delta_x;
		return ifloat;
	}

	public float getJFloatNoTransl(float y) {
		float jfloat = (delta_y*(h-1) -(y - jstart * delta_y))/delta_y;
		return jfloat;
	}

	public float getInternalZ(float x, float y, int[] indices) {
		return getInternalZ(x,y,indices,false);
	}

	public float getInternalZ(float x, float y, int[] indices, boolean noTransl) {
		
		float ifloat = noTransl? getIFloatNoTransl(x) : getIFloat(x);// (x - transl.translation.getValue().getX())/delta_x;
		float jfloat = noTransl ? getJFloatNoTransl(y) : getJFloat(y);// (delta_y*(h-1) -(y - transl.translation.getValue().getY() - jstart * delta_y))/delta_y;
		
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
		if(getIndexes(x,y,indices,noTransl) == null) {
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
		int[] indices = getIndexes(x, y, null,true);
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

		for( int i=0; i< forest.getNumIndexDouglas()/*NB_DOUGLAS_SEEDS*/; i++) {
			if( 0 == i%999 ) {
				progressBar.setValue((int)(progressBarInitialValue + (MAX_PROGRESS - progressBarInitialValue)*(long)i/forest.getNumIndexDouglas()));
			}

			float x = forest.getX(i);

			if(Float.isNaN(x)) {
				continue;
			}
			float y = forest.getY(i);
			float z = forest.getZ(i);

			treePoint.setValue(x,y,z);
			treesBSPTree.addPoint(treePoint,i);
		}
		progressBar.setValue(MAX_PROGRESS);
	}
		
	SoGroup getDouglasTreesT(SbVec3f refPoint,SbVec3f refPoint2, final float[] distance,final JProgressBar progressBar,final Counter douglasLoadCount) {
		
		if( forest == null) {
			computeDouglas(progressBar);
		}
		
		return forest.getDouglasTreesT(refPoint,refPoint2, distance,douglasLoadCount);
	}	
	
	SoGroup getDouglasTreesF(SbVec3f refPoint,SbVec3f refPoint2, final float[] distance, boolean withColors,final JProgressBar progressBar,final Counter douglasLoadCount) {
		
		if( forest == null) {
			computeDouglas(progressBar);
		}
		
		return forest.getDouglasTreesF(refPoint, refPoint2, distance, withColors,douglasLoadCount);
	}	
	
	static Random random = new Random(42);
	
	@Override
	public void preDestroy() {
		
		shadowGroup.removeAllChildren();
		
		for(int i=0; i<4; i++) {
			sunLight[i].on.setValue(false);
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
	    //masterS.setCamera(camera);
		RecursiveChunk.setCamera(chunkTree, camera);
		//RecursiveChunk.setCamera(shadowTree, camera);
	}

	@Override
	public void idle() {
		setBBoxCenter();
		hideOracleIfTooFar();
		setNearDistance();

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

		if(temporaryMessageStopNanoTime != 0) {
			if(System.nanoTime() > temporaryMessageStopNanoTime) {
				temporaryMessageStopNanoTime = 0;
				temporaryMessageDisplay.string.setNum(0);
			}
		}
		if (distanceFromSeaFuture == null) {
			final float curx = current_x;
			final float cury = current_y;
			final float curz = current_z;
			distanceFromSeaFuture = es.submit(()-> {
				return computeDistanceFromBeach(curx,cury,curz);
			});
		}
		if (distanceFromSeaFuture.isDone()) {
			try {
				distanceFromSea = distanceFromSeaFuture.get();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
			distanceFromSeaFuture = null;
		}

		//distanceFromSea = computeDistanceFromBeach();



		//updateObjectives();
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
			sunLight[i].enableNotify(true);
		}
	}

	public void disableNotifySun() {
		for( int i=0;i<4;i++) {
			sunLight[i].enableNotify(false);
		}
	}

	public float getLevelOfDetail() {
		return LEVEL_OF_DETAIL / master.getLodFactor();
	}

	public float getLevelOfDetailShadow() {
		return 0.05f;
	}

	public void setLevelOfDetail(float levelOfDetail) {
		float lodFactor = LEVEL_OF_DETAIL / levelOfDetail;
		master.setLodFactor(lodFactor);
	}

	public void setLevelOfDetailShadow(float levelOfDetailShadow) {
		float lodFactor = LEVEL_OF_DETAIL_SHADOW / levelOfDetailShadow;
		//masterS.setLodFactor(lodFactor);
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

	public void addTargetFamily(Target target) {
		targetFamilies.add(target);
	}

	public void shootTarget(Target t, int instance) {
		registerShot(targetFamilies.indexOf(t),instance);
		doShootTarget(t,instance);
	}

	public void registerShot(int index, int instance) {
		shotTargetsIndices.add(index);
		shotTargetsInstances.add(instance);
	}

	public boolean isShot(Target t, int instance) {
		int index = targetFamilies.indexOf(t);
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

		updateTargetDisplay();
	}

	private void updateTargetDisplay() {

		int shotTargetSize = shotTargets.size();
		if (haveBoots) {
			shotTargetSize++;
		}

		String[] targets = new String[shotTargetSize];
		int i=0;
		for(String name : shotTargets) {
			targets[i] = name;
			i++;
		}
		if(haveBoots) {
			targets[i] = "Boots";
		}
		targetDisplay.string.setValues(0,targets);
		targetDisplay.string.setNum(shotTargetSize);
	}

	public void loadShots(Properties saveGameProperties) {
		int i=0;
		final String keyIndex = "targetShotIndex";
		final String keyInstance = "targetShotInstance";
		while(saveGameProperties.containsKey(keyIndex+i)) {
			int index = Integer.valueOf(saveGameProperties.getProperty(keyIndex+i));
			int instance = Integer.valueOf(saveGameProperties.getProperty(keyInstance+i));

			SoTargets targetsNode = (SoTargets) targetsGroup.getChild(index);
			Target targetFamily = targetsNode.getTarget();
			targetFamily.setShot(instance);

			SoTarget target = targetsNode.getTargetChildFromInstance(instance);

			if (target != null) {

				SoVRMLBillboard billboard = (SoVRMLBillboard) target.getChild(1);

				SoMaterial c = new SoMaterial();
				c.diffuseColor.setValue(1, 0, 0);
				//billboard.enableNotify(false);
				billboard.insertChild(c, 0);
				//billboard.enableNotify(true);

				targetFamilies.get(index).setGroup(billboard, instance);
			}
			shootTarget(targetFamilies.get(index),instance);
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

	public void setHero(Hero hero) {
		this.hero = hero;
		//heroBody = hero.body;
	}


	public void stopBody() {
		if(null != hero.body) {
			hero.body.setLinearVel(0,0,0);
		}
		if(null != hero.ballBody) {
			hero.ballBody.setLinearVel(0,0,0);
			hero.ballBody.setAngularVel(0,0,0);
		}
	}

	public void setHeroPosition(float x, float y, float z) {
		camera.position.setValue(x,y,z - SCENE_POSITION.getZ());
		camera.orientation.setValue(new SbVec3f(0, 1, 0), -(float) Math.PI / 2.0f);

		SbVec3f cameraPositionValue = camera.position.getValue();

		final float above_ground = //4.5f; // Necessary for planks
				0.2f; // Necessary when respawning on water

		hero.body.setPosition(cameraPositionValue.getX(), cameraPositionValue.getY(), cameraPositionValue.getZ() - /*1.75f / 2*/0.4f + 0.13f + above_ground);
		hero.body.setLinearVel(0,0,0);

		hero.ballBody.setPosition(
				hero.body.getPosition().get0()/*cameraPositionValue.getX()*/,
				hero.body.getPosition().get1()/*cameraPositionValue.getY()*/,
				hero.body.getPosition().get2() /*cameraPositionValue.getZ() - 0.4f + 0.13f + above_ground*/ - 1.75f+ 2*0.4f);

		hero.ballBody.setLinearVel(0,0,0);
		hero.ballBody.setAngularVel(0,0,0);
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
			Target t = targetFamilies.get(indice);
			t.resurrect(instance);
		}

		shotTargetsIndices.clear();
		shotTargetsInstances.clear();
		shotTargets.clear();

		updateTargetDisplay();
	}

	public float getDistanceFromOracle() {
		return (float)Math.sqrt(
				(current_x - ORACLE_X)*(current_x - ORACLE_X) +
						(current_y - ORACLE_Y)*(current_y - ORACLE_Y) +
						(current_z - ORACLE_Z + zTranslation + 0.74f)*(current_z - ORACLE_Z + zTranslation + 0.74f)
		);
	}

	public SbVec3f getOracleCoordinates() {
		return new SbVec3f(ORACLE_X,ORACLE_Y,ORACLE_Z);
	}

	float getSlope(float x, float y) {
		float height1 = getInternalZ(x+1,y,new int[4]);
		float height2 = getInternalZ(x-1,y,new int[4]);
		float height3 = getInternalZ(x,y-1,new int[4]);
		float height4 = getInternalZ(x,y+1,new int[4]);
		float heightMin = Math.min(Math.min(Math.min(height1,height2),height3),height4);
		float heightMax = Math.max(Math.max(Math.max(height1,height2),height3),height4);
		return (heightMax - heightMin)/2;
	}

	public float getDistanceFromTrail(final float curx, final float cury, final float curz) {
		SbVec3f trans = transl.translation.getValue();

		SbVec3f hero = new SbVec3f(curx - trans.getX(),cury - trans.getY(),curz - trans.getZ());
		SbBSPTree.Filter filter = new SbBSPTree.Filter() {
			@Override
			public boolean filter(SbVec3f point) {
				double distXY = Math.sqrt((point.getX() - hero.getX())*(point.getX() - hero.getX()) +
						(point.getY() - hero.getY())*(point.getY() - hero.getY()));
				double distZ = point.getZ() - hero.getZ();
				if (distXY == 0) {
					return true;
				}
				for (int i=0; i< 10; i++) {
					float alpha = (float)i/9.0f; // 0 to 1
					float beta = (9.0f-(float)i)/9.0f; // 1 to 0
					float x = alpha*hero.getX() + beta*point.getX();
					float y = alpha*hero.getY() + beta*point.getY();
					float slope = getSlope(x + trans.getX(),y + trans.getY());
					if (slope > 0.7) {
						return false;
					}
				}
				if(distZ / distXY > 0.7) {
					return false;
				}
				return true;
			}
		};
		int closest = trailsBSPTree.findClosest(hero/*,filter*/);

		float distance = 30000;

		if( -1 != closest ) {
			SbVec3f closestPoint = trailsBSPTree.getPoint(closest);
			distance = (float)Math.sqrt(
					(curx - trans.getX() - closestPoint.getX())*(curx - trans.getX() - closestPoint.getX()) +
							(cury - trans.getY() - closestPoint.getY())*(cury - trans.getY() - closestPoint.getY()) +
							(curz - trans.getZ() - closestPoint.getZ())*(curz - trans.getZ() - closestPoint.getZ())
			);
		}
		return distance;
	}

	public float getDistanceFromBeach() {
		return distanceFromSea;
	}

	final SbListInt tmparray = new SbListInt();

	private float computeDistanceFromBeach(final float curx, final float cury, final float curz) {
		SbVec3f trans = transl.translation.getValue();

		SbVec3f hero = new SbVec3f(curx - trans.getX(),cury - trans.getY(),curz - trans.getZ());
		int closest = beachBSPTree.findClosest(hero,tmparray);

		float distance = Float.MAX_VALUE;

		if(-1 != closest) {
			SbVec3f closestPoint = beachBSPTree.getPoint(closest);
			distance = (float)Math.sqrt(
					(curx - trans.getX() - closestPoint.getX())*(curx - trans.getX() - closestPoint.getX()) +
							(cury - trans.getY() - closestPoint.getY())*(cury - trans.getY() - closestPoint.getY()) +
							(curz - trans.getZ() - closestPoint.getZ())*(curz - trans.getZ() - closestPoint.getZ())
			);
		}
		return distance;
	}

	float distanceFromTrail;

	public void displayObjectives(SoQtWalkViewer viewer) {

		String string1 = "Oracle distance: "+(int)getDistanceFromOracle()+ " m";

		if (distanceFromTrailFuture == null) {
			distanceFromTrailFuture = es.submit(()->{
				return getDistanceFromTrail(current_x,current_y,current_z);
			});
		}

		if(distanceFromTrailFuture.isDone()) {
			try {
				distanceFromTrail = distanceFromTrailFuture.get();//getDistanceFromTrail(current_x, current_y, current_z);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
			distanceFromTrailFuture = null;
		}

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
			//float distanceFromSea = getDistanceFromBeach();
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

	public void displayTemporaryMessage(String[] message, float durationSeconds) {
		temporaryMessageDisplay.string.setValues(0,message);
		temporaryMessageDisplay.string.setNum(message.length);
		temporaryMessageStopNanoTime = (long)(System.nanoTime() + durationSeconds*1.0e9);
	}

	public float getNearestCollectibleDistance() {
		float nearestCollectibleDistance = 99;
		for( Collectible collectible : collectibleFamilies) {
			nearestCollectibleDistance = Math.min(nearestCollectibleDistance,collectible.getGraphicObject().getNearestCollectibleDistance());
		}
		//nearestCollectibleDistance = Math.min(nearestCollectibleDistance,enemiesSeparator.getNearestEnemyDistance());

		return nearestCollectibleDistance;
	}

	public float getNearestEnemyDistance() {
		return enemiesSeparator.getNearestEnemyDistance();
	}

	public float getNearestObjectDistance() {
		return Math.min(getNearestCollectibleDistance(),getNearestEnemyDistance());
	}

	private void setNearDistance() {
		float nearestCollectible = getNearestCollectibleDistance();
		float nearestEnemy = getNearestEnemyDistance();
		float nearest = Math.min(nearestCollectible,nearestEnemy);
		float minViewDistance = Math.min(nearest/2.0f, MINIMUM_VIEW_DISTANCE);
		minViewDistance = Math.max(0.5f, minViewDistance);
		camera.nearDistance.setValue(minViewDistance);
		//System.out.println(minViewDistance);

		if (nearestCollectible < 1.4) {
			if (setBoots(true)) {
				String[] message = new String[2];
				message[0] = "Got the boots !";
				message[1] = "Now you can climb more easily";
                displayTemporaryMessage(message,8.0f);
            }
		}
	}

	public boolean haveBoots() {
		return haveBoots;
	}

    public float getMU() {
        return haveBoots ? CONTACT_SURFACE_MU_BOOTS : CONTACT_SURFACE_MU_BAREFOOT;
    }

	private double currentPos = 0;
	private double currentSpeed = 1;
	private double currentTargetTime = 0;
	private double lastRandom = 0;
	private Random targetSpeedRandom = new Random();

	public void updateTargetPositions(double dt) {
//		currentTargetTime += dt;
//		if(currentTargetTime - lastRandom > 1) {
//			lastRandom = currentTargetTime;
//			currentSpeed = targetSpeedRandom.nextDouble();
//		}
//
//		currentPos += currentSpeed * dt;
//		final float[] vector = new float[3];
//
//		float sinus = (float)Math.sin(/*currentPos*/currentTargetTime);
//		float cosinus = (float)Math.cos(/*currentPos*/currentTargetTime);
//
//		for(Target targetFamily : targetFamilies) {
//			SoTargets graphicObject = targetFamily.getGraphicObject();
//			for(SoTarget child : graphicObject.getNearChildren()) {
//				int instance = child.getInstance();
//				int index = targetFamily.indexOfInstance(instance);
//				targetFamily.getTarget(index, vector);
//				vector[0]+=sinus;
//				vector[1]+=cosinus;
//				vector[2]+=0.3;
//				SoTranslation transl = (SoTranslation)child.getChild(0);
//				transl.translation.setValue(vector);
//			}
//		}
	}

    /**
     *
     * @param bootsFlag
     * @return true if change
     */
	public boolean setBoots(boolean bootsFlag) {
        if (bootsFlag != haveBoots) {
            haveBoots = bootsFlag;
            updateTargetDisplay();
            boots.setSpin(!haveBoots);
            return true;
        }
        return false;
	}

	public void setSoftShadows(boolean soft) {
		softShadows = soft;

		boolean wasNotify = sunLight[0].isNotifyEnabled();

		sunLight[0].enableNotify(true); // In order not to recompute shaders
		sunLight[1].enableNotify(true); // In order not to recompute shaders
		sunLight[2].enableNotify(true); // In order not to recompute shaders
		sunLight[3].enableNotify(true); // In order not to recompute shaders

		sunLight[0].intensity.setValue((softShadows ? 1.0f/4.0f : 1.0f) * overallContrast);
		sunLight[1].on.setValue(softShadows);
		sunLight[2].on.setValue(softShadows);
		sunLight[3].on.setValue(softShadows);

		sunLight[0].enableNotify(wasNotify); // In order not to recompute shaders
		sunLight[1].enableNotify(wasNotify); // In order not to recompute shaders
		sunLight[2].enableNotify(wasNotify); // In order not to recompute shaders
		sunLight[3].enableNotify(wasNotify); // In order not to recompute shaders
	}

	float getRandomX(Random randomPlacementTrees) {
		SbBox3f sceneBox = getChunks().getSceneBoxFullIsland();
		float xMin = sceneBox.getBounds()[0];
		float xMax = sceneBox.getBounds()[3];
		return xMin + (xMax - xMin) * randomPlacementTrees.nextFloat();
	}

	float getRandomY(Random randomPlacementTrees) {
		SbBox3f sceneBox = getChunks().getSceneBoxFullIsland();
		float yMin = sceneBox.getBounds()[1];
		float yMax = sceneBox.getBounds()[4];
		return yMin + (yMax - yMin) * randomPlacementTrees.nextFloat();
	}

	private void computeEnemies() {
		enemiesSeparatorFuture = es.submit(()->{

			final int NB_ENEMIES = 100000;
			final int ENEMIES_SEED = 58;

			Random randomPlacementEnemies = new Random(ENEMIES_SEED);

			final int[] indices = new int[4];

			final float zWater =  - 150 + getzTranslation() - CUBE_DEPTH /2;

			float[] xyz = new float[3];
			int start;

			enemyFamily.enemiesInitialCoords.setNum(NB_ENEMIES);

			for (int i=0; i<NB_ENEMIES; i++) {
				float x = getRandomX(randomPlacementEnemies);
				float y = getRandomY(randomPlacementEnemies);
				float z = getInternalZ(x, y, indices,true) + getzTranslation();

				boolean isAboveWater = z > zWater;

				if (isAboveWater) {
					xyz[0] = x;
					xyz[1] = y;
					xyz[2] = z + 1.75f/2 - 0.03f;
					start = enemyFamily.nbEnemies;
					enemyFamily.enemiesInitialCoords.setValues(start,xyz);
					enemyFamily.enemiesInstances.add(i);
					enemyFamily.nbEnemies++;
				}
			}
			System.out.println("Enemies: "+enemyFamily.nbEnemies);
			enemyFamily.enemiesInitialCoords.setNum(enemyFamily.nbEnemies);

			enemiesSeparator = new SoEnemies(enemyFamily,this);
			enemiesSeparator.setReferencePoint(targetsRefPoint);

			//final int nbCollectibles = collectibleFamily.getNbCollectibles();

			final float[] vector = new float[3];

			for (int index = 0; index < enemyFamily.nbEnemies; index++) {
				int instance = enemyFamily.enemiesInstances.get(index);

				final SbVec3f enemyPosition = new SbVec3f();
				enemyPosition.setValue(enemyFamily.getEnemy(index, vector));

				enemiesSeparator.addMember(enemyPosition, instance);
			}

			SoSeparator mainEnemySep = new SoSeparator();
			mainEnemySep.addChild(transl);

			mainEnemySep.addChild(enemiesSeparator);

			return mainEnemySep;
		});
	}

	public void newGame(SoQtWalkViewer viewer) {
		if(viewer.isTimeStop()) {
			viewer.toggleTimeStop();
		}
		setHeroPosition(Hero.STARTING_X,Hero.STARTING_Y,Hero.STARTING_Z);
		getHero().life = 1.0f;
		resurrectTheAnimals();
		resetScenario(viewer);
		SwingUtilities.invokeLater(()->setBoots(false));
		if(viewer.isFlying()) {
			viewer.toggleFly();
		}
		viewer.setAllowToggleFly(false);

		String[] message = new String[2];
		message[0] = "Go to the oracle."; message[1] = "He is on the right on the path";
		displayTemporaryMessage(message,30);
	}

	public void setContrast(float contrast) {
		if (overallContrast != contrast) {
			overallContrast = contrast;
			onContrastChange();
		}
	}

	public float getOverallContrast() {
		return overallContrast;
	}

	private void onContrastChange() {
		for(int is=0;is<4;is++) {
			boolean wasNotified = sunLight[is].enableNotify(true);
			if (is == 0) {
				sunLight[0].intensity.setValue((softShadows ? 1.0f / 4.0f : 1.0f) * overallContrast);
			}
			else {
				sunLight[is].intensity.setValue(1.0F / 4.0f * overallContrast);
			}
			sunLight[is].enableNotify(wasNotified); // In order not to recompute shaders
		}
		sky[0].intensity.setValue(SKY_INTENSITY*overallContrast);
		sky[1].intensity.setValue(SKY_INTENSITY*overallContrast);
		sky[2].intensity.setValue(SKY_INTENSITY*overallContrast);
		sky[3].intensity.setValue(SKY_INTENSITY*overallContrast);
	}
	public static SbViewVolume getViewVolume(SbViewVolume view, SoPerspectiveCamera pcam, float useAspectRatio) {
		view.constructor();

		float       camAspect = (useAspectRatio != 0.0 ? useAspectRatio :
				pcam.aspectRatio.getValue());

		// Set up the perspective camera.
		view.perspective(pcam.heightAngle.getValue(), camAspect,
				pcam.nearDistance.getValue(), pcam.farDistance.getValue());

		// Note that these move the camera rather than moving objects
		// relative to the camera.
		view.rotateCamera(pcam.orientation.getValue());
		//view.translateCamera(pcam.position.getValue());
		return view;
	}

	private void updateObjectives(float viewportAspectRatio) {

		for(Map.Entry<String,SbVec3f> threeDObjective : threeDObjectives.entrySet()) {
			String name = threeDObjective.getKey();
			SbVec3f coordinates = threeDObjective.getValue();
			SbVec3f direction = new SbVec3f(-(current_x - coordinates.getX()),
					-(current_y - coordinates.getY()),
					-(current_z - coordinates.getZ() + zTranslation + 0.74f));

			SbViewVolume cameraViewVolume = new SbViewVolume();//camera.getViewVolume();
			getViewVolume(cameraViewVolume,(SoPerspectiveCamera) camera,0);
			final SbVec3f screenCoords = new SbVec3f();
			cameraViewVolume.projectToScreen(direction,screenCoords);

			SbVec4f direction4D = new SbVec4fSingle();
			direction4D.setValue(direction.getX(),direction.getY(),direction.getZ(),1.0f);

			final SbVec4fSingle screenCoords4D = new SbVec4fSingle();
			cameraViewVolume.getMatrix().multVecMatrix(direction4D,screenCoords4D);

			objectives.put(name,screenCoords4D);
		}

		for( Map.Entry<String,SbVec4f> entry : objectives.entrySet()) {
			String name = entry.getKey();
			SbVec4f screenCoords4D = entry.getValue();

			SoSeparator foundSep = null;
			for( int childNo = 0; childNo < objectivesGroup.getNumChildren(); childNo++) {
				SoNode child = objectivesGroup.getChild(childNo);
				if (Objects.equals(child.getName().getString(),name)) {
					foundSep = (SoSeparator) child;
				}
			}
			float w = screenCoords4D.getW();
			float angle = (float) Math.atan2(screenCoords4D.getY() / w * Math.signum(w), screenCoords4D.getX() / w * Math.signum(w));
			float norm = (float) Math.sqrt(screenCoords4D.getY() / w * screenCoords4D.getY() / w + screenCoords4D.getX() / w * screenCoords4D.getX() / w);
			norm = w > 0 ? Math.min(0.9f, norm) : 0.9f;
			if (foundSep == null) {
				SoSeparator objectiveSeparator = new SoSeparator();
				objectiveSeparator.setName(name);

				SoBlinker blinker = new SoBlinker();
				blinker.whichChild.setValue(SoSwitch.SO_SWITCH_ALL);

				objectiveSeparator.addChild(blinker);

				SoTranslation translation = new SoTranslation();
				translation.translation.setValue((float) Math.cos(angle) * norm, (float) Math.sin(angle) * norm, 0);
				blinker.addChild(translation);
				blinker.addChild(objectiveMarkerSet);
				objectivesGroup.addChild(objectiveSeparator);
			}
			else {
				SoTranslation translation = (SoTranslation) ((SoBlinker)foundSep.getChild(0)).getChild(0);
				translation.translation.setValue((float) Math.cos(angle) * norm, (float) Math.sin(angle) * norm, 0);
			}
		}
	}

	private void addObjective(String name, SbVec3f coordinates) {
		threeDObjectives.put(name,coordinates);
	}
}
