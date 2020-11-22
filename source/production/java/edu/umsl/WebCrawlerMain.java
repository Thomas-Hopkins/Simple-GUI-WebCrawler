package edu.umsl;

import java.util.InputMismatchException;
import java.util.Scanner;

public class WebCrawlerMain {
    public static void main(String[] args) throws InterruptedException {
        String url;
        int traversals;

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

        WebCrawler webCrawler = new WebCrawler(url);
        while (webCrawler.getNumTraversed() < traversals) {
            Thread.sleep(100);
            if (webCrawler.doTraversal() == -1) {
                System.out.println("No more URLs found.");
                break;
            }
            System.out.println("Crawling... " + webCrawler.getCurrentUrl());
        }
        webCrawler.getNumTraversed();
        webCrawler.displayWordsCount();

    }
}
