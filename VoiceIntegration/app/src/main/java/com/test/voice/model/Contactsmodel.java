package com.test.voice.model;

/**
 * Created by Madhura Nahar.
 */
public class Contactsmodel {
    private String _id;
    private String _name = "null";
    private String firstName = "null";
    private String lastName = "null";
    private String mobile_num = "null";
    private String home_num = "null";
    private String work_num = "null";
    private String workMobile_num = "null";
    private String other_num = "null";
    private Boolean isSoundex=false;

    public Contactsmodel() {
    }

    public Contactsmodel(String _id, String _name, String firstName, String lastName, String mobile_num, String home_num, String work_num, String workMobile_num, String other_num) {
        this._id = _id;
        this._name = _name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile_num = mobile_num;
        this.home_num = home_num;
        this.work_num = work_num;
        this.workMobile_num = workMobile_num;
        this.other_num = other_num;
    }


    public String getID() {
        return this._id;
    }

    public void setID(String id) {
        this._id = id.trim();
    }

    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName != null) {
            firstName = firstName.trim();
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName != null) {
            lastName = lastName.trim();
        }
        this.lastName = lastName;
    }

    public String getMobile_num() {
        return mobile_num;
    }

    public void setMobile_num(String mobile_num) {
        if (mobile_num != null)
            this.mobile_num = mobile_num.trim();
            this.mobile_num = this.mobile_num.replace(" ", "");
            this.mobile_num = this.mobile_num.replace("-", "");
    }

    public String getHome_num() {
        return home_num;
    }

    public void setHome_num(String home_num) {
        if (home_num != null)
            this.home_num = home_num.trim();
            this.home_num = this.home_num.replace(" ", "");
            this.home_num = this.home_num.replace("-", "");
    }

    public String getWork_num() {
        return work_num;
    }

    public void setWork_num(String work_num) {
        if (work_num != null)
            this.work_num = work_num.trim();
            this.work_num = this.work_num.replace(" ", "");
            this.work_num = this.work_num.replace("-", "");
    }

    public String getWorkMobile_num() {
        return workMobile_num;
    }

    public void setWorkMobile_num(String workMobile_num) {
        if (workMobile_num != null)
            this.workMobile_num = workMobile_num.trim();
            this.workMobile_num = this.workMobile_num.replace(" ", "");
            this.workMobile_num = this.workMobile_num.replace("-", "");
    }

    public String getOther_num() {
        return other_num;
    }

    public void setOther_num(String other_num) {
        if (other_num != null)
            this.other_num = other_num.trim();
            this.other_num = this.other_num.replace(" ", "");
            this.other_num = this.other_num.replace("-", "");
    }

    public Boolean getSoundex() {
        return isSoundex;
    }

    public void setSoundex(Boolean soundex) {
        isSoundex = soundex;
    }
}
