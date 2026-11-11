# Kịch bản video demo (5–7 phút)

> Yêu cầu của đề: video upload YouTube, mô tả ngắn gọn cấu trúc dữ liệu, cho biết
> phần trăm tham khảo và demo toàn bộ ít nhất 1 chức năng. Kịch bản dưới đây demo
> đủ 10 chức năng để an toàn.

## Chuẩn bị

- Chạy `run.bat` (hoặc `java -jar dist\SlangDictionary.jar`).
- Mở sẵn thư mục dự án để thấy `slang.txt`, `data/`.
- Quay màn hình 1280×720 trở lên, font dễ đọc.

## 0:00 – 0:30 · Giới thiệu

- “Xin chào, mình là …, MSSV …”.
- “Đây là đồ án #1 – Slang Word môn CSC13002, viết bằng Java Swing.”
- “Ứng dụng có 10 chức năng theo đề, dùng Java IO, OOP, Generic và Collections.”

## 0:30 – 1:30 · Cấu trúc dữ liệu

Mở `src/main/java/vn/edu/hcmus/slang/data/SlangDictionary.java` và `TrigramIndex.java`.

Nói ngắn gọn:

- `HashMap<String, List<Slang>>` cho tra cứu từ khoá O(1), vẫn giữ được các bản trùng.
- `TrigramIndex`: posting list `trigram → danh sách mục từ`, tìm definition bằng giao
  posting list + kiểm tra lại.
- `dictionary.bin`: snapshot nhị phân chứa cả chỉ mục; lần chạy sau đọc thẳng, không
  đánh chỉ mục lại (có CRC32 fingerprint để phát hiện file thay đổi).
- “Phần tham khảo: 0% — chỉ dùng JDK chuẩn, dữ liệu slang.txt do đề cung cấp.”

## 1:30 – 2:10 · Tổng quan + menu

- Chỉ dashboard: 7.656 mục, 7.636 từ duy nhất, 8.959 định nghĩa, lịch sử.
- Mở menu **Chức năng** để thấy đủ 10 mục; nhấn Ctrl+1 để mở nhanh.

## 2:10 – 2:40 · Chức năng 1 + 2

- Tra cứu `BOMB` → 2 định nghĩa; thử `bomb` để thấy không phân biệt hoa thường.
- Tìm định nghĩa `money` → 46 kết quả, highlight từ khoá.
- Nhấn Enter, chỉ ra thời gian `0 ms`.

## 2:40 – 3:10 · Chức năng 3

- Mở **Lịch sử tra cứu**: thấy `money` (định nghĩa) và `BOMB` (slang word), thời gian.
- Nhấn **Xóa lịch sử** → hộp thoại xác nhận → đồng ý.

## 3:10 – 4:10 · Chức năng 4, 5, 6

- **Thêm**: nhập `GHOSTING` + 2 định nghĩa → Lưu.
- Thêm lại `GHOSTING` → hộp thoại trùng: chọn **Thêm bản trùng** (hoặc **Ghi đè**).
- **Sửa**: tìm `GHOSTING`, chọn bản ghi, sửa định nghĩa, Lưu.
- **Xóa**: tìm `GHOSTING`, chọn, bấm Xóa → hộp thoại xác nhận → Hủy (để chứng minh
  bước xác nhận), sau đó thử lại và Xóa.

## 4:10 – 4:40 · Chức năng 7

- **Khôi phục dữ liệu gốc** → đọc cảnh báo → bấm Khôi phục → xác nhận.
- Chỉ số quay về 7.656; thử tìm `GHOSTING` → không còn.

## 4:40 – 5:20 · Chức năng 8, 9, 10

- **Slang word mỗi ngày**: bấm “Từ ngẫu nhiên khác” vài lần.
- **Đố vui: đoán định nghĩa**: chọn đáp án, chỉ viền đậm/nét đứt cho đúng/sai, bấm Câu tiếp theo.
- **Đố vui: đoán slang word**: làm tương tự.

## 5:20 – 6:00 · Hiệu năng + kết thúc

- Mở terminal, chạy `test.bat` hoặc chiếu `report/test-results.txt`.
- Nói: “88/88 test đạt; với 100.000 slang word, xây chỉ mục ~310ms, tìm kiếm 0–20ms,
  nạp snapshot ~508ms, đều dưới 1 giây.”
- “Source code, jar, dữ liệu, ảnh chụp và file tự đánh giá nằm trong file zip nộp bài.
  Cảm ơn thầy/cô đã xem.”

## Mẹo quay

- Dùng OBS hoặc PowerPoint Record; 1080p, 30fps.
- Tắt thông báo Windows.
- Thu âm rõ; nếu video dài quá 7 phút, tua nhanh phần gõ chữ nhưng giữ lại thao tác
  và kết quả.
- Sau khi upload, dán link vào `report/TuDanhGia.txt` và chạy `package.ps1` với
  `-VideoUrl`.
