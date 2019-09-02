package cn.tklvyou.guiderobot.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.widget.EditText
import cn.tklvyou.guiderobot.R
import cn.tklvyou.guiderobot.api.RetrofitHelper
import cn.tklvyou.guiderobot.api.RxSchedulers
import cn.tklvyou.guiderobot.base.BaseActivity
import cn.tklvyou.guiderobot.base.MyApplication
import cn.tklvyou.guiderobot.model.NavLocation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.blankj.utilcode.util.ToastUtils
import com.iflytek.aiui.uartkit.UARTAgent
import com.slamtec.slamware.discovery.DeviceManager
import com.slamtec.slamware.exceptions.ConnectionTimeOutException
import kotlinx.android.synthetic.main.activity_splash.*
import com.slamtec.slamware.robot.NetworkMode


class SplashActivity : BaseActivity() {
    override fun playComplete() {
    }

    override fun getActivityLayoutID(): Int {
        return R.layout.activity_splash
    }


    override fun initView() {
        btnGMapping.setOnClickListener {
            showDialog()
            btnGMapping.isEnabled = false
            val ipStr = editTextIp.text.toString().trim()
            if (ipStr.isEmpty()) {
                ToastUtils.showShort("请输入机器人IP地址")
            } else {
                Thread(Runnable {
                    try {
                        val robotPlatform = DeviceManager.connect(ipStr, 1445)
                        if (robotPlatform == null) {
                            ToastUtils.showShort("连接失败，请输入正确的IP地址")
                        } else {
                            (application as MyApplication).setRobotPlatform(robotPlatform)
                            //配置底盘路由模式——中继
//                            val options = HashMap<String, String>()
//                            options.put("ssid", "Robot")
//                            options.put("password", "12345678")
//                            val isSuccess = robotPlatform.configureNetwork(NetworkMode.NetworkModeStation, options)

                            ToastUtils.showShort("连接成功")
                            val intent = Intent(this, GmappingActivity::class.java)
                            startActivity(intent)
                        }
                        hideDialog()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (e is ConnectionTimeOutException) {
                            ToastUtils.showShort("连接超时，请检查网络")
                        } else {
                            ToastUtils.showShort("连接失败，请输入正确的IP地址")
                        }
                        hideDialog()
                    }
                }).start()

            }
            btnGMapping.isEnabled = true
        }

        btnExit.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
            System.exit(0)
        }

//        btnNavigation.setOnClickListener {
//            showDialog()
//            btnNavigation.isEnabled = false
//            val ipStr = editTextIp.text.toString().trim()
//            if (ipStr.isEmpty()) {
//                ToastUtils.showShort("请输入机器人IP地址")
//            } else {
//                Thread(Runnable {
//                    try {
//                        val robotPlatform = DeviceManager.connect(ipStr, 1445)
//                        if (robotPlatform == null) {
//                            ToastUtils.showShort("连接失败，请输入正确的IP地址")
//                        } else {
//                            (application as MyApplication).setRobotPlatform(robotPlatform)
//                            ToastUtils.showShort("连接成功")
//                            val intent = Intent(this, Main2Activity::class.java)
//                            startActivity(intent)
//                        }
//                        hideDialog()
//                        finish()
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        if (e is ConnectionTimeOutException) {
//                            ToastUtils.showShort("连接超时，请检查网络")
//                        } else {
//                            ToastUtils.showShort("连接失败，请输入正确的IP地址")
//                        }
//                        hideDialog()
//                    }
//                }).start()
//
//            }
//            btnNavigation.isEnabled = true
//        }


    }


    override fun onInitSuccess() {
        super.onInitSuccess()
        btnNavigation.setOnClickListener {
            showDialog()
            btnNavigation.isEnabled = false
            val ipStr = editTextIp.text.toString().trim()
            if (ipStr.isEmpty()) {
                ToastUtils.showShort("请输入机器人IP地址")
            } else {
                Thread(Runnable {
                    try {
                        val robotPlatform = DeviceManager.connect(ipStr, 1445)
                        if (robotPlatform == null) {
                            ToastUtils.showShort("连接失败，请输入正确的IP地址")
                        } else {
                            (application as MyApplication).setRobotPlatform(robotPlatform)
                            ToastUtils.showShort("连接成功")
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                        hideDialog()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (e is ConnectionTimeOutException) {
                            ToastUtils.showShort("连接超时，请检查网络")
                        } else {
                            ToastUtils.showShort("连接失败，请输入正确的IP地址")
                        }
                        hideDialog()
                    }
                }).start()

            }
            btnNavigation.isEnabled = true
        }

//        btnNavigation.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }

    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            appExit()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private var isExit: Boolean = false

    private fun appExit() {
        if (!isExit) {
            isExit = true
            ToastUtils.showShort("再按一次退出程序")
            exitHandler.sendEmptyMessageDelayed(0, 2000)
        } else {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
            System.exit(0)
        }
    }

    private val exitHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            isExit = false
        }
    }


}
