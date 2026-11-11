# Báo cáo quét và chuẩn hoá `slang.txt`

## 1. Tóm tắt

- File gốc: `slang.txt` — 7,662 dòng, 7,656 bản ghi, 8,959 định nghĩa.
- File chuẩn hoá: `slang_clean.txt` — 7,639 dòng, 7,638 bản ghi, 8,941 định nghĩa.
- Header: ``Slag`Meaning`` → ``Slang`Meaning``.
- Số bản ghi trùng hoàn toàn bị xoá: **15**; gộp key lặp hợp lệ: **3**.
- Key được trim: **14**; định nghĩa được trim: **14**; dòng pipe lỗi: **11**.
- Đổi key non-ASCII: **2**; sửa nghĩa cắt cụt: **4**; đổi thứ tự: **2**.
- Lỗi cấu trúc còn lại sau khi kiểm tra độc lập: **0**.
- File gốc `slang.txt` **không bị sửa**; file sạch là `slang_clean.txt`.

## 2. Bảng lỗi theo nhóm

| Nhóm | Số lượng | Trạng thái |
|---|---|---|
| Header sai ``Slag`Meaning`` | 1 | Đã sửa |
| Dòng thiếu key + dấu backtick | 5 | Đã gộp vào bản ghi trước |
| Bản ghi trùng hoàn toàn | 15 | Đã xoá bản sao |
| Key lặp có nghĩa khác (HB, HH, OTW) | 3 | Đã gộp định nghĩa |
| Key có khoảng trắng đầu/cuối | 14 | Đã trim |
| Định nghĩa có khoảng trắng đầu/cuối | 14 | Đã trim |
| Dấu \| không đồng nhất | 11 | Đã chuẩn hoá '\| ' |
| Key non-ASCII | 2 | Đã ánh xạ ®/™ |
| Nghĩa bị cắt cụt | 4 | Đã sửa 4/4 |
| Sai thứ tự A–Z | 2 | Đã đổi chỗ 2 cặp |
| HTML entity ở key | 2 | Không sửa — nội dung cố ý |
| Lỗi chính tả trong meaning | ~60 | Không sửa — nội dung gốc |

## 3. Chi tiết theo dòng

### 3.1. Header

| Dòng | Nội dung gốc | Hành động |
|---|---|---|
| 1 | Slag`Meaning | Sửa thành ``Slang`Meaning`` |

### 3.2. Dòng thiếu key và dấu backtick (gộp vào bản ghi trước)

| Dòng | Key được gộp | Lỗi |
|---|---|---|
| 413 | AMA | Dòng chỉ có nghĩa, thiếu key + dấu ` |
| 908 | BOMB | Dòng chỉ có nghĩa, thiếu key + dấu ` |
| 1112 | C4C | Dòng chỉ có nghĩa, thiếu key + dấu ` |
| 2004 | ER | Dòng chỉ có nghĩa, thiếu key + dấu ` |
| 2882 | HOODIE | Dòng chỉ có nghĩa, thiếu key + dấu ` |

### 3.3. Bản ghi trùng hoàn toàn

| Dòng giữ | Dòng xoá | Key | Lý do |
|---|---|---|---|
| 567 | 568 | AWS | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 1214 | 1215 | CHEESE | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 2183 | 2184 | FLAGGED | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 2344 | 2522 | GOAT | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 3468 | 3470 | JGI | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 3469 | 3471 | JGL | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 4117 | 4119 | MCR | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 4118 | 4120 | MD | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 4441 | 4442 | NAE | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 5087 | 5088 | PBUH | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 5409 | 5411 | PYP | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 5410 | 5412 | PYT | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 6055 | 6056 | SOWIE | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 6378 | 6379 | TEOTWAWKI | Trùng hoàn toàn (cùng key + cùng định nghĩa) |
| 7336 | 7337 | WUWU | Trùng hoàn toàn (cùng key + cùng định nghĩa) |

### 3.4. Key lặp có nghĩa khác — gộp định nghĩa

| Dòng bị gộp | Key | Hành động |
|---|---|---|
| 2738 | HB | Gộp vào dòng 2737 |
| 2800 | HH | Gộp vào dòng 2799 |
| 5002 | OTW | Gộp vào dòng 5001 |

### 3.5. Key có khoảng trắng đầu/cuối

| Dòng | Key gốc | Lỗi |
|---|---|---|
| 1080 | BW  | Key có khoảng trắng đầu/cuối |
| 2755 | HD  | Key có khoảng trắng đầu/cuối |
| 2825 |  HK | Key có khoảng trắng đầu/cuối |
| 2941 | HUNNED  | Key có khoảng trắng đầu/cuối |
| 4104 | MBFAM  | Key có khoảng trắng đầu/cuối |
| 4119 |  MCR | Key có khoảng trắng đầu/cuối |
| 4388 | MW2  | Key có khoảng trắng đầu/cuối |
| 4911 | ONG  | Key có khoảng trắng đầu/cuối |
| 6194 | SWAGG  | Key có khoảng trắng đầu/cuối |
| 6222 | SWND  | Key có khoảng trắng đầu/cuối |
| 6425 | THANQ  | Key có khoảng trắng đầu/cuối |
| 6443 | THT  | Key có khoảng trắng đầu/cuối |
| 6784 | UBER  | Key có khoảng trắng đầu/cuối |
| 7061 | WCB  | Key có khoảng trắng đầu/cuối |

### 3.6. Định nghĩa có khoảng trắng đầu/cuối

| Dòng | Key | Chi tiết |
|---|---|---|
| 65 | +1 | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Partner ' |
| 83 | /:) | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Flirtation ' |
| 162 | 8-) | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Cool ' |
| 163 | 8-\| | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Nerdy ' |
| 1089 | BWW | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Buffalo Wild Wings ' |
| 1160 | CBR | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Captive Bead Ring ' |
| 1448 | CULO | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Ass ' |
| 1989 | EOT | Định nghĩa #1 có khoảng trắng đầu/cuối: 'End Of Thread ' |
| 2564 | GPC | Định nghĩa #2 có khoảng trắng đầu/cuối: ' Get/Post/Cookie ' |
| 3028 | ICC | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Ice Crown Citadel ' |
| 3442 | JCB | Định nghĩa #1 có khoảng trắng đầu/cuối: 'J C Bamford ' |
| 3921 | LOOT | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Money ' |
| 5289 | POTW | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Patient of the Week ' |
| 6040 | SOOC | Định nghĩa #1 có khoảng trắng đầu/cuối: 'Straight Out Of Camera ' |

### 3.7. Dấu | không đồng nhất (11 dòng)

| Dòng | Key | Chi tiết |
|---|---|---|
| 13 | >3 | Định dạng dấu \| không đồng nhất: 'Evil but happy\|Hate' |
| 65 | +1 | Định dạng dấu \| không đồng nhất: 'Partner \|Me too' |
| 83 | /:) | Định dạng dấu \| không đồng nhất: 'Flirtation \|Sign Of Suspicion' |
| 353 | AI | Định dạng dấu \| không đồng nhất: 'Artificial Intelligence\|As If' |
| 1137 | CAPS | Định dạng dấu \| không đồng nhất: 'Bullets\|Mushrooms' |
| 1989 | EOT | Định dạng dấu \| không đồng nhất: 'End Of Thread \|end of discussion' |
| 2295 | FTC | Định dạng dấu \| không đồng nhất: 'Favorite\|Thumbs up\| Comment' |
| 3442 | JCB | Định dạng dấu \| không đồng nhất: 'J C Bamford \| excavator manufacturer' |
| 3742 | LATE | Định dạng dấu \| không đồng nhất: 'Missed a period\| possibly pregnant\|Dead' |
| 5349 | PSI | Định dạng dấu \| không đồng nhất: 'Pounds per Square Inch (pressure)\|Greek letter' |
| 6040 | SOOC | Định dạng dấu \| không đồng nhất: 'Straight Out Of Camera \|not Photoshopped' |

### 3.8. Key non-ASCII

| Dòng | Key gốc | Hành động |
|---|---|---|
| 7661 | Æ | Ánh xạ ký tự |
| 7662 | ô | Ánh xạ ký tự |

### 3.9. Nghĩa bị cắt cụt

| Dòng | Key | Chi tiết | Hành động |
|---|---|---|---|
| 1958 | ELNS | Định nghĩa bị cắt cụt: 'Eller noe s' | Sửa thành 'Eller noe slikt' |
| 2308 | FTSE | Định nghĩa bị cắt cụt: 'Financial Times and the London Stock Exchang' | Sửa thành 'Financial Times and the London Stock Exchange' |
| 2500 | GMBH | Định nghĩa bị cắt cụt: 'Gesellschaft mit Beschr' | Sửa thành 'Gesellschaft mit beschränkter Haftung' |
| 3185 | ILM | Định nghĩa bị cắt cụt: 'I lige m' | Sửa thành 'I lige måde' |

Ghi chú: FTSE, GMBH, ILM được khôi phục theo tên/cụm từ chuẩn; ELNS được khôi phục theo acronym E-L-N-S (Eller Noe Slikt). Nếu giảng viên yêu cầu giữ nguyên tuyệt đối các chuỗi bị cắt, có thể revert riêng 4 dòng này mà không ảnh hưởng lỗi cấu trúc.

### 3.10. Sai thứ tự A–Z

| Dòng | Key | Chi tiết |
|---|---|---|
| 3806 | LGBT | LGBT (dòng 3806) đứng sau LGBTQ (dòng 3805); sai thứ tự A–Z |
| 3868 | LMAO | LMAO (dòng 3868) đứng sau LMAP (dòng 3867); sai thứ tự A–Z |

## 4. Các mục không sửa có chủ ý

- `&APOS;S` (dòng 7) và `&HEARTS;` (dòng 22) là **key** mô tả mã HTML, không phải lỗi encoding; giữ nguyên.
- Các key `E&OE`, `G'DAY`, `I'MA` bị coi là “sai thứ tự” nếu so sánh chuỗi thông thường, nhưng thực tế do ký tự đặc biệt; không ảnh hưởng chức năng.
- Khoảng 60 lỗi chính tả trong meaning (ví dụ `Corportation`, `Basketabll`, `Enconomical`, `Suprised`, `mischevious`, `Chauvanist`) được giữ nguyên vì là nội dung gốc của dataset, không phải lỗi cấu trúc. Nếu muốn, có thể sửa ở một bước riêng.

## 5. Kiểm tra độc lập file sạch

- Số dòng: **7639** (1 header + 7638 bản ghi).
- Số bản ghi: **7638**; số key duy nhất: **7638**.
- Số định nghĩa: **8941**.
- Mọi dòng đều có đúng 1 dấu `: **True**.
- Không còn key/định nghĩa rỗng: **True**.
- Không còn key lặp: **True**.
- Không còn bản ghi trùng hoàn toàn: **True**.
- Không còn dấu | lỗi: **True**.
- Không còn khoảng trắng thừa: **True**.
- Không có CR, kết thúc bằng LF: **True**.
- Không mất định nghĩa gốc: **True**.
- Lỗi cấu trúc còn lại: **0**

## 6. Checksum

- SHA-256 `slang.txt`: `d905c5fbf77b105e44fea4f3ed8fa616f49cc04f84b3930694fd1867a6cf51b2`
- SHA-256 `slang_clean.txt`: `1edad148e920a29f7375c2530b3e41a4c4f3c0af925100b6176f90890d703931`

## 7. Ghi chú tích hợp ứng dụng

Ứng dụng sẽ đọc `slang_clean.txt` làm dữ liệu chuẩn; `slang.txt` gốc được giữ lại để đối chiếu. Nếu `slang_clean.txt` không có, loader fallback về `slang.txt` và vẫn xử lý dòng thiếu key/trim cơ bản.
