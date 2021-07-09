package vkbootstrap;

import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import port.error_code;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static vkbootstrap.VkBootstrap.*;

public class VkbDevice {
    public final VkDevice[] device = new VkDevice[1];//VK_NULL_HANDLE;
    public VkbPhysicalDevice physical_device;
    public /*VkSurfaceKHR*/long surface = VK_NULL_HANDLE;
    public final List<VkQueueFamilyProperties> queue_families = new ArrayList<>();
    public final VkAllocationCallbacks[] allocation_callbacks = new VkAllocationCallbacks[1];//VK_NULL_HANDLE;

    /*1310*/ public Result<Integer> get_queue_index(VkbQueueType type) {
        int index = QUEUE_INDEX_MAX_VALUE;
        switch (type) {
            case present:
                index = get_present_queue_index(physical_device.physical_device, surface, queue_families);
                if (index == QUEUE_INDEX_MAX_VALUE)
                    return new Result<Integer>( new error_code(VkbQueueError.present_unavailable.ordinal()));
                break;
            case graphics:
                index = get_first_queue_index(queue_families, VK_QUEUE_GRAPHICS_BIT);
                if (index == QUEUE_INDEX_MAX_VALUE)
                    return new Result<Integer>( new error_code(VkbQueueError.graphics_unavailable.ordinal()));
                break;
            case compute:
                index = get_separate_queue_index(queue_families, VK_QUEUE_COMPUTE_BIT, VK_QUEUE_TRANSFER_BIT);
                if (index == QUEUE_INDEX_MAX_VALUE)
                    return new Result<Integer>( new error_code(VkbQueueError.compute_unavailable.ordinal()));
                break;
            case transfer:
                index = get_separate_queue_index(queue_families, VK_QUEUE_TRANSFER_BIT, VK_QUEUE_COMPUTE_BIT);
                if (index == QUEUE_INDEX_MAX_VALUE)
                    return new Result<Integer>( new error_code(VkbQueueError.transfer_unavailable.ordinal()));
                break;
            default:
                return new Result<Integer>( new error_code(VkbQueueError.invalid_queue_family_index.ordinal()));
        }
        return new Result(index);
    }

    /*1363*/ public Result<VkQueue> get_queue(VkbQueueType type) {
        var index = get_queue_index(type);
        if (!index.has_value()) return new Result(index.error());
        return new Result(VkBootstrap.get_queue(device[0], index.value()));
    }
}
