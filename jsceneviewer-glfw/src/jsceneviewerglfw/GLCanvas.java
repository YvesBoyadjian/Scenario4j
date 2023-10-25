/**
 * 
 */
package jsceneviewerglfw;

import jsceneviewerglfw.inventor.qt.SoQtGLWidget;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.util.Point;
import org.lwjgl.util.Rectangle;

import com.jogamp.opengl.GL2;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * @author Yves Boyadjian
 *
 */
public class GLCanvas extends Composite {

	// The window handle
	private long window;
	
	private GLData format;
		
	public GLCanvas(Composite parent, int style, GLData format) {
		super(parent, style);
		this.format = format;

		createWindow();
		//setCallbacks();
        }

		private void createWindow() {

			GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			int  width = vidMode.width();
			int  height = vidMode.height();

			if( format.majorVersion > 0 ) {
				glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, format.majorVersion);
			}
			if( format.minorVersion >= 0 ) {
				glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, format.minorVersion);
			}
			if( format.profile != null ) {
				glfwWindowHint(GLFW_OPENGL_PROFILE, format.profile ==  GLData.Profile.COMPATIBILITY ? GLFW_OPENGL_COMPAT_PROFILE : GLFW_OPENGL_CORE_PROFILE);
			}
			if( format.debug) {
				glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
			}
			glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE);
			if(format.depthSize > 0) {
				glfwWindowHint(GLFW_DEPTH_BITS, format.depthSize);
			}

			glfwWindowHint(GLFW_RED_BITS, /*vidMode.redBits()*/format.redSize);
			glfwWindowHint(GLFW_GREEN_BITS, /*vidMode.greenBits()*/format.greenSize);
			glfwWindowHint(GLFW_BLUE_BITS, /*vidMode.blueBits()*/format.blueSize);
			glfwWindowHint(GLFW_REFRESH_RATE, vidMode.refreshRate());

			if(format.accumRedSize > 0) glfwWindowHint(GLFW_ACCUM_RED_BITS, format.accumRedSize);
			if(format.accumGreenSize > 0) glfwWindowHint(GLFW_ACCUM_GREEN_BITS, format.accumGreenSize);
			if(format.accumBlueSize > 0) glfwWindowHint(GLFW_ACCUM_BLUE_BITS, format.accumBlueSize);

			//if( vidMode.redBits() == 10) {
			glfwWindowHint(GLFW_ALPHA_BITS, format.alphaSize != 0 ? format.alphaSize : GLFW_DONT_CARE);
			//}

			glfwWindowHint(GLFW_DECORATED, GLFW_FALSE); //makes flashing the screen in windows, but necessary for linux

			//glfwWindowHint(GLFW_SRGB_CAPABLE, GLFW_TRUE); this does not seem to have effect

			String os = System.getProperty("os.name");

			boolean win = os.startsWith("Windows");

			if (!win) {
				glfwWindowHint(GLFW_FLOATING, GLFW_TRUE); //seems to make the mouse hang on Windows with Intel Graphics HD 4000
			}

			//glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_FALSE);

			// Create the window
			window = glfwCreateWindow(width, height, format.name, /*win ? glfwGetPrimaryMonitor() :*/ NULL, NULL);
			if ( window == NULL )
				throw new RuntimeException("Failed to create the GLFW window");

			// Get the thread stack and push a new frame
			try ( MemoryStack stack = stackPush() ) {
				IntBuffer pWidth = stack.mallocInt(1); // int*
				IntBuffer pHeight = stack.mallocInt(1); // int*

				// Get the window size passed to glfwCreateWindow
				glfwGetWindowSize(window, pWidth, pHeight);

				// Get the resolution of the primary monitor
				GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

				// Center the window
				glfwSetWindowPos(
						window,
						(vidmode.width() - pWidth.get(0)) / 2,
						(vidmode.height() - pHeight.get(0)) / 2
				);
			} // the stack frame is popped automatically

			// Make the OpenGL context current
			glfwMakeContextCurrent(window);
			// Enable v-sync (or not)
			glfwSwapInterval(format.waitForRefresh ? 1 : 0);

			GL.createCapabilities();

			int[] r = new int[1];
			int[] g = new int[1];
			int[] b = new int[1];
			int[] d = new int[1];
			glGetFramebufferAttachmentParameteriv(GL_FRAMEBUFFER, GL_FRONT_LEFT, GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE, r);
			glGetFramebufferAttachmentParameteriv(GL_FRAMEBUFFER, GL_FRONT_LEFT, GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE, g);
			glGetFramebufferAttachmentParameteriv(GL_FRAMEBUFFER, GL_FRONT_LEFT, GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE, b);
			//glGetFramebufferAttachmentParameteriv(GL_FRAMEBUFFER, GL_FRONT_LEFT, GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE, d);
			int[] fwidth = new int[1];
			int[] fheight = new int[1];
			glfwGetFramebufferSize(window, fwidth, fheight);

			System.out.println("Definition : "+fwidth[0]+" x "+fheight[0]);
			System.out.println("RGB : "+ r[0]+" "+g[0]+" "+b[0]);
			//System.out.println("Depth : "+ d[0]);
			System.out.println("Refresh Rate : "+vidMode.refreshRate()+" Hz");

			// Make the window visible
			//glfwShowWindow(window);
			// Must be called by the parent
			//setVisible(true);


			glfwSetInputMode(window, GLFW_CURSOR, format.grabCursor ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_HIDDEN);
//        if ( format.grabCursor && glfwRawMouseMotionSupported()) does not work
//            glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);

			//swapBuffers();

		}

		void setCallbacks() {

			glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallbackI() {

				@Override
				public void invoke(long window, int width, int height) {
					resizeCB(width,height);
				}

			});

			glfwSetKeyCallback(window, (windowHnd, key, scancode, action, mods) -> {
				keyCB(key,scancode,action,mods);
			});

			glfwSetCursorPosCallback(window, new GLFWCursorPosCallbackI() {

				@Override
				public void invoke(long window, double xpos, double ypos) {

					if (!isVisible()) {
						return;
					}

					boolean left_button = false;

					int state = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT);
					if (state == GLFW_PRESS) {
						left_button = true;
					}

					boolean right_button = false;

					state = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT);
					if (state == GLFW_PRESS) {
						right_button = true;
					}
					MouseEvent me = new MouseEvent(xpos, ypos);
					if(left_button) {
						me.stateMask |= SWT.BUTTON1;
					}
					if(right_button) {
						me.stateMask |= SWT.BUTTON2;
					}
					mouseMoveCB(me);
				}

			});

			glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallbackI() {

				@Override
				public void invoke(long window, int button, int action, int mods) {
					mouseClickCB(button,action,mods);
				}

			});

			glfwSetCursorEnterCallback(window, new GLFWCursorEnterCallbackI() {

				@Override
				public void invoke(long window, boolean entered) {
					mouseEnterCB(entered);
				}

			});
		}
	
	public void setCurrent() {
		glfwMakeContextCurrent(window);
	}

	public boolean setFocus () {
		if (!isVisible()) {
			return false;
		}
		glfwFocusWindow(window);
		return true;
	}
	
	public GLData getGLData() {
		return format;
	}

	public void swapBuffers() {
		if (isVisible()) {
			glfwSwapBuffers(window);
		}
	}

	public long getWindow() {
		return window;
	}

	public Point getSize (){
		IntBuffer sizex = BufferUtils.createIntBuffer(1);
		IntBuffer sizey = BufferUtils.createIntBuffer(1);
		glfwGetWindowSize(window,sizex,sizey);
		return new Point(sizex.get(),sizey.get());
	}

	public Rectangle getBounds (){		
		
		IntBuffer posx = BufferUtils.createIntBuffer(1);
		IntBuffer posy = BufferUtils.createIntBuffer(1);
		glfwGetWindowPos(window,posx,posy);
		IntBuffer sizex = BufferUtils.createIntBuffer(1);
		IntBuffer sizey = BufferUtils.createIntBuffer(1);
		glfwGetWindowSize(window,sizex,sizey);
		
		return new Rectangle(posx.get(),posy.get(),sizex.get(),sizey.get());
	}

	
	public boolean shouldClose() {
		if (0 == window) {
			return false;
		}
		return glfwWindowShouldClose(window) ;
	}
	
	public void maximize() {
		glfwMaximizeWindow(window);
	}

	private void destroyWindow() {
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwSetWindowShouldClose(window, true);
		glfwDestroyWindow(window);
		window = 0;
	}

	public void dispose() {
		destroyWindow();
		//glfwPollEvents();
		super.dispose();
	}

	public void setVisible (boolean visible) {
		if (visible) {
			super.setVisible(visible);
			if(0 != window) {
					glfwRestoreWindow(window);
					glfwShowWindow(window);
					glfwSetInputMode(window, GLFW_CURSOR, format.grabCursor ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_HIDDEN);
				setCallbacks();
			}
		}
		else {
			glfwFreeCallbacks(window);
			super.setVisible(visible);
			if(0 != window) {
				glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
				glfwHideWindow(window);
				glfwIconifyWindow(window);
			}
			//dispose();
		}
	}

	public boolean isVisible() {
		if( 0 != window ) {
			long visible = glfwGetWindowAttrib(window,GLFW_VISIBLE);
			long iconified = glfwGetWindowAttrib(window,GLFW_ICONIFIED);
			return visible != 0 && iconified == 0;
		}
		else {
			return super.isVisible();
		}
	}

	public boolean isDisposed() {
		return window == 0;
	}
}
