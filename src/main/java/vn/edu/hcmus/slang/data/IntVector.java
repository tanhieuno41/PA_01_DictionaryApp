package vn.edu.hcmus.slang.data;

import java.util.Arrays;

/** Mảng int co giãn, dùng khi xây dựng posting list của chỉ mục trigram. */
final class IntVector {

    private int[] values = new int[8];
    private int size;

    void add(int value) {
        if (size == values.length) {
            values = Arrays.copyOf(values, values.length * 2);
        }
        values[size++] = value;
    }

    int size() {
        return size;
    }

    int[] toSortedUniqueArray() {
        int[] copy = Arrays.copyOf(values, size);
        Arrays.sort(copy);
        int write = 0;
        for (int read = 0; read < copy.length; read++) {
            if (read == 0 || copy[read] != copy[read - 1]) {
                copy[write++] = copy[read];
            }
        }
        return Arrays.copyOf(copy, write);
    }
}
