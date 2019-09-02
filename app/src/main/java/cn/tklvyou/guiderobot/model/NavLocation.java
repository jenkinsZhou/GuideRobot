package cn.tklvyou.guiderobot.model;

import com.blankj.utilcode.util.LogUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.util.List;


@Entity
public class NavLocation {

    @Id(autoincrement = false)
    private Long id;
    private String name;
    private float x;
    private float y;
    private float z;
    private float rotation;
    private String content;
    @Generated(hash = 365566208)
    public NavLocation(Long id, String name, float x, float y, float z,
            float rotation, String content) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
        this.content = content;
    }
    @Generated(hash = 307033452)
    public NavLocation() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public float getX() {
        return this.x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return this.y;
    }
    public void setY(float y) {
        this.y = y;
    }
    public float getZ() {
        return this.z;
    }
    public void setZ(float z) {
        this.z = z;
    }
    public float getRotation() {
        return this.rotation;
    }
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }

}
