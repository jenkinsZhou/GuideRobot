package cn.tklvyou.guiderobot.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import cn.tklvyou.guiderobot.api.RetrofitHelper
import cn.tklvyou.guiderobot.api.RxSchedulers
import cn.tklvyou.guiderobot.base.BaseActivity
import cn.tklvyou.guiderobot.base.MyApplication
import cn.tklvyou.guiderobot.utils.MathUtil
import cn.tklvyou.guiderobot.log.TourCooLogUtil
import cn.tklvyou.guiderobot.manager.Robot
import cn.tklvyou.guiderobot.model.DaoSession
import cn.tklvyou.guiderobot.model.NavLocation
import cn.tklvyou.guiderobot.widget.RockerView
import cn.tklvyou.guiderobot_new.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.Gson
import com.slamtec.slamware.AbstractSlamwarePlatform
import com.slamtec.slamware.action.IMoveAction
import com.slamtec.slamware.exceptions.*
import kotlinx.android.synthetic.main.activity_gmapping.*
import com.slamtec.slamware.action.MoveDirection
import com.slamtec.slamware.discovery.DeviceManager
import com.slamtec.slamware.robot.*
import java.util.*
import com.slamtec.slamware.sdp.CompositeMapHelper
import kotlinx.android.synthetic.main.activity_splash.*


class GmappingActivity : BaseActivity(), View.OnClickListener {
    private val TAG = "GmappingActivity"
    override fun getActivityLayoutID(): Int {
        return R.layout.activity_gmapping
    }

    var list: MutableList<String>? = null
    private var index = 0

    private var robotPlatform: AbstractSlamwarePlatform? = null
    private var moveAction: IMoveAction? = null
    private var map: com.slamtec.slamware.robot.Map? = null

    private var isLoop = false
    private var currentDirection = RockerView.Direction.DIRECTION_CENTER
    private var timer: Timer? = null
    private var uiTimer: Timer? = null


    private var daoSession: DaoSession? = null
    private var pose: Pose? = null

    override fun initView() {
        robotPlatform = (application as MyApplication).getRobotPlatform()
//        robotPlatform!!.setSystemParameter(SYSPARAM_ROBOT_SPEED, SYSVAL_ROBOT_SPEED_LOW)
        robotPlatform!!.mapUpdate = true

        daoSession = (application as MyApplication).getDaoSession()
        slamWareMap.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event != null) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            pointView.visibility = View.INVISIBLE
                        }

                        MotionEvent.ACTION_UP -> {
                            pointView.visibility = View.VISIBLE
                        }
                        else -> {

                        }

                    }
                }
                return false
            }

        })

        saveMap.setOnClickListener(this)
        clearMap.setOnClickListener(this)
        saveLoaction.setOnClickListener(this)
//        saveOriginPoint.setOnClickListener(this)
//        navOriginPoint.setOnClickListener(this)
        tvTest.setOnClickListener(this)
        tvTest0.setOnClickListener(this)
        btnSafeExit.setOnClickListener(this)
        btnForceExit.setOnClickListener(this)

        val path = "/sdcard/robot/map.stcm"

        val compositeMapHelper = CompositeMapHelper()
        val compositeMap = compositeMapHelper.loadFile(path)

        val poseJson = SPUtils.getInstance().getString("pose")
        if (poseJson.isNotEmpty() && compositeMap != null) {
            robotPlatform!!.setCompositeMap(compositeMap, Gson().fromJson<Pose>(poseJson, Pose::class.java))
        }


        Thread(Runnable
        {

            uiTimer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    try {

                        /* 刷新Pose */
                        pose = robotPlatform!!.getPose()

//                        if (!isInit) {
//                            isInit = true
//                            val compisteMap = robotPlatform!!.compositeMap
//                            val maps = compisteMap.maps
//                            val itor = maps.iterator()
//                            while (itor.hasNext()) {
//                                val mapLayer = itor.next() as MapLayer
//                                if (mapLayer.usage == "explore") {
//                                    val gridMap = mapLayer as GridMap
//                                    gridMap.origin = pose!!.location
//                                }
//                            }
//                        }


                        /* 刷新电量 */
                        val percentage = robotPlatform!!.getBatteryPercentage()
                        tvShowBattery.text = "电量：" + percentage


                        /* 获取地图并刷新 */
                        val knownArea = robotPlatform!!.getKnownArea(MapType.BITMAP_8BIT, MapKind.EXPLORE_MAP)
                        map = robotPlatform!!.getMap(MapType.BITMAP_8BIT, MapKind.EXPLORE_MAP, knownArea)
                        slamWareMap.setMap(map)

                        val pointF = slamWareMap.mapCoordinateWidgetCoordinate(pose)
                        runOnUiThread {
                            pointView!!.x = pointF.x
                            pointView!!.y = pointF.y
                            pointView.rotation = (Math.toDegrees(pose!!.yaw.toDouble()).toFloat())
                        }

                    } catch (e: RequestFailException) {
                        e.printStackTrace()
                    } catch (e: ConnectionFailException) {
                        e.printStackTrace()
                    } catch (e: ConnectionTimeOutException) {
                        e.printStackTrace()
                    } catch (e: UnauthorizedRequestException) {
                        e.printStackTrace()
                    } catch (e: UnsupportedCommandException) {
                        e.printStackTrace()
                    } catch (e: ParseInvalidException) {
                        e.printStackTrace()
                    } catch (e: InvalidArgumentException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            }

            uiTimer!!.schedule(timerTask, 50, 100)
        }).start()

        Thread(Runnable
        {

            timer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    if (isLoop) {
                        try {

                            getDirection(currentDirection)
                        } catch (e: Exception) {

                        }
                    }
                }
            }


            timer!!.schedule(timerTask, 0, 500)
        }).start()

        // 设置回调模式
        rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE)

        // 监听摇动方向
        rockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_4_ROTATE_45,
                object : RockerView.OnShakeListener {
                    override fun onStart() {
                        isLoop = true
                    }

                    override fun direction(direction: RockerView.Direction) {
                        currentDirection = direction
                    }

                    override fun onFinish() {
                        isLoop = false
                    }
                })

    }

    override fun onClick(v: View?) {
        if (v != null) {
            try {
                when (v.id) {
                    R.id.saveMap -> {
                        val compositeMapHelper = CompositeMapHelper()
                        val destPath = "/sdcard/robot/map.stcm"
                        FileUtils.createOrExistsFile(destPath)
                        val errMsg = compositeMapHelper.saveFile(destPath, robotPlatform!!.compositeMap)
                        if (errMsg != null) { //若保存成功返回null，若失败返回表示错误信息的String。
                            ToastUtils.showShort(errMsg)
                        } else {
                            ToastUtils.showShort("保存地图成功")
                        }
                    }

                    R.id.clearMap -> {
                        robotPlatform!!.clearMap()
                    }

                    R.id.saveLoaction -> {
                        MaterialDialog(this).show {
                            title(R.string.saveTitle)

                            customView(R.layout.custom_input_view, scrollable = false)

                            positiveButton(R.string.save) { dialog ->
                                val id: EditText = dialog.getCustomView().findViewById(R.id.etId)
                                val name: EditText = dialog.getCustomView().findViewById(R.id.etName)
                                if (id.text.toString().isEmpty()) {
                                    ToastUtils.showShort("请输入编号")
                                    return@positiveButton
                                }
                                if (name.text.toString().isEmpty()) {
                                    ToastUtils.showShort("请输入名称")
                                    return@positiveButton
                                }
                                val navLocation = NavLocation()
                                navLocation.id = id.text.toString().toLong()
                                navLocation.name = name.text.toString()
                                navLocation.x = robotPlatform!!.pose.x
                                navLocation.y = robotPlatform!!.pose.y
                                navLocation.z = robotPlatform!!.pose.z
                                LogUtils.i(TAG,"X值="+robotPlatform!!.pose.x)
                                LogUtils.d(TAG,"Y值="+robotPlatform!!.pose.y)
                                LogUtils.e(TAG,"Z值="+robotPlatform!!.pose.z)
                                navLocation.rotation = robotPlatform!!.pose.yaw
                                TourCooLogUtil.i("线路", navLocation)
                                if (daoSession!!.navLocationDao.load(navLocation.id) != null) {
                                    ToastUtils.showShort("当前位置编号已被使用")
                                } else {
                                    showDialog()
                                    RetrofitHelper.getInstance().getServer()
                                            .addLocation(navLocation.id, navLocation.name)
                                            .compose(RxSchedulers.applySchedulers())
                                            .subscribe({ result ->
                                                hideDialog()
                                                when (result.status) {
                                                    0 -> {
                                                        ToastUtils.showShort(result.errmsg)
                                                    }
                                                    1 -> {
                                                        val index = daoSession!!.navLocationDao.insert(navLocation)
                                                        if (index == -1L) {
                                                            ToastUtils.showShort("保存失败")
                                                        } else {
                                                            ToastUtils.showShort("保存成功")
                                                        }
                                                    }

                                                }

                                            }, {
                                                ToastUtils.showShort(it.toString())
                                                it.printStackTrace()
                                            })
                                }

                            }

                        }

                    }

                    R.id.btnForceExit -> {
                        MaterialDialog(this).show {
                            title(R.string.tip)
                            message(R.string.force_exit)
                            positiveButton(R.string.exit) {
                                robotPlatform?.disconnect()
                                val intent = Intent(Intent.ACTION_MAIN)
                                intent.addCategory(Intent.CATEGORY_HOME)
                                startActivity(intent)
                                System.exit(0)
                            }

                            negativeButton(R.string.cancel) {
                                dismiss()
                            }
                        }
                    }

                    R.id.btnSafeExit -> {
                        MaterialDialog(this).show {
                            title(R.string.tip)
                            message(R.string.safe_exit)
                            positiveButton(R.string.exit) {
                                SPUtils.getInstance().put("pose", Gson().toJson(pose))
                                robotPlatform?.disconnect()
                                val intent = Intent(Intent.ACTION_MAIN)
                                intent.addCategory(Intent.CATEGORY_HOME)
                                startActivity(intent)
                                System.exit(0)
                            }

                            negativeButton(R.string.cancel) {
                                dismiss()
                            }
                        }
                    }

                    R.id.tvTest -> {
                      var  currentPose = robotPlatform!!.pose
                        var rotation = currentPose?.rotation
                        var yaw = rotation?.yaw
                         var newRotation = Rotation()
                        newRotation.yaw  = 20.0f
                        currentPose.rotation = newRotation
                        robotPlatform!!.pose = currentPose
                        val rotation1 = Rotation(MathUtil.PI*2)
                        val action = robotPlatform!!.rotate(rotation1)
                        LogUtils.d(TAG, "当前的yaw:${action.actionName}")
                        LogUtils.i(TAG, "当前的yaw:$yaw")
                    }
                    R.id.tvTest0 ->{
                   /*     var  currentPose = robotPlatform!!.pose
                        var rotation = currentPose?.rotation
                        var yaw = rotation?.yaw
                        LogUtils.d(TAG, "当前的yaw:$yaw")
                        var newRotation = Rotation()
                        newRotation.yaw  = 1f
                        currentPose.rotation = newRotation
                        robotPlatform!!.pose = currentPose
                        val rotation1 = Rotation(-MathUtil.PI*2)
                        val action = robotPlatform!!.rotate(rotation1)
                        LogUtils.d(TAG, "当前的yaw:${action.actionName}")*/
                        skipMainActivity()
                    }
//                    R.id.saveOriginPoint -> {
//                        val compisteMap = robotPlatform!!.compositeMap
//                        val maps = compisteMap.maps
//                        val itor = maps.iterator()
//                        while (itor.hasNext()) {
//                            val mapLayer = itor.next() as MapLayer
//                            if (mapLayer.usage == "explore") {
//                                val gridMap = mapLayer as GridMap
//                                gridMap.origin = pose!!.location
//                            }
//                        }
//
//                        ToastUtils.showShort("设置原点成功")
//                    }
//
//                    R.id.navOriginPoint -> {
//                        val compisteMap = robotPlatform!!.compositeMap
//                        val maps = compisteMap.maps
//                        val itor = maps.iterator()
//
//                        var location: Location? = null
//                        while (itor.hasNext()) {
//                            val mapLayer = itor.next() as MapLayer
//                            if (mapLayer.usage == "explore") {
//                                val gridMap = mapLayer as GridMap
//                                location = gridMap.origin
//                            }
//                        }
//                        if (location != null) {
//                            val moveOption = MoveOption()
//                            //机器人移动的时候精确到点
//                            moveOption.isPrecise = true
//                            moveOption.isMilestone = true
//
//                            val action = robotPlatform!!.moveTo(location, moveOption, 0f)
//                            action!!.waitUntilDone()
//                            ToastUtils.showShort("回到原点成功")
//                        }
//
//                    }

                    else -> {

                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun getDirection(direction: RockerView.Direction) {
        when (direction) {
            RockerView.Direction.DIRECTION_LEFT -> {
                // "左"
                robotPlatform!!.moveBy(MoveDirection.TURN_LEFT)
            }
            RockerView.Direction.DIRECTION_RIGHT -> {
                // "右"
                robotPlatform!!.moveBy(MoveDirection.TURN_RIGHT)
            }
            RockerView.Direction.DIRECTION_UP -> {
                // "上"
                robotPlatform!!.moveBy(MoveDirection.FORWARD)
            }
            RockerView.Direction.DIRECTION_DOWN -> {
                // "下"
                robotPlatform!!.moveBy(MoveDirection.BACKWARD)
            }
            RockerView.Direction.DIRECTION_UP_LEFT -> {
                // "左上"
            }
            RockerView.Direction.DIRECTION_UP_RIGHT -> {
                // "右上"
            }
            RockerView.Direction.DIRECTION_DOWN_LEFT -> {
                // "左下"
            }
            RockerView.Direction.DIRECTION_DOWN_RIGHT -> {
                // "右下"
            }
            else -> {

            }
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        uiTimer?.cancel()
        super.onDestroy()
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
            SPUtils.getInstance().put("pose", Gson().toJson(pose))
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


    private fun skipMainActivity(){
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
                        Robot.getInstance().slamWarePlatform = robotPlatform
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

}
