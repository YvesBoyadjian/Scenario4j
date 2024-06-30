package application.audio;

public interface ProgressUpdater {
    void makeCurrent(boolean current);
    void updateProgress();
}
