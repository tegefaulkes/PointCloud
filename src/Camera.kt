import org.joml.*
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import java.nio.FloatBuffer
import kotlin.math.*

object Camera {

    private val posVec = Vector3f(0f,0f,0f)
    private val lookVec = Vector3f(0f,0f,0f)
    private val upVec = Vector3f(0f, 1f, 0f)
    private val tempVec = Vector3f()
    private val viewMat = Matrix4f()
    private const val lookSpeed = 0.2f
    private const val moveSpeed = 100f
    private var moveMod = 1f
    private var window = 0L
    private var pitch = 0.0
    private var yaw = 0.0
    private var lastX = 0.0
    private var lastY = 0.0
    private var firstMouseUpdate = true
    private var lastFrame = 0.0

    object MousePos {
        var posX = 0.0
        var posY = 0.0
        var scrX = 0.0
        var scrY = 0.0

        fun init(window: Long) {
            val mousePosCallback = object : GLFWCursorPosCallback() {
                override fun invoke(window: Long, xpos: Double, ypos: Double){
                    posX = xpos
                    posY = ypos
                }
            }
            glfwSetCursorPosCallback(window, mousePosCallback)

            val scrollPosCallback = object: GLFWScrollCallback() {
                override fun invoke(window: Long, xoffset: Double, yoffset: Double) {
                    scrX = xoffset
                    scrY = yoffset
                }
            }
            glfwSetScrollCallback(window, scrollPosCallback)
        }

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

        yaw -= xOffset * lookSpeedScaled
        pitch += yOffset * lookSpeedScaled
//        if(pitch > 89.0) pitch = 89.0
//        if(pitch < -89.0) pitch = -89.0

        lookVec.set(cos(yaw) * cos(pitch), sin(pitch), sin(yaw) * cos(pitch))

        // Camera movement.
        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            posVec.sub(tempVec.set(lookVec).mul(moveSpeedScaled))
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            posVec.add(tempVec.set(lookVec).mul(moveSpeedScaled))
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            posVec.add(tempVec.set(lookVec).cross(upVec).normalize().mul(moveSpeedScaled))
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            posVec.sub(tempVec.set(lookVec).cross(upVec).normalize().mul(moveSpeedScaled))
        if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) moveMod = 10.0f
        else if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) moveMod = 0.5f
        else moveMod = 1.0f

    }


    fun getViewMat(buffer: FloatBuffer) {
        getViewMat().get(buffer)
    }

    fun getViewMat(): Matrix4f{
        tempVec.set(posVec).add(lookVec)
        return viewMat.identity().lookAt(posVec, tempVec, upVec)
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