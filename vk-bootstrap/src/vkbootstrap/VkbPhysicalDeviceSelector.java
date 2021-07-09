package vkbootstrap;

import org.lwjgl.vulkan.*;
import port.error_code;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.VK_API_VERSION_1_1;
import static org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2;
import static vkbootstrap.VkBootstrap.*;

public class VkbPhysicalDeviceSelector {

    private class InstanceInfo {
        public VkInstance instance = null;//VK_NULL_HANDLE;
        /*VkSurfaceKHR*/long surface = VK_NULL_HANDLE;
        public int version = VK_MAKE_VERSION(1, 0, 0);
        public boolean headless = false;
    }

    private class PhysicalDeviceDesc {
        public VkPhysicalDevice phys_device = null;//VK_NULL_HANDLE;
        public final List<VkQueueFamilyProperties> queue_families = new ArrayList<>();

        public final VkPhysicalDeviceFeatures device_features = VkPhysicalDeviceFeatures.create();
        public final VkPhysicalDeviceProperties device_properties = VkPhysicalDeviceProperties.create();
        public final VkPhysicalDeviceMemoryProperties mem_properties = VkPhysicalDeviceMemoryProperties.create();
//#if defined(VK_API_VERSION_1_1)
        public final VkPhysicalDeviceFeatures2 device_features2 = VkPhysicalDeviceFeatures2.create();
        public final List<VkbGenericFeaturesPNextNode> extended_features_chain = new ArrayList<>();
//#endif
    }

    private class SelectionCriteria {
        public VkbPreferredDeviceType preferred_type = VkbPreferredDeviceType.discrete;
        public boolean allow_any_type = true;
        public boolean require_present = true;
        public boolean require_dedicated_transfer_queue = false;
        public boolean require_dedicated_compute_queue = false;
        public boolean require_separate_transfer_queue = false;
        public boolean require_separate_compute_queue = false;
        public /*VkDeviceSize*/long required_mem_size = 0;
        public /*VkDeviceSize*/long desired_mem_size = 0;

        public final List<String> required_extensions = new ArrayList<>();
        public final List<String> desired_extensions = new ArrayList<>();

        public int required_version = VK_MAKE_VERSION(1, 0, 0);
        public int desired_version = VK_MAKE_VERSION(1, 0, 0);

        public final VkPhysicalDeviceFeatures required_features = VkPhysicalDeviceFeatures.create();
//#if defined(VK_API_VERSION_1_1)
        public final VkPhysicalDeviceFeatures2 required_features2 = VkPhysicalDeviceFeatures2.create();
        public final List<VkbGenericFeaturesPNextNode> extended_features_chain = new ArrayList<>();
//#endif

        public boolean defer_surface_initialization = false;
        public boolean use_first_gpu_unconditionally = false;
    }

    private enum Suitable {
        yes, partial, no
    }

    private final InstanceInfo instance_info = new InstanceInfo();

    private final SelectionCriteria criteria = new SelectionCriteria();

    /*987*/ PhysicalDeviceDesc populate_device_details(int instance_version,
                                                                                               VkPhysicalDevice phys_device,
                                                                                               List<VkbGenericFeaturesPNextNode> src_extended_features_chain) {
        final VkbPhysicalDeviceSelector.PhysicalDeviceDesc desc = new PhysicalDeviceDesc();
        desc.phys_device = phys_device;
        var queue_families = VkBootstrap.get_vector_noerror/*<VkQueueFamilyProperties>*/(
                vulkan_functions().fp_vkGetPhysicalDeviceQueueFamilyProperties, phys_device);
        desc.queue_families.clear(); desc.queue_families.addAll(queue_families);

        vulkan_functions().fp_vkGetPhysicalDeviceProperties.invoke(phys_device, desc.device_properties);
        vulkan_functions().fp_vkGetPhysicalDeviceFeatures.invoke(phys_device, desc.device_features);
        vulkan_functions().fp_vkGetPhysicalDeviceMemoryProperties.invoke(phys_device, desc.mem_properties);

//#if defined(VK_API_VERSION_1_1)
        desc.device_features2.sType( VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2);

        var fill_chain = src_extended_features_chain;

        if (!fill_chain.isEmpty() && instance_version >= VK_API_VERSION_1_1) {

            VkbGenericFeaturesPNextNode prev = null;
            for (var extension : fill_chain) {
                if (prev != null) {
                    prev.pNext(extension);
                }
                prev = extension;
            }

            final VkPhysicalDeviceFeatures2 local_features = VkPhysicalDeviceFeatures2.create();
            local_features.sType( VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2);
            local_features.pNext( fill_chain.get(0).address());

            vulkan_functions().fp_vkGetPhysicalDeviceFeatures2.invoke(phys_device, local_features);
        }

        desc.extended_features_chain.clear(); desc.extended_features_chain.addAll(fill_chain);
//#endif
        return desc;
    }

    /*1027*/ VkbPhysicalDeviceSelector.Suitable is_device_suitable(PhysicalDeviceDesc pd) {
        Suitable suitable = Suitable.yes;

        if (criteria.required_version > pd.device_properties.apiVersion()) return Suitable.no;
        if (criteria.desired_version > pd.device_properties.apiVersion()) suitable = Suitable.partial;

        boolean dedicated_compute = VkBootstrap.get_dedicated_queue_index(pd.queue_families,
                VK_QUEUE_COMPUTE_BIT,
                VK_QUEUE_TRANSFER_BIT) != QUEUE_INDEX_MAX_VALUE;
        boolean dedicated_transfer = VkBootstrap.get_dedicated_queue_index(pd.queue_families,
                VK_QUEUE_TRANSFER_BIT,
                VK_QUEUE_COMPUTE_BIT) != QUEUE_INDEX_MAX_VALUE;
        boolean separate_compute =
                VkBootstrap.get_separate_queue_index(pd.queue_families, VK_QUEUE_COMPUTE_BIT, VK_QUEUE_TRANSFER_BIT) !=
        QUEUE_INDEX_MAX_VALUE;
        boolean separate_transfer =
                VkBootstrap.get_separate_queue_index(pd.queue_families, VK_QUEUE_TRANSFER_BIT, VK_QUEUE_COMPUTE_BIT) !=
        QUEUE_INDEX_MAX_VALUE;

        boolean present_queue =
                VkBootstrap.get_present_queue_index(pd.phys_device, instance_info.surface, pd.queue_families) !=
        QUEUE_INDEX_MAX_VALUE;

        if (criteria.require_dedicated_compute_queue && !dedicated_compute) return Suitable.no;
        if (criteria.require_dedicated_transfer_queue && !dedicated_transfer) return Suitable.no;
        if (criteria.require_separate_compute_queue && !separate_compute) return Suitable.no;
        if (criteria.require_separate_transfer_queue && !separate_transfer) return Suitable.no;
        if (criteria.require_present && !present_queue && !criteria.defer_surface_initialization)
            return Suitable.no;

        var required_extensions_supported =
                check_device_extension_support(pd.phys_device, criteria.required_extensions);
        if (required_extensions_supported.size() != criteria.required_extensions.size())
            return Suitable.no;

        var desired_extensions_supported =
                check_device_extension_support(pd.phys_device, criteria.desired_extensions);
        if (desired_extensions_supported.size() != criteria.desired_extensions.size())
            suitable = Suitable.partial;

        boolean swapChainAdequate = false;
        if (criteria.defer_surface_initialization) {
            swapChainAdequate = true;
        } else if (!instance_info.headless) {
            final List<VkSurfaceFormatKHR> formats = new ArrayList<>();
            final List</*VkPresentModeKHR*/Integer> present_modes = new ArrayList<>();

            var formats_ret = VkBootstrap.get_vector/*<VkSurfaceFormatKHR>*/(formats,
            vulkan_functions().fp_vkGetPhysicalDeviceSurfaceFormatsKHR,
                    pd.phys_device,
                    instance_info.surface);
            var present_modes_ret = VkBootstrap.get_vector/*<VkPresentModeKHR>*/(present_modes,
            vulkan_functions().fp_vkGetPhysicalDeviceSurfacePresentModesKHR,
                    pd.phys_device,
                    instance_info.surface);

            if (formats_ret == VK_SUCCESS && present_modes_ret == VK_SUCCESS) {
                swapChainAdequate = !formats.isEmpty() && !present_modes.isEmpty();
            }
        }
        if (criteria.require_present && !swapChainAdequate) return Suitable.no;

        if (pd.device_properties.deviceType() != (/*VkPhysicalDeviceType*/int)(criteria.preferred_type.ordinal())) {
            if (criteria.allow_any_type)
                suitable = Suitable.partial;
            else
                return Suitable.no;
        }

        boolean required_features_supported = VkBootstrap.supports_features(
                pd.device_features, criteria.required_features, pd.extended_features_chain, criteria.extended_features_chain);
        if (!required_features_supported) return Suitable.no;

        boolean has_required_memory = false;
        boolean has_preferred_memory = false;
        final int memoryHeapCount = pd.mem_properties.memoryHeapCount();
        for (int i = 0; i < memoryHeapCount; i++) {
            if ((pd.mem_properties.memoryHeaps(i).flags() & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)!=0) {
                final long memoryHeapSize = pd.mem_properties.memoryHeaps(i).size();
                if (memoryHeapSize > criteria.required_mem_size) {
                    has_required_memory = true;
                }
                if (memoryHeapSize > criteria.desired_mem_size) {
                    has_preferred_memory = true;
                }
            }
        }
        if (!has_required_memory) return Suitable.no;
        if (!has_preferred_memory) suitable = Suitable.partial;

        return suitable;
    }

    // Requires a vkb::Instance to construct, needed to pass instance creation info.
    /*1118*/public VkbPhysicalDeviceSelector(VkbInstance instance) {
        instance_info.instance = instance.instance[0];
        instance_info.headless = instance.headless;
        instance_info.version = instance.instance_version;
        criteria.require_present = !instance.headless;
        criteria.required_version = instance.instance_version;
        criteria.desired_version = instance.instance_version;
    }

    /*1193*/ public VkbPhysicalDeviceSelector set_surface(/*VkSurfaceKHR*/long surface) {
        instance_info.surface = surface;
        instance_info.headless = false;
        return this;
    }

    /*1127*/ public Result<VkbPhysicalDevice> select() {
        if (!instance_info.headless && !criteria.defer_surface_initialization) {
            if (instance_info.surface == VK_NULL_HANDLE)
                return new Result<VkbPhysicalDevice>(new error_code(VkbPhysicalDeviceError.no_surface_provided.ordinal()));
        }


        final List<VkPhysicalDevice> physical_devices = new ArrayList<>();

        int physical_devices_ret = VkBootstrap.get_vector/*<VkPhysicalDevice,VkbVulkanFunctions.PFN_vkEnumeratePhysicalDevices>*/(
        physical_devices, vulkan_functions().fp_vkEnumeratePhysicalDevices, instance_info.instance);
        if (physical_devices_ret != VK_SUCCESS) {
            return new Result<VkbPhysicalDevice>( new error_code(VkbPhysicalDeviceError.failed_enumerate_physical_devices.ordinal()),
                    physical_devices_ret );
        }
        if (physical_devices.size() == 0) {
            return new Result<VkbPhysicalDevice>( new error_code(VkbPhysicalDeviceError.no_physical_devices_found.ordinal()));
        }

        final List<PhysicalDeviceDesc> phys_device_descriptions = new ArrayList<>();
        for (var phys_device : physical_devices) {
            phys_device_descriptions.add(populate_device_details(
                    instance_info.version, phys_device, criteria.extended_features_chain));
        }

        PhysicalDeviceDesc selected_device = new PhysicalDeviceDesc();

        if (criteria.use_first_gpu_unconditionally) {
            selected_device = phys_device_descriptions.get(0);
        } else {
            for (var device : phys_device_descriptions) {
                var suitable = is_device_suitable(device);
                if (suitable == Suitable.yes) {
                    selected_device = device;
                    break;
                } else if (suitable == Suitable.partial) {
                    selected_device = device;
                }
            }
        }

        if (selected_device.phys_device.address() == VK_NULL_HANDLE) {
            return new Result<VkbPhysicalDevice>(new error_code( VkbPhysicalDeviceError.no_suitable_device.ordinal() ));
        }
        final VkbPhysicalDevice out_device = new VkbPhysicalDevice();
        out_device.physical_device = selected_device.phys_device;
        out_device.surface = instance_info.surface;
        out_device.features.set(criteria.required_features);
        out_device.extended_features_chain.clear(); out_device.extended_features_chain.addAll(criteria.extended_features_chain);
        out_device.properties = selected_device.device_properties;
        out_device.memory_properties = selected_device.mem_properties;
        out_device.queue_families.clear(); out_device.queue_families.addAll(selected_device.queue_families);
        out_device.defer_surface_initialization = criteria.defer_surface_initialization;
        out_device.instance_version = instance_info.version;

        out_device.extensions_to_enable.addAll(criteria.required_extensions);
        var desired_extensions_supported =
                check_device_extension_support(out_device.physical_device, criteria.desired_extensions);
        out_device.extensions_to_enable.addAll(desired_extensions_supported);
        return new Result(out_device);
    }
    /*1212*/ public VkbPhysicalDeviceSelector set_minimum_version (int major, int minor) {
        criteria.required_version = VK_MAKE_VERSION (major, minor, 0);
        return this;
    }
}
