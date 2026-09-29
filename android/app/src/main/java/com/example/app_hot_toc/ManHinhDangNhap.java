package com.example.app_hot_toc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;

public class ManHinhDangNhap extends AppCompatActivity {

    public static final String PREF_NAME = "AppHotTocPrefs";
    public static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    public static final String KEY_LINKED_ACCOUNT = "linked_account";

    private static final int CHE_DO_DANG_NHAP = 1;
    private static final int CHE_DO_DANG_KY_VAN_TAY = 2;

    private int cheDoHienTai = CHE_DO_DANG_NHAP;
    private String taiKhoanDangXuLy = "";

    private SharedPreferences prefs;
    private BiometricPrompt hopThoaiVanTay;
    private BiometricPrompt.PromptInfo thongTinHopThoai;

    private FloatingActionButton nutQuetVanTay;
    private TextView tvHoac;
    private TextView tvMoTaVanTay;
    private TextInputLayout oNhapTaiKhoan;
    private TextInputLayout oNhapMatKhau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.giao_dien_dang_nhap);

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Ánh xạ các thành phần giao diện từ XML
        nutQuetVanTay = findViewById(R.id.nutQuetVanTay);
        tvHoac = findViewById(R.id.tvHoac);
        tvMoTaVanTay = findViewById(R.id.tvMoTaVanTay);
        MaterialButton nutDangNhapThuong = findViewById(R.id.nutDangNhapThuong);
        oNhapTaiKhoan = findViewById(R.id.oNhapTaiKhoan);
        oNhapMatKhau = findViewById(R.id.oNhapMatKhau);

        // Cập nhật trạng thái hiển thị của phần vân tay dựa theo cấu hình đã lưu
        capNhatTrangThaiVanTayUI();

        // Khởi tạo luồng xử lý và Callback nhận kết quả vân tay
        Executor luongXuLy = ContextCompat.getMainExecutor(this);
        hopThoaiVanTay = new BiometricPrompt(ManHinhDangNhap.this, luongXuLy, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int maLoi, @NonNull CharSequence chuoiLoi) {
                super.onAuthenticationError(maLoi, chuoiLoi);
                if (cheDoHienTai == CHE_DO_DANG_KY_VAN_TAY) {
                    Toast.makeText(getApplicationContext(), "Chưa kích hoạt vân tay: " + chuoiLoi, Toast.LENGTH_SHORT).show();
                    chuyenDenManHinhChinh(taiKhoanDangXuLy);
                } else {
                    Toast.makeText(getApplicationContext(), "Hủy xác thực: " + chuoiLoi, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult ketQua) {
                super.onAuthenticationSucceeded(ketQua);

                if (cheDoHienTai == CHE_DO_DANG_KY_VAN_TAY) {
                    // Lưu trạng thái đã kích hoạt vân tay thành công cho tài khoản này
                    prefs.edit()
                            .putBoolean(KEY_BIOMETRIC_ENABLED, true)
                            .putString(KEY_LINKED_ACCOUNT, taiKhoanDangXuLy)
                            .apply();

                    Toast.makeText(getApplicationContext(), "Kích hoạt đăng nhập vân tay thành công!", Toast.LENGTH_SHORT).show();
                    chuyenDenManHinhChinh(taiKhoanDangXuLy);
                } else {
                    // Đăng nhập nhanh bằng vân tay
                    String taiKhoanLuu = prefs.getString(KEY_LINKED_ACCOUNT, "Khách hàng");
                    Toast.makeText(getApplicationContext(), "Xác thực vân tay thành công! Chào " + taiKhoanLuu, Toast.LENGTH_SHORT).show();
                    chuyenDenManHinhChinh(taiKhoanLuu);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Vân tay không khớp, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });

        // Thiết lập giao diện hộp thoại hệ thống
        thongTinHopThoai = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực sinh trắc học")
                .setSubtitle("Quét vân tay để vào App Hớt Tóc")
                .setNegativeButtonText("Hủy")
                .build();

        // Bắt sự kiện bấm nút Đăng nhập thường (Tài khoản & Mật khẩu)
        if (nutDangNhapThuong != null) {
            nutDangNhapThuong.setOnClickListener(v -> xuLyDangNhapMatKhau());
        }

        // Bắt sự kiện click nút quét vân tay
        if (nutQuetVanTay != null) {
            nutQuetVanTay.setOnClickListener(v -> xuLyNutQuetVanTay());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại trạng thái nếu người dùng thay đổi thiết lập trong màn hình chính
        capNhatTrangThaiVanTayUI();
    }

    private void capNhatTrangThaiVanTayUI() {
        boolean daKichHoat = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
        String taiKhoanLuu = prefs.getString(KEY_LINKED_ACCOUNT, "");

        if (daKichHoat && !taiKhoanLuu.isEmpty()) {
            if (oNhapTaiKhoan != null && oNhapTaiKhoan.getEditText() != null) {
                if (oNhapTaiKhoan.getEditText().getText().toString().trim().isEmpty()) {
                    oNhapTaiKhoan.getEditText().setText(taiKhoanLuu);
                }
            }
            if (tvHoac != null) {
                tvHoac.setText("Hoặc đăng nhập nhanh bằng");
            }
            if (tvMoTaVanTay != null) {
                tvMoTaVanTay.setText("Vân tay đã liên kết: " + taiKhoanLuu);
                tvMoTaVanTay.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            }
            if (nutQuetVanTay != null) {
                nutQuetVanTay.setAlpha(1.0f);
            }
        } else {
            if (tvHoac != null) {
                tvHoac.setText("Đăng nhập bằng vân tay");
            }
            if (tvMoTaVanTay != null) {
                tvMoTaVanTay.setText("(Chưa kích hoạt - Đăng nhập mật khẩu trước)");
                tvMoTaVanTay.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            }
            if (nutQuetVanTay != null) {
                nutQuetVanTay.setAlpha(0.6f);
            }
        }
    }

    private void xuLyNutQuetVanTay() {
        boolean daKichHoat = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
        String taiKhoanLuu = prefs.getString(KEY_LINKED_ACCOUNT, "");

        // Kiểm tra xem đã kích hoạt vân tay lần đầu chưa
        if (!daKichHoat || taiKhoanLuu.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Chưa kích hoạt vân tay")
                    .setMessage("Để đảm bảo an toàn, bạn cần đăng nhập bằng Số điện thoại/Tài khoản và Mật khẩu trước một lần. Hệ thống sẽ hỏi bạn có muốn kích hoạt vân tay cho các lần sau hay không.")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
            return;
        }

        // Đã kích hoạt -> Bật quét vân tay để đăng nhập nhanh
        cheDoHienTai = CHE_DO_DANG_NHAP;
        kiemTraVaQuetVanTay();
    }

    private void xuLyDangNhapMatKhau() {
        String taiKhoan = (oNhapTaiKhoan != null && oNhapTaiKhoan.getEditText() != null)
                ? oNhapTaiKhoan.getEditText().getText().toString().trim() : "";
        String matKhau = (oNhapMatKhau != null && oNhapMatKhau.getEditText() != null)
                ? oNhapMatKhau.getEditText().getText().toString().trim() : "";

        if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
            Toast.makeText(ManHinhDangNhap.this, "Vui lòng nhập tài khoản và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        taiKhoanDangXuLy = taiKhoan;
        boolean daKichHoat = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
        String taiKhoanLuu = prefs.getString(KEY_LINKED_ACCOUNT, "");

        // Nếu tài khoản này đã kích hoạt vân tay rồi thì vào thẳng
        if (daKichHoat && taiKhoan.equalsIgnoreCase(taiKhoanLuu)) {
            Toast.makeText(ManHinhDangNhap.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            chuyenDenManHinhChinh(taiKhoan);
            return;
        }

        // Nếu chưa kích hoạt vân tay cho tài khoản này -> Hỏi người dùng có muốn kích hoạt không
        hoiKichHoatVanTay(taiKhoan);
    }

    private void hoiKichHoatVanTay(String taiKhoan) {
        new AlertDialog.Builder(this)
                .setTitle("Kích hoạt đăng nhập vân tay")
                .setMessage("Đăng nhập mật khẩu thành công! Bạn có muốn kích hoạt đăng nhập nhanh bằng vân tay cho tài khoản '" + taiKhoan + "' vào các lần sau không?")
                .setPositiveButton("Kích hoạt ngay", (dialog, which) -> {
                    // Kiểm tra xem thiết bị có hỗ trợ vân tay không trước khi kích hoạt
                    BiometricManager quanLySinhTracHoc = BiometricManager.from(this);
                    if (quanLySinhTracHoc.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                        cheDoHienTai = CHE_DO_DANG_KY_VAN_TAY;
                        hopThoaiVanTay.authenticate(thongTinHopThoai);
                    } else {
                        Toast.makeText(this, "Thiết bị không hỗ trợ vân tay hoặc chưa cài vân tay trong cài đặt máy.", Toast.LENGTH_LONG).show();
                        chuyenDenManHinhChinh(taiKhoan);
                    }
                })
                .setNegativeButton("Để sau", (dialog, which) -> {
                    chuyenDenManHinhChinh(taiKhoan);
                })
                .setCancelable(false)
                .show();
    }

    private void kiemTraVaQuetVanTay() {
        BiometricManager quanLySinhTracHoc = BiometricManager.from(this);
        switch (quanLySinhTracHoc.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                hopThoaiVanTay.authenticate(thongTinHopThoai);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Điện thoại của bạn không có cảm biến vân tay", Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Cảm biến đang bận hoặc lỗi", Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "Bạn chưa cài đặt vân tay nào trong phần cài đặt máy", Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(this, "Xác thực sinh trắc học không khả dụng. Vui lòng đăng nhập bằng mật khẩu.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void chuyenDenManHinhChinh(String tenNguoiDung) {
        Intent intent = new Intent(ManHinhDangNhap.this, ManHinhChinh.class);
        intent.putExtra("TEN_NGUOI_DUNG", tenNguoiDung);
        startActivity(intent);
        finish();
    }
}
