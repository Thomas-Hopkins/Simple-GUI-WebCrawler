package edu.umsl;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WebCrawler {
    private Map<String, Integer> wordsCount;
    private Set<String> traversedURLs;
    private Queue<String> pendingURLs;
    private Scanner inputStream;

    WebCrawler(String url) {
        wordsCount = new TreeMap<>();
        traversedURLs = new TreeSet<>();
        pendingURLs = new LinkedList<>();
        addPending(url);
    }

    private void addTraversed(String url) {
        traversedURLs.add(url);
    }

    private void addPending(String url) {
        pendingURLs.add(url);
    }

    private void parseDocument() {
        while (inputStream.hasNext()) {
            String ch = inputStream.next();
            // Skip <class> tags
            if (ch.equals("<")) {
                while (!ch.equals(">")) {
                    if (inputStream.hasNext()) {
                        ch = inputStream.next();
                    } else {
                        break;
                    }
                }
            // If not whitespace start adding word
            } else if (!ch.isBlank()) {
                String word = "";
                while (!ch.isBlank() && !ch.equals("<")) {
                    // Keep concatenating until end of word
                    word += ch;//URLDecoder.decode(ch, StandardCharsets.UTF_8);
                    if (inputStream.hasNext()) {
                        ch = inputStream.next();
                    } else {
                        break;
                    }
                }
                if (wordsCount.containsKey(word)) {
                    // If word already found increment value.
                    wordsCount.compute(word, (k, v) -> v += 1);
                } else {
                    // If word not already found add it.
                    wordsCount.put(word, 1);
                }
            }
        }
    }

    private void openUrlDocument() throws IOException {
        String urlStr = pendingURLs.peek();
        assert urlStr != null;
        URL url = new URL(urlStr);
        inputStream = new Scanner(url.openStream());
    }

    public int getNumTraversed() {
        return traversedURLs.size();
    }

    public void displayWordsCount() {
        wordsCount.forEach((k,v) -> System.out.println("Word: " + k + " Occurrences: " + v));
    }

    public void doTraversal() {
        try {
            openUrlDocument();
        } catch (IOException e) {
            e.printStackTrace();
            pendingURLs.remove();
            return;
        }
        parseDocument();
    }
}
