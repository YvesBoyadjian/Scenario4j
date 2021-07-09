package vulkanguide;

import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class Material {
    public /*VkDescriptorSet*/final long[] textureSet = new long[1];//VK_NULL_HANDLE;
    public /*VkPipeline*/long pipeline;
    public /*VkPipelineLayout*/long pipelineLayout;
}
