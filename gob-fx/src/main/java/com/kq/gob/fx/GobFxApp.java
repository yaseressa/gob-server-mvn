package com.kq.gob.fx;

import com.kq.gob.Gob.Gob;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Simple JavaFX front end for the Gob language.
 * The application allows entering source code and executing it
 * by calling the core {@link Gob#run(String)} interpreter.
 *
**/
public class GobFxApp extends Application {

    @Override
    public void start(Stage stage) {
        TextArea codeArea = new TextArea();
        codeArea.setPromptText("Enter Gob code...");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Output...");

        Button runButton = new Button("Run");
        runButton.setOnAction(e -> {
            String source = codeArea.getText();
            String result = Gob.run(source);
            outputArea.setText(result);
        });

        SplitPane splitPane = new SplitPane(codeArea, outputArea);
        splitPane.setDividerPositions(0.5);

        BorderPane root = new BorderPane();
        root.setCenter(splitPane);
        BorderPane.setMargin(runButton, new Insets(5));
        root.setBottom(runButton);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Gob Interpreter");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
