package vkbootstrap;

import port.error_category;

public class SurfaceSupportErrorCategory extends error_category {
    @Override
    public String name() {
        return "vbk_surface_support";
    }

    @Override
    public String message(int err) {
        switch (VkbSurfaceSupportError.values()[err]) {
            case surface_handle_null:
                return "surface_handle_null";
            case failed_get_surface_capabilities:
                return "failed_get_surface_capabilities";
            case failed_enumerate_surface_formats:
                return "failed_enumerate_surface_formats";
            case failed_enumerate_present_modes:
                return "failed_enumerate_present_modes";
            default:
                return "";
        }
    }
}
