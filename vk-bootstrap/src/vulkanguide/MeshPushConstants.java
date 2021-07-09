package vulkanguide;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryUtil.memAllocFloat;

public class MeshPushConstants {
    public final Vector4f data = new Vector4f();
    public final Matrix4f render_matrix = new Matrix4f();

    public static int sizeof() {
        return (4 + 16)* Float.BYTES;
    }

    public FloatBuffer toFloatBuffer() {
        FloatBuffer fb = memAllocFloat(sizeof()/Float.BYTES);
        data.get(fb);
        return fb;
    }
}
