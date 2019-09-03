package cn.tklvyou.guiderobot.log.widget.parser;





import java.util.Map;
import java.util.Set;

import cn.tklvyou.guiderobot.log.widget.utils.ObjectUtil;

/**
 * @author :zhoujian
 * @description : Map解析器
 * @company :途酷科技
 * @date 2018年09月07日上午 10:45
 * @Email: 971613168@qq.com
 */

public class MapParse implements IParser<Map> {
    @Override
    public Class<Map> parseClassType() {
        return Map.class;
    }

    @Override
    public String parseString(Map map) {
        String msg = map.getClass().getName() + " [" + LINE_SEPARATOR;
        Set<Object> keys = map.keySet();
        for (Object key : keys) {
            String itemString = "%s -> %s" + LINE_SEPARATOR;
            Object value = map.get(key);
            if (value != null) {
                if (value instanceof String) {
                    value = "\"" + value + "\"";
                } else if (value instanceof Character) {
                    value = "\'" + value + "\'";
                }
            }
            msg += String.format(itemString, ObjectUtil.objectToString(key),
                    ObjectUtil.objectToString(value));
        }
        return msg + "]";
    }
}
