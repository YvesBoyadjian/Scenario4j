package vkbootstrap;

import port.error_code;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class Result<T extends Object> {

    private T m_value;
    private Error m_error = new Error();
    private boolean m_init;

    public Result( T value) {
        m_value = value;
        m_init = true;
    }

    public Result(error_code error_code) {
        this(error_code,VK_SUCCESS);
    }
    public Result(error_code error_code, /*VkResult*/int result) {
	    m_error = new Error(error_code,result);
        m_init = false;
    }

    public T        value ()          { assert (m_init); return m_value; }

    // std::error_code associated with the error
    public error_code error() { assert (!m_init); return m_error.type; }
    // optional VkResult that could of been produced due to the error
    public /*VkResult*/int vk_result() { assert (!m_init); return m_error.vk_result; }

    public boolean not() {
        return !m_init;
    }

    public boolean has_value() { return m_init; }
}
