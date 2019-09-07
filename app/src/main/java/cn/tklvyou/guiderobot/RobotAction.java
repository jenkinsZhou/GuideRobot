package cn.tklvyou.guiderobot;

import java.util.HashMap;
import java.util.Map;

import cn.tklvyou.guiderobot.model.ControllerModel;
import cn.tklvyou.guiderobot.utils.HexUtils;

/**
 * Created by Administrator on 2019/4/25.
 */

public class RobotAction {

    public static final String TYPE_TEXT = "text";

    public static final String TYPE_ACTION = "action";
    //手臂动作指令
    public static final int LEFT_RIGHT_HANDS_ACTION = 1;

    //头部LED灯指令
    public static final int HEAD_LED_ACTION = 2;

    //身体转动
    public static final int HANDED_ROTATION_ACTION = 3;
    //延时
    public static final int DELAY_ACTION = 4;

    //等待1.5秒执行下一个动作
    public static final String AWAIT_TIME = "ACTION_AWAIT_TIME";

    //启动首页
    public static final String SCREEN_START = "ACTION_SCREEN_START";


    //头部LED灯常亮
    public static final String HEAD_LED_LIGHT = "ACTION_HEAD_LED_LIGHT";

    //头部LED灯闪烁
    public static final String HEAD_LED_BREATHE = "ACTION_HEAD_LED_BREATHE";

    //头部LED灯熄灭
    public static final String HEAD_LED_CLOSE = "ACTION_HEAD_LED_CLOSE";


    //左手举起
    public static final String LEFT_HANDS_UP = "ACTION_LEFT_HANDS_UP";

    //左手朝前
    public static final String LEFT_HANDS_FRONT = "ACTION_LEFT_HANDS_FRONT";

    //左手放下
    public static final String LEFT_HANDS_DOWN = "ACTION_LEFT_HANDS_DOWN";

    //右手举起
    public static final String RIGHT_HANDS_UP = "ACTION_RIGHT_HANDS_UP";

    //右手朝前
    public static final String RIGHT_HANDS_FRONT = "ACTION_RIGHT_HANDS_FRONT";

    //右手放下
    public static final String RIGHT_HANDS_DOWN = "ACTION_RIGHT_HANDS_DOWN";


    //身体左转30度
    public static final String ACTION_ROTATION_TURN_LEFT_30 = "ACTION_ROTATION_TURN_LEFT_30";

    //身体右转30度
    public static final String ACTION_ROTATION_TURN_RIGHT_30 = "ACTION_ROTATION_TURN_RIGHT_30";

    //身体左转45度
    public static final String ACTION_ROTATION_TURN_LEFT_45 = "ACTION_ROTATION_TURN_LEFT_45";

    //身体右转45度
    public static final String ACTION_ROTATION_TURN_RIGHT_45 = "ACTION_ROTATION_TURN_RIGHT_45";

    //身体左转90度
    public static final String ACTION_ROTATION_TURN_LEFT_90 = "ACTION_ROTATION_TURN_LEFT_90";

    //身体右转90度
    public static final String ACTION_ROTATION_TURN_RIGHT_90 = "ACTION_ROTATION_TURN_RIGHT_90";

    public static ControllerModel getControllerCommand(String action) {
        boolean isRotationAction = ACTION_ROTATION_TURN_LEFT_30.equalsIgnoreCase(action) || ACTION_ROTATION_TURN_RIGHT_30.equalsIgnoreCase(action) ||
                ACTION_ROTATION_TURN_LEFT_45.equalsIgnoreCase(action) || ACTION_ROTATION_TURN_RIGHT_45.equalsIgnoreCase(action) || ACTION_ROTATION_TURN_LEFT_90.equalsIgnoreCase(action) ||
                ACTION_ROTATION_TURN_RIGHT_90.equalsIgnoreCase(action);
        if (action.equals(LEFT_HANDS_UP) || action.equals(LEFT_HANDS_FRONT) || action.equals(LEFT_HANDS_DOWN)
                || action.equals(RIGHT_HANDS_UP) || action.equals(RIGHT_HANDS_FRONT) || action.equals(RIGHT_HANDS_DOWN)) {
            byte[] params = getHandsCommand(action);
            return new ControllerModel(LEFT_RIGHT_HANDS_ACTION, params);
        } else if (action.equals(HEAD_LED_LIGHT) || action.equals(HEAD_LED_BREATHE) || action.equals(HEAD_LED_CLOSE)) {
            return new ControllerModel(HEAD_LED_ACTION, getHeadLedCommand(action));
        } else if (isRotationAction) {
            //身体左转或右转
            return new ControllerModel(HANDED_ROTATION_ACTION, action);
        } else if (action.equalsIgnoreCase(AWAIT_TIME)) {
            return new ControllerModel(DELAY_ACTION, action);
        } else {
            return new ControllerModel(-1, null);
        }

    }


    /**
     * 获取左右手电机转动的通信协议
     * <p>
     * 数据包格式如下：
     * <p>
     * 帧头：55 55
     * ID号：01（左手）  02（右手）  03（头部）
     * 数据长度
     * 指令
     * 参数
     * 校验和
     *
     * @param handsAction
     * @return
     */
    private static byte[] getHandsCommand(String handsAction) {
        if (handsAction.equals(LEFT_HANDS_UP)) {
            return new byte[]{0x55, 0x55, 0x01, 0x07, 0x01, (byte) 0xE8, 0x03, (byte) 0xDC, 0x05, 0x2A};
        } else if (handsAction.equals(LEFT_HANDS_FRONT)) {
            String check = HexUtils.makeCheckSum("010701F401DC05");
            return new byte[]{0x55, 0x55, 0x01, 0x07, 0x01, (byte) 0xF4, 0x01, (byte) 0xDC, 0x05, HexUtils.parseHexStr2Byte(check)};
        } else if (handsAction.equals(LEFT_HANDS_DOWN)) {
            return new byte[]{0x55, 0x55, 0x01, 0x07, 0x01, 0x00, 0x00, (byte) 0xDC, 0x05, 0x15};
        } else if (handsAction.equals(RIGHT_HANDS_UP)) {
            String check = HexUtils.makeCheckSum("020701E803DC05");
            return new byte[]{0x55, 0x55, 0x02, 0x07, 0x01, (byte) 0xE8, 0x03, (byte) 0xDC, 0x05, HexUtils.parseHexStr2Byte(check)};
        } else if (handsAction.equals(RIGHT_HANDS_FRONT)) {
            String check = HexUtils.makeCheckSum("020701F401DC05");
            return new byte[]{0x55, 0x55, 0x02, 0x07, 0x01, (byte) 0xF4, 0x01, (byte) 0xDC, 0x05, HexUtils.parseHexStr2Byte(check)};
        } else if (handsAction.equals(RIGHT_HANDS_DOWN)) {
            String check = HexUtils.makeCheckSum("0207010000DC05");
            return new byte[]{0x55, 0x55, 0x02, 0x07, 0x01, 0x00, 0x00, (byte) 0xDC, 0x05, HexUtils.parseHexStr2Byte(check)};
        } else {
            return new byte[]{};
        }
    }


    /**
     * 获取头部LED灯带的通信协议
     *
     * @param action
     * @return
     */
    private static byte[] getHeadLedCommand(String action) {
        if (action.equals(HEAD_LED_LIGHT)) {
            return new byte[]{0x55, 0x01, 0x01, 0x02, 0x00, 0x00, 0x00, 0x59};
        } else if (action.equals(HEAD_LED_CLOSE)) {
            return new byte[]{0x55, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x58};
        } else {
            return new byte[]{};
        }
    }


}
