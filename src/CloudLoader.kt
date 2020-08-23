import org.joml.*
import org.lwjgl.BufferUtils
import java.io.File
import java.nio.FloatBuffer
import org.lwjgl.opengl.GL33.*


class CloudLoader(file: String, shader: Shader) {
    private var count = 0
    private val data: FloatBuffer
    private val vao: Int
    private val shader: Shader = shader
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
                    .put(z)

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


    }

    fun draw() {

        shader.use()

        // Setting uniforms.
        shader.set("minZ", -10f)
        shader.set("midZ", 20f)
        shader.set("maxZ", 40f)
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

