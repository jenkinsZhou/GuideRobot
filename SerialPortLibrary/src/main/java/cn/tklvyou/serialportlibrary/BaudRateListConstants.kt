package cn.tklvyou.serialportlibrary

import java.util.*

/**
 * Created by Administrator on 2018/5/15.
 */
object BaudRateListConstants {

    private val list = ArrayList<Int>()

    val baudRateList: List<Int>
        get() {
            if (list.size <= 0) {
                list.add(50)
                list.add(75)
                list.add(110)
                list.add(134)
                list.add(150)
                list.add(200)
                list.add(300)
                list.add(600)
                list.add(1200)
                list.add(1800)
                list.add(2400)
                list.add(4800)
                list.add(9600)
                list.add(38400)
                list.add(57600)
                list.add(115200)
                list.add(230400)
                list.add(460800)
                list.add(500000)
                list.add(576000)
                list.add(921600)
                list.add(1000000)
                list.add(1152000)
                list.add(1500000)
                list.add(2000000)
                list.add(2500000)
                list.add(3000000)
                list.add(3500000)
                list.add(4000000)
            }
            return list
        }


}