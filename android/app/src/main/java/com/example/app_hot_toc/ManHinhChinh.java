package com.example.app_hot_toc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManHinhChinh extends AppCompatActivity {

    private String tenNguoiDung = "Hoài An";
    private final List<LichHen> danhSachLichHenLocal = new ArrayList<>();
    private final String[] danhSachTho = {"Thợ Phúc", "Thợ An", "Thợ Quân", "Thợ Mạnh", "Thợ Trí"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.giao_dien_chinh);

        // Nhận tên người dùng từ màn hình đăng nhập nếu có
        String nhanTen = getIntent().getStringExtra("TEN_NGUOI_DUNG");
        if (nhanTen != null && !nhanTen.trim().isEmpty()) {
            tenNguoiDung = nhanTen.trim();
        }

        TextView tvLoiChao = findViewById(R.id.tvLoiChao);
        if (tvLoiChao != null) {
            tvLoiChao.setText("Chào, " + tenNguoiDung + " 👋");
        }

        MaterialButton nutDatLichNgay = findViewById(R.id.nutDatLichNgay);
        MaterialCardView cardDichVu1 = findViewById(R.id.cardDichVu1);
        MaterialCardView cardDichVu2 = findViewById(R.id.cardDichVu2);
        BottomNavigationView thanhDieuHuong = findViewById(R.id.thanhDieuHuong);

        // Bắt sự kiện bấm nút Đặt lịch trên Banner
        if (nutDatLichNgay != null) {
            nutDatLichNgay.setOnClickListener(v -> moHopThoaiChonLich("Combo Cắt Gội Tỏa Sáng"));
        }

        // Bắt sự kiện bấm vào các thẻ dịch vụ
        if (cardDichVu1 != null) {
            cardDichVu1.setOnClickListener(v -> moHopThoaiChonLich("Cắt tạo kiểu Mohican Fade"));
        }

        if (cardDichVu2 != null) {
            cardDichVu2.setOnClickListener(v -> moHopThoaiChonLich("Uốn/Nhuộm"));
        }

        // Bắt sự kiện chuyển tab ở thanh điều hướng dưới đáy
        if (thanhDieuHuong != null) {
            thanhDieuHuong.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_trang_chu) {
                    Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_lich_hen) {
                    hienThiDanhSachLichHen();
                    return true;
                } else if (itemId == R.id.nav_tai_khoan) {
                    hienThiThongTinTaiKhoan();
                    return true;
                }
                return false;
            });
        }
    }

    private void moHopThoaiChonLich(String tenDichVu) {
        Calendar calendar = Calendar.getInstance();
        int nam = calendar.get(Calendar.YEAR);
        int thang = calendar.get(Calendar.MONTH);
        int ngay = calendar.get(Calendar.DAY_OF_MONTH);

        // 1. Hộp thoại chọn Ngày
        DatePickerDialog datePickerDialog = new DatePickerDialog(ManHinhChinh.this, (view, year, month, dayOfMonth) -> {
            String ngayDaChon = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);

            // 2. Hộp thoại chọn Giờ
            int gio = calendar.get(Calendar.HOUR_OF_DAY);
            int phut = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(ManHinhChinh.this, (timeView, hourOfDay, minuteOfHour) -> {
                String gioDaChon = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);

                // 3. Hộp thoại chọn Thợ cắt tóc
                AlertDialog.Builder builderTho = new AlertDialog.Builder(ManHinhChinh.this);
                builderTho.setTitle("Chọn thợ cắt tóc cho: " + tenDichVu);
                builderTho.setItems(danhSachTho, (dialog, which) -> {
                    String thoDuocChon = danhSachTho[which];
                    luuLichHenLenFirebase(ngayDaChon, gioDaChon, thoDuocChon, tenDichVu);
                });
                builderTho.setNegativeButton("Hủy", null);
                builderTho.show();

            }, gio, phut, true);

            timePickerDialog.setTitle("Chọn giờ cắt tóc");
            timePickerDialog.show();
        }, nam, thang, ngay);

        datePickerDialog.setTitle("Chọn ngày cắt tóc");
        datePickerDialog.show();
    }

    private void luuLichHenLenFirebase(String ngayDaChon, String gioDaChon, String thoDuocChon, String tenDichVu) {
        if (gioDaChon == null || thoDuocChon == null || ngayDaChon == null ||
                gioDaChon.isEmpty() || thoDuocChon.isEmpty() || ngayDaChon.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ ngày, giờ và thợ cắt!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu vào danh sách local để hiển thị ngay
        LichHen lichMoi = new LichHen(ngayDaChon, gioDaChon, thoDuocChon, tenDichVu);
        danhSachLichHenLocal.add(lichMoi);

        Toast.makeText(this, "Đang gửi lịch hẹn lên Firebase...", Toast.LENGTH_SHORT).show();

        try {
            FirebaseDatabase database;
            try {
                // Thử lấy instance với URL mặc định của project app-hot-toc
                database = FirebaseDatabase.getInstance("https://app-hot-toc-default-rtdb.firebaseio.com");
            } catch (Exception e1) {
                database = FirebaseDatabase.getInstance();
            }

            DatabaseReference bangLichHen = database.getReference("DanhSachLichHen");

            // push() tạo ra một mã ID ngẫu nhiên không trùng lặp cho mỗi lịch hẹn
            bangLichHen.push().setValue(lichMoi)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tuyệt vời! Đã đặt lịch thành công (" + ngayDaChon + " " + gioDaChon + ") với " + thoDuocChon + "!", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        // Vẫn thông báo đã lưu vào máy nếu mạng lỗi
                        Toast.makeText(this, "Đã lưu lịch vào máy (Firebase offline: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            // Không crash app nếu Firebase chưa cấu hình mạng
            Toast.makeText(this, "Đã lưu lịch vào bộ nhớ tạm: " + ngayDaChon + " " + gioDaChon + " (" + thoDuocChon + ")", Toast.LENGTH_LONG).show();
        }
    }

    private void hienThiDanhSachLichHen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📅 Danh sách lịch hẹn đã đặt");

        if (danhSachLichHenLocal.isEmpty()) {
            builder.setMessage("Bạn chưa đặt lịch hẹn nào.\nHãy nhấn 'Đặt lịch ngay' hoặc chọn một dịch vụ để đặt lịch!");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < danhSachLichHenLocal.size(); i++) {
                LichHen l = danhSachLichHenLocal.get(i);
                sb.append(i + 1).append(". ").append(l.dichVu != null ? l.dichVu : "Cắt tóc")
                        .append("\n   ⏰ ").append(l.gioCat).append(" - ").append(l.ngayCat)
                        .append("\n   ✂️ ").append(l.thoCat).append("\n\n");
            }
            builder.setMessage(sb.toString().trim());
        }

        builder.setPositiveButton("Đóng", null);
        builder.show();
    }

    private void hienThiThongTinTaiKhoan() {
        android.content.SharedPreferences prefs = getSharedPreferences(ManHinhDangNhap.PREF_NAME, MODE_PRIVATE);
        boolean daBatVanTay = prefs.getBoolean(ManHinhDangNhap.KEY_BIOMETRIC_ENABLED, false);
        String taiKhoanLuu = prefs.getString(ManHinhDangNhap.KEY_LINKED_ACCOUNT, "");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👤 Thông tin tài khoản");

        String thongTin = "Tài khoản: " + tenNguoiDung +
                "\nTrạng thái: Đã xác thực" +
                "\nVai trò: Khách hàng thân thiết" +
                "\nĐăng nhập vân tay: " + (daBatVanTay ? ("Đã kích hoạt (" + taiKhoanLuu + ")") : "Chưa kích hoạt");
        builder.setMessage(thongTin);

        if (daBatVanTay) {
            builder.setNeutralButton("Tắt vân tay", (dialog, which) -> {
                prefs.edit().putBoolean(ManHinhDangNhap.KEY_BIOMETRIC_ENABLED, false).apply();
                Toast.makeText(this, "Đã tắt tính năng đăng nhập nhanh bằng vân tay", Toast.LENGTH_SHORT).show();
            });
        } else {
            builder.setNeutralButton("Bật vân tay", (dialog, which) -> {
                prefs.edit()
                        .putBoolean(ManHinhDangNhap.KEY_BIOMETRIC_ENABLED, true)
                        .putString(ManHinhDangNhap.KEY_LINKED_ACCOUNT, tenNguoiDung)
                        .apply();
                Toast.makeText(this, "Đã bật đăng nhập vân tay cho tài khoản " + tenNguoiDung, Toast.LENGTH_SHORT).show();
            });
        }

        builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ManHinhChinh.this, ManHinhDangNhap.class);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("Đóng", null);
        builder.show();
    }
}
