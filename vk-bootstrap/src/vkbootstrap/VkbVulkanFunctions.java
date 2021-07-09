package vkbootstrap;


import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.vulkan.*;
import tests.VulkanLibrary;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class VkbVulkanFunctions {

    public interface PFN_vkCreateDebugUtilsMessengerEXT {
        int invoke(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT pCreateInfo, VkAllocationCallbacks pAllocator, final long[] pMessenger);
    }

    public interface PFN_vkGetInstanceProcAddr {
        long invoke(VkInstance instance, CharSequence pName);
    }

    public interface PFN_vkEnumerateInstanceExtensionProperties {
        int invoke(ByteBuffer pLayerName, int[] pPropertyCount, VkExtensionProperties.Buffer pProperties);
    }

    public interface PFN_vkEnumerateInstanceLayerProperties {
        int invoke(int[] pPropertyCount, VkLayerProperties.Buffer pProperties);
    }

    public interface PFN_vkEnumerateInstanceVersion {
        int invoke(int[] pApiVersion);
    }

    public interface PFN_vkCreateInstance {
        int invoke(VkInstanceCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, final VkInstance[] pInstance);
    }

    public interface PFN_vkDestroyInstance {
        void invoke(VkInstance instance, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkEnumeratePhysicalDevices {
        int invoke(VkInstance instance, int[] pPhysicalDeviceCount, PointerBuffer pPhysicalDevices);
    }

    public interface PFN_vkGetPhysicalDeviceFeatures {
        void invoke(VkPhysicalDevice physicalDevice, VkPhysicalDeviceFeatures pFeatures);
    }

    public interface PFN_vkGetPhysicalDeviceFeatures2 {
        void invoke(VkPhysicalDevice physicalDevice, VkPhysicalDeviceFeatures2 pFeatures);
    }
    public interface PFN_vkGetPhysicalDeviceFormatProperties {
        void invoke(VkPhysicalDevice physicalDevice, int format, VkFormatProperties pFormatProperties);
    }

    public interface PFN_vkGetPhysicalDeviceProperties {
        void invoke(VkPhysicalDevice physicalDevice, VkPhysicalDeviceProperties pProperties);
    }

    public interface PFN_vkGetPhysicalDeviceQueueFamilyProperties {
        void invoke(VkPhysicalDevice physicalDevice, int[] pQueueFamilyPropertyCount, VkQueueFamilyProperties.Buffer pQueueFamilyProperties);
    }

    public interface PFN_vkGetPhysicalDeviceMemoryProperties {
        void invoke(VkPhysicalDevice physicalDevice, VkPhysicalDeviceMemoryProperties pMemoryProperties);
    }

    public interface PFN_vkCreateDevice {
        int invoke(VkPhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, final VkDevice[] pDevice);
    }

    public interface PFN_vkDestroyDevice {
        void invoke(VkDevice device, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkEnumerateDeviceExtensionProperties {
        int invoke(VkPhysicalDevice physicalDevice, CharSequence pLayerName, int[] pPropertyCount, org.lwjgl.vulkan.VkExtensionProperties.Buffer pProperties);
    }

    public interface PFN_vkGetDeviceQueue {
        void invoke(VkDevice device, int queueFamilyIndex, int queueIndex, final VkQueue[] pQueue);
    }

    public interface PFN_vkCreateImageView {
        int invoke(VkDevice device, VkImageViewCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pView);
    }

    public interface PFN_vkDestroyImageView {
        void invoke(VkDevice device, long imageView, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkGetPhysicalDeviceSurfaceSupportKHR {
        int invoke(VkPhysicalDevice physicalDevice, int queueFamilyIndex, long surface, int[] pSupported);
    }

    public interface PFN_vkGetPhysicalDeviceSurfaceFormatsKHR {
        int invoke(VkPhysicalDevice physicalDevice, long surface, int[] pSurfaceFormatCount, VkSurfaceFormatKHR.Buffer pSurfaceFormats);
    }

    public interface PFN_vkGetPhysicalDeviceSurfacePresentModesKHR {
        int invoke(VkPhysicalDevice physicalDevice, long surface, int[] pPresentModeCount, int[] pPresentModes);
    }

    public interface PFN_vkGetPhysicalDeviceSurfaceCapabilitiesKHR {
        int invoke(VkPhysicalDevice physicalDevice, long surface, VkSurfaceCapabilitiesKHR pSurfaceCapabilities);
    }

    public interface PFN_vkCreateSwapchainKHR {
        int invoke(VkDevice device, VkSwapchainCreateInfoKHR pCreateInfo, VkAllocationCallbacks pAllocator, long[] pSwapchain);
    }

    public interface PFN_vkDestroySwapchainKHR {
        void invoke(VkDevice device, long swapchain, VkAllocationCallbacks pAllocator);
    }

    public interface PFN_vkGetSwapchainImagesKHR {
        int invoke(VkDevice device, long swapchain, int[] pSwapchainImageCount, long[] pSwapchainImages);
    }

    public interface PFN_vkDestroyDebugUtilsMessengerEXT {
        void invoke(VkInstance instance, long messenger, VkAllocationCallbacks pAllocator);
    }

    /*100*/ boolean load_vulkan(PFN_vkGetInstanceProcAddr fp_vkGetInstanceProcAddr) {
        if (fp_vkGetInstanceProcAddr != null) {
            ptr_vkGetInstanceProcAddr = fp_vkGetInstanceProcAddr;
        } else {
            ptr_vkGetInstanceProcAddr = new PFN_vkGetInstanceProcAddr() {
                public long invoke(VkInstance instance, CharSequence pName) {
                    return VK10.vkGetInstanceProcAddr(instance, pName);
                }
            };
        }
        return true;
    }

    /*123*/ PFN_vkGetInstanceProcAddr ptr_vkGetInstanceProcAddr = null;

    /*126*/ PFN_vkEnumerateInstanceExtensionProperties fp_vkEnumerateInstanceExtensionProperties = null;
    /*127*/ PFN_vkEnumerateInstanceLayerProperties fp_vkEnumerateInstanceLayerProperties = null;

    /*128*/ PFN_vkEnumerateInstanceVersion fp_vkEnumerateInstanceVersion = null;
    /*129*/ PFN_vkCreateInstance fp_vkCreateInstance = null;
    /*130*/ PFN_vkDestroyInstance fp_vkDestroyInstance = null;

    /*132*/ PFN_vkEnumeratePhysicalDevices fp_vkEnumeratePhysicalDevices = null;
    /*133*/ PFN_vkGetPhysicalDeviceFeatures fp_vkGetPhysicalDeviceFeatures = null;
    /*134*/ PFN_vkGetPhysicalDeviceFeatures2 fp_vkGetPhysicalDeviceFeatures2 = null;
    /*135*/ PFN_vkGetPhysicalDeviceFormatProperties fp_vkGetPhysicalDeviceFormatProperties = null;
    /*137*/ PFN_vkGetPhysicalDeviceProperties fp_vkGetPhysicalDeviceProperties = null;
    /*139*/ PFN_vkGetPhysicalDeviceQueueFamilyProperties fp_vkGetPhysicalDeviceQueueFamilyProperties = null;
    /*141*/ PFN_vkGetPhysicalDeviceMemoryProperties fp_vkGetPhysicalDeviceMemoryProperties = null;

    /*145*/ PFN_vkCreateDevice fp_vkCreateDevice = null;
    /*146*/ PFN_vkDestroyDevice fp_vkDestroyDevice = null;
    /*147*/ PFN_vkEnumerateDeviceExtensionProperties fp_vkEnumerateDeviceExtensionProperties = null;
    /*148*/ PFN_vkGetDeviceQueue fp_vkGetDeviceQueue = null;

    /*150*/ PFN_vkCreateImageView fp_vkCreateImageView = null;
    /*151*/ PFN_vkDestroyImageView fp_vkDestroyImageView = null;

    VulkanLibrary.PFN_vkDestroySurfaceKHR fp_vkDestroySurfaceKHR = null;
    /*154*/ PFN_vkGetPhysicalDeviceSurfaceSupportKHR fp_vkGetPhysicalDeviceSurfaceSupportKHR = null;
    /*155*/ PFN_vkGetPhysicalDeviceSurfaceFormatsKHR fp_vkGetPhysicalDeviceSurfaceFormatsKHR = null;
    /*156*/ PFN_vkGetPhysicalDeviceSurfacePresentModesKHR fp_vkGetPhysicalDeviceSurfacePresentModesKHR = null;
    /*157*/ PFN_vkGetPhysicalDeviceSurfaceCapabilitiesKHR fp_vkGetPhysicalDeviceSurfaceCapabilitiesKHR = null;
    /*158*/ PFN_vkCreateSwapchainKHR fp_vkCreateSwapchainKHR = null;

    /*159*/ PFN_vkDestroySwapchainKHR fp_vkDestroySwapchainKHR = null;
    /*160*/ PFN_vkGetSwapchainImagesKHR fp_vkGetSwapchainImagesKHR = null;

    /*115*/ void init_pre_instance_funcs() {
        /*116*/ //get_proc_addr(fp_vkEnumerateInstanceExtensionProperties, "vkEnumerateInstanceExtensionProperties");
        fp_vkEnumerateInstanceExtensionProperties = new PFN_vkEnumerateInstanceExtensionProperties() {
            @Override
            public int invoke(ByteBuffer pLayerName, int[] pPropertyCount, VkExtensionProperties.Buffer pProperties) {
                return VK10.vkEnumerateInstanceExtensionProperties(pLayerName,pPropertyCount,pProperties);
            }
        };

        /*117*/ //get_proc_addr(fp_vkEnumerateInstanceLayerProperties, "vkEnumerateInstanceLayerProperties");
        fp_vkEnumerateInstanceLayerProperties = new PFN_vkEnumerateInstanceLayerProperties() {
            @Override
            public int invoke(int[] pPropertyCount, VkLayerProperties.Buffer pProperties) {
                return VK10.vkEnumerateInstanceLayerProperties(pPropertyCount,pProperties);
            }
        };

        /**/ //get_proc_addr(fp_vkEnumerateInstanceVersion, "vkEnumerateInstanceVersion");
        fp_vkEnumerateInstanceVersion = new PFN_vkEnumerateInstanceVersion() {
            @Override
            public int invoke(int[] pApiVersion) {
                return VK11.vkEnumerateInstanceVersion(pApiVersion);
            }
        };
        //get_proc_addr(fp_vkCreateInstance, "vkCreateInstance");
        fp_vkCreateInstance = new PFN_vkCreateInstance() {
            public int invoke(VkInstanceCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, final VkInstance[] pInstance) {
                PointerBuffer pInstancePB = memAllocPointer(1);
                int ret_code = VK11.vkCreateInstance(pCreateInfo,pAllocator,pInstancePB);
                if (ret_code != VK_SUCCESS) {
                    return ret_code;
                }
                long instance = pInstancePB.get(0);
                memFree(pInstancePB);
                pInstance[0] = new VkInstance(instance, pCreateInfo);
                return ret_code;
            }
        };
    }

    /*162*/ public boolean init_vulkan_funcs(PFN_vkGetInstanceProcAddr fp_vkGetInstanceProcAddr) {
//        std::lock_guard<std::mutex> lg(init_mutex);
        if (!load_vulkan(fp_vkGetInstanceProcAddr)) return false;
        init_pre_instance_funcs();
        return true;
    }
    /*174*/ void init_instance_funcs(VkInstance inst) {
        /*178*/ //get_proc_addr(fp_vkDestroyInstance, "vkDestroyInstance");
        fp_vkDestroyInstance = new PFN_vkDestroyInstance() {
            @Override
            public void invoke(VkInstance instance, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyInstance(instance,pAllocator);
            }
        };
        /*179*/ //get_proc_addr(fp_vkEnumeratePhysicalDevices, "vkEnumeratePhysicalDevices");
        fp_vkEnumeratePhysicalDevices = new PFN_vkEnumeratePhysicalDevices() {
            @Override
            public int invoke(VkInstance instance, int[] pPhysicalDeviceCount, PointerBuffer pPhysicalDevices) {
                return VK10.vkEnumeratePhysicalDevices(instance,pPhysicalDeviceCount,pPhysicalDevices);
            }
        };

        /*180*/ //get_proc_addr(fp_vkGetPhysicalDeviceFeatures, "vkGetPhysicalDeviceFeatures");
        fp_vkGetPhysicalDeviceFeatures = new PFN_vkGetPhysicalDeviceFeatures() {
            @Override
            public void invoke(VkPhysicalDevice physicalDevice, @NativeType("VkPhysicalDeviceFeatures *") VkPhysicalDeviceFeatures pFeatures) {
                VK10.vkGetPhysicalDeviceFeatures(physicalDevice,pFeatures);
            }
        };

        /*181*/ //get_proc_addr(fp_vkGetPhysicalDeviceFeatures2, "vkGetPhysicalDeviceFeatures2");
        fp_vkGetPhysicalDeviceFeatures2 = new PFN_vkGetPhysicalDeviceFeatures2() {
            @Override
            public void invoke(VkPhysicalDevice physicalDevice, VkPhysicalDeviceFeatures2 pFeatures) {
                VK11.vkGetPhysicalDeviceFeatures2(physicalDevice,pFeatures);
            }
        };

        /*182*/ //get_proc_addr(fp_vkGetPhysicalDeviceFormatProperties, "vkGetPhysicalDeviceFormatProperties");
        fp_vkGetPhysicalDeviceFormatProperties = new PFN_vkGetPhysicalDeviceFormatProperties() {
            @Override
            public void invoke(VkPhysicalDevice physicalDevice, int format, VkFormatProperties pFormatProperties) {
                VK10.vkGetPhysicalDeviceFormatProperties(physicalDevice,format,pFormatProperties);
            }
        };

        /*184*/ //get_proc_addr(fp_vkGetPhysicalDeviceProperties, "vkGetPhysicalDeviceProperties");
        fp_vkGetPhysicalDeviceProperties = new PFN_vkGetPhysicalDeviceProperties() {
            @Override
            public void invoke(VkPhysicalDevice physicalDevice, @NativeType("VkPhysicalDeviceProperties *") VkPhysicalDeviceProperties pProperties) {
                VK10.vkGetPhysicalDeviceProperties(physicalDevice,pProperties);
            }
        };

        /*186*/ //get_proc_addr(fp_vkGetPhysicalDeviceQueueFamilyProperties, "vkGetPhysicalDeviceQueueFamilyProperties");
        fp_vkGetPhysicalDeviceQueueFamilyProperties = new PFN_vkGetPhysicalDeviceQueueFamilyProperties() {
            @Override
            public void invoke(VkPhysicalDevice physicalDevice, int[] pQueueFamilyPropertyCount, VkQueueFamilyProperties.Buffer pQueueFamilyProperties) {
                VK10.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice,pQueueFamilyPropertyCount,pQueueFamilyProperties);
            }
        };

        /*188*/ //get_proc_addr(fp_vkGetPhysicalDeviceMemoryProperties, "vkGetPhysicalDeviceMemoryProperties");
        fp_vkGetPhysicalDeviceMemoryProperties = new PFN_vkGetPhysicalDeviceMemoryProperties() {
            @Override
            public void invoke(VkPhysicalDevice physicalDevice, VkPhysicalDeviceMemoryProperties pMemoryProperties) {
                VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice,pMemoryProperties);
            }
        };

        /*192*/ //get_proc_addr(fp_vkCreateDevice, "vkCreateDevice");
        fp_vkCreateDevice = new PFN_vkCreateDevice() {
            public int invoke(VkPhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator,  final VkDevice[] pDevice) {
                int ret_val = VK_SUCCESS;
                PointerBuffer pp = memAllocPointer(1);
                ret_val = VK10.vkCreateDevice(physicalDevice, pCreateInfo, pAllocator, pp);
                if (VK_SUCCESS == ret_val) {
                    pDevice[0] = new VkDevice(pp.get(0), physicalDevice, pCreateInfo);
                }
                memFree(pp);
                return ret_val;
            }
        };
        /*193*/ //get_proc_addr(fp_vkDestroyDevice, "vkDestroyDevice");
        fp_vkDestroyDevice = new PFN_vkDestroyDevice() {
            @Override
            public void invoke(VkDevice device, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyDevice(device,pAllocator);
            }
        };

        /*194*/ //get_proc_addr(fp_vkEnumerateDeviceExtensionProperties, "vkEnumerateDeviceExtensionProperties");
        fp_vkEnumerateDeviceExtensionProperties = new PFN_vkEnumerateDeviceExtensionProperties() {
            @Override
            public int invoke(VkPhysicalDevice physicalDevice, CharSequence pLayerName, int[] pPropertyCount, org.lwjgl.vulkan.VkExtensionProperties.Buffer pProperties) {
                return VK10.vkEnumerateDeviceExtensionProperties(physicalDevice,pLayerName,pPropertyCount,pProperties);
            }
        };

        /*195*/ //get_proc_addr(fp_vkGetDeviceQueue, "vkGetDeviceQueue");
        fp_vkGetDeviceQueue = new PFN_vkGetDeviceQueue() {
            @Override
            public void invoke(VkDevice device, int queueFamilyIndex, int queueIndex, final VkQueue[] pQueue) {
                PointerBuffer pb = memAllocPointer(1);
                VK10.vkGetDeviceQueue(device,queueFamilyIndex,queueIndex,pb);
                pQueue[0] = new VkQueue(pb.get(0),device);
                memFree(pb);
            }
        };

        /*197*/ //get_proc_addr(fp_vkCreateImageView, "vkCreateImageView");
        fp_vkCreateImageView = new PFN_vkCreateImageView() {
            @Override
            public int invoke(VkDevice device, VkImageViewCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pView) {
                return VK10.vkCreateImageView(device,pCreateInfo,pAllocator,pView);
            }
        };
        /*198*/ //get_proc_addr(fp_vkDestroyImageView, "vkDestroyImageView");
        fp_vkDestroyImageView = new PFN_vkDestroyImageView() {
            @Override
            public void invoke(VkDevice device, long imageView, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyImageView(device,imageView,pAllocator);
            }
        };

        /*188*/ //get_inst_proc_addr(fp_vkDestroySurfaceKHR, "vkDestroySurfaceKHR");
        fp_vkDestroySurfaceKHR = new VulkanLibrary.PFN_vkDestroySurfaceKHR() {
            @Override
            public void invoke(VkInstance instance, long surface, VkAllocationCallbacks pAllocator) {
                KHRSurface.vkDestroySurfaceKHR(instance,surface,pAllocator);
            }
        };

        /*201*/ //get_proc_addr(fp_vkGetPhysicalDeviceSurfaceSupportKHR, "vkGetPhysicalDeviceSurfaceSupportKHR");
        fp_vkGetPhysicalDeviceSurfaceSupportKHR = new PFN_vkGetPhysicalDeviceSurfaceSupportKHR() {
            @Override
            public int invoke(VkPhysicalDevice physicalDevice, int queueFamilyIndex, long surface, int[] pSupported) {
                return KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice,queueFamilyIndex,surface,pSupported);
            }
        };

        /*202*/ //get_proc_addr(fp_vkGetPhysicalDeviceSurfaceFormatsKHR, "vkGetPhysicalDeviceSurfaceFormatsKHR");
        fp_vkGetPhysicalDeviceSurfaceFormatsKHR = new PFN_vkGetPhysicalDeviceSurfaceFormatsKHR() {
            @Override
            public int invoke(VkPhysicalDevice physicalDevice, long surface, int[] pSurfaceFormatCount, VkSurfaceFormatKHR.Buffer pSurfaceFormats) {
                return KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice,surface,pSurfaceFormatCount,pSurfaceFormats);
            }
        };
        /*203*/ //get_proc_addr(fp_vkGetPhysicalDeviceSurfacePresentModesKHR, "vkGetPhysicalDeviceSurfacePresentModesKHR");
        fp_vkGetPhysicalDeviceSurfacePresentModesKHR = new PFN_vkGetPhysicalDeviceSurfacePresentModesKHR() {
            @Override
            public int invoke(VkPhysicalDevice physicalDevice, long surface, int[] pPresentModeCount, int[] pPresentModes) {
                return KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice,surface,pPresentModeCount,pPresentModes);
            }
        };

        /*204*/ //get_proc_addr(fp_vkGetPhysicalDeviceSurfaceCapabilitiesKHR, "vkGetPhysicalDeviceSurfaceCapabilitiesKHR");
        fp_vkGetPhysicalDeviceSurfaceCapabilitiesKHR = new PFN_vkGetPhysicalDeviceSurfaceCapabilitiesKHR() {
            @Override
            public int invoke(VkPhysicalDevice physicalDevice, long surface, VkSurfaceCapabilitiesKHR pSurfaceCapabilities) {
                return KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice,surface,pSurfaceCapabilities);
            }
        };

        /*205*/ //get_proc_addr(fp_vkCreateSwapchainKHR, "vkCreateSwapchainKHR");
        fp_vkCreateSwapchainKHR = new PFN_vkCreateSwapchainKHR() {
            @Override
            public int invoke(VkDevice device, VkSwapchainCreateInfoKHR pCreateInfo, VkAllocationCallbacks pAllocator, long[] pSwapchain) {
                return KHRSwapchain.vkCreateSwapchainKHR(device,pCreateInfo,pAllocator,pSwapchain);
            }
        };

        /*206*/ //get_proc_addr(fp_vkDestroySwapchainKHR, "vkDestroySwapchainKHR");
        fp_vkDestroySwapchainKHR = new PFN_vkDestroySwapchainKHR() {
            @Override
            public void invoke(VkDevice device, long swapchain, VkAllocationCallbacks pAllocator) {
                KHRSwapchain.vkDestroySwapchainKHR(device,swapchain,pAllocator);
            }
        };

        /*207*/ //get_proc_addr(fp_vkGetSwapchainImagesKHR, "vkGetSwapchainImagesKHR");
        fp_vkGetSwapchainImagesKHR = new PFN_vkGetSwapchainImagesKHR() {
            @Override
            public int invoke(VkDevice device, long swapchain, int[] pSwapchainImageCount, long[] pSwapchainImages) {
                return KHRSwapchain.vkGetSwapchainImagesKHR(device,swapchain,pSwapchainImageCount,pSwapchainImages);
            }
        };
    }
}
