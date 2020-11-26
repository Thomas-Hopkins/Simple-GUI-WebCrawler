package edu.umsl;

import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class WebCrawlerConsole {
    public static void main(String[] args) throws InterruptedException {
        String url;
        int traversals;
        boolean restrictDomain = false;

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter an URL to start crawling: ");
        url = scanner.next();

        System.out.println("How many urls do you wish to traverse? ");
        try {
            traversals = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Must enter an integer! Defaulting to 1 traversal.");
            traversals = 1;
        }

        System.out.println("Restrict traversals to same domain? (y/n): ");
        if (scanner.next().toUpperCase().equals("Y")) {
            restrictDomain = true;
        }

        // Main loop
        WebCrawler webCrawler = new WebCrawler(url, restrictDomain);
        while (webCrawler.getNumTraversed() < traversals) {
            Thread.sleep(100);
            webCrawler.doTraversal();
            if (webCrawler.getCurrentError() != null) {
                // If has an error print it
                System.out.println(webCrawler.getCurrentError());
                if (webCrawler.getCurrentUrl()==null) {
                    // If current URL is null there are no more urls left
                    break;
                }
            }
            System.out.println("Crawling... " + webCrawler.getCurrentUrl());
        }

        // Sort the map by values and output it
        Map<String, Integer> sortedWords = new LinkedHashMap<>();
        webCrawler.getWordsCount().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedWords.put(x.getKey(), x.getValue()));
        sortedWords.forEach((k,v) -> System.out.println("Word: " + k + " | Occurrences: " + v));
    }
}
