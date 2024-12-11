package com.Program;
import java.util.List;
import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.paint.Color;

import java.io.*;
import java.time.LocalDate;
import java.util.*;


import com.google.gson.Gson;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Main extends Application {
    VBox root = new VBox();
    Stage firStage = new Stage();
    Scene scene = new Scene(root, 300, 400);

    @Override
    public void start(Stage firstStage) {
        firstStage.setScene(scene);
        firstStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
