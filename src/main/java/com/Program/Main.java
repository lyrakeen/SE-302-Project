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
<<<<<<< Updated upstream
    VBox root = new VBox();
    Stage firStage = new Stage();
    Scene scene = new Scene(root, 300, 400);
=======

    static List<Course> courses;
    static Set<Teacher> teachers;
    static Set<Student> students;
    static List<Classroom> classrooms;

    VBox root = new VBox(10);
    HBox toBeNext = new HBox(10);
    HBox toCenter = new HBox(10);
    TableView table = new TableView<>();
    HBox forTablePadding = new HBox(table);
    Scene scene = new Scene(root, 900, 500);
    MenuBar menuBar = new MenuBar();
    Menu fileMenu = new Menu("File");
    Menu helpMenu = new Menu("Help");
    MenuItem importItem = new MenuItem("Import Files");
    MenuItem teacherItem = new MenuItem("Manage Teachers");
    MenuItem studentItem = new MenuItem("Manage Students");
    MenuItem courseItem = new MenuItem("Manage Courses");
    MenuItem saveItem = new MenuItem("Save");
    MenuItem quitItem = new MenuItem("Quit");
    MenuItem aboutItem = new MenuItem("About");
    MenuItem manualItem = new MenuItem("Manual");
    Region spacer1 = new Region();
    Region spacer2 = new Region();
    Region spacer3 = new Region();
    Label sketchuler = new Label("Sketchuler");
>>>>>>> Stashed changes

    @Override
    public void start(Stage firstStage) {
        firstStage.setScene(scene);
        firstStage.show();
    }

<<<<<<< Updated upstream
=======
    private void selectionResult(String selected) {
        table.getItems().clear();
        table.getColumns().clear();
        switch (selected) {
            case "Courses" :
                TableColumn<Course, String> nameColumn = new TableColumn<>("Course Name");
                nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

                TableColumn<Course, String> lecturerColumn = new TableColumn<>("Lecturer");
                lecturerColumn.setCellValueFactory(new PropertyValueFactory<>("lecturer"));

                TableColumn<Course, String> classroomColumn = new TableColumn<>("Classroom");
                classroomColumn.setCellValueFactory(cellData -> {
                    Classroom classroom = cellData.getValue().getClassroom();
                    return classroom != null ? new SimpleStringProperty(classroom.getName()) : new SimpleStringProperty("No Classroom");
                });

                table.getColumns().setAll(nameColumn, lecturerColumn, classroomColumn);
                adjustSize(table, new TableColumn[]{nameColumn, lecturerColumn, classroomColumn}, 0.33333);
                table.getItems().addAll(courses);
                break;

            case "Students" :
                TableColumn<Student, String> studentNameColumn = new TableColumn<>("Student Name");
                studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

                TableColumn<Student, String> enrolledCoursesColumn = new TableColumn<>("Enrolled Courses");
                enrolledCoursesColumn.setCellValueFactory(cellData -> {
                    List<Course> enrolledCourses = cellData.getValue().getEnrolledCourses();
                    String coursesString = enrolledCourses.stream().map(Course::getName).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
                    return new SimpleStringProperty(coursesString);
                });

                table.getColumns().setAll(studentNameColumn, enrolledCoursesColumn);
                adjustSize(table, new TableColumn[]{studentNameColumn, enrolledCoursesColumn}, 0.5);
                table.getItems().addAll(students);
                break;

            case "Teachers" :
                TableColumn<Teacher, String> teacherNameColumn = new TableColumn<>("Teacher Name");
                teacherNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

                TableColumn<Teacher, String> assignedCoursesColumn = new TableColumn<>("Assigned Courses");
                assignedCoursesColumn.setCellValueFactory(cellData -> {
                    List<Course> assignedCourses = cellData.getValue().getAssignedCourses();
                    String coursesString = assignedCourses.stream().map(Course::getName).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
                    return new SimpleStringProperty(coursesString);
                });

                table.getColumns().setAll(teacherNameColumn, assignedCoursesColumn);
                adjustSize(table, new TableColumn[]{teacherNameColumn, assignedCoursesColumn}, 0.5);
                table.getItems().addAll(teachers);
                break;

            case "Classes" :
                TableColumn<Classroom, String> classroomNameColumn = new TableColumn<>("Classroom Name");
                classroomNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

                TableColumn<Classroom, Integer> capacityColumn = new TableColumn<>("Capacity");
                capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));

                TableColumn<Classroom, String> assignedClassesColumn = new TableColumn<>("Assigned Courses");
                assignedClassesColumn.setCellValueFactory(cellData -> {
                    List<Course> assignedCourses = cellData.getValue().getAssignedCourses();
                    String coursesString = assignedCourses.stream().map(Course::getName).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
                    return new SimpleStringProperty(coursesString);
                });

                table.getColumns().setAll(classroomNameColumn, capacityColumn, assignedClassesColumn);
                adjustSize(table, new TableColumn[]{classroomNameColumn, capacityColumn, assignedClassesColumn}, 0.33333);
                table.getItems().addAll(classrooms);
                break;
        }
    }

    private void adjustSize(TableView<?> table, TableColumn<?, ?>[] columns, double widthPercentage) {
        for (TableColumn<?, ?> column : columns) {
            column.prefWidthProperty().bind(table.widthProperty().multiply(widthPercentage));
        }
    }

>>>>>>> Stashed changes
    public static void main(String[] args) {
        launch(args);
    }
}
