package jscenegraph.port.core;

import com.jogamp.opengl.GL2;
import jscenegraph.coin3d.inventor.elements.SoGLMultiTextureEnabledElement;
import jscenegraph.coin3d.inventor.elements.SoMultiTextureImageElement;
import jscenegraph.coin3d.inventor.elements.gl.SoGLMultiTextureImageElement;
import jscenegraph.coin3d.inventor.misc.SoGLImage;
import jscenegraph.database.inventor.*;
import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoText2;
import jscenegraph.port.SbVec2fArray;
import jscenegraph.port.SbVec3fArray;
import jscenegraph.port.memorybuffer.FloatMemoryBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;

public class GLBitmap {

    public interface Transformer {
        SbVec3f transform(SbVec3f in);
    }

    public static void glBitmap(
            SoState state,
            SoNode node,
            SbVec3f charPosition,
            Transformer transformer,
            int width,
            int height,
            float xorig,
            float yorig,
            float xmove,
            float ymove,
            SoGLImage image
    ) {

//        SoGLImage image = new SoGLImage();
//
//        SbVec2s size = new SbVec2s((short)width,(short)height);
//
//        image.setData(
//                bitmap,
//                size,
//                1,
//                false,
//                SoGLImage.Wrap.CLAMP,
//                SoGLImage.Wrap.CLAMP,
//                1.0f,
//                0,
//                /*state*/null);

        SoGLMultiTextureImageElement.set(
                state,
                node,
                0,
                image,
                SoMultiTextureImageElement.Model.MODULATE,
                new SbColor(0.0f, 0.0f, 0.0f)
        );

        SoGLMultiTextureEnabledElement.set(state,node,0,true);

        SbVec3fArray vboArray = new SbVec3fArray(FloatMemoryBuffer.allocateFloats(3 * 6));

        float xScreenStart = charPosition.getX();
        float yScreenStart = charPosition.getY();
        float xScreenEnd = xScreenStart + width;
        float yScreenEnd = yScreenStart + height;

        SbVec3f a = new SbVec3fSingle(xScreenStart,yScreenStart,-1);
        SbVec3f b = new SbVec3fSingle(xScreenEnd,yScreenStart,-1);
        SbVec3f c = new SbVec3fSingle(xScreenStart,yScreenEnd,-1);
        SbVec3f d = new SbVec3fSingle(xScreenEnd,yScreenEnd,-1);

        a = transformer.transform(a);
        b = transformer.transform(b);
        c = transformer.transform(c);
        d = transformer.transform(d);

        vboArray.setO(0,a);
        vboArray.setO(1,b);
        vboArray.setO(2,c);
        vboArray.setO(3,b);
        vboArray.setO(4,d);
        vboArray.setO(5,c);

        SbVec2fArray tcArray = new SbVec2fArray(FloatMemoryBuffer.allocateFloats(2 * 6));

        SbVec2f at = new SbVec2fSingle(0,0);
        SbVec2f bt = new SbVec2fSingle(1,0);
        SbVec2f ct = new SbVec2fSingle(0,1);
        SbVec2f dt = new SbVec2fSingle(1,1);

        tcArray.setO(0,at);
        tcArray.setO(1,bt);
        tcArray.setO(2,ct);
        tcArray.setO(3,bt);
        tcArray.setO(4,dt);
        tcArray.setO(5,ct);

        // ________________________________________________ Vertex coords
        final int[] vertex_bo = new int[1];

        GL2 gl2 = state.getGL2();

        gl2.glGenBuffers(1,vertex_bo);
        gl2.glBindBuffer(GL_ARRAY_BUFFER,vertex_bo[0]);
        gl2.glBufferData(GL_ARRAY_BUFFER,vboArray.sizeof(),vboArray.toFloatBuffer(),GL_STATIC_DRAW);

        gl2.glVertexAttribPointer(0,3,GL_FLOAT,false,/*3*Float.BYTES*/0,0);

        gl2.glEnableVertexAttribArray(0);

// __________________________________________________ Texture coords
        final int[] texture_coordinate_bo = new int[1];

        gl2.glGenBuffers(1, texture_coordinate_bo);
        gl2.glBindBuffer(GL_ARRAY_BUFFER,texture_coordinate_bo[0]);
        gl2.glBufferData(GL_ARRAY_BUFFER,tcArray.sizeof(),tcArray.toFloatBuffer(),GL_STATIC_DRAW);

        gl2.glVertexAttribPointer(2,2,GL_FLOAT,false,0,0);

        gl2.glEnableVertexAttribArray(2);

        int mode = GL_TRIANGLES;
        int first = 0;
        int count = vboArray.length();

        gl2.glDrawArrays(mode,first,count);


        gl2.glBindBuffer(GL_ARRAY_BUFFER,0);

        gl2.glDisableVertexAttribArray(0);
        gl2.glDisableVertexAttribArray(2);

//    gl2.glDeleteBuffers(1,ebo);
        gl2.glDeleteBuffers(1,vertex_bo);
        gl2.glDeleteBuffers(1,texture_coordinate_bo);

        //image.unref(state);

        charPosition.setX(charPosition.getX()+xmove);
        charPosition.setY(charPosition.getY()+ymove);
    }
}
