package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.ui.AppHost;
import vn.edu.hcmus.slang.ui.Dialogs;

import java.awt.Component;
import java.awt.Cursor;
import java.io.IOException;

/** Thao tác lưu dùng chung cho các panel quản lý. */
final class PanelSupport {

    private PanelSupport() {
    }

    static boolean save(AppHost host, Component parent, String statusMessage) {
        Cursor previous = parent.getCursor();
        try {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            host.store().save(host.dictionary());
        } catch (IOException ex) {
            Dialogs.error(parent, "Không lưu được dữ liệu",
                    "<html><div style='width:420px'>Không ghi được file dữ liệu:<br><b>"
                            + ex.getMessage() + "</b><br><br>Dữ liệu trong bộ nhớ vẫn còn, "
                            + "hãy kiểm tra quyền ghi của thư mục dự án.</div></html>");
            return false;
        } finally {
            parent.setCursor(previous);
        }
        host.notifyDataChanged();
        host.setStatus(statusMessage);
        return true;
    }
}
