package vulkanguide;

import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryUtil.memAllocFloat;

public class GPUObjectData {
    public final Matrix4f modelMatrix = new Matrix4f();

    public static int sizeof() {
        return 4 * 4 * Float.BYTES;
    }

    public static void setModelMatrix(long l, Matrix4f transformMatrix) {
        FloatBuffer fb = MemoryUtil.memFloatBuffer(l,sizeof()/Float.BYTES);
        transformMatrix.get(fb);
    }
}
