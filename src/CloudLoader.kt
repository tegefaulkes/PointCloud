import org.joml.*
import org.lwjgl.BufferUtils
import java.io.File
import java.nio.FloatBuffer
import org.lwjgl.opengl.GL33.*
import org.lwjgl.stb.STBImage
import java.lang.RuntimeException


class CloudLoader(file: String, private val shader: Shader) {
    private var count = 0
    private val data: FloatBuffer
    private val vao: Int
    private val modelMat: Matrix4f

    // tracking out the scope of the cloud.
    private var xMin = 0f
    private var yMin = 0f
    private var zMin = 0f
    private var xMax = 0f
    private var yMax = 0f
    private var zMax = 0f



    init {
        File(file).forEachLine { count++ }
        println(count)
        data = BufferUtils.createFloatBuffer(count * 3)
        var first = true
        File(file).forEachLine {
            val line = it.split(",")
            val x = line[0].toFloat()
            val y = line[1].toFloat()
            val z = line[2].toFloat()

            if (first) {
                xMin = x
                xMax = x
                yMin = y
                yMax = y
                zMin = z
                zMax = z
                first = false
            }
            if (xMin > x) xMin = x
            if (xMax < x) xMax = x
            if (yMin > y) yMin = y
            if (yMax < y) yMax = y
            if (zMin > z) zMin = z
            if (zMax < z) zMax = z

            data.put(x)
                    .put(y)
                    .put(z)// * 0.3048f)

        }
        data.rewind()
        modelMat = Matrix4f()
                .identity()
                .translate(mid().mul(-1f))
                //.rotate(3.414f,1f,0f,0f)

        // Constructing the point cloud in memory.
        vao = glGenVertexArrays()
        glBindVertexArray(vao)
        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)

        //texture.
        val path = "textures/awesomeface.png"

        val texture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        val width = BufferUtils.createIntBuffer(1)
        val height = BufferUtils.createIntBuffer(1)
        val nrChannels = BufferUtils.createIntBuffer(1)
//        STBImage.stbi_set_flip_vertically_on_load(true)
        val data = STBImage.stbi_load(path, width, height, nrChannels, 0)
        if (data != null){
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, data)
            glGenerateMipmap(GL_TEXTURE_2D)
        }else throw RuntimeException("Failed to load image form path: $path")
        STBImage.stbi_image_free(data)

        shader.set("tex", 0)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture)

    }

    fun draw() {

        shader.use()

        // Setting uniforms.
        shader.set("minZ", -256f)
        shader.set("midZ", -51f)
        shader.set("maxZ", 152f)
        shader.set("model", modelMat)

        // draw
        glBindVertexArray(vao)
        glDrawArrays(GL_POINTS, 0, count)


    }

    fun getData() = data

    fun getNum() = count

    fun xMin() = xMin
    fun xMax() = xMax
    fun xMid() = (xMax + xMin) / 2f

    fun yMin() = yMin
    fun yMax() = yMax
    fun yMid() = (yMax + yMin) / 2f

    fun zMin() = zMin
    fun zMax() = zMax
    fun zMid() = (zMax + zMin) / 2f

    fun mid() = Vector3f(xMid(), yMid(), zMid())

    fun printBounds() {
        println("x [$xMin,  $xMax]")
        println("y [$yMin,  $yMax]")
        println("z [$zMin,  $zMax]")
        println("center [${xMid()}, ${zMid()}, ${zMid()}]")
    }

}

