package connections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Category {
    private final String categoryDescription;
    private final int difficulty;
    private final Set<String> words;
    private final String[] difficultyColor = {"ffdb58","50c878","b272e0","468fea"};

    public Category(String categoryDescription, int difficulty, String[] words) {
        this.categoryDescription = categoryDescription;
        this.difficulty = difficulty;
        this.words = new HashSet<>(Arrays.asList(words));
    }

    public boolean check(Set<String> guess) {
        return this.words.equals(guess);
    }

    public String getDescription() {
        return categoryDescription;
    }

    public String getDifficultyColor() {
        return difficultyColor[this.difficulty];
    }
}
