package vkbootstrap.example;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkQueue;

import java.util.ArrayList;
import java.util.List;

public class RenderData {
    public VkQueue graphics_queue;
    public VkQueue present_queue;

    public final List</*VkImage*/Long> swapchain_images = new ArrayList<>();
    public final List</*VkImageView*/Long> swapchain_image_views = new ArrayList<>();
    public final List</*VkFramebuffer*/Long> framebuffers = new ArrayList<>();

    public /*VkRenderPass*/final long[] render_pass = new long[1];
    public /*VkPipelinLayout*/final long[] pipeline_layout = new long[1];
    public /*VkPipeline*/final long[] graphics_pipeline = new long[1];

    public /*VkCommandPool*/final long[] command_pool = new long[1];
    public final List<VkCommandBuffer> command_buffers = new ArrayList<>();

    public final List</*VkSemaphore*/long[]> available_semaphores = new ArrayList<>();
    public final List</*VkSemaphore*/long[]> finished_semaphore = new ArrayList<>();
    public final List</*VkFence*/long[]> in_flight_fences = new ArrayList<>();
    public final List</*VkFence*/long[]> image_in_flight = new ArrayList<>();
    public long current_frame = 0;
}
