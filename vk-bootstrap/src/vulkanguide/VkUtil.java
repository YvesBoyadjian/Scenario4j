package vulkanguide;

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.libc.LibCString.memcpy;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

public class VkUtil {
    /*11*/ public static boolean load_image_from_file(VulkanEngine engine, String file, final AllocatedImage[] outImage)
    {
        final int[] texWidth = new int[1];
        final int[] texHeight = new int[1];
        final int[] texChannels = new int[1];

//        ByteBuffer buffer;
//        try {
//            FileInputStream fis = new FileInputStream(file);
//
//            FileChannel fc = fis.getChannel();
//            buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
//            fc.close();
//
//            //now that the file is loaded into the buffer, we can close it
//            fis.close();
//        } catch (IOException e) {
//            return false;
//        }


        ByteBuffer pixels = stbi_load(file, texWidth, texHeight, texChannels, STBI_rgb_alpha);

        if (null == pixels) {
            System.out.println( "Failed to load texture file " + file );
            return false;
        }

        ByteBuffer pixel_ptr = pixels;
        /*VkDeviceSize*/int imageSize = texWidth[0] * texHeight[0] * 4;

        /*VkFormat*/int image_format = VK_FORMAT_R8G8B8A8_SRGB;

        AllocatedBuffer stagingBuffer = engine.create_buffer(imageSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VMA_MEMORY_USAGE_CPU_ONLY);

        final PointerBuffer data = memAllocPointer(1);
        vmaMapMemory(engine._allocator, stagingBuffer._allocation, data);

        memcpy(data.getByteBuffer(imageSize), pixel_ptr/*, (long)(imageSize)*/);

        vmaUnmapMemory(engine._allocator, stagingBuffer._allocation);

        stbi_image_free(pixels);

        final VkExtent3D imageExtent = VkExtent3D.create();
        imageExtent.width ( (int)(texWidth[0]));
        imageExtent.height ( (int)(texHeight[0]));
        imageExtent.depth ( 1);

        VkImageCreateInfo dimg_info = VkInit.image_create_info(image_format, VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT, imageExtent);

        final AllocatedImage newImage = new AllocatedImage();

        final VmaAllocationCreateInfo dimg_allocinfo = VmaAllocationCreateInfo.create();
        dimg_allocinfo.usage ( VMA_MEMORY_USAGE_GPU_ONLY);

        //allocate and create the image
        LongBuffer dummy1 = memAllocLong(1);
        PointerBuffer dummy2 = memAllocPointer(1);
        vmaCreateImage(engine._allocator, dimg_info, dimg_allocinfo, /*newImage._image*/dummy1, /*newImage._allocation*/dummy2, null);
        newImage._image = dummy1.get(0);
        newImage._allocation = dummy2.get(0);

        memFree(dummy1);
        memFree(dummy2);

        //transition image to transfer-receiver
        engine.immediate_submit((VkCommandBuffer cmd) -> {
        final VkImageSubresourceRange range = VkImageSubresourceRange.create();
        range.aspectMask ( VK_IMAGE_ASPECT_COLOR_BIT);
        range.baseMipLevel ( 0);
        range.levelCount ( 1);
        range.baseArrayLayer ( 0);
        range.layerCount ( 1);

        final VkImageMemoryBarrier.Buffer imageBarrier_toTransfer = VkImageMemoryBarrier.create(1);
        imageBarrier_toTransfer.sType ( VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);

        imageBarrier_toTransfer.oldLayout ( VK_IMAGE_LAYOUT_UNDEFINED);
        imageBarrier_toTransfer.newLayout ( VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
        imageBarrier_toTransfer.image ( newImage._image);
        imageBarrier_toTransfer.subresourceRange ( range);

        imageBarrier_toTransfer.srcAccessMask ( 0);
        imageBarrier_toTransfer.dstAccessMask ( VK_ACCESS_TRANSFER_WRITE_BIT);

        //barrier the image into the transfer-receive layout
        vkCmdPipelineBarrier(cmd, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, 0, /*0,*/ null, /*0,*/ null, /*1,*/ imageBarrier_toTransfer);

        final VkBufferImageCopy.Buffer copyRegion = VkBufferImageCopy.create(1);
        copyRegion.bufferOffset ( 0);
        copyRegion.bufferRowLength ( 0);
        copyRegion.bufferImageHeight ( 0);

        copyRegion.imageSubresource().aspectMask ( VK_IMAGE_ASPECT_COLOR_BIT);
        copyRegion.imageSubresource().mipLevel ( 0);
        copyRegion.imageSubresource().baseArrayLayer ( 0);
        copyRegion.imageSubresource().layerCount ( 1);
        copyRegion.imageExtent ( imageExtent);

        //copy the buffer into the image
        vkCmdCopyBufferToImage(cmd, stagingBuffer._buffer[0], newImage._image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, /*1,*/ copyRegion);

        VkImageMemoryBarrier.Buffer imageBarrier_toReadable = VkImageMemoryBarrier.create(1);imageBarrier_toReadable.put(0, imageBarrier_toTransfer.get(0));

        imageBarrier_toReadable.oldLayout ( VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
        imageBarrier_toReadable.newLayout ( VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

        imageBarrier_toReadable.srcAccessMask ( VK_ACCESS_TRANSFER_WRITE_BIT);
        imageBarrier_toReadable.dstAccessMask ( VK_ACCESS_SHADER_READ_BIT);

        //barrier the image into the shader readable layout
        vkCmdPipelineBarrier(cmd, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0, /*0,*/ null, /*0,*/ null, /*1,*/ imageBarrier_toReadable);
    });


        engine._mainDeletionQueue.push_function(() -> {

        vmaDestroyImage(engine._allocator, newImage._image, newImage._allocation);
    });

        vmaDestroyBuffer(engine._allocator, stagingBuffer._buffer[0], stagingBuffer._allocation);

        System.out.println( "Texture loaded succesfully " + file );

        outImage[0] = newImage;
        return true;
    }
}
