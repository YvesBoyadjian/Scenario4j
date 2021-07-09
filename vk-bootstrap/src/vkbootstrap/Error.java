package vkbootstrap;

import port.error_code;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class Error {
    public error_code type = new error_code();
    public /*VkResult*/int vk_result = VK_SUCCESS; // optional error value if a vulkan call failed

    public Error() {
        // do nothing
    }

    public Error(error_code error_code) {
        type = error_code;
    }

    public Error(error_code error_code, int result) {
        type = error_code;
        vk_result = result;
    }
}
