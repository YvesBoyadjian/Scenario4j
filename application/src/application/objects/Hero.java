package application.objects;

import org.ode4j.ode.DBody;

public class Hero {

    static public final float STARTING_X = 250.5f;

    static public final float STARTING_Y = 303.5f;

    static public final float STARTING_Z = 1256f;

    public static final String HERO_X = "hero_x";

    public static final String HERO_Y = "hero_y";

    public static final String HERO_Z = "hero_z";

    public static final String FLY = "fly";

    public static final String BOOTS = "boots";

    public static final String LIFE = "life";

    public boolean fly = false;

    public DBody body;
    public DBody ballBody;

    public float life = 1; // From 0 to 1
}
