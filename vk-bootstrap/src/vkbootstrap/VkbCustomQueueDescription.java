package vkbootstrap;

import java.util.ArrayList;
import java.util.List;

// For advanced device queue setup
public class VkbCustomQueueDescription {
    public VkbCustomQueueDescription(int index, int count, List<Float> priorities) {
 this.index = index; this.count = count; this.priorities.addAll(priorities);
            assert(count == priorities.size());
    }
    public int index = 0;
    public int count = 0;
    public final List<Float> priorities = new ArrayList<>();
}
