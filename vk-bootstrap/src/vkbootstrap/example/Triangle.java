package vkbootstrap.example;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import port.Port;
import tests.Common;
import vkbootstrap.*;

import javax.management.RuntimeErrorException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static tests.Common.destroy_window_glfw;
import static vkbootstrap.VkBootstrap.*;

public class Triangle {

    static final String EXAMPLE_BUILD_DIRECTORY = "vk-bootstrap/src/vkbootstrap/example/shaders";

    static final int MAX_FRAMES_IN_FLIGHT = 2;

    public static void main(String[] args) {
        final Init init = new Init();
        final RenderData render_data = new RenderData();

        if (0 != device_initialization (init)) return;
        if (0 != create_swapchain (init)) return;
        if (0 != get_queues (init, render_data)) return;
        if (0 != create_render_pass (init, render_data)) return;
        if (0 != create_graphics_pipeline (init, render_data)) return;
//        if (0 != create_framebuffers (init, render_data)) return;
//        if (0 != create_command_pool (init, render_data)) return;
//        if (0 != create_command_buffers (init, render_data)) return;
        if(0 != recreate_swapchain(init,render_data)) return;
        if (0 != create_sync_objects (init, render_data)) return;

        while (!glfwWindowShouldClose (init.window)) {
            glfwPollEvents ();
            int res = draw_frame (init, render_data);
            if (res != 0) {
                System.out.println( "failed to draw frame ");
                return;
            }
        }
        init.arrow_operator().vkDeviceWaitIdle.invoke (init.device.device[0]);

        cleanup (init, render_data);
    }

    static int device_initialization (Init init) {
        init.window = Common.create_window_glfw ("Vulkan Triangle", true);

        final VkbInstanceBuilder instance_builder = new VkbInstanceBuilder();
        var instance_ret = instance_builder.use_default_debug_messenger ().request_validation_layers ().build ();
        if (instance_ret.not()) {
            System.out.println( instance_ret.error ().message () );
            return -1;
        }
        init.instance = instance_ret.value ();

        init.vk_lib.init(init.instance.instance[0]);

        init.surface = Common.create_surface_glfw (init.instance.instance[0], init.window);

        final VkbPhysicalDeviceSelector phys_device_selector = new VkbPhysicalDeviceSelector(init.instance);
        var phys_device_ret = phys_device_selector.set_surface (init.surface).select ();
        if (phys_device_ret.not()) {
            System.out.println( phys_device_ret.error ().message () );
            return -1;
        }
        final VkbPhysicalDevice physical_device = phys_device_ret.value ();

        final VkbDeviceBuilder device_builder = new VkbDeviceBuilder( physical_device );
        var device_ret = device_builder.build ();
        if (device_ret.not()) {
            System.out.println(device_ret.error ().message ());
            return -1;
        }
        init.device = device_ret.value ();
        init.vk_lib.init(init.device.device[0]);

        return 0;
    }

    static int create_swapchain (final Init init) {

        final VkbSwapchainBuilder swapchain_builder = new VkbSwapchainBuilder( init.device );
        var swap_ret = swapchain_builder.set_old_swapchain (init.swapchain).build ();
        if (swap_ret.not()) {
            System.out.println( swap_ret.error().message () + " " + swap_ret.vk_result() );
            return -1;
        }
        destroy_swapchain(init.swapchain);
        init.swapchain = swap_ret.value ();
        return 0;
    }

    static int get_queues (final Init init, final RenderData data) {
        var gq = init.device.get_queue (VkbQueueType.graphics);
        if (!gq.has_value ()) {
            System.out.println( "failed to get graphics queue: " + gq.error ().message () );
            return -1;
        }
        data.graphics_queue = gq.value ();

        var pq = init.device.get_queue (VkbQueueType.present);
        if (!pq.has_value ()) {
            System.out.println( "failed to get present queue: " + pq.error ().message () );
            return -1;
        }
        data.present_queue = pq.value ();
        return 0;
    }

    static int create_render_pass (final Init init, final RenderData data) {
        final VkAttachmentDescription.Buffer color_attachment_desc_buf = VkAttachmentDescription.create(1);
        final VkAttachmentDescription color_attachment = color_attachment_desc_buf.get(0);
        color_attachment.format( init.swapchain.image_format);
        color_attachment.samples( VK_SAMPLE_COUNT_1_BIT);
        color_attachment.loadOp( VK_ATTACHMENT_LOAD_OP_CLEAR);
        color_attachment.storeOp( VK_ATTACHMENT_STORE_OP_STORE);
        color_attachment.stencilLoadOp( VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        color_attachment.stencilStoreOp( VK_ATTACHMENT_STORE_OP_DONT_CARE);
        color_attachment.initialLayout( VK_IMAGE_LAYOUT_UNDEFINED);
        color_attachment.finalLayout( VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        final VkAttachmentReference.Buffer color_attachment_buf = VkAttachmentReference.create(1);
        final VkAttachmentReference color_attachment_ref = color_attachment_buf.get(0);
        color_attachment_ref.attachment( 0);
        color_attachment_ref.layout( VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        final VkSubpassDescription.Buffer subpass_buf = VkSubpassDescription.create(1);
        final VkSubpassDescription subpass = subpass_buf.get(0);
        subpass.pipelineBindPoint( VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.colorAttachmentCount( 1);
        subpass.pColorAttachments( color_attachment_buf);

        final VkSubpassDependency.Buffer dependency_buf = VkSubpassDependency.create(1);
        final VkSubpassDependency dependency = dependency_buf.get(0);
        dependency.srcSubpass( VK_SUBPASS_EXTERNAL);
        dependency.dstSubpass( 0);
        dependency.srcStageMask( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.srcAccessMask( 0);
        dependency.dstStageMask( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.dstAccessMask( VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        final VkRenderPassCreateInfo render_pass_info = VkRenderPassCreateInfo.create();
        render_pass_info.sType( VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
        //render_pass_info.attachmentCount( 1); java port
        render_pass_info.pAttachments( color_attachment_desc_buf);
        //render_pass_info.subpassCount( 1); java port
        render_pass_info.pSubpasses( subpass_buf);
        //render_pass_info.dependencyCount( 1); java port
        render_pass_info.pDependencies( dependency_buf);

        if (init.arrow_operator().vkCreateRenderPass.invoke (init.device.device[0], render_pass_info, null, data.render_pass) != VK_SUCCESS) {
            System.out.println( "failed to create render pass");
            return -1; // failed to create render pass!
        }
        return 0;
    }

    static ByteBuffer readFile (final String filename) {
        //std::ifstream file (filename, std::ios::ate | std::ios::binary);

        ByteBuffer buffer;
        try {
            FileInputStream fis = new FileInputStream(filename);
            FileChannel fc = fis.getChannel();
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            fc.close();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException("failed to open file!");
        }
        return buffer;
    }

    static /*VkShaderModule*/long createShaderModule (final Init init, final ByteBuffer code) {
        final VkShaderModuleCreateInfo create_info = VkShaderModuleCreateInfo.create();
        create_info.sType( VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
        //create_info.codeSize( code.size ()); java port
        create_info.pCode( code );

        final /*VkShaderModule*/long[] shaderModule = new long[1];
        if (init.arrow_operator().vkCreateShaderModule.invoke(init.device.device[0], create_info, null, shaderModule) != VK_SUCCESS) {
            return VK_NULL_HANDLE; // failed to create shader module
        }

        return shaderModule[0];
    }

    static int create_graphics_pipeline (final Init init, final RenderData data) {
        var vert_code = readFile(EXAMPLE_BUILD_DIRECTORY + "/vert.spv");
        var frag_code = readFile(EXAMPLE_BUILD_DIRECTORY + "/frag.spv");

        /*VkShaderModule*/long vert_module = createShaderModule (init, vert_code);
        /*VkShaderModule*/long frag_module = createShaderModule (init, frag_code);
        if (vert_module == VK_NULL_HANDLE || frag_module == VK_NULL_HANDLE) {
            System.out.println( "failed to create shader module");
            return -1; // failed to create shader modules
        }

        final VkPipelineShaderStageCreateInfo.Buffer shader_stages = VkPipelineShaderStageCreateInfo.create(2); // java port

        final VkPipelineShaderStageCreateInfo vert_stage_info = shader_stages.get(0); // java port
        vert_stage_info.sType( VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        vert_stage_info.stage( VK_SHADER_STAGE_VERTEX_BIT);
        vert_stage_info.module( vert_module);
        vert_stage_info.pName( memUTF8("main"));

        final VkPipelineShaderStageCreateInfo frag_stage_info = shader_stages.get(1); // java port
        frag_stage_info.sType( VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        frag_stage_info.stage( VK_SHADER_STAGE_FRAGMENT_BIT);
        frag_stage_info.module( frag_module);
        frag_stage_info.pName( memUTF8("main"));

        //VkPipelineShaderStageCreateInfo shader_stages[] = { vert_stage_info, frag_stage_info }; java port

        final VkPipelineVertexInputStateCreateInfo vertex_input_info = VkPipelineVertexInputStateCreateInfo.create();
        vertex_input_info.sType( VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        //vertex_input_info.vertexBindingDescriptionCount( 0); java port
        VkVertexInputBindingDescription.Buffer dummy1 = VkVertexInputBindingDescription.create(0); // java port
        vertex_input_info.pVertexBindingDescriptions( dummy1); // java port
        //vertex_input_info.vertexAttributeDescriptionCount( 0); java port
        VkVertexInputAttributeDescription.Buffer dummy2 = VkVertexInputAttributeDescription.create(0); // java port
        vertex_input_info.pVertexAttributeDescriptions( dummy2); // java port

        final VkPipelineInputAssemblyStateCreateInfo input_assembly = VkPipelineInputAssemblyStateCreateInfo.create();
        input_assembly.sType( VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        input_assembly.topology( VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
        input_assembly.primitiveRestartEnable( VK_FALSE != 0);

        final VkViewport.Buffer viewport_buf = VkViewport.create(1);
        final VkViewport viewport = viewport_buf.get(0);
        viewport.x( 0.0f);
        viewport.y( 0.0f);
        viewport.width( (float)init.swapchain.extent.width());
        viewport.height( (float)init.swapchain.extent.height());
        viewport.minDepth( 0.0f);
        viewport.maxDepth( 1.0f);

        final VkRect2D.Buffer scissor_buf = VkRect2D.create(1);
        final VkRect2D scissor = scissor_buf.get(0);
        final VkOffset2D dummy = VkOffset2D.create(); dummy.x(0); dummy.y(0);
        scissor.offset( dummy);
        scissor.extent( init.swapchain.extent);

        final VkPipelineViewportStateCreateInfo viewport_state = VkPipelineViewportStateCreateInfo.create();
        viewport_state.sType( VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        viewport_state.viewportCount( 1);
        viewport_state.pViewports( viewport_buf);
        viewport_state.scissorCount( 1);
        viewport_state.pScissors( scissor_buf);

        final VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.create();
        rasterizer.sType( VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        rasterizer.depthClampEnable( VK_FALSE != 0);
        rasterizer.rasterizerDiscardEnable( VK_FALSE != 0);
        rasterizer.polygonMode( VK_POLYGON_MODE_FILL);
        rasterizer.lineWidth( 1.0f);
        rasterizer.cullMode( VK_CULL_MODE_BACK_BIT);
        rasterizer.frontFace( VK_FRONT_FACE_CLOCKWISE);
        rasterizer.depthBiasEnable( VK_FALSE != 0);

        final VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.create();
        multisampling.sType( VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        multisampling.sampleShadingEnable( VK_FALSE != 0);
        multisampling.rasterizationSamples( VK_SAMPLE_COUNT_1_BIT);

        final VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment_buf = VkPipelineColorBlendAttachmentState.create(1);
        final VkPipelineColorBlendAttachmentState colorBlendAttachment = colorBlendAttachment_buf.get(0);
        colorBlendAttachment.colorWriteMask( VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT |
                VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
        colorBlendAttachment.blendEnable( VK_FALSE != 0);

        final VkPipelineColorBlendStateCreateInfo color_blending = VkPipelineColorBlendStateCreateInfo.create();
        color_blending.sType( VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        color_blending.logicOpEnable( VK_FALSE != 0);
        color_blending.logicOp( VK_LOGIC_OP_COPY);
        //color_blending.attachmentCount( 1); java port
        color_blending.pAttachments( colorBlendAttachment_buf);
        color_blending.blendConstants(0, 0.0f);
        color_blending.blendConstants(1, 0.0f);
        color_blending.blendConstants(2, 0.0f);
        color_blending.blendConstants(3, 0.0f);

        final VkPipelineLayoutCreateInfo pipeline_layout_info = VkPipelineLayoutCreateInfo.create();
        pipeline_layout_info.sType( VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
        //pipeline_layout_info.setLayoutCount( 0); java port
        pipeline_layout_info.pSetLayouts(BufferUtils.createLongBuffer(0)); // java port
        //pipeline_layout_info.pushConstantRangeCount( 0); java port
        VkPushConstantRange.Buffer dummy3 = VkPushConstantRange.create(0); // java port
        pipeline_layout_info.pPushConstantRanges( dummy3); // java port

        if (init.arrow_operator().vkCreatePipelineLayout.invoke (
                init.device.device[0], pipeline_layout_info, null, data.pipeline_layout) != VK_SUCCESS) {
            System.out.println( "failed to create pipeline layout");
            return -1; // failed to create pipeline layout
        }

        final int[] dynamic_states = { VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR };

        final VkPipelineDynamicStateCreateInfo dynamic_info = VkPipelineDynamicStateCreateInfo.create();
        dynamic_info.sType( VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
        //dynamic_info.dynamicStateCount( dynamic_states.length); java port
        dynamic_info.pDynamicStates( Port.toIntBuffer(dynamic_states));

        final VkGraphicsPipelineCreateInfo.Buffer pipeline_info_buf = VkGraphicsPipelineCreateInfo.create(1);
        final VkGraphicsPipelineCreateInfo pipeline_info = pipeline_info_buf.get(0);
        pipeline_info.sType( VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
        //pipeline_info.stageCount( 2); java port
        pipeline_info.pStages( shader_stages);
        pipeline_info.pVertexInputState( vertex_input_info);
        pipeline_info.pInputAssemblyState( input_assembly);
        pipeline_info.pViewportState( viewport_state);
        pipeline_info.pRasterizationState( rasterizer);
        pipeline_info.pMultisampleState( multisampling);
        pipeline_info.pColorBlendState( color_blending);
        pipeline_info.pDynamicState( dynamic_info);
        pipeline_info.layout( data.pipeline_layout[0]);
        pipeline_info.renderPass( data.render_pass[0]);
        pipeline_info.subpass( 0);
        pipeline_info.basePipelineHandle( VK_NULL_HANDLE);

        if (init.arrow_operator().vkCreateGraphicsPipelines.invoke (
                init.device.device[0], VK_NULL_HANDLE,/* 1,*/ pipeline_info_buf, null, data.graphics_pipeline) != VK_SUCCESS) {
            System.out.println( "failed to create pipline");
            return -1; // failed to create graphics pipeline
        }

        init.arrow_operator().vkDestroyShaderModule.invoke (init.device.device[0], frag_module, null);
        init.arrow_operator().vkDestroyShaderModule.invoke (init.device.device[0], vert_module, null);
        return 0;
    }

    static int create_framebuffers (final Init init, final RenderData data) {
        data.swapchain_images.clear(); data.swapchain_images.addAll(init.swapchain.get_images ().value ());
        data.swapchain_image_views.clear(); data.swapchain_image_views.addAll( init.swapchain.get_image_views ().value ());

        //data.framebuffers.resize (data.swapchain_image_views.size ()); java port
        data.framebuffers.clear();

        for (int i = 0; i < data.swapchain_image_views.size (); i++) {
            /*VkImageView*/long[] attachments = { data.swapchain_image_views.get(i) };

            final VkFramebufferCreateInfo framebuffer_info = VkFramebufferCreateInfo.create();
            framebuffer_info.sType( VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebuffer_info.renderPass( data.render_pass[0]);
            //framebuffer_info.attachmentCount( 1); java port
            framebuffer_info.pAttachments( Port.toLongBuffer(attachments));
            framebuffer_info.width( init.swapchain.extent.width());
            framebuffer_info.height( init.swapchain.extent.height());
            framebuffer_info.layers( 1);

            final long[] p_framebuffer = new long[1];

            if (init.arrow_operator().vkCreateFramebuffer.invoke (init.device.device[0], framebuffer_info, null, p_framebuffer) != VK_SUCCESS) {
                return -1; // failed to create framebuffer
            }
            data.framebuffers.add(p_framebuffer[0]);
        }
        return 0;
    }

    static int create_command_pool (final Init init, final RenderData data) {
        final VkCommandPoolCreateInfo pool_info = VkCommandPoolCreateInfo.create();
        pool_info.sType( VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
        pool_info.queueFamilyIndex( init.device.get_queue_index (VkbQueueType.graphics).value ());

        if (init.arrow_operator().vkCreateCommandPool.invoke (init.device.device[0], pool_info, null, data.command_pool) != VK_SUCCESS) {
            System.out.println( "failed to create command pool");
            return -1; // failed to create command pool
        }
        return 0;
    }

    static int create_command_buffers (final Init init, final RenderData data) {
        //data.command_buffers.resize (data.framebuffers.size ()); java port

        final VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.create();
        allocInfo.sType( VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
        allocInfo.commandPool( data.command_pool[0]);
        allocInfo.level( VK_COMMAND_BUFFER_LEVEL_PRIMARY);
        allocInfo.commandBufferCount( (int)/*data.command_buffers.size ()*/data.framebuffers.size ()); // java port

        if (init.arrow_operator().vkAllocateCommandBuffers.invoke (init.device.device[0], allocInfo, data.command_buffers, data.framebuffers.size ()) != VK_SUCCESS) {
            return -1; // failed to allocate command buffers;
        }

        for (int i = 0; i < data.command_buffers.size (); i++) {
            final VkCommandBufferBeginInfo begin_info = VkCommandBufferBeginInfo.create();
            begin_info.sType( VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            if (init.arrow_operator().vkBeginCommandBuffer.invoke (data.command_buffers.get(i), begin_info) != VK_SUCCESS) {
                return -1; // failed to begin recording command buffer
            }

            final VkRenderPassBeginInfo render_pass_info = VkRenderPassBeginInfo.create();
            render_pass_info.sType( VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            render_pass_info.renderPass( data.render_pass[0]);
            render_pass_info.framebuffer( data.framebuffers.get(i));
            VkOffset2D dummy = VkOffset2D.create();dummy.x( 0); dummy.y( 0);
            render_pass_info.renderArea().offset( dummy );
            render_pass_info.renderArea().extent( init.swapchain.extent);
            VkClearValue.Buffer clearColor = VkClearValue.create(1);
            VkClearColorValue dummy2 = VkClearColorValue.create();
            dummy2.float32(0,0.0f);
            dummy2.float32(1,0.0f);
            dummy2.float32(2,0.0f);
            dummy2.float32(3,1.0f);
            clearColor.get(0).color(dummy2);
            //render_pass_info.clearValueCount( 1); java port
            render_pass_info.pClearValues ( clearColor);

            final VkViewport.Buffer viewport = VkViewport.create(1);
            viewport.x( 0.0f);
            viewport.y( 0.0f);
            viewport.width( (float)init.swapchain.extent.width());
            viewport.height( (float)init.swapchain.extent.height());
            viewport.minDepth( 0.0f);
            viewport.maxDepth( 1.0f);

            final VkRect2D.Buffer scissor = VkRect2D.create(1);
            VkOffset2D dummy3 = VkOffset2D.create();
            dummy3.x(0);
            dummy3.y(0);
            scissor.get(0).offset( dummy3);
            scissor.get(0).extent( init.swapchain.extent);

            init.arrow_operator().vkCmdSetViewport.invoke (data.command_buffers.get(i), 0, /*1,*/ viewport);
            init.arrow_operator().vkCmdSetScissor.invoke (data.command_buffers.get(i), 0, /*1,*/ scissor);

            init.arrow_operator().vkCmdBeginRenderPass.invoke (data.command_buffers.get(i), render_pass_info, VK_SUBPASS_CONTENTS_INLINE);

            init.arrow_operator().vkCmdBindPipeline.invoke (data.command_buffers.get(i), VK_PIPELINE_BIND_POINT_GRAPHICS, data.graphics_pipeline[0]);

            init.arrow_operator().vkCmdDraw.invoke (data.command_buffers.get(i), 3, 1, 0, 0);

            init.arrow_operator().vkCmdEndRenderPass.invoke (data.command_buffers.get(i));

            if (init.arrow_operator().vkEndCommandBuffer.invoke (data.command_buffers.get(i)) != VK_SUCCESS) {
                System.out.println( "failed to record command buffer");
                return -1; // failed to record command buffer!
            }
        }
        return 0;
    }

    static int create_sync_objects (final Init init, final RenderData data) {
        data.available_semaphores.clear(); for(int i=0;i<MAX_FRAMES_IN_FLIGHT;i++) data.available_semaphores.add(new long[1]);//resize (MAX_FRAMES_IN_FLIGHT);
        data.finished_semaphore.clear(); for(int i=0;i<MAX_FRAMES_IN_FLIGHT;i++) data.finished_semaphore.add(new long[1]);//resize (MAX_FRAMES_IN_FLIGHT);
        data.in_flight_fences.clear(); for(int i=0;i<MAX_FRAMES_IN_FLIGHT;i++) data.in_flight_fences.add(new long[1]);//resize (MAX_FRAMES_IN_FLIGHT);
        data.image_in_flight.clear(); for(int i=0;i<init.swapchain.image_count;i++) data.image_in_flight.add(new long[1]);//resize (init.swapchain.image_count, VK_NULL_HANDLE);

        final VkSemaphoreCreateInfo semaphore_info = VkSemaphoreCreateInfo.create();
        semaphore_info.sType( VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

        final VkFenceCreateInfo fence_info = VkFenceCreateInfo.create();
        fence_info.sType( VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
        fence_info.flags( VK_FENCE_CREATE_SIGNALED_BIT);

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            if (init.arrow_operator().vkCreateSemaphore.invoke (init.device.device[0], semaphore_info, null, data.available_semaphores.get(i)) != VK_SUCCESS ||
                    init.arrow_operator().vkCreateSemaphore.invoke (init.device.device[0], semaphore_info, null, data.finished_semaphore.get(i)) != VK_SUCCESS ||
                    init.arrow_operator().vkCreateFence.invoke (init.device.device[0], fence_info, null, data.in_flight_fences.get(i)) != VK_SUCCESS) {
                System.out.println( "failed to create sync objects");
                return -1; // failed to create synchronization objects for a frame
            }
        }
        return 0;
    }

    static int recreate_swapchain (final Init init, final RenderData data) {
        init.arrow_operator().vkDeviceWaitIdle.invoke (init.device.device[0]);

        init.arrow_operator().vkDestroyCommandPool.invoke (init.device.device[0], data.command_pool[0], null);

        for (var framebuffer : data.framebuffers) {
            init.arrow_operator().vkDestroyFramebuffer.invoke (init.device.device[0], framebuffer, null);
        }

        init.swapchain.destroy_image_views (data.swapchain_image_views);

        if (0 != create_swapchain (init)) return -1;
        if (0 != create_framebuffers (init, data)) return -1;
        if (0 != create_command_pool (init, data)) return -1;
        if (0 != create_command_buffers (init, data)) return -1;
        return 0;
    }

    static int draw_frame (final Init init, final RenderData data) {
        init.arrow_operator().vkWaitForFences.invoke (init.device.device[0], /*1,*/ data.in_flight_fences.get((int)data.current_frame), VK_TRUE != 0, Port.UINT64_MAX);

        final int[] image_index = new int[1];
        /*VkResult*/int result = init.arrow_operator().vkAcquireNextImageKHR.invoke (init.device.device[0],
                init.swapchain.swapchain[0],
                Port.UINT64_MAX,
                data.available_semaphores.get((int)data.current_frame)[0],
                VK_NULL_HANDLE,
                image_index);

        if (result == VK_ERROR_OUT_OF_DATE_KHR) {
            return recreate_swapchain (init, data);
        } else if (result != VK_SUCCESS && result != VK_SUBOPTIMAL_KHR) {
            System.out.println( "failed to acquire swapchain image. Error " + result );
            return -1;
        }

        if (data.image_in_flight.get(image_index[0])[0] != VK_NULL_HANDLE) {
            init.arrow_operator().vkWaitForFences.invoke (init.device.device[0], /*1,*/ data.image_in_flight.get(image_index[0]), VK_TRUE != 0, Port.UINT64_MAX);
        }
        data.image_in_flight.get(image_index[0])[0] = data.in_flight_fences.get((int)data.current_frame)[0];

        final VkSubmitInfo submitInfo = VkSubmitInfo.create();
        submitInfo.sType( VK_STRUCTURE_TYPE_SUBMIT_INFO);

        /*VkSemaphore*/
        LongBuffer wait_semaphores = memAllocLong(1); wait_semaphores.put(0, data.available_semaphores.get((int)data.current_frame)[0]);
        /*VkPipelineStageFlags*/
        IntBuffer wait_stages = memAllocInt(1); wait_stages.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT );
        submitInfo.waitSemaphoreCount( 1);
        submitInfo.pWaitSemaphores( wait_semaphores);
        submitInfo.pWaitDstStageMask( wait_stages);

        //submitInfo.commandBufferCount( 1); java port
        PointerBuffer pCommandBuffers = memAllocPointer(1); pCommandBuffers.put(0,data.command_buffers.get(image_index[0]).address());
        submitInfo.pCommandBuffers( pCommandBuffers);

        LongBuffer /*VkSemaphore*/ signal_semaphores= memAllocLong(1); signal_semaphores.put(0, data.finished_semaphore.get((int)data.current_frame)[0] );
        //submitInfo.signalSemaphoreCount( 1); java port
        submitInfo.pSignalSemaphores( signal_semaphores);

        init.arrow_operator().vkResetFences.invoke (init.device.device[0], /*1,*/ data.in_flight_fences.get((int)data.current_frame));

        if (init.arrow_operator().vkQueueSubmit.invoke (data.graphics_queue, /*1,*/ submitInfo, data.in_flight_fences.get((int)data.current_frame)[0]) != VK_SUCCESS) {
            System.out.println( "failed to submit draw command buffer");
            return -1; //"failed to submit draw command buffer
        }

        final VkPresentInfoKHR present_info = VkPresentInfoKHR.create();
        present_info.sType( VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);

        //present_info.waitSemaphoreCount( 1); java port
        present_info.pWaitSemaphores( signal_semaphores);

        /*VkSwapchainKHR*/LongBuffer swapChains = memAllocLong(1);swapChains.put(0, init.swapchain.swapchain[0] );
        present_info.swapchainCount( 1);
        present_info.pSwapchains( swapChains);

        IntBuffer pImageIndex = memAllocInt(1); // java port
        pImageIndex.put(0,image_index[0]); // java port
        present_info.pImageIndices( /*image_index[0]*/pImageIndex);

        result = init.arrow_operator().vkQueuePresentKHR.invoke (data.present_queue, present_info);
        if (result == VK_ERROR_OUT_OF_DATE_KHR || result == VK_SUBOPTIMAL_KHR) {
            return recreate_swapchain (init, data);
        } else if (result != VK_SUCCESS) {
            System.out.println( "failed to present swapchain image");
            return -1;
        }

        data.current_frame = (data.current_frame + 1) % MAX_FRAMES_IN_FLIGHT;
        return 0;
    }

    static void cleanup (final Init init, final RenderData data) {
        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            init.arrow_operator().vkDestroySemaphore.invoke (init.device.device[0], data.finished_semaphore.get(i)[0], null);
            init.arrow_operator().vkDestroySemaphore.invoke (init.device.device[0], data.available_semaphores.get(i)[0], null);
            init.arrow_operator().vkDestroyFence.invoke (init.device.device[0], data.in_flight_fences.get(i)[0], null);
        }

        init.arrow_operator().vkDestroyCommandPool.invoke (init.device.device[0], data.command_pool[0], null);

        for (var framebuffer : data.framebuffers) {
            init.arrow_operator().vkDestroyFramebuffer.invoke (init.device.device[0], framebuffer, null);
        }

        init.arrow_operator().vkDestroyPipeline.invoke (init.device.device[0], data.graphics_pipeline[0], null);
        init.arrow_operator().vkDestroyPipelineLayout.invoke (init.device.device[0], data.pipeline_layout[0], null);
        init.arrow_operator().vkDestroyRenderPass.invoke (init.device.device[0], data.render_pass[0], null);

        init.swapchain.destroy_image_views (data.swapchain_image_views);

        destroy_swapchain (init.swapchain);
        destroy_device (init.device);
        destroy_surface(init.instance, init.surface);
        destroy_instance (init.instance);
        destroy_window_glfw (init.window);
    }
}
