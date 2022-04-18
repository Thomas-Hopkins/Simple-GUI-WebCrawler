package edu.umsl;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class WebCrawler {
    private Map<String, Integer> wordsCount;
    private Set<String> traversedURLs;
    private Queue<String> pendingURLs;
    private Document document;
    private boolean restrictDomain;
    private String domainRestriction;
    private String currentError;

    WebCrawler(String startingURL, boolean restrictDomain) {
        wordsCount = new TreeMap<>();
        traversedURLs = new TreeSet<>();
        pendingURLs = new LinkedList<>();
        pendingURLs.add(startingURL);
        currentError = null;
        this.restrictDomain = restrictDomain;

        // Try and get valid domain name
        if (restrictDomain) {
            try {
                domainRestriction = getDomainName(startingURL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                this.restrictDomain = false;
            }
        }
    }

    private String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getHost();
    }

    private String formatURL(String url) throws URISyntaxException {
        // Remove Fragments and Query strings
        URI uri = new URI(url);
        uri = new URI(uri.getScheme(),uri.getHost(),uri.getPath(),null);
        String uriStr = uri.toString();
        // Remove trailing slashes
        if (uriStr.endsWith("/")) {
            return uriStr.substring(0,uriStr.length()-1);
        } else {
            return uriStr;
        }
    }

    private void addTraversed(String url) {
        traversedURLs.add(url);
    }

    private void addPending(String url) {
        // Strip Query and fragments from URLs to ensure no duplicate webpages
        try {
            url = formatURL(url);
        } catch (URISyntaxException e) {
            // If reached must be invalid url
            return;
        }
        // Check domain if domain restrictions enabled
        if (restrictDomain) {
            if (url.contains(domainRestriction)) {
                pendingURLs.add(url);
            }
        } else {
            pendingURLs.add(url);
        }
    }

    private void parseDocument() {
        // Get all document's elements and see if they have text
        for (Element element: document.getAllElements()) {
            if (element.hasText() && !element.ownText().isBlank() && !element.tagName().equals("script")) {
                // Split element's text into words
                for (String word : element.ownText().split("[\\s.,\\\\/;?!:\\-+)(\"]")) {
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
                addPending(element.absUrl("href"));
            }
        }
    }

    private boolean openUrlDocument() {
        return openUrlDocument(1);
    }

    private boolean openUrlDocument(int numTimeoutTries) {
        boolean successState = false;
        final int maxTimeoutTries = 5;
        String urlStr = pendingURLs.peek();

        // Try and connect to the url if we haven't already traversed it
        if (!traversedURLs.contains(urlStr)) {
            try {
                document = Jsoup.connect(urlStr).get();
                successState = true;
            } catch (UnsupportedMimeTypeException e) {
                e.printStackTrace();
                currentError = "Unsupported document type: " + e.getMimeType() + " on " + urlStr;
            } catch (IllegalArgumentException | NoSuchElementException | MalformedURLException e) {
                e.printStackTrace();
                currentError = e.getCause() + " URL IS: " + urlStr;
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                currentError = "Connection timed out... ";
                if (numTimeoutTries < maxTimeoutTries) {
                    currentError += "Trying again x" + numTimeoutTries;
                    successState = openUrlDocument(numTimeoutTries + 1);
                }
            } catch (HttpStatusException e) {
                e.printStackTrace();
                currentError = "HTTP Error code " + e.getStatusCode() + " on " + urlStr;
            } catch (Exception e) {
                e.printStackTrace();
                currentError = "Unknown error when attempting to open url: " + urlStr;
            }
            addTraversed(urlStr);
        }
        pendingURLs.remove();
        return successState;
    }

    public int getNumTraversed() {
        return traversedURLs.size();
    }

    public Map<String, Integer> getWordsCount() {
        return wordsCount;
    }

    public String getCurrentUrl() {
        return pendingURLs.peek();
    }

    public String getCurrentError() {
        return currentError;
    }

    public void doTraversal() {
        currentError = null;
        if (pendingURLs.size() == 0) {
            currentError = "No more URLs left to parse.";
            return;
        }
        if (openUrlDocument()) {
            parseDocument();
        } else {
            // Hit a duplicate url, do next traversal
            doTraversal();
        }
    }
}
