package vulkanguide;

import org.joml.Matrix4f;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;

public class GPUCameraData {
    public final Matrix4f view = new Matrix4f();
    public final Matrix4f proj = new Matrix4f();
    public final Matrix4f viewproj = new Matrix4f();

    public static int sizeof() {
        return 3 * 4 * 4 * Float.BYTES;
    }

    public Buffer toBuffer() {
        FloatBuffer buffer = memAllocFloat(sizeof()/Float.BYTES);
        float[] coefs = new float[16];
        buffer.put(view.get(coefs));
        buffer.put(proj.get(coefs));
        buffer.put(viewproj.get(coefs));
        buffer.flip();
        return buffer;
    }
}
