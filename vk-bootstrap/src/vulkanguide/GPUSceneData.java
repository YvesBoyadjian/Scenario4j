package vulkanguide;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryUtil.memAllocFloat;

public class GPUSceneData {
    public final Vector4f fogColor = new Vector4f(); // w is for exponent
    public final Vector4f fogDistances = new Vector4f(); //x for min, y for max, zw unused.
    public final Vector4f ambientColor = new Vector4f();
    public final Vector4f sunlightDirection = new Vector4f(); //w for sun power
    public final Vector4f sunlightColor = new Vector4f();

    public static int sizeof() {
        return 5 * 4 * Float.BYTES;
    }

    public Buffer toBuffer() {
        FloatBuffer buffer = memAllocFloat(sizeof()/Float.BYTES);
        fillBuffer(buffer,fogColor);
        fillBuffer(buffer,fogDistances);
        fillBuffer(buffer,ambientColor);
        fillBuffer(buffer,sunlightDirection);
        fillBuffer(buffer,sunlightColor);
        buffer.flip();
        return buffer;
    }
    public static void fillBuffer(FloatBuffer buffer, Vector4f vector) {
        buffer.put(vector.x());
        buffer.put(vector.y());
        buffer.put(vector.z());
        buffer.put(vector.w());
    }
}
