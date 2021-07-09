package vkbootstrap;

public enum VkbInstanceError {
    vulkan_unavailable,
    vulkan_version_unavailable,
    vulkan_version_1_1_unavailable,
    vulkan_version_1_2_unavailable,
    failed_create_instance,
    failed_create_debug_messenger,
    requested_layers_not_present,
    requested_extensions_not_present,
    windowing_extensions_not_present
}
