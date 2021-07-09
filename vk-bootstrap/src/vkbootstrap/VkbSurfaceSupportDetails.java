package vkbootstrap;

import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.util.ArrayList;
import java.util.List;

public class VkbSurfaceSupportDetails {
    public VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.create();
    public final List<VkSurfaceFormatKHR> formats = new ArrayList<>();
    public final List</*VkPresentModeKHR*/Integer> present_modes = new ArrayList<>();

    public VkbSurfaceSupportDetails(VkSurfaceCapabilitiesKHR capabilities,
                                    List<VkSurfaceFormatKHR> formats,
                                    List</*VkPresentModeKHR*/Integer> present_modes
                                    ) {
        this.capabilities = capabilities;
        this.formats.addAll(formats);
        this.present_modes.addAll(present_modes);
    }
}
