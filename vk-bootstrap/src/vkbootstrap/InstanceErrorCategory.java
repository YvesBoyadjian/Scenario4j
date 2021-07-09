package vkbootstrap;

import port.error_category;

import static vkbootstrap.VkBootstrap.to_string;

public class InstanceErrorCategory extends error_category {

	public String name() { return "vkb_instance"; }
    public String message(int err) {
        return to_string(VkbInstanceError.values()[err]);
    }
}
