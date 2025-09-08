package com.kq.gob.fx;

import com.kq.gob.Gob.Gob;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Simple JavaFX front end for the Gob language.
 * The application allows entering source code and executing it
 * by calling the core {@link Gob#run(String)} interpreter.
 */
public class GobFxApp extends Application {

    private Group buildLogoGraphic() {
        Circle apple = new Circle(50, Color.web("#CD8C00"));
        Rectangle stem = new Rectangle(48, -20, 4, 20);
        stem.setFill(Color.web("#502800"));
        Ellipse leaf = new Ellipse(70, -35, 20, 10);
        leaf.setFill(Color.web("#009966"));
        return new Group(apple, stem, leaf);
    }

    private TextArea loadLesson(String resource) {
        TextArea area = new TextArea();
        area.setEditable(false);
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in != null) {
                area.setText(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            } else {
                area.setText("Cashar ma jiro: " + resource);
            }
        } catch (IOException ex) {
            area.setText("Khalad: " + ex.getMessage());
        }
        return area;
    }

    private Tab lessonTab(String title, String resource) {
        Tab tab = new Tab(title, loadLesson(resource));
        tab.setClosable(false);
        return tab;
    }

    @Override
    public void start(Stage stage) {
        TextArea codeArea = new TextArea();
        codeArea.setPromptText("Geli koodhka Gob...");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Natiijo...");

        Button runButton = new Button("Orod");
        runButton.setOnAction(e -> {
            String source = codeArea.getText();
            String result = Gob.run(source);
            outputArea.setText(result);
        });

        SplitPane splitPane = new SplitPane(codeArea, outputArea);
        splitPane.setDividerPositions(0.5);

        BorderPane codePane = new BorderPane();
        codePane.setCenter(splitPane);
        BorderPane.setMargin(runButton, new Insets(5));
        codePane.setBottom(runButton);

        TabPane lessonTabs = new TabPane();
        lessonTabs.getTabs().addAll(
                lessonTab("Hordhac", "/lessons/introduction.txt"),
                lessonTab("Noocyada Xogta", "/lessons/data-types.txt"),
                lessonTab("Xulashooyinka", "/lessons/conditionals.txt"),
                lessonTab("Wareegyada", "/lessons/loops.txt"),
                lessonTab("Qabte", "/lessons/functions.txt"),
                lessonTab("OOP", "/lessons/oop.txt")
        );

        TabPane tabs = new TabPane();
        Tab codeTab = new Tab("Koodh", codePane);
        codeTab.setClosable(false);
        Tab tutorialTab = new Tab("Casharo", lessonTabs);
        tutorialTab.setClosable(false);
        tabs.getTabs().addAll(codeTab, tutorialTab);

        StackPane logoPane = new StackPane(buildLogoGraphic());
        logoPane.setPadding(new Insets(10));
        WritableImage logoImage = logoPane.snapshot(new SnapshotParameters(), null);
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitHeight(64);
        logoView.setPreserveRatio(true);

        Label copyright = new Label("© Made by Yaser Issa");

        BorderPane root = new BorderPane();
        root.setTop(logoView);
        root.setCenter(tabs);
        BorderPane.setMargin(copyright, new Insets(5));
        root.setBottom(copyright);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("Tarjumaan Gob");
        stage.getIcons().add(logoImage);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
