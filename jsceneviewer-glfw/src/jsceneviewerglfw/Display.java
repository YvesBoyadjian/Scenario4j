/**
 * 
 */
package jsceneviewerglfw;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.util.Point;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import javax.swing.*;
import java.nio.*;
import java.time.Instant;
import java.util.*;
import java.util.function.BinaryOperator;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
/**
 * @author Yves Boyadjian
 *
 */
public class Display {

	public void unregister(Composite composite) {
		composites.remove(composite);
	}

	private static class FutureEvent {
		static long globalId;
		double startTimeMicroseconds;
		long id;
		Runnable doit;
	}
	
	static Display current;
	
	Set<Composite> composites = Collections.newSetFromMap(new WeakHashMap<Composite,Boolean>());
	
	final Comparator<FutureEvent> comparator = new Comparator<FutureEvent>() {

		@Override
		public int compare(FutureEvent o1, FutureEvent o2) {
			double t1 = o1.startTimeMicroseconds;
			double t2 = o2.startTimeMicroseconds;
			if( t1 == t2 ) {
				if (o1.id == o2.id) {
					return 0;
				}
				return o1.id < o2.id ? -1 : 1;
			}
			return t1 < t2 ? -1 : 1;
		}
		
	};
	
	final SortedSet<FutureEvent> timersQueue = new TreeSet<>(comparator);
	
	public Display() {
		current = this;
		
		// Setup an error callback. The default implementation
				// will print the error message in System.err.
				GLFWErrorCallback.createPrint(System.err).set();

				// Initialize GLFW. Most GLFW functions will not work before doing this.
				if ( !glfwInit() )
					throw new IllegalStateException("Unable to initialize GLFW");

				// Configure GLFW
				glfwDefaultWindowHints(); // optional, the current window hints are already the default
				glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
				glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable								
	}

	public static Display getCurrent() {
		if(current == null) {
			current = new Display();
		}
		return current;
	}

	public void timerExec(long microsec, Runnable object) {
		double currentTimeMicro = System.nanoTime()/1000.0;//Instant.now().toEpochMilli();
		double startTimeMicro = currentTimeMicro + microsec;
		
		FutureEvent fe = new FutureEvent();
		fe.startTimeMicroseconds = startTimeMicro;
		fe.id = fe.globalId++;
		fe.doit = object;

		SwingUtilities.invokeLater(()->{
			timersQueue.add(fe);
		});
	}

	public Point getCursorLocation() {
		throw new UnsupportedOperationException("getCursorLocation");
	}

	public void dispose() {
		Set<Composite> compositesCopy = new HashSet<>(composites);
		compositesCopy.forEach(Composite::dispose);
		
		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	public void sleep() {
		// TODO Auto-generated method stub
		
	}

	public boolean shouldClose() {
		for(Composite composite : composites) {
			if (composite.shouldClose()) {
				return true;
			}
		}
		return false;
//		return composites.stream().map(Composite::shouldClose).reduce(false, new BinaryOperator<Boolean>() {
//
//			@Override
//			public Boolean apply(Boolean first, Boolean second) {
//				return first || second;
//			}
//
//		});
	}

	public /*boolean*/void readAndDispatch() {
		glfwPollEvents();

		double currentTimeMicro = System.nanoTime()/1000.0;//Instant.now().toEpochMilli();

		boolean treated;
		do {
			treated = false;

			FutureEvent fe = null;
			if (!timersQueue.isEmpty()) {
				fe = timersQueue.first();
			}
			if(fe != null) {
				if( fe.startTimeMicroseconds <= currentTimeMicro) {
					treated = true;
					timersQueue.remove(fe);
					fe.doit.run();
				}
			}
//				for(Long timerStartTime : timers.keySet()) {
//					if(timerStartTime <= currentTimeMicro) {
//						List<Runnable> runnables = timers.get(timerStartTime);
//						timers.remove(timerStartTime);
//						runnables.forEach(Runnable::run);
//						treated = true;
//						break;
//					}
//				}
		} while(treated);

		composites.forEach(Composite::loop);

//		    Thread t = new Thread(()->  {
//		    	System.gc();
//		    });
//		    t.start();


//			System.gc();
//			System.runFinalization();
	}
	
	public void register(Composite composite) {
		composites.add(composite);
	}
	
	public void loop() {
		while( !shouldClose()) {
			readAndDispatch();
		}
	}

	
}
