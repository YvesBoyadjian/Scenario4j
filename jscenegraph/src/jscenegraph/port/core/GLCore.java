package jscenegraph.port.core;

import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.SoNode;

import java.nio.FloatBuffer;

public class GLCore {

    SoState state;

    VertexAttribList list;
    VertexAttribList.List l;
    VertexAttribBuilder vab;

    public GLCore(SoState state) {
        this.state = state;
    }

    public void glEnd() {
        vab.glEnd();
        list.glEndList(0);

        SoNode node = null;

        list.callList(0,node);
        list.unref(state);
        list = null;
    }

    public void glBegin(int mode) {
        list = new VertexAttribList(state,1);
        list.ref();
        l = list.glNewList(0);
        vab = new VertexAttribBuilder(l);
        vab.glBegin(mode);
    }

    public void glNormal3fv(float[] valueRead, int i) {
        //TODO
    }

    public void glNormal3fv(FloatBuffer toFloatBuffer) {
        //TODO
    }

    public void glNormal3fv(float[] valueRead) {
        //TODO
    }

    public void glVertex3fv(FloatBuffer floatBuffer) {
        vab.glVertex3fv(floatBuffer);
    }

    public void glVertex3fv(float[] floatBuffer) {
        //TODO
    }

    public void glVertex4fv(float[] valueRead, int i) {
    }
}
