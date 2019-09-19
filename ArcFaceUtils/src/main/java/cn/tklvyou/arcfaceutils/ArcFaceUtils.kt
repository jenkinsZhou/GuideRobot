package cn.tklvyou.arcfaceutils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.widget.Toast
import cn.tklvyou.arcfaceutils.common.Constants
import cn.tklvyou.arcfaceutils.interfaces.IArcFacePeopleListener
import cn.tklvyou.arcfaceutils.interfaces.IArcFaceStatsuListener
import cn.tklvyou.arcfaceutils.util.ImageUtils
import com.arcsoft.face.*
import com.serenegiant.usb.IFrameCallback
import com.serenegiant.usb.Size
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList

public class ArcFaceUtils(context: Context, delayTime: Long = 10000) {

    private val TAG = "ArcFaceUtils"

    //activity 上下文
    private var mContext: Context = context

    //人脸追踪 结果返回间隔时间
    private val DELAY_TIME = delayTime

    //人脸检测引擎
    private var faceEngine: FaceEngine
    //人脸检测引擎初始化返回值
    private var afCode = -1

    private val mSync = Any()
    // for accessing USB and USB camera
    private var mUSBMonitor: USBMonitor? = null
    private var mUVCCamera: UVCCamera? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mPreviewTexture: Surface? = null

    private var bitmap: Bitmap? = null
    private var previewSize: Size? = null


    private var lastTime = 0L

    @SuppressLint("HandlerLeak")
    private var handler: Handler

    //是否已初始化引擎
    private var isStart = false

    init {
        faceEngine = FaceEngine()
        handler = Handler()
    }


    /**
     * 激活引擎
     *
     * @param view
     */
    public fun activeEngine(listener: IArcFaceStatsuListener?) {
        Observable.create(ObservableOnSubscribe<Int> { emitter ->
            val activeCode = faceEngine.activeOnline(mContext, Constants.APP_ID, Constants.SDK_KEY)
            emitter.onNext(activeCode)
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Int> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(activeCode: Int) {
                        if (activeCode == ErrorInfo.MOK || activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            listener?.activeStatus(true)
                            Log.d(TAG, "arcface active success")
                        } else {
                            listener?.activeStatus(false)
                            Log.d(TAG, "arcface active failure")
                        }
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {

                    }
                })

    }


    public fun initUSBMonitor(previewView: SurfaceView) {
        initEngine()

        mUSBMonitor = USBMonitor(mContext, mOnDeviceConnectListener)//创建

        mPreviewTexture = previewView.holder.surface
//        previewView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
//            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
//                mSurfaceTexture = surface
//                Log.i(TAG, "onSurfaceTextureAvailable:==$mSurfaceTexture")
//            }
//
//            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
//                Log.i(TAG, "onSurfaceTextureSizeChanged :  width==" + width + "height=" + height)
//            }
//
//            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
//                Log.i(TAG, "onSurfaceTextureDestroyed :  ")
//                if (mUVCCamera != null) {
//                    mUVCCamera!!.stopPreview()
//                }
//                mSurfaceTexture = null
//                return true
//            }
//
//            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//                Log.i(TAG, "onSurfaceTextureUpdated :  ")
//            }
//        }

        //注意此处的注册和反注册  注册后会有相机usb设备的回调
        synchronized(mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor!!.register()
            }
        }

    }

//    public fun initUSBMonitor(previewView: TextureView) {
//        initEngine()
//
//        mUSBMonitor = USBMonitor(mContext, mOnDeviceConnectListener)//创建
//
//        previewView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
//            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
//                mSurfaceTexture = surface
//                Log.i(TAG, "onSurfaceTextureAvailable:==$mSurfaceTexture")
//            }
//
//            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
//                Log.i(TAG, "onSurfaceTextureSizeChanged :  width==" + width + "height=" + height)
//            }
//
//            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
//                Log.i(TAG, "onSurfaceTextureDestroyed :  ")
//                if (mUVCCamera != null) {
//                    mUVCCamera!!.stopPreview()
//                }
//                mSurfaceTexture = null
//                return true
//            }
//
//            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//                Log.i(TAG, "onSurfaceTextureUpdated :  ")
//            }
//        }
//
//        //注意此处的注册和反注册  注册后会有相机usb设备的回调
//        synchronized(mSync) {
//            if (mUSBMonitor != null) {
//                mUSBMonitor!!.register()
//            }
//        }
//
//    }

    public fun initEngine() {
        if (!isStart) {
            faceEngine = FaceEngine()
            afCode = faceEngine.init(
                    mContext,
                    FaceEngine.ASF_DETECT_MODE_VIDEO,
                    FaceEngine.ASF_OP_0_HIGHER_EXT,
                    16,
                    20,
                    FaceEngine.ASF_FACE_DETECT
            )
            val versionInfo = VersionInfo()
            faceEngine.getVersion(versionInfo)
            Log.i(TAG, "initEngine:  init: $afCode  version:$versionInfo")
            if (afCode != ErrorInfo.MOK) {
                isStart = false
                Toast.makeText(mContext, "初始化引擎失败：$afCode", Toast.LENGTH_SHORT).show()
            } else {
                isStart = true
            }
        }
    }

    public fun unInitEngine() {
        if (afCode == 0) {
            isStart = false
            afCode = faceEngine.unInit()
            Log.i(TAG, "unInitEngine: $afCode")
        }
    }


    private val mOnDeviceConnectListener = object : USBMonitor.OnDeviceConnectListener {
        override fun onAttach(device: UsbDevice) {
            Log.v(TAG, "onAttach:")
            if (device.deviceClass == 239 && device.deviceSubclass == 2) {
                mUSBMonitor!!.requestPermission(device)
            }
        }

        override fun onConnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean) {
            Log.v(TAG, "onConnect:")
            synchronized(mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera!!.destroy()
                }
            }
            handler.post(Runnable {
                synchronized(mSync) {
                    val camera = UVCCamera()
                    Log.v(TAG, "创建相机完成时间:" + System.currentTimeMillis())

                    camera.open(ctrlBlock)
                    Log.i(TAG, "supportedSize:" + camera.supportedSize)
                    try {
                        //设置预览尺寸 根据设备自行设置
                        camera.setPreviewSize(
                                UVCCamera.DEFAULT_PREVIEW_WIDTH,
                                UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                                UVCCamera.FRAME_FORMAT_MJPEG
                        )
                    } catch (e: IllegalArgumentException) {
                        try {
                            // fallback to YUV mode
                            //设置预览尺寸 根据设备自行设置
                            camera.setPreviewSize(
                                    UVCCamera.DEFAULT_PREVIEW_WIDTH,
                                    UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                                    UVCCamera.DEFAULT_PREVIEW_MODE
                            )
                        } catch (e1: IllegalArgumentException) {
                            camera.destroy()
                        }
                    }

                    Log.e(TAG, "isNull  : "+(mSurfaceTexture == null))
//                    mSurfaceTexture = mUVCCameraView.getHolder().getSurface();//使用Surfaceview的接口
                    if (mSurfaceTexture == null) {
                        camera.setPreviewDisplay(mPreviewTexture);//使用Surfaceview的接口
//                        camera.setPreviewTexture(mSurfaceTexture)
                        val time = System.currentTimeMillis()
                        Log.v(TAG, "设置相机参数准备启动预览时间:$time")
                        camera.startPreview()
                        val endTime = System.currentTimeMillis()
                        val costTime = endTime - time
                        Log.i(TAG, "设置相机参数准备启动预览完成时间:$costTime")
                        val supportedSizeList = camera.supportedSizeList
                        println("size个数" + supportedSizeList.size)
                        for (s in supportedSizeList) {
                            println("size=" + s.width + "***" + s.height)
                        }
                        camera.setFrameCallback(iFrameCallback, UVCCamera.PIXEL_FORMAT_RGB565)//设置回调 和回调数据类型
                        //                            设置预览尺寸 根据设备自行设置
                        camera.setPreviewSize(640, 480)
                        previewSize = camera.previewSize
                        Log.e(TAG, "run: size " + previewSize!!.width + "          " + previewSize!!.height)

                    }
                    synchronized(mSync) {
                        mUVCCamera = camera
                    }
                }
            })
        }

        override fun onDisconnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock) {
            Log.v(TAG, "onDisconnect:")
            // XXX you should check whether the comming device equal to camera device that currently using
            handler.postDelayed(Runnable {
                synchronized(mSync) {
                    if (mUVCCamera != null) {
                        mUVCCamera!!.close()
                    }
                }
            }, 0)
        }

        override fun onDettach(device: UsbDevice) {
            Log.v(TAG, "onDettach:")
        }

        override fun onCancel(device: UsbDevice) {}
    }


    private var peopleListener: IArcFacePeopleListener? = null
    public fun setIArcFacePeopleListener(listener: IArcFacePeopleListener) {
        peopleListener = listener
    }

    val faceInfoList = ArrayList<FaceInfo>()
    private var iFrameCallback: IFrameCallback = IFrameCallback { byteBuffer ->
        byteBuffer.clear()
        faceInfoList.clear()

        bitmap = Bitmap.createBitmap(
                UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                Bitmap.Config.RGB_565
        )

        bitmap!!.copyPixelsFromBuffer(byteBuffer)

        val data =
                ImageUtils.getNV21(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, bitmap)//nv21 数据


        val code = faceEngine.detectFaces(
                data,
                previewSize!!.width,
                previewSize!!.height,
                FaceEngine.CP_PAF_NV21,
                faceInfoList
        )

        val currentTime = System.currentTimeMillis()

        if (code == ErrorInfo.MOK && faceInfoList.size > 0) {
            if (currentTime - lastTime >= DELAY_TIME) {
                lastTime = currentTime
                peopleListener?.onPeopleAround()

            }
            Log.e(TAG, "faceInfoList.size  :" + faceInfoList.size)
        }
    }


    public fun onDestroy() {
        synchronized(mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor!!.unregister()
            }

            if (mUVCCamera != null) {
                mUVCCamera!!.destroy()
                mUVCCamera!!.close()
                mUVCCamera = null
            }
            if (mUSBMonitor != null) {
                mUSBMonitor!!.destroy()
                mUSBMonitor = null
            }
        }

        unInitEngine()
    }


}
