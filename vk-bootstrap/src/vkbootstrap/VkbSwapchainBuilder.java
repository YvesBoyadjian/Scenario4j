package vkbootstrap;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import port.Port;
import port.error_code;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.VK10.*;
import static vkbootstrap.VkBootstrap.QUEUE_INDEX_MAX_VALUE;
import static vkbootstrap.VkBootstrap.vulkan_functions;

public class VkbSwapchainBuilder {

    private static class SwapchainInfo {
        public VkPhysicalDevice physical_device = /*VK_NULL_HANDLE*/null;
        public VkDevice device = /*VK_NULL_HANDLE*/null;
        public final List<VkBaseOutStructure> pNext_chain = new ArrayList<>();
        public /*VkSwapchainCreateFlagBitsKHR*/int create_flags = (/*VkSwapchainCreateFlagBitsKHR*/int)(0);
        public /*VkSurfaceKHR*/ long surface = VK_NULL_HANDLE;
        public final List<VkSurfaceFormatKHR> desired_formats = new ArrayList<>();
        public int desired_width = 256;
        public int desired_height = 256;
        public int array_layer_count = 1;
        public /*VkImageUsageFlags*/int image_usage_flags = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
        public /*VkFormatFeatureFlags*/int format_feature_flags = VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT;
        public int graphics_queue_index = 0;
        public int present_queue_index = 0;
        public /*VkSurfaceTransformFlagBitsKHR*/int pre_transform = (/*VkSurfaceTransformFlagBitsKHR*/int)(0);
        public /*VkCompositeAlphaFlagBitsKHR*/int composite_alpha = VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
        public final List</*VkPresentModeKHR*/Integer> desired_present_modes = new ArrayList<>();
        public boolean clipped = true;
        public /*VkSwapchainKHR*/long old_swapchain = VK_NULL_HANDLE;
        public VkAllocationCallbacks allocation_callbacks = null;//VK_NULL_HANDLE;
    };

    private final SwapchainInfo info = new SwapchainInfo();

    /*1518*/ public VkbSwapchainBuilder(final VkbDevice device) {
        info.device = device.device[0];
        info.physical_device = device.physical_device.physical_device;
        info.surface = device.surface;
        var present = device.get_queue_index(VkbQueueType.present);
        var graphics = device.get_queue_index(VkbQueueType.graphics);
        assert(graphics.has_value() && present.has_value() && "Graphics and Present queue indexes must be valid" != null);
        info.graphics_queue_index = present.value();
        info.present_queue_index = graphics.value();
        info.allocation_callbacks = device.allocation_callbacks[0];
    }

    public VkbSwapchainBuilder (VkPhysicalDevice physical_device, VkDevice device, /*VkSurfaceKHR*/long surface) {
        this(physical_device,device,surface,-1,-1);
    }
    /*1540*/ public VkbSwapchainBuilder (VkPhysicalDevice physical_device, VkDevice device, /*VkSurfaceKHR*/long surface, int graphics_queue_index, int present_queue_index){
        info.physical_device = physical_device;
        info.device = device;
        info.surface = surface;
        info.graphics_queue_index = (int)(graphics_queue_index);
        info.present_queue_index = (int)(present_queue_index);
        if (graphics_queue_index == QUEUE_INDEX_MAX_VALUE || present_queue_index == QUEUE_INDEX_MAX_VALUE) {
            var queue_families = VkBootstrap.get_vector_noerror/*<VkQueueFamilyProperties>*/ (
                    VkBootstrap.vulkan_functions().fp_vkGetPhysicalDeviceQueueFamilyProperties, physical_device);
            if (graphics_queue_index == QUEUE_INDEX_MAX_VALUE)
                info.graphics_queue_index = (int)(VkBootstrap.get_first_queue_index (queue_families, VK_QUEUE_GRAPHICS_BIT));
            if (present_queue_index == QUEUE_INDEX_MAX_VALUE)
                info.present_queue_index = (int)(VkBootstrap.get_present_queue_index (physical_device, surface, queue_families));
        }
    }

    /*1655*/ public Result<VkbSwapchain> build() {
        if (info.surface == VK_NULL_HANDLE) {
            return new Result(new Error( new error_code(VkbSwapchainError.surface_handle_not_provided.ordinal()) ));
        }

        List<VkSurfaceFormatKHR> desired_formats = new ArrayList<>(); desired_formats.addAll(info.desired_formats);
        if (desired_formats.size() == 0) add_desired_formats(desired_formats);
        List<Integer> desired_present_modes = new ArrayList<>(); desired_present_modes.addAll(info.desired_present_modes);
        if (desired_present_modes.size() == 0) add_desired_present_modes(desired_present_modes);

        var surface_support_ret = VkBootstrap.query_surface_support_details(info.physical_device, info.surface);
        if (!surface_support_ret.has_value())
            return new Result(new Error( new error_code(VkbSwapchainError.failed_query_surface_support_details.ordinal()),
                surface_support_ret.vk_result() ));
        var surface_support = surface_support_ret.value();

        int image_count = surface_support.capabilities.minImageCount() + 1;
        if (surface_support.capabilities.maxImageCount() > 0 && image_count > surface_support.capabilities.maxImageCount()) {
            image_count = surface_support.capabilities.maxImageCount();
        }
        VkSurfaceFormatKHR surface_format = VkBootstrap.find_surface_format(
                info.physical_device, surface_support.formats, desired_formats, info.format_feature_flags);

        VkExtent2D extent =
                VkBootstrap.find_extent(surface_support.capabilities, info.desired_width, info.desired_height);

        int image_array_layers = info.array_layer_count;
        if (surface_support.capabilities.maxImageArrayLayers() < info.array_layer_count)
            image_array_layers = surface_support.capabilities.maxImageArrayLayers();
        if (info.array_layer_count == 0) image_array_layers = 1;

        int queue_family_indices[] = { info.graphics_queue_index, info.present_queue_index };


        /*VkPresentModeKHR*/int present_mode =
                VkBootstrap.find_present_mode(surface_support.present_modes, desired_present_modes);

        /*VkSurfaceTransformFlagBitsKHR*/int pre_transform = info.pre_transform;
        if (info.pre_transform == (/*VkSurfaceTransformFlagBitsKHR*/int)(0))
            pre_transform = surface_support.capabilities.currentTransform();

        final VkSwapchainCreateInfoKHR swapchain_create_info = VkSwapchainCreateInfoKHR.create();
        swapchain_create_info.sType( VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
        VkBootstrap.setup_pNext_chain(swapchain_create_info, info.pNext_chain);
        for (var node : info.pNext_chain) {
            assert(node.sType() != VK_STRUCTURE_TYPE_APPLICATION_INFO);
        }
        swapchain_create_info.flags( info.create_flags);
        swapchain_create_info.surface( info.surface);
        swapchain_create_info.minImageCount( image_count);
        swapchain_create_info.imageFormat( surface_format.format());
        swapchain_create_info.imageColorSpace( surface_format.colorSpace());
        swapchain_create_info.imageExtent( extent);
        swapchain_create_info.imageArrayLayers( image_array_layers);
        swapchain_create_info.imageUsage( info.image_usage_flags);

        if (info.graphics_queue_index != info.present_queue_index) {
            swapchain_create_info.imageSharingMode( VK_SHARING_MODE_CONCURRENT);
            //swapchain_create_info.queueFamilyIndexCount( 2); java port
            swapchain_create_info.pQueueFamilyIndices( Port.toIntBuffer(queue_family_indices));
        } else {
            swapchain_create_info.imageSharingMode( VK_SHARING_MODE_EXCLUSIVE);
        }

        swapchain_create_info.preTransform( pre_transform);
        swapchain_create_info.compositeAlpha( info.composite_alpha);
        swapchain_create_info.presentMode( present_mode);
        swapchain_create_info.clipped( info.clipped);
        swapchain_create_info.oldSwapchain( info.old_swapchain);
        final VkbSwapchain swapchain = new VkbSwapchain();
        /*VkResult*/int res = vulkan_functions().fp_vkCreateSwapchainKHR.invoke(
                info.device, swapchain_create_info, info.allocation_callbacks, swapchain.swapchain);
        if (res != VK_SUCCESS) {
            return new Result(new Error( new error_code(VkbSwapchainError.failed_create_swapchain.ordinal()), res ));
        }
        swapchain.device = info.device;
        swapchain.image_format = surface_format.format();
        swapchain.extent = extent;
        var images = swapchain.get_images();
        if (images.not()) {
            return new Result(new Error( new error_code(VkbSwapchainError.failed_get_swapchain_images.ordinal() )));
        }
        swapchain.image_count = (int)(images.value().size());
        swapchain.allocation_callbacks = info.allocation_callbacks;
        return new Result(swapchain);
    }

    /*1791*/
    public VkbSwapchainBuilder set_old_swapchain(final VkbSwapchain swapchain) {
        info.old_swapchain = swapchain.swapchain[0];
        return this;
    }

    public VkbSwapchainBuilder set_desired_extent(int width, int height) {
        info.desired_width = width;
        info.desired_height = height;
        return this;
    }

    /*1840*/ public VkbSwapchainBuilder use_default_format_selection() {
        info.desired_formats.clear();
        add_desired_formats(info.desired_formats);
        return this;
    }

    /*1846*/
    public VkbSwapchainBuilder set_desired_present_mode(/*VkPresentModeKHR*/int present_mode) {
        info.desired_present_modes.add(/*info.desired_present_modes.begin()*/0, present_mode);
        return this;
    }

    /*1876*/ void add_desired_formats(List<VkSurfaceFormatKHR> formats) {
        VkSurfaceFormatKHR fmt1 = VkSurfaceFormatKHR.create();
        Port.UNSAFE.putInt(null, fmt1.address() + VkSurfaceFormatKHR.FORMAT, VK_FORMAT_B8G8R8A8_SRGB);
        Port.UNSAFE.putInt(null, fmt1.address() + VkSurfaceFormatKHR.COLORSPACE, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
        formats.add( /*{ VK_FORMAT_B8G8R8A8_SRGB, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR }*/fmt1);
        VkSurfaceFormatKHR fmt2 = VkSurfaceFormatKHR.create();
        Port.UNSAFE.putInt(null, fmt2.address() + VkSurfaceFormatKHR.FORMAT, VK_FORMAT_R8G8B8A8_SRGB);
        Port.UNSAFE.putInt(null, fmt2.address() + VkSurfaceFormatKHR.COLORSPACE, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
        formats.add( /*{ VK_FORMAT_R8G8B8A8_SRGB, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR }*/fmt2);
    }
    /*1880*/ void add_desired_present_modes(List</*VkPresentModeKHR*/Integer> modes) {
        modes.add(VK_PRESENT_MODE_MAILBOX_KHR);
        modes.add(VK_PRESENT_MODE_FIFO_KHR);
    }
}
