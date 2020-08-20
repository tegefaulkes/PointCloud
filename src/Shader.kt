import com.sun.jdi.InvalidTypeException
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL33.*
import java.io.File


// "shaders/shader.vert"
// "shaders/shader.frag"

class Shader(vertexPath: String, fragmentPath: String) {
    private val vertexShaderSource = File(vertexPath).readText()
    private val fragmentShaderSource = File(fragmentPath).readText()
    private val vertexShader = createShader(vertexShaderSource, GL_VERTEX_SHADER)
    private val fragmentShader = createShader(fragmentShaderSource, GL_FRAGMENT_SHADER)
    val id = createShaderProgram(vertexShader, fragmentShader)
    val tempMat4FB = BufferUtils.createFloatBuffer(16)

    fun use() = glUseProgram(id)

    fun <T> set (name :String, value: T) {
        when (value){
            is Boolean  -> glUniform1i(glGetUniformLocation(id, name), if (value) 1 else 0)
            is Int      -> glUniform1i(glGetUniformLocation(id, name), value)
            is Float    -> glUniform1f(glGetUniformLocation(id, name), value)
            is Matrix4f -> {
                value.get(tempMat4FB)
                glUniformMatrix4fv(glGetUniformLocation(id, name), false, tempMat4FB)
            }
            else -> throw InvalidTypeException("unsupported type")
        }
    }

    fun getUniformLocation(name: String): Int {
        return glGetUniformLocation(id, name)
    }

    private fun createShaderProgram(vertexShader: Int, fragmentShader: Int): Int {
        val shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glLinkProgram(shaderProgram)

        val success = glGetProgrami(shaderProgram, GL_LINK_STATUS)
        if(success == GL_FALSE) throw RuntimeException(glGetProgramInfoLog(shaderProgram))
        return shaderProgram
    }

    private fun createShader(shaderSource: String, shaderType: Int): Int {
        val shader = glCreateShader(shaderType)
        glShaderSource(shader, shaderSource)
        glCompileShader(shader)
        val success = glGetShaderi(shader, GL_COMPILE_STATUS)
        if(success == GL_FALSE) throw RuntimeException(glGetShaderInfoLog(shader))
        return shader
    }

}