package cn.tklvyou.guiderobot.log.widget.parser;

import android.os.Message;

import cn.tklvyou.guiderobot.log.widget.utils.ObjectUtil;


/**
 * @author :zhoujian
 * @description : Message解析器
 * @company :途酷科技
 * @date 2018年09月07日上午 10:47
 * @Email: 971613168@qq.com
 */
public class MessageParse implements IParser<Message> {
    @Override
    public Class<Message> parseClassType() {
        return Message.class;
    }

    @Override
    public String parseString(Message message) {
        if (message == null) {
            return "null";
        }
        StringBuilder stringBuilder = new StringBuilder(message.getClass().getName() + " [" + LINE_SEPARATOR);
        stringBuilder.append(String.format("%s = %s", "what", message.what)).append(LINE_SEPARATOR);
        stringBuilder.append(String.format("%s = %s", "when", message.getWhen())).append(LINE_SEPARATOR);
        stringBuilder.append(String.format("%s = %s", "arg1", message.arg1)).append(LINE_SEPARATOR);
        stringBuilder.append(String.format("%s = %s", "arg2", message.arg2)).append(LINE_SEPARATOR);
        stringBuilder.append(String.format("%s = %s", "data",
                new BundleParse().parseString(message.getData()))).append(LINE_SEPARATOR);
        stringBuilder.append(String.format("%s = %s", "obj",
                ObjectUtil.objectToString(message.obj))).append(LINE_SEPARATOR);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
