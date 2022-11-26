package jscenegraph.port.memorybuffer;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class MemoryBufferPool {

    private static final SortedSet<ByteBuffer> s = Collections.synchronizedSortedSet(new TreeSet(new Comparator<ByteBuffer>() {
        @Override
        public int compare(ByteBuffer o1, ByteBuffer o2) {
            int c1 = o1.capacity();
            int c2 = o2.capacity();
            if (c1 == c2) {
                return 0;
            }
            return c1 < c2 ? -1 : 1;
        }
    }));

    public static final ByteBuffer createByteBuffer(int numBytes) {
        for(ByteBuffer byteBuffer : s) {
            int c = byteBuffer.capacity();
            if (c >= numBytes) {
                if (numBytes * 120 / 100 >= c) {
                    s.remove(byteBuffer);
                    byteBuffer.position(0);
                    byteBuffer.limit(numBytes);
                    return byteBuffer;
                }
                break;
            }
        }
        return BufferUtils.createByteBuffer(numBytes);
    }

    /**
     *
     * @param byteBuffer
     */
    public static final void deleteByteBuffer(ByteBuffer byteBuffer) {
        s.add(byteBuffer);
    }
}
