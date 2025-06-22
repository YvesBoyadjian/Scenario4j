package application.audio;

import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import org.lwjgl.glfw.*;
import org.lwjgl.openal.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.*;
import org.lwjgl.system.*;
import org.lwjgl.system.windows.*;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static application.util.IOUtil.ioResourceToByteBuffer;
import static java.lang.Math.*;
//import static org.lwjgl.demo.util.IOUtil.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTThreadLocalContext.*;
import static org.lwjgl.openal.SOFTDirectChannels.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBEasyFont.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.windows.User32.*;

import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.system.MemoryStack.stackPush;

public class VorbisTrack implements AutoCloseable {
    private ByteBuffer encodedAudio;

    private final long handle;

    final int channels;
    final int sampleRate;

    final int   samplesLength;
    final float samplesSec;

    private final AtomicInteger sampleIndex;

    public VorbisTrack(String filePath, AtomicInteger sampleIndex) {
        try {
            encodedAudio = ioResourceToByteBuffer(filePath, 256 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            handle = stb_vorbis_open_memory(encodedAudio, error, null);
            if (handle == NULL) {
                throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
            }

            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            print(info);
            this.channels = info.channels();
            this.sampleRate = info.sample_rate();
        }

        this.samplesLength = stb_vorbis_stream_length_in_samples(handle);
        this.samplesSec = stb_vorbis_stream_length_in_seconds(handle);

        this.sampleIndex = sampleIndex;
        sampleIndex.set(0);
    }

    @Override
    public void close() {
        stb_vorbis_close(handle);
        encodedAudio = null;
    }

    void progressBy(int samples) {
        sampleIndex.set(sampleIndex.get() + samples);
    }

    void setSampleIndex(int sampleIndex) {
        this.sampleIndex.set(sampleIndex);
    }

    void rewind() {
        seek(0);
    }

    void skip(int direction) {
        seek(min(max(0, stb_vorbis_get_sample_offset(handle) + direction * sampleRate), samplesLength));
    }

    void skipTo(float offset0to1) {
        seek(round(samplesLength * offset0to1));
    }

    // called from audio thread
    synchronized int getSamples(ShortBuffer pcm) {
        return stb_vorbis_get_samples_short_interleaved(handle, channels, pcm);
    }

    // called from UI thread
    private synchronized void seek(int sampleIndex) {
        stb_vorbis_seek(handle, sampleIndex);
        setSampleIndex(sampleIndex);
    }

    private void print(STBVorbisInfo info) {
        System.out.println("stream length, samples: " + stb_vorbis_stream_length_in_samples(handle));
        System.out.println("stream length, seconds: " + stb_vorbis_stream_length_in_seconds(handle));

        System.out.println();

        stb_vorbis_get_info(handle, info);

        System.out.println("channels = " + info.channels());
        System.out.println("sampleRate = " + info.sample_rate());
        System.out.println("maxFrameSize = " + info.max_frame_size());
        System.out.println("setupMemoryRequired = " + info.setup_memory_required());
        System.out.println("setupTempMemoryRequired() = " + info.setup_temp_memory_required());
        System.out.println("tempMemoryRequired = " + info.temp_memory_required());
    }
}
