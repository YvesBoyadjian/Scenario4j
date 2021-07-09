package vkbootstrap;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.NativeType;
import org.lwjgl.vulkan.*;
import port.Port;
import port.error_code;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class VkBootstrap {

    public static String to_string_message_severity(/*VkDebugUtilsMessageSeverityFlagBitsEXT*/int s) {
        switch (s) {
            case /*VkDebugUtilsMessageSeverityFlagBitsEXT::*/VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT:
                return "VERBOSE";
            case /*VkDebugUtilsMessageSeverityFlagBitsEXT::*/VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT:
                return "ERROR";
            case /*VkDebugUtilsMessageSeverityFlagBitsEXT::*/VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:
                return "WARNING";
            case /*VkDebugUtilsMessageSeverityFlagBitsEXT::*/VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:
                return "INFO";
            default:
                return "UNKNOWN";
        }
    }

    public static String to_string_message_type(/*VkDebugUtilsMessageTypeFlagsEXT*/int s) {
        if (s == 7) return "General | Validation | Performance";
        if (s == 6) return "Validation | Performance";
        if (s == 5) return "General | Performance";
        if (s == 4 /*VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT*/) return "Performance";
        if (s == 3) return "General | Validation";
        if (s == 2 /*VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT*/) return "Validation";
        if (s == 1 /*VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT*/) return "General";
        return "Unknown";
    }

    private static final VkbVulkanFunctions v = new VkbVulkanFunctions();
    /*211*/ public static VkbVulkanFunctions vulkan_functions() {
        return v;
    }

    // Helper for robustly executing the two-call pattern
    /*218*/ public static <T extends VkPhysicalDevice, F extends VkbVulkanFunctions.PFN_vkEnumeratePhysicalDevices> int get_vector(final List<T> out, F f, VkInstance ts) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke(ts, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            PointerBuffer pPhysicalDevices = memAllocPointer(count[0]);
            err = f.invoke(ts, count, /*out.data()*/pPhysicalDevices);
            out.clear();//out.resize(count);
            if( err == VK_SUCCESS) {
                for (int i = 0; i < count[0]; i++) {
                    long physicalDevice = pPhysicalDevices.get(i);
                    out.add((T) new VkPhysicalDevice(physicalDevice, ts));
                }
            }
            memFree(pPhysicalDevices);

        } while (err == VK_INCOMPLETE);
        return err;
    }

    /*218*/ public static <T extends VkExtensionProperties, F extends VkbVulkanFunctions.PFN_vkEnumerateDeviceExtensionProperties> int get_vector(final List<T> out, F f, VkPhysicalDevice ts, CharSequence o) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke(ts, o, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            org.lwjgl.vulkan.VkExtensionProperties.Buffer pExtensionsProperties = org.lwjgl.vulkan.VkExtensionProperties.create(count[0]);
            err = f.invoke(ts, o, count, /*out.data()*/pExtensionsProperties);
            out.clear();//out.resize(count);
            if( err == VK_SUCCESS) {
                for (int i = 0; i < count[0]; i++) {
                    out.add((T) pExtensionsProperties.get(i));
                }
            }

        } while (err == VK_INCOMPLETE);
        return err;
    }

    /*218*/ public static <T extends VkExtensionProperties, F extends VkbVulkanFunctions.PFN_vkEnumerateInstanceExtensionProperties> int get_vector(final List<T> out, F f, ByteBuffer o) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke( o, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            org.lwjgl.vulkan.VkExtensionProperties.Buffer pExtensionsProperties = org.lwjgl.vulkan.VkExtensionProperties.create(count[0]);
            err = f.invoke( o, count, /*out.data()*/pExtensionsProperties);
            out.clear();//out.resize(count);
            if( err == VK_SUCCESS) {
                for (int i = 0; i < count[0]; i++) {
                    out.add((T) pExtensionsProperties.get(i));
                }
            }

        } while (err == VK_INCOMPLETE);
        return err;
    }

    /*218*/ public static <T extends VkLayerProperties, F extends VkbVulkanFunctions.PFN_vkEnumerateInstanceLayerProperties> int get_vector(final List<T> out, F f) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke( count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            org.lwjgl.vulkan.VkLayerProperties.Buffer pExtensionsProperties = org.lwjgl.vulkan.VkLayerProperties.create(count[0]);
            err = f.invoke( count, /*out.data()*/pExtensionsProperties);
            out.clear();//out.resize(count);
            if( err == VK_SUCCESS) {
                for (int i = 0; i < count[0]; i++) {
                    out.add((T) pExtensionsProperties.get(i));
                }
            }

        } while (err == VK_INCOMPLETE);
        return err;
    }

    // Helper for robustly executing the two-call pattern
    /*218*/ public static int get_vector(final List<VkSurfaceFormatKHR> out, VkbVulkanFunctions.PFN_vkGetPhysicalDeviceSurfaceFormatsKHR f, VkPhysicalDevice ts1, long ts2 ) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke(ts1, ts2, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            VkSurfaceFormatKHR.Buffer buffer = VkSurfaceFormatKHR.create(count[0]);
            err = f.invoke(ts1,ts2, count, buffer);
            //out.resize(count);
            out.clear();
            for(int i=0; i<count[0];i++) {
                out.add(buffer.get(i));
            }
        } while (err == VK_INCOMPLETE);
        return err;
    }

    // Helper for robustly executing the two-call pattern
    /*218*/ public static int get_vector(final List<Integer> out, VkbVulkanFunctions.PFN_vkGetPhysicalDeviceSurfacePresentModesKHR f, VkPhysicalDevice ts1, long ts2) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke(ts1,ts2, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            final int[] pPresentModes = new int[count[0]];
            err = f.invoke(ts1,ts2, count, pPresentModes);
            //out.resize(count);
            out.clear();
            for( int i=0;i<count[0];i++) {
                out.add(pPresentModes[i]);
            }
        } while (err == VK_INCOMPLETE);
        return err;
    }

    // Helper for robustly executing the two-call pattern
    //template <typename T, typename F, typename... Ts>
    /*218*/public static int get_vector(final List<Long> out, VkbVulkanFunctions.PFN_vkGetSwapchainImagesKHR f, VkDevice ts1, long ts2) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke(ts1,ts2, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            long[] outArray = new long[count[0]];
            err = f.invoke(ts1,ts2, count, outArray);
            //out.resize(count);
            out.clear();
            for( int i=0;i<count[0];i++) {
                out.add(outArray[i]);
            }
        } while (err == VK_INCOMPLETE);
        return err;
    }

    /*234*/ public static List<VkQueueFamilyProperties> get_vector_noerror(VkbVulkanFunctions.PFN_vkGetPhysicalDeviceQueueFamilyProperties f, VkPhysicalDevice ts) {
        final int[] count = new int[1];
        f.invoke(ts, count, null);
        final VkQueueFamilyProperties.Buffer results = VkQueueFamilyProperties.create(count[0]);
        f.invoke(ts, count, results);
        List<VkQueueFamilyProperties> rl = new ArrayList<>();
        for(int i=0;i<count[0];i++) {
            rl.add(results.get(i));
        }
        return rl;
    }

    /*270*/ /*VkResult*/public static int create_debug_utils_messenger(VkInstance instance,
                                          VkDebugUtilsMessengerCallbackEXT debug_callback,
                                          /*VkDebugUtilsMessageSeverityFlagsEXT*/int severity,
                                          /*VkDebugUtilsMessageTypeFlagsEXT*/int type,
                                          /*VkDebugUtilsMessengerEXT*/final long[] pDebugMessenger,
                                          VkAllocationCallbacks allocation_callbacks) {

        if (debug_callback == null) debug_callback = default_debug_callback;
        final VkDebugUtilsMessengerCreateInfoEXT messengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.create();
        messengerCreateInfo.sType( VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
        messengerCreateInfo.pNext( 0);
        messengerCreateInfo.messageSeverity( severity);
        messengerCreateInfo.messageType( type);
        messengerCreateInfo.pfnUserCallback( debug_callback);

        VkbVulkanFunctions.PFN_vkCreateDebugUtilsMessengerEXT createMessengerFunc;
        //vulkan_functions().get_inst_proc_addr(createMessengerFunc, "vkCreateDebugUtilsMessengerEXT");
        createMessengerFunc = new VkbVulkanFunctions.PFN_vkCreateDebugUtilsMessengerEXT() {
            public int invoke(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT pCreateInfo, VkAllocationCallbacks pAllocator, final long[] pMessenger) {
                return EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance,pCreateInfo,pAllocator,pMessenger);
            }
        };

        if (createMessengerFunc != null) {
            return createMessengerFunc.invoke(instance, messengerCreateInfo, allocation_callbacks, pDebugMessenger);
        } else {
            return VK_ERROR_EXTENSION_NOT_PRESENT;
        }
    }

    public static void destroy_debug_utils_messenger(VkInstance instance, /*VkDebugUtilsMessengerEXT*/long debugMessenger) {
        destroy_debug_utils_messenger(instance,debugMessenger,null);
    }
    /*295*/ public static void destroy_debug_utils_messenger(
            VkInstance instance, /*VkDebugUtilsMessengerEXT*/long debugMessenger, VkAllocationCallbacks allocation_callbacks) {

        VkbVulkanFunctions.PFN_vkDestroyDebugUtilsMessengerEXT deleteMessengerFunc;
        //vulkan_functions().get_inst_proc_addr(deleteMessengerFunc, "vkDestroyDebugUtilsMessengerEXT");
        deleteMessengerFunc = new VkbVulkanFunctions.PFN_vkDestroyDebugUtilsMessengerEXT() {
            @Override
            public void invoke(VkInstance instance, long messenger, VkAllocationCallbacks pAllocator) {
                EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance,messenger,pAllocator);
            }
        };

        if (deleteMessengerFunc != null) {
            deleteMessengerFunc.invoke(instance, debugMessenger, allocation_callbacks);
        }
    }

    /*306*/ public static final VkDebugUtilsMessengerCallbackEXT default_debug_callback = VkDebugUtilsMessengerCallbackEXT.create(new VkDebugUtilsMessengerCallbackEXTI() {
        @Override
        public int invoke(int messageSeverity, int messageType, long pCallbackData, long voide) {
            var ms = to_string_message_severity(messageSeverity);
            var mt = to_string_message_type(messageType);
            VkDebugUtilsMessengerCallbackDataEXT cbd = VkDebugUtilsMessengerCallbackDataEXT.createSafe(pCallbackData);
            String msg = "";
            if(null != cbd) {
                msg = cbd.pMessageString();
            }
            System.out.println("["+ms+": "+mt+"]\n"+msg+"\n");

            return VK_FALSE;
        }
    });

    /*318*/ public static boolean check_layer_supported(final List<VkLayerProperties> available_layers, String layer_name) {
        if (null==layer_name) return false;
        for (var layer_properties : available_layers) {
            if (Objects.equals(layer_name, layer_properties.layerNameString())) {
                return true;
            }
        }
        return false;
    }

    /*328*/ public static boolean check_layers_supported(List<VkLayerProperties> available_layers,
                                List<String> layer_names) {
        boolean all_found = true;
        for (var layer_name : layer_names) {
            boolean found = check_layer_supported(available_layers, layer_name);
            if (!found) all_found = false;
        }
        return all_found;
    }

    /*338*/ public static boolean check_extension_supported(
            final List<VkExtensionProperties> available_extensions, String extension_name) {
        if (extension_name == null) return false;
        for (var extension_properties : available_extensions) {
            if (Objects.equals(extension_name, extension_properties.extensionNameString())) {
                return true;
            }
        }
        return false;
    }

    /*349*/ public static boolean check_extensions_supported(final List<VkExtensionProperties> available_extensions,
                                    final List<String> extension_names) {
        boolean all_found = true;
        for (var extension_name : extension_names) {
            boolean found = check_extension_supported(available_extensions, extension_name);
            if (!found) all_found = false;
        }
        return all_found;
    }

    //template <typename T>
    /*360 */public static <T extends VkInstanceCreateInfo> void setup_pNext_chain(T structure, final List<VkBaseOutStructure> structs) {
        structure.pNext(0);
        if (structs.size() <= 0) return;
        for (int i = 0; i < structs.size() - 1; i++) {
            structs.get(i).pNext(structs.get(i + 1));
        }
        structure.pNext(structs.get(0).address());
    }

    //template <typename T>
    /*360 */public static <T extends VkDeviceCreateInfo> void setup_pNext_chain(T structure, final List<VkBaseOutStructure> structs) {
        structure.pNext(0);
        if (structs.size() <= 0) return;
        for (int i = 0; i < structs.size() - 1; i++) {
            structs.get(i).pNext(structs.get(i + 1));
        }
        structure.pNext(structs.get(0).address());
    }

    //template <typename T>
    /*360*/ public static <T extends VkSwapchainCreateInfoKHR> void setup_pNext_chain(T structure, final List<VkBaseOutStructure> structs) {
        structure.pNext(0);
        if (structs.size() <= 0) return;
        for (int i = 0; i < structs.size() - 1; i++) {
            structs.get(i).pNext(structs.get(i + 1));
        }
        structure.pNext(structs.get(0).address());
    }

    /*368*/ public static final String validation_layer_name = "VK_LAYER_KHRONOS_validation";

    static final InstanceErrorCategory instance_error_category = new InstanceErrorCategory();

    /*408*/ public static error_code make_error_code(VkbInstanceError instance_error) {
        return new error_code( instance_error.ordinal(), instance_error_category );
    }

    /*424*/ public static String to_string(VkbInstanceError err) {
        switch (err) {
            case vulkan_unavailable:
                return "vulkan_unavailable";
            case vulkan_version_unavailable:
                return "vulkan_version_unavailable";
            case vulkan_version_1_1_unavailable:
                return "vulkan_version_1_1_unavailable";
            case vulkan_version_1_2_unavailable:
                return "vulkan_version_1_2_unavailable";
            case failed_create_debug_messenger:
                return "failed_create_debug_messenger";
            case failed_create_instance:
                return "failed_create_instance";
            case requested_layers_not_present:
                return "requested_layers_not_present";
            case requested_extensions_not_present:
                return "requested_extensions_not_present";
            case windowing_extensions_not_present:
                return "windowing_extensions_not_present";
            default:
                return "";
        }
    }

    // Sentinel value, used in implementation only
    public static final int QUEUE_INDEX_MAX_VALUE = 65536;

    /*535*/ public static void destroy_surface(VkbInstance instance, /*VkSurfaceKHR*/long surface) {
        if (instance.instance[0] != /*VK_NULL_HANDLE*/null && surface != VK_NULL_HANDLE) {
            vulkan_functions().fp_vkDestroySurfaceKHR.invoke(instance.instance[0], surface, instance.allocation_callbacks);
        }
    }
    public static void destroy_surface(VkInstance instance,long surface) {
        destroy_surface(instance,surface,null);
    }
    /*540*/ public static void destroy_surface(VkInstance instance, /*VkSurfaceKHR*/long surface, VkAllocationCallbacks callbacks) {
        if (instance != /*VK_NULL_HANDLE*/null && surface != VK_NULL_HANDLE) {
            vulkan_functions().fp_vkDestroySurfaceKHR.invoke(instance, surface, callbacks);
        }
    }
    /*560*/ public static void destroy_instance(VkbInstance instance) {
        if (instance.instance[0] != /*VK_NULL_HANDLE*/null) {
            if (instance.debug_messenger[0] != VK_NULL_HANDLE)
                destroy_debug_utils_messenger(instance.instance[0], instance.debug_messenger[0], instance.allocation_callbacks);
            vulkan_functions().fp_vkDestroyInstance.invoke(instance.instance[0], instance.allocation_callbacks);
        }
    }

    /*842*/
    static List<String> check_device_extension_support(
            VkPhysicalDevice device, List<String> desired_extensions) {
        final List<VkExtensionProperties> available_extensions = new ArrayList<>();
        var available_extensions_ret = get_vector/*<VkExtensionProperties>*/(
        available_extensions, vulkan_functions().fp_vkEnumerateDeviceExtensionProperties, device, null);
        if (available_extensions_ret != VK_SUCCESS) return new ArrayList<>();

        final List<String> extensions_to_enable = new ArrayList<>();
        for (var extension : available_extensions) {
            for (var req_ext : desired_extensions) {
                if (Objects.equals(req_ext, extension.extensionNameString())) {
                    extensions_to_enable.add(req_ext);
                    break;
                }
            }
        }
        return extensions_to_enable;
    }

    // clang-format off
    /*862*/ public static boolean supports_features(VkPhysicalDeviceFeatures supported,
                           VkPhysicalDeviceFeatures requested,
                           final List<VkbGenericFeaturesPNextNode> extension_supported,
                           final List<VkbGenericFeaturesPNextNode> extension_requested) {

        if (requested.robustBufferAccess() && !supported.robustBufferAccess()) return false;
        if (requested.fullDrawIndexUint32() && !supported.fullDrawIndexUint32()) return false;
        if (requested.imageCubeArray() && !supported.imageCubeArray()) return false;
        if (requested.independentBlend() && !supported.independentBlend()) return false;
        if (requested.geometryShader() && !supported.geometryShader()) return false;
        if (requested.tessellationShader() && !supported.tessellationShader()) return false;
        if (requested.sampleRateShading() && !supported.sampleRateShading()) return false;
        if (requested.dualSrcBlend() && !supported.dualSrcBlend()) return false;
        if (requested.logicOp() && !supported.logicOp()) return false;
        if (requested.multiDrawIndirect() && !supported.multiDrawIndirect()) return false;
        if (requested.drawIndirectFirstInstance() && !supported.drawIndirectFirstInstance()) return false;
        if (requested.depthClamp() && !supported.depthClamp()) return false;
        if (requested.depthBiasClamp() && !supported.depthBiasClamp()) return false;
        if (requested.fillModeNonSolid() && !supported.fillModeNonSolid()) return false;
        if (requested.depthBounds() && !supported.depthBounds()) return false;
        if (requested.wideLines() && !supported.wideLines()) return false;
        if (requested.largePoints() && !supported.largePoints()) return false;
        if (requested.alphaToOne() && !supported.alphaToOne()) return false;
        if (requested.multiViewport() && !supported.multiViewport()) return false;
        if (requested.samplerAnisotropy() && !supported.samplerAnisotropy()) return false;
        if (requested.textureCompressionETC2() && !supported.textureCompressionETC2()) return false;
        if (requested.textureCompressionASTC_LDR() && !supported.textureCompressionASTC_LDR()) return false;
        if (requested.textureCompressionBC() && !supported.textureCompressionBC()) return false;
        if (requested.occlusionQueryPrecise() && !supported.occlusionQueryPrecise()) return false;
        if (requested.pipelineStatisticsQuery() && !supported.pipelineStatisticsQuery()) return false;
        if (requested.vertexPipelineStoresAndAtomics() && !supported.vertexPipelineStoresAndAtomics()) return false;
        if (requested.fragmentStoresAndAtomics() && !supported.fragmentStoresAndAtomics()) return false;
        if (requested.shaderTessellationAndGeometryPointSize() && !supported.shaderTessellationAndGeometryPointSize()) return false;
        if (requested.shaderImageGatherExtended() && !supported.shaderImageGatherExtended()) return false;
        if (requested.shaderStorageImageExtendedFormats() && !supported.shaderStorageImageExtendedFormats()) return false;
        if (requested.shaderStorageImageMultisample() && !supported.shaderStorageImageMultisample()) return false;
        if (requested.shaderStorageImageReadWithoutFormat() && !supported.shaderStorageImageReadWithoutFormat()) return false;
        if (requested.shaderStorageImageWriteWithoutFormat() && !supported.shaderStorageImageWriteWithoutFormat()) return false;
        if (requested.shaderUniformBufferArrayDynamicIndexing() && !supported.shaderUniformBufferArrayDynamicIndexing()) return false;
        if (requested.shaderSampledImageArrayDynamicIndexing() && !supported.shaderSampledImageArrayDynamicIndexing()) return false;
        if (requested.shaderStorageBufferArrayDynamicIndexing() && !supported.shaderStorageBufferArrayDynamicIndexing()) return false;
        if (requested.shaderStorageImageArrayDynamicIndexing() && !supported.shaderStorageImageArrayDynamicIndexing()) return false;
        if (requested.shaderClipDistance() && !supported.shaderClipDistance()) return false;
        if (requested.shaderCullDistance() && !supported.shaderCullDistance()) return false;
        if (requested.shaderFloat64() && !supported.shaderFloat64()) return false;
        if (requested.shaderInt64() && !supported.shaderInt64()) return false;
        if (requested.shaderInt16() && !supported.shaderInt16()) return false;
        if (requested.shaderResourceResidency() && !supported.shaderResourceResidency()) return false;
        if (requested.shaderResourceMinLod() && !supported.shaderResourceMinLod()) return false;
        if (requested.sparseBinding() && !supported.sparseBinding()) return false;
        if (requested.sparseResidencyBuffer() && !supported.sparseResidencyBuffer()) return false;
        if (requested.sparseResidencyImage2D() && !supported.sparseResidencyImage2D()) return false;
        if (requested.sparseResidencyImage3D() && !supported.sparseResidencyImage3D()) return false;
        if (requested.sparseResidency2Samples() && !supported.sparseResidency2Samples()) return false;
        if (requested.sparseResidency4Samples() && !supported.sparseResidency4Samples()) return false;
        if (requested.sparseResidency8Samples() && !supported.sparseResidency8Samples()) return false;
        if (requested.sparseResidency16Samples() && !supported.sparseResidency16Samples()) return false;
        if (requested.sparseResidencyAliased() && !supported.sparseResidencyAliased()) return false;
        if (requested.variableMultisampleRate() && !supported.variableMultisampleRate()) return false;
        if (requested.inheritedQueries() && !supported.inheritedQueries()) return false;

        for(int i = 0; i < extension_requested.size(); ++i) {
            var res = VkbGenericFeaturesPNextNode.match(extension_requested.get(i), extension_supported.get(i));
            if(!res) return false;
        }

        return true;
    }
    // Finds the first queue which supports the desired operations. Returns QUEUE_INDEX_MAX_VALUE if none is found
    /*931*/ public static int get_first_queue_index(final List<VkQueueFamilyProperties> families, /*VkQueueFlags*/int desired_flags) {
        for (int i = 0; i < (int)(families.size()); i++) {
            if ((families.get(i).queueFlags() & desired_flags) != 0) return i;
        }
        return QUEUE_INDEX_MAX_VALUE;
    }

    // Finds the queue which is separate from the graphics queue and has the desired flag and not the
    // undesired flag, but will select it if no better options are available compute support. Returns
    // QUEUE_INDEX_MAX_VALUE if none is found.
    /*940*/ public static int get_separate_queue_index(List<VkQueueFamilyProperties> families,
                                      /*VkQueueFlags*/int desired_flags,
                                      /*VkQueueFlags*/int undesired_flags) {
        int index = QUEUE_INDEX_MAX_VALUE;
        for (int i = 0; i < (int)(families.size()); i++) {
            if ((families.get(i).queueFlags() & desired_flags)!=0 && ((families.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) == 0)) {
                if ((families.get(i).queueFlags() & undesired_flags) == 0) {
                    return i;
                } else {
                    index = i;
                }
            }
        }
        return index;
    }

    // finds the first queue which supports only the desired flag (not graphics or transfer). Returns QUEUE_INDEX_MAX_VALUE if none is found.
    /*957*/
    static int get_dedicated_queue_index(List<VkQueueFamilyProperties> families,
            /*VkQueueFlags*/int desired_flags,
            /*VkQueueFlags*/int undesired_flags) {
        for (int i = 0; i < (int)(families.size()); i++) {
            if ((families.get(i).queueFlags() & desired_flags)!=0 && (families.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) == 0 &&
                    (families.get(i).queueFlags() & undesired_flags) == 0)
                return i;
        }
        return QUEUE_INDEX_MAX_VALUE;
    }

    // finds the first queue which supports presenting. returns QUEUE_INDEX_MAX_VALUE if none is found
    /*969*/ public static int get_present_queue_index(VkPhysicalDevice phys_device,
                                     /*VkSurfaceKHR*/long surface,
                                     final List<VkQueueFamilyProperties> families) {
        for (int i = 0; i < (int)(families.size()); i++) {
            /*VkBool32*/final int[] presentSupport = new int[1];
            if (surface != VK_NULL_HANDLE) {
                int res = vulkan_functions().fp_vkGetPhysicalDeviceSurfaceSupportKHR.invoke(
                        phys_device, i, surface, presentSupport);
                if (res != VK_SUCCESS)
                    return QUEUE_INDEX_MAX_VALUE; // TODO: determine if this should fail another way
            }
            if (presentSupport[0] == VK_TRUE) return i;
        }
        return QUEUE_INDEX_MAX_VALUE;
    }

    /*1357*/ public static VkQueue get_queue(VkDevice device, int family) {
        final VkQueue[] out_queue = new VkQueue[1];
        vulkan_functions().fp_vkGetDeviceQueue.invoke(device, family, 0, out_queue);
        return out_queue[0];
    }

    /*1381*/ public static void destroy_device(VkbDevice device) {
        vulkan_functions().fp_vkDestroyDevice.invoke(device.device[0], device.allocation_callbacks[0]);
    }

    /*1521*/ static final SurfaceSupportErrorCategory surface_support_error_category = new SurfaceSupportErrorCategory();

    /*1523*/ public static error_code make_error_code(VkbSurfaceSupportError surface_support_error) {
        return new error_code( (int)(surface_support_error.ordinal()), surface_support_error_category );
    }

    /*1527*/
    static Result<VkbSurfaceSupportDetails> query_surface_support_details(VkPhysicalDevice phys_device, /*VkSurfaceKHR*/long surface) {
        if (surface == VK_NULL_HANDLE) return new Result(make_error_code(VkbSurfaceSupportError.surface_handle_null));

        final VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.create();
        /*VkResult*/int res = vulkan_functions().fp_vkGetPhysicalDeviceSurfaceCapabilitiesKHR.invoke(
                phys_device, surface, capabilities);
        if (res != VK_SUCCESS) {
            return new Result( make_error_code(VkbSurfaceSupportError.failed_get_surface_capabilities), res );
        }

        final List<VkSurfaceFormatKHR> formats = new ArrayList<>();
        final List</*VkPresentModeKHR*/Integer> present_modes = new ArrayList<>();

        var formats_ret = get_vector/*<VkSurfaceFormatKHR>*/(
        formats, vulkan_functions().fp_vkGetPhysicalDeviceSurfaceFormatsKHR, phys_device, surface);
        if (formats_ret != VK_SUCCESS)
            return new Result( make_error_code(VkbSurfaceSupportError.failed_enumerate_surface_formats), formats_ret );
        var present_modes_ret = get_vector/*<VkPresentModeKHR>*/(
        present_modes, vulkan_functions().fp_vkGetPhysicalDeviceSurfacePresentModesKHR, phys_device, surface);
        if (present_modes_ret != VK_SUCCESS)
            return new Result( make_error_code(VkbSurfaceSupportError.failed_enumerate_present_modes), present_modes_ret );

        return new Result(new VkbSurfaceSupportDetails( capabilities, formats, present_modes ));
    }

    /*1552*/
    static VkSurfaceFormatKHR find_surface_format(VkPhysicalDevice phys_device,
                                                  final List<VkSurfaceFormatKHR> available_formats,
                                                  final List<VkSurfaceFormatKHR> desired_formats,
            /*VkFormatFeatureFlags*/int feature_flags) {
        for (var desired_format : desired_formats) {
            for (var available_format : available_formats) {
                // finds the first format that is desired and available
                if (desired_format.format() == available_format.format() &&
                        desired_format.colorSpace() == available_format.colorSpace()) {
                    final VkFormatProperties properties = VkFormatProperties.create();
                    vulkan_functions().fp_vkGetPhysicalDeviceFormatProperties.invoke(
                            phys_device, desired_format.format(), properties);
                    if ((properties.optimalTilingFeatures() & feature_flags) == feature_flags)
                        return desired_format;
                }
            }
        }

        // use the first available one if any desired formats aren't found
        return available_formats.get(0);
    }

    /*1574*/ public static /*VkPresentModeKHR*/int find_present_mode(final List</*VkPresentModeKHR*/Integer> available_resent_modes,
                                       final List</*VkPresentModeKHR*/Integer> desired_present_modes) {
        for (var desired_pm : desired_present_modes) {
            for (var available_pm : available_resent_modes) {
                // finds the first present mode that is desired and available
                if (Objects.equals(desired_pm , available_pm)) return desired_pm;
            }
        }
        // only present mode required, use as a fallback
        return VK_PRESENT_MODE_FIFO_KHR;
    }

    /*1589*/
    static VkExtent2D find_extent(VkSurfaceCapabilitiesKHR capabilities, int desired_width, int desired_height) {
        if (capabilities.currentExtent().width() != Port.UINT32_MAX) {
            return capabilities.currentExtent();
        } else {
            VkExtent2D actualExtent = VkExtent2D.create();//{ desired_width, desired_height };
            actualExtent.width(desired_width);
            actualExtent.height(desired_height);

            actualExtent.width( Math.max(capabilities.minImageExtent().width(),
                    Math.min(capabilities.maxImageExtent().width(), actualExtent.width())));
            actualExtent.height( Math.max(capabilities.minImageExtent().height(),
                    Math.min(capabilities.maxImageExtent().height(), actualExtent.height())));

            return actualExtent;
        }
    }

    /*1605*/ public static void destroy_swapchain(VkbSwapchain swapchain) {
        if (swapchain.device != null/*VK_NULL_HANDLE*/ && swapchain.swapchain[0] != VK_NULL_HANDLE) {
            vulkan_functions().fp_vkDestroySwapchainKHR.invoke(
                    swapchain.device, swapchain.swapchain[0], swapchain.allocation_callbacks);
        }
    }
}
