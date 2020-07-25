
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

object HelloWorld2 {
    @JvmStatic
    fun main(args: Array<String>) {
        var window: Long
        val vao: Int
        val vbo: Int
        val points = floatArrayOf(
            -1f, -1f, 0.0f,
            1f, -1f, 0.0f,
            1f, 1f, 0.0f,
            -1f, -1f, 0f,
            -1f, 1f, 0f,
            1f, 1f, 0f
        )
        val vertex_shader = """
            #version 410
            in vec3 vp;
            
            void main () {
                gl_Position = vec4 (vp, 1.0);
            }
            """.trimIndent()
        val fragment_shader = """
            #version 410
            
            in vec4 gl_FragCoord;
            
            out vec4 frag_colour;
            
            void main () {
                frag_colour = vec4 (0.0, gl_FragCoord.x, gl_FragCoord.y, 1.0);
            }
            
            """.trimIndent()
        val vs: Int
        val fs: Int
        val shader_programme: Int
        if (!GLFW.glfwInit()) {
            System.err.println("Can't initialize glfw!")
            return
        }
        window = GLFW.glfwCreateWindow(640, 480, "Hello Triangle", MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == 0L) {
            GLFW.glfwTerminate()
            return
        }
        GLFW.glfwMakeContextCurrent(window)
        GL.createCapabilities()
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_LESS)
        vbo = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        val buffer = BufferUtils.createFloatBuffer(points.size)
        buffer.put(points)
        buffer.rewind()
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW)
        vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)
        GL20.glEnableVertexAttribArray(0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)
        vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vs, vertex_shader)
        GL20.glCompileShader(vs)
        fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fs, fragment_shader)
        GL20.glCompileShader(fs)
        shader_programme = GL20.glCreateProgram()
        GL20.glAttachShader(shader_programme, fs)
        GL20.glAttachShader(shader_programme, vs)
        GL20.glLinkProgram(shader_programme)
        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
            GL20.glUseProgram(shader_programme)
            GL30.glBindVertexArray(vao)
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6)
            GLFW.glfwPollEvents()
            GLFW.glfwSwapBuffers(window)
        }
        GLFW.glfwTerminate()
        return
    }
}