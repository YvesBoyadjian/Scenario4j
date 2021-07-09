package vkbootstrap;

import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkInstance;

import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class VkbInstance {
    public final VkInstance[] instance = new VkInstance[1];//VK_NULL_HANDLE;
    public /*VkDebugUtilsMessengerEXT*/final long[] debug_messenger = new long[1];//VK_NULL_HANDLE;
    public VkAllocationCallbacks allocation_callbacks = null;//VK_NULL_HANDLE;

    VkbVulkanFunctions.PFN_vkGetInstanceProcAddr fp_vkGetInstanceProcAddr = null;

    boolean headless = false; // package access
    int instance_version = VK_MAKE_VERSION(1, 0, 0); // package access
}
