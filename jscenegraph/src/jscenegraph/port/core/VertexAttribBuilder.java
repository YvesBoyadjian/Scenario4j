package jscenegraph.port.core;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.jogamp.opengl.GL.GL_TRIANGLE_FAN;
import static com.jogamp.opengl.GL2.GL_POLYGON;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public class VertexAttribBuilder {

    private VertexAttribList.List list;

    private Builder currentBuilder;

    private final List<Float> vertices = new ArrayList<>();

    private abstract class Builder {

        public void glVertex2f(float x, float y) {
            glVertex3f(x,y,0.0f);
        };
        public abstract void glVertex3f(float x, float y, float z);
    }

    private class TriangleFanBuilder extends Builder {

        int firstIndex = -1;
        boolean secondVertex = false;
        boolean lastVertex = false;

        public void glVertex3f(float x, float y, float z) {
            if (-1 == firstIndex) {
                vertices.add(x);
                firstIndex = vertices.size() - 1;
                vertices.add(y);
                vertices.add(z);
            }
            else if (!secondVertex) {
                secondVertex = true;
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
            }
            else if(!lastVertex){
                lastVertex = true;
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
            }
            else {
                float x2 = vertices.get(vertices.size()-3);
                float y2 = vertices.get(vertices.size()-2);
                float z2 = vertices.get(vertices.size()-1);
                vertices.add(vertices.get(firstIndex));
                vertices.add(vertices.get(firstIndex+1));
                vertices.add(vertices.get(firstIndex+2));
                vertices.add(x2);
                vertices.add(y2);
                vertices.add(z2);
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
            }
        }
    }

    private class TriangleStripBuilder extends Builder {

        boolean firstSet = false;
        boolean secondSet = false;
        boolean thirdSet = false;
        boolean inverseDrawingOrder = false;
        @Override
        public void glVertex3f(float x, float y, float z) {
            if(!firstSet) {
                firstSet = true;
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
            }
            else if(!secondSet) {
                secondSet = true;
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
            }
            else if(!thirdSet) {
                thirdSet = true;
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
            }
            else {
                inverseDrawingOrder = !inverseDrawingOrder;
                int size = vertices.size();
                float x1 = inverseDrawingOrder ? vertices.get(size-3) : vertices.get(size-9);
                float y1 = inverseDrawingOrder ? vertices.get(size-2) : vertices.get(size-8);
                float z1 = inverseDrawingOrder ? vertices.get(size-1) : vertices.get(size-7);
                float x2 = inverseDrawingOrder ? vertices.get(size-6) : vertices.get(size-3);
                float y2 = inverseDrawingOrder ? vertices.get(size-5) : vertices.get(size-2);
                float z2 = inverseDrawingOrder ? vertices.get(size-4) : vertices.get(size-1);
                vertices.add(x1);
                vertices.add(y1);
                vertices.add(z1);
                vertices.add(x2);
                vertices.add(y2);
                vertices.add(z2);
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
            }
        }
    }

    private class TrianglesBuilder extends Builder {

        @Override
        public void glVertex3f(float x, float y, float z) {
            vertices.add(x);
            vertices.add(y);
            vertices.add(z);
        }
    }

    private class QuadBuilder extends Builder {

        int firstIndex = -1;
        boolean secondVertex = false;
        boolean thirdVertex = false;

        @Override
        public void glVertex3f(float x, float y, float z) {
            if (-1 == firstIndex) {
                vertices.add(x);
                firstIndex = vertices.size() - 1;
                vertices.add(y);
                vertices.add(z);
            }
            else if (!secondVertex) {
                secondVertex = true;
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
            }
            else if(!thirdVertex){
                thirdVertex = true;
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
            }
            else {
                float x2 = vertices.get(vertices.size()-3);
                float y2 = vertices.get(vertices.size()-2);
                float z2 = vertices.get(vertices.size()-1);
                vertices.add(vertices.get(firstIndex));
                vertices.add(vertices.get(firstIndex+1));
                vertices.add(vertices.get(firstIndex+2));
                vertices.add(x2);
                vertices.add(y2);
                vertices.add(z2);
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
                firstIndex = -1;
                secondVertex = false;
                thirdVertex = false;
            }
        }
    }

    public VertexAttribBuilder(VertexAttribList.List list) {
        this.list = list;
        list.setVerticesList(vertices);
    }

    public void glBegin(int arg) {
        assert(null == currentBuilder);
        currentBuilder = getBuilder(arg);
    }

    private Builder getBuilder(int arg) {

        Builder builder;
        switch(arg) {
            case GL_TRIANGLE_FAN:
            case GL_POLYGON:
                builder = new TriangleFanBuilder();
                break;
            case GL_TRIANGLE_STRIP:
                builder = new TriangleStripBuilder();
                break;
            case GL_TRIANGLES:
                builder = new TrianglesBuilder();
                break;
            case GL_QUADS:
                builder = new QuadBuilder();
                break;
            default:
                builder = null;
                break;
        }
        return builder;
    }

    public void glEnd() {
        currentBuilder = null;
    }

    public void glVertex2fv(FloatBuffer object)
    {
        int pos = object.position();
        currentBuilder.glVertex2f(object.get(),object.get());
        object.position(pos);
    }

    public void glVertex2fv(float[] valueRead, int i) {
        currentBuilder.glVertex2f(valueRead[i],valueRead[i+1]);
    }

    public void glVertex3fv(FloatBuffer object) {
        {
            int pos = object.position();
            currentBuilder.glVertex3f(object.get(), object.get(), object.get());
            object.position(pos);
        }
    }
}
