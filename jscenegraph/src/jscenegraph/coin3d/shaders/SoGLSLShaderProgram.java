
/**************************************************************************\
 *
 *  This file is part of the Coin 3D visualization library.
 *  Copyright (C) by Kongsberg Oil & Gas Technologies.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  ("GPL") version 2 as published by the Free Software Foundation.
 *  See the file LICENSE.GPL at the root directory of this source
 *  distribution for additional information about the GNU GPL.
 *
 *  For using Coin with software that can not be combined with the GNU
 *  GPL, and for taking advantage of the additional benefits of our
 *  support services, please contact Kongsberg Oil & Gas Technologies
 *  about acquiring a Coin Professional Edition License.
 *
 *  See http://www.coin3d.org/ for more information.
 *
 *  Kongsberg Oil & Gas Technologies, Bygdoy Alle 5, 0257 Oslo, NORWAY.
 *  http://www.sim.no/  sales@sim.no  coin-support@coin3d.org
 *
\**************************************************************************/

package jscenegraph.coin3d.shaders;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jscenegraph.coin3d.inventor.nodes.SoShaderObject;
import jscenegraph.database.inventor.errors.SoDebugError;
import jscenegraph.database.inventor.nodes.SoNode;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;

import com.jogamp.opengl.GL2;

import jscenegraph.coin3d.glue.cc_glglue;
import jscenegraph.coin3d.inventor.lists.SbList;
import jscenegraph.coin3d.inventor.misc.SoContextHandler;
import jscenegraph.coin3d.misc.SoGL;
import jscenegraph.database.inventor.SbName;
import jscenegraph.database.inventor.elements.SoGLCacheContextElement;
import jscenegraph.database.inventor.misc.SoState;

/**
 * @author Yves Boyadjian
 *
 */
public class SoGLSLShaderProgram {

protected
  final SbList <Integer> programParameters = new SbList<>();
  protected final SbList <SoGLSLShaderObject > shaderObjects = new SbList<>();
  protected final Map </*COIN_GLhandle*/Integer, Handle> programHandles;

  protected boolean isExecutable;
  protected boolean neededlinking;

  public static class Handle {
    public int handle;
    private final Map<String,Uniform> uniformLocations = new HashMap<>();

    public static class Uniform {
      public int location;
      final float[] value4 = new float[4];
      final FloatBuffer value16 = BufferUtils.createFloatBuffer(16);

      public static boolean isValid(Uniform uniform) {
        if(null == uniform) {
          return false;
        }
        if(0 > uniform.location) {
          return false;
        }
        return true;
      }

      public void glUniform4fv(GL2 gl2, int num, float[] values4) {
        if(
                this.value4[0] != values4[0] ||
                        this.value4[1] != values4[1] ||
                        this.value4[2] != values4[2] ||
                        this.value4[3] != values4[3]
        ) {
          this.value4[0] = values4[0];
          this.value4[1] = values4[1];
          this.value4[2] = values4[2];
          this.value4[3] = values4[3];
          gl2.glUniform4fv(location, num, values4);
        }
      }

      public void glUniform1f(GL2 gl2, float value) {
        if(this.value4[0] != value) {
          this.value4[0] = value;
          gl2.glUniform1f(location, value);
        }
      }
      public void glUniformMatrix4fvARB(GL2 gl2, int num, boolean transpose, FloatBuffer value) {
        if(value16.compareTo(value) != 0) {
          value16.put(value);
          value.flip();
          value16.flip();
          gl2.glUniformMatrix4fvARB(location, num, transpose, value);
        }
      }
    }

    public static boolean isValid(Handle handle) {
      if (null == handle) {
        return false;
      }
      if(handle.handle <= 0) {
        return false;
      }
      return true;
    }

    public Uniform glGetUniformLocation(GL2 gl2, String name) {
      Uniform location = uniformLocations.get(name);
      if(null == location) {
        location = new Uniform();
        location.location = gl2.glGetUniformLocation(handle, name);
        uniformLocations.put(name,location);
      }
      return location;
    }
  }

public SoGLSLShaderProgram() {
  programHandles = new HashMap<>();//SbHash(5);

  this.isExecutable = false;
  this.neededlinking = true;
  SoContextHandler.addContextDestructionCallback(SoGLSLShaderProgram::context_destruction_cb, this);
}

public void destructor()
{
  SoContextHandler.removeContextDestructionCallback(SoGLSLShaderProgram::context_destruction_cb, this);
  this.deletePrograms();
}


public void
deleteProgram( cc_glglue g)
{
  /*COIN_GLhandle*/Handle glhandle;
  if ((glhandle = this.programHandles.get(g.contextid))!= null) {
    int tmp = (int) glhandle.handle;
    SoGLCacheContextElement.scheduleDeleteCallback(g.contextid,
    		SoGLSLShaderProgram::really_delete_object, (Object) tmp);
    this.programHandles.remove(g.contextid);
  }
}

public void
deletePrograms()
{
  //final SbList <Integer> keylist = new SbList<>();
  //this.programHandles.makeKeyList(keylist);
	Map<Integer,Handle> copyMap = new HashMap<>(this.programHandles);
  for (/*int i = 0; i < keylist.getLength(); i++*/Map.Entry<Integer,Handle> entry : copyMap.entrySet()) {
    /*COIN_GLhandle*/Handle glhandle;
     glhandle = entry.getValue();//this.programHandles.get(keylist.operator_square_bracket(i));
    int tmp = (int) glhandle.handle;
    SoGLCacheContextElement.scheduleDeleteCallback(/*keylist.operator_square_bracket(i)*/entry.getKey(),
    		SoGLSLShaderProgram::really_delete_object, (Object) tmp);
    this.programHandles.remove(entry.getKey()/*keylist.operator_square_bracket(i)*/);
  }
}


public void
addShaderObject(SoGLSLShaderObject shaderObject)
{
  if (shaderObject!=null) {
    if (this.indexOfShaderObject(shaderObject) < 0) {
      this.shaderObjects.append(shaderObject);
    }
  }
}

public void
removeShaderObjects()
{
  this.shaderObjects.truncate(0);
}

public void
enable( cc_glglue g)
{
  this.neededlinking = false;
  this.ensureLinking(g);

  if (this.isExecutable) {

    SoGLSLShaderObject.didOpenGLErrorOccur("SoGLSLShaderProgram::enable() : previous errors",g); // YB : fixing bug in Coin3D

    /*COIN_GLhandle*/int programhandle = this.getProgramHandle(g, true);
    g.glUseProgramObjectARB(programhandle);

    if (SoGLSLShaderObject.didOpenGLErrorOccur("SoGLSLShaderProgram::enable",g)) {
      SoGLSLShaderObject.printInfoLog(g, programhandle, 0);
    }
  }
}

public void
disable( cc_glglue g)
{
  if (this.isExecutable) {
    g.glUseProgramObjectARB(0);
  }
}

//#if defined(SOURCE_HINT)
//SbString
//SoGLSLShaderProgram::getSourceHint(void)  
//{
//  SbString result;
//  for (int i=0; i<this.shaderObjects.size(); i++) {
//    SoGLSLShaderObject *shader = this.shaderObjects[i];
//    if (shader && shader.isActive()) {
//      SbString str = shader.sourceHint;
//      if (str.getLength() > 0) str += " ";
//      result += str;
//    }
//  }
//  return result;
//}
//#endif

public void
ensureLinking( cc_glglue  g)
{
  boolean shouldlink = false;
  for (int i = 0; i < this.shaderObjects.getLength() && !shouldlink; i++) {
    if (!this.shaderObjects.operator_square_bracket(i).isAttached()) shouldlink = true;
  }

  if (!shouldlink) return;

  // delete old programs
  this.deleteProgram(g);

  this.isExecutable = false;

  /*COIN_GLhandle*/Handle programHandle = this.getProgramHandleClass(g, true);

  int cnt = this.shaderObjects.getLength();

  if (cnt > 0) {
    int i;
    final int[] didLink = new int[1];

    for (i = 0; i < cnt; i++) {
      this.shaderObjects.operator_square_bracket(i).attach(programHandle);
    }

    for (i = 0; i < this.programParameters.getLength(); i += 2) {
      g.glProgramParameteriEXT(programHandle.handle,
                                (int) this.programParameters.operator_square_bracket(i),
                                this.programParameters.operator_square_bracket(i+1));
      
    }

    ARBShaderObjects.glLinkProgramARB(programHandle.handle);

    if (SoGLSLShaderObject.didOpenGLErrorOccur("SoGLSLShaderProgram::ensureLinking",g)) {
      SoGLSLShaderObject.printInfoLog(g, programHandle.handle, 0);
    }
    g.glGetObjectParameterivARB(programHandle.handle,
                                 GL2.GL_OBJECT_LINK_STATUS_ARB,didLink);

    this.isExecutable = (didLink[0] != 0);

    if(!isExecutable) { // linking failed
      String infoLog = g.getGL2().glGetProgramInfoLog(programHandle.handle);
      SoDebugError.post("SoGLSLShaderProgram::ensureLinking",infoLog);
    }

    this.neededlinking = true;
  }
}

public int
indexOfShaderObject(SoGLSLShaderObject shaderObject)
{
  if (shaderObject == null) return -1;

  int cnt = this.shaderObjects.getLength();
  for (int i=0; i<cnt; i++) {
    if (shaderObject == this.shaderObjects.operator_square_bracket(i)) return i;
  }
  return -1;
}

public void
ensureProgramHandle( cc_glglue g)
{
  this.getProgramHandle(g, true);
}

public int getProgramHandle( cc_glglue  g) { // java port
	return getProgramHandle(g, false);
}

  public Handle getProgramHandleClass( cc_glglue  g) { // java port
    return getProgramHandleClass(g, false);
  }

public /*COIN_GLhandle*/int
getProgramHandle( cc_glglue  g, boolean create)
{
  /*COIN_GLhandle*/Handle handle;
  if ((handle = this.programHandles.get(g.contextid))==null && create) {
    handle = new Handle();
    handle.handle = g.glCreateProgramObjectARB();
    this.programHandles.put(g.contextid, handle);
  }
  return handle.handle;
}

  public Handle
  getProgramHandleClass( cc_glglue  g, boolean create)
  {
    /*COIN_GLhandle*/Handle handle;
    if ((handle = this.programHandles.get(g.contextid))==null && create) {
      handle = new Handle();
      handle.handle = g.glCreateProgramObjectARB();
      this.programHandles.put(g.contextid, handle);
    }
    return handle;
  }

public boolean 
neededLinking()
{
  return this.neededlinking;
}

public static void
context_destruction_cb(int cachecontext, Object userdata)
{
  SoGLSLShaderProgram thisp = (SoGLSLShaderProgram) userdata;

  /*COIN_GLhandle*/Handle glhandle;
  if ((glhandle = thisp.programHandles.get(cachecontext))!=null) {
    // just delete immediately. The context is current
      cc_glglue glue = SoGL.cc_glglue_instance(cachecontext);
    glue.glDeleteObjectARB(glhandle.handle);
    thisp.programHandles.remove(cachecontext);
  }
}

public static void
really_delete_object(Object closure, int contextid)
{
  /*uintptr_t*/int tmp = (/*uintptr_t*/int) closure;

  /*COIN_GLhandle*/int glhandle = (/*COIN_GLhandle*/int) tmp;

    cc_glglue glue = SoGL.cc_glglue_instance(contextid);
  glue.glDeleteObjectARB(glhandle);
}

public void
updateCoinParameter(SoState  state, final SbName  name,   int value)
{
    int n = this.shaderObjects.getLength();
  for (int i = 0; i < n; i++) {
    this.shaderObjects.operator_square_bracket(i).updateCoinParameter(state, name, null, value);
  }
}

public void 
addProgramParameter(int mode, int value)
{
  this.programParameters.append(mode);
  this.programParameters.append(value);
}

public void 
removeProgramParameters()
{
  this.programParameters.truncate(0);
}
}
