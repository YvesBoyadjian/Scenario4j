package port;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import vulkanguide.Vertex;

import java.nio.*;
import java.util.List;
import java.util.Vector;

import static org.lwjgl.system.MemoryUtil.*;

public class Port {

    public static final int UINT32_MAX = (int)0xffffffffl;

    public static final long UINT64_MAX = 0xffffffffffffffffl;

    public static final sun.misc.Unsafe UNSAFE = getUnsafeInstance();

    public static final IntBuffer datai(List<Integer> listOfInt) {

        int nb = listOfInt.size();
        IntBuffer ret = BufferUtils.createIntBuffer(nb);
        for( Integer i : listOfInt) {
            ret.put(i);
        }
        ret.flip();
        return ret;
    };

    public static final IntBuffer toIntBuffer(int[] arrayOfInt) {

        int nb = arrayOfInt.length;
        IntBuffer ret = BufferUtils.createIntBuffer(nb);
        for( Integer i : arrayOfInt) {
            ret.put(i);
        }
        ret.flip();
        return ret;
    };

    public static final LongBuffer toLongBuffer(long[] arrayOfInt) {

        int nb = arrayOfInt.length;
        LongBuffer ret = BufferUtils.createLongBuffer(nb);
        for( Long i : arrayOfInt) {
            ret.put(i);
        }
        ret.flip();
        return ret;
    };

    public static final FloatBuffer dataf(List<Float> listOfFloat) {

        int nb = listOfFloat.size();
        FloatBuffer ret = BufferUtils.createFloatBuffer(nb);
        for( Float f : listOfFloat) {
            ret.put(f);
        }
        ret.flip();
        return ret;
    };

    public static final PointerBuffer datastr(List<String> listOfString) {

        int nb = listOfString.size();
        PointerBuffer pb = memAllocPointer(nb);
        for( int i=0; i<nb; i++) {
            ByteBuffer bb = memUTF8(listOfString.get(i));
            pb.put(bb);
        }
        pb.flip();

        return pb;
    }

    public static final Buffer data(List<Vertex> vectorList) {

        int nb = vectorList.size();
        FloatBuffer ret = BufferUtils.createFloatBuffer(nb*Vertex.sizeof()/Float.BYTES);
        for( var vertex : vectorList) {
            ret.put(vertex.position.x());
            ret.put(vertex.position.y());
            ret.put(vertex.position.z());
            ret.put(vertex.normal.x());
            ret.put(vertex.normal.y());
            ret.put(vertex.normal.z());
            ret.put(vertex.color.x());
            ret.put(vertex.color.y());
            ret.put(vertex.color.z());
            ret.put(vertex.uv.x());
            ret.put(vertex.uv.y());
        }

        ret.flip();

        return ret;
    }

    private static sun.misc.Unsafe getUnsafeInstance() {
        java.lang.reflect.Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();

        /*
        Different runtimes use different names for the Unsafe singleton,
        so we cannot use .getDeclaredField and we scan instead. For example:

        Oracle: theUnsafe
        PERC : m_unsafe_instance
        Android: THE_ONE
        */
        for (java.lang.reflect.Field field : fields) {
            if (!field.getType().equals(sun.misc.Unsafe.class)) {
                continue;
            }

            int modifiers = field.getModifiers();
            if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers))) {
                continue;
            }

            try {
                field.setAccessible(true);
                return (sun.misc.Unsafe)field.get(null);
            } catch (Exception ignored) {
            }
            break;
        }

        throw new UnsupportedOperationException("LWJGL requires sun.misc.Unsafe to be available.");
    }
}
