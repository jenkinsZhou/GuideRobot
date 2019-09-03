package cn.tklvyou.guiderobot.log.widget.parser;





import java.util.Collection;
import java.util.Iterator;

import cn.tklvyou.guiderobot.log.widget.utils.ObjectUtil;

/**
 * @author :zhoujian
 * @description : 集合解析器
 * @company :途酷科技
 * @date 2018年09月07日下午 03:09
 * @Email: 971613168@qq.com
 */
public class CollectionParse implements IParser<Collection> {
    @Override
    public Class<Collection> parseClassType() {
        return Collection.class;
    }

    @Override
    public String parseString(Collection collection) {
        String simpleName = collection.getClass().getName();
        String msg = "%s size = %d [" + LINE_SEPARATOR;
        msg = String.format(msg, simpleName, collection.size());
        if (!collection.isEmpty()) {
            Iterator<Object> iterator = collection.iterator();
            int flag = 0;
            while (iterator.hasNext()) {
                String itemString = "[%d]:%s%s";
                Object item = iterator.next();
                msg += String.format(itemString, flag, ObjectUtil.objectToString(item),
                        flag++ < collection.size() - 1 ? "," + LINE_SEPARATOR : LINE_SEPARATOR);
            }
        }
        return msg + "]";
    }
}
