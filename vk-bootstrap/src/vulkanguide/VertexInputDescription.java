package vulkanguide;

import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import java.util.ArrayList;
import java.util.List;

public class VertexInputDescription {
    public final List<VkVertexInputBindingDescription> bindings = new ArrayList<>();
    public final List<VkVertexInputAttributeDescription> attributes = new ArrayList<>();

    public/*VkPipelineVertexInputStateCreateFlags*/int flags = 0;
}
