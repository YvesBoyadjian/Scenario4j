package application.scenegraph.douglas;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.port.memorybuffer.FloatMemoryBuffer;

import java.util.List;

public class IndexedFaceSetParametersImpl implements IndexedFaceSetParameters {

    public int[] douglasIndicesNearF;
    private FloatMemoryBuffer douglasVerticesNearF;
    private FloatMemoryBuffer douglasNormalsNearF;
    public int[] douglasColorsNearF;
    private FloatMemoryBuffer douglasTexCoordsNearF;

    @Override
    public int[] coordIndices() {
        return douglasIndicesNearF;
    }

    @Override
    public FloatMemoryBuffer vertices() {
        return douglasVerticesNearF;
    }

    @Override
    public FloatMemoryBuffer normals() {
        return douglasNormalsNearF;
    }

    @Override
    public FloatMemoryBuffer textureCoords() {
        return douglasTexCoordsNearF;
    }

    @Override
    public int[] colorsRGBA() {
        return douglasColorsNearF;
    }

    @Override
    public boolean keepOwnership() {
        return false;
    }

    @Override
    public void markConsumed() {
        douglasIndicesNearF = null;
        douglasVerticesNearF = null;
        douglasNormalsNearF = null;
        douglasColorsNearF = null;
        douglasTexCoordsNearF = null;
    }

    public void fill(final int nbVerticesNear,
                     final List<SbVec3f> vertices,
                     final boolean branchExtremityOn,
                     final List<SbVec3f> normals,
                     final List<Integer> colors,
                     final List<Float> branchHoriLengths,
                     final List<Float> branchVertiLengths,
                     final float zDeltaBaseBranch,
                     final float falling) {

        douglasVerticesNearF = FloatMemoryBuffer.allocateFloats(nbVerticesNear * 3);
        int floatIndex = 0;
        for(SbVec3f vec : vertices) {
            douglasVerticesNearF.setFloat(floatIndex,vec.getX()); floatIndex++;
            douglasVerticesNearF.setFloat(floatIndex,vec.getY()); floatIndex++;
            douglasVerticesNearF.setFloat(floatIndex,vec.getZ()); floatIndex++;
        }

        // ____________________________________________ indices
        final int nbBranches = nbVerticesNear/(branchExtremityOn ? 6 : 4);
        final int nbIndicesForOneTriangle = 4;
        // _______________________ Top of branch, up and down _________ extremity of branch __________________________ sides of branch_____________bottom of branch
        final int nbIndicesPerBranch = nbIndicesForOneTriangle*2 + (branchExtremityOn ?  2*nbIndicesForOneTriangle : 0) + 2 * nbIndicesForOneTriangle + nbIndicesForOneTriangle;
        int nbIndicesNear = nbBranches * nbIndicesPerBranch;

        douglasIndicesNearF = new int[nbIndicesNear];
        int indice = 0;

        int deltaSides = branchExtremityOn ? 16 : 8;
        int deltaSides2 = branchExtremityOn ? 20 : 12;
        int deltaBottom = branchExtremityOn ? 24 : 16;

        int startIndice = 0;
        for( int i=0; i< nbBranches; i++) {

            // Branch base (top)
            douglasIndicesNearF[startIndice] = indice;
            douglasIndicesNearF[startIndice+4] = indice;
            douglasIndicesNearF[startIndice+deltaSides+1] = indice;
            douglasIndicesNearF[startIndice+deltaSides2] = indice;
            indice++;
            // Branch base 2 (bottom)
            douglasIndicesNearF[startIndice+deltaSides] = indice;
            douglasIndicesNearF[startIndice+deltaSides2+1] = indice;
            douglasIndicesNearF[startIndice+deltaBottom] = indice;
            indice++;
            // Branch extremity
            douglasIndicesNearF[startIndice+1] = indice;
            douglasIndicesNearF[startIndice+2+4] = indice;
            if(branchExtremityOn) {
                douglasIndicesNearF[startIndice + 8] = indice;
            }
            douglasIndicesNearF[startIndice+deltaSides+2] = indice;
            douglasIndicesNearF[startIndice+deltaBottom+1] = indice;
            indice++;
            // Branch extremity 2
            douglasIndicesNearF[startIndice+2] = indice;
            douglasIndicesNearF[startIndice+1+4] = indice;
            if(branchExtremityOn) {
                douglasIndicesNearF[startIndice + 9] = indice;
                douglasIndicesNearF[startIndice + 12] = indice;
            }
            douglasIndicesNearF[startIndice+deltaSides2+2] = indice;
            douglasIndicesNearF[startIndice+deltaBottom+2] = indice;
            indice++;
            if(branchExtremityOn) {
                // Branch extremity 3
                douglasIndicesNearF[startIndice + 10] = indice;
                douglasIndicesNearF[startIndice + 14] = indice;
                indice++;
                // Branch extremity 4
                douglasIndicesNearF[startIndice + 13] = indice;
                indice++;
            }
            douglasIndicesNearF[startIndice+3] = -1; // Topup
            douglasIndicesNearF[startIndice+3+4] = -1; // Topdown
            douglasIndicesNearF[startIndice+11] = -1;
            douglasIndicesNearF[startIndice+15] = -1;
            douglasIndicesNearF[startIndice+19] = -1;
            if(branchExtremityOn) {
                douglasIndicesNearF[startIndice + 19 + 4] = -1;
                douglasIndicesNearF[startIndice + 19 + 8] = -1;
            }
            startIndice += nbIndicesPerBranch;
        }

        // ____________________________________________ normals
        douglasNormalsNearF = FloatMemoryBuffer.allocateFloats(nbVerticesNear * 3);
        floatIndex = 0;
        final int mult = branchExtremityOn ? 2 : 1;
        //final SbVec3f down = new SbVec3fSingle(0,0,-1);

        for (int i=0; i< nbBranches;i++) {
            SbVec3f normal = normals.get(mult*i);
            for(int j=0;j<4;j++) { // four points
                if(j==1) {
                    douglasNormalsNearF.setFloat(floatIndex, 0);
                    floatIndex++;
                    douglasNormalsNearF.setFloat(floatIndex, 0);
                    floatIndex++;
                    douglasNormalsNearF.setFloat(floatIndex, -1);
                    floatIndex++;
                }
                else if (j==2 || j==3) {
                    douglasNormalsNearF.setFloat(floatIndex, -normal.getX());
                    floatIndex++;
                    douglasNormalsNearF.setFloat(floatIndex, -normal.getY());
                    floatIndex++;
                    douglasNormalsNearF.setFloat(floatIndex, -normal.getZ());
                    floatIndex++;
                }
                else {
                    douglasNormalsNearF.setFloat(floatIndex, -normal.getX());
                    floatIndex++;
                    douglasNormalsNearF.setFloat(floatIndex, -normal.getY());
                    floatIndex++;
                    douglasNormalsNearF.setFloat(floatIndex, -normal.getZ());
                    floatIndex++;
                }
            }
            if (branchExtremityOn) {
                SbVec3f normal2 = normals.get(mult * i + 1);
                for (int j = 0; j < 2; j++) { // two points
                    douglasNormalsNearF.setFloat(floatIndex, -normal2.getX());
                    floatIndex++;
                    douglasNormalsNearF.setFloat(floatIndex, -normal2.getY());
                    floatIndex++;
                    douglasNormalsNearF.setFloat(floatIndex, -normal2.getZ());
                    floatIndex++;
                }
            }
        }

        // _______________________________________________ colors

        douglasColorsNearF = new int[nbVerticesNear];
        indice = 0;
        for(int i=0;i<nbBranches;i++) {
            int color = colors.get(i);
            douglasColorsNearF[indice] = color; indice++;
            douglasColorsNearF[indice] = color; indice++;
            douglasColorsNearF[indice] = color; indice++;
            douglasColorsNearF[indice] = color; indice++;
            if(branchExtremityOn) {
                douglasColorsNearF[indice] = color;
                indice++;
                douglasColorsNearF[indice] = color;
                indice++;
            }
        }

        // ________________________________________________ texture coordinates

        douglasTexCoordsNearF = FloatMemoryBuffer.allocateFloats(nbVerticesNear*2);
        floatIndex = 0;
        for(int i=0;i<nbBranches;i++) {
            float branchHoriLength = branchHoriLengths.get(i);
            float branchVertiLength = branchVertiLengths.get(i);
            // Branch base
            douglasTexCoordsNearF.setFloat(floatIndex,0); floatIndex++;
            douglasTexCoordsNearF.setFloat(floatIndex,0); floatIndex++;
            // Branch base 2
            douglasTexCoordsNearF.setFloat(floatIndex,/*zDeltaBaseBranch*/branchVertiLength); floatIndex++;
            douglasTexCoordsNearF.setFloat(floatIndex,0); floatIndex++;
            // Branch extremity
            douglasTexCoordsNearF.setFloat(floatIndex,/*branchHoriLength*/branchVertiLength); floatIndex++;
            douglasTexCoordsNearF.setFloat(floatIndex,-branchHoriLength); floatIndex++;
            // Branch Extremity 2
            douglasTexCoordsNearF.setFloat(floatIndex,/*branchHoriLength*/branchVertiLength); floatIndex++;
            douglasTexCoordsNearF.setFloat(floatIndex,branchHoriLength); floatIndex++;
            if(branchExtremityOn) {
                // Branch extremity 3
                douglasTexCoordsNearF.setFloat(floatIndex, branchHoriLength + falling);
                floatIndex++;
                douglasTexCoordsNearF.setFloat(floatIndex, -branchHoriLength);
                floatIndex++;
                // Branch Extremity 4
                douglasTexCoordsNearF.setFloat(floatIndex, branchHoriLength + falling);
                floatIndex++;
                douglasTexCoordsNearF.setFloat(floatIndex, branchHoriLength);
                floatIndex++;
            }
        }
    }
}
