package com.example.studentdetail;

public class details {
    private String Name, Roll, DOB, Dept, Year, stay;

    public details(String name, String stay, String year, String dept, String DOB, String roll) {
        this.Name = name;
        this.stay = stay;
        this.Year = year;
        this.Dept = dept;
        this.DOB = DOB;
        this.Roll = roll;
    }

    public String getName() {
        return Name;
    }

    public String getRoll() {
        return Roll;
    }

    public String getDOB() {
        return DOB;
    }

    public String getDept() {
        return Dept;
    }

    public String getYear() {
        return Year;
    }

    public String getStay() {
        return stay;
    }
}
