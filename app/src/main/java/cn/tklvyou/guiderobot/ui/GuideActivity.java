package cn.tklvyou.guiderobot.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.iflytek.aiui.uartkit.ctrdemo.util.ToastUtil;
import com.slamtec.slamware.AbstractSlamwarePlatform;
import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.action.IAction;
import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.robot.ArtifactUsage;
import com.slamtec.slamware.robot.CompositeMap;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.MoveOption;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.sdp.CompositeMapHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.tklvyou.guiderobot.api.RetrofitHelper;
import cn.tklvyou.guiderobot.api.RxSchedulers;
import cn.tklvyou.guiderobot.base.BaseActivity;
import cn.tklvyou.guiderobot.base.BaseResult;
import cn.tklvyou.guiderobot.base.MyApplication;
import cn.tklvyou.guiderobot.constant.HomeConstant;
import cn.tklvyou.guiderobot.log.TourCooLogUtil;
import cn.tklvyou.guiderobot.manager.GlideManager;
import cn.tklvyou.guiderobot.manager.Robot;
import cn.tklvyou.guiderobot.model.DaoSession;
import cn.tklvyou.guiderobot.model.LocationModel;
import cn.tklvyou.guiderobot.model.NavLocation;
import cn.tklvyou.guiderobot.threadpool.ThreadPoolManager;
import cn.tklvyou.guiderobot_new.R;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.observers.BasicFuseableObserver;

import static cn.tklvyou.guiderobot.constant.HomeConstant.MSG_CLOSE_LOADING;
import static cn.tklvyou.guiderobot.constant.HomeConstant.MSG_SHOW_LOADING;
import static cn.tklvyou.guiderobot.constant.HomeConstant.MSG_TOAST;
import static cn.tklvyou.guiderobot.constant.RequestConstant.REQUEST_ERROR;
import static cn.tklvyou.guiderobot.constant.RequestConstant.REQUEST_SUCCESS;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2019年09月05日13:50
 * @Email: 971613168@qq.com
 */
public class GuideActivity extends BaseActivity implements View.OnClickListener {
    private static final long END = 0;
    private IAction action = null;
    public static final String TAG = "GuideActivity";
    private ImageView ivShow;
    private ImageView btnStartNav;
    private boolean isTip;
    private DaoSession daoSession;
    private AbstractSlamwarePlatform slamWarePlatform;
    private NavLocation mCurrentPositionInfo;
    /**
     * 数据库存储的位置点
     */
    private List<NavLocation> navLocationList;
    private MainHandler mHandler = new MainHandler(this);

    @Override
    protected int getActivityLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mHandler = new MainHandler(this);
        slamWarePlatform = Robot.getInstance().getSlamWarePlatform();
        if (slamWarePlatform == null) {
            ToastUtils.showShort("未实例化机器人");
            finish();
            return;
        }
        ivShow = findViewById(R.id.ivShow);
        btnStartNav = findViewById(R.id.btnStartNav);
        btnStartNav.setOnClickListener(this);
        initData();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartNav:
                TourCooLogUtil.i(TAG, "点击了");
                break;
            default:
                break;
        }
    }


    private void initData() {
        daoSession = ((MyApplication) getApplication()).getDaoSession();
        navLocationList = daoSession.getNavLocationDao().queryBuilder().list();
        if (navLocationList == null) {
            ToastUtils.showShort("数据库初始化异常");
            finish();
            return;
        }
        initRobotMap();
    }

    /**
     * 初始化机器人自动寻路需要的地图
     */
    private void initRobotMap() {
        ThreadPoolManager.getThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.i(TAG, "initRobotMap");
                String path = "/sdcard/robot/map.stcm";
                CompositeMapHelper compositeMapHelper = new CompositeMapHelper();
                CompositeMap compositeMap = compositeMapHelper.loadFile(path);
                String poseJson = SPUtils.getInstance().getString("pose");
                if (!TextUtils.isEmpty(poseJson) && compositeMap != null) {
                    Pose pose = new Gson().fromJson(poseJson, Pose.class);
                    try {
                        slamWarePlatform.setCompositeMap(compositeMap, pose);
                        //加载成功后 则获取后台配置的第一个点
                        TourCooLogUtil.i(TAG, "地图加载成功");
                        sendEmptyMsg(HomeConstant.MSG_START);
                    } catch (Exception e) {
                        ToastUtils.showShort("地图初始化异常 原因---->" + e.toString());
                        TourCooLogUtil.e(TAG, "地图初始化异常");
                    }
                    //加载地图和上次pose信息后
                } else {
                    ToastUtils.showShort("数据加载异常");
                }
            }
        });
    }


    private static class MainHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        private MainHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mWeakReference != null) {
                GuideActivity activity = (GuideActivity) mWeakReference.get();
                if (activity == null) {
                    ToastUtils.showShort("消息为null!");
                    return;
                }
                switch (msg.what) {
                    case HomeConstant.MSG_START:
                        activity.requestFirstLocation();
                        break;
                    case MSG_TOAST:
                        ToastUtils.showShort((String) msg.obj);
                        break;
                    case MSG_SHOW_LOADING:
                        activity.showLoading((String) msg.obj);
                        break;
                    case MSG_CLOSE_LOADING:
                        activity.closeLoading();
                    default:
                        break;
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }


    private void sendEmptyMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }

    /**
     * 请求网络 获取第一个讲解点的数据信息
     */
    @SuppressLint("CheckResult")
    private void requestFirstLocation() {
        showLoading("正在请求第一个位置点");
        setViewVisible(btnStartNav, false);
        RetrofitHelper.getInstance().getServer()
                .getLocationMessage(0)
                .compose(RxSchedulers.applySchedulers())
                .subscribe(result -> {
                    closeLoading();
                    switch (result.getStatus()) {
                        case REQUEST_ERROR:
                            ToastUtils.showShort(result.getErrmsg());
                            setViewVisible(btnStartNav, true);
                            break;
                        case REQUEST_SUCCESS:
                            TourCooLogUtil.i("第一个位置点", result.getData());
                            handleLocationTarget(result.getData());
                            break;
                        default:
                            break;
                    }
                }, throwable -> {
                    closeLoading();
                    ToastUtils.showShort("请求失败");
                    setViewVisible(btnStartNav, true);
                });
    }


    private void handleLocationTarget(LocationModel locationModel) {
        if (locationModel == null || locationModel.getLocal() == 0) {
            ToastUtils.showShort("目的地为null");
            return;
        }
        ThreadPoolManager.getThreadPoolProxy().execute(() -> {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 根据服务器返回的LocationModel中的当前id 去数据库寻找与之对应的NavLocation实体
                    mCurrentPositionInfo = daoSession.getNavLocationDao().load(locationModel.getLocal());
                    GlideManager.loadImg(locationModel.getThumb(), ivShow);
                }
            });

            if (mCurrentPositionInfo == null) {
                ToastUtils.showShort("未获取到位置信息");
                return;
            }
            speckTextSynthesis("倒计时5秒后开始寻路", false);
            try {
                Thread.sleep(5000);
                goToTheDestination(mCurrentPositionInfo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

    }


    /**
     * 前往 设置过的讲解点
     */
    private void goToTheDestination(NavLocation navLocation) {
        if (navLocation == null) {
            showToast("讲解点数据为空");
            return;
        }
        try {
            MoveOption moveOption = new MoveOption();
            //机器人移动的时候精确到点
            moveOption.setPrecise(true);
            moveOption.setMilestone(true);
            //先获取当前位置信息
            Pose currentPose = slamWarePlatform.getPose();
            //根据当前位置和传进来的NavLocation 构建一条虚拟路径line
            int segmentId = navLocation.getId().intValue();
            Line line = new Line(segmentId, currentPose.getX(), currentPose.getY(), navLocation.getX(), navLocation.getY());
            TourCooLogUtil.i("虚拟线路", line);
            //添加虚拟路径
            slamWarePlatform.addLine(ArtifactUsage.ArtifactUsageVirtualTrack, line);
            Location location = new Location(navLocation.getX(), navLocation.getY(), navLocation.getZ());
            //todo 后面传旋转角度
            action = slamWarePlatform.moveTo(location, moveOption, 0f);
            ActionStatus status = action.waitUntilDone();
            if (status == ActionStatus.FINISHED) {
                showToast("本次行走结束");
                return;
            } else {
                isTip = true;
                speckTextSynthesis("小哥哥小姐姐们，请不要挡住我的路好嘛，谢谢！", false);
                goToTheDestination(navLocation);
            }

        } catch (Exception e) {
            showToast("导航异常,异常原因--->" + e.toString());
        }
    }


    private void showToast(String text) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_TOAST;
        message.obj = text;
        mHandler.sendMessage(message);
    }


    private void showLoadingDialog(String text) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_SHOW_LOADING;
        message.obj = text;
        mHandler.sendMessage(message);
    }


    private void closeLoadingDialog(){
        Message message = mHandler.obtainMessage();
        message.what = MSG_CLOSE_LOADING;
        mHandler.sendMessage(message);
    }
}
