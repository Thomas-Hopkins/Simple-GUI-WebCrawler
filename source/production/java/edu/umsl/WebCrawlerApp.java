package edu.umsl;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
    private TextArea outputField;
    private Label progressLabel;
    private Button btnStartCrawl;
    private CheckBox chkDomainRestrict;
    private ProgressBar progressBar;
    private TableView<WordCount> wordTable;
    private final ObservableList<WordCount> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) throws Exception {
        // Setup grid //
        GridPane rootGrid = new GridPane();
        rootGrid.setHgap(5.5);
        rootGrid.setVgap(5.5);
        rootGrid.setPadding(new Insets(12, 12, 12, 12));

        // Configuration area //
        Label configLabel = new Label("Webcrawler Configuration");
        configLabel.setStyle("-fx-font-weight: bold");
        rootGrid.add(configLabel, 0, 0, 2, 1);

        Label startUrlLabel = new Label("Starting URL:");
        rootGrid.add(startUrlLabel, 0, 1);

        startUrlField = new TextField("https://en.wikipedia.org/wiki/Special:Random");
        rootGrid.add(startUrlField, 1, 1);

        Label iterationsLabel = new Label("# Iterations:");
        rootGrid.add(iterationsLabel, 0, 2);

        iterationsField = new TextField("100");
        rootGrid.add(iterationsField, 1, 2);

        // Start Button Area //
        btnStartCrawl = new Button("Start Crawl");
        rootGrid.add(btnStartCrawl, 0, 3 );
        btnStartCrawl.setOnAction(new StartCrawlHandler());

        chkDomainRestrict = new CheckBox("Restrict Domain");
        rootGrid.add(chkDomainRestrict, 1, 3);

        // Progress Bar Area //
        progressBar = new ProgressBar(0);
        rootGrid.add(progressBar, 0, 4 );

        progressLabel = new Label();
        rootGrid.add(progressLabel, 1, 4);

        // Status Output Area //
        outputField = new TextArea();
        outputField.maxWidthProperty().bind(iterationsLabel.widthProperty().multiply(4));
        rootGrid.add(outputField, 0, 5, 2, 1);

        // Word Count Table //
        wordTable = new TableView<>();
        wordTable.setEditable(false);

        TableColumn<WordCount, String> wordColumn = new TableColumn<>("Word");
        wordColumn.setCellValueFactory(data -> data.getValue().getWord());

        TableColumn<WordCount, Integer> countColumn = new TableColumn<>("Count");
        countColumn.setCellValueFactory(data -> data.getValue().getCount().asObject());

        wordTable.setItems(data);
        wordTable.getColumns().addAll(wordColumn, countColumn);

        countColumn.setSortType(TableColumn.SortType.DESCENDING);
        wordTable.getSortOrder().add(countColumn);

        rootGrid.add(wordTable, 2, 0, 2, 6);

        // Parent to scene //
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

    class CrawlerTask extends Task<Boolean> {
        @Override
        protected Boolean call() throws Exception {
            // Disable start button, cleanup table data, and instantiate the WebCrawler
            btnStartCrawl.setDisable(true);
            wordTable.getItems().clear();
            WebCrawler webCrawler = new WebCrawler(startUrlField.getText(), chkDomainRestrict.isSelected());

            // Try to get the number of iterations from the field
            int iterations = 0;
            try {
                iterations = Integer.parseInt(iterationsField.getText());
            } catch (NumberFormatException e) {
                updateMessage("Enter an Integer for iterations.");
            }

            // Start the main iterations loop
            while (webCrawler.getNumTraversed() < iterations) {
                String currUrl = webCrawler.getCurrentUrl();
                webCrawler.doTraversal();
                if (webCrawler.getCurrentError() != null) {
                    // Hit an error
                    updateMessage("ERROR: " + webCrawler.getCurrentError() + "\n" + outputField.getText());
                    if (webCrawler.getCurrentUrl() == null) {
                        // If curr url is null there are no more urls left
                        btnStartCrawl.setDisable(false);
                        break;
                    }
                } else {
                    updateMessage("Crawling...\n" + currUrl + "\n" + outputField.getText().replaceFirst("Crawling...\n", ""));
                }
                // Sleep between iterations to avoid DDOS
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Update our progress and message output for visuals
                updateProgress(webCrawler.getNumTraversed(), iterations);
            }
            // End loop, get our resulting word count and set it to our data.
            Map<String, Integer> wordsCount = webCrawler.getWordsCount();
            for (String key: wordsCount.keySet()) {
                data.add(new WordCount(key, wordsCount.get(key)));
            }
            // Re-enable the start button and sort the table
            btnStartCrawl.setDisable(false);
            wordTable.sort();
            return true;
        }
    }

    class StartCrawlHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {
            CrawlerTask crawlerTask = new CrawlerTask();
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(crawlerTask.progressProperty());
            progressLabel.textProperty().bind(Bindings.format("%.0f%%",crawlerTask.progressProperty().multiply(100)));
            outputField.textProperty().bind(crawlerTask.messageProperty());
            new Thread(crawlerTask).start();
        }
    }

    public static void main(String[] args) { launch(); }
}
