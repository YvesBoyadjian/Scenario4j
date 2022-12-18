package application.scenegraph;

public class Counter {

    private int count;

    public int count() {
        return count;
    }

    public void increment() {
        count++;
    }

    public void reset() {
        count = 0;
    }
}
