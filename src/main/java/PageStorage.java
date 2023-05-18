import java.util.Map;

public class PageStorage {
    private final int page;
    private final Map<String, Integer> wordCount;

    public int getPage() {
        return page;
    }

    public Map<String, Integer> getWordCount() {
        return wordCount;
    }

    public PageStorage(int page, Map<String, Integer> wordCount) {
        this.page = page;
        this.wordCount = wordCount;
    }
}
