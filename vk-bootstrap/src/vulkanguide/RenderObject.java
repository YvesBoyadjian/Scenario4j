package vulkanguide;

import org.joml.Matrix4f;

public class RenderObject {
    public Mesh mesh; // ptr

    public Material material; //ptr

    public final Matrix4f transformMatrix = new Matrix4f();
}
