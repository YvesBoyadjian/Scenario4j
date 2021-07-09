package tests;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.NativeType;
import org.lwjgl.vulkan.*;

import java.util.List;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class VulkanLibrary {

    public interface PFN_vkCreateRenderPass {
        int invoke(VkDevice device, VkRenderPassCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pRenderPass);
    }

    public interface PFN_vkCreateShaderModule {
        int invoke(VkDevice device, VkShaderModuleCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pShaderModule);
    }

    public interface PFN_vkCreatePipelineLayout {
        int invoke(VkDevice device, VkPipelineLayoutCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pPipelineLayout);
    }

    public interface PFN_vkCreateGraphicsPipelines {
        int invoke(VkDevice device, long pipelineCache, VkGraphicsPipelineCreateInfo.Buffer pCreateInfos, VkAllocationCallbacks pAllocator, long[] pPipelines);
    }

    public interface PFN_vkDestroyShaderModule {
        void invoke(VkDevice device, long shaderModule, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkCreateFramebuffer {
        int invoke(VkDevice device, VkFramebufferCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pFramebuffer);
    }

    public interface PFN_vkCreateCommandPool {
        int invoke(VkDevice device, VkCommandPoolCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pCommandPool);
    }

    public interface PFN_vkAllocateCommandBuffers {
        int invoke(VkDevice device, VkCommandBufferAllocateInfo pAllocateInfo, /*PointerBuffer*/List<VkCommandBuffer> pCommandBuffers, int size);
    }

    public interface PFN_vkBeginCommandBuffer {
        int invoke(VkCommandBuffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo);
    }

    public interface PFN_vkEndCommandBuffer{
        int invoke(VkCommandBuffer commandBuffer);
    }

    public interface PFN_vkCmdSetViewport{
        void invoke(VkCommandBuffer commandBuffer, int firstViewport, VkViewport.Buffer pViewports);
    }

    public interface PFN_vkCmdSetScissor{
        void invoke(VkCommandBuffer commandBuffer, int firstScissor, VkRect2D.Buffer pScissors);
    }

    public interface PFN_vkCmdBeginRenderPass{
        void invoke(VkCommandBuffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, int contents);
    }

    public interface PFN_vkCmdEndRenderPass{
        void invoke(VkCommandBuffer commandBuffer);
    }

    public interface PFN_vkCmdBindPipeline{
        void invoke(VkCommandBuffer commandBuffer, int pipelineBindPoint, long pipeline);
    }

    public interface PFN_vkCmdDraw{
        void invoke(VkCommandBuffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance);
    }

    public interface PFN_vkCreateSemaphore {
        int invoke(VkDevice device, VkSemaphoreCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pSemaphore);
    }
    public interface PFN_vkCreateFence {
        int invoke(VkDevice device, VkFenceCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pFence);
    }

    public interface PFN_vkDeviceWaitIdle {
        int invoke(VkDevice device);
    }

    public interface PFN_vkDestroyCommandPool {
        void invoke(VkDevice device, long commandPool, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkDestroyFramebuffer {
        void invoke(VkDevice device, long framebuffer, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkWaitForFences {
        int invoke(VkDevice device, long[] pFence, boolean waitAll, long timeout);
    }

    public interface PFN_vkAcquireNextImageKHR {
        int invoke(VkDevice device, long swapchain, long timeout, long semaphore, long fence, int[] pImageIndex);
    }

    public interface PFN_vkResetFences {
        int invoke(VkDevice device, long[] pFences);
    }

    public interface PFN_vkQueueSubmit {
        int invoke(VkQueue queue, VkSubmitInfo pSubmit, long fence);
    }

    public interface PFN_vkQueuePresentKHR {
        int invoke(VkQueue queue, VkPresentInfoKHR pPresentInfo);
    }

    public interface PFN_vkDestroySemaphore {
        void invoke(VkDevice device, long semaphore, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkDestroyFence {
        void invoke(VkDevice device, long fence, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkDestroyPipeline {
        void invoke(VkDevice device, long pipeline, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkDestroyPipelineLayout {
        void invoke(VkDevice device, long pipelineLayout, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkDestroySurfaceKHR {
        void invoke(VkInstance instance, long surface, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkDestroyRenderPass {
        void invoke(VkDevice device, long renderPass, VkAllocationCallbacks pAllocator);
    }

    /*135*/ public PFN_vkCreateRenderPass vkCreateRenderPass = /*VK_NULL_HANDLE*/null;
    /*136*/ public PFN_vkCreateShaderModule vkCreateShaderModule = /*VK_NULL_HANDLE*/null;
    /*137*/ public PFN_vkCreatePipelineLayout vkCreatePipelineLayout = /*VK_NULL_HANDLE*/null;
    /*138*/ public PFN_vkCreateGraphicsPipelines vkCreateGraphicsPipelines = /*VK_NULL_HANDLE*/null;
    /*139*/ public PFN_vkDestroyShaderModule vkDestroyShaderModule = /*VK_NULL_HANDLE*/null;
    /*140*/ public PFN_vkCreateFramebuffer vkCreateFramebuffer = /*VK_NULL_HANDLE*/null;
    /*141*/ public PFN_vkCreateCommandPool vkCreateCommandPool = /*VK_NULL_HANDLE*/null;
    /*142*/ public PFN_vkAllocateCommandBuffers vkAllocateCommandBuffers = /*VK_NULL_HANDLE*/null;
    /*143*/ public PFN_vkBeginCommandBuffer vkBeginCommandBuffer = /*VK_NULL_HANDLE*/null;
    /*144*/ public PFN_vkEndCommandBuffer vkEndCommandBuffer = /*VK_NULL_HANDLE*/null;
    /*145*/ public PFN_vkCmdSetViewport vkCmdSetViewport = /*VK_NULL_HANDLE*/null;
    /*146*/ public PFN_vkCmdSetScissor vkCmdSetScissor = /*VK_NULL_HANDLE*/null;
    /*147*/ public PFN_vkCmdBeginRenderPass vkCmdBeginRenderPass = /*VK_NULL_HANDLE*/null;
    /*148*/ public PFN_vkCmdEndRenderPass vkCmdEndRenderPass = /*VK_NULL_HANDLE*/null;
    /*149*/ public PFN_vkCmdBindPipeline vkCmdBindPipeline = /*VK_NULL_HANDLE*/null;
    /*150*/ public PFN_vkCmdDraw vkCmdDraw = /*VK_NULL_HANDLE*/null;
    /*151*/ public PFN_vkCreateSemaphore vkCreateSemaphore = /*VK_NULL_HANDLE*/null;
    /*152*/ public PFN_vkCreateFence vkCreateFence = /*VK_NULL_HANDLE*/null;
    /*153*/ public PFN_vkDeviceWaitIdle vkDeviceWaitIdle = /*VK_NULL_HANDLE*/null;
    /*154*/ public PFN_vkDestroyCommandPool vkDestroyCommandPool = /*VK_NULL_HANDLE*/null;
    /*155*/ public PFN_vkDestroyFramebuffer vkDestroyFramebuffer = /*VK_NULL_HANDLE*/null;
    /*156*/ public PFN_vkWaitForFences vkWaitForFences = /*VK_NULL_HANDLE*/null;
    /*157*/ public PFN_vkAcquireNextImageKHR vkAcquireNextImageKHR = /*VK_NULL_HANDLE*/null;
    /*158*/ public PFN_vkResetFences vkResetFences = /*VK_NULL_HANDLE*/null;
    /*159*/	public PFN_vkQueueSubmit vkQueueSubmit = /*VK_NULL_HANDLE*/null;
    /*160*/ public PFN_vkQueuePresentKHR vkQueuePresentKHR = /*VK_NULL_HANDLE*/null;
    /*161*/ public PFN_vkDestroySemaphore vkDestroySemaphore = /*VK_NULL_HANDLE*/null;
    /*162*/ public PFN_vkDestroyFence vkDestroyFence = /*VK_NULL_HANDLE*/null;
    /*163*/ public PFN_vkDestroyPipeline vkDestroyPipeline = /*VK_NULL_HANDLE*/null;
    /*164*/ public PFN_vkDestroyPipelineLayout vkDestroyPipelineLayout = /*VK_NULL_HANDLE*/null;
    /*165*/ public PFN_vkDestroySurfaceKHR vkDestroySurfaceKHR = /*VK_NULL_HANDLE*/null;
    /*166*/ public PFN_vkDestroyRenderPass vkDestroyRenderPass = /*VK_NULL_HANDLE*/null;

    public void init(VkInstance instance) {
        vkDestroySurfaceKHR = //(PFN_vkDestroySurfaceKHR)vkGetInstanceProcAddr(instance, "vkDestroySurfaceKHR");
        new PFN_vkDestroySurfaceKHR() {
            @Override
            public void invoke(VkInstance instance, long surface, VkAllocationCallbacks pAllocator) {
                KHRSurface.vkDestroySurfaceKHR(instance,surface,pAllocator);
            }
        };
    }

    public void init(VkDevice device) {
        /*95*/ vkCreateRenderPass = //(PFN_vkCreateRenderPass)vkGetDeviceProcAddr(device, "vkCreateRenderPass");
        new PFN_vkCreateRenderPass() {
            @Override
            public int invoke(VkDevice device, VkRenderPassCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pRenderPass) {
                return VK10.vkCreateRenderPass(device,pCreateInfo,pAllocator,pRenderPass);
            }
        };
        /*96*/ vkCreateShaderModule = //(PFN_vkCreateShaderModule)vkGetDeviceProcAddr(device, "vkCreateShaderModule");
        new PFN_vkCreateShaderModule() {
            @Override
            public int invoke(VkDevice device, VkShaderModuleCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pShaderModule) {
                return VK10.vkCreateShaderModule(device,pCreateInfo,pAllocator,pShaderModule);
            }
        };
        /*97*/ vkCreatePipelineLayout =
                //(PFN_vkCreatePipelineLayout)vkGetDeviceProcAddr(device, "vkCreatePipelineLayout");
        new PFN_vkCreatePipelineLayout() {
            @Override
            public int invoke(VkDevice device, VkPipelineLayoutCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pPipelineLayout) {
                return VK10.vkCreatePipelineLayout(device,pCreateInfo,pAllocator,pPipelineLayout);
            }
        };
        /*99*/ vkCreateGraphicsPipelines =
                //(PFN_vkCreateGraphicsPipelines)vkGetDeviceProcAddr(device, "vkCreateGraphicsPipelines");
        new PFN_vkCreateGraphicsPipelines() {
            @Override
            public int invoke(VkDevice device, long pipelineCache, VkGraphicsPipelineCreateInfo.Buffer pCreateInfos, VkAllocationCallbacks pAllocator, long[] pPipelines) {
                return VK10.vkCreateGraphicsPipelines(device,pipelineCache,pCreateInfos,pAllocator,pPipelines);
            }
        };
        /*101*/vkDestroyShaderModule = //(PFN_vkDestroyShaderModule)vkGetDeviceProcAddr(device, "vkDestroyShaderModule");
        new PFN_vkDestroyShaderModule() {
            @Override
            public void invoke(VkDevice device, long shaderModule, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyShaderModule(device,shaderModule,pAllocator);
            }
        };
        /*102*/ vkCreateFramebuffer = //(PFN_vkCreateFramebuffer)vkGetDeviceProcAddr(device, "vkCreateFramebuffer");
        new PFN_vkCreateFramebuffer() {
            @Override
            public int invoke(VkDevice device, VkFramebufferCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pFramebuffer) {
                return VK10.vkCreateFramebuffer(device,pCreateInfo,pAllocator,pFramebuffer);
            }
        };
        /*103*/ vkCreateCommandPool = //(PFN_vkCreateCommandPool)vkGetDeviceProcAddr(device, "vkCreateCommandPool");
        new PFN_vkCreateCommandPool() {
            @Override
            public int invoke(VkDevice device, VkCommandPoolCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pCommandPool) {
                return VK10.vkCreateCommandPool(device,pCreateInfo,pAllocator,pCommandPool);
            }
        };
        /*104*/ vkAllocateCommandBuffers =
                //(PFN_vkAllocateCommandBuffers)vkGetDeviceProcAddr(device, "vkAllocateCommandBuffers");
        new PFN_vkAllocateCommandBuffers() {
            @Override
            public int invoke(VkDevice device, VkCommandBufferAllocateInfo pAllocateInfo, /*PointerBuffer*/List<VkCommandBuffer> commandBuffers, int size) {
                PointerBuffer pCommandBuffers = memAllocPointer(size);
                int ret_val = VK10.vkAllocateCommandBuffers(device, pAllocateInfo, pCommandBuffers);
                commandBuffers.clear();
                if(ret_val == VK_SUCCESS) {
                    for( int i=0;i<size;i++) {
                        commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), device));
                    }
                }
                memFree(pCommandBuffers);
                return ret_val;
            }
        };
        /*106*/ vkBeginCommandBuffer = //(PFN_vkBeginCommandBuffer)vkGetDeviceProcAddr(device, "vkBeginCommandBuffer");
        new PFN_vkBeginCommandBuffer() {
            @Override
            public int invoke(VkCommandBuffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo) {
                return VK10.vkBeginCommandBuffer(commandBuffer, pBeginInfo);
            }
        };
        /*107*/ vkEndCommandBuffer = //(PFN_vkEndCommandBuffer)vkGetDeviceProcAddr(device, "vkEndCommandBuffer");
                new PFN_vkEndCommandBuffer() {
                    @Override
                    public int invoke(VkCommandBuffer commandBuffer) {
                        return VK10.vkEndCommandBuffer(commandBuffer);
                    }
                };
        /*108*/ vkCmdSetViewport = //(PFN_vkCmdSetViewport)vkGetDeviceProcAddr(device, "vkCmdSetViewport");
                new PFN_vkCmdSetViewport() {
                    @Override
                    public void invoke(VkCommandBuffer commandBuffer, int firstViewport, VkViewport.Buffer pViewports) {
                        VK10.vkCmdSetViewport(commandBuffer,firstViewport,pViewports);
                    }
                };
        /*109*/ vkCmdSetScissor = //(PFN_vkCmdSetScissor)vkGetDeviceProcAddr(device, "vkCmdSetScissor");
                new PFN_vkCmdSetScissor() {
                    @Override
                    public void invoke(VkCommandBuffer commandBuffer, int firstScissor, VkRect2D.Buffer pScissors) {
                        VK10.vkCmdSetScissor(commandBuffer,firstScissor,pScissors);
                    }
                };
        /*110*/ vkCmdBeginRenderPass = //(PFN_vkCmdBeginRenderPass)vkGetDeviceProcAddr(device, "vkCmdBeginRenderPass");
                new PFN_vkCmdBeginRenderPass() {
                    @Override
                    public void invoke(VkCommandBuffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, int contents) {
                        VK10.vkCmdBeginRenderPass(commandBuffer,pRenderPassBegin,contents);
                    }
                };
        /*111*/ vkCmdEndRenderPass = //(PFN_vkCmdEndRenderPass)vkGetDeviceProcAddr(device, "vkCmdEndRenderPass");
                new PFN_vkCmdEndRenderPass() {
                    @Override
                    public void invoke(VkCommandBuffer commandBuffer) {
                        VK10.vkCmdEndRenderPass(commandBuffer);
                    }
                };
        /*112*/ vkCmdBindPipeline = //(PFN_vkCmdBindPipeline)vkGetDeviceProcAddr(device, "vkCmdBindPipeline");
                new PFN_vkCmdBindPipeline() {
                    @Override
                    public void invoke(VkCommandBuffer commandBuffer, int pipelineBindPoint, long pipeline) {
                        VK10.vkCmdBindPipeline(commandBuffer,pipelineBindPoint,pipeline);
                    }
                };
        /*113*/ vkCmdDraw = //(PFN_vkCmdDraw)vkGetDeviceProcAddr(device, "vkCmdDraw");
        new PFN_vkCmdDraw() {
            @Override
            public void invoke(VkCommandBuffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
                VK10.vkCmdDraw(commandBuffer,vertexCount,instanceCount,firstVertex,firstInstance);
            }
        };
        /*114*/ vkCreateSemaphore = //(PFN_vkCreateSemaphore)vkGetDeviceProcAddr(device, "vkCreateSemaphore");
                new PFN_vkCreateSemaphore() {
                    @Override
                    public int invoke(VkDevice device, VkSemaphoreCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pSemaphore) {
                        return VK10.vkCreateSemaphore(device,pCreateInfo,pAllocator,pSemaphore);
                    }
                };
        /*115*/ vkCreateFence = //(PFN_vkCreateFence)vkGetDeviceProcAddr(device, "vkCreateFence");
        new PFN_vkCreateFence() {
            @Override
            public int invoke(VkDevice device, VkFenceCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pFence) {
                return VK10.vkCreateFence(device,pCreateInfo,pAllocator,pFence);
            }
        };
        /*116*/ vkDeviceWaitIdle = //(PFN_vkDeviceWaitIdle)vkGetDeviceProcAddr(device, "vkDeviceWaitIdle");
        new PFN_vkDeviceWaitIdle() {
            @Override
            public int invoke(VkDevice device) {
                return VK10.vkDeviceWaitIdle(device);
            }
        };
        /*117*/ vkDestroyCommandPool = //(PFN_vkDestroyCommandPool)vkGetDeviceProcAddr(device, "vkDestroyCommandPool");
        new PFN_vkDestroyCommandPool() {
            @Override
            public void invoke(VkDevice device, long commandPool, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyCommandPool(device,commandPool,pAllocator);
            }
        };
        /*118*/ vkDestroyFramebuffer = //(PFN_vkDestroyFramebuffer)vkGetDeviceProcAddr(device, "vkDestroyFramebuffer");
        new PFN_vkDestroyFramebuffer() {
            @Override
            public void invoke(VkDevice device, long framebuffer, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyFramebuffer(device,framebuffer,pAllocator);
            }
        };
        /*119*/ vkWaitForFences = //(PFN_vkWaitForFences)vkGetDeviceProcAddr(device, "vkWaitForFences");
        new PFN_vkWaitForFences() {
            @Override
            public int invoke(VkDevice device, long[] pFence, boolean waitAll, long timeout) {
                return VK10.vkWaitForFences(device,pFence,waitAll,timeout);
            }
        };
        /*120*/ vkAcquireNextImageKHR = //(PFN_vkAcquireNextImageKHR)vkGetDeviceProcAddr(device, "vkAcquireNextImageKHR");
        new PFN_vkAcquireNextImageKHR() {
            @Override
            public int invoke(VkDevice device, long swapchain, long timeout, long semaphore, long fence, int[] pImageIndex) {
                return KHRSwapchain.vkAcquireNextImageKHR(device,swapchain,timeout,semaphore,fence,pImageIndex);
            }
        };
        /*121*/ vkResetFences = //(PFN_vkResetFences)vkGetDeviceProcAddr(device, "vkResetFences");
        new PFN_vkResetFences() {
            @Override
            public int invoke(VkDevice device, long[] pFences) {
                return VK10.vkResetFences(device,pFences);
            }
        };
        /*122*/ vkQueueSubmit = //(PFN_vkQueueSubmit)vkGetDeviceProcAddr(device, "vkQueueSubmit");
        new PFN_vkQueueSubmit() {
            @Override
            public int invoke(VkQueue queue, VkSubmitInfo pSubmit, long fence) {
                return VK10.vkQueueSubmit(queue,pSubmit,fence);
            }
        };
        /*123*/ vkQueuePresentKHR = //(PFN_vkQueuePresentKHR)vkGetDeviceProcAddr(device, "vkQueuePresentKHR");
        new PFN_vkQueuePresentKHR() {
            @Override
            public int invoke(VkQueue queue, VkPresentInfoKHR pPresentInfo) {
                return KHRSwapchain.vkQueuePresentKHR(queue,pPresentInfo);
            }
        };
        /*124*/ vkDestroySemaphore = //(PFN_vkDestroySemaphore)vkGetDeviceProcAddr(device, "vkDestroySemaphore");
        new PFN_vkDestroySemaphore() {
            @Override
            public void invoke(VkDevice device, long semaphore, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroySemaphore(device,semaphore,pAllocator);
            }
        };
        /*125*/ vkDestroyFence = //(PFN_vkDestroyFence)vkGetDeviceProcAddr(device, "vkDestroyFence");
        new PFN_vkDestroyFence() {
            @Override
            public void invoke(VkDevice device, long fence, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyFence(device,fence,pAllocator);
            }
        };
        /*126*/ vkDestroyPipeline = //(PFN_vkDestroyPipeline)vkGetDeviceProcAddr(device, "vkDestroyPipeline");
        new PFN_vkDestroyPipeline() {
            @Override
            public void invoke(VkDevice device, long pipeline, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyPipeline(device,pipeline,pAllocator);
            }
        };
        /*127*/ vkDestroyPipelineLayout =
        //(PFN_vkDestroyPipelineLayout)vkGetDeviceProcAddr(device, "vkDestroyPipelineLayout");
        new PFN_vkDestroyPipelineLayout() {
            @Override
            public void invoke(VkDevice device, long pipelineLayout, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyPipelineLayout(device,pipelineLayout,pAllocator);
            }
        };
        /*129*/ vkDestroyRenderPass = //(PFN_vkDestroyRenderPass)vkGetDeviceProcAddr(device, "vkDestroyRenderPass");
        new PFN_vkDestroyRenderPass() {
            @Override
            public void invoke(VkDevice device, long renderPass, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyRenderPass(device,renderPass,pAllocator);
            }
        };
    }
}
