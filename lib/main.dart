import 'package:flutter/material.dart';

void main() {
  runApp(const AppHotToc());
}

// Lớp quản lý trạng thái xác thực sinh trắc học
class QuanLyXacThuc {
  static bool daKichHoatVanTay = false;
  static String taiKhoanLienKet = '';
}

class LichHenItem {
  final String dichVu;
  final String ngay;
  final String gio;
  final String tho;

  LichHenItem({
    required this.dichVu,
    required this.ngay,
    required this.gio,
    required this.tho,
  });
}

class AppHotToc extends StatelessWidget {
  const AppHotToc({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'App Hớt Tóc',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF1976D2),
          primary: const Color(0xFF1976D2),
        ),
        useMaterial3: true,
      ),
      home: const DangNhapPage(),
    );
  }
}

class DangNhapPage extends StatefulWidget {
  const DangNhapPage({super.key});

  @override
  State<DangNhapPage> createState() => _DangNhapPageState();
}

class _DangNhapPageState extends State<DangNhapPage> {
  final TextEditingController _taiKhoanController = TextEditingController();
  final TextEditingController _matKhauController = TextEditingController();
  bool _anMatKhau = true;

  @override
  void initState() {
    super.initState();
    // Nếu đã kích hoạt vân tay, tự động điền tài khoản đã liên kết
    if (QuanLyXacThuc.daKichHoatVanTay && QuanLyXacThuc.taiKhoanLienKet.isNotEmpty) {
      _taiKhoanController.text = QuanLyXacThuc.taiKhoanLienKet;
    }
  }

  void _dangNhap() {
    final taiKhoan = _taiKhoanController.text.trim();
    final matKhau = _matKhauController.text.trim();

    if (taiKhoan.isEmpty || matKhau.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng nhập tài khoản và mật khẩu!')),
      );
      return;
    }

    // Nếu tài khoản này đã kích hoạt vân tay trước đó -> Vào thẳng màn hình chính
    if (QuanLyXacThuc.daKichHoatVanTay && QuanLyXacThuc.taiKhoanLienKet.toLowerCase() == taiKhoan.toLowerCase()) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đăng nhập thành công!')),
      );
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (context) => TrangChinhPage(tenNguoiDung: taiKhoan),
        ),
      );
      return;
    }

    // Nếu chưa kích hoạt vân tay cho tài khoản này -> Hiển thị hộp thoại hỏi người dùng
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          title: const Text('Kích hoạt đăng nhập vân tay'),
          content: Text(
            'Đăng nhập mật khẩu thành công! Bạn có muốn kích hoạt tính năng đăng nhập nhanh bằng vân tay cho tài khoản "$taiKhoan" vào các lần sau không?',
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(context);
                Navigator.pushReplacement(
                  context,
                  MaterialPageRoute(
                    builder: (context) => TrangChinhPage(tenNguoiDung: taiKhoan),
                  ),
                );
              },
              child: const Text('Để sau'),
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF1976D2),
                foregroundColor: Colors.white,
              ),
              onPressed: () {
                Navigator.pop(context);
                _xacNhanKichHoatVanTay(taiKhoan);
              },
              child: const Text('Kích hoạt ngay'),
            ),
          ],
        );
      },
    );
  }

  void _xacNhanKichHoatVanTay(String taiKhoan) {
    // Giả lập quét vân tay xác nhận chủ sở hữu thiết bị
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          title: const Text('Xác thực chủ thiết bị'),
          content: const Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.fingerprint, size: 64, color: Color(0xFF1976D2)),
              SizedBox(height: 16),
              Text(
                'Vui lòng chạm cảm biến vân tay để hoàn tất kích hoạt.',
                textAlign: TextAlign.center,
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Chưa kích hoạt vân tay.')),
                );
                Navigator.pushReplacement(
                  context,
                  MaterialPageRoute(
                    builder: (context) => TrangChinhPage(tenNguoiDung: taiKhoan),
                  ),
                );
              },
              child: const Text('Hủy'),
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF1976D2),
                foregroundColor: Colors.white,
              ),
              onPressed: () {
                Navigator.pop(context);
                // Lưu trạng thái đã kích hoạt
                setState(() {
                  QuanLyXacThuc.daKichHoatVanTay = true;
                  QuanLyXacThuc.taiKhoanLienKet = taiKhoan;
                });
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Kích hoạt đăng nhập vân tay thành công!'),
                    backgroundColor: Colors.green,
                  ),
                );
                Navigator.pushReplacement(
                  context,
                  MaterialPageRoute(
                    builder: (context) => TrangChinhPage(tenNguoiDung: taiKhoan),
                  ),
                );
              },
              child: const Text('Quét thành công'),
            ),
          ],
        );
      },
    );
  }

  void _quetVanTay() {
    // Kiểm tra quy tắc: Chưa kích hoạt thì không cho vào
    if (!QuanLyXacThuc.daKichHoatVanTay || QuanLyXacThuc.taiKhoanLienKet.isEmpty) {
      showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: const Text('Chưa kích hoạt vân tay'),
            content: const Text(
              'Để đảm bảo an toàn, bạn cần đăng nhập bằng Số điện thoại/Tài khoản và Mật khẩu trước một lần. Hệ thống sẽ hỏi bạn có muốn kích hoạt vân tay cho các lần sau hay không.',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Đã hiểu'),
              ),
            ],
          );
        },
      );
      return;
    }

    // Đã kích hoạt -> Cho phép quét vân tay để đăng nhập nhanh
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Xác thực sinh trắc học'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.fingerprint, size: 64, color: Color(0xFF1976D2)),
              const SizedBox(height: 16),
              Text(
                'Quét vân tay để đăng nhập nhanh cho tài khoản:\n${QuanLyXacThuc.taiKhoanLienKet}',
                textAlign: TextAlign.center,
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Hủy'),
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF1976D2),
                foregroundColor: Colors.white,
              ),
              onPressed: () {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('Xác thực vân tay thành công! Chào ${QuanLyXacThuc.taiKhoanLienKet}'),
                    backgroundColor: Colors.green[700],
                  ),
                );
                Navigator.pushReplacement(
                  context,
                  MaterialPageRoute(
                    builder: (context) => TrangChinhPage(tenNguoiDung: QuanLyXacThuc.taiKhoanLienKet),
                  ),
                );
              },
              child: const Text('Xác nhận'),
            ),
          ],
        );
      },
    );
  }

  @override
  void dispose() {
    _taiKhoanController.dispose();
    _matKhauController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final bool daKichHoat = QuanLyXacThuc.daKichHoatVanTay && QuanLyXacThuc.taiKhoanLienKet.isNotEmpty;

    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 40),
              const Text(
                'Tiệm Hớt Tóc',
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1D1D1D),
                ),
              ),
              const SizedBox(height: 40),
              TextField(
                controller: _taiKhoanController,
                decoration: InputDecoration(
                  labelText: 'Số điện thoại / Email',
                  prefixIcon: const Icon(Icons.person_outline),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _matKhauController,
                obscureText: _anMatKhau,
                decoration: InputDecoration(
                  labelText: 'Mật khẩu',
                  prefixIcon: const Icon(Icons.lock_outline),
                  suffixIcon: IconButton(
                    icon: Icon(_anMatKhau ? Icons.visibility_off : Icons.visibility),
                    onPressed: () {
                      setState(() {
                        _anMatKhau = !_anMatKhau;
                      });
                    },
                  ),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
              ),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                height: 54,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF1976D2),
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  onPressed: _dangNhap,
                  child: const Text('Đăng nhập', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ),
              const SizedBox(height: 32),
              Center(
                child: Text(
                  daKichHoat ? 'Hoặc đăng nhập nhanh bằng' : 'Đăng nhập bằng vân tay',
                  style: const TextStyle(color: Colors.grey),
                ),
              ),
              const SizedBox(height: 16),
              Center(
                child: FloatingActionButton(
                  backgroundColor: daKichHoat ? const Color(0xFF1976D2) : Colors.grey[400],
                  foregroundColor: Colors.white,
                  elevation: 4,
                  onPressed: _quetVanTay,
                  tooltip: 'Quét vân tay',
                  child: const Icon(Icons.fingerprint, size: 36),
                ),
              ),
              const SizedBox(height: 8),
              Center(
                child: Text(
                  daKichHoat
                      ? 'Vân tay đã liên kết: ${QuanLyXacThuc.taiKhoanLienKet}'
                      : '(Chưa kích hoạt - Đăng nhập mật khẩu trước)',
                  style: TextStyle(
                    fontSize: 13,
                    color: daKichHoat ? const Color(0xFF1976D2) : Colors.grey[600],
                    fontWeight: daKichHoat ? FontWeight.w600 : FontWeight.normal,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class TrangChinhPage extends StatefulWidget {
  final String tenNguoiDung;

  const TrangChinhPage({super.key, this.tenNguoiDung = 'Hoài An'});

  @override
  State<TrangChinhPage> createState() => _TrangChinhPageState();
}

class _TrangChinhPageState extends State<TrangChinhPage> {
  int _tabHienTai = 0;
  final List<LichHenItem> _danhSachLichHen = [];
  final List<String> _danhSachTho = [
    'Thợ Phúc',
    'Thợ An',
    'Thợ Quân',
    'Thợ Mạnh',
    'Thợ Trí'
  ];

  Future<void> _moHopThoaiChonLich(String tenDichVu) async {
    // 1. Chọn ngày
    final DateTime? ngayChon = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 30)),
    );

    if (ngayChon == null || !mounted) return;

    // 2. Chọn giờ
    final TimeOfDay? gioChon = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
    );

    if (gioChon == null || !mounted) return;

    // 3. Chọn thợ
    final String? thoChon = await showDialog<String>(
      context: context,
      builder: (context) {
        return SimpleDialog(
          title: Text('Chọn thợ cắt tóc cho: $tenDichVu'),
          children: _danhSachTho.map((tho) {
            return SimpleDialogOption(
              padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 24),
              onPressed: () => Navigator.pop(context, tho),
              child: Text(tho, style: const TextStyle(fontSize: 16)),
            );
          }).toList(),
        );
      },
    );

    if (thoChon == null || !mounted) return;

    final String ngayStr = '${ngayChon.day.toString().padLeft(2, '0')}/${ngayChon.month.toString().padLeft(2, '0')}/${ngayChon.year}';
    final String gioStr = '${gioChon.hour.toString().padLeft(2, '0')}:${gioChon.minute.toString().padLeft(2, '0')}';

    setState(() {
      _danhSachLichHen.add(LichHenItem(
        dichVu: tenDichVu,
        ngay: ngayStr,
        gio: gioStr,
        tho: thoChon,
      ));
    });

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Tuyệt vời! Đã đặt lịch ($ngayStr $gioStr) với $thoChon!'),
        backgroundColor: Colors.green[700],
      ),
    );
  }

  void _hienThiDanhSachLichHen() {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('📅 Danh sách lịch hẹn đã đặt'),
          content: _danhSachLichHen.isEmpty
              ? const Text('Bạn chưa đặt lịch hẹn nào.\nHãy nhấn "Đặt lịch ngay" hoặc chọn dịch vụ để đặt!')
              : SizedBox(
                  width: double.maxFinite,
                  child: ListView.separated(
                    shrinkWrap: true,
                    itemCount: _danhSachLichHen.length,
                    separatorBuilder: (_, __) => const Divider(),
                    itemBuilder: (context, index) {
                      final item = _danhSachLichHen[index];
                      return ListTile(
                        leading: const CircleAvatar(
                          backgroundColor: Color(0xFF1976D2),
                          foregroundColor: Colors.white,
                          child: Icon(Icons.content_cut),
                        ),
                        title: Text(item.dichVu, style: const TextStyle(fontWeight: FontWeight.bold)),
                        subtitle: Text('⏰ ${item.gio} - ${item.ngay}\n✂️ ${item.tho}'),
                      );
                    },
                  ),
                ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Đóng'),
            ),
          ],
        );
      },
    );
  }

  void _hienThiThongTinTaiKhoan() {
    showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              title: const Text('👤 Thông tin tài khoản'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Tài khoản: ${widget.tenNguoiDung}', style: const TextStyle(fontSize: 16)),
                  const SizedBox(height: 8),
                  const Text('Trạng thái: Đã xác thực', style: TextStyle(color: Colors.green)),
                  const SizedBox(height: 8),
                  const Text('Vai trò: Khách hàng thân thiết'),
                  const SizedBox(height: 8),
                  Text(
                    'Đăng nhập vân tay: ${QuanLyXacThuc.daKichHoatVanTay ? "Đã kích hoạt (${QuanLyXacThuc.taiKhoanLienKet})" : "Chưa kích hoạt"}',
                    style: TextStyle(
                      fontWeight: FontWeight.w600,
                      color: QuanLyXacThuc.daKichHoatVanTay ? const Color(0xFF1976D2) : Colors.grey,
                    ),
                  ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () {
                    setDialogState(() {
                      if (QuanLyXacThuc.daKichHoatVanTay) {
                        QuanLyXacThuc.daKichHoatVanTay = false;
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('Đã tắt đăng nhập vân tay')),
                        );
                      } else {
                        QuanLyXacThuc.daKichHoatVanTay = true;
                        QuanLyXacThuc.taiKhoanLienKet = widget.tenNguoiDung;
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('Đã bật đăng nhập vân tay cho ${widget.tenNguoiDung}')),
                        );
                      }
                    });
                    setState(() {});
                  },
                  child: Text(QuanLyXacThuc.daKichHoatVanTay ? 'Tắt vân tay' : 'Bật vân tay'),
                ),
                TextButton(
                  style: TextButton.styleFrom(foregroundColor: Colors.red),
                  onPressed: () {
                    Navigator.pop(context);
                    Navigator.pushReplacement(
                      context,
                      MaterialPageRoute(builder: (context) => const DangNhapPage()),
                    );
                  },
                  child: const Text('Đăng xuất'),
                ),
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Đóng'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Lời chào người dùng
              Text(
                'Chào, ${widget.tenNguoiDung} 👋',
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1D1D1D),
                ),
              ),
              const SizedBox(height: 4),
              const Text(
                'Hôm nay bạn muốn cắt kiểu gì?',
                style: TextStyle(fontSize: 16, color: Color(0xFF757575)),
              ),
              const SizedBox(height: 24),

              // Banner Khuyến mãi / Đặt lịch nhanh
              Card(
                color: const Color(0xFF1976D2),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
                elevation: 4,
                child: Padding(
                  padding: const EdgeInsets.all(20.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Combo Cắt Gội Tỏa Sáng',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'Giảm 20% cho lần đặt lịch đầu tiên qua app',
                        style: TextStyle(fontSize: 14, color: Color(0xFFE3F2FD)),
                      ),
                      const SizedBox(height: 16),
                      OutlinedButton(
                        style: OutlinedButton.styleFrom(
                          foregroundColor: Colors.white,
                          side: const BorderSide(color: Colors.white),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8),
                          ),
                        ),
                        onPressed: () => _moHopThoaiChonLich('Combo Cắt Gội Tỏa Sáng'),
                        child: const Text('Đặt lịch ngay'),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 32),
              const Text(
                'Dịch vụ nổi bật',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1D1D1D),
                ),
              ),
              const SizedBox(height: 16),

              // Danh sách dịch vụ dạng lưới 2 cột
              Row(
                children: [
                  Expanded(
                    child: InkWell(
                      onTap: () => _moHopThoaiChonLich('Cắt tạo kiểu Mohican Fade'),
                      borderRadius: BorderRadius.circular(12),
                      child: Card(
                        elevation: 2,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const SizedBox(
                          height: 120,
                          child: Center(
                            child: Text(
                              'Cắt tạo kiểu\nMohican Fade',
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                color: Color(0xFF1D1D1D),
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: InkWell(
                      onTap: () => _moHopThoaiChonLich('Uốn/Nhuộm'),
                      borderRadius: BorderRadius.circular(12),
                      child: Card(
                        elevation: 2,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const SizedBox(
                          height: 120,
                          child: Center(
                            child: Text(
                              'Uốn/Nhuộm',
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                color: Color(0xFF1D1D1D),
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _tabHienTai,
        selectedItemColor: const Color(0xFF1976D2),
        onTap: (index) {
          setState(() {
            _tabHienTai = index;
          });
          if (index == 0) {
            // Trang chủ
          } else if (index == 1) {
            _hienThiDanhSachLichHen();
          } else if (index == 2) {
            _hienThiThongTinTaiKhoan();
          }
        },
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.home),
            label: 'Trang chủ',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.calendar_month),
            label: 'Lịch hẹn',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person),
            label: 'Tài khoản',
          ),
        ],
      ),
    );
  }
}
