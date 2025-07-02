/**
 * 
 */
package application.actor;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.SoNode;

/**
 * 
 */
public interface ThreeDObject {

    SoNode getNode();
    void setPosition(SbVec3f position);
    SbVec3f getPosition();
}
