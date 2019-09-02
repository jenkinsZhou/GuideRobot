//package cn.tklvyou.guiderobot.ui
//
//import android.app.Service
//import android.content.Context
//import android.graphics.Color
//import android.os.*
//import android.view.*
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.ItemTouchHelper
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//
//import cn.tklvyou.guiderobot.R
//import cn.tklvyou.guiderobot.base.BaseActivity
//import cn.tklvyou.guiderobot.base.MyApplication
//import cn.tklvyou.guiderobot.model.DaoSession
//import cn.tklvyou.guiderobot.model.NavLocation
//import cn.tklvyou.guiderobot.model.NavLocationDao
//import cn.tklvyou.guiderobot.utils.RecycleViewDivider
//import com.blankj.utilcode.util.LogUtils
//import com.blankj.utilcode.util.ToastUtils
//import com.google.gson.Gson
//import com.wuhenzhizao.titlebar.widget.CommonTitleBar
//
//import kotlinx.android.synthetic.main.activity_setting_nav_order.*
//import java.util.*
//
///**
// * 配置导航路径排序
// */
//class SettingNavOrderActivity : BaseActivity() {
//
//
//    override fun getActivityLayoutID(): Int {
//        return R.layout.activity_setting_nav_order
//    }
//
//
//    private var ap: MyAdapter? = null
//    private var datas: MutableList<NavLocation>? = null
//    private var originDatas: MutableList<NavLocation>? = null
//
//    internal var helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
//
//        override fun isLongPressDragEnabled(): Boolean {
//            return true
//        }
//
//        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
//            var dragFrlg = 0
//            if (recyclerView.getLayoutManager() is GridLayoutManager) {
//                dragFrlg = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//            } else if (recyclerView.getLayoutManager() is LinearLayoutManager) {
//                dragFrlg = ItemTouchHelper.UP or ItemTouchHelper.DOWN
//            }
//            return makeMovementFlags(dragFrlg, 0)
//        }
//
//        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
//            //滑动事件  下面注释的代码，滑动后数据和条目错乱，被舍弃
//            //            Collections.swap(datas,viewHolder.getAdapterPosition(),target.getAdapterPosition());
//            //            ap.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
//
//            //得到当拖拽的viewHolder的Position
//            val fromPosition = viewHolder.getAdapterPosition()
//            //拿到当前拖拽到的item的viewHolder
//            val toPosition = target.getAdapterPosition()
//            if (fromPosition < toPosition) {
//                for (i in fromPosition until toPosition) {
//                    Collections.swap(datas, i, i + 1)
//                }
//            } else {
//                for (i in fromPosition downTo toPosition + 1) {
//                    Collections.swap(datas, i, i - 1)
//                }
//            }
//            ap!!.notifyItemMoved(fromPosition, toPosition)
//            return true
//        }
//
//        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//            //侧滑删除可以使用；
//        }
//
//        /**
//         * 长按选中Item的时候开始调用
//         * 长按高亮
//         * @param viewHolder
//         * @param actionState
//         */
//        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
//            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//                viewHolder!!.itemView.setBackgroundColor(Color.RED)
//                //获取系统震动服务//震动70毫秒
//                val vib = getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
//                vib.vibrate(70)
//            }
//            super.onSelectedChanged(viewHolder, actionState)
//        }
//
//        /**
//         * 手指松开的时候还原高亮
//         * @param recyclerView
//         * @param viewHolder
//         */
//        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
//            super.clearView(recyclerView, viewHolder)
//            viewHolder.itemView.setBackgroundColor(0)
//            ap!!.notifyDataSetChanged()  //完成拖动后刷新适配器，这样拖动后删除就不会错乱
//        }
//    })
//
//    override fun initView() {
//        titleBar.setListener { v, action, extra ->
//            // CommonTitleBar.ACTION_LEFT_TEXT;        // 左边TextView被点击
//            // CommonTitleBar.ACTION_LEFT_BUTTON;      // 左边ImageBtn被点击
//            // CommonTitleBar.ACTION_RIGHT_TEXT;       // 右边TextView被点击
//            // CommonTitleBar.ACTION_RIGHT_BUTTON;     // 右边ImageBtn被点击
//            // CommonTitleBar.ACTION_SEARCH;           // 搜索框被点击,搜索框不可输入的状态下会被触发
//            // CommonTitleBar.ACTION_SEARCH_SUBMIT;    // 搜索框输入状态下,键盘提交触发，此时，extra为输入内容
//            // CommonTitleBar.ACTION_SEARCH_VOICE;     // 语音按钮被点击
//            // CommonTitleBar.ACTION_SEARCH_DELETE;    // 搜索删除按钮被点击
//            // CommonTitleBar.ACTION_CENTER_TEXT;      // 中间文字点击
//
//            when (action) {
//                CommonTitleBar.ACTION_LEFT_BUTTON -> {
//                    finish()
//                }
//
//                CommonTitleBar.ACTION_RIGHT_TEXT -> {
//
//                    val changeList = ap!!.getData()
//
//                    originDatas!!.forEach {
//                        it.isSelect = false
//                        it.order = 0
//                    }
//
//                    for (i in 0 until originDatas!!.size) {
//                        for (j in 0 until changeList.size) {
//                            if (changeList[j].id == originDatas!![i].id) {
//                                originDatas!![i].isSelect = true
//                                originDatas!![i].order = changeList.size - j
//                                break
//                            }
//                        }
//                    }
//
//                    LogUtils.e(Gson().toJson(originDatas))
//                    try {
//                        daoSession!!.navLocationDao.updateInTx(originDatas)
//                    }catch (e:Exception){
//                        e.printStackTrace()
//                    }
//
//                }
//
//            }
//
//        }
//
//        initData()
//        rv.layoutManager = LinearLayoutManager(this)
//        rv.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL))
//        ap = MyAdapter(this, datas!!)
//        rv!!.adapter = ap
//        helper.attachToRecyclerView(rv)
//    }
//
//    private var daoSession:DaoSession ? = null
//    private fun initData() {
//        daoSession = (application as MyApplication).getDaoSession()
//        datas = daoSession!!.navLocationDao.queryBuilder().where(NavLocationDao.Properties.IsSelect.eq(true)).list()
//        originDatas = datas
//    }
//
//    internal inner class MyAdapter(private val context: Context, var navList: MutableList<NavLocation>) : RecyclerView.Adapter<MyAdapter.Vh>() {
//
//        override fun getItemCount(): Int {
//            return navList.size
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.Vh {
//            return Vh(LayoutInflater.from(context).inflate(R.layout.item_rv, null))
//        }
//
//        override fun onBindViewHolder(holder: MyAdapter.Vh, position: Int) {
//            holder.tv.text = navList[position].name
//            holder.iv.setOnClickListener { remove(position) }
//            holder.itemView.setOnClickListener {
//                ToastUtils.showShort("" + position)
//                LogUtils.e(navList.get(position).name)
//            }
//        }
//
//        fun getData(): MutableList<NavLocation> {
//            return navList
//        }
//
//        fun add(item: NavLocation) {
//            val position = navList.size
//            navList.add(item)
//            notifyItemInserted(position)
//        }
//
//        fun add(position: Int, item: NavLocation) {
//            navList.add(position, item)
//            notifyItemInserted(position)
//        }
//
//
//        fun remove(position: Int) {
//            navList.removeAt(position)
//            notifyItemRemoved(position)
//            notifyItemRangeChanged(position, navList.size)
//        }
//
//        internal inner class Vh(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            var tv: TextView
//            var iv: ImageView
//
//            init {
//                tv = itemView.findViewById(R.id.tv)
//                iv = itemView.findViewById(R.id.iv_delete)
//            }
//        }
//    }
//
//
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            finish()
//        }
//        return super.onKeyDown(keyCode, event)
//    }
//
//}
