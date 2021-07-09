package vulkanguide;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

public class Vertex {

    public final Vector3f position = new Vector3f();
    public final Vector3f normal = new Vector3f();
    public final Vector3f color = new Vector3f();
    public final Vector2f uv = new Vector2f();

    public static int sizeof() {
        return Float.BYTES *( 3 + 3 + 3 + 2);
    }

    public static int offsetof_position() {
        return 0;
    }

    public static int offsetof_normal() {
        return Float.BYTES * 3;
    }

    public static int offsetof_color() {
        return Float.BYTES * 6;
    }

    public static int offsetof_uv() {
        return Float.BYTES * 9;
    }

    public static VertexInputDescription get_vertex_description() {

        final VertexInputDescription description = new VertexInputDescription();

        //we will have just 1 vertex buffer binding, with a per-vertex rate
        final VkVertexInputBindingDescription mainBinding = VkVertexInputBindingDescription.create();
        mainBinding.binding ( 0);
        mainBinding.stride ( Vertex.sizeof());
        mainBinding.inputRate ( VK_VERTEX_INPUT_RATE_VERTEX);

        description.bindings.add(mainBinding);

        //Position will be stored at Location 0
        final VkVertexInputAttributeDescription positionAttribute = VkVertexInputAttributeDescription.create();
        positionAttribute.binding ( 0);
        positionAttribute.location ( 0);
        positionAttribute.format ( VK_FORMAT_R32G32B32_SFLOAT);
        positionAttribute.offset ( Vertex.offsetof_position(/*Vertex, position*/));

        //Normal will be stored at Location 1
        final VkVertexInputAttributeDescription normalAttribute = VkVertexInputAttributeDescription.create();
        normalAttribute.binding ( 0);
        normalAttribute.location ( 1);
        normalAttribute.format ( VK_FORMAT_R32G32B32_SFLOAT);
        normalAttribute.offset ( Vertex.offsetof_normal(/*Vertex, normal*/));

        //Position will be stored at Location 2
        final VkVertexInputAttributeDescription colorAttribute = VkVertexInputAttributeDescription.create();
        colorAttribute.binding ( 0);
        colorAttribute.location ( 2);
        colorAttribute.format ( VK_FORMAT_R32G32B32_SFLOAT);
        colorAttribute.offset ( Vertex.offsetof_color(/*Vertex, color*/));

        //UV will be stored at Location 2
        final VkVertexInputAttributeDescription uvAttribute = VkVertexInputAttributeDescription.create();
        uvAttribute.binding ( 0);
        uvAttribute.location ( 3);
        uvAttribute.format ( VK_FORMAT_R32G32_SFLOAT);
        uvAttribute.offset ( Vertex.offsetof_uv(/*Vertex, uv*/));


        description.attributes.add(positionAttribute);
        description.attributes.add(normalAttribute);
        description.attributes.add(colorAttribute);
        description.attributes.add(uvAttribute);
        return description;
    }
}
