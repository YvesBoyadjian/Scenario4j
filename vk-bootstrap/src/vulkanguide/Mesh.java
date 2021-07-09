package vulkanguide;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryUtil.*;
import static util.IOUtils.ioResourceToByteBuffer;

public class Mesh {
    public final List<Vertex> _vertices = new ArrayList<>();

    public final AllocatedBuffer _vertexBuffer = new AllocatedBuffer();

    public boolean load_from_obj(String filename) {
        AIScene scene = loadModel(filename);

        if(null == scene) {
            return false;
        }

        int meshCount = scene.mNumMeshes();
        PointerBuffer meshesBuffer = scene.mMeshes();
        for (int i = 0; i < meshCount; ++i) {
            AIMesh mesh = AIMesh.create(meshesBuffer.get(i));
            AIVector3D.Buffer vertices = mesh.mVertices();
            AIVector3D.Buffer normals = mesh.mNormals();
            AIFace.Buffer facesBuffer = mesh.mFaces();
            AIVector3D.Buffer textCoords = mesh.mTextureCoords(0);

            // Loop over faces(polygon)
            long index_offset = 0;
            long num_face_vertices_size = mesh.mNumFaces();//vertices.limit();
            for (int f = 0; f < num_face_vertices_size; f++) {

                AIFace face = facesBuffer.get(f);
                IntBuffer indices = face.mIndices();

                //hardcode loading to triangles
                int fv = 3;

                // Loop over vertices in the face.
                for (long v = 0; v < fv; v++) {
                    // access to vertex
                    int idx = indices.get((int)(/*index_offset +*/ v));

                    int vertex_index = idx;

                    int normal_index = idx;

                    int texcoord_index = idx;

                    //vertex position
                    float vx = vertices.get(vertex_index).x();
                    float vy = vertices.get(vertex_index).y();
                    float vz = vertices.get(vertex_index).z();
                    //vertex normal
                    float nx = normals.get(normal_index).x();
                    float ny = normals.get(normal_index).y();
                    float nz = normals.get(normal_index).z();

                    //vertex uv
                    float ux = textCoords.get(texcoord_index).x();
                    float uy = textCoords.get(texcoord_index).y();

                    //copy it into our vertex
                    final Vertex new_vert = new Vertex();
                    new_vert.position.x = vx;
                    new_vert.position.y = vy;
                    new_vert.position.z = vz;

                    new_vert.normal.x = nx;
                    new_vert.normal.y = ny;
                    new_vert.normal.z = nz;


                    new_vert.uv.x = ux;
                    new_vert.uv.y = 1-uy;

                    //we are setting the vertex color as the vertex normal. This is just for display purposes
                    new_vert.color.set(new_vert.normal);


                    _vertices.add(new_vert);

                    int ii=0;
                }
                index_offset += fv;
            }
        }

        return true;
    }

    public static AIScene loadModel(String filename) {
        AIFileIO fileIo = AIFileIO.create()
                .OpenProc((pFileIO, fileName, openMode) -> {
                    ByteBuffer data;
                    String fileNameUtf8 = memUTF8(fileName);
                    try {
                        data = ioResourceToByteBuffer(fileNameUtf8, 8192);
                    } catch (IOException e) {
                        /*throw new RuntimeException(*/System.out.println("Could not open file: " + fileNameUtf8);
                        return 0;
                    }

                    return AIFile.create()
                            .ReadProc((pFile, pBuffer, size, count) -> {
                                long max = Math.min(data.remaining(), size * count);
                                memCopy(memAddress(data) + data.position(), pBuffer, max);
                                return max;
                            })
                            .SeekProc((pFile, offset, origin) -> {
                                if (origin == Assimp.aiOrigin_CUR) {
                                    data.position(data.position() + (int) offset);
                                } else if (origin == Assimp.aiOrigin_SET) {
                                    data.position((int) offset);
                                } else if (origin == Assimp.aiOrigin_END) {
                                    data.position(data.limit() + (int) offset);
                                }
                                return 0;
                            })
                            .FileSizeProc(pFile -> data.limit())
                            .address();
                })
                .CloseProc((pFileIO, pFile) -> {
                    AIFile aiFile = AIFile.create(pFile);

                    aiFile.ReadProc().free();
                    aiFile.SeekProc().free();
                    aiFile.FileSizeProc().free();
                });
        AIScene scene = aiImportFileEx(filename,
                aiProcess_JoinIdenticalVertices | aiProcess_Triangulate, fileIo);
        fileIo.OpenProc().free();
        fileIo.CloseProc().free();
        if (scene == null) {
            /*throw new IllegalStateException(*/System.out.println(aiGetErrorString());
        }
        return scene;
    }
}
