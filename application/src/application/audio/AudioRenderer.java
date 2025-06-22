package application.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.util.generator.openal.ALenum;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.openal.SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT;
import static org.lwjgl.system.MemoryUtil.*;

public class AudioRenderer implements AutoCloseable {
    private static final int BUFFER_SIZE = 1024 * 8;

    private VorbisTrack track;

    private final int format;

    private final long device;
    private final long context;

    private int       source;
    private IntBuffer buffers;

    private ShortBuffer pcm;

    long bufferOffset; // offset of last processed buffer
    long offset; // bufferOffset + offset of current buffer
    long lastOffset; // last offset update

    public AudioRenderer(VorbisTrack track) {
        this.track = track;

        switch (track.channels) {
            case 1:
                this.format = AL_FORMAT_MONO16;
                break;
            case 2:
                this.format = AL_FORMAT_STEREO16;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported number of channels: " + track.channels);
        }

        device = alcOpenDevice((ByteBuffer)null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default device.");
        }

        context = alcCreateContext(device, (IntBuffer)null);
        if (context == NULL) {
            throw new IllegalStateException("Failed to create an OpenAL context.");
        }

        this.pcm = memAllocShort(BUFFER_SIZE);

        alcSetThreadContext(context);

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        AL.createCapabilities(deviceCaps);

        checkError();
        source = alGenSources();
        checkError();
        alSourcei(source, AL_DIRECT_CHANNELS_SOFT, AL_TRUE);
        checkError();

        buffers = memAllocInt(2);
        alGenBuffers(buffers);
        checkError();
    }

    @Override
    public void close() {
        alDeleteBuffers(buffers);
        alDeleteSources(source);

        memFree(buffers);
        memFree(pcm);

        alcSetThreadContext(NULL);
        alcDestroyContext(context);
        alcCloseDevice(device);
        
        track = null;
        buffers = null;
        pcm = null;
    }

    private int stream(int buffer) {
        int samples = 0;

        while (samples < BUFFER_SIZE) {
            pcm.position(samples);
            int samplesPerChannel = track.getSamples(pcm);
            if (samplesPerChannel == 0) {
                break;
            }

            samples += samplesPerChannel * track.channels;
        }

        if (samples != 0) {
            pcm.position(0);
            pcm.limit(samples);
            alBufferData(buffer, format, pcm, track.sampleRate);
            checkError();
            pcm.limit(BUFFER_SIZE);
        }

        return samples;
    }

    public boolean play() {
        for (int i = 0; i < buffers.limit(); i++) {
            if (stream(buffers.get(i)) == 0) {
                return false;
            }
        }

        alSourceQueueBuffers(source, buffers);
        checkError();
        alSourcePlay(source);
        checkError();

        return true;
    }

    public boolean update(boolean loop) {
        int processed = alGetSourcei(source, AL_BUFFERS_PROCESSED);
        checkError();

        for (int i = 0; i < processed; i++) {
            bufferOffset += BUFFER_SIZE / track.channels;

            int buffer = alSourceUnqueueBuffers(source);
            checkError();

            if (stream(buffer) == 0) {
                boolean shouldExit = true;

                if (loop) {
                    track.rewind();
                    lastOffset = offset = bufferOffset = 0;
                    shouldExit = stream(buffer) == 0;
                }

                if (shouldExit) {
                    return false;
                }
            }
            alSourceQueueBuffers(source, buffer);
            checkError();
        }

        if (processed == 2) {
            alSourcePlay(source);
            checkError();
        }

        return true;
    }

    public ProgressUpdater getProgressUpdater() {
        return new ProgressUpdater() {
            @Override
            public void makeCurrent(boolean current) {
                alcSetThreadContext(current ? context : NULL);
            }

            @Override
            public void updateProgress() {
                offset = bufferOffset + alGetSourcei(source, AL_SAMPLE_OFFSET);
                track.progressBy((int)(offset - lastOffset));
                lastOffset = offset;
            }
        };
    }

    public void setVolume(float newVolume) {

        checkError(1);
        alDeleteSources(source);
        checkError(2);
        source = alGenSources();
        checkError(3);
        alSourcei(source, AL_DIRECT_CHANNELS_SOFT, AL_TRUE);

        checkError(4);
        alSourcef(source, AL_GAIN, newVolume);

        checkError(5);
        alSourceQueueBuffers(source, buffers);
        checkError(6);
        alSourcePlay(source);
        checkError(7);
    }
    private void checkError() {
        checkError(0);
    }
    private void checkError(int code) {

        int ALerror = AL_NO_ERROR;
        ALerror = alGetError();
        if (ALerror != AL_NO_ERROR) {
            System.err.println("ALError " + code + " " +getALErrorString(ALerror));
        }
    }
    private String getALErrorString(int err) {
        switch(err) {
            case AL_NO_ERROR:       return ("AL_NO_ERROR - (No error).");
            case AL_INVALID_NAME:       return ("AL_INVALID_NAME - Invalid Name paramater passed to AL call.");
            case AL_INVALID_ENUM:       return ("AL_INVALID_ENUM - Invalid parameter passed to AL call.");
            case AL_INVALID_VALUE:      return ("AL_INVALID_VALUE - Invalid enum parameter value.");
            case AL_INVALID_OPERATION:  return ("AL_INVALID_OPERATION");
            case AL_OUT_OF_MEMORY:      return ("AL_OUT_OF_MEMORY");
            default:            return ("AL Unknown Error.");
        }
    }}
