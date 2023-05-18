import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, Set<PageStorage>> indexedPageEntry;
    private final Set<String> stopWords;
    private static final String STOP_FILE = "stop-ru.txt";

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        this.stopWords = getStopWords();
        this.indexedPageEntry = indexedPages(pdfsDir);
    }

    private Map<String, Set<PageStorage>> indexedPages(File pdfsDir) throws IOException {
        Map<String, Set<PageStorage>> indexedPages = new HashMap<>();
        for (File pdf : Objects.requireNonNull(pdfsDir.listFiles())) {
            String fileName = pdf.getName();
            indexedPages.put(fileName, new HashSet<>());
            var doc = new PdfDocument(new PdfReader(pdf));
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                var page = doc.getPage(i + 1);
                var text = PdfTextExtractor.getTextFromPage(page);
                var words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>();
                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }
                indexedPages.get(fileName).add(new PageStorage(i, freqs));
            }
        }
        return indexedPages;
    }

    private Set<String> getStopWords() throws IOException {
        var words = new HashSet<String>();
        var file = new File(STOP_FILE);
        try (var in = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String word;
            while ((word = in.readLine()) != null) {
                words.add(word.toLowerCase());
            }
        }
        return words;
    }

    @Override
    public List<PageEntry> search(String word) {
        List<String> words = (new ArrayList<>(List.of(word.toLowerCase()
                .split("\\P{IsAlphabetic}+")))).stream()
                .filter(search -> !stopWords.contains(search))
                .collect(Collectors.toList());
        List<PageEntry> response = new ArrayList<>();
        var pdfFiles = indexedPageEntry.keySet();
        for (var pdfName : pdfFiles) {
            var pages = indexedPageEntry.get(pdfName);
            for (var page : pages) {
                int count = 0;
                for (var searchWord : words) {
                    if (page.getWordCount().containsKey(searchWord)) {
                        count += page.getWordCount().get(searchWord);
                    }
                }
                if (count > 0) {
                    response.add(new PageEntry(pdfName, page.getPage() + 1, count));
                }
            }
        }
        Collections.sort(response);
        return response;
    }
}
