package vn.edu.hcmus.slang.ui;

/** Panel cần làm mới khi được hiển thị hoặc khi dữ liệu thay đổi. */
public interface Refreshable {

    default void onShown() {
    }

    default void onDataChanged() {
        onShown();
    }
}
