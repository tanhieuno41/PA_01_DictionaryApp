package vn.edu.hcmus.slang.service;

import vn.edu.hcmus.slang.data.SlangDictionary;
import vn.edu.hcmus.slang.model.QuizQuestion;
import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.util.TextNormalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Sinh câu hỏi trắc nghiệm 4 đáp án cho hai chế độ đố vui. */
public final class QuizService {

    private static final int CHOICE_COUNT = 4;

    private final SlangDictionary dictionary;
    private final Random random;

    public QuizService(SlangDictionary dictionary) {
        this(dictionary, new Random());
    }

    public QuizService(SlangDictionary dictionary, Random random) {
        this.dictionary = dictionary;
        this.random = random;
    }

    public QuizQuestion nextQuestion(QuizQuestion.Mode mode) {
        List<Slang> entries = dictionary.allEntries();
        if (entries.size() < CHOICE_COUNT) {
            throw new IllegalStateException("Cần ít nhất " + CHOICE_COUNT + " slang word để tạo câu hỏi");
        }
        return mode == QuizQuestion.Mode.WORD_TO_DEFINITION
                ? wordToDefinition(entries)
                : definitionToWord(entries);
    }

    private QuizQuestion wordToDefinition(List<Slang> entries) {
        Slang target = pick(entries);
        String correct = target.getDefinitions().get(random.nextInt(target.getDefinitions().size()));

        Set<String> used = new HashSet<>();
        used.add(TextNormalizer.normalizeSearchText(correct));
        List<String> choices = new ArrayList<>(CHOICE_COUNT);
        choices.add(correct);

        int guard = 0;
        while (choices.size() < CHOICE_COUNT && guard++ < 10_000) {
            Slang other = pick(entries);
            if (other.getId() == target.getId()) {
                continue;
            }
            List<String> definitions = other.getDefinitions();
            String candidate = definitions.get(random.nextInt(definitions.size()));
            if (used.add(TextNormalizer.normalizeSearchText(candidate))) {
                choices.add(candidate);
            }
        }
        if (choices.size() < CHOICE_COUNT) {
            throw new IllegalStateException("Không đủ đáp án nhiễu cho câu hỏi");
        }

        Collections.shuffle(choices, random);
        int correctIndex = indexOfNormalized(choices, correct);
        String explanation = target.getWord() + ": " + target.getJoinedDefinitions();
        return new QuizQuestion(QuizQuestion.Mode.WORD_TO_DEFINITION, target.getWord(),
                choices, correctIndex, explanation, target);
    }

    private QuizQuestion definitionToWord(List<Slang> entries) {
        Slang target = pick(entries);
        String correct = target.getWord();
        String prompt = target.getDefinitions().get(random.nextInt(target.getDefinitions().size()));

        Set<String> used = new HashSet<>();
        used.add(TextNormalizer.normalizeWord(correct));
        List<String> choices = new ArrayList<>(CHOICE_COUNT);
        choices.add(correct);

        int guard = 0;
        while (choices.size() < CHOICE_COUNT && guard++ < 10_000) {
            Slang other = pick(entries);
            if (other.getId() == target.getId()) {
                continue;
            }
            String candidate = other.getWord();
            if (used.add(TextNormalizer.normalizeWord(candidate))) {
                choices.add(candidate);
            }
        }
        if (choices.size() < CHOICE_COUNT) {
            throw new IllegalStateException("Không đủ đáp án nhiễu cho câu hỏi");
        }

        Collections.shuffle(choices, random);
        int correctIndex = indexOfNormalized(choices, correct);
        String explanation = target.getWord() + ": " + target.getJoinedDefinitions();
        return new QuizQuestion(QuizQuestion.Mode.DEFINITION_TO_WORD, prompt,
                choices, correctIndex, explanation, target);
    }

    private Slang pick(List<Slang> entries) {
        return entries.get(random.nextInt(entries.size()));
    }

    private static int indexOfNormalized(List<String> values, String value) {
        String normalized = TextNormalizer.normalizeSearchText(value);
        for (int i = 0; i < values.size(); i++) {
            if (TextNormalizer.normalizeSearchText(values.get(i)).equals(normalized)) {
                return i;
            }
        }
        return -1;
    }
}
