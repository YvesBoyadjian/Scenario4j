package vulkanguide;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.*;
import port.Port;
import vkbootstrap.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.libc.LibCString.memcpy;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static tests.Common.destroy_window_glfw;

public class VulkanEngine {

    public static final/*constexpr unsigned*/ int FRAME_OVERLAP = 2;

    public static final boolean bUseValidationLayers = true;

    public boolean _isInitialized = false;
    public int _frameNumber = 0;
    public int _selectedShader = 0;

    public final VkExtent2D _windowExtent = VkExtent2D.create();

    public/*struct SDL_Window**/long _window = /*nullptr*/0;

    public VkInstance _instance;
    public /*VkDebugUtilsMessengerEXT*/long _debug_messenger;
    public VkPhysicalDevice _chosenGPU;
    public VkDevice _device;

    public final VkPhysicalDeviceProperties _gpuProperties = VkPhysicalDeviceProperties.create();

    public final FrameData[] _frames = new FrameData[FRAME_OVERLAP];

    public VkQueue _graphicsQueue;
    public int _graphicsQueueFamily;

    public /*VkRenderPass*/final long[] _renderPass = new long[1];

    public /*VkSurfaceKHR*/final long[] _surface = new long[1];
    public /*VkSwapchainKHR*/long _swapchain;
    public /*VkFormat*/long _swachainImageFormat;

    public List</*VkFramebuffer*/long[]> _framebuffers = new ArrayList<>();
    public List</*VkImage*/Long> _swapchainImages = new ArrayList<>();
    public List</*VkImageView*/Long> _swapchainImageViews = new ArrayList<>();

    public final DeletionQueue _mainDeletionQueue = new DeletionQueue();

    public /*VmaAllocator*/long _allocator; //vma lib allocator

    //depth resources
    public /*VkImageView*/final long[] _depthImageView = new long[1];
    public final AllocatedImage _depthImage = new AllocatedImage();

    //the format for the depth image
    public /*VkFormat*/long _depthFormat;

    public /*VkDescriptorPool*/final long[] _descriptorPool = new long[1];

    public /*VkDescriptorSetLayout*/final long[] _globalSetLayout = new long[1];
    public /*VkDescriptorSetLayout*/final long[] _objectSetLayout = new long[1];
    public /*VkDescriptorSetLayout*/final long[] _singleTextureSetLayout = new long[1];

    public final GPUSceneData _sceneParameters = new GPUSceneData();
    public AllocatedBuffer _sceneParameterBuffer = new AllocatedBuffer();

    public final UploadContext _uploadContext = new UploadContext();

    //default array of renderable objects
    public final List<RenderObject> _renderables = new ArrayList<>();

    public final Map<String, Material> _materials = new HashMap<>();
    public final Map<String, Mesh> _meshes = new HashMap<>();
    public final Map<String, Texture> _loadedTextures = new HashMap<>();

    public VulkanEngine() {
        _windowExtent.width(1700);
        _windowExtent.height(900);

        for( int i = 0; i< FRAME_OVERLAP; i++) {
            _frames[i] = new FrameData();
        }
    }

    public void VK_CHECK(int err) {
        if (err != 0)
        {
            System.out.println("Detected Vulkan error: " + err );
            System.exit(-1);
        }
    }

    /*37*/ public void init()
    {
        // We initialize SDL and create a window with it.
        //SDL_Init(SDL_INIT_VIDEO);
        glfwInit();

        //SDL_WindowFlags window_flags = (SDL_WindowFlags)(SDL_WINDOW_VULKAN);
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        _window = /*SDL_CreateWindow(
                "Vulkan Engine",
                SDL_WINDOWPOS_UNDEFINED,
                SDL_WINDOWPOS_UNDEFINED,
                _windowExtent.width,
                _windowExtent.height,
                window_flags
        );*/
                glfwCreateWindow(_windowExtent.width(), _windowExtent.height(), "Vulkan Engine", 0, 0);

        init_vulkan();

        init_swapchain();

        init_default_renderpass();

        init_framebuffers();

        init_commands();

        init_sync_structures();

        init_descriptors();

        init_pipelines();

        load_images();

        load_meshes();

        init_scene();

        //everything went fine
        _isInitialized = true;
    }
    /*77*/ void cleanup()
    {
        if (_isInitialized) {

            //make sure the gpu has stopped doing its things
            vkDeviceWaitIdle(_device);

            _mainDeletionQueue.flush();

            KHRSurface.vkDestroySurfaceKHR(_instance, _surface[0], null);

            vkDestroyDevice(_device, null);
            VkBootstrap.destroy_debug_utils_messenger(_instance, _debug_messenger);
            vkDestroyInstance(_instance, null);

            //SDL_DestroyWindow(_window);
            destroy_window_glfw (_window);
        }
    }

    /*96*/ void draw()
    {

        //wait until the gpu has finished rendering the last frame. Timeout of 1 second
        VK_CHECK(VK10.vkWaitForFences(_device, /*1,*/ get_current_frame()._renderFence[0], true, 1000000000l));
        VK_CHECK(vkResetFences(_device, /*1,*/ get_current_frame()._renderFence[0]));

        //now that we are sure that the commands finished executing, we can safely reset the command buffer to begin recording again.
        VK_CHECK(vkResetCommandBuffer(get_current_frame()._mainCommandBuffer, 0));

        //request image from the swapchain
        final int[] swapchainImageIndex = new int[1];
        VK_CHECK(KHRSwapchain.vkAcquireNextImageKHR(_device, _swapchain, 1000000000l, get_current_frame()._presentSemaphore[0], 0, swapchainImageIndex));

        //naming it cmd for shorter writing
        VkCommandBuffer cmd = get_current_frame()._mainCommandBuffer;

        //begin the command buffer recording. We will use this command buffer exactly once, so we want to let vulkan know that
        VkCommandBufferBeginInfo cmdBeginInfo = VkInit.command_buffer_begin_info(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

        VK_CHECK(vkBeginCommandBuffer(cmd, cmdBeginInfo));

        //make a clear-color from frame number. This will flash with a 120 frame period.
        final VkClearValue clearValue = VkClearValue.create();
        float flash = (float)abs(sin(_frameNumber / 120.f));

        VkClearColorValue dummy = VkClearColorValue.create();
        dummy.float32(0,0.0f);
        dummy.float32(1,0.0f);
        dummy.float32(2,flash);
        dummy.float32(3,1.0f);

        clearValue.color ( dummy/*{ { 0.0f, 0.0f, flash, 1.0f } }*/);

        //clear depth at 1
        final VkClearValue depthClear = VkClearValue.create();
        depthClear.depthStencil().depth ( 1.f);

        //start the main renderpass.
        //We will use the clear color from above, and the framebuffer of the index the swapchain gave us
        VkRenderPassBeginInfo rpInfo = VkInit.renderpass_begin_info(_renderPass[0], _windowExtent, _framebuffers.get(swapchainImageIndex[0])[0]);

        //connect clear values
        // rpInfo.clearValueCount ( 2); java port

        VkClearValue.Buffer clearValues = VkClearValue.create(2);
        clearValues.put(0, clearValue);
        clearValues.put(1, depthClear);

        rpInfo.pClearValues ( clearValues);

        vkCmdBeginRenderPass(cmd, rpInfo, VK_SUBPASS_CONTENTS_INLINE);

        draw_objects(cmd, _renderables/*.data()*/, _renderables.size());

        //finalize the render pass
        vkCmdEndRenderPass(cmd);
        //finalize the command buffer (we can no longer add commands, but it can now be executed)
        VK_CHECK(vkEndCommandBuffer(cmd));

        //prepare the submission to the queue.
        //we want to wait on the _presentSemaphore, as that semaphore is signaled when the swapchain is ready
        //we will signal the _renderSemaphore, to signal that rendering has finished

        VkSubmitInfo submit = VkInit.submit_info(cmd);
        /*VkPipelineStageFlags*/int waitStage = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;

        IntBuffer dummy7 = memAllocInt(1);
        dummy7.put(0,waitStage);

        submit.pWaitDstStageMask ( /*waitStage*/dummy7);

        submit.waitSemaphoreCount ( 1);

        LongBuffer dummy6 = memAllocLong(1);
        dummy6.put(0,get_current_frame()._presentSemaphore[0]);

        submit.pWaitSemaphores ( /*get_current_frame()._presentSemaphore*/dummy6);

        //submit.signalSemaphoreCount ( 1); java port

        LongBuffer dummy5 = memAllocLong(1);
        dummy5.put(0,get_current_frame()._renderSemaphore[0]);

        submit.pSignalSemaphores ( /*get_current_frame()._renderSemaphore*/dummy5);

        //submit command buffer to the queue and execute it.
        // _renderFence will now block until the graphic commands finish execution
        VK_CHECK(vkQueueSubmit(_graphicsQueue, /*1,*/ submit, get_current_frame()._renderFence[0]));

        //prepare present
        // this will put the image we just rendered to into the visible window.
        // we want to wait on the _renderSemaphore for that,
        // as its necessary that drawing commands have finished before the image is displayed to the user
        final VkPresentInfoKHR presentInfo = VkInit.present_info();

        LongBuffer dummy4 = memAllocLong(1);
        dummy4.put(0,_swapchain);

        presentInfo.pSwapchains ( /*_swapchain*/dummy4);
        presentInfo.swapchainCount ( 1);

        LongBuffer dummy3 = memAllocLong(1);
        dummy3.put(0,get_current_frame()._renderSemaphore[0]);

        presentInfo.pWaitSemaphores ( /*get_current_frame()._renderSemaphore*/dummy3);
        //presentInfo.waitSemaphoreCount ( 1); java port

        IntBuffer dummy2 = memAllocInt(1);
        dummy2.put(0,swapchainImageIndex[0]);

        presentInfo.pImageIndices ( /*swapchainImageIndex*/dummy2);

        VK_CHECK(vkQueuePresentKHR(_graphicsQueue, presentInfo));

        //increase the number of frames drawn
        _frameNumber++;
    }

    /*186*/ public void run()
    {
        //SDL_Event e;
        boolean bQuit = false;

        //main loop
        while (!glfwWindowShouldClose (_window))
        {
            //Handle events on queue
            glfwPollEvents ();
                //close the window when user alt-f4s or clicks the X button
//                if (e.type == SDL_QUIT)
//                {
//                    bQuit = true;
//                }
//                else if (e.type == SDL_KEYDOWN)
//                {
//                    if (e.key.keysym.sym == SDLK_SPACE)
//                    {
//                        _selectedShader += 1;
//                        if (_selectedShader > 1)
//                        {
//                            _selectedShader = 0;
//                        }
//                    }
//                }


            draw();
        }
    }

    /*219*/ FrameData get_current_frame()
    {
        return _frames[_frameNumber % FRAME_OVERLAP];
    }

    /*230*/ public void init_vulkan()
    {
        final VkbInstanceBuilder builder = new VkbInstanceBuilder();

        //make the vulkan instance, with basic debug features
        var inst_ret = builder.set_app_name("Example Vulkan Application")
                .request_validation_layers(bUseValidationLayers)
                .use_default_debug_messenger()
                .require_api_version(1, 1, 0)
                .build();

        VkbInstance vkb_inst = inst_ret.value();

        //grab the instance
        _instance = vkb_inst.instance[0];
        _debug_messenger = vkb_inst.debug_messenger[0];

        //SDL_Vulkan_CreateSurface(_window, _instance, &_surface);
        glfwCreateWindowSurface(_instance, _window, /*allocator*/null, _surface);

        //use vkbootstrap to select a gpu.
        //We want a gpu that can write to the SDL surface and supports vulkan 1.2
        final VkbPhysicalDeviceSelector selector = new VkbPhysicalDeviceSelector( vkb_inst );
        final VkbPhysicalDevice physicalDevice = selector
            .set_minimum_version(1, 1)
            .set_surface(_surface[0])
            .select()
            .value();

        //create the final vulkan device

        final VkbDeviceBuilder deviceBuilder = new VkbDeviceBuilder( physicalDevice );

        final VkbDevice vkbDevice = deviceBuilder.build().value();

        // Get the VkDevice handle used in the rest of a vulkan application
        _device = vkbDevice.device[0];
        _chosenGPU = physicalDevice.physical_device;

        // use vkbootstrap to get a Graphics queue
        _graphicsQueue = vkbDevice.get_queue(VkbQueueType.graphics).value();

        _graphicsQueueFamily = vkbDevice.get_queue_index(VkbQueueType.graphics).value();

        //initialize the memory allocator
        VmaAllocatorCreateInfo allocatorInfo = VmaAllocatorCreateInfo.create();
        allocatorInfo.physicalDevice( _chosenGPU );
        allocatorInfo.device( _device );
        allocatorInfo.instance( _instance );

        VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.create();
        vmaVulkanFunctions.set( _instance, _device );
        allocatorInfo.pVulkanFunctions( vmaVulkanFunctions ); // java port

        PointerBuffer pb = memAllocPointer(1);
        vmaCreateAllocator(allocatorInfo, /*_allocator*/pb);
        _allocator = pb.get(0);
        memFree(pb);

        _mainDeletionQueue.push_function(() -> {
        vmaDestroyAllocator(_allocator);
    });

        vkGetPhysicalDeviceProperties(_chosenGPU, _gpuProperties);

        System.out.println( "The gpu has a minimum buffer alignement of " + _gpuProperties.limits().minUniformBufferOffsetAlignment() );

    }

    /*290*/ public void init_swapchain()
    {
        final VkbSwapchainBuilder swapchainBuilder = new VkbSwapchainBuilder(_chosenGPU,_device,_surface[0] );

        VkbSwapchain vkbSwapchain = swapchainBuilder
            .use_default_format_selection()
            //use vsync present mode
            .set_desired_present_mode(VK_PRESENT_MODE_FIFO_KHR)
            .set_desired_extent(_windowExtent.width(), _windowExtent.height())
            .build()
            .value();

        //store swapchain and its related images
        _swapchain = vkbSwapchain.swapchain[0];
        _swapchainImages = vkbSwapchain.get_images().value();
        _swapchainImageViews = vkbSwapchain.get_image_views().value();

        _swachainImageFormat = vkbSwapchain.image_format;

        _mainDeletionQueue.push_function(() -> {
        vkDestroySwapchainKHR(_device, _swapchain, null);
    });

        //depth image size will match the window
        final VkExtent3D depthImageExtent = VkExtent3D.create();

        depthImageExtent.set(
                _windowExtent.width(),
                _windowExtent.height(),
                1
        );

        //hardcoding the depth format to 32 bit float
        _depthFormat = VK_FORMAT_D32_SFLOAT;

        //the depth image will be a image with the format we selected and Depth Attachment usage flag
        VkImageCreateInfo dimg_info = VkInit.image_create_info(_depthFormat, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, depthImageExtent);

        //for the depth image, we want to allocate it from gpu local memory
        final VmaAllocationCreateInfo dimg_allocinfo = VmaAllocationCreateInfo.create();
        dimg_allocinfo.usage( VMA_MEMORY_USAGE_GPU_ONLY);
        dimg_allocinfo.requiredFlags( /*VkMemoryPropertyFlags*/(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));

        //allocate and create the image
        LongBuffer dummy1 = memAllocLong(1);
        PointerBuffer dummy2 = memAllocPointer(1);

        vmaCreateImage(_allocator, dimg_info, dimg_allocinfo, /*_depthImage._image*/dummy1, /*_depthImage._allocation*/dummy2, null);
        _depthImage._image = dummy1.get(0);
        _depthImage._allocation = dummy2.get(0);
        memFree(dummy1);
        memFree(dummy2);

        //build a image-view for the depth image to use for rendering
        VkImageViewCreateInfo dview_info = VkInit.imageview_create_info(_depthFormat, _depthImage._image, VK_IMAGE_ASPECT_DEPTH_BIT);;

        VK_CHECK(VK10.vkCreateImageView(_device, dview_info, null, _depthImageView));

        //add to deletion queues
        _mainDeletionQueue.push_function(() -> {
        vkDestroyImageView(_device, _depthImageView[0], null);
        vmaDestroyImage(_allocator, _depthImage._image, _depthImage._allocation);
    });
    }

    /*346*/ public void init_default_renderpass()
    {
        //we define an attachment description for our main color image
        //the attachment is loaded as "clear" when renderpass start
        //the attachment is stored when renderpass ends
        //the attachment layout starts as "undefined", and transitions to "Present" so its possible to display it
        //we dont care about stencil, and dont use multisampling

        final VkAttachmentDescription color_attachment = VkAttachmentDescription.create();
        color_attachment.format ( (int)_swachainImageFormat);
        color_attachment.samples ( VK_SAMPLE_COUNT_1_BIT);
        color_attachment.loadOp ( VK_ATTACHMENT_LOAD_OP_CLEAR);
        color_attachment.storeOp ( VK_ATTACHMENT_STORE_OP_STORE);
        color_attachment.stencilLoadOp ( VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        color_attachment.stencilStoreOp ( VK_ATTACHMENT_STORE_OP_DONT_CARE);
        color_attachment.initialLayout ( VK_IMAGE_LAYOUT_UNDEFINED);
        color_attachment.finalLayout ( VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        final VkAttachmentReference.Buffer color_attachment_ref = VkAttachmentReference.create(1);
        color_attachment_ref.attachment ( 0);
        color_attachment_ref.layout ( VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        final VkAttachmentDescription depth_attachment = VkAttachmentDescription.create();
        // Depth attachment
        depth_attachment.flags ( 0);
        depth_attachment.format ( (int)_depthFormat);
        depth_attachment.samples ( VK_SAMPLE_COUNT_1_BIT);
        depth_attachment.loadOp ( VK_ATTACHMENT_LOAD_OP_CLEAR);
        depth_attachment.storeOp ( VK_ATTACHMENT_STORE_OP_STORE);
        depth_attachment.stencilLoadOp ( VK_ATTACHMENT_LOAD_OP_CLEAR);
        depth_attachment.stencilStoreOp ( VK_ATTACHMENT_STORE_OP_DONT_CARE);
        depth_attachment.initialLayout ( VK_IMAGE_LAYOUT_UNDEFINED);
        depth_attachment.finalLayout ( VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        final VkAttachmentReference depth_attachment_ref = VkAttachmentReference.create();
        depth_attachment_ref.attachment ( 1);
        depth_attachment_ref.layout ( VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        //we are going to create 1 subpass, which is the minimum you can do
        final VkSubpassDescription.Buffer subpass = VkSubpassDescription.create(1);
        subpass.pipelineBindPoint ( VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.colorAttachmentCount ( 1);
        subpass.pColorAttachments ( color_attachment_ref);
        //hook the depth attachment into the subpass
        subpass.pDepthStencilAttachment ( depth_attachment_ref);

        //1 dependency, which is from "outside" into the subpass. And we can read or write color
        final VkSubpassDependency.Buffer dependency = VkSubpassDependency.create(1);
        dependency.srcSubpass ( VK_SUBPASS_EXTERNAL);
        dependency.dstSubpass ( 0);
        dependency.srcStageMask ( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.srcAccessMask ( 0);
        dependency.dstStageMask ( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.dstAccessMask ( VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);


        //array of 2 attachments, one for the color, and other for depth
        VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.create(2);
        attachments.put(0, color_attachment); attachments.put(1, depth_attachment);

        final VkRenderPassCreateInfo render_pass_info = VkRenderPassCreateInfo.create();
        render_pass_info.sType ( VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
        //2 attachments from said array
        //render_pass_info.attachmentCount ( 2); java port
        render_pass_info.pAttachments ( attachments);
        //render_pass_info.subpassCount ( 1); // java port
        render_pass_info.pSubpasses ( subpass);
        //render_pass_info.dependencyCount ( 1); java port
        render_pass_info.pDependencies ( dependency);

        VK_CHECK(VK10.vkCreateRenderPass(_device, render_pass_info, null, _renderPass));

        _mainDeletionQueue.push_function(() -> {
        vkDestroyRenderPass(_device, _renderPass[0], null);
    });
    }

    /*422*/ public void init_framebuffers()
    {
        //create the framebuffers for the swapchain images. This will connect the render-pass to the images for rendering
        VkFramebufferCreateInfo fb_info = VkInit.framebuffer_create_info(_renderPass[0], _windowExtent);

	int swapchain_imagecount = _swapchainImages.size();
        _framebuffers.clear(); // java port
	for( int i=0; i< swapchain_imagecount;i++) {
        _framebuffers.add(new long[1]); // java port
    }
        for (int i = 0; i < swapchain_imagecount; i++) {

            LongBuffer attachments = memAllocLong(2);
            attachments.put(0, _swapchainImageViews.get(i));
            attachments.put(1, _depthImageView[0]);

            fb_info.pAttachments( attachments);
            //fb_info.attachmentCount ( 2); java port
            VK_CHECK(VK10.vkCreateFramebuffer(_device, fb_info, null, _framebuffers.get(i)));

            final int ii = i;

            _mainDeletionQueue.push_function(() -> {
                vkDestroyFramebuffer(_device, _framebuffers.get(ii)[0], null);
                vkDestroyImageView(_device, _swapchainImageViews.get(ii), null);
            });
        }
    }

    /*447*/ public void init_commands()
    {
        //create a command pool for commands submitted to the graphics queue.
        //we also want the pool to allow for resetting of individual command buffers
        VkCommandPoolCreateInfo commandPoolInfo = VkInit.command_pool_create_info(_graphicsQueueFamily, VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);


        for (int i = 0; i < FRAME_OVERLAP; i++) {


            VK_CHECK(VK10.vkCreateCommandPool(_device, commandPoolInfo, null, _frames[i]._commandPool));

            //allocate the default command buffer that we will use for rendering
            VkCommandBufferAllocateInfo cmdAllocInfo = VkInit.command_buffer_allocate_info(_frames[i]._commandPool[0], 1);

            PointerBuffer dummy = memAllocPointer(1);
            VK_CHECK(vkAllocateCommandBuffers(_device, cmdAllocInfo, /*_frames[i]._mainCommandBuffer)*/dummy));
            _frames[i]._mainCommandBuffer = new VkCommandBuffer(dummy.get(0),_device);

            final int ii = i;

            _mainDeletionQueue.push_function(() -> {
                vkDestroyCommandPool(_device, _frames[ii]._commandPool[0], null);
            });
        }


        VkCommandPoolCreateInfo uploadCommandPoolInfo = VkInit.command_pool_create_info(_graphicsQueueFamily);
        //create pool for upload context
        VK_CHECK(VK10.vkCreateCommandPool(_device, uploadCommandPoolInfo, null, _uploadContext._commandPool));

        _mainDeletionQueue.push_function(() -> {
        vkDestroyCommandPool(_device, _uploadContext._commandPool[0], null);
    });
    }

    /*479*/ public void init_sync_structures()
    {
        //create syncronization structures
        //one fence to control when the gpu has finished rendering the frame,
        //and 2 semaphores to syncronize rendering with swapchain
        //we want the fence to start signalled so we can wait on it on the first frame
        VkFenceCreateInfo fenceCreateInfo = VkInit.fence_create_info(VK_FENCE_CREATE_SIGNALED_BIT);

        VkSemaphoreCreateInfo semaphoreCreateInfo = VkInit.semaphore_create_info();

        for (int i = 0; i < FRAME_OVERLAP; i++) {

            VK_CHECK(VK10.vkCreateFence(_device, fenceCreateInfo, null, _frames[i]._renderFence));

            final int ii = i;
            //enqueue the destruction of the fence
            _mainDeletionQueue.push_function(() -> {
                vkDestroyFence(_device, _frames[ii]._renderFence[0], null);
            });


            VK_CHECK(vkCreateSemaphore(_device, semaphoreCreateInfo, null, _frames[i]._presentSemaphore));
            VK_CHECK(vkCreateSemaphore(_device, semaphoreCreateInfo, null, _frames[i]._renderSemaphore));

            //enqueue the destruction of semaphores
            _mainDeletionQueue.push_function(() -> {
                vkDestroySemaphore(_device, _frames[ii]._presentSemaphore[0], null);
                vkDestroySemaphore(_device, _frames[ii]._renderSemaphore[0], null);
            });
        }


        VkFenceCreateInfo uploadFenceCreateInfo = VkInit.fence_create_info();

        VK_CHECK(vkCreateFence(_device, uploadFenceCreateInfo, null, _uploadContext._uploadFence));
        _mainDeletionQueue.push_function(() -> {
        vkDestroyFence(_device, _uploadContext._uploadFence[0], null);
    });
    }

    /*519*/ public void init_pipelines()
    {
        /*VkShaderModule*/final long[] colorMeshShader = new long[1];
        if (!load_shader_module("../../shaders/default_lit.frag.spv", colorMeshShader))
        {
            if (!load_shader_module("vk-bootstrap/src/vulkanguide/shaders/default_lit.frag.spv", colorMeshShader))
            {
                System.out.println("Error when building the colored mesh shader");
            }
        }

        /*VkShaderModule*/final long[] texturedMeshShader = new long[1];
        if (!load_shader_module("../../shaders/textured_lit.frag.spv", texturedMeshShader))
        {
            if (!load_shader_module("vk-bootstrap/src/vulkanguide/shaders/textured_lit.frag.spv", texturedMeshShader)) {
                System.out.println("Error when building the colored mesh shader");
            }
        }

        /*VkShaderModule*/final long[] meshVertShader = new long[1];
        if (!load_shader_module("../../shaders/tri_mesh_ssbo.vert.spv", meshVertShader))
        {
            if (!load_shader_module("vk-bootstrap/src/vulkanguide/shaders/tri_mesh_ssbo.vert.spv", meshVertShader)) {
                System.out.println("Error when building the mesh vertex shader module");
            }
        }


        //build the stage-create-info for both vertex and fragment stages. This lets the pipeline know the shader modules per stage
        final PipelineBuilder pipelineBuilder = new PipelineBuilder();

        pipelineBuilder._shaderStages.add(
                VkInit.pipeline_shader_stage_create_info(VK_SHADER_STAGE_VERTEX_BIT, meshVertShader[0]));

        pipelineBuilder._shaderStages.add(
                VkInit.pipeline_shader_stage_create_info(VK_SHADER_STAGE_FRAGMENT_BIT, colorMeshShader[0]));


        //we start from just the default empty pipeline layout info
        VkPipelineLayoutCreateInfo mesh_pipeline_layout_info = VkInit.pipeline_layout_create_info();

        //setup push constants
        final VkPushConstantRange.Buffer push_constant = VkPushConstantRange.create(1);
        //offset 0
        push_constant.offset ( 0);
        //size of a MeshPushConstant struct
        push_constant.size ( MeshPushConstants.sizeof());
        //for the vertex shader
        push_constant.stageFlags ( VK_SHADER_STAGE_VERTEX_BIT);

        mesh_pipeline_layout_info.pPushConstantRanges ( push_constant);
        //mesh_pipeline_layout_info.pushConstantRangeCount ( 1); java port

        /*VkDescriptorSetLayout*/LongBuffer setLayouts = memAllocLong(2);
        setLayouts.put(0,_globalSetLayout[0]);
        setLayouts.put(1,_objectSetLayout[0]);

        //mesh_pipeline_layout_info.setLayoutCount ( 2); java port
        mesh_pipeline_layout_info.pSetLayouts ( setLayouts);

        /*VkPipelineLayout*/final long[] meshPipLayout = new long[1];
        VK_CHECK(VK10.vkCreatePipelineLayout(_device, mesh_pipeline_layout_info, null, meshPipLayout));


        //we start from  the normal mesh layout
        VkPipelineLayoutCreateInfo textured_pipeline_layout_info = mesh_pipeline_layout_info;

        /*VkDescriptorSetLayout*/final LongBuffer texturedSetLayouts = memAllocLong(3);
        texturedSetLayouts.put(0,_globalSetLayout[0]);
        texturedSetLayouts.put(1,_objectSetLayout[0]);
        texturedSetLayouts.put(2,_singleTextureSetLayout[0]);

        //textured_pipeline_layout_info.setLayoutCount ( 3); java port
        textured_pipeline_layout_info.pSetLayouts ( texturedSetLayouts);

        /*VkPipelineLayout*/final long[] texturedPipeLayout = new long[1];
        VK_CHECK(vkCreatePipelineLayout(_device, textured_pipeline_layout_info, null, texturedPipeLayout));

        //hook the push constants layout
        pipelineBuilder._pipelineLayout = meshPipLayout[0];

        //vertex input controls how to read vertices from vertex buffers. We arent using it yet
        pipelineBuilder._vertexInputInfo = VkInit.vertex_input_state_create_info();

        //input assembly is the configuration for drawing triangle lists, strips, or individual points.
        //we are just going to draw triangle list
        pipelineBuilder._inputAssembly = VkInit.input_assembly_create_info(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);

        //build viewport and scissor from the swapchain extents
        pipelineBuilder._viewport.x ( 0.0f);
        pipelineBuilder._viewport.y ( 0.0f);
        pipelineBuilder._viewport.width ( (float)_windowExtent.width());
        pipelineBuilder._viewport.height ( (float)_windowExtent.height());
        pipelineBuilder._viewport.minDepth ( 0.0f);
        pipelineBuilder._viewport.maxDepth ( 1.0f);

        VkOffset2D dummy = VkOffset2D.create();
        dummy.x(0); dummy.y(0);
        pipelineBuilder._scissor.offset ( dummy);
        pipelineBuilder._scissor.extent ( _windowExtent);

        //configure the rasterizer to draw filled triangles
        pipelineBuilder._rasterizer = VkInit.rasterization_state_create_info(VK_POLYGON_MODE_FILL);

        //we dont use multisampling, so just run the default one
        pipelineBuilder._multisampling = VkInit.multisampling_state_create_info();

        //a single blend attachment with no blending and writing to RGBA
        pipelineBuilder._colorBlendAttachment.put(0, VkInit.color_blend_attachment_state());


        //default depthtesting
        pipelineBuilder._depthStencil = VkInit.depth_stencil_create_info(true, true, VK_COMPARE_OP_LESS_OR_EQUAL);

        //build the mesh pipeline

        VertexInputDescription vertexDescription = Vertex.get_vertex_description();

        //connect the pipeline builder vertex input info to the one we get from Vertex
        VkVertexInputAttributeDescription.Buffer dummy1 = VkVertexInputAttributeDescription.create(vertexDescription.attributes.size());
        for( int i=0; i< vertexDescription.attributes.size();i++) dummy1.put(i,vertexDescription.attributes.get(i));
        pipelineBuilder._vertexInputInfo.pVertexAttributeDescriptions ( /*vertexDescription.attributes.data()*/dummy1);
        //pipelineBuilder._vertexInputInfo.vertexAttributeDescriptionCount ( vertexDescription.attributes.size()); java port

        VkVertexInputBindingDescription.Buffer dummy2 = VkVertexInputBindingDescription.create(vertexDescription.bindings.size());
        for( int i=0; i<vertexDescription.bindings.size();i++) dummy2.put(i,vertexDescription.bindings.get(i));
        pipelineBuilder._vertexInputInfo.pVertexBindingDescriptions ( /*vertexDescription.bindings.data()*/dummy2);
        //pipelineBuilder._vertexInputInfo.vertexBindingDescriptionCount ( vertexDescription.bindings.size()); java port


        //build the mesh triangle pipeline
        /*VkPipeline*/long meshPipeline = pipelineBuilder.build_pipeline(_device, _renderPass[0]);

        create_material(meshPipeline, meshPipLayout[0], "defaultmesh");

        pipelineBuilder._shaderStages.clear();
        pipelineBuilder._shaderStages.add(
                VkInit.pipeline_shader_stage_create_info(VK_SHADER_STAGE_VERTEX_BIT, meshVertShader[0]));

        pipelineBuilder._shaderStages.add(
                VkInit.pipeline_shader_stage_create_info(VK_SHADER_STAGE_FRAGMENT_BIT, texturedMeshShader[0]));

        pipelineBuilder._pipelineLayout = texturedPipeLayout[0];
        /*VkPipeline*/long texPipeline = pipelineBuilder.build_pipeline(_device, _renderPass[0]);
        create_material(texPipeline, texturedPipeLayout[0], "texturedmesh");


        vkDestroyShaderModule(_device, meshVertShader[0], null);
        vkDestroyShaderModule(_device, colorMeshShader[0], null);
        vkDestroyShaderModule(_device, texturedMeshShader[0], null);


        _mainDeletionQueue.push_function(() -> {
        vkDestroyPipeline(_device, meshPipeline, null);
        vkDestroyPipeline(_device, texPipeline, null);

        vkDestroyPipelineLayout(_device, meshPipLayout[0], null);
        vkDestroyPipelineLayout(_device, texturedPipeLayout[0], null);
    });
    }

    /*662*/ public boolean load_shader_module(String filePath, /*VkShaderModule**/final long[] outShaderModule)
    {
        //open the file. With cursor at the end
        ByteBuffer buffer;
        try {
            FileInputStream file = new FileInputStream(filePath);

//            if (!file.is_open()) {
//                return false;
//            }

            //find what the size of the file is by looking up the location of the cursor
            //because the cursor is at the end, it gives the size directly in bytes
            //size_t fileSize = (size_t) file.tellg();

            //spirv expects the buffer to be on uint32, so make sure to reserve a int vector big enough for the entire file

            //put file cursor at beggining
            //file.seekg(0);

            //load the entire file into the buffer
            //file.read(( char*)buffer.data(), fileSize);
            FileChannel fc = file.getChannel();
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            fc.close();

            //now that the file is loaded into the buffer, we can close it
            file.close();
        } catch (IOException e) {
            return false;
        }

        //create a new shader module, using the buffer we loaded
        final VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.create();
        createInfo.sType ( VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
        createInfo.pNext ( 0);

        //codeSize has to be in bytes, so multply the ints in the buffer by size of int to know the real size of the buffer
        //createInfo.codeSize ( buffer.capacity() * Integer.BYTES); java port
        createInfo.pCode ( buffer);

        //check that the creation goes well.
        /*VkShaderModule*/final long[] shaderModule = new long[1];
        if (vkCreateShaderModule(_device, createInfo, null, shaderModule) != VK_SUCCESS) {
        return false;
    }
	outShaderModule[0] = shaderModule[0];
        return true;
    }

    /*762*/ void load_meshes()
    {
        final Mesh triMesh = new Mesh();
        //make the array 3 vertices long
        //triMesh._vertices.resize(3);
        triMesh._vertices.clear();
        for( int i=0; i<3; i++) {
            triMesh._vertices.add(new Vertex());
        }

        //vertex positions
        triMesh._vertices.get(0).position.set( 1.f,1.f, 0.0f );
        triMesh._vertices.get(1).position.set( -1.f,1.f, 0.0f );
        triMesh._vertices.get(2).position.set( 0.f,-1.f, 0.0f );

        //vertex colors, all green
        triMesh._vertices.get(0).color.set( 0.f,1.f, 0.0f ); //pure green
        triMesh._vertices.get(1).color.set( 0.f,1.f, 0.0f ); //pure green
        triMesh._vertices.get(2).color.set( 0.f,1.f, 0.0f ); //pure green
        //we dont care about the vertex normals

        //load the monkey
        final Mesh monkeyMesh = new Mesh();
        if (!monkeyMesh.load_from_obj("../../assets/monkey_smooth.obj")) {
            monkeyMesh.load_from_obj("vulkanguide/assets/monkey_smooth.obj");
        }

        final Mesh lostEmpire = new Mesh();
        if(!lostEmpire.load_from_obj("../../assets/lost_empire.obj")) {
            lostEmpire.load_from_obj("vulkanguide/assets/lost_empire.obj");
        }

        upload_mesh(triMesh);
        upload_mesh(monkeyMesh);
        upload_mesh(lostEmpire);

        _meshes.put("monkey", monkeyMesh);
        _meshes.put("triangle", triMesh);
        _meshes.put("empire", lostEmpire);
    }

    /*796*/ void load_images()
    {
        final Texture lostEmpire = new Texture();

        if(!VkUtil.load_image_from_file(this, "../../assets/lost_empire-RGBA.png", lostEmpire.image)) {
            VkUtil.load_image_from_file(this, "vk-bootstrap/src/vulkanguide/assets/lost_empire-RGBA.png", lostEmpire.image);
        }

        VkImageViewCreateInfo imageinfo = VkInit.imageview_create_info(VK_FORMAT_R8G8B8A8_SRGB, lostEmpire.image[0]._image, VK_IMAGE_ASPECT_COLOR_BIT);
        VK10.vkCreateImageView(_device, imageinfo, null, lostEmpire.imageView);

        _mainDeletionQueue.push_function(() -> {
        vkDestroyImageView(_device, lostEmpire.imageView[0], null);
    });

        _loadedTextures.put("empire_diffuse", lostEmpire);
    }

    /*812*/ void upload_mesh(Mesh mesh)
    {
	long bufferSize= mesh._vertices.size() * Vertex.sizeof();
        //allocate vertex buffer
        final VkBufferCreateInfo stagingBufferInfo = VkBufferCreateInfo.create();
        stagingBufferInfo.sType ( VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
        stagingBufferInfo.pNext ( 0);
        //this is the total size, in bytes, of the buffer we are allocating
        stagingBufferInfo.size ( bufferSize);
        //this buffer is going to be used as a Vertex Buffer
        stagingBufferInfo.usage ( VK_BUFFER_USAGE_TRANSFER_SRC_BIT);


        //let the VMA library know that this data should be writeable by CPU, but also readable by GPU
        final VmaAllocationCreateInfo vmaallocInfo = VmaAllocationCreateInfo.create();
        vmaallocInfo.usage ( VMA_MEMORY_USAGE_CPU_ONLY);

        final AllocatedBuffer stagingBuffer = new AllocatedBuffer();

        LongBuffer dummy1 = memAllocLong(1);
        PointerBuffer dummy2 = memAllocPointer(1);

        //allocate the buffer
        VK_CHECK(vmaCreateBuffer(_allocator, stagingBufferInfo, vmaallocInfo,
		/*stagingBuffer._buffer*/dummy1,
		/*stagingBuffer._allocation*/dummy2,
            null));

        stagingBuffer._buffer[0] = dummy1.get(0);
        stagingBuffer._allocation = dummy2.get(0);

        memFree(dummy1);
        memFree(dummy2);

        //copy vertex data
        PointerBuffer data = memAllocPointer(1);
        vmaMapMemory(_allocator, stagingBuffer._allocation, data);

        Buffer dummy = Port.data(mesh._vertices);

        MemoryUtil.memCopy(memAddress(dummy), data.get(),  mesh._vertices.size() * Vertex.sizeof());

        vmaUnmapMemory(_allocator, stagingBuffer._allocation);


        //allocate vertex buffer
        final VkBufferCreateInfo vertexBufferInfo = VkBufferCreateInfo.create();
        vertexBufferInfo.sType ( VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
        vertexBufferInfo.pNext ( 0);
        //this is the total size, in bytes, of the buffer we are allocating
        vertexBufferInfo.size ( bufferSize);
        //this buffer is going to be used as a Vertex Buffer
        vertexBufferInfo.usage ( VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT);

        //let the VMA library know that this data should be gpu native
        vmaallocInfo.usage ( VMA_MEMORY_USAGE_GPU_ONLY);

        LongBuffer dummy3 = memAllocLong(1);
        PointerBuffer dummy4 = memAllocPointer(1);

        //allocate the buffer
        VK_CHECK(vmaCreateBuffer(_allocator, vertexBufferInfo, vmaallocInfo,
		/*mesh._vertexBuffer._buffer*/dummy3,
		/*mesh._vertexBuffer._allocation*/dummy4,
            null));

        mesh._vertexBuffer._buffer[0] = dummy3.get(0);
        mesh._vertexBuffer._allocation = dummy4.get(0);

        memFree(dummy3);
        memFree(dummy4);

        //add the destruction of triangle mesh buffer to the deletion queue
        _mainDeletionQueue.push_function(() -> {

        vmaDestroyBuffer(_allocator, mesh._vertexBuffer._buffer[0], mesh._vertexBuffer._allocation);
    });

        immediate_submit((VkCommandBuffer cmd) -> {
        final VkBufferCopy.Buffer copy = VkBufferCopy.create(1);
        copy.dstOffset ( 0);
        copy.srcOffset ( 0);
        copy.size ( bufferSize);
        vkCmdCopyBuffer(cmd, stagingBuffer._buffer[0], mesh._vertexBuffer._buffer[0], /*1,*/ copy);
    });

        vmaDestroyBuffer(_allocator, stagingBuffer._buffer[0], stagingBuffer._allocation);
    }

    /*881*/ Material create_material(/*VkPipeline*/long pipeline, /*VkPipelineLayout*/long layout, String name)
    {
        final Material mat = new Material();
        mat.pipeline = pipeline;
        mat.pipelineLayout = layout;
        _materials.put(name, mat);
        return _materials.get(name);
    }

    /*890*/ Material get_material(String name)
    {
        //search for the object, and return nullpointer if not found
        return _materials.get(name);
    }

    /*903*/ Mesh get_mesh(String name)
    {
        return _meshes.get(name);
    }

    /*914*/ void draw_objects(VkCommandBuffer cmd, List<RenderObject> first, int count)
    {
        //make a model view matrix for rendering the object
        //camera view
        Vector3f camPos = new Vector3f( 0.f,-6.f,-10.f );

        Matrix4f view = new Matrix4f().translate(camPos);
        //camera projection
        Matrix4f projection = new Matrix4f().perspective((float)Math.toRadians(70.f), 1700.f / 900.f, 0.1f, 200.0f);
        projection.m11( projection.m11() * -1);

        final GPUCameraData camData = new GPUCameraData();
        camData.proj.set( projection);
        camData.view.set( view);
        camData.viewproj.set( projection.mul( view));

        PointerBuffer data = memAllocPointer(1);
        vmaMapMemory(_allocator, get_current_frame().cameraBuffer._allocation, data);

        Buffer dummy = camData.toBuffer();

        /*memcpy*/MemoryUtil.memCopy(/*data,*/ /*camData*/memAddress(dummy), data.get(), GPUCameraData.sizeof());

        vmaUnmapMemory(_allocator, get_current_frame().cameraBuffer._allocation);

        float framed = (_frameNumber / 120.f);

        _sceneParameters.ambientColor.set(new Vector4f( (float)sin(framed),0,(float)cos(framed),1 ));

        PointerBuffer sceneData_ = memAllocPointer(1);
        vmaMapMemory(_allocator, _sceneParameterBuffer._allocation , sceneData_);

        int frameIndex = _frameNumber % FRAME_OVERLAP;

        long sceneData = sceneData_.get();

        sceneData += pad_uniform_buffer_size(GPUSceneData.sizeof()) * frameIndex;

        /*memcpy*/MemoryUtil.memCopy(/*sceneData,*/ memAddress(_sceneParameters.toBuffer()), sceneData, GPUSceneData.sizeof());

        vmaUnmapMemory(_allocator, _sceneParameterBuffer._allocation);


        PointerBuffer objectData = memAllocPointer(1);
        vmaMapMemory(_allocator, get_current_frame().objectBuffer._allocation, objectData);

        /*GPUObjectData*/long objectSSBO = objectData.get(0);

        for (int i = 0; i < count; i++)
        {
            RenderObject object = first.get(i);
            GPUObjectData.setModelMatrix(objectSSBO + i* GPUObjectData.sizeof(), object.transformMatrix);
        }

        vmaUnmapMemory(_allocator, get_current_frame().objectBuffer._allocation);

        Mesh lastMesh = null;
        Material lastMaterial = null;

        for (int i = 0; i < count; i++)
        {
            RenderObject object = first.get(i);

            //only bind the pipeline if it doesnt match with the already bound one
            if (object.material != lastMaterial) {

                vkCmdBindPipeline(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, object.material.pipeline);
                lastMaterial = object.material;

                int uniform_offset = (int)pad_uniform_buffer_size(GPUSceneData.sizeof()) * frameIndex;
                int[] dummy1 = new int[1]; dummy1[0] = uniform_offset;
                VK10.vkCmdBindDescriptorSets(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, object.material.pipelineLayout, 0, /*1,*/ get_current_frame().globalDescriptor, /*1,*/ /*uniform_offset*/dummy1);

                //object data descriptor
                vkCmdBindDescriptorSets(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, object.material.pipelineLayout, 1, /*1,*/ get_current_frame().objectDescriptor, /*0,*/ null);

                if (object.material.textureSet[0] != VK_NULL_HANDLE) {
                    //texture descriptor
                    VK10.vkCmdBindDescriptorSets(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, object.material.pipelineLayout, 2, /*1,*/ object.material.textureSet, /*0,*/ null);

                }
            }

            Matrix4f model = object.transformMatrix;
            //final render matrix, that we are calculating on the cpu
            Matrix4f mesh_matrix = model;

            final MeshPushConstants constants = new MeshPushConstants();
            constants.render_matrix.set( mesh_matrix);

            //upload the mesh to the gpu via pushconstants
            VK10.vkCmdPushConstants(cmd, object.material.pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, /*MeshPushConstants.sizeof(),*/ constants.toFloatBuffer());

            //only bind the mesh if its a different one from last bind
            if (object.mesh != lastMesh) {
                //bind the mesh vertex buffer with offset 0
                final /*VkDeviceSize*/long[] offset = new long[1];
                VK10.vkCmdBindVertexBuffers(cmd, 0, /*1,*/ object.mesh._vertexBuffer._buffer, offset);
                lastMesh = object.mesh;
            }
            //we can now draw
            vkCmdDraw(cmd, object.mesh._vertices.size(), 1,0 , i);
        }
    }

    /*1104*/ long pad_uniform_buffer_size(long originalSize)
    {
        // Calculate required alignment based on minimum device offset alignment
        long minUboAlignment = _gpuProperties.limits().minUniformBufferOffsetAlignment();
        long alignedSize = originalSize;
        if (minUboAlignment > 0) {
            alignedSize = (alignedSize + minUboAlignment - 1) & ~(minUboAlignment - 1);
        }
        return alignedSize;
    }

    /*1017*/ void init_scene()
    {
        final RenderObject monkey = new RenderObject();
        monkey.mesh = get_mesh("monkey");
        monkey.material = get_material("defaultmesh");
        monkey.transformMatrix.set( new Matrix4f() );//glm::mat4{ 1.0f };

        _renderables.add(monkey);

        final RenderObject map = new RenderObject();
        map.mesh = get_mesh("empire");
        map.material = get_material("texturedmesh");
        map.transformMatrix.set( new Matrix4f().translate(new Vector3f( 5,-10,0 ))); //glm::mat4{ 1.0f };

        _renderables.add(map);

        for (int x = -20; x <= 20; x++) {
            for (int y = -20; y <= 20; y++) {

                final RenderObject tri = new RenderObject();
                tri.mesh = get_mesh("triangle");
                tri.material = get_material("defaultmesh");
                Matrix4f translation = new Matrix4f().translate(new Vector3f(x, 0, y));
                Matrix4f scale = (new Matrix4f().scale(new Vector3f(0.2f, 0.2f, 0.2f)));
                tri.transformMatrix.set( translation.mul(scale));

                _renderables.add(tri);
            }
        }


        Material texturedMat=	get_material("texturedmesh");

        final VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.create();
        allocInfo.pNext ( 0);
        allocInfo.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
        allocInfo.descriptorPool ( _descriptorPool[0]);
        //allocInfo.descriptorSetCount ( 1); java port

        LongBuffer dummy1 = memAllocLong(1);
        dummy1.put(0,_singleTextureSetLayout[0]);

        allocInfo.pSetLayouts ( /*_singleTextureSetLayout*/dummy1);

        VK10.vkAllocateDescriptorSets(_device, allocInfo, texturedMat.textureSet);

        memFree(dummy1);

        VkSamplerCreateInfo samplerInfo = VkInit.sampler_create_info(VK_FILTER_NEAREST);

        /*VkSampler*/final long[] blockySampler = new long[1];
        vkCreateSampler(_device, samplerInfo, null, blockySampler);

        _mainDeletionQueue.push_function(() -> {
        vkDestroySampler(_device, blockySampler[0], null);
    });

        final VkDescriptorImageInfo imageBufferInfo = VkDescriptorImageInfo.create();
        imageBufferInfo.sampler ( blockySampler[0]);
        imageBufferInfo.imageView ( _loadedTextures.get("empire_diffuse").imageView[0]);
        imageBufferInfo.imageLayout ( VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

        VkWriteDescriptorSet texture1 = VkInit.write_descriptor_image(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, texturedMat.textureSet[0], imageBufferInfo, 0);

        VkWriteDescriptorSet.Buffer dummy = VkWriteDescriptorSet.create(1); dummy.put(0,texture1);

        vkUpdateDescriptorSets(_device, /*1,*/ /*texture1*/dummy, /*0,*/ null);
    }

    /*1078*/ AllocatedBuffer create_buffer(long allocSize, /*VkBufferUsageFlags*/int usage, /*VmaMemoryUsage*/int memoryUsage)
    {
        //allocate vertex buffer
        final VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.create();
        bufferInfo.sType ( VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
        bufferInfo.pNext ( 0);
        bufferInfo.size ( allocSize);

        bufferInfo.usage ( usage);


        //let the VMA library know that this data should be writeable by CPU, but also readable by GPU
        final VmaAllocationCreateInfo vmaallocInfo = VmaAllocationCreateInfo.create();
        vmaallocInfo.usage ( memoryUsage);

        final AllocatedBuffer newBuffer = new AllocatedBuffer();

        LongBuffer dummy1 = memAllocLong(1);
        PointerBuffer dummy2 = memAllocPointer(1);

        //allocate the buffer
        VK_CHECK(vmaCreateBuffer(_allocator, bufferInfo, vmaallocInfo,
		/*newBuffer._buffer*/dummy1,
		/*newBuffer._allocation*/dummy2,
            null));

        newBuffer._buffer[0] = dummy1.get(0);
        newBuffer._allocation = dummy2.get(0);

        memFree(dummy1);
        memFree(dummy2);

        return newBuffer;
    }

    /*1116*/ void immediate_submit(Consumer<VkCommandBuffer> function)
    {
        final VkCommandBuffer cmd;

        //allocate the default command buffer that we will use for rendering
        VkCommandBufferAllocateInfo cmdAllocInfo = VkInit.command_buffer_allocate_info(_uploadContext._commandPool[0], 1);

        PointerBuffer dummy1 = memAllocPointer(1);
        VK_CHECK(vkAllocateCommandBuffers(_device, cmdAllocInfo, /*cmd*/dummy1));
        cmd = new VkCommandBuffer(dummy1.get(0),_device);

        memFree(dummy1);

        //begin the command buffer recording. We will use this command buffer exactly once, so we want to let vulkan know that
        VkCommandBufferBeginInfo cmdBeginInfo = VkInit.command_buffer_begin_info(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

        VK_CHECK(vkBeginCommandBuffer(cmd, cmdBeginInfo));


        function.accept(cmd);


        VK_CHECK(vkEndCommandBuffer(cmd));

        VkSubmitInfo submit = VkInit.submit_info(cmd);


        //submit command buffer to the queue and execute it.
        // _renderFence will now block until the graphic commands finish execution
        VK_CHECK(vkQueueSubmit(_graphicsQueue, /*1,*/ submit, _uploadContext._uploadFence[0]));

        VK10.vkWaitForFences(_device, /*1,*/ _uploadContext._uploadFence, true, 9999999999l);
        VK10.vkResetFences(_device, /*1,*/ _uploadContext._uploadFence[0]);

        vkResetCommandPool(_device, _uploadContext._commandPool[0], 0);
    }

    /*1149*/ public void init_descriptors()
    {

        //create a descriptor pool that will hold 10 uniform buffers
        VkDescriptorPoolSize.Buffer sizes = VkDescriptorPoolSize.create(4);
                {
                    sizes.get(0).set( VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 10 );
                    sizes.get(1).set( VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, 10 );
                    sizes.get(2).set( VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, 10 );
                    sizes.get(3).set( VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 10 );
                };

        final VkDescriptorPoolCreateInfo pool_info = VkDescriptorPoolCreateInfo.create();
        pool_info.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
        pool_info.flags ( 0);
        pool_info.maxSets ( 10);
        //pool_info.poolSizeCount ( (int)sizes.size()); // java port
        pool_info.pPoolSizes ( sizes);

        vkCreateDescriptorPool(_device, pool_info, null, _descriptorPool);

        VkDescriptorSetLayoutBinding cameraBind = VkInit.descriptorset_layout_binding(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,VK_SHADER_STAGE_VERTEX_BIT,0);
        VkDescriptorSetLayoutBinding sceneBind = VkInit.descriptorset_layout_binding(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 1);

        VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.create(2);
        bindings.put(0,cameraBind);
        bindings.put(1,sceneBind);

        final VkDescriptorSetLayoutCreateInfo setinfo = VkDescriptorSetLayoutCreateInfo.create();
        //setinfo.bindingCount ( 2); java port
        setinfo.flags ( 0);
        setinfo.pNext ( 0);
        setinfo.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
        setinfo.pBindings ( bindings);

        vkCreateDescriptorSetLayout(_device, setinfo, null, _globalSetLayout);

        VkDescriptorSetLayoutBinding.Buffer objectBind = VkDescriptorSetLayoutBinding.create(1);
        objectBind.put(0,VkInit.descriptorset_layout_binding(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, VK_SHADER_STAGE_VERTEX_BIT, 0));

        final VkDescriptorSetLayoutCreateInfo set2info = VkDescriptorSetLayoutCreateInfo.create();
        //set2info.bindingCount ( 1); java port
        set2info.flags ( 0);
        set2info.pNext ( 0);
        set2info.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
        set2info.pBindings ( objectBind);

        vkCreateDescriptorSetLayout(_device, set2info, null, _objectSetLayout);

        VkDescriptorSetLayoutBinding.Buffer textureBind = VkDescriptorSetLayoutBinding.create(1);
        textureBind.put(0,VkInit.descriptorset_layout_binding(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT, 0));

        VkDescriptorSetLayoutCreateInfo set3info = VkDescriptorSetLayoutCreateInfo.create();
        //set3info.bindingCount ( 1); java port
        set3info.flags ( 0);
        set3info.pNext ( 0);
        set3info.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
        set3info.pBindings ( textureBind);

        vkCreateDescriptorSetLayout(_device, set3info, null, _singleTextureSetLayout);


	    long sceneParamBufferSize = FRAME_OVERLAP * pad_uniform_buffer_size(GPUSceneData.sizeof());

        _sceneParameterBuffer = create_buffer(sceneParamBufferSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VMA_MEMORY_USAGE_CPU_TO_GPU);


        for (int i = 0; i < FRAME_OVERLAP; i++)
        {
            _frames[i].cameraBuffer = create_buffer(GPUCameraData.sizeof(), VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VMA_MEMORY_USAGE_CPU_TO_GPU);

		final int MAX_OBJECTS = 10000;
            _frames[i].objectBuffer = create_buffer(GPUObjectData.sizeof() * MAX_OBJECTS, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, VMA_MEMORY_USAGE_CPU_TO_GPU);

            final VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.create();
            allocInfo.pNext ( 0);
            allocInfo.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocInfo.descriptorPool ( _descriptorPool[0]);
            //allocInfo.descriptorSetCount ( 1); java port
            LongBuffer dummy = memAllocLong(1);
            dummy.put(0,_globalSetLayout[0]);

            allocInfo.pSetLayouts ( /*_globalSetLayout*/dummy);

            VK10.vkAllocateDescriptorSets(_device, allocInfo, _frames[i].globalDescriptor);

            final VkDescriptorSetAllocateInfo objectSetAlloc = VkDescriptorSetAllocateInfo.create();
            objectSetAlloc.pNext ( 0);
            objectSetAlloc.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            objectSetAlloc.descriptorPool ( _descriptorPool[0]);
            //objectSetAlloc.descriptorSetCount ( 1); java port

            LongBuffer dummy2 = memAllocLong(1);
            dummy2.put(0,_objectSetLayout[0]);
            objectSetAlloc.pSetLayouts ( /*_objectSetLayout*/dummy2);

            vkAllocateDescriptorSets(_device, objectSetAlloc, _frames[i].objectDescriptor);

            final VkDescriptorBufferInfo.Buffer cameraInfo = VkDescriptorBufferInfo.create(1);
            cameraInfo.buffer ( _frames[i].cameraBuffer._buffer[0]);
            cameraInfo.offset ( 0);
            cameraInfo.range ( GPUCameraData.sizeof());

            final VkDescriptorBufferInfo.Buffer sceneInfo = VkDescriptorBufferInfo.create(1);
            sceneInfo.buffer ( _sceneParameterBuffer._buffer[0]);
            sceneInfo.offset ( 0);
            sceneInfo.range ( GPUSceneData.sizeof());

            final VkDescriptorBufferInfo.Buffer objectBufferInfo = VkDescriptorBufferInfo.create(1);
            objectBufferInfo.buffer ( _frames[i].objectBuffer._buffer[0]);
            objectBufferInfo.offset ( 0);
            objectBufferInfo.range ( GPUObjectData.sizeof() * MAX_OBJECTS);


            VkWriteDescriptorSet cameraWrite = VkInit.write_descriptor_buffer(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, _frames[i].globalDescriptor[0],cameraInfo,0);

            VkWriteDescriptorSet sceneWrite = VkInit.write_descriptor_buffer(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, _frames[i].globalDescriptor[0], sceneInfo, 1);

            VkWriteDescriptorSet objectWrite = VkInit.write_descriptor_buffer(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, _frames[i].objectDescriptor[0], objectBufferInfo, 0);

            VkWriteDescriptorSet.Buffer setWrites = VkWriteDescriptorSet.create(3);
            setWrites.put(0,cameraWrite);
            setWrites.put(1,sceneWrite);
            setWrites.put(2,objectWrite);

            vkUpdateDescriptorSets(_device, /*3,*/ setWrites, /*0,*/ null);
        }


        _mainDeletionQueue.push_function(() -> {

        vmaDestroyBuffer(_allocator, _sceneParameterBuffer._buffer[0], _sceneParameterBuffer._allocation);

        vkDestroyDescriptorSetLayout(_device, _objectSetLayout[0], null);
        vkDestroyDescriptorSetLayout(_device, _globalSetLayout[0], null);
        vkDestroyDescriptorSetLayout(_device, _singleTextureSetLayout[0], null);

        vkDestroyDescriptorPool(_device, _descriptorPool[0], null);

        for (int i = 0; i < FRAME_OVERLAP; i++)
        {
            vmaDestroyBuffer(_allocator, _frames[i].cameraBuffer._buffer[0], _frames[i].cameraBuffer._allocation);

            vmaDestroyBuffer(_allocator, _frames[i].objectBuffer._buffer[0], _frames[i].objectBuffer._allocation);
        }
    });
    }
}
