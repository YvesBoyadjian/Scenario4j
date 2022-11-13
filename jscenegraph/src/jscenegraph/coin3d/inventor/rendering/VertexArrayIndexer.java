package jscenegraph.coin3d.inventor.rendering;

import jscenegraph.coin3d.glue.cc_glglue;
import jscenegraph.port.Destroyable;

public interface VertexArrayIndexer extends Destroyable {
    void
    addTriangle( int v0,
                 int v1,
                 int v2);
    void
    addQuad( int v0,
             int v1,
             int v2,
             int v3);
    void
    beginTarget(/*GLenum*/int targetin);
    void
    targetVertex(/*GLenum*/int targetin, int v);
    void
    endTarget(/*GLenum*/int targetin);
    void
    close();
    int
    getNumVertices();
    void
    render(cc_glglue glue, boolean renderasvbo, int contextid);
}
