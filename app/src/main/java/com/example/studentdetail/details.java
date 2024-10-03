package com.example.studentdetail;

public class details {
    private String Name,Roll, DOB, Dept, Year, stay, Bus_no, Room_no, Address ,Phone,Cgpa ;

    public details(String cgpa) {
        Cgpa = cgpa;
    }

    public details(String name, String roll, String DOB, String dept, String year, String stay, String bus_no, String room_no, String address, String phone) {
        this.Name = name;
        this.Roll = roll;
        this.DOB = DOB;
        this.Dept = dept;
        this.Year = year;
        this.stay = stay;
        this.Bus_no = bus_no;
        this.Room_no = room_no;
        this.Address = address;
        this.Phone = phone;
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

    public String getBus_no() {
        return Bus_no;
    }

    public String getRoom_no() {
        return Room_no;
    }

    public String getCgpa() {
        return Cgpa;
    }

    public String getAddress() {
        return Address;
    }

    public String getPhone() {
        return Phone;
    }
}

