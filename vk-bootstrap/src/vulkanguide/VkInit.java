package vulkanguide;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class VkInit {

    public static VkCommandPoolCreateInfo command_pool_create_info(int queueFamilyIndex) {
        return command_pool_create_info(queueFamilyIndex,0);
    }
    /*3*/ public static VkCommandPoolCreateInfo command_pool_create_info(int queueFamilyIndex, /*VkCommandPoolCreateFlags*/int flags /*= 0*/)
    {
        final VkCommandPoolCreateInfo info = VkCommandPoolCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
        info.pNext ( 0);

        info.flags ( flags);
        return info;
    }

    public static VkCommandBufferAllocateInfo command_buffer_allocate_info(/*VkCommandPool*/long pool, int count /*= 1*/) {
        return command_buffer_allocate_info(pool,count,VK_COMMAND_BUFFER_LEVEL_PRIMARY);
    }
    /*13*/ public static VkCommandBufferAllocateInfo command_buffer_allocate_info(/*VkCommandPool*/long pool, int count /*= 1*/, /*VkCommandBufferLevel*/int level /*= VK_COMMAND_BUFFER_LEVEL_PRIMARY*/)
    {
        final VkCommandBufferAllocateInfo info = VkCommandBufferAllocateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
        info.pNext ( 0);

        info.commandPool ( pool);
        info.commandBufferCount ( count);
        info.level ( level);
        return info;
    }

    /*25*/
    static VkCommandBufferBeginInfo command_buffer_begin_info(/*VkCommandBufferUsageFlags*/int flags /*= 0*/)
    {
        final VkCommandBufferBeginInfo info = VkCommandBufferBeginInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
        info.pNext ( 0);

        info.pInheritanceInfo ( null);
        info.flags ( flags);
        return info;
    }

    /*36*/ public static VkFramebufferCreateInfo framebuffer_create_info(/*VkRenderPass*/long renderPass, VkExtent2D extent)
    {
        final VkFramebufferCreateInfo info = VkFramebufferCreateInfo.create();
        info.sType( VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
        info.pNext ( 0);

        info.renderPass ( renderPass);
        //info.attachmentCount ( 1); java port
        LongBuffer dummy = memAllocLong(1);
        info.pAttachments(dummy);
        info.width( extent.width());
        info.height( extent.height());
        info.layers ( 1);

        return info;
    }

    public static VkFenceCreateInfo fence_create_info() {
        return fence_create_info(0);
    }
    /*51*/ public static VkFenceCreateInfo fence_create_info(/*VkFenceCreateFlags*/int flags /*= 0*/)
    {
        final VkFenceCreateInfo info = VkFenceCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
        info.pNext ( 0);

        info.flags ( flags);

        return info;
    }

    public static VkSemaphoreCreateInfo semaphore_create_info() {
        return semaphore_create_info(0);
    }
    /*62*/ public static VkSemaphoreCreateInfo semaphore_create_info(/*VkSemaphoreCreateFlags*/int flags /*= 0*/)
    {
        final VkSemaphoreCreateInfo info = VkSemaphoreCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
        info.pNext ( 0);
        info.flags ( flags);
        return info;
    }

    /*71*/ public static VkSubmitInfo submit_info(VkCommandBuffer cmd)
    {
        final VkSubmitInfo info = VkSubmitInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_SUBMIT_INFO);
        info.pNext ( 0);

        info.waitSemaphoreCount ( 0);
        info.pWaitSemaphores ( null);
        info.pWaitDstStageMask ( null);
        //info.commandBufferCount ( 1); java port
        PointerBuffer dummy = memAllocPointer(1);
        dummy.put(0,cmd);
        info.pCommandBuffers ( /*cmd*/dummy);
        //info.signalSemaphoreCount ( 0); java port
        info.pSignalSemaphores ( null);

        return info;
    }

    /*88*/
    static VkPresentInfoKHR present_info()
    {
        final VkPresentInfoKHR info = VkPresentInfoKHR.create();
        info.sType ( VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
        info.pNext ( 0);

        info.swapchainCount ( 0);
        //info.pSwapchains ( null); java port
        info.pWaitSemaphores ( null);
        //info.waitSemaphoreCount ( 0); java port
        //info.pImageIndices ( null); java port

        return info;
    }

    /*103*/ public static VkRenderPassBeginInfo renderpass_begin_info(/*VkRenderPass*/long renderPass, VkExtent2D windowExtent, /*VkFramebuffer*/long framebuffer)
    {
        final VkRenderPassBeginInfo info = VkRenderPassBeginInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
        info.pNext ( 0);

        info.renderPass ( renderPass);
        info.renderArea().offset().x ( 0);
        info.renderArea().offset().y ( 0);
        info.renderArea().extent( windowExtent);
        //info.clearValueCount ( 1); java port
        info.pClearValues ( null);
        info.framebuffer ( framebuffer);

        return info;
    }

    /*120*/
    static VkPipelineShaderStageCreateInfo pipeline_shader_stage_create_info(/*VkShaderStageFlagBits*/int stage, /*VkShaderModule*/long shaderModule)
    {
        final VkPipelineShaderStageCreateInfo info = VkPipelineShaderStageCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        info.pNext ( 0);

        //shader stage
        info.stage ( stage);
        //module containing the code for this shader stage
        info.module ( shaderModule);
        //the entry point of the shader
        info.pName ( memUTF8("main"));
        return info;
    }
    /*134*/ static VkPipelineVertexInputStateCreateInfo vertex_input_state_create_info() {
        final VkPipelineVertexInputStateCreateInfo info = VkPipelineVertexInputStateCreateInfo.create();
        info.sType( VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        info.pNext ( 0);

        //no vertex bindings or attributes
        //info.vertexBindingDescriptionCount ( 0); java port
        //info.vertexAttributeDescriptionCount ( 0); java port
        return info;
    }

    /*145*/ static VkPipelineInputAssemblyStateCreateInfo input_assembly_create_info(/*VkPrimitiveTopology*/long topology) {
        final VkPipelineInputAssemblyStateCreateInfo info = VkPipelineInputAssemblyStateCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        info.pNext ( 0);

        info.topology ( (int)topology);
        //we are not going to use primitive restart on the entire tutorial so leave it on false
        info.primitiveRestartEnable ( VK_FALSE != 0);
        return info;
    }
    /*155*/static VkPipelineRasterizationStateCreateInfo rasterization_state_create_info(/*VkPolygonMode*/long polygonMode)
    {
        final VkPipelineRasterizationStateCreateInfo info = VkPipelineRasterizationStateCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        info.pNext ( 0);

        info.depthClampEnable ( VK_FALSE != 0);
        //rasterizer discard allows objects with holes, default to no
        info.rasterizerDiscardEnable ( VK_FALSE != 0);

        info.polygonMode ( (int)polygonMode);
        info.lineWidth ( 1.0f);
        //no backface cull
        info.cullMode ( VK_CULL_MODE_NONE);
        info.frontFace ( VK_FRONT_FACE_CLOCKWISE);
        //no depth bias
        info.depthBiasEnable ( VK_FALSE != 0);
        info.depthBiasConstantFactor ( 0.0f);
        info.depthBiasClamp ( 0.0f);
        info.depthBiasSlopeFactor ( 0.0f);

        return info;
    }
    /*178*/static VkPipelineMultisampleStateCreateInfo multisampling_state_create_info()
    {
        final VkPipelineMultisampleStateCreateInfo info = VkPipelineMultisampleStateCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        info.pNext ( 0);

        info.sampleShadingEnable ( VK_FALSE != 0);
        //multisampling defaulted to no multisampling (1 sample per pixel)
        info.rasterizationSamples ( VK_SAMPLE_COUNT_1_BIT);
        info.minSampleShading ( 1.0f);
        info.pSampleMask ( null);
        info.alphaToCoverageEnable ( VK_FALSE != 0);
        info.alphaToOneEnable ( VK_FALSE != 0);
        return info;
    }
    /*193*/static VkPipelineColorBlendAttachmentState color_blend_attachment_state() {
        final VkPipelineColorBlendAttachmentState colorBlendAttachment = VkPipelineColorBlendAttachmentState.create();
        colorBlendAttachment.colorWriteMask ( VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT |
                VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
        colorBlendAttachment.blendEnable ( VK_FALSE != 0);
        return colorBlendAttachment;
    }

    /*200*/
    static VkPipelineLayoutCreateInfo pipeline_layout_create_info() {
        final VkPipelineLayoutCreateInfo info = VkPipelineLayoutCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
        info.pNext ( 0);

        //empty defaults
        info.flags ( 0);
        //info.setLayoutCount ( 0); java port
        info.pSetLayouts ( null);
        //info.pushConstantRangeCount ( 0); java port
        info.pPushConstantRanges ( null);
        return info;
    }

    /*214*/ public static VkImageCreateInfo image_create_info(/*VkFormat*/long format, /*VkImageUsageFlags*/int usageFlags, VkExtent3D extent)
    {
        final VkImageCreateInfo info = VkImageCreateInfo.create();
        info.sType( VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
        info.pNext( 0);

        info.imageType( VK_IMAGE_TYPE_2D);

        info.format( (int)format);
        info.extent( extent);

        info.mipLevels( 1);
        info.arrayLayers( 1);
        info.samples( VK_SAMPLE_COUNT_1_BIT);
        info.tiling( VK_IMAGE_TILING_OPTIMAL);
        info.usage( usageFlags);

        return info;
    }

    /*234*/ public static VkImageViewCreateInfo imageview_create_info(/*VkFormat*/long format, /*VkImage*/long image, /*VkImageAspectFlags*/int aspectFlags)
    {
        //build a image-view for the depth image to use for rendering
        final VkImageViewCreateInfo info = VkImageViewCreateInfo.create();
        info.sType( VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
        info.pNext( 0);

        info.viewType( VK_IMAGE_VIEW_TYPE_2D);
        info.image( image);
        info.format( (int)format);
        info.subresourceRange().baseMipLevel( 0);
        info.subresourceRange().levelCount( 1);
        info.subresourceRange().baseArrayLayer( 0);
        info.subresourceRange().layerCount( 1);
        info.subresourceRange().aspectMask( aspectFlags);

        return info;
    }

    /*253*/static VkPipelineDepthStencilStateCreateInfo depth_stencil_create_info(boolean bDepthTest, boolean bDepthWrite, /*VkCompareOp*/int compareOp)
    {
        final VkPipelineDepthStencilStateCreateInfo info = VkPipelineDepthStencilStateCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
        info.pNext ( 0);

        info.depthTestEnable ( bDepthTest ? (VK_TRUE!=0) : (VK_FALSE!=0));
        info.depthWriteEnable ( bDepthWrite ? (VK_TRUE!=0) : (VK_FALSE!=0));
        info.depthCompareOp ( bDepthTest ? compareOp : VK_COMPARE_OP_ALWAYS);
        info.depthBoundsTestEnable ( VK_FALSE != 0);
        info.minDepthBounds ( 0.0f); // Optional
        info.maxDepthBounds ( 1.0f); // Optional
        info.stencilTestEnable ( VK_FALSE != 0);

        return info;
    }

    /*270*/ public static VkDescriptorSetLayoutBinding descriptorset_layout_binding(/*VkDescriptorType*/long type, /*VkShaderStageFlags*/int stageFlags, int binding)
    {
        final VkDescriptorSetLayoutBinding setbind = VkDescriptorSetLayoutBinding.create();
        setbind.binding ( binding);
        setbind.descriptorCount ( 1);
        setbind.descriptorType ( (int)type);
        setbind.pImmutableSamplers ( null);
        setbind.stageFlags ( stageFlags);

        return setbind;
    }

    /*281*/ public static VkWriteDescriptorSet write_descriptor_buffer(/*VkDescriptorType*/long type, /*VkDescriptorSet*/long dstSet, VkDescriptorBufferInfo.Buffer bufferInfo , int binding)
    {
        final VkWriteDescriptorSet write = VkWriteDescriptorSet.create();
        write.sType ( VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        write.pNext ( 0);

        write.dstBinding ( binding);
        write.dstSet ( dstSet);
        write.descriptorCount ( 1);
        write.descriptorType ( (int)type);
        write.pBufferInfo ( bufferInfo);

        return write;
    }

    /*296*/ public static VkWriteDescriptorSet write_descriptor_image(/*VkDescriptorType*/int type, /*VkDescriptorSet*/long dstSet, VkDescriptorImageInfo imageInfo, int binding)
    {
        final VkWriteDescriptorSet write = VkWriteDescriptorSet.create();
        write.sType ( VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        write.pNext ( 0);

        write.dstBinding ( binding);
        write.dstSet ( dstSet);
        write.descriptorCount ( 1);
        write.descriptorType ( type);

        VkDescriptorImageInfo.Buffer dummy = VkDescriptorImageInfo.create(1); dummy.put(0,imageInfo);

        write.pImageInfo ( /*imageInfo*/dummy);

        return write;
    }

    public static VkSamplerCreateInfo sampler_create_info(/*VkFilter*/int filters) {
        return sampler_create_info(filters,VK_SAMPLER_ADDRESS_MODE_REPEAT);
    }
    /*311*/ public static VkSamplerCreateInfo sampler_create_info(/*VkFilter*/int filters, /*VkSamplerAddressMode*/int samplerAdressMode /*= VK_SAMPLER_ADDRESS_MODE_REPEAT*/)
    {
        final VkSamplerCreateInfo info = VkSamplerCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
        info.pNext ( 0);

        info.magFilter ( filters);
        info.minFilter ( filters);
        info.addressModeU ( samplerAdressMode);
        info.addressModeV ( samplerAdressMode);
        info.addressModeW ( samplerAdressMode);

        return info;
    }

}
