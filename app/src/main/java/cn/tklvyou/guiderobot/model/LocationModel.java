package cn.tklvyou.guiderobot.model;

import java.util.List;

public class LocationModel {


    /**
     * content : [{"type":"text","value":"大家好，欢迎来到铜川馆，我是特邀讲解员童童。"},{"type":"text","value":"新动能，新经济，新形象，新铜川。现在，就请跟随我一起来领略一下\u201c渭北明珠\u201d\u2014\u2014五色铜川的风采吧！"}]
     * local : 1
     * next : 9
     * thumb : http://robot.tklvyou.cn/uploads/admin/article_thumb/20190508/0e304f2344e96133248307bfd347ae63.jpg
     */
    /**
     * 当前位置信息id
     */
    private long local;
    /**
     * 下一个位置信息的id
     */
    private long next;
    private String thumb;
    private List<ContentBean> content;

    public long getLocal() {
        return local;
    }

    public void setLocal(long local) {
        this.local = local;
    }

    public long getNext() {
        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public List<ContentBean> getContent() {
        return content;
    }

    public void setContent(List<ContentBean> content) {
        this.content = content;
    }

    public static class ContentBean {
        /**
         * type : text
         * value : 大家好，欢迎来到铜川馆，我是特邀讲解员童童。
         */

        private String type;
        private String value;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
