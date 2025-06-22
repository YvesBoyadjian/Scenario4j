package application.audio;

import static java.lang.Thread.sleep;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Player {

    volatile Thread seaThread;

    AudioRenderer seaRenderer;

    CountDownLatch seaAudioLatch = new CountDownLatch(1);
    
    public void play(String seaPathString, float volume) {

        AtomicInteger seaAtomicInteger = new AtomicInteger();

        //String seaPathString = "ressource/AMBSea_Falaise 2 (ID 2572)_LS_120_70_Audacity_Quality_6.ogg";

        Path seaPath = Path.of(seaPathString);
        if (!seaPath.toFile().exists()) {
            seaPath = Path.of("application", seaPath.toString());
        }

        VorbisTrack seaTrack = new VorbisTrack(seaPath.toString(), seaAtomicInteger, false);

        final float[] currentSeaVolume = new float[1];

        seaThread = new Thread(() -> {

            seaRenderer = new AudioRenderer(seaTrack);

            seaRenderer.play();

            seaRenderer.setVolume(0.001f);

            seaAudioLatch.countDown();

            while (seaRenderer.update(false)) {
                try {
                    sleep(1000 / 30);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (currentSeaVolume[0] != volume) {
                    currentSeaVolume[0] = volume;
                    seaRenderer.setVolume(volume);
                }
            }
            seaRenderer.close();
            seaTrack.close();
            
            seaThread = null;
            seaRenderer = null;
            seaAudioLatch = null;
        }) {

        };
        seaThread.start();

        try {
            seaAudioLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    		
    }

}
