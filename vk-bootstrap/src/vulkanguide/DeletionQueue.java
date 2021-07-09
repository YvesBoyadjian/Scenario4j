package vulkanguide;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class DeletionQueue {
    public final Deque<Runnable> deletors = new ArrayDeque<>();

    void push_function(Runnable function) {
        deletors.push(function);
    }

    void flush() {
        // reverse iterate the deletion queue to execute all the functions
        while ( !deletors.isEmpty() ) {
            deletors.pop().run(); //call functors
        }

        deletors.clear();
    }
}
