package vn.edu.hcmus.slang.model;

import java.util.List;

/** Một câu hỏi trắc nghiệm 4 đáp án. */
public record QuizQuestion(
        Mode mode,
        String prompt,
        List<String> choices,
        int correctIndex,
        String explanation,
        Slang slang) {

    public enum Mode {
        WORD_TO_DEFINITION,
        DEFINITION_TO_WORD
    }

    public QuizQuestion {
        choices = List.copyOf(choices);
        if (choices.size() != 4) {
            throw new IllegalArgumentException("Câu hỏi phải có đúng 4 đáp án");
        }
        if (correctIndex < 0 || correctIndex >= choices.size()) {
            throw new IllegalArgumentException("Đáp án đúng không hợp lệ");
        }
    }

    public String correctChoice() {
        return choices.get(correctIndex);
    }
}
