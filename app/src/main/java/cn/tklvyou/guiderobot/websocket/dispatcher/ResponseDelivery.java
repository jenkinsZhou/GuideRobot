package cn.tklvyou.guiderobot.websocket.dispatcher;


import cn.tklvyou.guiderobot.websocket.SocketListener;

/**
 * 消息发射器接口
 *
 * Created by ZhangKe on 2018/6/26.
 */
public interface ResponseDelivery extends SocketListener {

    void addListener(SocketListener listener);

    void removeListener(SocketListener listener);

    void clear();

    boolean isEmpty();
}
