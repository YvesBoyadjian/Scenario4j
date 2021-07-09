package vkbootstrap;

import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkLayerProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static vkbootstrap.VkBootstrap.validation_layer_name;
import static vkbootstrap.VkBootstrap.vulkan_functions;

// Gathers useful information about the available vulkan capabilities, like layers and instance
// extensions. Use this for enabling features conditionally, ie if you would like an extension but
// can use a fallback if it isn't supported but need to know if support is available first.
public class VkbSystemInfo {

    public final List<VkLayerProperties> available_layers = new ArrayList<>();
    public final List<VkExtensionProperties> available_extensions = new ArrayList<>();
    public boolean validation_layers_available = false;
    public boolean debug_utils_available = false;

    // Use get_system_info to create a SystemInfo struct. This is because loading vulkan could fail.
    /*505*/ public static Result<VkbSystemInfo> get_system_info() {

        if (!vulkan_functions().init_vulkan_funcs(null)) {
            return new Result(VkBootstrap.make_error_code(VkbInstanceError.vulkan_unavailable));
        }
        return new Result(new VkbSystemInfo());
    }

    /*511*/ public static Result<VkbSystemInfo> get_system_info(VkbVulkanFunctions.PFN_vkGetInstanceProcAddr fp_vkGetInstanceProcAddr) {
        // Using externally provided function pointers, assume the loader is available
        vulkan_functions().init_vulkan_funcs(fp_vkGetInstanceProcAddr);
        return new Result(new VkbSystemInfo());
    }

    /*518*/ public VkbSystemInfo() {
        var available_layers_ret = VkBootstrap.get_vector/*<VkLayerProperties>*/(
                this.available_layers, vulkan_functions().fp_vkEnumerateInstanceLayerProperties);
        if (available_layers_ret != VK_SUCCESS) {
            this.available_layers.clear();
        }

        for (var layer : this.available_layers)
        if (Objects.equals(layer.layerNameString(), validation_layer_name))
            validation_layers_available = true;

        var available_extensions_ret = VkBootstrap.get_vector/*<VkExtensionProperties>*/(
                this.available_extensions, vulkan_functions().fp_vkEnumerateInstanceExtensionProperties, null);
        if (available_extensions_ret != VK_SUCCESS) {
            this.available_extensions.clear();
        }

        for (var ext : this.available_extensions)
        if (Objects.equals(ext.extensionNameString(), VK_EXT_DEBUG_UTILS_EXTENSION_NAME))
            debug_utils_available = true;

        for (var layer : this.available_layers) {
            final List<VkExtensionProperties> layer_extensions = new ArrayList<>();
            var layer_extensions_ret = VkBootstrap.get_vector/*<VkExtensionProperties>*/(layer_extensions,
            vulkan_functions().fp_vkEnumerateInstanceExtensionProperties,
                    layer.layerName());
            if (layer_extensions_ret == VK_SUCCESS) {
                for (var ext : layer_extensions)
                if (Objects.equals(ext.extensionNameString(), VK_EXT_DEBUG_UTILS_EXTENSION_NAME))
                    debug_utils_available = true;
            }
        }
    }
}
