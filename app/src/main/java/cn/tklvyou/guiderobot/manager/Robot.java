package cn.tklvyou.guiderobot.manager;

import com.slamtec.slamware.AbstractSlamwarePlatform;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2019年09月05日14:23
 * @Email: 971613168@qq.com
 */
public class Robot {
    private AbstractSlamwarePlatform slamWarePlatform;
    private static class SingletonInstance {
        private static final Robot INSTANCE = new Robot();
    }

    public static Robot getInstance() {
        return SingletonInstance.INSTANCE;
    }


    public AbstractSlamwarePlatform getSlamWarePlatform() {
        return slamWarePlatform;
    }

    public void setSlamWarePlatform(AbstractSlamwarePlatform slamWarePlatform) {
        this.slamWarePlatform = slamWarePlatform;
    }
}
