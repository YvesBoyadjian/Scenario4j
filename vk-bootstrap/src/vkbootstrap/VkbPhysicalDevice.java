package vkbootstrap;

import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;

public class VkbPhysicalDevice {
    public VkPhysicalDevice physical_device = null;//VK_NULL_HANDLE;
    public /*VkSurfaceKHR*/long surface = 0;//VK_NULL_HANDLE;

    // Note that this reflects selected features carried over from required features, not all features the physical device supports.
    public final VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.create();
    public VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.create(); // Immutable
    public VkPhysicalDeviceMemoryProperties memory_properties = VkPhysicalDeviceMemoryProperties.create(); // Immutable

    public int instance_version = VK_MAKE_VERSION(1, 0, 0);
    public final List<String> extensions_to_enable = new ArrayList<>();
    /*372*/ final List<VkQueueFamilyProperties> queue_families = new ArrayList<>();
    public final List<VkbGenericFeaturesPNextNode> extended_features_chain = new ArrayList<>();
    public boolean defer_surface_initialization = false;
}
