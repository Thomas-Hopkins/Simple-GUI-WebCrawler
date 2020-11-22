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
            boolean inTag = false;
            Stack<String> tagStack = new Stack<>();
            String line = inputStream.nextLine();
            String word = "";
            String currTag = "";
            System.out.println(line);
            for (int i = 0; i < line.length(); i++) {
                Character ch = line.charAt(i);
                if (inTag) {

                    // Covers case when tag name has leading whitespace.
                    if (Character.isWhitespace(ch) && currTag.isEmpty()) {
                        continue;
                    // Hit trailing whitespace so tag name must be done
                    } else if (Character.isWhitespace(ch) && !currTag.isEmpty()) {
                        tagStack.push(currTag);
                        currTag = "";
                        continue;
                    } else {
                        currTag += ch;
                    }

                    // Hit ending bracket must be end of tag declaration.
                    if (ch.equals('>')) {
                        inTag = false;
                    // Hit forwards slash, no longer inside current tag.
                    } else if (ch.equals('/')) {
                        System.out.println(tagStack.peek());
                        tagStack.pop();
                    }
                    continue;
                }

                // Opening bracket means start of a tag.
                if (ch.equals('<')) {
                    inTag = true;
                    continue;
                }

                // Hit non whitespace and we are out of a tag, must be a word
                if (!Character.isWhitespace(ch)) {
                    word += ch;
                // If hit whitespace must be end of a word
                } else if (Character.isWhitespace(ch)) {
                    // Add this word and it's count to the wordsCount map
                    if (wordsCount.containsKey(word)) {
                        wordsCount.compute(word, (k,v) -> v += 1);
                    } else {
                        wordsCount.put(word, 1);
                    }
                    word = "";
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
