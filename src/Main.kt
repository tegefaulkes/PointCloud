import org.joml.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFWFramebufferSizeCallback
import kotlin.math.*
import java.lang.RuntimeException
import java.nio.*
import org.lwjgl.stb.STBImage.*


//TODO complete point shader to change point size bassed on distance and color on height.

object Main{

    @JvmStatic
    fun main(args: Array<String>) {

        val aPos = 0
        var aspect = 1f;

        //Init stuff
        if (!glfwInit()) {
            System.err.println("Can't initialize glfw!")
            return
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        //glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE); // If MAC

        val window = glfwCreateWindow(800,800, "LearnSHit", 0,0)
        if(window == 0L){
            println("Failed to create GLFW window.")
            glfwTerminate()
            return
        }
        glfwMakeContextCurrent(window)
        GL.createCapabilities()


        glViewport(0,0,800,800)

        val resizeWindow: GLFWFramebufferSizeCallback = object : GLFWFramebufferSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                glViewport(0, 0, width, height)
                aspect = width.toFloat()/height.toFloat()
                //update any other window vars you might have (aspect ratio, MVP matrices, etc)
            }
        }
        glfwSetFramebufferSizeCallback(window, resizeWindow)

        //val baseShader = Shader("shaders/shader.vert", "shaders/shader.frag")
        val baseShader = Shader("shaders/point")

        val cloud = CloudLoader("data/Random city scape/raw_2909008nw.csv", baseShader)
        cloud.printBounds()
        Camera.init(window)
//        Camera.setPosVec(cloud.mid())

//        val vao = glGenVertexArrays()
//        glBindVertexArray(vao)


//        val vbo = glGenBuffers()
//        glBindBuffer(GL_ARRAY_BUFFER, vbo)
//        glBufferData(GL_ARRAY_BUFFER, cloud.getData(), GL_STATIC_DRAW)


        baseShader.use()

        val fb = BufferUtils.createFloatBuffer(16)
        val modelMat = Matrix4f()
        val projectionMat = Matrix4f().perspective(3.14f/4f, aspect, 0.1f, 300f)

        val floatSize = 4
        glVertexAttribPointer(aPos, 3, GL_FLOAT, false, 3 * floatSize, 0)
        glEnableVertexAttribArray(aPos)

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_VERTEX_PROGRAM_POINT_SIZE)
        glEnable(GL_BLEND)
        glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)




        while(!glfwWindowShouldClose(window)){
            val timeValue = glfwGetTime().toFloat()

            processInput(window)
            Camera.updateInputs()

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            baseShader.use()

            Camera.getViewMat(fb)
            baseShader.set("view", Camera.getViewMat())

            projectionMat.identity().perspective(3.14f/4f, aspect, 0.1f, 10000f)
            baseShader.set("projection", projectionMat)

//            modelMat.identity()
////                    .translate(cloud.mid())
////                    .scale(0.1f)
////                    .translate(cloud.mid().mul(-1f))
//            baseShader.set("model", modelMat)

            val greenValue = sin(timeValue) / 2.0f + 0.5f
            val vertexColorLocation = baseShader.getUniformLocation("ourColor")
            glUniform4f(vertexColorLocation, 0.0f, greenValue, 0.0f, 1.0f)

            cloud.draw()

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
