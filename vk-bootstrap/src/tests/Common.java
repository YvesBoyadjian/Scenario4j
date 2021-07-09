package tests;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkInstance;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class Common {

    public static /*GLFWwindow*/long create_window_glfw() {
        return create_window_glfw("");
    }
    public static /*GLFWwindow*/long create_window_glfw(String window_name) {
        return create_window_glfw(window_name,true);
    }
    /*23*/ public static /*GLFWwindow*/long create_window_glfw(String window_name, boolean resize) {
        glfwInit();
        if (!glfwVulkanSupported()) {
            throw new AssertionError("GLFW failed to find the Vulkan loader");
        }
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        if (!resize) glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        return glfwCreateWindow(1024, 1024, window_name, 0, 0);
    }
    /*30*/ public static void destroy_window_glfw(/*GLFWwindow*/long window) {
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static long create_surface_glfw(VkInstance instance, /*GLFWwindow*/long window) {
        return create_surface_glfw(instance,window,null);
    }
    /*34*/ public static /*VkSurfaceKHR*/long create_surface_glfw(VkInstance instance, /*GLFWwindow*/long window, VkAllocationCallbacks allocator) {
        /*VkSurfaceKHR*/final long[] surface = new long[1];//VK_NULL_HANDLE;
        /*VkResult*/int err = glfwCreateWindowSurface(instance, window, allocator, surface);
        if (err != 0) {
		    final PointerBuffer error_msg = PointerBuffer.allocateDirect(999);
            int ret = glfwGetError(error_msg);
            if (ret != 0) {
                System.out.print(ret + " ");
                if (error_msg != null) System.out.print( error_msg.getStringUTF8() );
                System.out.print("\n");
            }
            surface[0] = VK_NULL_HANDLE;
        }
        return surface[0];
    }
}
