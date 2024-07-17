package application.objects;

import jscenegraph.database.inventor.SbVec3f;

import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;

public class Hero {

    static public final float STARTING_X = 255.5f;

    static public final float STARTING_Y = 303.5f;

    static public final float STARTING_Z = 1256f;

    public static final String HERO_X = "hero_x";

    public static final String HERO_Y = "hero_y";

    public static final String HERO_Z = "hero_z";

    public static final String CAT_X = "cat_x";

    public static final String CAT_Y = "cat_y";

    public static final String CAT_Z = "cat_z";

    public static final String FLY = "fly";

    public static final String BOOTS = "boots";

    public static final String LIFE = "life";

    public boolean fly = false;

    private DBody body;
    public DBody ballBody;

    public float life = 1; // From 0 to 1
    public boolean hurting;

    public SbVec3f getPosition() {
        DVector3C bodyPostion = body.getPosition();
        return new SbVec3f((float)bodyPostion.get0(),(float)bodyPostion.get1(),(float)bodyPostion.get2());
    }

	public void setBody(DBody body2) {
		body = body2;
		
	}

	public void setPosition(DVector3 saved_pos) {
		body.setPosition(saved_pos);		
	}

	public boolean hasPosition() {
		return body != null;
	}

	public void setPosition(float x, float y, float z) {
		body.setPosition(x,y,z);
		
	}

	public void stop() {
		body.setLinearVel(0,0,0);		
	}
}
