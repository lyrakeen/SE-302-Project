package com.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    static List<Course> courses;
    static Set<Teacher> teachers;
    static Set<Student> students;
    static List<Classroom> classrooms;

    VBox root = new VBox(10);
    VBox infoRoot = new VBox(10);
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
    Stage infoStage = new Stage();
    Scene infoScene = new Scene(infoRoot);

    private String currentTab = null;

    @Override
    public void start(Stage firstStage) {
        menuBar.getMenus().addAll(fileMenu, helpMenu);
        fileMenu.getItems().addAll(importItem, teacherItem, studentItem, courseItem, saveItem, quitItem);
        helpMenu.getItems().addAll(aboutItem, manualItem);

        Button mainProceed = new Button("Proceed");
        Button clearButton = new Button("Clear");
        ComboBox<String> dayFilterBox = new ComboBox<>();
        dayFilterBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        dayFilterBox.setPromptText("Filter by Day");

        Button searchButton = new Button("Search");
        ComboBox<String> selection = new ComboBox<>();
        selection.getItems().addAll("Courses", "Students", "Teachers", "Classes");

        sketchuler.setStyle("-fx-font-size: 60px; " +
                "-fx-text-fill:rgb(255, 157, 0); " +
                "-fx-font-family: 'Caveat'; "+
                "-fx-font-weight: normal;");

        toCenter.getChildren().add(sketchuler);
        toBeNext.getChildren().addAll(selection, mainProceed, searchButton, dayFilterBox, clearButton);
        root.getChildren().addAll(menuBar, spacer1, toCenter, spacer3, toBeNext, spacer2, forTablePadding);

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

        mainProceed.setOnAction(e -> {
            if (selection.getValue() == null) {
                showAlert("Please select an option before proceeding.");
                return;
            }
            currentTab = selection.getValue();
            selectionResult(currentTab);
        });

        clearButton.setOnAction(e -> {
            table.getItems().clear();
            table.getColumns().clear();
            currentTab = null;
        });

        searchButton.setOnAction(e -> {
            if (currentTab == null) {
                showAlert("Please proceed with a selection before searching.");
                return;
            }

            String query = showInputDialog("Enter search term for " + currentTab);
            if (query == null || query.isEmpty()) {
                return;
            }

            switch (currentTab) {
                case "Students":
                    List<Student> filteredStudents = new ArrayList<>();
                    for (Student student : students) {
                        if (student.getFullName().toLowerCase().contains(query.toLowerCase())) {
                            filteredStudents.add(student);
                        }
                    }
                    table.getItems().setAll(filteredStudents);
                    break;
                case "Teachers":
                    List<Teacher> filteredTeachers = new ArrayList<>();
                    for (Teacher teacher : teachers) {
                        if (teacher.getFullName().toLowerCase().contains(query.toLowerCase())) {
                            filteredTeachers.add(teacher);
                        }
                    }
                    table.getItems().setAll(filteredTeachers);
                    break;
                case "Courses":
                    List<Course> filteredCourses = new ArrayList<>();
                    for (Course course : courses) {
                        if (course.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredCourses.add(course);
                        }
                    }
                    table.getItems().setAll(filteredCourses);
                    break;
                case "Classes":
                    List<Classroom> filteredClassrooms = new ArrayList<>();
                    for (Classroom classroom : classrooms) {
                        if (classroom.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredClassrooms.add(classroom);
                        }
                    }
                    table.getItems().setAll(filteredClassrooms);
                    break;
                default:
                    showAlert("Search is not supported for the selected tab.");
            }
        });

        dayFilterBox.setOnAction(e -> {
            if (currentTab == null || !currentTab.equals("Courses")) {
                showAlert("Day filtering is only available for Courses.");
                return;
            }

            String selectedDay = dayFilterBox.getValue();
            if (selectedDay == null) {
                return;
            }

            List<Course> filteredByDay = new ArrayList<>();
            for (Course course : courses) {
                if (course.getDay().equalsIgnoreCase(selectedDay)) {
                    filteredByDay.add(course);
                }
            }

            table.getItems().setAll(filteredByDay);
        });

        table.setOnMouseClicked(event -> {
            displayInfo(table.getSelectionModel().getSelectedItem());
        });

        firstStage.setTitle("Sketchuler");
        firstStage.setScene(scene);
        firstStage.show();
    }

    private void displayInfo(Object selected) {
        if (selected instanceof Course) {
            infoRoot.getChildren().clear();

            Course course = (Course) selected;
            Label nameLabel = new Label("Name");
            Label name = new Label(course.getName());
            Label timeStartLabel = new Label("Start Time");
            Label time = new Label(course.getTimeToStart());
            Label durationLabel = new Label("Duration");
            Label duration = new Label((course.getEndTime())); // durationa dönecek
            Label dayLabel = new Label("Day");
            Label day = new Label(course.getDay());
            Label lecturerLabel = new Label("Lecturer");
            Label lecturer = new Label(course.getLecturer());

            HBox first = new HBox(5);
            HBox second = new HBox(5);
            HBox third = new HBox(5);
            HBox fourth = new HBox(5);
            HBox fifth = new HBox(5);

            first.getChildren().addAll(nameLabel, name);
            second.getChildren().addAll(timeStartLabel, time);
            third.getChildren().addAll(durationLabel, duration);
            fourth.getChildren().addAll(dayLabel, day);
            fifth.getChildren().addAll(lecturerLabel, lecturer);
            infoRoot.getChildren().addAll(fifth, fourth, third, second, first);
            infoStage.setScene(infoScene);
            infoStage.show();
        }
    }

    private void selectionResult(String selected) {
        table.getItems().clear();
        table.getColumns().clear();
        switch (selected) {
            case "Courses":
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

            case "Students":
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

            case "Teachers":
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

            case "Classes":
                TableColumn<Classroom, String> classroomNameColumn = new TableColumn<>("Classroom Name");
                classroomNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

                TableColumn<Classroom, Integer> capacityColumn = new TableColumn<>("Capacity");
                capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));

                TableColumn<Classroom, String> assignedClassesColumn = new TableColumn<>("Assigned Classes");
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String showInputDialog(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search");
        dialog.setHeaderText(prompt);
        dialog.setContentText("Search term:");
        return dialog.showAndWait().orElse(null);
    }

    public static void main(String[] args) {
        DatabaseLoader databaseLoader = new DatabaseLoader();
        databaseLoader.start();

        courses = databaseLoader.getCourses();
        teachers = databaseLoader.getTeachers();
        students = databaseLoader.getStudents();
        classrooms = databaseLoader.getClassrooms();

        CourseManager courseManager = new CourseManager(courses, teachers, students, classrooms);
        courseManager.allocateClassrooms();

        launch(args);
    }
}
