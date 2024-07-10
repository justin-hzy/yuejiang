package com.engine.interfaces.tx.field.entity;

/**
 * FileName: FieldSignEntity.java
 * 外勤打卡实体类
 *
 * @Author tx
 * @Date 2022/12/20
 * @Version 1.00
 **/
public class FieldSignEntity {
    private String id;//唯一标识
    private String user;//打卡人员
    private String dep;//打卡部门
    private String sub;//打卡分部
    private String dateTime;//打卡日期时间
    private String visitMethod;//拜访方式
    private String cus;//打卡客户
    private String newCus;//打卡新客户
    private String region;//大区
    private String province;//省
    private String city;//市
    private String address;//打卡位置
    private double lng;//打卡经度
    private double lat;//打卡纬度
    private String photo;//打卡照片
    private String status;//打卡状态
    private String checkType;//打卡类型
    private String item01;//备用字段1

    public FieldSignEntity(){

    }

    public FieldSignEntity(String id, String user, String dep, String sub, String dateTime, String visitMethod, String cus, String newCus, String region, String province, String city, String address, double lng, double lat, String photo, String status, String checkType) {
        this.id = id;
        this.user = user;
        this.dep = dep;
        this.sub = sub;
        this.dateTime = dateTime;
        this.visitMethod = visitMethod;
        this.cus = cus;
        this.newCus = newCus;
        this.region = region;
        this.province = province;
        this.city = city;
        this.address = address;
        this.lng = lng;
        this.lat = lat;
        this.photo = photo;
        this.status = status;
        this.checkType = checkType;
    }

    public FieldSignEntity(String id, String user, String dep, String sub, String dateTime, String visitMethod, String cus, String newCus, String region, String province, String city, String address, double lng, double lat, String photo, String status, String checkType, String item01) {
        this.id = id;
        this.user = user;
        this.dep = dep;
        this.sub = sub;
        this.dateTime = dateTime;
        this.visitMethod = visitMethod;
        this.cus = cus;
        this.newCus = newCus;
        this.region = region;
        this.province = province;
        this.city = city;
        this.address = address;
        this.lng = lng;
        this.lat = lat;
        this.photo = photo;
        this.status = status;
        this.checkType = checkType;
        this.item01 = item01;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDep() {
        return dep;
    }

    public void setDep(String dep) {
        this.dep = dep;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getVisitMethod() {
        return visitMethod;
    }

    public void setVisitMethod(String visitMethod) {
        this.visitMethod = visitMethod;
    }

    public String getCus() {
        return cus;
    }

    public void setCus(String cus) {
        this.cus = cus;
    }

    public String getNewCus() {
        return newCus;
    }

    public void setNewCus(String newCus) {
        this.newCus = newCus;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getItem01() {
        return item01;
    }

    public void setItem01(String item01) {
        this.item01 = item01;
    }

    @Override
    public String toString() {
        return "FieldSignEntity{" +
                "id='" + id + '\'' +
                ", user='" + user + '\'' +
                ", dep='" + dep + '\'' +
                ", sub='" + sub + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", visitMethod='" + visitMethod + '\'' +
                ", cus='" + cus + '\'' +
                ", newCus='" + newCus + '\'' +
                ", region='" + region + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", lng=" + lng +
                ", lat=" + lat +
                ", photo='" + photo + '\'' +
                ", status='" + status + '\'' +
                ", checkType='" + checkType + '\'' +
                ", item01='" + item01 + '\'' +
                '}';
    }
}
