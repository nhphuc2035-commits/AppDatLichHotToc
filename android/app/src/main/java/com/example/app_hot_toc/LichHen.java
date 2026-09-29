package com.example.app_hot_toc;

public class LichHen {
    public String ngayCat;
    public String gioCat;
    public String thoCat;
    public String dichVu;

    // Firebase bat buoc phai co constructor rong
    public LichHen() {
    }

    public LichHen(String ngayCat, String gioCat, String thoCat) {
        this.ngayCat = ngayCat;
        this.gioCat = gioCat;
        this.thoCat = thoCat;
        this.dichVu = "Cắt tóc tiêu chuẩn";
    }

    public LichHen(String ngayCat, String gioCat, String thoCat, String dichVu) {
        this.ngayCat = ngayCat;
        this.gioCat = gioCat;
        this.thoCat = thoCat;
        this.dichVu = dichVu;
    }
}