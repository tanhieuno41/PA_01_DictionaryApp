#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Sinh báo cáo đồ án Slang Dictionary (DOCX, đen–trắng).

Chạy từ thư mục gốc:
    python tools/build_report.py
"""
from __future__ import annotations

from pathlib import Path

from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK, WD_LINE_SPACING
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

ROOT = Path(__file__).resolve().parent.parent
SCREENS = ROOT / "report" / "screens"
OUT = ROOT / "report" / "BAO_CAO_DO_AN.docx"

BLACK = RGBColor(0, 0, 0)
GRAY = RGBColor(0x52, 0x52, 0x52)
FONT = "Times New Roman"
_number_counter = 0
MONO = "Consolas"


# ---------------------------------------------------------------------------
# Low-level helpers
# ---------------------------------------------------------------------------
def set_run(run, size=13, bold=False, italic=False, color=BLACK, font=FONT):
    run.font.name = font
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.italic = italic
    run.font.color.rgb = color
    run._element.rPr.rFonts.set(qn("w:eastAsia"), font)
    return run


def set_paragraph_shading(paragraph, fill="F2F2F2"):
    p_pr = paragraph._p.get_or_add_pPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), fill)
    p_pr.append(shd)


def set_cell_shading(cell, fill="D9D9D9"):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), fill)
    tc_pr.append(shd)


def set_cell_text(cell, text, bold=False, size=11, align=WD_ALIGN_PARAGRAPH.LEFT):
    cell.text = ""
    paragraph = cell.paragraphs[0]
    paragraph.alignment = align
    paragraph.paragraph_format.space_after = Pt(0)
    run = paragraph.add_run(str(text))
    set_run(run, size=size, bold=bold)


def set_table_borders(table):
    tbl = table._tbl
    tbl_pr = tbl.tblPr
    borders = OxmlElement("w:tblBorders")
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        element = OxmlElement(f"w:{edge}")
        element.set(qn("w:val"), "single")
        element.set(qn("w:sz"), "4")
        element.set(qn("w:space"), "0")
        element.set(qn("w:color"), "000000")
        borders.append(element)
    tbl_pr.append(borders)


def add_page_number_footer(section):
    paragraph = section.footer.paragraphs[0]
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    paragraph.paragraph_format.space_before = Pt(0)
    run = paragraph.add_run("Trang ")
    set_run(run, size=9, color=GRAY)
    run = paragraph.add_run()
    fld_begin = OxmlElement("w:fldChar")
    fld_begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = "PAGE"
    fld_end = OxmlElement("w:fldChar")
    fld_end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_begin)
    run._r.append(instr)
    run._r.append(fld_end)
    run = paragraph.add_run(" / ")
    set_run(run, size=9, color=GRAY)
    run = paragraph.add_run()
    fld_begin = OxmlElement("w:fldChar")
    fld_begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = "NUMPAGES"
    fld_end = OxmlElement("w:fldChar")
    fld_end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_begin)
    run._r.append(instr)
    run._r.append(fld_end)


def add_header(section):
    paragraph = section.header.paragraphs[0]
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = paragraph.add_run("CSC13002 – Lập trình ứng dụng Java  ·  Đồ án #1: Slang Dictionary")
    set_run(run, size=8.5, color=GRAY)


def add_toc_field(document):
    paragraph = document.add_paragraph()
    paragraph.paragraph_format.space_after = Pt(4)
    run = paragraph.add_run()
    fld_begin = OxmlElement("w:fldChar")
    fld_begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = 'TOC \\o "1-1" \\h \\z \\u'
    fld_sep = OxmlElement("w:fldChar")
    fld_sep.set(qn("w:fldCharType"), "separate")
    placeholder = OxmlElement("w:t")
    placeholder.text = "Nhấn chuột phải vào mục lục và chọn Update Field để cập nhật số trang."
    fld_end = OxmlElement("w:fldChar")
    fld_end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_begin)
    run._r.append(instr)
    run._r.append(fld_sep)
    run._r.append(placeholder)
    run._r.append(fld_end)


def force_update_fields(document):
    settings = document.settings.element
    update = OxmlElement("w:updateFields")
    update.set(qn("w:val"), "true")
    settings.append(update)


def h1(document, text):
    global _number_counter
    _number_counter = 0
    paragraph = document.add_heading(level=1)
    paragraph.paragraph_format.keep_with_next = True
    paragraph.add_run(text)
    return paragraph


def h2(document, text):
    global _number_counter
    _number_counter = 0
    paragraph = document.add_heading(level=2)
    paragraph.paragraph_format.keep_with_next = True
    paragraph.add_run(text)
    return paragraph


def h3(document, text):
    global _number_counter
    _number_counter = 0
    paragraph = document.add_heading(level=3)
    paragraph.paragraph_format.keep_with_next = True
    paragraph.add_run(text)
    return paragraph


def para(document, text="", bold=False, italic=False, size=13, color=BLACK,
         align=WD_ALIGN_PARAGRAPH.JUSTIFY, space_after=5):
    paragraph = document.add_paragraph()
    paragraph.alignment = align
    paragraph.paragraph_format.space_after = Pt(space_after)
    if text:
        set_run(paragraph.add_run(text), size=size, bold=bold, italic=italic, color=color)
    return paragraph


def bullet(document, text, level=0):
    style = "List Bullet" if level == 0 else "List Bullet 2"
    paragraph = document.add_paragraph(style=style)
    paragraph.paragraph_format.space_after = Pt(2)
    set_run(paragraph.add_run(text), size=12.5)
    return paragraph


def numbered(document, text):
    global _number_counter
    _number_counter += 1
    paragraph = document.add_paragraph()
    paragraph.paragraph_format.left_indent = Cm(0.75)
    paragraph.paragraph_format.first_line_indent = Cm(-0.5)
    paragraph.paragraph_format.space_after = Pt(2)
    set_run(paragraph.add_run(f"{_number_counter}. "), size=12.5)
    set_run(paragraph.add_run(text), size=12.5)
    return paragraph


def caption(document, text):
    paragraph = document.add_paragraph()
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    paragraph.paragraph_format.space_after = Pt(9)
    set_run(paragraph.add_run(text), size=10, italic=True, color=GRAY)


def image(document, filename, caption_text, width_cm=15.4):
    path = SCREENS / filename
    if not path.exists():
        para(document, f"[Thiếu ảnh: {filename}]", italic=True, color=GRAY,
             align=WD_ALIGN_PARAGRAPH.CENTER)
        return
    paragraph = document.add_paragraph()
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    paragraph.paragraph_format.space_after = Pt(2)
    run = paragraph.add_run()
    run.add_picture(str(path), width=Cm(width_cm))
    caption(document, caption_text)


def code_block(document, text):
    paragraph = document.add_paragraph()
    paragraph.paragraph_format.left_indent = Cm(0.5)
    paragraph.paragraph_format.right_indent = Cm(0.5)
    paragraph.paragraph_format.space_before = Pt(4)
    paragraph.paragraph_format.space_after = Pt(8)
    paragraph.paragraph_format.keep_together = True
    set_paragraph_shading(paragraph, "F2F2F2")
    lines = text.split("\n")
    for index, line in enumerate(lines):
        if index > 0:
            paragraph.add_run().add_break()
        set_run(paragraph.add_run(line), size=9.5, font=MONO)


def table(document, headers, rows, widths=None, font_size=10.5, header_fill="D9D9D9"):
    table_obj = document.add_table(rows=1, cols=len(headers))
    table_obj.alignment = WD_TABLE_ALIGNMENT.CENTER
    table_obj.style = "Table Grid"
    table_obj.autofit = True
    set_table_borders(table_obj)

    header_cells = table_obj.rows[0].cells
    for index, header in enumerate(headers):
        set_cell_text(header_cells[index], header, bold=True, size=font_size)
        set_cell_shading(header_cells[index], header_fill)
        header_cells[index].vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        tr_pr = table_obj.rows[0]._tr.get_or_add_trPr()
        tbl_header = OxmlElement("w:tblHeader")
        tbl_header.set(qn("w:val"), "true")
        tr_pr.append(tbl_header)

    for row in rows:
        cells = table_obj.add_row().cells
        for index, value in enumerate(row):
            set_cell_text(cells[index], value, size=font_size)
            cells[index].vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        cells[0].paragraphs[0].runs[0].font.bold = False

    if widths:
        for index, width in enumerate(widths):
            for row in table_obj.rows:
                row.cells[index].width = Cm(width)
    paragraph = document.add_paragraph()
    paragraph.paragraph_format.space_after = Pt(6)
    return table_obj


def section_break(document):
    document.add_page_break()


# ---------------------------------------------------------------------------
# Build document
# ---------------------------------------------------------------------------
def build():
    document = Document()

    section = document.sections[0]
    section.page_width = Cm(21.0)
    section.page_height = Cm(29.7)
    section.top_margin = Cm(2.0)
    section.bottom_margin = Cm(2.0)
    section.left_margin = Cm(2.5)
    section.right_margin = Cm(2.0)
    section.header_distance = Cm(1.0)
    section.footer_distance = Cm(1.0)
    section.different_first_page_header_footer = True

    # Base styles: everything black on white.
    normal = document.styles["Normal"]
    normal.font.name = FONT
    normal.font.size = Pt(13)
    normal.font.color.rgb = BLACK
    normal.paragraph_format.line_spacing = 1.3
    normal.paragraph_format.space_after = Pt(5)
    normal._element.rPr.rFonts.set(qn("w:eastAsia"), FONT)

    for style_name, size in (("Heading 1", 16), ("Heading 2", 14), ("Heading 3", 13)):
        style = document.styles[style_name]
        style.font.name = FONT
        style.font.size = Pt(size)
        style.font.bold = True
        style.font.color.rgb = BLACK
        style.paragraph_format.space_before = Pt(12 if style_name != "Heading 1" else 16)
        style.paragraph_format.space_after = Pt(6)
        style.paragraph_format.keep_with_next = True
        style._element.rPr.rFonts.set(qn("w:eastAsia"), FONT)

    for style_name in ("List Bullet", "List Bullet 2", "List Number"):
        style = document.styles[style_name]
        style.font.name = FONT
        style.font.size = Pt(12.5)
        style.font.color.rgb = BLACK

    for style_name in ("TOC 1", "TOC 2", "TOC 3"):
        try:
            style = document.styles[style_name]
            style.font.name = FONT
            style.font.size = Pt(11)
            style.font.color.rgb = BLACK
            style.paragraph_format.space_after = Pt(1)
            style.paragraph_format.line_spacing = 1.0
        except KeyError:
            pass

    add_header(section)
    add_page_number_footer(section)
    force_update_fields(document)

    # ------------------------------------------------------------------
    # Cover page
    # ------------------------------------------------------------------
    for _ in range(2):
        document.add_paragraph()
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("TRƯỜNG ĐẠI HỌC KHOA HỌC TỰ NHIÊN – ĐHQG-HCM"), size=13, bold=True)
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("KHOA CÔNG NGHỆ THÔNG TIN"), size=12.5, bold=True)
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("──────────  ◆  ──────────"), size=11, color=GRAY)

    for _ in range(2):
        document.add_paragraph()
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("ĐỒ ÁN MÔN HỌC"), size=15, bold=True)
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("CSC13002 – LẬP TRÌNH ỨNG DỤNG JAVA"), size=13, bold=True)

    for _ in range(2):
        document.add_paragraph()
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("ĐỒ ÁN #1"), size=22, bold=True)
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("SLANG DICTIONARY"), size=30, bold=True)
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("Ứng dụng từ điển slang word bằng Java Swing"), size=14, italic=True)

    for _ in range(2):
        document.add_paragraph()
    info = document.add_table(rows=5, cols=2)
    info.alignment = WD_TABLE_ALIGNMENT.CENTER
    info.style = "Table Grid"
    set_table_borders(info)
    labels = [
        ("Giảng viên phụ trách", "Nguyễn Văn Khiết (LT) – Nguyễn Đức Huy (TG) – Hồ Tuấn Thanh (TH)"),
        ("Sinh viên thực hiện", "................................................................"),
        ("MSSV", "................................................................"),
        ("Lớp", "................................................................"),
        ("Thời gian", "Tháng 11 năm 2026"),
    ]
    for index, (label, value) in enumerate(labels):
        set_cell_text(info.rows[index].cells[0], label, bold=True, size=11)
        set_cell_text(info.rows[index].cells[1], value, size=11)
        info.rows[index].cells[0].width = Cm(5.0)
        info.rows[index].cells[1].width = Cm(10.5)

    for _ in range(2):
        document.add_paragraph()
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("TP. Hồ Chí Minh, tháng 11 năm 2026"), size=11, italic=True, color=GRAY)

    section_break(document)

    # ------------------------------------------------------------------
    # Table of contents
    # ------------------------------------------------------------------
    p = document.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(10)
    set_run(p.add_run("MỤC LỤC"), size=17, bold=True)
    add_toc_field(document)
    section_break(document)

    # ------------------------------------------------------------------
    # 1. Introduction
    # ------------------------------------------------------------------
    h1(document, "1. GIỚI THIỆU")
    h2(document, "1.1. Bối cảnh và mục tiêu")
    para(document, "Đồ án #1 của môn CSC13002 yêu cầu xây dựng một ứng dụng từ điển slang word "
                   "bằng Java, vận dụng Java IO, lập trình hướng đối tượng, Generic và Collections. "
                   "Ứng dụng phải hỗ trợ tra cứu nhanh, lưu dữ liệu sau khi thay đổi và đáp ứng "
                   "10 chức năng theo mô tả của đề bài.")
    para(document, "Mục tiêu của đồ án là tạo ra một ứng dụng desktop hoàn chỉnh, có giao diện "
                   "dễ sử dụng, dữ liệu được tổ chức hợp lý và có khả năng mở rộng. Báo cáo này "
                   "trình bày quá trình phân tích, thiết kế, cài đặt, kiểm thử và đánh giá kết quả.")
    h2(document, "1.2. Phạm vi")
    bullet(document, "Dữ liệu đầu vào: file slang_clean.txt (đã chuẩn hoá từ slang.txt), gồm 7.638 mục từ và 8.941 định nghĩa.")
    bullet(document, "Ứng dụng desktop Java Swing, chạy offline, không cần máy chủ hay kết nối mạng.")
    bullet(document, "Hỗ trợ 10 chức năng: tra cứu theo word/definition, lịch sử, thêm, sửa, xóa, "
                     "reset, random và hai chế độ đố vui.")
    bullet(document, "Kiểm thử hiệu năng với dữ liệu sinh tự động 100.000 slang word.")
    bullet(document, "Giao diện chỉ dùng đen–trắng (thang xám), không sử dụng màu nhấn.")
    h2(document, "1.3. Công nghệ sử dụng")
    table(document, ["Thành phần", "Công nghệ / lựa chọn"], [
        ["Ngôn ngữ", "Java 17+ (biên dịch với --release 17, kiểm thử trên JDK 23)"],
        ["Giao diện", "Java Swing + Java2D (component bo góc, vẽ trạng thái bằng viền/nét đứt)"],
        ["Lưu trữ", "Java IO: text UTF-8 và snapshot nhị phân DataInputStream/DataOutputStream"],
        ["Cấu trúc dữ liệu", "HashMap, LinkedHashMap, ArrayList, ArrayDeque, posting list trigram"],
        ["Kiểm thử", "Bộ test tự viết, không phụ thuộc thư viện ngoài; đo thời gian bằng nanoTime"],
        ["Đóng gói", "jar (JDK), PowerShell build script, Git"],
    ], widths=[4.0, 11.5])
    para(document, "Ứng dụng không sử dụng thư viện bên ngoài. Toàn bộ giao diện, chỉ mục và "
                   "định dạng lưu trữ đều được cài đặt trong mã nguồn của đồ án.")

    # ------------------------------------------------------------------
    # 2. Requirements
    # ------------------------------------------------------------------
    h1(document, "2. PHÂN TÍCH YÊU CẦU")
    h2(document, "2.1. Yêu cầu chức năng")
    table(document, ["STT", "Chức năng", "Mô tả ngắn"], [
        ["1", "Tìm theo slang word", "Tra cứu chính xác, không phân biệt hoa thường; hiển thị đầy đủ định nghĩa."],
        ["2", "Tìm theo definition", "Hiển thị tất cả slang word có định nghĩa chứa chuỗi con; highlight từ khoá."],
        ["3", "Lịch sử tra cứu", "Lưu từ khoá, loại tra cứu, số kết quả, thời gian; xóa được."],
        ["4", "Thêm slang word", "Kiểm tra trùng; cho chọn ghi đè, thêm bản trùng hoặc hủy."],
        ["5", "Chỉnh sửa", "Chọn đúng mục từ, sửa word/định nghĩa, lưu ngay."],
        ["6", "Xóa", "Chọn mục từ, xác nhận trước khi xóa."],
        ["7", "Reset", "Khôi phục danh sách từ slang_clean.txt (bản chuẩn hoá), có xác nhận."],
        ["8", "Random", "Hiển thị một slang word ngẫu nhiên (on this day)."],
        ["9", "Đố vui word → nghĩa", "Một slang word và 4 định nghĩa; chọn đáp án, phản hồi đúng/sai."],
        ["10", "Đố vui nghĩa → word", "Một định nghĩa và 4 slang word; chọn đáp án, phản hồi đúng/sai."],
    ], widths=[1.2, 4.4, 9.9], font_size=10.2)

    h2(document, "2.2. Yêu cầu phi chức năng")
    table(document, ["Nhóm", "Yêu cầu", "Cách đáp ứng"], [
        ["Hiệu năng", "Tra cứu dưới 1 giây với 100.000 slang word",
         "HashMap O(1) cho word; trigram inverted index cho definition; benchmark 100k."],
        ["Lưu trữ", "Không đánh chỉ mục lại ở lần chạy sau",
         "Snapshot nhị phân chứa cả word index và posting list; kiểm tra fingerprint trước khi dùng."],
        ["Toàn vẹn", "Không mất dữ liệu khi add/edit/delete",
         "Ghi file tạm rồi move atomic; lưu cả text và snapshot; có fallback rebuild khi snapshot hỏng."],
        ["Giao diện", "Menu rõ ràng, dễ dùng, chỉ đen–trắng",
         "Menu bar + sidebar + phím tắt; Theme grayscale, trạng thái đúng/sai bằng viền đậm/nét đứt."],
        ["Nền tảng", "Chạy độc lập, không cần mạng", "Jar tự chứa dữ liệu mẫu; dữ liệu làm việc lưu trong thư mục data."],
    ], widths=[2.5, 5.2, 7.8], font_size=10.2)

    h2(document, "2.3. Các luồng nghiệp vụ chính")
    h3(document, "Tra cứu")
    numbered(document, "Chuẩn hoá từ khoá (NFC, gộp khoảng trắng, chữ thường).")
    numbered(document, "Nếu tìm theo word: tra HashMap<String, List<Slang>> để lấy tất cả bản ghi trùng tên.")
    numbered(document, "Nếu tìm theo definition: lấy giao posting list trigram, kiểm tra lại từng định nghĩa.")
    numbered(document, "Hiển thị kết quả, thời gian chạy và ghi vào lịch sử.")
    h3(document, "Thêm / sửa / xóa")
    numbered(document, "Kiểm tra dữ liệu nhập và chuẩn hoá.")
    numbered(document, "Nếu từ đã tồn tại khi thêm: hỏi người dùng chọn Ghi đè, Thêm bản trùng hoặc Hủy.")
    numbered(document, "Cập nhật SlangDictionary, ghi data/slang_modified.txt và snapshot.")
    numbered(document, "Thông báo kết quả qua thanh trạng thái; làm mới các màn hình liên quan.")
    h3(document, "Đố vui")
    numbered(document, "Chọn ngẫu nhiên một mục từ và một định nghĩa làm đáp án đúng.")
    numbered(document, "Sinh 3 đáp án nhiễu duy nhất, không trùng đáp án đúng.")
    numbered(document, "Trộn 4 đáp án; khi người dùng chọn, đánh dấu đáp án đúng bằng viền đậm, "
                        "đáp án chọn sai bằng viền nét đứt, các đáp án khác chuyển xám.")

    # ------------------------------------------------------------------
    # 3. Architecture
    # ------------------------------------------------------------------
    h1(document, "3. KIẾN TRÚC VÀ THIẾT KẾ")
    h2(document, "3.1. Sơ đồ tổng quan")
    para(document, "Ứng dụng được chia thành các tầng độc lập: model, data, service và ui. "
                   "Cửa sổ chính đóng vai trò điều phối, các panel chỉ giao tiếp qua interface AppHost.")
    code_block(document, """App / ScreenshotMode
        |
        v
   MainFrame (menu, sidebar, CardLayout)
        |  AppHost
        v
   Home / Search / Add / Edit / Delete / Reset / Random / Quiz panels
        |
        v
   SlangDictionary  <---->  DictionaryStore  ---->  data/*.bin, *.txt
        ^
        |
   HistoryService, QuizService""")
    h2(document, "3.2. Thành phần chính")
    table(document, ["Lớp / package", "Trách nhiệm"], [
        ["model.Slang", "Một mục từ: id, word, danh sách định nghĩa, thời gian; implements Comparable."],
        ["model.HistoryEntry", "Một lượt tra cứu: thời gian, loại, từ khoá, số kết quả."],
        ["model.QuizQuestion", "Câu hỏi 4 đáp án, chỉ số đáp án đúng, giải thích."],
        ["data.SlangDictionary", "Quản lý kho mục từ, word index, trigram index, thao tác thêm/sửa/xóa."],
        ["data.TrigramIndex", "Xây dựng và truy vấn posting list trigram cho definition."],
        ["data.DictionaryStore", "Đọc/ghi slang.txt/slang_clean.txt, slang_modified.txt, dictionary.bin; fingerprint và fallback."],
        ["service.HistoryService", "Quản lý lịch sử 200 mục, lưu/đọc data/history.txt."],
        ["service.QuizService", "Sinh câu hỏi 4 đáp án duy nhất cho hai chế độ đố vui."],
        ["ui.MainFrame", "Cửa sổ chính, menu, sidebar, điều hướng CardLayout, thanh trạng thái."],
        ["ui.Theme / components", "Bảng màu grayscale, font, nút/panel/ô nhập/option quiz tự vẽ."],
    ], widths=[4.4, 11.2], font_size=10.2)
    h2(document, "3.3. Luồng khởi động")
    numbered(document, "App khởi tạo Look & Feel, font và các thiết lập grayscale.")
    numbered(document, "Bootstrap xác định thư mục dự án; nếu thiếu dữ liệu thì trích xuất slang.txt / slang_clean.txt đi kèm jar.")
    numbered(document, "DictionaryStore kiểm tra data/slang_modified.txt và data/dictionary.bin.")
    numbered(document, "Nếu fingerprint khớp: đọc snapshot, không đánh chỉ mục lại. Nếu không: parse text, "
                        "xây chỉ mục, ghi snapshot mới.")
    numbered(document, "MainFrame được tạo trên EDT; HomePanel hiển thị thống kê và nạp dữ liệu.")
    h2(document, "3.4. Thiết kế giao diện")
    para(document, "Giao diện dùng một cửa sổ chính với menu bar ở trên, sidebar điều hướng bên trái và "
                   "vùng nội dung thay đổi bằng CardLayout. Mỗi chức năng là một panel riêng, kế thừa "
                   "Refreshable để làm mới khi được mở hoặc khi dữ liệu thay đổi.")
    para(document, "Theo yêu cầu, toàn bộ giao diện chỉ dùng đen–trắng: các màu trong Theme có R = G = B. "
                   "Trạng thái không dùng màu để phân biệt; thay vào đó dùng độ đậm của viền, nền xám, "
                   "nét đứt và nội dung chữ. Ảnh trong báo cáo cũng được chuyển sang thang xám.")

    # ------------------------------------------------------------------
    # 4. Data structures
    # ------------------------------------------------------------------
    h1(document, "4. CẤU TRÚC DỮ LIỆU VÀ GIẢI THUẬT")
    h2(document, "4.1. Bài toán")
    para(document, "Với 100.000 slang word, nếu chỉ dùng List thì mỗi lần tìm kiếm phải duyệt tuần tự, "
                   "không đáp ứng yêu cầu dưới 1 giây và còn bị giới hạn điểm theo quy định của đề. "
                   "Đồ án sử dụng thêm HashMap, LinkedHashMap, ArrayDeque và chỉ mục nghịch đảo trigram.")
    para(document, "Dữ liệu đầu vào được kiểm tra và chuẩn hoá trước khi đưa vào ứng dụng. "
                   "File gốc slang.txt (7.656 mục từ, 8.959 định nghĩa) được giữ nguyên để đối chiếu; "
                   "file slang_clean.txt (7.638 mục từ, 8.941 định nghĩa) là dữ liệu chuẩn mà ứng dụng sử dụng. "
                   "Quá trình chuẩn hoá đã sửa header, gộp 5 dòng thiếu key, xoá 15 bản ghi trùng hoàn toàn, "
                   "gộp 3 cặp key có nghĩa khác, trim 14 key và 14 định nghĩa, chuẩn hoá 11 dòng dấu |, "
                   "ánh xạ 2 key non-ASCII, sửa 4 định nghĩa bị cắt cụt và đổi chỗ 2 cặp sai thứ tự A–Z. "
                   "Chi tiết theo từng dòng nằm trong report/SLANG_ERRORS_REPORT.md và file CSV đi kèm.")
    h2(document, "4.2. Chỉ mục từ khoá")
    para(document, "Mỗi mục từ có một id duy nhất; nhiều mục từ có thể trùng tên. Vì vậy cấu trúc chính là:")
    code_block(document, """LinkedHashMap<Long, Slang> byId;              // giữ thứ tự chèn
HashMap<String, List<Slang>> byWord;          // key = word đã chuẩn hoá
List<Slang> ordered;                          // thứ tự dùng cho posting list""")
    bullet(document, "Tra cứu chính xác trung bình O(1); danh sách kết quả vẫn giữ được các bản ghi trùng.")
    bullet(document, "Word được chuẩn hoá bằng Unicode NFC, gộp khoảng trắng và lowercase(Locale.ROOT).")
    bullet(document, "Gợi ý theo prefix duyệt các key của byWord, sắp xếp theo độ dài rồi alphabet.")
    h2(document, "4.3. Chỉ mục trigram cho định nghĩa")
    para(document, "Mỗi định nghĩa được chuẩn hoá và tách thành các trigram (3 ký tự liên tiếp). "
                   "Mỗi trigram ánh xạ tới danh sách chỉ số các mục từ có chứa trigram đó.")
    code_block(document, """for each entry:
    for each trigram in entry.searchText:
        postings[trigram].add(entryIndex)

query(keyword):
    if keyword.length < 3: scan entries
    else:
        candidates = intersection(postings[t] for t in trigrams(keyword))
        for candidate in candidates:
            if candidate.matchesDefinition(keyword): add candidate""")
    para(document, "Bước kiểm tra lại bằng matchesDefinition loại bỏ false-positive do các trigram xuất hiện "
                   "ở vị trí khác nhau. Việc kiểm tra được thực hiện trên từng định nghĩa riêng, nên một cụm "
                   "từ không thể khớp “xuyên” qua ranh giới hai định nghĩa.")
    h2(document, "4.4. Độ phức tạp")
    table(document, ["Thao tác", "Cấu trúc / chi phí", "Ghi chú"], [
        ["Tìm theo word", "HashMap, O(1) trung bình", "O(k) với k bản ghi trùng tên"],
        ["Tìm theo definition (≥3 ký tự)", "Giao posting list + kiểm tra", "Thực tế 1–20 ms với 100k"],
        ["Tìm theo definition (<3 ký tự)", "Quét tuần tự", "Vẫn dưới 1 giây với dữ liệu trong bộ nhớ"],
        ["Thêm / sửa / xóa", "Cập nhật map + rebuild chỉ mục", "Thao tác người dùng, không phải truy vấn"],
        ["Lưu / nạp", "Text UTF-8 + snapshot nhị phân", "Nạp snapshot ~0,6 giây với 100k"],
    ], widths=[4.8, 5.6, 5.2], font_size=10.2)
    h2(document, "4.5. Lưu trữ và phục hồi")
    para(document, "Dữ liệu được lưu ở hai dạng:")
    bullet(document, "slang_modified.txt: text UTF-8, mỗi dòng word`definition1|definition2, có thể mở bằng editor.")
    bullet(document, "dictionary.bin: snapshot nhị phân chứa entries, word index và toàn bộ posting list trigram.")
    para(document, "Snapshot lưu kèm CRC32, kích thước và tên file nguồn. Khi khởi động, nếu fingerprint khớp, "
                   "ứng dụng đọc thẳng snapshot. Nếu snapshot cũ, hỏng hoặc file text đã thay đổi, chương trình "
                   "tự parse lại text, xây chỉ mục và ghi snapshot mới. File được ghi qua file tạm rồi move atomic.")
    h2(document, "4.6. Lịch sử tra cứu")
    para(document, "HistoryService dùng ArrayDeque<HistoryEntry>, thêm mục mới vào đầu, giới hạn 200 mục và "
                   "ghi data/history.txt sau mỗi lần tra cứu. Định dạng text có escape ký tự tab/newline.")
    h2(document, "4.7. Vì sao đáp ứng yêu cầu điểm")
    para(document, "Ứng dụng không chỉ dùng List: HashMap, LinkedHashMap, ArrayDeque, HashSet, posting list "
                   "int[] và chỉ mục nghịch đảo trigram đều được sử dụng. Đây là căn cứ để đồ án không bị giới hạn "
                   "ở mức 70 điểm theo quy định “chỉ dùng List”.")

    # ------------------------------------------------------------------
    # 5. UI
    # ------------------------------------------------------------------
    h1(document, "5. GIAO DIỆN VÀ CHỨC NĂNG")
    h2(document, "5.1. Nguyên tắc đen–trắng")
    para(document, "Toàn bộ Theme sử dụng các giá trị xám (R = G = B). Không có màu xanh, đỏ, tím hay vàng. "
                   "Các trạng thái được phân biệt bằng:")
    bullet(document, "Đậm/nhạt của nền và viền.")
    bullet(document, "Viền liền đậm cho đáp án đúng; viền nét đứt cho đáp án chọn sai.")
    bullet(document, "Chữ mô tả trực tiếp: “Chính xác”, “Chưa đúng”, “Đáp án đúng: …”.")
    bullet(document, "Badge A/B/C/D chuyển đen hoặc xám tùy trạng thái.")
    para(document, "Ảnh chụp trong báo cáo được chuyển sang grayscale bằng Java2D trước khi ghi, nên không còn "
                   "pixel lệch kênh màu do ClearType.")
    image(document, "01-home.png", "Hình 5.1. Trang tổng quan với thống kê, từ của ngày và thao tác nhanh")
    image(document, "02-search-word.png", "Hình 5.2. Tra cứu chính xác theo slang word")
    image(document, "03-search-definition.png", "Hình 5.3. Tìm theo định nghĩa, highlight từ khoá bằng nền xám")
    image(document, "04-history.png", "Hình 5.4. Lịch sử tra cứu, mới nhất ở trên cùng")
    image(document, "05-add.png", "Hình 5.5. Form thêm slang word")
    image(document, "06-edit.png", "Hình 5.6. Chỉnh sửa mục từ đã chọn")
    image(document, "07-delete.png", "Hình 5.7. Xóa mục từ, có xác nhận trước khi thực hiện")
    image(document, "08-reset.png", "Hình 5.8. Khôi phục danh sách chuẩn từ slang_clean.txt")
    image(document, "09-random.png", "Hình 5.9. Slang word ngẫu nhiên (on this day)")
    image(document, "10b-quiz-word-answered.png",
          "Hình 5.10. Đố vui word → nghĩa sau khi chọn sai: đáp án đúng viền đậm, đáp án sai viền nét đứt")
    image(document, "11b-quiz-definition-answered.png",
          "Hình 5.11. Đố vui nghĩa → word sau khi chọn sai: phản hồi bằng chữ và kiểu viền")

    document.add_page_break()
    h2(document, "5.2. Điều hướng và phím tắt")
    table(document, ["Phím", "Chức năng"], [
        ["Ctrl+1 … Ctrl+0", "Mở nhanh 10 chức năng"],
        ["Ctrl+H", "Trang tổng quan"],
        ["Ctrl+Q", "Thoát"],
        ["Enter", "Thực hiện tìm kiếm ở ô nhập"],
    ], widths=[4.0, 11.4], font_size=10.5)

    # ------------------------------------------------------------------
    # 6. Testing
    # ------------------------------------------------------------------
    h1(document, "6. KIỂM THỬ VÀ ĐÁNH GIÁ")
    h2(document, "6.1. Chiến lược kiểm thử")
    bullet(document, "Kiểm thử đơn vị/tích hợp cho parser, tìm kiếm, thêm/sửa/xóa, lưu trữ, reset, lịch sử, quiz.")
    bullet(document, "Đối chiếu tìm kiếm definition với brute-force trên dữ liệu thật.")
    bullet(document, "Đo hiệu năng với 100.000 mục sinh tự động, kiểm tra nạp snapshot không rebuild.")
    bullet(document, "Smoke test giao diện: mở 11 màn hình, chụp 13 ảnh trạng thái.")
    h2(document, "6.2. Kết quả kiểm thử tự động")
    table(document, ["Nhóm", "Nội dung", "Số kiểm tra"], [
        ["1", "Parser, dữ liệu chuẩn, AMA đã gộp", "5"],
        ["2", "Tìm theo slang word", "7"],
        ["3", "Tìm theo definition + đối chiếu brute-force", "20"],
        ["4", "Add / overwrite / duplicate / edit / delete", "13"],
        ["5", "Persistence, snapshot, fallback", "10"],
        ["6", "Reset dữ liệu chuẩn", "7"],
        ["7", "History persistence", "10"],
        ["8", "Quiz generation", "2"],
        ["9", "Hiệu năng 100.000 slang word", "14"],
        ["", "Tổng", "88/88 đạt"],
    ], widths=[1.2, 10.8, 3.4], font_size=10.5)
    para(document, "Kết quả đầy đủ nằm trong report/test-results.txt. Hai cảnh báo fallback snapshot trong "
                   "quá trình test là có chủ đích; test harness đã nuôi im lặng các cảnh báo này để log sạch.")
    h2(document, "6.3. Hiệu năng với 100.000 slang word")
    table(document, ["Phép đo", "Kết quả", "Yêu cầu"], [
        ["Sinh 100.000 mục", "~345 ms", "—"],
        ["Xây dictionary + trigram index", "~300 ms", "—"],
        ["200 lần tra cứu chính xác", "0 ms (tổng)", "< 1 giây mỗi lần"],
        ["Tìm definition “money”", "~1 ms (587 kết quả)", "< 1 giây"],
        ["Tìm definition “go”", "~20 ms (5.152 kết quả)", "< 1 giây"],
        ["Tìm definition “the”", "~7 ms (7.536 kết quả)", "< 1 giây"],
        ["Lưu text + snapshot 14 MB", "~400 ms", "—"],
        ["Nạp snapshot lần sau", "~600–650 ms, indexLoaded=true", "Không rebuild"],
        ["Tìm kiếm sau khi nạp", "0–1 ms", "< 1 giây"],
    ], widths=[5.6, 5.3, 4.6], font_size=10.2)
    para(document, "Bộ nhớ sử dụng khi test 100k khoảng 184 MB. Các phép tìm kiếm đều dưới 1 giây, đáp ứng "
                   "yêu cầu của đề.")
    h2(document, "6.4. Kiểm thử giao diện")
    para(document, "ScreenshotMode mở cửa sổ thật, đi qua 11 màn hình, gọi dữ liệu mẫu BOMB/money, chụp 13 ảnh "
                   "(11 màn hình + 2 trạng thái quiz đã trả lời) và thoát. Ảnh được chuyển sang grayscale; "
                   "kiểm tra bằng Python/Pillow cho thấy 0 pixel lệch kênh màu.")
    code_block(document, "SMOKE OK: 11 màn hình, 13 ảnh đã mở/chụp thành công.")
    h2(document, "6.5. Các trường hợp biên")
    bullet(document, "Từ khoá rỗng, toàn khoảng trắng, hoa/thường, khoảng trắng đầu cuối.")
    bullet(document, "Từ khoá 1–2 ký tự (fallback quét tuần tự).")
    bullet(document, "Definition không chứa keyword trả về danh sách rỗng, không lỗi.")
    bullet(document, "Snapshot sai magic, snapshot cũ, file text thay đổi → rebuild đúng.")
    bullet(document, "Reset khi có dữ liệu sửa → quay về đúng 7.638 mục chuẩn.")
    bullet(document, "History vượt 200 mục → cắt mục cũ.")
    bullet(document, "Quiz với dữ liệu trùng từ → vẫn đủ 4 đáp án duy nhất.")
    h2(document, "6.6. Hạn chế")
    bullet(document, "Tìm definition là tìm chuỗi con, chưa phải tìm mờ (fuzzy) hay tìm theo ngữ nghĩa.")
    bullet(document, "Khi overwrite một từ có nhiều bản trùng, ứng dụng cập nhật mục đầu tiên; các bản trùng khác giữ nguyên.")
    bullet(document, "Điểm quiz chỉ giữ trong phiên chạy, không lưu qua các lần mở.")
    bullet(document, "Ảnh chụp commit GitHub và video YouTube là artifact bên ngoài, cần sinh viên hoàn thiện trước khi nộp.")

    # ------------------------------------------------------------------
    # 7. Conclusion
    # ------------------------------------------------------------------
    h1(document, "7. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN")
    para(document, "Đồ án đã xây dựng hoàn chỉnh ứng dụng Slang Dictionary bằng Java Swing với 10 chức năng "
                   "theo yêu cầu, giao diện đen–trắng nhất quán và kiến trúc tách biệt model/data/service/ui.")
    para(document, "Điểm nổi bật là chỉ mục từ khoá HashMap kết hợp chỉ mục nghịch đảo trigram, giúp tra cứu "
                   "chính xác tức thời và tìm definition trong vài mili giây với 100.000 slang word. Snapshot "
                   "nhị phân có fingerprint giúp lần chạy sau không phải đánh chỉ mục lại, đồng thời vẫn có cơ chế "
                   "fallback an toàn khi dữ liệu thay đổi hoặc snapshot hỏng.")
    para(document, "Hướng phát triển tiếp theo:")
    bullet(document, "Bổ sung tìm kiếm mờ, tìm theo nhiều từ khoá và xếp hạng kết quả.")
    bullet(document, "Cho phép import/export CSV/JSON và đồng bộ dữ liệu.")
    bullet(document, "Thêm nhiều dạng câu hỏi, độ khó và lưu điểm qua các phiên.")
    bullet(document, "Đóng gói installer hoặc dùng JavaFX cho trải nghiệm hiện đại hơn.")

    # ------------------------------------------------------------------
    # 8. References
    # ------------------------------------------------------------------
    h1(document, "8. TÀI LIỆU THAM KHẢO")
    numbered(document, "Đề bài Đồ án #1 – Slang Word, môn CSC13002 – Lập trình ứng dụng Java (file DAMH#1.pdf).")
    numbered(document, "Java SE 17 API Specification: java.util (Collections, HashMap, ArrayDeque), "
                        "java.io, java.nio.file, javax.swing.")
    numbered(document, "Dữ liệu slang.txt do đề bài cung cấp; bản chuẩn hoá là slang_clean.txt.")
    para(document, "Tỷ lệ tham khảo mã nguồn bên ngoài: 0%. Ứng dụng chỉ sử dụng JDK chuẩn, không dùng "
                   "thư viện bên thứ ba và không sao chép mã nguồn từ internet hay từ người khác.")

    # ------------------------------------------------------------------
    # Appendix
    # ------------------------------------------------------------------
    h1(document, "PHỤ LỤC A – TỰ ĐÁNH GIÁ")
    table(document, ["STT", "Chức năng", "Điểm", "Minh chứng"], [
        ["1", "Tìm theo slang word", "10/10", "Ảnh 5.2; test Search by slang word"],
        ["2", "Tìm theo definition", "10/10", "Ảnh 5.3; test Search by definition"],
        ["3", "Lịch sử", "10/10", "Ảnh 5.4; test History persistence"],
        ["4", "Thêm + xử lý trùng", "10/10", "Ảnh 5.5; test Add/overwrite/duplicate"],
        ["5", "Chỉnh sửa", "10/10", "Ảnh 5.6; test Edit"],
        ["6", "Xóa + xác nhận", "10/10", "Ảnh 5.7; test Delete"],
        ["7", "Reset", "10/10", "Ảnh 5.8; test Reset"],
        ["8", "Random", "10/10", "Ảnh 5.9"],
        ["9", "Đố vui word → nghĩa", "10/10", "Ảnh 5.10; 200 câu test"],
        ["10", "Đố vui nghĩa → word", "10/10", "Ảnh 5.11; 200 câu test"],
        ["", "Tổng", "100/100", ""],
    ], widths=[1.2, 5.5, 2.2, 6.7], font_size=10.2)

    h1(document, "PHỤ LỤC B – CHẠY VÀ ĐÓNG GÓI")
    code_block(document, """run.bat          # chạy ứng dụng
test.bat         # build + chạy 88 kiểm tra + tạo jar
powershell -NoProfile -ExecutionPolicy Bypass -File package.ps1 ^
  -Mssv <MSSV> -FullName "<Họ tên>" -ClassName "<Lớp>" ^
  -VideoUrl "https://youtu.be/..." -Score 100""")
    para(document, "File nộp có dạng <MSSV>_100.zip, gồm source, jar, dữ liệu, ảnh chụp, kết quả test, "
                   "file tự đánh giá và kịch bản video.")
    h1(document, "PHỤ LỤC C – VIDEO VÀ COMMIT")
    bullet(document, "Link video YouTube: ................................................................")
    bullet(document, "Ảnh chụp commit: cần chụp trang Commits trên GitHub/Bitbucket/GitLab sau khi push.")
    bullet(document, "Repo local có lịch sử commit thực tế; ảnh local trong report/commits chỉ là minh chứng tạm.")
    bullet(document, "Cần đảm bảo commit được phân bố theo các ngày làm việc thực tế trước deadline.")

    document.core_properties.title = "Báo cáo đồ án #1 – Slang Dictionary"
    document.core_properties.subject = "CSC13002 – Lập trình ứng dụng Java"
    document.core_properties.author = "Sinh viên thực hiện"
    document.core_properties.keywords = "Java, Swing, Slang Dictionary, HashMap, Trigram, CSC13002"

    document.save(OUT)
    print(f"Wrote {OUT}")


if __name__ == "__main__":
    build()
