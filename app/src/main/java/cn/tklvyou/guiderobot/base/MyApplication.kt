package cn.tklvyou.guiderobot.base

import android.app.Application
import android.content.Context
import android.os.Environment
import cn.tklvyou.guiderobot.crash.CrashManager
import cn.tklvyou.guiderobot.log.LogConfig.PATH_LOG_SAVE
import cn.tklvyou.guiderobot.log.LogConfig.TAG_LOG_PRE_SUFFIX
import cn.tklvyou.guiderobot.log.TourCooLogUtil
import cn.tklvyou.guiderobot.log.widget.LogFileEngineFactory
import cn.tklvyou.guiderobot.log.widget.config.LogLevel
import cn.tklvyou.guiderobot.model.DaoMaster
import cn.tklvyou.guiderobot.model.DaoSession
import cn.tklvyou.guiderobot.utils.MotorController
import cn.tklvyou.serialportlibrary.BuildConfig

import cn.tklvyou.serialportlibrary.SerialPort
import com.iflytek.aiui.uartkit.UARTAgent
import com.iflytek.aiui.uartkit.ctrdemo.AIUITextSynthesis
import com.slamtec.slamware.AbstractSlamwarePlatform

/**
 * Created by Administrator on 2019/4/24.
 */

class MyApplication : Application() {

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
        initGreenDao()
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

        //异常处理初始化
        CrashManager.init(this)
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
        var filePath = "" + Environment.getExternalStorageDirectory() +PATH_LOG_SAVE
        TourCooLogUtil.getLogFileConfig().configLogFileEnable(BuildConfig.DEBUG)
                .configLogFilePath(filePath)
                .configLogFileLevel(LogLevel.TYPE_VERBOSE)
                .configLogFileEngine(LogFileEngineFactory(this))
    }

}
