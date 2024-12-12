package com.Program;
import java.util.List;
import java.util.Set;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Region;

public class Main extends Application {

    VBox root = new VBox(10);
    HBox toBeNext = new HBox(10);
    HBox toCenter = new HBox(10);
    Scene scene = new Scene(root, 600, 400);
    TableView table = new TableView<>();
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
        selection.setOnAction(e -> {
            String selected = selection.getValue(); 
            System.out.println("Seçilen meyve: " + selected);
        });
        sketchuler.setStyle("-fx-font-size: 35px; " + 
                            "-fx-text-fill: #71D0F2; " + 
                            "-fx-font-family: 'Comic Sans MS'; "+
                            "-fx-font-weight: bold;"); 
        toCenter.getChildren().add(sketchuler);
        toBeNext.getChildren().addAll(selection, mainProceed);
        root.getChildren().addAll(menuBar,spacer1,toCenter,spacer3,toBeNext,spacer2,table);
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

        firstStage.setTitle("Sketchuler");
        firstStage.setScene(scene);
        firstStage.show();
    }

    public static void main(String[] args) {
        // Initialize the DatabaseLoader
        DatabaseLoader databaseLoader = new DatabaseLoader();
        databaseLoader.start();

        // Retrieve data from the loader
        List<Course> courses = databaseLoader.getCourses();
        Set<Teacher> teachers = databaseLoader.getTeachers();
        Set<Student> students = databaseLoader.getStudents();
        List<Classroom> classrooms = databaseLoader.getClassrooms();

        // Initialize the CourseManager
        CourseManager courseManager = new CourseManager(courses, teachers, students, classrooms);

        // Allocate classrooms to courses
        courseManager.allocateClassrooms();

        // Display results (Example output)
        for (Course course : courses) {
            Classroom classroom = course.getClassroom();
            System.out.println("Course: " + course.getName() + ", Day: " + course.getDay() + ", Start Time: " + course.getStartTime() + ", Classroom: " + (classroom != null ? classroom.getName() : "Not Allocated"));
        }
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
        }

        launch(args);

    }
}
