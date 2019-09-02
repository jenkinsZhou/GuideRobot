package cn.tklvyou.guiderobot.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.slamtec.slamware.sdp.CompositeMapHelper
import cn.tklvyou.guiderobot.R
import cn.tklvyou.guiderobot.RobotAction
import cn.tklvyou.guiderobot.api.RetrofitHelper
import cn.tklvyou.guiderobot.api.RxSchedulers
import cn.tklvyou.guiderobot.base.BaseActivity
import cn.tklvyou.guiderobot.base.MyApplication
import cn.tklvyou.guiderobot.model.DaoSession
import cn.tklvyou.guiderobot.model.LocationModel
import cn.tklvyou.guiderobot.model.NavLocation
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.iflytek.aiui.uartkit.UARTAgent
import com.slamtec.slamware.AbstractSlamwarePlatform
import com.slamtec.slamware.action.ActionStatus
import com.slamtec.slamware.action.IAction
import com.slamtec.slamware.robot.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : BaseActivity() {

    override fun getActivityLayoutID(): Int {
        return R.layout.activity_main
    }

    private lateinit var mUARTAgent: UARTAgent

    private var robotPlatform: AbstractSlamwarePlatform? = null
    private var action: IAction? = null
    private var daoSession: DaoSession? = null
    private var datas: MutableList<NavLocation>? = null

    private var nextId = 0L //下一个坐标点的ID
    private var index = 0  //每个讲解点的下标
    private var list: MutableList<LocationModel.ContentBean>? = null //每个讲解点的内容

    private var currentNavLocation: NavLocation? = null

    private var isTip = false

    private var handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if (msg != null) {

                when (msg.what) {
                    0 -> {
                        btnStartNav.setOnClickListener {
                            btnStartNav.visibility = View.INVISIBLE

                            RetrofitHelper.getInstance().getServer()
                                    .getLocationMessage(0)
                                    .compose(RxSchedulers.applySchedulers())
                                    .subscribe({ result ->
                                        hideDialog()
                                        when (result.status) {
                                            0 -> {
                                                ToastUtils.showShort(result.errmsg)
                                                btnStartNav.visibility = View.VISIBLE
                                            }
                                            1 -> {
                                                index = 0
                                                val model = result.data
                                                nextId = model.next
                                                currentNavLocation = daoSession!!.navLocationDao.load(model.local) as NavLocation
                                                try {
                                                    navToLocation(currentNavLocation!!)

                                                    Glide.with(this@MainActivity).load(model.thumb).into(ivShow)
                                                    list = model.content

                                                    val ctrMsg = Message()
                                                    ctrMsg.what = 999
                                                    ctrMsg.obj = list
                                                    sendMessage(ctrMsg)


                                                } catch (e: Exception) {
                                                    /* Exception Handle code*/
                                                    ToastUtils.showShort(e.message)
                                                }
                                            }

                                        }

                                    }, {
                                        it.printStackTrace()
                                    })
                        }
                    }

                    999 -> {
                        checkContent(msg.obj as MutableList<LocationModel.ContentBean>)
                    }

                    1000 -> {
                        val action_value = msg.data.getString("action")
                        LogUtils.e(action_value)

                        val ctrList = msg.obj as MutableList<LocationModel.ContentBean>

                        if (action_value == RobotAction.AWAIT_TIME) {
                            postDelayed({

                                /*当前动作指令不是列表中的最后一条记录*/
                                if (ctrList.size - 1 > index) {
                                    index++
                                    val ctrMsg = Message()
                                    ctrMsg.what = 999
                                    ctrMsg.obj = ctrList
                                    sendMessage(ctrMsg)
                                } else {  /*当前动作指令已经是最后一条记录了*/
                                    /**
                                     * 若存在下一个讲解点，就执行网络请求获取下个点的数据，
                                     * 否则不执行任何动作
                                     */
                                    if (nextId != 0L) {
                                        try {
                                            RetrofitHelper.getInstance().getServer()
                                                    .getLocationMessage(nextId)
                                                    .compose(RxSchedulers.applySchedulers())
                                                    .subscribe({ result ->
                                                        hideDialog()
                                                        when (result.status) {
                                                            0 -> {
                                                                ToastUtils.showShort(result.errmsg)
                                                            }
                                                            1 -> {
                                                                index = 0
                                                                val model = result.data
                                                                nextId = model.next
                                                                currentNavLocation = daoSession!!.navLocationDao.load(model.local) as NavLocation
                                                                try {
                                                                    navToLocation(currentNavLocation!!)

                                                                    Glide.with(this@MainActivity).load(model.thumb).into(ivShow)
                                                                    list = model.content


                                                                    val ctrMsg = Message()
                                                                    ctrMsg.what = 999
                                                                    ctrMsg.obj = list
                                                                    sendMessage(ctrMsg)

                                                                } catch (e: Exception) {
                                                                    /* Exception Handle code*/
                                                                    ToastUtils.showShort(e.message)
                                                                }
                                                            }
                                                        }

                                                    }, {
                                                        it.printStackTrace()
                                                    })

                                        } catch (e: Exception) {
                                            /* Exception Handle code*/

                                        }
                                    }


                                }


                            }, 1500)
                        } else {
                            val actionModel = RobotAction.getControllerCommand(action_value)
                            when (actionModel.type) {
                                RobotAction.LEFT_RIGHT_HANDS_ACTION -> {
                                    (application as MyApplication).getMotorController()!!.sendData(actionModel.params as ByteArray)
                                }

                                RobotAction.HEAD_LED_ACTION -> {
                                    (application as MyApplication).getLedController().sendDataToSerialPort(actionModel.params as ByteArray)
                                }

                            }


                            /*当前动作指令不是列表中的最后一条记录*/
                            if (ctrList.size - 1 > index) {
                                index++
                                val ctrMsg = Message()
                                ctrMsg.what = 999
                                ctrMsg.obj = ctrList
                                sendMessage(ctrMsg)
                            } else {  /*当前动作指令已经是最后一条记录了*/

                                /**
                                 * 若存在下一个讲解点，就执行网络请求获取下个点的数据，
                                 * 否则不执行任何动作
                                 */
                                if (nextId != 0L) {
                                    try {
                                        RetrofitHelper.getInstance().getServer()
                                                .getLocationMessage(nextId)
                                                .compose(RxSchedulers.applySchedulers())
                                                .subscribe({ result ->
                                                    hideDialog()
                                                    when (result.status) {
                                                        0 -> {
                                                            ToastUtils.showShort(result.errmsg)
                                                        }
                                                        1 -> {
                                                            index = 0
                                                            val model = result.data
                                                            nextId = model.next
                                                            currentNavLocation = daoSession!!.navLocationDao.load(model.local) as NavLocation
                                                            try {
                                                                navToLocation(currentNavLocation!!)

                                                                Glide.with(this@MainActivity).load(model.thumb).into(ivShow)
                                                                list = model.content


                                                                val ctrMsg = Message()
                                                                ctrMsg.what = 999
                                                                ctrMsg.obj = list
                                                                sendMessage(ctrMsg)

                                                            } catch (e: Exception) {
                                                                /* Exception Handle code*/
                                                                ToastUtils.showShort(e.message)
                                                            }
                                                        }
                                                    }

                                                }, {
                                                    it.printStackTrace()
                                                })

                                    } catch (e: Exception) {
                                        /* Exception Handle code*/

                                    }
                                } else {

                                    ivShow.setImageResource(R.drawable.end_bg)

                                    val timer = Timer()
                                    val timerTask = object : TimerTask() {
                                        override fun run() {
                                            runOnUiThread {
                                                btnStartNav.visibility = View.VISIBLE
                                                ivShow.setImageResource(R.drawable.default_bg)
                                            }
                                        }
                                    }
                                    timer.schedule(timerTask, 5000)
                                }


                            }

                        }

                    }

                }

            }

        }
    }


    private fun navToLocation(currentNavLocation: NavLocation) {
        val moveOption = MoveOption()
        //机器人移动的时候精确到点
        moveOption.isPrecise = true
        moveOption.isMilestone = true
        val location = Location(currentNavLocation.x, currentNavLocation.y, currentNavLocation.z)
        action = robotPlatform!!.moveTo(location, moveOption, 0f)
        val status = action!!.waitUntilDone()

        if (status == ActionStatus.FINISHED) {
            return
        } else {
            isTip = true
            speckTextSynthesis("小哥哥小姐姐们，请不要挡住我的路好嘛，谢谢！", false)
            navToLocation(currentNavLocation)
        }
    }


    override fun initView() {
        robotPlatform = (application as MyApplication).getRobotPlatform()

        mUARTAgent = (application as MyApplication).getUARTAgent()

        initData()

        Thread(Runnable {
            val path = "/sdcard/robot/map.stcm"

            val compositeMapHelper = CompositeMapHelper()
            val compositeMap = compositeMapHelper.loadFile(path)

            val poseJson = SPUtils.getInstance().getString("pose")
            if (poseJson.isNotEmpty() && compositeMap != null) {
                robotPlatform!!.setCompositeMap(compositeMap, Gson().fromJson<Pose>(poseJson, Pose::class.java))
                handler.sendEmptyMessage(0)
            }
        }).start()

    }

    override fun playComplete() {
        super.playComplete()

        if (isTip) {
            isTip = false
        } else {
            index++

            /**
             * 如果当前已经是指令列表中最后一条操作记录，则判断当前是否是最后一个讲解点
             * 如果为当前已经是最后一个点，则显示最后的尾页，否则网络请求获取下一个讲解点数据
             */
            if (list!!.size - 1 < index) {
                if (nextId != 0L) {
                    try {
                        RetrofitHelper.getInstance().getServer()
                                .getLocationMessage(nextId)
                                .compose(RxSchedulers.applySchedulers())
                                .subscribe({ result ->
                                    hideDialog()
                                    when (result.status) {
                                        0 -> {
                                            ToastUtils.showShort(result.errmsg)
                                        }
                                        1 -> {
                                            index = 0
                                            val model = result.data
                                            nextId = model.next
                                            currentNavLocation = daoSession!!.navLocationDao.load(model.local) as NavLocation
                                            try {
                                                navToLocation(currentNavLocation!!)

                                                Glide.with(this@MainActivity).load(model.thumb).into(ivShow)
                                                this.list = model.content

                                                val ctrMsg = Message()
                                                ctrMsg.what = 999
                                                ctrMsg.obj = list
                                                handler.sendMessage(ctrMsg)

                                            } catch (e: Exception) {
                                                /* Exception Handle code*/
                                                ToastUtils.showShort(e.message)
                                            }

                                        }

                                    }

                                }, {
                                    it.printStackTrace()
                                })

                    } catch (e: Exception) {
                        /* Exception Handle code*/

                    }

                } else {

                    runOnUiThread {
                        ivShow.setImageResource(R.drawable.end_bg)

                        val timer = Timer()
                        val timerTask = object : TimerTask() {
                            override fun run() {
                                runOnUiThread {
                                    btnStartNav.visibility = View.VISIBLE
                                    ivShow.setImageResource(R.drawable.default_bg)
                                }
                            }
                        }
                        timer.schedule(timerTask, 5000)
                    }
                }
            } else {
                val ctrMsg = Message()
                ctrMsg.what = 999
                ctrMsg.obj = list
                handler.sendMessage(ctrMsg)
            }
        }

    }

    private fun checkContent(list: MutableList<LocationModel.ContentBean>) {

        if (list[index].type == "text") {
            speckTextSynthesis(list[index].value, index == list.size - 1)
            return
        } else {
            val bundle = Bundle()
            bundle.putString("action", list[index].value)
            val ctrMsg = Message()
            ctrMsg.what = 1000
            ctrMsg.obj = list
            ctrMsg.data = bundle
            handler.sendMessageDelayed(ctrMsg, 200)
        }
    }


    private fun initData() {
        daoSession = (application as MyApplication).getDaoSession()
        datas = daoSession!!.navLocationDao.queryBuilder().list()
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
            SPUtils.getInstance().put("pose", Gson().toJson(robotPlatform!!.pose))
            robotPlatform?.disconnect()
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
