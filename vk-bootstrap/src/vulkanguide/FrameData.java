package vulkanguide;

import org.lwjgl.vulkan.VkCommandBuffer;

public class FrameData {
    public/*VkSemaphore*/final long[] _presentSemaphore = new long[1];
    public final long[] _renderSemaphore = new long[1];
    public/*VkFence*/final long[] _renderFence = new long[1];

    public DeletionQueue _frameDeletionQueue;

    public /*VkCommandPool*/final long[] _commandPool = new long[1];
    public VkCommandBuffer _mainCommandBuffer;

    public AllocatedBuffer cameraBuffer;
    public/*VkDescriptorSet*/final long[] globalDescriptor = new long[1];

    public AllocatedBuffer objectBuffer;
    public/*VkDescriptorSet*/final long[] objectDescriptor = new long[1];
}
