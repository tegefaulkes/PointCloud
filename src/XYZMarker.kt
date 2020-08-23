import org.lwjgl.BufferUtils

//TODO complete this object to draw the standard XYZ origin marker.
object XYZMarker {
    val verticies = BufferUtils.createFloatBuffer(12).put(floatArrayOf(
            0f, 0f, 0f,
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
    ))
    lateinit var shaderProgram: Shader


    fun setShader(shader: Shader) {
        this.shaderProgram = shader
    }
}