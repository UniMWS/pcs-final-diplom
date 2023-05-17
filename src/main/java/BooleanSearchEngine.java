import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> indexedPageEntry;

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        indexedPageEntry = new HashMap<>();

        for (File pdf : Objects.requireNonNull(pdfsDir.listFiles())) {
            var doc = new PdfDocument(new PdfReader(pdf));
            for (int i = 1; i < doc.getNumberOfPages(); i++) {
                var page = doc.getPage(i);
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
                String pdfName = pdf.getName();
                int pageNumber = i;
                freqs.keySet().forEach(word -> {
                    PageEntry pageEntry = (new PageEntry(pdfName, pageNumber, freqs.get(word)));
                    if (indexedPageEntry.containsKey(word)) {
                        indexedPageEntry.get(word).add(pageEntry);
                    } else {
                        List<PageEntry> pageEntryList = new LinkedList<>();
                        pageEntryList.add(pageEntry);
                        indexedPageEntry.put(word, pageEntryList);
                    }
                });
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        List<PageEntry> response = indexedPageEntry.get(word.toLowerCase());
        if (response != null) response.sort(PageEntry::compareTo);
        return response;
    }
}
