package org.lwjgl.opengl.swt;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBMultisample;
import org.lwjgl.opengl.ARBRobustness;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.KHRNoError;
import org.lwjgl.opengl.NVMultisampleCoverage;
import org.lwjgl.opengl.WGL;
import org.lwjgl.opengl.WGLARBContextFlushControl;
import org.lwjgl.opengl.WGLARBCreateContext;
import org.lwjgl.opengl.WGLARBCreateContextNoError;
import org.lwjgl.opengl.WGLARBCreateContextProfile;
import org.lwjgl.opengl.WGLARBCreateContextRobustness;
import org.lwjgl.opengl.WGLARBMultisample;
import org.lwjgl.opengl.WGLARBPixelFormat;
import org.lwjgl.opengl.WGLARBPixelFormatFloat;
import org.lwjgl.opengl.WGLARBRobustnessApplicationIsolation;
import org.lwjgl.opengl.WGLEXTCreateContextES2Profile;
import org.lwjgl.opengl.WGLEXTFramebufferSRGB;
import org.lwjgl.opengl.WGLNVMultisampleCoverage;
import org.lwjgl.opengl.swt.GLData.API;
import org.lwjgl.opengl.swt.GLData.Profile;
import org.lwjgl.opengl.swt.GLData.ReleaseBehavior;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.APIUtil.APIVersion;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.windows.GDI32;
import org.lwjgl.system.windows.PIXELFORMATDESCRIPTOR;
import org.lwjgl.system.windows.User32;

/**
 * Windows-specific implementation of methods for GLCanvas.
 * 
 * @author Kai Burjack
 */
class PlatformWin32GLCanvas extends AbstractPlatformGLCanvas {
    private static final String USE_OWNDC_KEY = "org.eclipse.swt.internal.win32.useOwnDC";

    private long wglDelayBeforeSwapNVAddr = 0L;
    private boolean wglDelayBeforeSwapNVAddr_set = false;

    /**
     * Encode the pixel format attributes stored in the given {@link GLData} into the given {@link IntBuffer} for wglChoosePixelFormatARB to
     * consume.
     */
    private void encodePixelFormatAttribs(IntBuffer ib, GLData attribs) {
        ib.put(WGLARBPixelFormat.WGL_DRAW_TO_WINDOW_ARB).put(1);
        ib.put(WGLARBPixelFormat.WGL_SUPPORT_OPENGL_ARB).put(1);
        ib.put(WGLARBPixelFormat.WGL_ACCELERATION_ARB).put(WGLARBPixelFormat.WGL_FULL_ACCELERATION_ARB);
        if (attribs.doubleBuffer)
            ib.put(WGLARBPixelFormat.WGL_DOUBLE_BUFFER_ARB).put(1);
        if (attribs.pixelFormatFloat)
            ib.put(WGLARBPixelFormat.WGL_PIXEL_TYPE_ARB).put(WGLARBPixelFormatFloat.WGL_TYPE_RGBA_FLOAT_ARB);
        else
            ib.put(WGLARBPixelFormat.WGL_PIXEL_TYPE_ARB).put(WGLARBPixelFormat.WGL_TYPE_RGBA_ARB);
        if (attribs.redSize > 0)
            ib.put(WGLARBPixelFormat.WGL_RED_BITS_ARB).put(attribs.redSize);
        if (attribs.greenSize > 0)
            ib.put(WGLARBPixelFormat.WGL_GREEN_BITS_ARB).put(attribs.greenSize);
        if (attribs.blueSize > 0)
            ib.put(WGLARBPixelFormat.WGL_BLUE_BITS_ARB).put(attribs.blueSize);
        if (attribs.alphaSize > 0)
            ib.put(WGLARBPixelFormat.WGL_ALPHA_BITS_ARB).put(attribs.alphaSize);
        if (attribs.depthSize > 0)
            ib.put(WGLARBPixelFormat.WGL_DEPTH_BITS_ARB).put(attribs.depthSize);
        if (attribs.stencilSize > 0)
            ib.put(WGLARBPixelFormat.WGL_STENCIL_BITS_ARB).put(attribs.stencilSize);
        if (attribs.accumRedSize > 0)
            ib.put(WGLARBPixelFormat.WGL_ACCUM_RED_BITS_ARB).put(attribs.accumRedSize);
        if (attribs.accumGreenSize > 0)
            ib.put(WGLARBPixelFormat.WGL_ACCUM_GREEN_BITS_ARB).put(attribs.accumGreenSize);
        if (attribs.accumBlueSize > 0)
            ib.put(WGLARBPixelFormat.WGL_ACCUM_BLUE_BITS_ARB).put(attribs.accumBlueSize);
        if (attribs.accumAlphaSize > 0)
            ib.put(WGLARBPixelFormat.WGL_ACCUM_ALPHA_BITS_ARB).put(attribs.accumAlphaSize);
        if (attribs.accumRedSize > 0 || attribs.accumGreenSize > 0 || attribs.accumBlueSize > 0 || attribs.accumAlphaSize > 0)
            ib.put(WGLARBPixelFormat.WGL_ACCUM_BITS_ARB).put(attribs.accumRedSize + attribs.accumGreenSize + attribs.accumBlueSize + attribs.accumAlphaSize);
        if (attribs.sRGB)
            ib.put(WGLEXTFramebufferSRGB.WGL_FRAMEBUFFER_SRGB_CAPABLE_EXT).put(1);
        if (attribs.samples > 0) {
            ib.put(WGLARBMultisample.WGL_SAMPLE_BUFFERS_ARB).put(1);
            ib.put(WGLARBMultisample.WGL_SAMPLES_ARB).put(attribs.samples);
            if (attribs.colorSamplesNV > 0) {
                ib.put(WGLNVMultisampleCoverage.WGL_COLOR_SAMPLES_NV).put(attribs.colorSamplesNV);
            }
        }
        ib.put(0);
    }

    public int checkStyle(Composite parent, int style) {
        // Somehow we need to temporarily set 'org.eclipse.swt.internal.win32.useOwnDC'
        // to true or else context creation on Windows fails...
        if (parent != null) {
        	// Removed windows version check - SWT dropped support for windows before Vista
        	// https://github.com/eclipse/eclipse.platform.swt/commit/84c9e305cb087110cb300a5e58f86583cf80914d#diff-bb4584995e162b851fafacf3b046cc35
            parent.getDisplay().setData(USE_OWNDC_KEY, Boolean.TRUE);
        }
        return style;
    }

    public void resetStyle(Composite parent) {
        parent.getDisplay().setData(USE_OWNDC_KEY, Boolean.FALSE);
    }

    public long create(GLCanvas canvas, GLData attribs, GLData effective) {
        Canvas dummycanvas = new Canvas(canvas.getParent(), checkStyle(canvas.getParent(), canvas.getStyle()));
        long context = 0L;
        MemoryStack stack = MemoryStack.stackGet(); int ptr = stack.getPointer();
        try {
            context = create(canvas.handle, dummycanvas.handle, attribs, effective);
        } catch (SWTException e) {
            stack.setPointer(ptr);
            SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH, e);
        }
        final long finalContext = context;
        dummycanvas.dispose();
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Dispose:
                    deleteContext(canvas, finalContext);
                    break;
                }
            }
        };
        canvas.addListener(SWT.Dispose, listener);
        return context;
    }

    private long create(long windowHandle, long dummyWindowHandle, GLData attribs, GLData effective) throws SWTException {
        MemoryStack stack = MemoryStack.stackGet();
        long bufferAddr = stack.nmalloc(4, (4*2) << 2);

        // Validate context attributes
        validateAttributes(attribs);

        // Find this exact pixel format, though for now without multisampling. This comes later!
        PIXELFORMATDESCRIPTOR pfd = PIXELFORMATDESCRIPTOR.callocStack();
        pfd.nSize((short) PIXELFORMATDESCRIPTOR.SIZEOF);
        pfd.nVersion((short) 1); // this should always be 1
        pfd.dwLayerMask(GDI32.PFD_MAIN_PLANE);
        pfd.iPixelType(GDI32.PFD_TYPE_RGBA);
        int flags = GDI32.PFD_DRAW_TO_WINDOW | GDI32.PFD_SUPPORT_OPENGL;
        if (attribs.doubleBuffer)
            flags |= GDI32.PFD_DOUBLEBUFFER;
        if (attribs.stereo)
            flags |= GDI32.PFD_STEREO;
        pfd.dwFlags(flags);
        pfd.cRedBits((byte) attribs.redSize);
        pfd.cGreenBits((byte) attribs.greenSize);
        pfd.cBlueBits((byte) attribs.blueSize);
        pfd.cAlphaBits((byte) attribs.alphaSize);
        pfd.cDepthBits((byte) attribs.depthSize);
        pfd.cStencilBits((byte) attribs.stencilSize);
        pfd.cAccumRedBits((byte) attribs.accumRedSize);
        pfd.cAccumGreenBits((byte) attribs.accumGreenSize);
        pfd.cAccumBlueBits((byte) attribs.accumBlueSize);
        pfd.cAccumAlphaBits((byte) attribs.accumAlphaSize);
        pfd.cAccumBits((byte) (attribs.accumRedSize + attribs.accumGreenSize + attribs.accumBlueSize + attribs.accumAlphaSize));
        long hDCdummy = User32.GetDC(dummyWindowHandle);
        int pixelFormat = GDI32.ChoosePixelFormat(null, hDCdummy, pfd);
        if (pixelFormat == 0 || !GDI32.SetPixelFormat(null, hDCdummy, pixelFormat, pfd)) {
            // Pixel format unsupported
            User32.ReleaseDC(dummyWindowHandle, hDCdummy);
            throw new SWTException("Unsupported pixel format");
        }

        /*
         * Next, create a dummy context using Opengl32.lib's wglCreateContext. This should ALWAYS work, but won't give us a "new"/"core" context if we requested
         * that and also does not support multisampling. But we use this "dummy" context then to request the required WGL function pointers to create a new
         * OpenGL >= 3.0 context and with optional multisampling.
         */
        long dummyContext = WGL.wglCreateContext(null, hDCdummy);
        if (dummyContext == 0L) {
            User32.ReleaseDC(dummyWindowHandle, hDCdummy);
            throw new SWTException("Failed to create OpenGL context");
        }

        // Save current context to restore it later
        final long currentContext = WGL.wglGetCurrentContext(null);
        final long currentDc = WGL.wglGetCurrentDC();

        // Make the new dummy context current
        boolean success = WGL.wglMakeCurrent(null, hDCdummy, dummyContext);
        if (!success) {
            User32.ReleaseDC(dummyWindowHandle, hDCdummy);
            WGL.wglDeleteContext(null, dummyContext);
            throw new SWTException("Failed to make OpenGL context current");
        }

        // Query supported WGL extensions
        String wglExtensions = null;
        long wglGetExtensionsStringARBAddr = WGL.wglGetProcAddress(null, "wglGetExtensionsStringARB");
        if (wglGetExtensionsStringARBAddr != 0L) {
            long str = JNI.callPP(hDCdummy, wglGetExtensionsStringARBAddr);
            if (str != 0L) {
                wglExtensions = MemoryUtil.memASCII(str);
            } else {
                wglExtensions = "";
            }
        } else {
            // Try the EXT extension
            long wglGetExtensionsStringEXTAddr = WGL.wglGetProcAddress(null, "wglGetExtensionsStringEXT");
            if (wglGetExtensionsStringEXTAddr != 0L) {
                long str = JNI.callP(wglGetExtensionsStringEXTAddr);
                if (str != 0L) {
                    wglExtensions = MemoryUtil.memASCII(str);
                } else {
                    wglExtensions = "";
                }
            } else {
                wglExtensions = "";
            }
        }
        String[] splitted = wglExtensions.split(" ");
        Set<String> wglExtensionsList = new HashSet<String>(splitted.length);
        for (String str : splitted) {
            wglExtensionsList.add(str);
        }
        success = User32.ReleaseDC(dummyWindowHandle, hDCdummy);
        if (!success) {
            WGL.wglDeleteContext(null, dummyContext);
            WGL.wglMakeCurrent(null, currentDc, currentContext);
            throw new SWTException("Could not release dummy DC");
        }

        // For some constellations of context attributes, we can stop right here.
        if (!atLeast30(attribs.majorVersion, attribs.minorVersion) && attribs.samples == 0 && !attribs.sRGB && !attribs.pixelFormatFloat
                && attribs.contextReleaseBehavior == null && !attribs.robustness && attribs.api != API.GLES) {
            /* Finally, create the real context on the real window */
            long hDC = User32.GetDC(windowHandle);
            GDI32.SetPixelFormat(null, hDC, pixelFormat, pfd);
            success = WGL.wglDeleteContext(null, dummyContext);
            if (!success) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                throw new SWTException("Could not delete dummy GL context");
            }
            long context = WGL.wglCreateContext(null, hDC);

            if (attribs.swapInterval != null) {
                boolean has_WGL_EXT_swap_control = wglExtensionsList.contains("WGL_EXT_swap_control");
                if (!has_WGL_EXT_swap_control) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    WGL.wglDeleteContext(null, context);
                    throw new SWTException("Swap interval requested but WGL_EXT_swap_control is unavailable");
                }
                if (attribs.swapInterval < 0) {
                    // Only allowed if WGL_EXT_swap_control_tear is available
                    boolean has_WGL_EXT_swap_control_tear = wglExtensionsList.contains("WGL_EXT_swap_control_tear");
                    if (!has_WGL_EXT_swap_control_tear) {
                        User32.ReleaseDC(windowHandle, hDC);
                        WGL.wglMakeCurrent(null, currentDc, currentContext);
                        WGL.wglDeleteContext(null, context);
                        throw new SWTException("Negative swap interval requested but WGL_EXT_swap_control_tear is unavailable");
                    }
                }
                // Make context current to set the swap interval
                success = WGL.wglMakeCurrent(null, hDC, context);
                if (!success) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    WGL.wglDeleteContext(null, context);
                    throw new SWTException("Could not make GL context current");
                }
                long wglSwapIntervalEXTAddr = WGL.wglGetProcAddress(null, "wglSwapIntervalEXT");
                if (wglSwapIntervalEXTAddr != 0L) {
                    JNI.callI(attribs.swapInterval, wglSwapIntervalEXTAddr);
                }
            }

            if (attribs.swapGroupNV > 0 || attribs.swapBarrierNV > 0) {
                // Only allowed if WGL_NV_swap_group is available
                boolean has_WGL_NV_swap_group = wglExtensionsList.contains("WGL_NV_swap_group");
                if (!has_WGL_NV_swap_group) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    WGL.wglDeleteContext(null, context);
                    throw new SWTException("Swap group or barrier requested but WGL_NV_swap_group is unavailable");
                }
                // Make context current to join swap group and/or barrier
                success = WGL.wglMakeCurrent(null, hDC, context);
                try {
                    wglNvSwapGroupAndBarrier(attribs, stack.getAddress() + stack.getPointer(), hDC);
                } catch (SWTException e) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    WGL.wglDeleteContext(null, context);
                    throw e;
                }
            }

            /* Check if we want to share context */
            if (attribs.shareContext != null) {
                success = WGL.wglShareLists(null, context, attribs.shareContext.context);
                if (!success) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    WGL.wglDeleteContext(null, context);
                    throw new SWTException("Failed while configuring context sharing");
                }
            }

            // Describe pixel format
            int pixFmtIndex = GDI32.DescribePixelFormat(null, hDC, pixelFormat, pfd);
            if (pixFmtIndex == 0) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                WGL.wglDeleteContext(null, context);
                throw new SWTException("Failed to describe pixel format");
            }
            success = User32.ReleaseDC(windowHandle, hDC);
            if (!success) {
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                WGL.wglDeleteContext(null, context);
                throw new SWTException("Could not release DC");
            }
            effective.redSize = pfd.cRedBits();
            effective.greenSize = pfd.cGreenBits();
            effective.blueSize = pfd.cBlueBits();
            effective.alphaSize = pfd.cAlphaBits();
            effective.depthSize = pfd.cDepthBits();
            effective.stencilSize = pfd.cStencilBits();
            int pixelFormatFlags = pfd.dwFlags();
            effective.doubleBuffer = (pixelFormatFlags & GDI32.PFD_DOUBLEBUFFER) != 0;
            effective.stereo = (pixelFormatFlags & GDI32.PFD_STEREO) != 0;
            effective.accumRedSize = pfd.cAccumRedBits();
            effective.accumGreenSize = pfd.cAccumGreenBits();
            effective.accumBlueSize = pfd.cAccumBlueBits();
            effective.accumAlphaSize = pfd.cAccumAlphaBits();

            // Restore old context
            WGL.wglMakeCurrent(null, currentDc, currentContext);
            return context;
        }

        // Check for WGL_ARB_create_context support
        if (!wglExtensionsList.contains("WGL_ARB_create_context")) {
            WGL.wglDeleteContext(null, dummyContext);
            WGL.wglMakeCurrent(null, currentDc, currentContext);
            throw new SWTException("Extended context attributes requested but WGL_ARB_create_context is unavailable");
        }

        // Obtain wglCreateContextAttribsARB function pointer
        long wglCreateContextAttribsARBAddr = WGL.wglGetProcAddress(null, "wglCreateContextAttribsARB");
        if (wglCreateContextAttribsARBAddr == 0L) {
            WGL.wglDeleteContext(null, dummyContext);
            WGL.wglMakeCurrent(null, currentDc, currentContext);
            throw new SWTException("WGL_ARB_create_context available but wglCreateContextAttribsARB is NULL");
        }

        IntBuffer attribList = BufferUtils.createIntBuffer(64);
        long attribListAddr = MemoryUtil.memAddress(attribList);
        long hDC = User32.GetDC(windowHandle);

        // Obtain wglChoosePixelFormatARB if multisampling or sRGB or floating point pixel format is requested
        if (attribs.samples > 0 || attribs.sRGB || attribs.pixelFormatFloat) {
            long wglChoosePixelFormatAddr = WGL.wglGetProcAddress(null, "wglChoosePixelFormatARB");
            if (wglChoosePixelFormatAddr == 0L) {
                // Try EXT function (the WGL constants are the same in both extensions)
                wglChoosePixelFormatAddr = WGL.wglGetProcAddress(null, "wglChoosePixelFormatEXT");
                if (wglChoosePixelFormatAddr == 0L) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglDeleteContext(null, dummyContext);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    throw new SWTException("No support for wglChoosePixelFormatARB/EXT. Cannot query supported pixel formats.");
                }
            }
            if (attribs.samples > 0) {
                // Check for ARB or EXT extension (their WGL constants have the same value)
                boolean has_WGL_ARB_multisample = wglExtensionsList.contains("WGL_ARB_multisample");
                boolean has_WGL_EXT_multisample = wglExtensionsList.contains("WGL_EXT_multisample");
                if (!has_WGL_ARB_multisample && !has_WGL_EXT_multisample) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglDeleteContext(null, dummyContext);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    throw new SWTException("Multisampling requested but neither WGL_ARB_multisample nor WGL_EXT_multisample available");
                }
                if (attribs.colorSamplesNV > 0) {
                    boolean has_WGL_NV_multisample_coverage = wglExtensionsList.contains("WGL_NV_multisample_coverage");
                    if (!has_WGL_NV_multisample_coverage) {
                        User32.ReleaseDC(windowHandle, hDC);
                        WGL.wglDeleteContext(null, dummyContext);
                        WGL.wglMakeCurrent(null, currentDc, currentContext);
                        throw new SWTException("Color samples requested but WGL_NV_multisample_coverage is unavailable");
                    }
                }
            }
            if (attribs.sRGB) {
                // Check for WGL_EXT_framebuffer_sRGB
                boolean has_WGL_EXT_framebuffer_sRGB = wglExtensionsList.contains("WGL_EXT_framebuffer_sRGB");
                if (!has_WGL_EXT_framebuffer_sRGB) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglDeleteContext(null, dummyContext);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    throw new SWTException("sRGB color space requested but WGL_EXT_framebuffer_sRGB is unavailable");
                }
            }
            if (attribs.pixelFormatFloat) {
                // Check for WGL_ARB_pixel_format_float
                boolean has_WGL_ARB_pixel_format_float = wglExtensionsList.contains("WGL_ARB_pixel_format_float");
                if (!has_WGL_ARB_pixel_format_float) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglDeleteContext(null, dummyContext);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    throw new SWTException("Floating-point format requested but WGL_ARB_pixel_format_float is unavailable");
                }
            }
            // Query matching pixel formats
            encodePixelFormatAttribs(attribList, attribs);
            success = JNI.callPPPPPI(hDC, attribListAddr, 0L, 1, bufferAddr + 4, bufferAddr, wglChoosePixelFormatAddr) == 1;
            int numFormats = MemoryUtil.memGetInt(bufferAddr);
            if (!success || numFormats == 0) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglDeleteContext(null, dummyContext);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                throw new SWTException("No supported pixel format found.");
            }
            pixelFormat = MemoryUtil.memGetInt(bufferAddr + 4);
            // Describe pixel format for the PIXELFORMATDESCRIPTOR to match the chosen format
            int pixFmtIndex = GDI32.DescribePixelFormat(null, hDC, pixelFormat, pfd);
            if (pixFmtIndex == 0) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglDeleteContext(null, dummyContext);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                throw new SWTException("Failed to validate supported pixel format.");
            }
            // Obtain extended pixel format attributes
            long wglGetPixelFormatAttribivAddr = WGL.wglGetProcAddress(null, "wglGetPixelFormatAttribivARB");
            if (wglGetPixelFormatAttribivAddr == 0L) {
                // Try EXT function (function signature is the same)
                wglGetPixelFormatAttribivAddr = WGL.wglGetProcAddress(null, "wglGetPixelFormatAttribivEXT");
                if (wglGetPixelFormatAttribivAddr == 0L) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglDeleteContext(null, dummyContext);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    throw new SWTException("No support for wglGetPixelFormatAttribivARB/EXT. Cannot get effective pixel format attributes.");
                }
            }
            attribList.rewind();
            attribList.put(WGLARBPixelFormat.WGL_DOUBLE_BUFFER_ARB);
            attribList.put(WGLARBPixelFormat.WGL_STEREO_ARB);
            attribList.put(WGLARBPixelFormat.WGL_PIXEL_TYPE_ARB);
            attribList.put(WGLARBPixelFormat.WGL_RED_BITS_ARB);
            attribList.put(WGLARBPixelFormat.WGL_GREEN_BITS_ARB);
            attribList.put(WGLARBPixelFormat.WGL_BLUE_BITS_ARB);
            attribList.put(WGLARBPixelFormat.WGL_ALPHA_BITS_ARB);
            attribList.put(WGLARBPixelFormat.WGL_ACCUM_RED_BITS_ARB);
            attribList.put(WGLARBPixelFormat.WGL_ACCUM_GREEN_BITS_ARB);
            attribList.put(WGLARBPixelFormat.WGL_ACCUM_BLUE_BITS_ARB);
            attribList.put(WGLARBPixelFormat.WGL_ACCUM_ALPHA_BITS_ARB);
            attribList.put(WGLARBPixelFormat.WGL_DEPTH_BITS_ARB);
            attribList.put(WGLARBPixelFormat.WGL_STENCIL_BITS_ARB);
            IntBuffer attribValues = BufferUtils.createIntBuffer(attribList.position());
            long attribValuesAddr = MemoryUtil.memAddress(attribValues);
            success = JNI.callPPPI(hDC, pixelFormat, GDI32.PFD_MAIN_PLANE, attribList.position(), attribListAddr,
                    attribValuesAddr, wglGetPixelFormatAttribivAddr) == 1;
            if (!success) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglDeleteContext(null, dummyContext);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                throw new SWTException("Failed to get pixel format attributes.");
            }
            effective.doubleBuffer = attribValues.get(0) == 1;
            effective.stereo = attribValues.get(1) == 1;
            int pixelType = attribValues.get(2);
            effective.pixelFormatFloat = pixelType == WGLARBPixelFormatFloat.WGL_TYPE_RGBA_FLOAT_ARB;
            effective.redSize = attribValues.get(3);
            effective.greenSize = attribValues.get(4);
            effective.blueSize = attribValues.get(5);
            effective.alphaSize = attribValues.get(6);
            effective.accumRedSize = attribValues.get(7);
            effective.accumGreenSize = attribValues.get(8);
            effective.accumBlueSize = attribValues.get(9);
            effective.accumAlphaSize = attribValues.get(10);
            effective.depthSize = attribValues.get(11);
            effective.stencilSize = attribValues.get(12);
        }

        // Compose the attributes list
        attribList.rewind();
        if (attribs.api == API.GL && atLeast30(attribs.majorVersion, attribs.minorVersion) || attribs.api == API.GLES && attribs.majorVersion > 0) {
            attribList.put(WGLARBCreateContext.WGL_CONTEXT_MAJOR_VERSION_ARB).put(attribs.majorVersion);
            attribList.put(WGLARBCreateContext.WGL_CONTEXT_MINOR_VERSION_ARB).put(attribs.minorVersion);
        }
        int profile = 0;
        if (attribs.api == API.GL) {
            if (attribs.profile == Profile.COMPATIBILITY) {
                profile = WGLARBCreateContextProfile.WGL_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB;
            } else if (attribs.profile == Profile.CORE) {
                profile = WGLARBCreateContextProfile.WGL_CONTEXT_CORE_PROFILE_BIT_ARB;
            }
        } else if (attribs.api == API.GLES) {
            boolean has_WGL_EXT_create_context_es2_profile = wglExtensionsList.contains("WGL_EXT_create_context_es2_profile");
            if (!has_WGL_EXT_create_context_es2_profile) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglDeleteContext(null, dummyContext);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                throw new SWTException("OpenGL ES API requested but WGL_EXT_create_context_es2_profile is unavailable");
            }
            profile = WGLEXTCreateContextES2Profile.WGL_CONTEXT_ES2_PROFILE_BIT_EXT;
        }
        if (profile > 0) {
            boolean has_WGL_ARB_create_context_profile = wglExtensionsList.contains("WGL_ARB_create_context_profile");
            if (!has_WGL_ARB_create_context_profile) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglDeleteContext(null, dummyContext);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                throw new SWTException("OpenGL profile requested but WGL_ARB_create_context_profile is unavailable");
            }
            attribList.put(WGLARBCreateContextProfile.WGL_CONTEXT_PROFILE_MASK_ARB).put(profile);
        }
        int contextFlags = 0;
        if (attribs.debug) {
            contextFlags |= WGLARBCreateContext.WGL_CONTEXT_DEBUG_BIT_ARB;
        }
        if (attribs.forwardCompatible) {
            contextFlags |= WGLARBCreateContext.WGL_CONTEXT_FORWARD_COMPATIBLE_BIT_ARB;
        }
        if (attribs.noErrorContext) {
            contextFlags |= KHRNoError.GL_CONTEXT_FLAG_NO_ERROR_BIT_KHR;
            attribList.put(WGLARBCreateContextNoError.WGL_CONTEXT_OPENGL_NO_ERROR_ARB).put(1);
        }
        if (attribs.robustness) {
            // Check for WGL_ARB_create_context_robustness
            boolean has_WGL_ARB_create_context_robustness = wglExtensions.contains("WGL_ARB_create_context_robustness");
            if (!has_WGL_ARB_create_context_robustness) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglDeleteContext(null, dummyContext);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                throw new SWTException("Context with robust buffer access requested but WGL_ARB_create_context_robustness is unavailable");
            }
            contextFlags |= WGLARBCreateContextRobustness.WGL_CONTEXT_ROBUST_ACCESS_BIT_ARB;
            if (attribs.loseContextOnReset) {
                attribList.put(WGLARBCreateContextRobustness.WGL_CONTEXT_RESET_NOTIFICATION_STRATEGY_ARB).put(
                        WGLARBCreateContextRobustness.WGL_LOSE_CONTEXT_ON_RESET_ARB);
                // Note: WGL_NO_RESET_NOTIFICATION_ARB is default behaviour and need not be specified.
            }
            if (attribs.contextResetIsolation) {
                // Check for WGL_ARB_robustness_application_isolation or WGL_ARB_robustness_share_group_isolation
                boolean has_WGL_ARB_robustness_application_isolation = wglExtensions.contains("WGL_ARB_robustness_application_isolation");
                boolean has_WGL_ARB_robustness_share_group_isolation = wglExtensions.contains("WGL_ARB_robustness_share_group_isolation");
                if (!has_WGL_ARB_robustness_application_isolation && !has_WGL_ARB_robustness_share_group_isolation) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglDeleteContext(null, dummyContext);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    throw new SWTException(
                            "Robustness isolation requested but neither WGL_ARB_robustness_application_isolation nor WGL_ARB_robustness_share_group_isolation available");
                }
                contextFlags |= WGLARBRobustnessApplicationIsolation.WGL_CONTEXT_RESET_ISOLATION_BIT_ARB;
            }
        }
        if (contextFlags > 0)
            attribList.put(WGLARBCreateContext.WGL_CONTEXT_FLAGS_ARB).put(contextFlags);
        if (attribs.contextReleaseBehavior != null) {
            boolean has_WGL_ARB_context_flush_control = wglExtensionsList.contains("WGL_ARB_context_flush_control");
            if (!has_WGL_ARB_context_flush_control) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglDeleteContext(null, dummyContext);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                throw new SWTException("Context release behavior requested but WGL_ARB_context_flush_control is unavailable");
            }
            if (attribs.contextReleaseBehavior == ReleaseBehavior.NONE)
                attribList.put(WGLARBContextFlushControl.WGL_CONTEXT_RELEASE_BEHAVIOR_ARB).put(WGLARBContextFlushControl.WGL_CONTEXT_RELEASE_BEHAVIOR_NONE_ARB);
            else if (attribs.contextReleaseBehavior == ReleaseBehavior.FLUSH)
                attribList.put(WGLARBContextFlushControl.WGL_CONTEXT_RELEASE_BEHAVIOR_ARB)
                        .put(WGLARBContextFlushControl.WGL_CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB);
        }
        attribList.put(0).put(0);
        // Set pixelformat
        success = GDI32.SetPixelFormat(null, hDC, pixelFormat, pfd);
        if (!success) {
            User32.ReleaseDC(windowHandle, hDC);
            WGL.wglDeleteContext(null, dummyContext);
            WGL.wglMakeCurrent(null, currentDc, currentContext);
            throw new SWTException("Failed to set pixel format.");
        }
        // And create new context with it
        long newCtx = JNI.callPPPP(hDC, attribs.shareContext != null ? attribs.shareContext.context : 0L, attribListAddr, wglCreateContextAttribsARBAddr);
        WGL.wglDeleteContext(null, dummyContext);
        if (newCtx == 0L) {
            User32.ReleaseDC(windowHandle, hDC);
            WGL.wglMakeCurrent(null, currentDc, currentContext);
            throw new SWTException("Failed to create OpenGL context.");
        }
        // Make context current for next operations
        WGL.wglMakeCurrent(null, hDC, newCtx);
        if (attribs.swapInterval != null) {
            boolean has_WGL_EXT_swap_control = wglExtensionsList.contains("WGL_EXT_swap_control");
            if (!has_WGL_EXT_swap_control) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                WGL.wglDeleteContext(null, newCtx);
                throw new SWTException("Swap interval requested but WGL_EXT_swap_control is unavailable");
            }
            if (attribs.swapInterval < 0) {
                // Only allowed if WGL_EXT_swap_control_tear is available
                boolean has_WGL_EXT_swap_control_tear = wglExtensionsList.contains("WGL_EXT_swap_control_tear");
                if (!has_WGL_EXT_swap_control_tear) {
                    User32.ReleaseDC(windowHandle, hDC);
                    WGL.wglMakeCurrent(null, currentDc, currentContext);
                    WGL.wglDeleteContext(null, newCtx);
                    throw new SWTException("Negative swap interval requested but WGL_EXT_swap_control_tear is unavailable");
                }
            }
            long wglSwapIntervalEXTAddr = WGL.wglGetProcAddress(null, "wglSwapIntervalEXT");
            if (wglSwapIntervalEXTAddr != 0L) {
                JNI.callI(attribs.swapInterval, wglSwapIntervalEXTAddr);
            }
        }
        if (attribs.swapGroupNV > 0 || attribs.swapBarrierNV > 0) {
            // Only allowed if WGL_NV_swap_group is available
            boolean has_WGL_NV_swap_group = wglExtensionsList.contains("WGL_NV_swap_group");
            if (!has_WGL_NV_swap_group) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                WGL.wglDeleteContext(null, newCtx);
                throw new SWTException("Swap group or barrier requested but WGL_NV_swap_group is unavailable");
            }
            try {
                wglNvSwapGroupAndBarrier(attribs, bufferAddr, hDC);
            } catch (SWTException e) {
                User32.ReleaseDC(windowHandle, hDC);
                WGL.wglMakeCurrent(null, currentDc, currentContext);
                WGL.wglDeleteContext(null, newCtx);
                throw e;
            }
        }
        User32.ReleaseDC(windowHandle, hDC);
        long getInteger = GL.getFunctionProvider().getFunctionAddress("glGetIntegerv");
        long getString = GL.getFunctionProvider().getFunctionAddress("glGetString");
        effective.api = attribs.api;
        if (atLeast30(attribs.majorVersion, attribs.minorVersion)) {
            JNI.callPV(GL30.GL_MAJOR_VERSION, bufferAddr, getInteger);
            effective.majorVersion = MemoryUtil.memGetInt(bufferAddr);
            JNI.callPV(GL30.GL_MINOR_VERSION, bufferAddr, getInteger);
            effective.minorVersion = MemoryUtil.memGetInt(bufferAddr);
            JNI.callPV(GL30.GL_CONTEXT_FLAGS, bufferAddr, getInteger);
            int effectiveContextFlags = MemoryUtil.memGetInt(bufferAddr);
            effective.debug = (effectiveContextFlags & GL43.GL_CONTEXT_FLAG_DEBUG_BIT) != 0;
            effective.forwardCompatible = (effectiveContextFlags & GL30.GL_CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT) != 0;
            effective.robustness = (effectiveContextFlags & ARBRobustness.GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT_ARB) != 0;
        } else if (attribs.api == API.GL) {
            APIVersion version = APIUtil.apiParseVersion(MemoryUtil.memUTF8(Checks.check(JNI.callP(GL11.GL_VERSION, getString))));
            effective.majorVersion = version.major;
            effective.minorVersion = version.minor;
        } else if (attribs.api == API.GLES) {
            APIVersion version = APIUtil.apiParseVersion(MemoryUtil.memUTF8(Checks.check(JNI.callP(GL11.GL_VERSION, getString)))+ "OpenGL ES");
            effective.majorVersion = version.major;
            effective.minorVersion = version.minor;
        }
        if (attribs.api == API.GL && atLeast32(effective.majorVersion, effective.minorVersion)) {
            JNI.callPV(GL32.GL_CONTEXT_PROFILE_MASK, bufferAddr, getInteger);
            int effectiveProfileMask = MemoryUtil.memGetInt(bufferAddr);
            boolean core = (effectiveProfileMask & GL32.GL_CONTEXT_CORE_PROFILE_BIT) != 0;
            boolean comp = (effectiveProfileMask & GL32.GL_CONTEXT_COMPATIBILITY_PROFILE_BIT) != 0;
            if (comp) {
                effective.profile = Profile.COMPATIBILITY;
            } else if (core) {
                effective.profile = Profile.CORE;
            } else {
                effective.profile = null;
            }
        }
        if (attribs.samples >= 1) {
            JNI.callPV(ARBMultisample.GL_SAMPLES_ARB, bufferAddr, getInteger);
            effective.samples = MemoryUtil.memGetInt(bufferAddr);
            JNI.callPV(ARBMultisample.GL_SAMPLE_BUFFERS_ARB, bufferAddr, getInteger);
            effective.sampleBuffers = MemoryUtil.memGetInt(bufferAddr);
            boolean has_WGL_NV_multisample_coverage = wglExtensionsList.contains("WGL_NV_multisample_coverage");
            if (has_WGL_NV_multisample_coverage) {
                JNI.callPV(NVMultisampleCoverage.GL_COLOR_SAMPLES_NV, bufferAddr, getInteger);
                effective.colorSamplesNV = MemoryUtil.memGetInt(bufferAddr);
            }
        }
        // Restore old context
        WGL.wglMakeCurrent(null, currentDc, currentContext);
        return newCtx;
    }

    private void wglNvSwapGroupAndBarrier(GLData attribs, long bufferAddr, long hDC) throws SWTException {
        int success;
        long wglQueryMaxSwapGroupsNVAddr = WGL.wglGetProcAddress(null, "wglQueryMaxSwapGroupsNV");
        success = JNI.callPPPI(hDC, bufferAddr, bufferAddr + 4, wglQueryMaxSwapGroupsNVAddr);
        int maxGroups = MemoryUtil.memGetInt(bufferAddr);
        if (maxGroups < attribs.swapGroupNV) {
            throw new SWTException("Swap group exceeds maximum group index");
        }
        int maxBarriers = MemoryUtil.memGetInt(bufferAddr + 4);
        if (maxBarriers < attribs.swapBarrierNV) {
            throw new SWTException("Swap barrier exceeds maximum barrier index");
        }
        if (attribs.swapGroupNV > 0) {
            long wglJoinSwapGroupNVAddr = WGL.wglGetProcAddress(null, "wglJoinSwapGroupNV");
            if (wglJoinSwapGroupNVAddr == 0L) {
                throw new SWTException("WGL_NV_swap_group available but wglJoinSwapGroupNV is NULL");
            }
            success = JNI.callPI(hDC, attribs.swapGroupNV, wglJoinSwapGroupNVAddr);
            if (success == 0) {
                throw new SWTException("Failed to join swap group");
            }
            if (attribs.swapBarrierNV > 0) {
                long wglBindSwapBarrierNVAddr = WGL.wglGetProcAddress(null, "wglBindSwapBarrierNV");
                if (wglBindSwapBarrierNVAddr == 0L) {
                    throw new SWTException("WGL_NV_swap_group available but wglBindSwapBarrierNV is NULL");
                }
                success = JNI.callI(attribs.swapGroupNV, attribs.swapBarrierNV, wglBindSwapBarrierNVAddr);
                if (success == 0) {
                    throw new SWTException("Failed to bind swap barrier. Probably no G-Sync card installed.");
                }
            }
        }
    }

    public boolean isCurrent(long context) {
        long ret = WGL.wglGetCurrentContext(null);
        return ret == context;
    }

    public boolean makeCurrent(GLCanvas canvas, long context) {
        long hDC = User32.GetDC(canvas.handle);
        boolean ret = WGL.wglMakeCurrent(null, hDC, context);
        User32.ReleaseDC(canvas.handle, hDC);
        return ret;
    }

    public boolean deleteContext(GLCanvas canvas, long context) {
        boolean ret = WGL.wglDeleteContext(null, context);
        return ret;
    }

    public boolean swapBuffers(GLCanvas canvas) {
        long hDC = User32.GetDC(canvas.handle);
        boolean ret = GDI32.SwapBuffers(null, hDC);
        User32.ReleaseDC(canvas.handle, hDC);
        return ret;
    }

    public boolean delayBeforeSwapNV(GLCanvas canvas, float seconds) {
        if (!wglDelayBeforeSwapNVAddr_set) {
            wglDelayBeforeSwapNVAddr = WGL.wglGetProcAddress(null, "wglDelayBeforeSwapNV");
            wglDelayBeforeSwapNVAddr_set = true;
        }
        if (wglDelayBeforeSwapNVAddr == 0L) {
            throw new UnsupportedOperationException("wglDelayBeforeSwapNV is unavailable");
        }
        long hDC = User32.GetDC(canvas.handle);
        int ret = JNI.callPI(hDC, seconds, wglDelayBeforeSwapNVAddr);
        User32.ReleaseDC(canvas.handle, hDC);
        return ret == 1;
    }

}
