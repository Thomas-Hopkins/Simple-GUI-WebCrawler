package edu.umsl;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.*;

public class WebCrawler {
    private Map<String, Integer> wordsCount;
    private Set<String> traversedURLs;
    private Queue<String> pendingURLs;
    private Document document;

    WebCrawler(String url) {
        wordsCount = new TreeMap<>();
        traversedURLs = new TreeSet<>();
        pendingURLs = new LinkedList<>();
        pendingURLs.add(url);
    }

    private void addTraversed(String url) {
        traversedURLs.add(url);
    }

    private void addPending(String url) {
        pendingURLs.add(url);
    }

    private void parseDocument() {
        // Get all document's elements and see if they have text
        for (Element element: document.getAllElements()) {
            if (element.hasText() && !element.ownText().isBlank() && !element.tagName().equals("script")) {
                // Split element's text into words
                for (String word : element.ownText().split("[\\s.,\\\\/;?!\\:\\-\\+\\)\\(\"“]")) {
                    if (!word.isBlank() && word.matches("\\D+")) {
                        if (wordsCount.containsKey(word)) {
                            // Increment value by one if map contains the word
                            wordsCount.compute(word, (k,v) -> v += 1);
                        } else {
                            wordsCount.put(word, 1);
                        }
                    }
                }
            }
            // If element contains a link append that link to pending urls
            if (element.hasAttr("href") && !element.tagName().equals("link") && !element.tagName().equals("script")) {
                String url = element.absUrl("href");
                if (!(url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith(".ico")) && !url.isBlank()) {
                    pendingURLs.add(element.absUrl("href"));
                }
            }
        }
    }

    public void openUrlDocument() {
        openUrlDocument(0);
    }

    private void openUrlDocument(int tries) {
        final int timeout = 5;
        String urlStr = pendingURLs.peek();
        if (!traversedURLs.contains(urlStr)) {
            try {
                document = Jsoup.connect(urlStr).get();
            } catch (UnsupportedMimeTypeException e) {
                System.out.println("Unsupported document type: " + e.getMimeType() + " on " + urlStr);
            } catch (IllegalArgumentException | NoSuchElementException | MalformedURLException e) {
                e.printStackTrace();
                System.out.println("URL IS: " + urlStr);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                System.out.println("Connection timed out... ");
                if (tries < timeout) {
                    System.out.println("Trying again.");
                    openUrlDocument(tries + 1);
                }
            } catch (HttpStatusException e) {
                System.out.println("HTTP Error code " + e.getStatusCode() + " on " + urlStr);
            } catch (IOException e) {
                e.printStackTrace();
            }
            traversedURLs.add(pendingURLs.peek());
        }
        pendingURLs.remove();

    }

    public int getNumTraversed() {
        return traversedURLs.size();
    }

    public void displayWordsCount() {
        Map<String, Integer> sortedWords = new LinkedHashMap<>();
        wordsCount.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedWords.put(x.getKey(), x.getValue()));
        sortedWords.forEach((k,v) -> System.out.println("Word: " + k + " | Occurrences: " + v));
    }

    public String getCurrentUrl() {
        return pendingURLs.peek();
    }

    public int doTraversal() {
        if (pendingURLs.size() == 0) {
            return -1;
        }
        openUrlDocument();
        parseDocument();
        return 1;
    }
}
