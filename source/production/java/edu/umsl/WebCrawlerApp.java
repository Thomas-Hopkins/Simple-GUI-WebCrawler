package edu.umsl;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

public class WebCrawlerApp extends Application {
    private TextField startUrlField, iterationsField;
    private Button btnStartCrawl;
    private CheckBox chkDomainRestrict;
    private final ObservableList<WordCount> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) throws Exception {
        GridPane rootGrid = new GridPane();
        rootGrid.setHgap(5.5);
        rootGrid.setVgap(5.5);
        rootGrid.setPadding(new Insets(10, 10, 10, 10));

        Label configLabel = new Label("Webcrawler Configuration");
        rootGrid.add(configLabel, 0, 0, 2, 1);

        Label startUrlLabel = new Label("Starting URL:");
        rootGrid.add(startUrlLabel, 0, 1);

        startUrlField = new TextField("https://en.wikipedia.org/wiki/Special:Random");
        rootGrid.add(startUrlField, 1, 1);

        Label iterationsLabel = new Label("# Iterations:");
        rootGrid.add(iterationsLabel, 0, 2);

        iterationsField = new TextField("100");
        rootGrid.add(iterationsField, 1, 2);

        btnStartCrawl = new Button("Start Crawl");
        rootGrid.add(btnStartCrawl, 0, 3 );
        btnStartCrawl.setOnAction(new StartCrawlHandler());

        chkDomainRestrict = new CheckBox("Restrict Domain");
        rootGrid.add(chkDomainRestrict, 1, 3);

        TableView<WordCount> wordTable = new TableView<>();
        wordTable.setEditable(false);

        TableColumn<WordCount, String> wordColumn = new TableColumn<>("Word");
        wordColumn.setMinWidth(wordTable.getWidth()/2);
        wordColumn.setCellValueFactory(data -> data.getValue().getWord());

        TableColumn<WordCount, Integer> countColumn = new TableColumn<>("Count");
        countColumn.setMinWidth(wordTable.getWidth()/2);
        countColumn.setCellValueFactory(data -> data.getValue().getCount().asObject());

        wordTable.setItems(data);
        wordTable.getColumns().addAll(wordColumn, countColumn);

        rootGrid.add(wordTable, 2, 0, 2, 6);

        Scene scene = new Scene(rootGrid,
                Screen.getPrimary().getBounds().getMaxX()/3,
                Screen.getPrimary().getBounds().getMaxY()/3);
        stage.getIcons().add(new Image("icon.png"));
        stage.setScene(scene);
        stage.setTitle("Web Crawler");
        stage.show();
    }

    public class WordCount {
        private final SimpleStringProperty word;
        private final SimpleIntegerProperty count;

        WordCount(String word, int count) {
            this.word = new SimpleStringProperty(word);
            this.count = new SimpleIntegerProperty(count);
        }

        public SimpleStringProperty getWord() {
            return word;
        }

        public SimpleIntegerProperty getCount() {
            return count;
        }
    }

    class CrawlerTask extends Thread {
        @Override
        public void run() {
            btnStartCrawl.setDisable(true);
            data.removeAll(data);
            WebCrawler webCrawler = new WebCrawler(startUrlField.getText(), chkDomainRestrict.isSelected());
            int iterations = 0;
            try {
                 iterations = Integer.parseInt(iterationsField.getText());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            while (webCrawler.getNumTraversed() < iterations) {
                if (webCrawler.doTraversal() == -1) {
                    btnStartCrawl.setDisable(false);
                    return;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Map<String, Integer> wordsCount = webCrawler.getWordsCount();
            for (String key: wordsCount.keySet()) {
                data.add(new WordCount(key, wordsCount.get(key)));
            }
            btnStartCrawl.setDisable(false);
        }
    }

    class StartCrawlHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {
            CrawlerTask crawlerTask = new CrawlerTask();
            crawlerTask.start();
        }
    }

    public static void main(String[] args) { launch(); }
}
