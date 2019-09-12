package cn.tklvyou.guiderobot.base

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.multidex.MultiDexApplication
import cn.tklvyou.guiderobot.constant.RequestConstant.WEB_SOCKET_URL
import cn.tklvyou.guiderobot.crash.CrashManager
import cn.tklvyou.guiderobot.log.LogConfig.PATH_LOG_SAVE
import cn.tklvyou.guiderobot.log.LogConfig.TAG_LOG_PRE_SUFFIX
import cn.tklvyou.guiderobot.log.TourCooLogUtil
import cn.tklvyou.guiderobot.log.widget.LogFileEngineFactory
import cn.tklvyou.guiderobot.log.widget.config.LogLevel
import cn.tklvyou.guiderobot.model.DaoMaster
import cn.tklvyou.guiderobot.model.DaoSession
import cn.tklvyou.guiderobot.utils.MotorController
import cn.tklvyou.guiderobot.websocket.WebSocketHandler
import cn.tklvyou.guiderobot.websocket.WebSocketSetting
import cn.tklvyou.guiderobot.widget.toast.ToastImpl
import cn.tklvyou.guiderobot.widget.toast.UiManager
import cn.tklvyou.serialportlibrary.BuildConfig

import cn.tklvyou.serialportlibrary.SerialPort
import com.iflytek.aiui.uartkit.UARTAgent
import com.iflytek.aiui.uartkit.ctrdemo.AIUITextSynthesis
import com.slamtec.slamware.AbstractSlamwarePlatform

/**
 * Created by Administrator on 2019/4/24.
 */

class MyApplication : MultiDexApplication() {

    private lateinit var aiui: AIUITextSynthesis

    private lateinit var robotPlatform: AbstractSlamwarePlatform

    private var daoSession: DaoSession? = null
    private var mContext: Context? = null

    private var platform: MotorController? = null

    private lateinit var serialPort: SerialPort

    override fun onCreate() {
        super.onCreate()
        mContext = this
        initLog()
        //异常处理初始化
        CrashManager.init(this)
        initGreenDao()
        UiManager.init(this)
        UiManager.getInstance().toastControl = ToastImpl()
        //COM1串口
        aiui = AIUITextSynthesis(this)
        //COM2串口
        serialPort = SerialPort.getInstance("/dev/ttyS1", 9600)
        serialPort.installAllConfigs()
//        val timer = Timer()
//        val task = object : TimerTask() {
//            override fun run() {
//                serialPort.sendDataToSerialPort("test".toByteArray())
//            }
//        }
//        timer.schedule(task, 2000, 3000)


        //电机控制类  数据监听回调
        platform = MotorController(this, MotorController.MotorControllerListener { data: String? ->

        })
        initWebSocket()
    }


    /**
     * 初始化GreenDao
     */
    private fun initGreenDao() {
        val helper = DaoMaster.DevOpenHelper(this, "guide_robot.db")
        val db = helper.getWritableDatabase()
        val daoMaster = DaoMaster(db)
        daoSession = daoMaster.newSession()
    }

    fun getAppContext(): Context {
        return mContext!!
    }


    public fun getDaoSession(): DaoSession {
        return daoSession!!
    }

    public fun getLedController(): SerialPort {
        return serialPort
    }

    public fun getUARTAgent(): UARTAgent {
        return aiui.mAgent
    }

    public fun getAIUITextSynthesis(): AIUITextSynthesis {
        return this.aiui
    }

    public fun getMotorController(): MotorController? {
        return platform
    }

    public fun setRobotPlatform(robotPlatform: AbstractSlamwarePlatform) {
        this.robotPlatform = robotPlatform
    }

    public fun getRobotPlatform(): AbstractSlamwarePlatform {
        return this.robotPlatform
    }


    /**
     * 初始化日志配置
     */
    private fun initLog() {
        TourCooLogUtil.getLogConfig()
                .configAllowLog(BuildConfig.DEBUG)
                .configTagPrefix(TAG_LOG_PRE_SUFFIX)
                .configShowBorders(false).configLevel(LogLevel.TYPE_VERBOSE)
        // 支持输入日志到文件
        var filePath = "" + Environment.getExternalStorageDirectory() + PATH_LOG_SAVE
        TourCooLogUtil.getLogFileConfig().configLogFileEnable(BuildConfig.DEBUG)
                .configLogFilePath(filePath)
                .configLogFileLevel(LogLevel.TYPE_VERBOSE)
                .configLogFileEngine(LogFileEngineFactory(this))
    }


    private fun initWebSocket() {
        val setting = WebSocketSetting()
        //连接地址，必填
        setting.connectUrl = WEB_SOCKET_URL
        //设置连接超时时间
        setting.connectTimeout = 15 * 1000

        //设置心跳间隔时间
        setting.connectionLostTimeout = 30

        //设置断开后的重连次数，可以设置的很大，不会有什么性能上的影响
        setting.reconnectFrequency = 10

        //网络状态发生变化后是否重连，
        //需要调用 WebSocketHandler.registerNetworkChangedReceiver(context) 方法注册网络监听广播
        setting.setReconnectWithNetworkChanged(true)

        //通过 init 方法初始化默认的 WebSocketManager 对象
        val manager = WebSocketHandler.init(setting)
        //启动连接
        manager.start()
        //注意，需要在 AndroidManifest 中配置网络状态获取权限
        //注册网路连接状态变化广播
        WebSocketHandler.registerNetworkChangedReceiver(this)
    }



}
