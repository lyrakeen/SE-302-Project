package com.Program;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Region;
import javafx.scene.control.TableColumn;

public class Main extends Application {

    static List<Course> courses;
    static Set<Teacher> teachers;
    static Set<Student> students;
    static List<Classroom> classrooms;

    VBox root = new VBox(10);
    HBox toBeNext = new HBox(10);
    HBox toCenter = new HBox(10);
    TableView table = new TableView<>();
    HBox forTablePadding = new HBox(table);
    Scene scene = new Scene(root, 600, 400);
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

    @Override
    public void start(Stage firstStage) {
        menuBar.getMenus().addAll(fileMenu, helpMenu);
        fileMenu.getItems().addAll(importItem,teacherItem,studentItem,courseItem,saveItem,quitItem);
        helpMenu.getItems().addAll(aboutItem,manualItem);
        Button mainProceed = new Button("Proceed");
        ComboBox<String> selection = new ComboBox<>();
        selection.getItems().addAll("Courses", "Students", "Teachers", "Classes");
        sketchuler.setStyle("-fx-font-size: 60px; " + 
                            "-fx-text-fill:rgb(255, 157, 0); " + 
                            "-fx-font-family: 'Caveat'; "+
                            "-fx-font-weight: normal;"); 
        toCenter.getChildren().add(sketchuler);
        toBeNext.getChildren().addAll(selection, mainProceed);
        root.getChildren().addAll(menuBar,spacer1,toCenter,spacer3,toBeNext,spacer2,forTablePadding);
        forTablePadding.setPadding(new Insets(10));
        toBeNext.setAlignment(Pos.CENTER);
        toCenter.setAlignment(Pos.CENTER);
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        VBox.setVgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        VBox.setVgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        VBox.setVgrow(spacer3, Priority.ALWAYS);
        HBox.setHgrow(table, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);
        mainProceed.setOnAction(e -> selectionResult(selection.getValue()));

        firstStage.setTitle("Sketchuler");
        firstStage.setScene(scene);
        firstStage.show();
    }

    private void selectionResult(String selected) {
        switch (selected) {
            case "Courses" :
                //kursun ismi hocası klasrumu
                TableColumn<Course, String> nameColumn = new TableColumn<>("Course Name");
                nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

                TableColumn<Course, String> lecturerColumn = new TableColumn<>("Lecturer");
                lecturerColumn.setCellValueFactory(new PropertyValueFactory<>("lecturer"));

                TableColumn<Course, String> classroomColumn = new TableColumn<>("Classroom");
                classroomColumn.setCellValueFactory(cellData -> {
                    Classroom classroom = cellData.getValue().getClassroom();
                    return classroom != null ? new SimpleStringProperty(classroom.getName()) : new SimpleStringProperty("No Classroom");
                });

                TableColumn<?, ?>[] columns = {nameColumn, lecturerColumn, classroomColumn};
                System.out.println(courses.get(0).getDay());
                table.getColumns().setAll(columns);
                adjustSize(table, columns, 0.33333);
                table.getItems().addAll(courses);
                break;

            

        }

    }

    private void adjustSize(TableView<?> table, TableColumn<?, ?>[] columns, double widthPercentage) { // to adjust tableview columns width
        for (TableColumn<?, ?> column : columns) {
            column.prefWidthProperty().bind(table.widthProperty().multiply(widthPercentage));
        }
    }

    public static void main(String[] args) {
        // Initialize the DatabaseLoader
        DatabaseLoader databaseLoader = new DatabaseLoader();
        databaseLoader.start();

        // Retrieve data from the loader
        courses = databaseLoader.getCourses();
        teachers = databaseLoader.getTeachers();
        students = databaseLoader.getStudents();
        classrooms = databaseLoader.getClassrooms();

        // Initialize the CourseManager
        CourseManager courseManager = new CourseManager(courses, teachers, students, classrooms);

        // Allocate classrooms to courses
        courseManager.allocateClassrooms();

        // Display results (Example output)
         for (Course course : courses) {
            Classroom classroom = course.getClassroom();
            System.out.println("Course: " + course.getName() + ", Day: " + course.getDay() + ", Start Time: " + course.getStartTime() + ", Classroom: " + (classroom != null ? classroom.getName() : "Not Allocated"));
        } /* 
        System.out.println("\nTeachers and their courses:");
        for (Teacher teacher : databaseLoader.getTeachers()) {
            System.out.println("Teacher: " + teacher.getFullName());
            for (Course course : teacher.getAssignedCourses()) {
                System.out.println("  Course: " + course.getName());
            }
        }
        System.out.println("\nStudents and their enrolled courses:");
        for (Student student : databaseLoader.getStudents()) {
            System.out.println("Student: " + student.getFullName());
            for (Course course : student.getEnrolledCourses()) {
                System.out.println("  Enrolled in: " + course.getName() + " on " + course.getDay() + " at " + course.getStartTime());
            }
        } */

        launch(args);

    }
}
