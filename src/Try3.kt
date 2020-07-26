import org.joml.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFWFramebufferSizeCallback
import java.io.File
import java.lang.Math.*
import java.lang.RuntimeException
import java.nio.*
import org.lwjgl.stb.STBImage.*



//TODO: Got up to page 83 in the PDF

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
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        //glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        val window = glfwCreateWindow(800,800, "LearnSHit", 0,0)
        if(window == 0L){
            println("Failed to create GLFW window.")
            glfwTerminate()
            return
        }
        glfwMakeContextCurrent(window)
        GL.createCapabilities()

//        causes an error, look into how to handle this with GL3.3
//        Play around with this, i can set the output space of the screen.
        glViewport(0,0,800,800)

        val resizeWindow: GLFWFramebufferSizeCallback = object : GLFWFramebufferSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                glViewport(0, 0, width, height)
                //update any other window vars you might have (aspect ratio, MVP matrices, etc)
            }
        }
        glfwSetFramebufferSizeCallback(window, resizeWindow)

        val shaderProgram = loadShaders()

        val vao = glGenVertexArrays()
        glBindVertexArray(vao)
        val verticies = floatArrayOf(
            0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f)


        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, bufferFromFloatArray(verticies), GL_STATIC_DRAW)

        val indices:IntArray = intArrayOf(0,1,3,1,2,3)
        val ebo = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, bufferFromIntArray(indices), GL_STATIC_DRAW)

        val texCoords = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.5f, 1.0f)


        glUseProgram(shaderProgram)
        val texture1 = loadTexture("textures/container.jpg", GL_RGB)
        val texture2 = loadTexture("textures/awesomeface.png", GL_RGBA)
        glUniform1i(glGetUniformLocation(shaderProgram, "texture1"), 0)
        glUniform1i(glGetUniformLocation(shaderProgram, "texture2"), 1)

        val fb = BufferUtils.createFloatBuffer(16)
        val mat = Matrix4f()

        val transformLoc = glGetUniformLocation(shaderProgram, "transform")


        val floatSize = 4
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * floatSize, 0)
        glEnableVertexAttribArray(0)

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * floatSize, 12)
        glEnableVertexAttribArray(1)

        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8*floatSize, 24)
        glEnableVertexAttribArray(2)



        while(!glfwWindowShouldClose(window)){
            processInput(window)

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            glUseProgram(shaderProgram)

            val timeValue = glfwGetTime()
            val greenValue = sin(timeValue) / 2.0f + 0.5f
            val vertexColorLocation = glGetUniformLocation(shaderProgram, "ourColor")
            glUniform4f(vertexColorLocation, 0.0f, greenValue.toFloat(), 0.0f, 1.0f)
            mat.identity()
                .rotate(timeValue.toFloat(), Vector3f(0.0f, 0.0f, 1.0f))
                .scale(0.5f)
                .get(fb)
            glUniformMatrix4fv(transformLoc, false, fb)

            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, texture1)
            glActiveTexture(GL_TEXTURE1)
            glBindTexture(GL_TEXTURE_2D, texture2)
            glBindVertexArray(vao)
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0)
            glBindVertexArray(0)

            glfwSwapBuffers(window)
            glfwPollEvents()
        }


        glfwTerminate()
        return
    }

    private fun processInput(window: Long){
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS){
            glfwSetWindowShouldClose(window, true)
        }
    }

    private fun bufferFromFloatArray(data:FloatArray):FloatBuffer{
        val buffer = BufferUtils.createFloatBuffer(data.size)
        buffer.put(data)
        buffer.rewind()
        return buffer
    }

    private fun bufferFromIntArray(data:IntArray):IntBuffer{
        val buffer = BufferUtils.createIntBuffer(data.size)
        buffer.put(data)
        buffer.rewind()
        return buffer
    }

    // shader creation util functions.
    private fun createShader(shaderSource: String, shaderType:Int):Int{
        val shader = glCreateShader(shaderType)
        glShaderSource(shader, shaderSource)
        glCompileShader(shader)
        val success = glGetShaderi(shader, GL_COMPILE_STATUS)
        if(success == GL_FALSE) throw RuntimeException(glGetShaderInfoLog(shader))
        return shader
    }

    private fun createShaderProgram(vertexShader: Int, fragmentShader: Int): Int{
        val shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glLinkProgram(shaderProgram)

        val success = glGetProgrami(shaderProgram, GL_LINK_STATUS)
        if(success == GL_FALSE) throw RuntimeException(glGetProgramInfoLog(shaderProgram))
        return shaderProgram
    }

    private fun loadShaders():Int{
        val vertexShaderSource = File("shaders/shader.vert").readText()
        val fragmentshaderSource = File("shaders/shader.frag").readText()
        val vertexShader = createShader(vertexShaderSource, GL_VERTEX_SHADER)
        val fragmentShader = createShader(fragmentshaderSource, GL_FRAGMENT_SHADER)
        return createShaderProgram(vertexShader, fragmentShader)
    }

    private fun loadTexture(path:String, type: Int):Int{
        val texture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        val width = BufferUtils.createIntBuffer(1)
        val height = BufferUtils.createIntBuffer(1)
        val nrChannels = BufferUtils.createIntBuffer(1)
        stbi_set_flip_vertically_on_load(true)
        val data = stbi_load(path, width, height, nrChannels, 0)
        if (data != null){
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(), height.get(), 0, type, GL_UNSIGNED_BYTE, data)
            glGenerateMipmap(GL_TEXTURE_2D)
        }else throw RuntimeException("Failed to load image form path: $path")
        stbi_image_free(data)
        return texture
    }
}
