package com.apigcc.example.jaxrs;

/**
 * 学生信息实体
 * @email eumji025@gmail.com
 * @author:EumJi
 * @date: 2019/3/23
 * @time: 14:18
 */
public class Student {
    /**
     * 学生id
     */
    private int id;
    /**
     * 学生姓名
     */
    private String name;
    /**
     * 性别 1.男性 2.女性
     */
    private int sex;
    /**
     * 班级id
     */
    private int clsId;
    /**
     * 年龄
     */
    private int age;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getSex() {
        return sex;
    }
    public void setSex(int sex) {
        this.sex = sex;
    }
    public int getClsId() {
        return clsId;
    }
    public void setClsId(int clsId) {
        this.clsId = clsId;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
}
