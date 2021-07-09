package vkbootstrap;

import org.lwjgl.vulkan.*;
import port.Port;
import port.error_code;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.VK_API_VERSION_1_1;
import static org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2;
import static vkbootstrap.VkBootstrap.vulkan_functions;

public class VkbDeviceBuilder {
    private static class DeviceInfo {
        /*VkDeviceCreateFlags*/int flags = 0;
        final List<VkBaseOutStructure> pNext_chain = new ArrayList<>();
        final List<VkbCustomQueueDescription> queue_descriptions = new ArrayList<>();
        VkAllocationCallbacks allocation_callbacks = null;//VK_NULL_HANDLE;
    }

    private VkbPhysicalDevice physical_device;// = new VkbPhysicalDevice();
    private final DeviceInfo info = new DeviceInfo();

    // Any features and extensions that are requested/required in PhysicalDeviceSelector are automatically enabled.
    /*1385*/ public VkbDeviceBuilder(VkbPhysicalDevice phys_device) {
        physical_device = phys_device;
    }

    /*1387*/ public Result<VkbDevice> build() {

        final List<VkbCustomQueueDescription> queue_descriptions = new ArrayList<>();
        queue_descriptions.addAll(
                info.queue_descriptions);

        if (queue_descriptions.size() == 0) {
            for (int i = 0; i < physical_device.queue_families.size(); i++) {
                List<Float> l = new ArrayList<>(); l.add(1.0f);
                queue_descriptions.add(new VkbCustomQueueDescription( i, 1, l ));
            }
        }

        //final List<VkDeviceQueueCreateInfo> queueCreateInfos = new ArrayList<>();
        final VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.create(queue_descriptions.size()); // java port
        int index = 0;
        for (var desc : queue_descriptions) {
            final VkDeviceQueueCreateInfo queue_create_info = queueCreateInfos.get(index);//VkDeviceQueueCreateInfo.create();
            queue_create_info.sType( VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
            queue_create_info.queueFamilyIndex( desc.index);
            //queue_create_info.queueCount( desc.count); java port
            queue_create_info.pQueuePriorities(Port.dataf(desc.priorities) );
            //queueCreateInfos.add(queue_create_info); java port
            index++;
        }

        final List<String> extensions = new ArrayList<>(); extensions.addAll(physical_device.extensions_to_enable);
        if (physical_device.surface != VK_NULL_HANDLE || physical_device.defer_surface_initialization)
            extensions.add( VK_KHR_SWAPCHAIN_EXTENSION_NAME );

        boolean has_phys_dev_features_2 = false;
        boolean user_defined_phys_dev_features_2 = false;
        final List<VkBaseOutStructure> final_pnext_chain = new ArrayList<>();
        final VkDeviceCreateInfo device_create_info = VkDeviceCreateInfo.create();

//#if defined(VK_API_VERSION_1_1)
        for (var pnext : info.pNext_chain) {
            if (pnext.sType() == VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2) {
                user_defined_phys_dev_features_2 = true;
                break;
            }
        }

        List<VkbGenericFeaturesPNextNode> physical_device_extension_features_copy = new ArrayList<>();physical_device_extension_features_copy.addAll(physical_device.extended_features_chain);
        final VkPhysicalDeviceFeatures2 local_features2 = VkPhysicalDeviceFeatures2.create();
        local_features2.sType( VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2);

        if (!user_defined_phys_dev_features_2) {
            if (physical_device.instance_version >= VK_MAKE_VERSION(1, 1, 0)) {
                local_features2.features( physical_device.features);
                final_pnext_chain.add(VkBaseOutStructure.createSafe(local_features2.address()));
                has_phys_dev_features_2 = true;
                for (var features_node : physical_device_extension_features_copy) {
/*1436*/                    final_pnext_chain.add(VkBaseOutStructure.createSafe(features_node.address()));
                }
            }
        } else {
            System.out.print("User provided VkPhysicalDeviceFeatures2 instance found in pNext chain. All "+
                    "requirements added via 'add_required_extension_features' will be ignored.");
        }

        if (!user_defined_phys_dev_features_2 && !has_phys_dev_features_2) {
            device_create_info.pEnabledFeatures( physical_device.features);
        }
//#endif

        for (var pnext : info.pNext_chain) {
            final_pnext_chain.add(pnext);
        }

        VkBootstrap.setup_pNext_chain(device_create_info, final_pnext_chain);
        for (var node : final_pnext_chain) {
            assert(node.sType() != VK_STRUCTURE_TYPE_APPLICATION_INFO);
        }

        device_create_info.sType( VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
        device_create_info.flags( info.flags);
        //device_create_info.queueCreateInfoCount( (int)(queueCreateInfos.size())); java port
        device_create_info.pQueueCreateInfos(queueCreateInfos);
        //device_create_info.enabledExtensionCount( (int)(extensions.size())); java port
        device_create_info.ppEnabledExtensionNames( Port.datastr(extensions));

        final VkbDevice device = new VkbDevice();

        /*VkResult*/int res = vulkan_functions().fp_vkCreateDevice.invoke(
                physical_device.physical_device, device_create_info, info.allocation_callbacks, device.device);
        if (res != VK_SUCCESS) {
            return new Result( new error_code(VkbDeviceError.failed_create_device.ordinal()), res );
        }

        device.physical_device = physical_device;
        device.surface = physical_device.surface;
        device.queue_families.clear(); device.queue_families.addAll(physical_device.queue_families);
        device.allocation_callbacks[0] = info.allocation_callbacks;
        return new Result(device);
    }
}
