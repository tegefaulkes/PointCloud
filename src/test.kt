import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class HelloWorld {
    // The window handle
    private var window: Long = 0

    fun floatArrayToBuffer(array: Array<Float>, buffer:FloatBuffer):FloatBuffer {
        for(number in array) buffer.put(number)
        buffer.flip()
        return buffer
    }

    fun orthographic(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float):Array<Float>{
        val tx = -(right + left) / (right - left)
        val ty = -(top + bottom) / (top - bottom)
        val tz = -(far + near) / (far - near)
        val m00 = 2f / (right - left)
        val m11 = 2f / (top - bottom)
        val m22 = -2f / (far - near)


        //return arrayOf(m00, 0f, 0f, tx, 0f, m11, 0f, ty, 0f, 0f, m22, tz)
        return arrayOf(m00, 0f, 0f, 0f, 0f, m11, 0f, 0f, 0f, 0f, m22, 0f, tx, ty, tz, 1f)
    }

    fun identity() = arrayOf(1F,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f)

    fun run() {
        println("Hello LWJGL " + Version.getVersion() + "!")
        init()
        loop()

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)!!.free()
    }

    private fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
//        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
//        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
//        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
//        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE) // the window will be resizable

        // Create the window
        window = GLFW.glfwCreateWindow(540, 380, "Hello World!", MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) throw RuntimeException("Failed to create the GLFW window")

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        GLFW.glfwSetKeyCallback(
            window
        ) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) GLFW.glfwSetWindowShouldClose(
                window,
                true
            ) // We will detect this in the rendering loop
        }

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

            // Center the window
            GLFW.glfwSetWindowPos(
                window,
                (vidmode!!.width() - pWidth[0]) / 2,
                (vidmode.height() - pHeight[0]) / 2
            )
        }



        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window)
        GLFW.glfwInit()
        // Enable v-sync
        GLFW.glfwSwapInterval(1)



        // Make the window visible
        GLFW.glfwShowWindow(window)
        GL.createCapabilities()

        val vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)

        val stack:MemoryStack = MemoryStack.stackPush()
        val vertices: FloatBuffer = stack.mallocFloat(3*6)
        vertices.put(-0.6f).put(-0.4f).put(0f).put(1f).put(0f).put(0f)
        vertices.put(0.6f).put(-0.4f).put(0f).put(0f).put(1f).put(0f)
        vertices.put(0f).put(0.6f).put(0f).put(0f).put(0f).put(1f)
        vertices.flip()

        val vbo = GL30.glGenBuffers()
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo)
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW)
        //MemoryStack.stackPop()


        val vertexSource = """
            #version 150 core

            in vec3 position;
            in vec3 color;

            out vec3 vertexColor;

            uniform mat4 model;
            uniform mat4 view;
            uniform mat4 projection;

            void main() {
                vertexColor = color;
                mat4 mvp = projection * view * model;
                gl_Position = vec4(position, 1.0);
            }
        """.trimIndent()
        val fragmentSource = """
            #version 150 core

            in vec3 vertexColor;

            out vec4 fragColor;

            void main() {
                fragColor = vec4(vertexColor, 1.0);
                //fragColor = vec4(1.0,0.0,0.0,1.0);
            }
        """.trimIndent()

        val vertexShader = GL30.glCreateShader(GL30.GL_VERTEX_SHADER)
        GL30.glShaderSource(vertexShader, vertexSource)
        GL30.glCompileShader(vertexShader)

        val fragementShader = GL30.glCreateShader(GL30.GL_FRAGMENT_SHADER)
        GL30.glShaderSource(fragementShader, fragmentSource)
        GL30.glCompileShader(fragementShader)

        val vertexStatus = GL30.glGetShaderi(vertexShader, GL30.GL_COMPILE_STATUS)
        val fragmentStatus = GL30.glGetShaderi(fragementShader, GL30.GL_COMPILE_STATUS)
        if(vertexStatus != GL30.GL_TRUE) throw RuntimeException(GL30.glGetShaderInfoLog(vertexShader))
        if(fragmentStatus != GL30.GL_TRUE) throw RuntimeException(GL30.glGetShaderInfoLog(fragementShader))


        val shaderProgram = GL30.glCreateProgram()
        GL30.glAttachShader(shaderProgram, vertexShader)
        GL30.glAttachShader(shaderProgram, fragementShader)
        GL30.glBindFragDataLocation(shaderProgram, 0, "fragColor")
        GL30.glLinkProgram(shaderProgram)
        
        val programStatus = GL30.glGetProgrami(shaderProgram, GL30.GL_LINK_STATUS)
        if(programStatus != GL30.GL_TRUE) throw RuntimeException(GL30.glGetProgramInfoLog(shaderProgram))
        GL30.glUseProgram(shaderProgram)

        val floatSize = 4
        val posAttrib = GL30.glGetAttribLocation(shaderProgram, "Position")
        GL30.glEnableVertexAttribArray(posAttrib)
        GL30.glVertexAttribPointer(posAttrib, 3, GL30.GL_FLOAT, false, 6 * floatSize, 0)

        val stride: Long = 3*4
        val colAttrib = GL30.glGetAttribLocation(shaderProgram, "color")
        GL30.glEnableVertexAttribArray(colAttrib)
        GL30.glVertexAttribPointer(colAttrib, 3, GL30.GL_FLOAT, false, 6 * floatSize, stride)

        val uniModel: Int = GL30.glGetUniformLocation(shaderProgram, "model")
        val model = floatArrayToBuffer(identity(), stack.mallocFloat(16))
        GL30.glUniformMatrix4fv(uniModel, false, model)

        val uniView: Int = GL30.glGetUniformLocation(shaderProgram, "view")
        val view = floatArrayToBuffer(identity(), stack.mallocFloat(16))
        GL30.glUniformMatrix4fv(uniView, false, view)

        val uniProjection: Int = GL30.glGetUniformLocation(shaderProgram, "projection")
        val ratio = 640f / 480f
        val projection = orthographic(-ratio, ratio, -1f, 1f, -1f, 1f)
        val ortho = floatArrayToBuffer(projection, stack.mallocFloat(16))
        GL30.glUniformMatrix4fv(uniProjection, false, ortho)



    }

    private fun loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        //GL.createCapabilities()

        // Set the clear color

        

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClearColor(0f, 0f, 1.0f, 1.0f)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer
            GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 3)
            GLFW.glfwSwapBuffers(window) // swap the color buffers


            // Poll for window events. The key callback above will only be
            // invoked during this call.
            GLFW.glfwPollEvents()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HelloWorld().run()
        }
    }
}