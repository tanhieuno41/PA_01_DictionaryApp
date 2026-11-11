# Kế hoạch và kết quả kiểm thử

## 1. Môi trường

- Windows (PowerShell), JDK 23.0.2 (`javac --release 17`).
- Không dùng thư viện kiểm thử ngoài; bộ test tự viết `AllTests`.
- Dữ liệu gốc: `slang.txt` – 7.656 mục từ, 8.959 định nghĩa.

## 2. Cách chạy

```bat
test.bat
```

hoặc:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File build.ps1
java -cp "build/classes;build/test-classes" vn.edu.hcmus.slang.AllTests
```

Kiểm thử giao diện (mở 11 màn hình, chụp 13 ảnh trạng thái):

```bat
java -jar dist\SlangDictionary.jar --smoke-test
```

Kết quả đầy đủ: [`test-results.txt`](test-results.txt).

## 3. Tổng hợp

**88/88 kiểm tra đạt.** Các test cố tình làm hỏng / làm cũ snapshot để kiểm tra
cơ chế fallback rebuild; cảnh báo của `DictionaryStore` được test harness nuôi im lặng.

| Nhóm | Nội dung | Kết quả |
|------|----------|---------|
| 1 | Parser, dữ liệu gốc, dòng định nghĩa nối tiếp | 5/5 |
| 2 | Tìm theo slang word | 7/7 |
| 3 | Tìm theo definition + đối chiếu brute-force | 20/20 |
| 4 | Add / overwrite / duplicate / edit / delete | 13/13 |
| 5 | Persistence, snapshot, fallback | 10/10 |
| 6 | Reset dữ liệu gốc | 7/7 |
| 7 | History persistence | 10/10 |
| 8 | Quiz generation | 2/2 |
| 9 | Hiệu năng 100.000 slang word | 14/14 |

## 4. Đối chiếu yêu cầu đồ án

### Yêu cầu chung

| Yêu cầu | Cách đáp ứng | Minh chứng |
|---------|--------------|------------|
| Java IO, OOP, Collections | `DictionaryStore` đọc/ghi UTF-8, binary `DataInputStream`/`DataOutputStream`; model OOP; HashMap, LinkedHashMap, ArrayDeque, List | `src/main/java/.../data`, `service`; test nhóm 5, 7 |
| Có menu chọn chức năng | `JMenuBar` (Hệ thống / Chức năng / Trợ giúp) + sidebar 10 chức năng | ảnh 01–11b |
| Load dữ liệu, tìm nhanh < 1s với 100k | HashMap O(1) + trigram inverted index | test nhóm 9; bảng hiệu năng bên dưới |
| Lưu cấu trúc đã index, lần sau không index lại | `data/dictionary.bin` + fingerprint CRC32/kích thước/tên nguồn | test nhóm 5; log `indexLoaded=true` |
| Lưu danh sách sau add/edit/delete | `data/slang_modified.txt` + snapshot được ghi sau mỗi thao tác | test nhóm 4, 5 |
| Dùng Git ≥ 10 commit | Repo có lịch sử commit thực tế theo từng bước phát triển | `report/commits/local-git-log.png`, `git-log.txt`; **cần push và chụp ảnh GitHub** |
| Đặt tên bài nộp đúng | `package.ps1` tạo `MSSV_100.zip` | xem README |

### 10 chức năng

| # | Chức năng | Kiểm thử tự động | Ảnh |
|---|-----------|------------------|-----|
| 1 | Tìm theo slang word | `testSearchByWord`: BOMB, lower/mixed case, trim, không tồn tại, gợi ý | 02 |
| 2 | Tìm theo definition | `testDefinitionSearch`: 6 từ khoá, đối chiếu brute-force, thời gian | 03 |
| 3 | Lịch sử | `testHistory`: thứ tự, persistence, giới hạn 200, clear | 04 |
| 4 | Add + trùng | `testMutations`: add, duplicate, overwrite | 05 |
| 5 | Edit | `testMutations`: đổi word, đổi definition | 06 |
| 6 | Delete + confirm | `testMutations`: delete; UI dùng `Dialogs.confirmDelete` | 07 |
| 7 | Reset | `testReset`: khôi phục 7.656 mục, xóa working file | 08 |
| 8 | Random | `RandomPanel`; `SlangDictionary.randomEntry` | 09 |
| 9 | Quiz word → nghĩa | `testQuiz`: 200 câu, 4 đáp án duy nhất, correctIndex | 10 |
| 10 | Quiz nghĩa → word | `testQuiz`: 200 câu, 4 đáp án duy nhất, correctIndex | 11 |

## 5. Hiệu năng (100.000 slang word)

Dữ liệu test được sinh bằng cách nhân bản dữ liệu gốc với hậu tố duy nhất, giữ nguyên
độ dài định nghĩa và phân bố trigram thực tế.

| Phép đo | Kết quả | Yêu cầu |
|---------|---------|---------|
| Sinh 100.000 mục | ~352 ms | – |
| Xây dictionary + trigram index | ~300 ms | – |
| 200 lần tra cứu chính xác | 0 ms (tổng) | < 1s mỗi lần |
| Definition `"money"` | ~1 ms (600 kết quả) | < 1s |
| Definition `"go"` | ~20 ms (5.161 kết quả) | < 1s |
| Definition `"the"` | ~7 ms (7.545 kết quả) | < 1s |
| Definition `"has money"` | ~0 ms (14 kết quả) | < 1s |
| Definition không tồn tại | ~0 ms | < 1s |
| Lưu text + snapshot 14 MB | ~400 ms | – |
| Nạp snapshot lần sau | ~560 ms, `indexLoaded=true` | không rebuild |
| Tìm kiếm sau khi nạp | ~0 ms | < 1s |
| Bộ nhớ dùng | ~184 MB | – |

## 6. Kiểm thử giao diện

`ScreenshotMode` mở cửa sổ thật, đi qua 11 màn hình, gọi dữ liệu mẫu (BOMB / money),
chụp 13 ảnh bằng Java2D và thoát. Ảnh được chuyển sang grayscale; kiểm tra bằng
Python/Pillow cho thấy **0 pixel lệch kênh màu**. Kết quả:

```
SMOKE OK: 11 màn hình, 13 ảnh đã mở/chụp thành công.
```

Ảnh: `report/screens/01-home.png` … `11b-quiz-definition-answered.png`.

## 7. Các trường hợp biên đã kiểm tra

- Từ khoá rỗng / toàn khoảng trắng.
- Hoa/thường, khoảng trắng đầu cuối.
- Từ khoá 2 ký tự (fallback quét tuần tự).
- Định nghĩa không chứa keyword → list rỗng (không lỗi).
- Snapshot hỏng (magic sai) → rebuild.
- File text thay đổi sau khi lưu snapshot → phát hiện fingerprint lệch, rebuild.
- Reset khi đang có dữ liệu sửa → quay về 7.656 mục gốc.
- History vượt 200 mục → cắt bớt mục cũ.
- Quiz với dữ liệu trùng từ → vẫn đảm bảo 4 đáp án duy nhất.

## 8. Hạn chế / ghi chú

- Tìm theo nghĩa là **tìm chuỗi con** (đúng yêu cầu “definition có chứa keyword”), không phải tìm mờ.
- Overwrite khi có nhiều bản trùng sẽ cập nhật **mục đầu tiên**; các bản trùng khác giữ nguyên. UI hiển thị danh sách bản trùng trước khi xác nhận.
- Điểm quiz được giữ trong phiên chạy, không lưu qua các lần mở app.
- Ảnh chụp commit và video YouTube là hai artifact cần sinh viên hoàn thiện trước khi nộp (xem `report/VIDEO_SCRIPT.md` và README).
- Lịch sử commit local được tạo trong một phiên làm việc, nên **chưa đáp ứng yêu cầu phân bố commit nhiều ngày**. Không nên chỉnh ngày commit giả tạo; hãy tiếp tục commit các thay đổi thực tế (hoặc dùng lịch sử phát triển thật của bạn) trước deadline.

