package edu.umsl;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class WebCrawlerApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        VBox leftPanel = new VBox();
        leftPanel.getChildren().add(new Label("Webcrawler configuration:"));
        leftPanel.setSpacing(5.5);
        leftPanel.setPadding(new Insets(10, 10, 10, 10));

        HBox startUrlPanel = new HBox();
        startUrlPanel.getChildren().add(new Label("Starting URL:"));
        startUrlPanel.getChildren().add(new TextField());
        startUrlPanel.getChildren().add(new Label("If left blank will start at a random Wikipedia article."));
        startUrlPanel.setSpacing(5.5);
        leftPanel.getChildren().add(startUrlPanel);

        HBox iterationPanel = new HBox();
        iterationPanel.getChildren().add(new Label("Iterations:"));
        iterationPanel.getChildren().add(new TextField());
        iterationPanel.setSpacing(5.5);
        leftPanel.getChildren().add(iterationPanel);

        Button button = new Button("Start Crawl");
        leftPanel.getChildren().add(button);

        Scene scene = new Scene(new StackPane(leftPanel), Screen.getPrimary().getBounds().getMaxX()/3, Screen.getPrimary().getBounds().getMaxY()/3);
        stage.getIcons().add(new Image("icon.png"));
        stage.setScene(scene);
        stage.setTitle("Web Crawler");
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}
