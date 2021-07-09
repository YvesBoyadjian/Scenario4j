package vkbootstrap.example;

import tests.VulkanLibrary;
import vkbootstrap.VkbDevice;
import vkbootstrap.VkbInstance;
import vkbootstrap.VkbSwapchain;

public class Init {
    public /*GLFWwindow*/long window;
    public final VulkanLibrary vk_lib = new VulkanLibrary();
    public VkbInstance instance = new VkbInstance();
    public /*VkSurfaceKHR*/long surface;
    public VkbDevice device = new VkbDevice();
    public VkbSwapchain swapchain = new VkbSwapchain();

    public VulkanLibrary arrow_operator() {
        return vk_lib;
    }
}
