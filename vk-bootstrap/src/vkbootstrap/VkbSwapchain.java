package vkbootstrap;

import org.lwjgl.vulkan.*;
import port.error_code;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static vkbootstrap.VkBootstrap.vulkan_functions;

public class VkbSwapchain {
    public VkDevice device = null;//VK_NULL_HANDLE;
    public /*VkSwapchainKHR*/final long[] swapchain = new long[1];//VK_NULL_HANDLE;
    public int image_count = 0;
    public /*VkFormat*/int image_format = VK_FORMAT_UNDEFINED;
    public VkExtent2D extent = VkExtent2D.create();
    VkAllocationCallbacks allocation_callbacks = null;//VK_NULL_HANDLE;

    /*1741*/
    public Result<List</*VkImage*/Long>> get_images() {
        final List</*VkImage*/Long> swapchain_images = new ArrayList<>();

        var swapchain_images_ret = VkBootstrap.get_vector/*VkImage>*/(
        swapchain_images, vulkan_functions().fp_vkGetSwapchainImagesKHR, device, swapchain[0]);
        if (swapchain_images_ret != VK_SUCCESS) {
            return new Result(new Error( new error_code(VkbSwapchainError.failed_get_swapchain_images.ordinal()), swapchain_images_ret ));
        }
        return new Result(swapchain_images);
    }

    /*1751*/ public Result<List</*VkImageView*/Long>> get_image_views() {

        var swapchain_images_ret = get_images();
        if (swapchain_images_ret.not()) return new Result<>(swapchain_images_ret.error());
        var swapchain_images = swapchain_images_ret.value();

        final List</*VkImageView*/Long> views = new ArrayList<>(/*swapchain_images.size()*/);

        for (int i = 0; i < swapchain_images.size(); i++) {
            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.create();
            createInfo.sType( VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            createInfo.image( swapchain_images.get(i));
            createInfo.viewType( VK_IMAGE_VIEW_TYPE_2D);
            createInfo.format( image_format);
            createInfo.components().r( VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.components().g( VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.components().b( VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.components().a( VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.subresourceRange().aspectMask( VK_IMAGE_ASPECT_COLOR_BIT);
            createInfo.subresourceRange().baseMipLevel( 0);
            createInfo.subresourceRange().levelCount( 1);
            createInfo.subresourceRange().baseArrayLayer( 0);
            createInfo.subresourceRange().layerCount( 1);

            final long[] p_view = new long[1];

            /*VkResult*/int res = vulkan_functions().fp_vkCreateImageView.invoke(
                    device, createInfo, allocation_callbacks, p_view);
            if (res != VK_SUCCESS)
                return new Result(new Error( new error_code(VkbSwapchainError.failed_create_swapchain_image_views.ordinal()), res ));
            views.add(p_view[0]);
        }
        return new Result<>(views);
    }
    /*1782*/ public void destroy_image_views(final List</*VkImageView*/Long> image_views) {
        for (var image_view : image_views) {
            vulkan_functions().fp_vkDestroyImageView.invoke(device, image_view, allocation_callbacks);
        }
    }
}
