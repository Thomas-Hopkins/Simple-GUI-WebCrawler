package edu.umsl;

import java.util.Scanner;

public class WebCrawlerMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter an URL to start crawling: ");
        String url = scanner.next();
        WebCrawler webCrawler = new WebCrawler(url);
        while (webCrawler.getNumTraversed() < 10) {
            webCrawler.doTraversal();
        }
        webCrawler.getNumTraversed();
        webCrawler.displayWordsCount();

    }
}
