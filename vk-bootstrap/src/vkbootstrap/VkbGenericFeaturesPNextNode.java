package vkbootstrap;

import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VkBaseOutStructure;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.*;

public class VkbGenericFeaturesPNextNode extends Struct implements NativeResource {

    /** The struct size in bytes. */
    public static final int SIZEOF;

    public static final int field_capacity = 256;

    public static final int
            STYPE,
            PNEXT,
            FIELDS;

    static {
        Layout layout = __struct(
                __member(4),
                __member(POINTER_SIZE),
                __array(4,field_capacity)
        );

        SIZEOF = layout.getSize();

        STYPE = layout.offsetof(0);
        PNEXT = layout.offsetof(1);
        FIELDS = layout.offsetof(2);
    }

    /**
     * Creates a {@code VkBaseOutStructure} instance at the current position of the specified {@link ByteBuffer} container. Changes to the buffer's content will be
     * visible to the struct instance and vice versa.
     *
     * <p>The created instance holds a strong reference to the container object.</p>
     */
    public VkbGenericFeaturesPNextNode(ByteBuffer container) {
        super(memAddress(container), __checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public int sType() {
        return nsType(this.address());
    }

    public int fields(int i) { return nsFields(this.address,i); }

    public VkbGenericFeaturesPNextNode pNext(VkbGenericFeaturesPNextNode value) { npNext(address(), value); return this; }

    public static void npNext(long struct, VkbGenericFeaturesPNextNode value) {
        memPutAddress(struct + VkbGenericFeaturesPNextNode.PNEXT, memAddressSafe(value));
    }

    public static int nsType(long struct) {
        return UNSAFE.getInt((Object)null, struct + (long)STYPE);
    }

    public static int nsFields(long struct,int i) {
        return UNSAFE.getInt((Object)null, struct + (long)FIELDS+4*i);
    }

    public static boolean match(
            final VkbGenericFeaturesPNextNode requested, final VkbGenericFeaturesPNextNode supported) {
        assert(requested.sType() == supported.sType() && "Non-matching sTypes in features nodes!"!=null);
        for (int i = 0; i < field_capacity; i++) {
            if (requested.fields(i) != 0 && supported.fields(i) == 0) return false;
        }
        return true;
    }
}
