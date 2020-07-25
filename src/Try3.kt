import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL33.*
import org.lwjgl.BufferUtils
import java.io.File
import java.lang.RuntimeException
import java.nio.FloatBuffer

object HelloWorld3{
    @JvmStatic
    fun main(args: Array<String>) {


        //Init stuff
        if (!glfwInit()) {
            System.err.println("Can't initialize glfw!")
            return
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        //glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        val window = glfwCreateWindow(800,600, "LearnSHit", 0,0)
        if(window == 0L){
            println("Failed to create GLFW window.")
            glfwTerminate()
            return
        }
        glfwMakeContextCurrent(window)
        GL.createCapabilities()

//        causes an error, look into how to handle this with GL3.3
//        Play around with this, i can set the output space of the screen.
//        GL33.glViewport(0,0,800,600)
//
//        val resizeWindow: GLFWFramebufferSizeCallback = object : GLFWFramebufferSizeCallback() {
//            override fun invoke(window: Long, width: Int, height: Int) {
//                GL33.glViewport(0, 0, width, height)
//                //update any other window vars you might have (aspect ratio, MVP matrices, etc)
//            }
//        }
//        glfwSetFramebufferSizeCallback(window, resizeWindow)

        val verticies = floatArrayOf(-0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.0f, 0.5f, 0.0f)
        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, bufferFromFloatArray(verticies), GL_STATIC_DRAW)

        loadShaders()

        while(!glfwWindowShouldClose(window)){
            processInput(window)

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            glfwSwapBuffers(window)
            glfwPollEvents()
        }

        glfwTerminate()
        return
    }

    fun processInput(window: Long){
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS){
            glfwSetWindowShouldClose(window, true)
        }
    }

    fun bufferFromFloatArray(data:FloatArray):FloatBuffer{
        val buffer = BufferUtils.createFloatBuffer(data.size)
        buffer.put(data)
        buffer.rewind()
        return buffer
    }

    // shader creation util functions.
    fun createShader(shaderSource: String, shaderType:Int):Int{
        val shader = glCreateShader(shaderType)
        GL20.glShaderSource(shader, shaderSource)
        glCompileShader(shader)
        val success = glGetShaderi(shader, GL_COMPILE_STATUS)
        if(success == GL_FALSE) throw RuntimeException(glGetShaderInfoLog(shader))
        return shader
    }

    fun createShaderProgram(vertexShader: Int, fragmentShader: Int): Int{
        val shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glLinkProgram(shaderProgram)

        val success = glGetProgrami(shaderProgram, GL_LINK_STATUS)
        if(success == GL_FALSE) throw RuntimeException(glGetProgramInfoLog(shaderProgram))
        return shaderProgram
    }

    fun loadShaders(){
        val vertexShaderSource = File("shaders/shader.vert").readText()
        val fragmentshaderSource = File("shaders/shader.frag").readText()
        val vertexShader = createShader(vertexShaderSource, GL_VERTEX_SHADER)
        val fragmentShader = createShader(fragmentshaderSource, GL_FRAGMENT_SHADER)

        val shaderProgram = createShaderProgram(vertexShader, fragmentShader)
        glUseProgram(shaderProgram)
    }
}
