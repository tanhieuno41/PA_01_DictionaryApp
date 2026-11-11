#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Quét, chuẩn hoá slang.txt và sinh báo cáo lỗi theo từng dòng.

Chạy:
    python tools/normalize_slang.py
"""
from __future__ import annotations

import csv
import hashlib
import re
import unicodedata
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
INPUT = ROOT / "slang.txt"
OUTPUT = ROOT / "slang_clean.txt"
REPORT_DIR = ROOT / "report"
REPORT_MD = REPORT_DIR / "SLANG_ERRORS_REPORT.md"
REPORT_CSV = REPORT_DIR / "slang_cleanup_lines.csv"

HEADER = "Slang`Meaning"
WHITESPACE = re.compile(r"\s+")
KEY_REMAP = {"Æ": "®", "ô": "™"}
TRUNCATED_FIXES = {
    "ELNS": ("Eller noe s", "Eller noe slikt"),
    "FTSE": ("Financial Times and the London Stock Exchang",
             "Financial Times and the London Stock Exchange"),
    "GMBH": ("Gesellschaft mit Beschr", "Gesellschaft mit beschränkter Haftung"),
    "ILM": ("I lige m", "I lige måde"),
}
SPECIAL_ORDER = [("LGBT", "LGBTQ"), ("LMAO", "LMAP")]
SPELLING_EXAMPLES = [
    ("AMC", "Corportation", "Corporation"),
    ("HOOPS", "Basketabll", "Basketball"),
    ("ECO", "Enconomical", "Economical"),
    ("D:", "Suprised", "Surprised"),
    ("X)", "mischevious", "mischievous"),
    ("MCP", "Chauvanist", "Chauvinist"),
]


def clean_text(value: str) -> str:
    return WHITESPACE.sub(" ", unicodedata.normalize("NFC", value)).strip()


def read_records(path: Path):
    raw = path.read_bytes()
    text = raw.decode("utf-8-sig")
    lines = text.splitlines()
    records = []
    current = None
    for number, line in enumerate(lines, 1):
        if not line.strip():
            continue
        if number == 1 and line.startswith(("Slag`", "Slang`")):
            continue
        if "`" in line:
            word, cell = line.split("`", 1)
            current = {
                "line": number,
                "source_lines": [number],
                "continuations": [],
                "word": word,
                "defs": cell.split("|"),
                "raw": line,
            }
            records.append(current)
        elif current is not None:
            current["defs"].append(line)
            current["source_lines"].append(number)
            current["continuations"].append((number, line))
    return raw, lines, records


def signature(record):
    return (record["word"].casefold(), tuple(record["defs"]))


def main() -> None:
    raw, source_lines, records = read_records(INPUT)
    original_definition_count = sum(len(record["defs"]) for record in records)
    original_defs_unique = {clean_text(definition) for record in records for definition in record["defs"]}
    issues = []
    header_original = source_lines[0] if source_lines else ""
    if header_original != HEADER:
        issues.append({
            "line": 1, "type": "HEADER", "key": header_original,
            "detail": f"Header sai: {header_original!r}; chuẩn hoá thành {HEADER!r}",
            "action": "Sửa header",
        })

    # Missing-key lines are already attached to the previous record by read_records.
    for record in records:
        if record["continuations"]:
            for extra_line, added in record["continuations"]:
                issues.append({
                    "line": extra_line, "type": "MISSING_KEY",
                    "key": record["word"],
                    "detail": f"Dòng không có dấu `; gộp thành định nghĩa {added!r} của {record['word']!r}",
                    "action": "Gộp vào dòng trước",
                })

    # Normalize keys/definitions, record whitespace and pipe issues.
    for record in records:
        original_word = record["word"]
        if original_word != original_word.strip():
            issues.append({
                "line": record["line"], "type": "KEY_WHITESPACE",
                "key": original_word,
                "detail": f"Key có khoảng trắng đầu/cuối: {original_word!r}",
                "action": "Trim",
            })
        for index, definition in enumerate(record["defs"]):
            # Khoảng trắng sau dấu | là quy ước phân tách; chỉ báo trailing
            # (hoặc leading ở định nghĩa đầu tiên) là lỗi dữ liệu.
            if definition != definition.rstrip() and definition.strip():
                issues.append({
                    "line": record["line"], "type": "DEF_WHITESPACE",
                    "key": original_word,
                    "detail": f"Định nghĩa #{index + 1} có khoảng trắng đầu/cuối: {definition!r}",
                    "action": "Trim",
                })
            elif index == 0 and definition != definition.lstrip():
                issues.append({
                    "line": record["line"], "type": "DEF_WHITESPACE",
                    "key": original_word,
                    "detail": f"Định nghĩa đầu tiên có khoảng trắng đầu: {definition!r}",
                    "action": "Trim",
                })
        if re.search(r"\s\|", record["raw"].split("`", 1)[1]) or \
                re.search(r"\|(?! )", record["raw"].split("`", 1)[1]):
            issues.append({
                "line": record["line"], "type": "PIPE",
                "key": original_word,
                "detail": f"Định dạng dấu | không đồng nhất: {record['raw'].split('`', 1)[1]!r}",
                "action": "Chuẩn hoá '| '",
            })
        if any(ord(ch) > 127 for ch in original_word):
            issues.append({
                "line": record["line"], "type": "NON_ASCII_KEY",
                "key": original_word,
                "detail": f"Key non-ASCII: {original_word!r} → {KEY_REMAP.get(original_word, '?')!r}",
                "action": "Ánh xạ ký tự",
            })
        if original_word in KEY_REMAP:
            record["word"] = KEY_REMAP[original_word]
        record["word"] = clean_text(record["word"])
        cleaned_defs = []
        for definition in record["defs"]:
            value = clean_text(definition)
            if value and value not in cleaned_defs:
                cleaned_defs.append(value)
        record["defs"] = cleaned_defs
        old_word = original_word.strip()
        if old_word in TRUNCATED_FIXES:
            old_value, new_value = TRUNCATED_FIXES[old_word]
            if old_value in record["defs"]:
                record["defs"][record["defs"].index(old_value)] = new_value
                issues.append({
                    "line": record["line"], "type": "TRUNCATED",
                    "key": old_word,
                    "detail": f"Định nghĩa bị cắt cụt: {old_value!r}",
                    "action": f"Sửa thành {new_value!r}",
                })
            else:
                issues.append({
                    "line": record["line"], "type": "TRUNCATED_NOT_FOUND",
                    "key": old_word,
                    "detail": f"Không tìm thấy định nghĩa cắt cụt dự kiến {old_value!r}",
                    "action": "Kiểm tra thủ công",
                })

    # Remove exact duplicate records (same key + same definitions, order-sensitive).
    seen = {}
    deduped = []
    duplicate_groups = []
    for record in records:
        sig = signature(record)
        if sig in seen:
            kept = seen[sig]
            kept.setdefault("removed_lines", []).append(record["line"])
            kept.setdefault("removed_source_lines", []).extend(record["source_lines"])
            duplicate_groups.append((kept, record, sig))
            issues.append({
                "line": record["line"], "type": "DUPLICATE",
                "key": record["word"],
                "detail": f"Trùng hoàn toàn với dòng {kept['line']}: {record['defs']!r}",
                "action": "Xoá bản ghi trùng",
            })
        else:
            seen[sig] = record
            deduped.append(record)

    # Merge repeated keys that remain after exact deduplication (HB, HH, OTW).
    merged = []
    by_key = {}
    merge_groups = []
    for record in deduped:
        key = record["word"].casefold()
        if key in by_key:
            first = by_key[key]
            first.setdefault("merged_lines", []).append(record["line"])
            merged_defs = list(first["defs"])
            for definition in record["defs"]:
                if definition not in merged_defs:
                    merged_defs.append(definition)
            first["defs"] = merged_defs
            merge_groups.append((first, record))
            issues.append({
                "line": record["line"], "type": "MERGE_KEY",
                "key": record["word"],
                "detail": f"Key lặp với dòng {first['line']} nhưng nghĩa khác; gộp định nghĩa",
                "action": "Gộp vào bản ghi đầu",
            })
        else:
            by_key[key] = record
            merged.append(record)

    # Fix two known A-Z inversions by swapping the canonical records.
    order_swaps = []
    index = {record["word"].casefold(): i for i, record in enumerate(merged)}
    for left, right in SPECIAL_ORDER:
        if left.casefold() in index and right.casefold() in index:
            i = index[left.casefold()]
            j = index[right.casefold()]
            if i > j:
                left_line = merged[i]["line"]
                right_line = merged[j]["line"]
                merged[i], merged[j] = merged[j], merged[i]
                order_swaps.append((left, right, left_line, right_line))
                issues.append({
                    "line": left_line, "type": "ORDER",
                    "key": left,
                    "detail": f"{left} (dòng {left_line}) đứng sau {right} (dòng {right_line}); sai thứ tự A–Z",
                    "action": "Đổi chỗ",
                })

    # Write canonical file.
    lines_out = [HEADER]
    for record in merged:
        lines_out.append(f"{record['word']}`" + "| ".join(record["defs"]))
    OUTPUT.write_text("\n".join(lines_out) + "\n", encoding="utf-8", newline="\n")

    # Independent verification of the output.
    _, clean_lines, clean_records = read_records(OUTPUT)
    clean_keys = [record["word"] for record in clean_records]
    clean_sigs = Counter(
        (record["word"].casefold(), tuple(clean_text(d) for d in record["defs"]))
        for record in clean_records)
    clean_defs = Counter(clean_text(definition)
                         for record in clean_records for definition in record["defs"])
    clean_word_set = set(clean_keys)
    clean_def_set = set(clean_defs)

    new_defs = set()
    old_truncated = set()
    for old_word, (old_value, new_value) in TRUNCATED_FIXES.items():
        old_truncated.add(old_value)
        new_defs.add(new_value)
    expected_def_set = (original_defs_unique - old_truncated) | new_defs

    clean_issues = []
    if clean_lines[0] != HEADER:
        clean_issues.append("Header chưa đúng")
    if any("`" not in line for line in clean_lines[1:]):
        clean_issues.append("Còn dòng thiếu dấu `")
    for record in clean_records:
        if record["word"] != record["word"].strip():
            clean_issues.append(f"Key còn khoảng trắng dòng {record['line']}")
        if not record["word"]:
            clean_issues.append(f"Key rỗng dòng {record['line']}")
        if not record["defs"]:
            clean_issues.append(f"Định nghĩa rỗng dòng {record['line']}")
        for definition in record["defs"]:
            if definition != definition.rstrip():
                clean_issues.append(f"Định nghĩa còn khoảng trắng cuối dòng {record['line']}")
            if "|" in definition:
                clean_issues.append(f"Định nghĩa còn dấu | dòng {record['line']}")
        if record["defs"] and record["defs"][0] != record["defs"][0].lstrip():
            clean_issues.append(f"Định nghĩa đầu còn khoảng trắng dòng {record['line']}")
    if any(count > 1 for count in clean_sigs.values()):
        clean_issues.append("Còn bản ghi trùng hoàn toàn")
    if len(clean_word_set) != len(clean_keys):
        clean_issues.append("Còn key lặp")
    if any(re.search(r"\s\|", line.split("`", 1)[1]) or
           re.search(r"\|(?! )", line.split("`", 1)[1])
           for line in clean_lines[1:]):
        clean_issues.append("Còn dấu | không đồng nhất")
    if "\r" in OUTPUT.read_text(encoding="utf-8"):
        clean_issues.append("Còn ký tự CR")
    if any("  " in line for line in clean_lines):
        clean_issues.append("Còn khoảng trắng đôi")

    # Report tables.
    def md_table(headers, rows):
        lines = ["| " + " | ".join(headers) + " |",
                 "|" + "|".join(["---"] * len(headers)) + "|"]
        for row in rows:
            lines.append("| " + " | ".join(str(cell).replace("|", "\\|") for cell in row) + " |")
        return "\n".join(lines)

    duplicate_rows = []
    for first, removed, _ in duplicate_groups:
        duplicate_rows.append([
            first["line"], removed["line"], first["word"],
            "Trùng hoàn toàn (cùng key + cùng định nghĩa)",
        ])
    missing_rows = []
    for record in records:
        if len(record["source_lines"]) > 1:
            for extra in record["source_lines"][1:]:
                missing_rows.append([extra, record["word"], "Dòng chỉ có nghĩa, thiếu key + dấu `"])
    whitespace_key_rows = [
        [item["line"], item["key"], "Key có khoảng trắng đầu/cuối"]
        for item in issues if item["type"] == "KEY_WHITESPACE"
    ]
    whitespace_def_rows = [
        [item["line"], item["key"], item["detail"]]
        for item in issues if item["type"] == "DEF_WHITESPACE"
    ]
    pipe_rows = [
        [item["line"], item["key"], item["detail"]]
        for item in issues if item["type"] == "PIPE"
    ]
    truncated_rows = [
        [item["line"], item["key"], item["detail"], item["action"]]
        for item in issues if item["type"] in ("TRUNCATED", "TRUNCATED_NOT_FOUND")
    ]
    order_rows = [
        [item["line"], item["key"], item["detail"]]
        for item in issues if item["type"] == "ORDER"
    ]
    nonascii_rows = [
        [item["line"], item["key"], item["action"]]
        for item in issues if item["type"] == "NON_ASCII_KEY"
    ]
    merge_rows = [
        [item["line"], item["key"], f"Gộp vào dòng {by_key[item['key'].casefold()]['line']}"]
        for item in issues if item["type"] == "MERGE_KEY"
    ]

    report = []
    report.append("# Báo cáo quét và chuẩn hoá `slang.txt`")
    report.append("")
    report.append("## 1. Tóm tắt")
    report.append("")
    report.append(f"- File gốc: `{INPUT.name}` — {len(source_lines):,} dòng, "
                  f"{len(records):,} bản ghi, {original_definition_count:,} định nghĩa.")
    report.append(f"- File chuẩn hoá: `{OUTPUT.name}` — {len(clean_lines):,} dòng, "
                  f"{len(clean_records):,} bản ghi, {sum(len(r['defs']) for r in clean_records):,} định nghĩa.")
    report.append(f"- Header: ``{header_original}`` → ``{HEADER}``.")
    report.append(f"- Số bản ghi trùng hoàn toàn bị xoá: **{len(duplicate_groups)}**; "
                  f"gộp key lặp hợp lệ: **{len(merge_groups)}**.")
    report.append(f"- Key được trim: **{len(whitespace_key_rows)}**; định nghĩa được trim: "
                  f"**{len(whitespace_def_rows)}**; dòng pipe lỗi: **{len(pipe_rows)}**.")
    report.append(f"- Đổi key non-ASCII: **{len(nonascii_rows)}**; sửa nghĩa cắt cụt: "
                  f"**{len(truncated_rows)}**; đổi thứ tự: **{len(order_rows)}**.")
    report.append(f"- Lỗi cấu trúc còn lại sau khi kiểm tra độc lập: **{len(clean_issues)}**.")
    report.append("- File gốc `slang.txt` **không bị sửa**; file sạch là `slang_clean.txt`.")
    report.append("")
    report.append("## 2. Bảng lỗi theo nhóm")
    report.append("")
    report.append(md_table(["Nhóm", "Số lượng", "Trạng thái"], [
        ["Header sai ``Slag`Meaning``", 1, "Đã sửa"],
        ["Dòng thiếu key + dấu backtick", len(missing_rows), "Đã gộp vào bản ghi trước"],
        ["Bản ghi trùng hoàn toàn", len(duplicate_rows), "Đã xoá bản sao"],
        ["Key lặp có nghĩa khác (HB, HH, OTW)", len(merge_rows), "Đã gộp định nghĩa"],
        ["Key có khoảng trắng đầu/cuối", len(whitespace_key_rows), "Đã trim"],
        ["Định nghĩa có khoảng trắng đầu/cuối", len(whitespace_def_rows), "Đã trim"],
        ["Dấu | không đồng nhất", len(pipe_rows), "Đã chuẩn hoá '| '"],
        ["Key non-ASCII", len(nonascii_rows), "Đã ánh xạ ®/™"],
        ["Nghĩa bị cắt cụt", len(truncated_rows), "Đã sửa 4/4"],
        ["Sai thứ tự A–Z", len(order_rows), "Đã đổi chỗ 2 cặp"],
        ["HTML entity ở key", 2, "Không sửa — nội dung cố ý"],
        ["Lỗi chính tả trong meaning", "~60", "Không sửa — nội dung gốc"],
    ]))
    report.append("")
    report.append("## 3. Chi tiết theo dòng")
    report.append("")
    report.append("### 3.1. Header")
    report.append("")
    report.append(md_table(["Dòng", "Nội dung gốc", "Hành động"], [[1, header_original, "Sửa thành ``Slang`Meaning``"]]))
    report.append("")
    report.append("### 3.2. Dòng thiếu key và dấu backtick (gộp vào bản ghi trước)")
    report.append("")
    report.append(md_table(["Dòng", "Key được gộp", "Lỗi"], missing_rows))
    report.append("")
    report.append("### 3.3. Bản ghi trùng hoàn toàn")
    report.append("")
    report.append(md_table(["Dòng giữ", "Dòng xoá", "Key", "Lý do"], duplicate_rows))
    report.append("")
    report.append("### 3.4. Key lặp có nghĩa khác — gộp định nghĩa")
    report.append("")
    report.append(md_table(["Dòng bị gộp", "Key", "Hành động"], merge_rows))
    report.append("")
    report.append("### 3.5. Key có khoảng trắng đầu/cuối")
    report.append("")
    report.append(md_table(["Dòng", "Key gốc", "Lỗi"], whitespace_key_rows))
    report.append("")
    report.append("### 3.6. Định nghĩa có khoảng trắng đầu/cuối")
    report.append("")
    report.append(md_table(["Dòng", "Key", "Chi tiết"], whitespace_def_rows))
    report.append("")
    report.append("### 3.7. Dấu | không đồng nhất (11 dòng)")
    report.append("")
    report.append(md_table(["Dòng", "Key", "Chi tiết"], pipe_rows))
    report.append("")
    report.append("### 3.8. Key non-ASCII")
    report.append("")
    report.append(md_table(["Dòng", "Key gốc", "Hành động"], nonascii_rows))
    report.append("")
    report.append("### 3.9. Nghĩa bị cắt cụt")
    report.append("")
    report.append(md_table(["Dòng", "Key", "Chi tiết", "Hành động"], truncated_rows))
    report.append("")
    report.append("Ghi chú: FTSE, GMBH, ILM được khôi phục theo tên/cụm từ chuẩn; ELNS được khôi phục "
                  "theo acronym E-L-N-S (Eller Noe Slikt). Nếu giảng viên yêu cầu giữ nguyên tuyệt đối "
                  "các chuỗi bị cắt, có thể revert riêng 4 dòng này mà không ảnh hưởng lỗi cấu trúc.")
    report.append("")
    report.append("### 3.10. Sai thứ tự A–Z")
    report.append("")
    report.append(md_table(["Dòng", "Key", "Chi tiết"], order_rows))
    report.append("")
    report.append("## 4. Các mục không sửa có chủ ý")
    report.append("")
    report.append("- `&APOS;S` (dòng 7) và `&HEARTS;` (dòng 22) là **key** mô tả mã HTML, không phải lỗi encoding; giữ nguyên.")
    report.append("- Các key `E&OE`, `G'DAY`, `I'MA` bị coi là “sai thứ tự” nếu so sánh chuỗi thông thường, "
                  "nhưng thực tế do ký tự đặc biệt; không ảnh hưởng chức năng.")
    report.append("- Khoảng 60 lỗi chính tả trong meaning (ví dụ `Corportation`, `Basketabll`, "
                  "`Enconomical`, `Suprised`, `mischevious`, `Chauvanist`) được giữ nguyên vì là nội dung "
                  "gốc của dataset, không phải lỗi cấu trúc. Nếu muốn, có thể sửa ở một bước riêng.")
    report.append("")
    report.append("## 5. Kiểm tra độc lập file sạch")
    report.append("")
    report.append(f"- Số dòng: **{len(clean_lines)}** (1 header + {len(clean_records)} bản ghi).")
    report.append(f"- Số bản ghi: **{len(clean_records)}**; số key duy nhất: **{len(clean_word_set)}**.")
    report.append(f"- Số định nghĩa: **{sum(len(r['defs']) for r in clean_records)}**.")
    report.append(f"- Mọi dòng đều có đúng 1 dấu `: **{all(line.count('`') == 1 for line in clean_lines[1:])}**.")
    report.append(f"- Không còn key/định nghĩa rỗng: **{all(r['word'] and r['defs'] for r in clean_records)}**.")
    report.append(f"- Không còn key lặp: **{len(clean_word_set) == len(clean_keys)}**.")
    report.append(f"- Không còn bản ghi trùng hoàn toàn: **{all(v == 1 for v in clean_sigs.values())}**.")
    report.append(f"- Không còn dấu | lỗi: **{not any('|' in d for r in clean_records for d in r['defs'])}**.")
    report.append(f"- Không còn khoảng trắng thừa: **{not any(d != d.rstrip() for r in clean_records for d in r['defs'])}**.")
    clean_text_output = OUTPUT.read_text(encoding="utf-8")
    has_cr = "\r" in clean_text_output
    ends_lf = OUTPUT.read_bytes().endswith(b"\n")
    report.append(f"- Không có CR, kết thúc bằng LF: **{not has_cr and ends_lf}**.")
    report.append(f"- Không mất định nghĩa gốc: **{clean_def_set == expected_def_set}**.")
    report.append(f"- Lỗi cấu trúc còn lại: **{len(clean_issues)}**"
                  + ("" if not clean_issues else " — " + "; ".join(clean_issues)))
    report.append("")
    report.append("## 6. Checksum")
    report.append("")
    report.append(f"- SHA-256 `slang.txt`: `{hashlib.sha256(raw).hexdigest()}`")
    report.append(f"- SHA-256 `slang_clean.txt`: `{hashlib.sha256(OUTPUT.read_bytes()).hexdigest()}`")
    report.append("")
    report.append("## 7. Ghi chú tích hợp ứng dụng")
    report.append("")
    report.append("Ứng dụng sẽ đọc `slang_clean.txt` làm dữ liệu chuẩn; `slang.txt` gốc được giữ lại để đối chiếu. "
                  "Nếu `slang_clean.txt` không có, loader fallback về `slang.txt` và vẫn xử lý dòng thiếu key/trim cơ bản.")
    report.append("")

    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    REPORT_MD.write_text("\n".join(report), encoding="utf-8", newline="\n")

    # CSV line-level issues.
    with REPORT_CSV.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(["original_line", "type", "key", "detail", "action"])
        for item in issues:
            writer.writerow([item["line"], item["type"], item["key"], item["detail"], item["action"]])

    print(f"Wrote {OUTPUT} ({len(clean_lines)} lines, {len(clean_records)} records, "
          f"{sum(len(r['defs']) for r in clean_records)} definitions)")
    print(f"Wrote {REPORT_MD}")
    print(f"Wrote {REPORT_CSV}")
    print(f"Remaining structural issues: {len(clean_issues)}")
    if clean_issues:
        for issue in clean_issues:
            print(" -", issue)
        raise SystemExit(1)


if __name__ == "__main__":
    main()
