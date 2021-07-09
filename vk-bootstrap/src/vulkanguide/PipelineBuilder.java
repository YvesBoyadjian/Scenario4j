package vulkanguide;

import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class PipelineBuilder {
    public final List<VkPipelineShaderStageCreateInfo> _shaderStages = new ArrayList<>();
    public VkPipelineVertexInputStateCreateInfo _vertexInputInfo;
    public VkPipelineInputAssemblyStateCreateInfo _inputAssembly;
    public final VkViewport.Buffer _viewport = VkViewport.create(1);
    public final VkRect2D.Buffer _scissor = VkRect2D.create(1);
    public VkPipelineRasterizationStateCreateInfo _rasterizer;
    public final VkPipelineColorBlendAttachmentState.Buffer _colorBlendAttachment = VkPipelineColorBlendAttachmentState.create(1);
    public VkPipelineMultisampleStateCreateInfo _multisampling;
    public /*VkPipelineLayout*/long _pipelineLayout;
    public VkPipelineDepthStencilStateCreateInfo _depthStencil;

    public /*VkPipeline*/long build_pipeline(VkDevice device, /*VkRenderPass*/long pass) {
        //make viewport state from our stored viewport and scissor.
        //at the moment we wont support multiple viewports or scissors
        final VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.create();
        viewportState.sType (VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        viewportState.pNext (0);

        viewportState.viewportCount (1);
        viewportState.pViewports (_viewport);
        viewportState.scissorCount (1);
        viewportState.pScissors (_scissor);

        //setup dummy color blending. We arent using transparent objects yet
        //the blending is just "no blend", but we do write to the color attachment
        final VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.create();
        colorBlending.sType (VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        colorBlending.pNext (0);

        colorBlending.logicOpEnable (VK_FALSE != 0);
        colorBlending.logicOp (VK_LOGIC_OP_COPY);
        //colorBlending.attachmentCount (1); java port
        colorBlending.pAttachments (_colorBlendAttachment);

        //build the actual pipeline
        //we now use all of the info structs we have been writing into into this one to create the pipeline
        final VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.create(1);
        pipelineInfo.sType (VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
        pipelineInfo.pNext (0);

        //pipelineInfo.stageCount (_shaderStages.size()); java port
        VkPipelineShaderStageCreateInfo.Buffer dummy = VkPipelineShaderStageCreateInfo.create(_shaderStages.size());
        for(int i=0;i<_shaderStages.size();i++) dummy.put(i,_shaderStages.get(i));
        pipelineInfo.pStages (/*_shaderStages*/dummy);
        pipelineInfo.pVertexInputState (_vertexInputInfo);
        pipelineInfo.pInputAssemblyState (_inputAssembly);
        pipelineInfo.pViewportState (viewportState);
        pipelineInfo.pRasterizationState (_rasterizer);
        pipelineInfo.pMultisampleState (_multisampling);
        pipelineInfo.pColorBlendState (colorBlending);
        pipelineInfo.pDepthStencilState (_depthStencil);
        pipelineInfo.layout (_pipelineLayout);
        pipelineInfo.renderPass (pass);
        pipelineInfo.subpass (0);
        pipelineInfo.basePipelineHandle (VK_NULL_HANDLE);

        //its easy to error out on create graphics pipeline, so we handle it a bit better than the common VK_CHECK case
        /*VkPipeline*/final long[] newPipeline = new long[1];
        if(

        VK10.vkCreateGraphicsPipelines(
                device, VK_NULL_HANDLE, /*1,*/pipelineInfo, null, newPipeline) !=VK_SUCCESS)

        {
            System.out.println( "failed to create pipline");
            return VK_NULL_HANDLE; // failed to create graphics pipeline
        }
        else

        {
            return newPipeline[0];
        }
    }
}
