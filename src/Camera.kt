import org.joml.*
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import java.nio.FloatBuffer
import kotlin.math.*

object Camera {

    private val posVec  = Vector3f(0f, 0f, 0f)
    private val fVec    = Vector3f(1f, 0f, 0f)
    private val rVec    = Vector3f(0f, 1f, 0f)
    private val uVec    = Vector3f(0f, 0f, 1f)
    private val rotation = Quaternionf()

    private val tempVec = Vector3f()
    private val viewMat = Matrix4f()
    private const val lookSpeed = 0.2f
    private const val moveSpeed = 100f
    private var moveMod = 1f
    private var window = 0L
    private var lastX = 0.0f
    private var lastY = 0.0f
    private var firstMouseUpdate = true
    private var lastFrame = 0.0

    object MousePos {
        var posX = 0.0f
        var posY = 0.0f
        var scrX = 0.0f
        var scrY = 0.0f

        fun init(window: Long) {
            val mousePosCallback = object : GLFWCursorPosCallback() {
                override fun invoke(window: Long, xpos: Double, ypos: Double){
                    posX = xpos.toFloat()
                    posY = ypos.toFloat()
                }
            }
            glfwSetCursorPosCallback(window, mousePosCallback)

            val scrollPosCallback = object: GLFWScrollCallback() {
                override fun invoke(window: Long, xoffset: Double, yoffset: Double) {
                    scrX = xoffset.toFloat()
                    scrY = yoffset.toFloat()
                }
            }
            glfwSetScrollCallback(window, scrollPosCallback)
        }

    }

    private fun rotatePitch(rad: Float) {
        rotation.fromAxisAngleRad(rVec, rad)
        rotation.transform(fVec)
        rotation.transform(uVec)
    }

    private fun rotateYaw(rad: Float) {
        rotation.fromAxisAngleRad(uVec, rad)
        rotation.transform(fVec)
        rotation.transform(rVec)
    }

    private fun rotateRoll(rad: Float) {
        rotation.fromAxisAngleRad(fVec, rad)
        rotation.transform(uVec)
        rotation.transform(rVec)
    }



    fun init(window: Long) {
        this.window = window
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        //hook on mouse input
        MousePos.init(window)
        updateInputs()
    }

    fun updateInputs() {

        // Time
        val timeValue = glfwGetTime()
        val deltaTime = (lastFrame - timeValue).toFloat()
        lastFrame = timeValue
        val moveSpeedScaled = moveSpeed * deltaTime * moveMod
        val lookSpeedScaled = lookSpeed * deltaTime

        // Mouse stuff.
        if (firstMouseUpdate) {
            lastX = MousePos.posX
            lastY = MousePos.posY
            firstMouseUpdate = false
        }

        val xOffset = MousePos.posX - lastX
        val yOffset = MousePos.posY - lastY
        lastX = MousePos.posX
        lastY = MousePos.posY

        rotateYaw(xOffset * lookSpeedScaled)
        rotatePitch(- yOffset * lookSpeedScaled)

        // Camera movement.
        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) posVec.sub(tempVec.set(fVec).mul(moveSpeedScaled))
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) posVec.add(tempVec.set(fVec).mul(moveSpeedScaled))
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) posVec.sub(tempVec.set(rVec).normalize().mul(moveSpeedScaled))
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) posVec.add(tempVec.set(rVec).normalize().mul(moveSpeedScaled))

        if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) rotateRoll( 1.5f * deltaTime)
        if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) rotateRoll(-1.5f * deltaTime)

        if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS)        posVec.sub(tempVec.set(uVec).mul(moveSpeedScaled))
        if(glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) posVec.add(tempVec.set(uVec).mul(moveSpeedScaled))

        if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) moveMod = 10.0f
        else moveMod = 1.0f
    }


    fun getViewMat(buffer: FloatBuffer) {
        getViewMat().get(buffer)
    }

    fun getViewMat(): Matrix4f{
        tempVec.set(posVec).add(fVec)
        return viewMat.identity().lookAt(posVec, tempVec, uVec)
    }

    fun getPosVec(): Vector3f {
        return Vector3f().set(posVec)
    }

    fun setPosvec(x: Float, y: Float, z: Float) {
        setPosVec(Vector3f(x, y, z))
    }
    fun setPosVec(xyz:Vector3f) {
        posVec.set(xyz)
    }


}