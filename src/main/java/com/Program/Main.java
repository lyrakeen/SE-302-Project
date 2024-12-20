package com.Program;
import javafx.application.Platform;

import java.io.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;


public class Main extends Application {

    static List<Course> courses;
    static Set<Teacher> teachers;
    static Set<Student> students;
    static List<Classroom> classrooms;
    static DatabaseLoader databaseLoader = new DatabaseLoader();
    static CourseManager courseManager;
    List<Student> courseStudents;
    List<String> studentNames;
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
    Stage manualStage = new Stage();
    Scene infoScene = new Scene(infoRoot);

    private String currentTab = null;

    @Override
    public void start(Stage firstStage) {
        menuBar.getMenus().addAll(fileMenu, helpMenu);
        fileMenu.getItems().addAll(importItem, teacherItem, studentItem, courseItem, saveItem, quitItem);
        helpMenu.getItems().addAll(aboutItem, manualItem);

        saveItem.setOnAction(e -> {
            showAlert("Save Successful all visible data changes are saved temporarily.");
        });

        quitItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Quit");
            alert.setHeaderText("Do you want to save changes before quitting?");
            alert.setContentText("Choose your action:");

            ButtonType saveAndQuit = new ButtonType("Save and Quit");
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(saveAndQuit, cancel);

            alert.showAndWait().ifPresent(response -> {
                if (response == saveAndQuit) {
                    showAlert("Save Successful all visible data changes are saved temporarily.");
                    Platform.exit();
                }
            });
        });
        if (!fileMenu.getItems().contains(quitItem)) {
            fileMenu.getItems().add(quitItem);
        }

        courseItem.setOnAction(e -> {
            managingCourses();
        });
        teacherItem.setOnAction(e -> managingTeachers());
        studentItem.setOnAction(e -> managingStudents());

        importItem.setOnAction(event -> {
            List<String> options = new ArrayList<>();
            options.add("Courses.csv");
            options.add("ClassroomCapacity.csv");

            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Courses.csv", options);
            choiceDialog.setTitle("Select File to Replace");
            choiceDialog.setHeaderText("Choose the file you want to replace:");
            choiceDialog.setContentText("File:");

            String selectedFileName = choiceDialog.showAndWait().orElse(null);
            if (selectedFileName != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select New CSV File");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                File selectedFile = fileChooser.showOpenDialog(null);

                if (selectedFile != null) {
                    try {
                        File existingFile = new File(selectedFileName);
                        File backupFile = new File(selectedFileName.replace(".csv", "_backup.csv"));

                        //bir yanlışlık olmasına karşın eski dosyayı yedekliyor
                        if (existingFile.exists()) {
                            copyFile(existingFile, backupFile);
                            showAlert("Backup created: " + backupFile.getName());
                        }

                        //yeni dosyayı eskisiyle değiştirme
                        copyFile(selectedFile, existingFile);
                        showAlert("File replaced successfully: " + selectedFileName);

                        // Dosya türüne göre işlemler
                        if ("Courses.csv".equals(selectedFileName)) {
                            List<String[]> newCourseData = DatabaseLoader.readCSV("Courses.csv");
                            databaseLoader.insertCoursesData(DriverManager.getConnection("jdbc:sqlite:university.db"), newCourseData);


                            databaseLoader.reloadCourses();

                            // UI güncelleniyor
                            table.getItems().clear();
                            selectionResult("Courses");

                        } else if ("ClassroomCapacity.csv".equals(selectedFileName)) {
                            List<String[]> newClassroomData = DatabaseLoader.readCSV("ClassroomCapacity.csv");
                            databaseLoader.insertClassroomData(DriverManager.getConnection("jdbc:sqlite:university.db"), newClassroomData);


                            databaseLoader.reloadClassrooms();

                            // UI güncelleniyor
                            table.getItems().clear();
                            selectionResult("Classes");
                        }
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                        showAlert("Failed to replace file: " + e.getMessage());
                    }
                } else {
                    showAlert("No file selected.");
                }
            } else {
                showAlert("No file selected to replace.");
            }
        });




        Button mainProceed = new Button("Proceed");
        Button addCourseButton = new Button("Add Course");

        Button addTeacherButton = new Button("Add Teacher");
        Button clearButton = new Button("Clear");
        ComboBox<String> dayFilterBox = new ComboBox<>();
        dayFilterBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        dayFilterBox.setPromptText("Filter by Day");
        dayFilterBox.setEditable(false); // Günlerin sabit olması için

        Button searchButton = new Button("Search");
        ComboBox<String> selection = new ComboBox<>();
        selection.getItems().addAll("Courses", "Students", "Teachers", "Classes");

        sketchuler.setStyle("-fx-font-size: 60px; " +
                "-fx-text-fill:rgb(255, 157, 0); " +
                "-fx-font-family: 'Caveat'; " +
                "-fx-font-weight: normal;");

        toCenter.getChildren().add(sketchuler);
        toBeNext.getChildren().addAll(selection, mainProceed, searchButton, dayFilterBox, clearButton);
        root.getChildren().addAll(menuBar, spacer1, toCenter, spacer3, toBeNext, spacer2, forTablePadding);

        //Label resultCountLabel = new Label("Results: 0");
        //root.getChildren().add(resultCountLabel);

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
            //resultCountLabel.setText("Results: " + table.getItems().size());
        });

        clearButton.setOnAction(e -> {
            table.getItems().clear();
            table.getColumns().clear();
            currentTab = null;
            //resultCountLabel.setText("Results: 0");
        });
        searchButton.setOnAction(e -> {
            if (currentTab == null) {
                showAlert("Please select a tab before searching.");
                return;
            }

            String query = showInputDialog("Enter search term for " + currentTab);
            if (query == null || query.isEmpty()) {
                return;
            }

            if (currentTab.equals("Courses")) {
                List<String> selectedDays = new ArrayList<>();
                if (dayFilterBox.getValue() != null) {
                    selectedDays.add(dayFilterBox.getValue());
                }

                List<Course> filteredCourses = new ArrayList<>();
                for (Course course : courses) {
                    boolean matchesDay = selectedDays.isEmpty() || selectedDays.contains(course.getDay());
                    boolean matchesQuery = course.getName().toLowerCase().contains(query.toLowerCase());

                    if (matchesDay && matchesQuery) {
                        filteredCourses.add(course);
                    }
                }
                table.getItems().setAll(filteredCourses);
            } else {
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
            }
            //resultCountLabel.setText("Results: " + table.getItems().size());
        });

        dayFilterBox.setOnAction(e -> {
            if (currentTab == null || !currentTab.equals("Courses")) {
                showAlert("Day filtering is only available for Courses.");
                return;
            }

            String selectedDay = dayFilterBox.getValue();
            if (selectedDay == null) {
                showAlert("Please select a day for filtering.");
                return;
            }

            List<Course> filteredByDay = new ArrayList<>();
            for (Course course : courses) {
                if (course.getDay().equalsIgnoreCase(selectedDay)) {
                    filteredByDay.add(course);
                }
            }

            table.getItems().setAll(filteredByDay);
            //resultCountLabel.setText("Results: " + table.getItems().size());
        });

        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("About Sketchuler");
            alert.setContentText(
                    "This application is made for scheduling courses with classrooms efficently. It is the project of the course SE 302.");
            alert.setTitle("About");
            alert.showAndWait();
        });

        manualItem.setOnAction(e -> {
            showManual();
        });

        table.setOnMouseClicked(event -> {
            displayInfo(table.getSelectionModel().getSelectedItem());
        });

        firstStage.setTitle("Sketchuler");
        firstStage.setScene(scene);
        firstStage.show();
    }

    private static void copyFile(File source, File destination) throws IOException {
        if (!source.exists()) {
            throw new FileNotFoundException("Source file does not exist: " + source.getAbsolutePath());
        }
        try (InputStream input = new FileInputStream(source);
             OutputStream output = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        }
    }

    private void managingCourses() {
        HBox forButtons = new HBox(5);
        VBox mcBox = new VBox(5);
        Stage mcs = new Stage();
        Scene mcSc = new Scene(mcBox);

        ListView<Course> courseLists = new ListView<>();
        courseLists.getItems().addAll(courses);
        courseLists.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Course> call(ListView<Course> listView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Course course, boolean empty) {
                        super.updateItem(course, empty);
                        if (empty || course == null) {
                            setText(null);
                        } else {
                            setText(course.getName());
                        }
                    }
                };
            }
        });

        Button addCourseButton = new Button("Add");
        Button editC = new Button("Edit");
        Button deleteC = new Button("Delete");

        forButtons.getChildren().addAll(addCourseButton, editC, deleteC);

        addCourseButton.setOnAction(ev -> {
            Stage addCourseStage = new Stage();
            VBox inputBox = new VBox(10);
            inputBox.setPadding(new Insets(10));

            TextField courseNameField = new TextField();
            courseNameField.setPromptText("Enter course name");

            ComboBox<Teacher> lecturerComboBox = new ComboBox<>();
            lecturerComboBox.getItems().addAll(teachers);
            lecturerComboBox.setPromptText("Select Lecturer");

            Button nextButton = new Button("Next");

            inputBox.getChildren().addAll(
                    new Label("Course Name:"), courseNameField,
                    new Label("Select Lecturer:"), lecturerComboBox,
                    nextButton
            );

            Scene addCourseScene = new Scene(inputBox, 400, 300);
            addCourseStage.setScene(addCourseScene);
            addCourseStage.setTitle("Add New Course");
            addCourseStage.show();

            nextButton.setOnAction(e -> {
                String courseName = courseNameField.getText().trim();
                Teacher selectedTeacher = lecturerComboBox.getValue();

                if (courseName.isEmpty() || selectedTeacher == null) {
                    showAlert("All fields must be filled out.");
                    return;
                }

                // Öğretmenin programını göster
                showTeacherSchedule(addCourseStage, courseName, selectedTeacher);
            });
        });

        editC.setOnAction(e -> {
            Course selectedCourse = courseLists.getSelectionModel().getSelectedItem();
            if (selectedCourse == null) {
                showAlert("Please select a course to edit.");
                return;
            }

            Stage editStage = new Stage();
            VBox editBox = new VBox(10);
            editBox.setPadding(new Insets(10));

            // Kurs bilgilerini göster
            Label nameLabel = new Label("Course Name: " + selectedCourse.getName());
            Label timeLabel = new Label("Start Time: " + selectedCourse.getTimeToStart());
            Label durationLabel = new Label("Duration: " + selectedCourse.getDuration());
            Label dayLabel = new Label("Day: " + selectedCourse.getDay());
            Label classroomLabel = new Label("Classroom: " +
                    (selectedCourse.getClassroom() != null ? selectedCourse.getClassroom().getName() : "No Classroom"));

            // Rename Button
            Button renameButton = new Button("Rename");
            renameButton.setOnAction(ev -> {
                TextInputDialog renameDialog = new TextInputDialog(selectedCourse.getName());
                renameDialog.setTitle("Rename Course");
                renameDialog.setHeaderText("Change Course Name");
                renameDialog.setContentText("New Name:");
                renameDialog.showAndWait().ifPresent(newName -> {
                    if (newName.trim().isEmpty()) {
                        showAlert("Course name cannot be empty.");
                        return;
                    }
                    try {
                        databaseLoader.updateCourseName(selectedCourse.getName(), newName);
                        selectedCourse.setName(newName);
                        nameLabel.setText("Course Name: " + newName);
                        courseLists.refresh();
                        showAlert("Course name updated successfully!");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Failed to update course name in database.");
                    }
                });
            });

            // Change Classroom Button
            Button changeClassroomButton = new Button("Change Classroom");
            changeClassroomButton.setOnAction(ev -> {
                ListView<Classroom> classroomList = new ListView<>();
                classroomList.getItems().addAll(classrooms);
                classroomList.setCellFactory(param -> new ListCell<>() {
                    @Override
                    protected void updateItem(Classroom classroom, boolean empty) {
                        super.updateItem(classroom, empty);
                        if (empty || classroom == null) {
                            setText(null);
                        } else {
                            setText(classroom.getName() + " (Capacity: " + classroom.getCapacity() + ")");
                        }
                    }
                });

                Stage classroomStage = new Stage();
                VBox classroomBox = new VBox(10, new Label("Select a Classroom:"), classroomList);
                classroomBox.setPadding(new Insets(10));
                Scene classroomScene = new Scene(classroomBox);
                classroomStage.setScene(classroomScene);
                classroomStage.setTitle("Change Classroom");
                classroomStage.show();

                classroomList.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        Classroom selectedClassroom = classroomList.getSelectionModel().getSelectedItem();
                        if (selectedClassroom != null) {
                            // Kapasite kontrolü
                            if (selectedClassroom.getCapacity() < selectedCourse.getStudents().size()) {
                                showAlert("Classroom capacity is insufficient for this course!");
                                return;
                            }

                                databaseLoader.updateCourseClassroom(selectedCourse.getName(), selectedClassroom.getName());
                                selectedCourse.setClassroom(selectedClassroom);
                                refreshClassrooms();
                                classroomLabel.setText("Classroom: " + selectedClassroom.getName());
                                classroomStage.close();
                                showAlert("Classroom updated successfully!");


                        }
                    }
                });
            });

            HBox buttonBox = new HBox(10, renameButton, changeClassroomButton);
            buttonBox.setAlignment(Pos.CENTER);

            editBox.getChildren().addAll(nameLabel, timeLabel, durationLabel, dayLabel, classroomLabel, buttonBox);

            Scene editScene = new Scene(editBox);
            editStage.setScene(editScene);
            editStage.setTitle("Edit Course");
            editStage.show();
        });


        courseLists.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        /**
         * Implements the logic for safely deleting courses, ensuring the consistency of teacher assignments.
         * This method dynamically adjusts the `assignedCourses` list for any teacher associated with a deleted course.
         * By utilizing a database-backed approach for course deletion, this method maintains synchronization between the runtime state and the persistent storage, thus avoiding data anomalies.
         */

        deleteC.setOnAction(e -> {
            ObservableList<Course> selectedCourses = courseLists.getSelectionModel().getSelectedItems();
            if (!selectedCourses.isEmpty()) {
                for (Course c : selectedCourses) {
                    try {
                        databaseLoader.deleteCourse(c.getName());
                        Teacher assignedTeacher = teachers.stream()
                                .filter(teacher -> teacher.getFullName().equals(c.getLecturer()))
                                .findFirst()
                                .orElse(null);

                        if (assignedTeacher != null) {
                            assignedTeacher.getAssignedCourses().remove(c);
                        }
                        // Even if the assignedCourses list is empty, the teacher is retained in the teachers list, ensuring they remain visible in the UI.
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                // courses.removeAll(selectedCourses); //Bunu zaten databaseLoader'ın deleteCourse methodu yapıyor.
                courseLists.getItems().removeAll(selectedCourses);
                showAlert("Selected courses deleted successfully!");
            }
            else {
                showAlert("Please select at least one course to delete.");
            }
        });

        forButtons.setAlignment(Pos.CENTER);

        mcBox.getChildren().addAll(courseLists, forButtons);
        mcs.setScene(mcSc);
        mcs.show();
    }

    private void managingStudents() {
        HBox forButtons = new HBox(5);
        VBox msBox = new VBox(5);
        Stage mss = new Stage();
        Scene msSc = new Scene(msBox);

        ListView<Student> studentLists = new ListView<>();
        studentLists.getItems().addAll(students);

        Button addStudentButton = new Button("Add Student");
        Button editS = new Button("Edit");
        Button deleteS = new Button("Delete");

        addStudentButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Student");
            dialog.setHeaderText("Enter new student name");
            dialog.setContentText("Name:");

            dialog.setOnHidden(ev -> {
                String studentName = dialog.getResult();
                if (studentName != null && !studentName.trim().isEmpty()) {
                    // Show course selection window
                    Stage courseSelectionStage = new Stage();
                    courseSelectionStage.setTitle("Select Courses for Student");

                    VBox layout = new VBox(10);
                    layout.setPadding(new Insets(10));

                    Label instructionLabel = new Label("Select courses for the student:");
                    ListView<Course> courseListView = new ListView<>();
                    courseListView.getItems().addAll(courses);
                    courseListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

                    Button confirmButton = new Button("Confirm");
                    Button cancelButton = new Button("Cancel");

                    HBox buttonBox = new HBox(10, confirmButton, cancelButton);
                    buttonBox.setAlignment(Pos.CENTER);

                    layout.getChildren().addAll(instructionLabel, courseListView, buttonBox);

                    Scene scene = new Scene(layout);
                    courseSelectionStage.setScene(scene);

                    // Confirm Button Logic
                    confirmButton.setOnAction(ev2 -> {
                        List<Course> selectedCourses = courseListView.getSelectionModel().getSelectedItems();
                        if (selectedCourses.isEmpty()) {
                            showAlert("No courses selected!");
                            return;
                        }

                        List<String> selectedCourseNames = selectedCourses.stream()
                                .map(Course::getName)
                                .collect(Collectors.toList());

                        boolean hasConflict = false;

                        // Check for time conflicts
                        for (int i = 0; i < selectedCourses.size(); i++) {
                            for (int j = i + 1; j < selectedCourses.size(); j++) {
                                if (selectedCourses.get(i).isTimeConflict(selectedCourses.get(j))) {
                                    hasConflict = true;
                                    break;
                                }
                            }
                            if (hasConflict) break;
                        }

                        if (hasConflict) {
                            showAlert("Selected courses have time conflicts!");
                        } else {
                            try {
                                // Hem veritabanına hem de belleğe kaydet
                                databaseLoader.addStudent(studentName, selectedCourseNames);
                                Student newStudent = new Student(studentName);
                                for (Course course : selectedCourses) {
                                    newStudent.enrollCourse(course);
                                    course.getStudents().add(newStudent);
                                }
                                students.add(newStudent);
                                studentLists.getItems().add(newStudent);
                                studentLists.refresh();
                                showAlert("Student added successfully!");
                                courseSelectionStage.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                showAlert("Failed to add student to the database.");
                            }
                        }
                    });

                    // Cancel Button Logic
                    cancelButton.setOnAction(ev2 -> courseSelectionStage.close());

                    // Customize how courses are displayed in the ListView
                    courseListView.setCellFactory(param -> new ListCell<Course>() {
                        @Override
                        protected void updateItem(Course course, boolean empty) {
                            super.updateItem(course, empty);
                            if (empty || course == null) {
                                setText(null);
                            } else {
                                setText(course.getName());
                            }
                        }
                    });

                    courseSelectionStage.showAndWait();
                } else {
                    showAlert("Student name cannot be empty!");
                }
            });

            dialog.show();
        });

        forButtons.getChildren().addAll(addStudentButton, editS, deleteS);


        editS.setOnAction(e -> {
            Student selectedStudent = studentLists.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                openStudentEditStage(selectedStudent);
            } else {
                showAlert("Please select a student to edit.");
            }
        });


        studentLists.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        deleteS.setOnAction(e -> {
            ObservableList<Student> selectedStudents = studentLists.getSelectionModel().getSelectedItems();
            if (!selectedStudents.isEmpty()) {
                for (Student s : selectedStudents) {
                    try {
                        databaseLoader.deleteStudent(s.getFullName());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                students.removeAll(selectedStudents);
                studentLists.getItems().removeAll(selectedStudents);
                showAlert("Selected students deleted successfully!");
            } else {
                showAlert("Please select at least one student to delete.");
            }
        });

        msBox.getChildren().addAll(studentLists, forButtons);
        mss.setScene(msSc);
        mss.show();
    }

    private void managingTeachers() {
        HBox forButtons = new HBox(5);
        VBox mtBox = new VBox(5);
        Stage mts = new Stage();
        Scene mtSc = new Scene(mtBox);

        ListView<Teacher> teacherLists = new ListView<>();
        teacherLists.getItems().addAll(teachers);

        Button addTeacherButton = new Button("Add Teacher");
        addTeacherButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Teacher");
            dialog.setHeaderText("Enter new teacher name");
            dialog.setContentText("Name:");

            dialog.showAndWait().ifPresent(name -> {
                if (!name.trim().isEmpty()) {
                    Teacher newTeacher = new Teacher(name);
                    teachers.add(newTeacher);
                    teacherLists.getItems().add(newTeacher);
                    showAlert("Teacher added successfully!");
                } else {
                    showAlert("Teacher name cannot be empty!");
                }
            });
        });

        Button deleteT = new Button("Delete");

        forButtons.getChildren().addAll(addTeacherButton, deleteT);

        addTeacherButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Teacher");
            dialog.setHeaderText("Enter new teacher name");
            dialog.setContentText("Name:");

            dialog.showAndWait().ifPresent(name -> {
                if (!name.trim().isEmpty()) {
                    Teacher newTeacher = new Teacher(name);
                    teachers.add(newTeacher);
                    teacherLists.getItems().add(newTeacher);
                    showAlert("Teacher added successfully!");
                }
            });
        });
        teacherLists.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        /**
         * Handles the deletion of selected teachers while preserving the system's data integrity.
         * This method ensures that teachers assigned to courses cannot be removed, preventing orphan courses (courses with no assigned teacher). The `getAssignedCourses()` method is leveraged to check
         * course assignments dynamically at runtime, making the system robust against potential state inconsistencies.
         */

        deleteT.setOnAction(e -> {
            ObservableList<Teacher> selectedTeachers = teacherLists.getSelectionModel().getSelectedItems();
            if (selectedTeachers.isEmpty()) {
                showAlert("No teacher selected. Please select at least one teacher to delete.");
                return;
            }

            List<Teacher> teachersToRemove = new ArrayList<>();
            for (Teacher teacher : selectedTeachers) {
                if (!teacher.getAssignedCourses().isEmpty()) {
                    showAlert("Cannot delete teacher: Teacher " + teacher.getFullName() + " is assigned to courses and cannot be deleted.");
                } else {
                    teachersToRemove.add(teacher);
                }
            }

            if (!teachersToRemove.isEmpty()) {
                teachers.removeAll(teachersToRemove);
                teacherLists.getItems().removeAll(teachersToRemove);
                showAlert("Selected teacher(s) deleted successfully!");
            }
        });

        mtBox.getChildren().addAll(teacherLists, forButtons);
        mts.setScene(mtSc);
        mts.show();
    }


        private void displayInfo(Object selected) {

        if (selected instanceof Course) { // search should be added
            infoRoot.getChildren().clear();
            try {
                courseStudents.clear();
                studentNames.clear();
            } catch (Exception e) {
            }

            Course course = (Course) selected;
            Label nameLabel = new Label("Name :");
            Label name = new Label(course.getName());
            Label timeStartLabel = new Label("Start Time :");
            Label time = new Label(course.getTimeToStart());
            Label durationLabel = new Label("Duration :");
            Label duration = new Label((Integer.toString(course.getDuration()))); // durationa dönecek
            Label dayLabel = new Label("Day :");
            Label day = new Label(course.getDay());
            Label lecturerLabel = new Label("Lecturer :");
            Label lecturer = new Label(course.getLecturer());
            Label studentCountLabel = new Label("Student Count : ");
            Label studentCount = new Label(Integer.toString(course.getStudents().size()));
            ListView studentList = new ListView<>();
            List<Student> students = course.getStudents();
            List<String> studentNames = students.stream().map(Student::getFullName).collect(Collectors.toList());
            studentList.getItems().addAll(studentNames);

            HBox nextBox = new HBox(5);
            VBox allIn = new VBox(5);
            HBox first = new HBox(5);
            HBox second = new HBox(5);
            HBox third = new HBox(5);
            HBox fourth = new HBox(5);
            HBox fifth = new HBox(5);
            HBox sixth = new HBox(5);

            infoRoot.setAlignment(Pos.CENTER);
            allIn.setAlignment(Pos.CENTER);
            nextBox.setAlignment(Pos.CENTER);

            first.getChildren().addAll(nameLabel, name);
            second.getChildren().addAll(timeStartLabel, time);
            third.getChildren().addAll(durationLabel, duration);
            fourth.getChildren().addAll(dayLabel, day);
            fifth.getChildren().addAll(lecturerLabel, lecturer);
            sixth.getChildren().addAll(studentCountLabel, studentCount);
            allIn.getChildren().addAll(sixth, fifth, fourth, third, second, first);
            nextBox.getChildren().addAll(allIn, studentList);
            infoRoot.getChildren().addAll(nextBox);
            infoStage.setScene(infoScene);
            infoStage.show();
        }
        if (selected instanceof Student) {
            infoRoot.getChildren().clear();
            Student student = (Student) selected;
            createScheduleTable(FXCollections.observableArrayList(student.getEnrolledCourses()), infoRoot, infoStage, infoScene);
            Label forStudent = new Label(student.getFullName() + "'s Weekly Program");
            infoRoot.getChildren().add(0, forStudent); // BURAYA TASARIM YAPILACAK
        }
        if (selected instanceof Teacher) {
            infoRoot.getChildren().clear();
            Teacher teacher = (Teacher) selected;
            createScheduleTable(FXCollections.observableArrayList(teacher.getAssignedCourses()), infoRoot, infoStage, infoScene);
            Label forTeacher = new Label(teacher.getFullName() + "'s Weekly Program");
            infoRoot.getChildren().add(0, forTeacher); // BURAYA TASARIM YAPILACAK
        }
        if (selected instanceof Classroom) {
            infoRoot.getChildren().clear();
            Classroom classroom = (Classroom) selected;
            createScheduleTable(FXCollections.observableArrayList(classroom.getAssignedCourses()), infoRoot, infoStage, infoScene);
            Label forClass = new Label(classroom.getName() + "'s Weekly Program  Capacity : " + classroom.getCapacity());
            infoRoot.getChildren().add(0, forClass); // BURAYA TASARIM YAPILACAK
        }
    }

    private void createScheduleTable(ObservableList<Course> courses, VBox infoRoot, Stage infoStage, Scene infoScene) {
        TableView<String[]> infoTable = new TableView<>();

        String[] startTimes = {"8:30", "9:25", "10:20", "11:15", "12:10", "13:05", "14:00", "14:55", "15:50", "16:45", "17:40", "18:35"};
        String[][] dayData = new String[5][12];

        @SuppressWarnings("unchecked") // to supress warnings
        ObservableList<Course>[] dayCourses = new ObservableList[]{
                FXCollections.observableArrayList(), // Monday
                FXCollections.observableArrayList(), // Tuesday
                FXCollections.observableArrayList(), // Wednesday
                FXCollections.observableArrayList(), // Thursday
                FXCollections.observableArrayList()  // Friday
        };

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        for (Course course : courses) {
            for (int i = 0; i < days.length; i++) {
                if (course.getDay().equals(days[i])) {
                    dayCourses[i].add(course);
                    break;
                }
            }
        }

        for (int i = 0; i < days.length; i++) {
            fillDayData(dayCourses[i], dayData[i], startTimes);
        }

        TableColumn<String[], String> timeColumn = createTableColumn("Time", 0);
        infoTable.getColumns().add(timeColumn);

        for (int i = 0; i < days.length; i++) {
            TableColumn<String[], String> dayColumn = createTableColumn(days[i], i + 1);
            infoTable.getColumns().add(dayColumn);
        }

        for (int i = 0; i < 12; i++) {
            String[] row = new String[6];
            row[0] = startTimes[i];
            for (int j = 0; j < 5; j++) {
                row[j + 1] = dayData[j][i];
            }
            infoTable.getItems().add(row);
        }
        infoTable.getSelectionModel().setCellSelectionEnabled(false);
        infoTable.setFocusTraversable(false);
        infoTable.setOnMouseClicked(event -> {
            infoTable.getSelectionModel().clearSelection();
        });
        adjustSize(infoTable, infoTable.getColumns().toArray(new TableColumn[0]), 0.16);
        infoRoot.getChildren().addAll(infoTable);
        infoStage.setScene(infoScene);
        infoStage.show();

    }

    private TableColumn<String[], String> createTableColumn(String header, int index) {
        TableColumn<String[], String> column = new TableColumn<>(header);
        column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[index]));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item);
                    setAlignment(Pos.CENTER);
                } else {
                    setText(null);
                }
            }
        });

        column.setSortable(false);
        column.setReorderable(false);

        return column;
    }

    private void fillDayData(ObservableList<Course> dayData, String[] dayArr, String[] startTimes) {
        for (Course course : dayData) {
            int index = -1;
            for (int i = 0; i < startTimes.length; i++) {
                if (course.getStartTime().equals(startTimes[i])) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                for (int j = index; j < index + course.getDuration(); j++) {
                    if (j < 12) {
                        dayArr[j] = course.getName();
                    }
                }
            }
        }
    }


    private void showManual() {
        manualStage.setTitle("Manual");
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(120);

        Button gettingStartedButton = new Button("Getting Started");
        Button navigationButton = new Button("Navigation");
        Button searchingButton = new Button("Searching");
        Button filteringButton = new Button("Filtering");

        sidebar.getChildren().addAll(gettingStartedButton, navigationButton, searchingButton, filteringButton);

        VBox detailContent = new VBox(10);
        detailContent.setPadding(new Insets(10));
        Label detailTitle = new Label("Welcome to the User Manual!");
        detailTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label detailText = new Label("Select a section from the sidebar to learn more about how to use Sketchuler.");
        detailText.setWrapText(true);
        detailContent.getChildren().addAll(detailTitle, detailText);

        HBox manualLayout = new HBox(10);
        manualLayout.getChildren().addAll(sidebar, detailContent);

        gettingStartedButton.setOnAction(event -> {
            detailTitle.setText("Getting Started");
            detailText.setText(
                    "To start using Sketchuler:\n" +
                            "1. Select a category (Courses, Students, Teachers, Classes) from the dropdown menu.\n" +
                            "2. Click 'Proceed' to view the selected category's data in the table.\n" +
                            "3. Use the 'Search' or 'Filter by Day' options to refine your view."
            );
        });

        navigationButton.setOnAction(event -> {
            detailTitle.setText("Navigation");
            detailText.setText(
                    "Navigate through the application:\n" +
                            "1. Use the menu bar for options like Import, Save, or Quit.\n" +
                            "2. Use the dropdown menu and buttons in the main interface to explore data."
            );
        });

        searchingButton.setOnAction(event -> {
            detailTitle.setText("Searching");
            detailText.setText(
                    "Search functionality allows you to find specific items:\n" +
                            "1. Click the 'Search' button.\n" +
                            "2. Enter a keyword to search in the current category.\n" +
                            "3. Results matching your search will be displayed in the table."
            );
        });

        filteringButton.setOnAction(event -> {
            detailTitle.setText("Filtering");
            detailText.setText(
                    "Filter courses by day:\n" +
                            "1. Use the 'Filter by Day' dropdown menu.\n" +
                            "2. Select a day (e.g., Monday, Tuesday).\n" +
                            "3. Only courses Studentd on the selected day will be displayed."
            );
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> manualStage.close());
        detailContent.getChildren().add(closeButton);

        Scene manualScene = new Scene(manualLayout, 600, 400);
        manualStage.setScene(manualScene);
        manualStage.show();
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

    private void openStudentEditStage(Student selectedStudent) {
        // Edit sayfasını aç
        Stage editStage = new Stage();
        VBox editBox = new VBox(10);
        editBox.setPadding(new Insets(10));
        Label programLabel = new Label(selectedStudent.getFullName() + "'s Weekly Program");
        VBox scheduleBox = new VBox();
        Scene editScene = new Scene(editBox, 600, 400);
        createScheduleTable(FXCollections.observableArrayList(selectedStudent.getEnrolledCourses()), scheduleBox, editStage, editScene);

        Button renameButton = new Button("Rename");
        Button changeCourseButton = new Button("Change Course");
        Button assignCourseButton = new Button("Assign Course");

        editStage.setScene(editScene);
        editStage.setTitle("Edit Student - " + selectedStudent.getFullName());
        editStage.show();

        renameButton.setOnAction(ev -> {
            TextInputDialog renameDialog = new TextInputDialog(selectedStudent.getFullName());
            renameDialog.setTitle("Rename Student");
            renameDialog.setHeaderText("Edit Student Name");
            renameDialog.setContentText("New Name:");

            renameDialog.showAndWait().ifPresent(newName -> {
                if (newName.trim().isEmpty()) {
                    showAlert("Name cannot be empty.");
                    return;
                }

                try {
                    // Öğrencinin kayıtlı olduğu derslerin isimlerini al
                    List<String> enrolledCourseNames = selectedStudent.getEnrolledCourses()
                            .stream()
                            .map(Course::getName)
                            .collect(Collectors.toList());

                    // Her bir ders için öğrenci ismini güncelle
                    for (String courseName : enrolledCourseNames) {
                        databaseLoader.updateStudentName(selectedStudent.getFullName(), newName, courseName);
                    }

                    // Bellekte de güncelle
                    selectedStudent.setFullName(newName);

                    editStage.close();
                    openStudentEditStage(selectedStudent);
                        showAlert("Student name updated successfully!");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Failed to update student name in database.");
                    }

            });
        });

        changeCourseButton.setOnAction(ev -> {
            try {
                // Mevcut kurslar için TableView oluştur
                TableView<Course> enrolledTableView = new TableView<>();
                enrolledTableView.setPlaceholder(new Label("No enrolled courses"));

                TableColumn<Course, String> enrolledNameColumn = new TableColumn<>("Course Name");
                enrolledNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

                TableColumn<Course, String> enrolledDayColumn = new TableColumn<>("Day");
                enrolledDayColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDay()));

                TableColumn<Course, String> enrolledTimeColumn = new TableColumn<>("Time");
                enrolledTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimeToStart()));

                TableColumn<Course, String> enrolledDurationColumn = new TableColumn<>("Duration");
                enrolledDurationColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDuration())));

                enrolledTableView.getColumns().addAll(enrolledNameColumn, enrolledDayColumn, enrolledTimeColumn, enrolledDurationColumn);
                enrolledTableView.getItems().addAll(selectedStudent.getEnrolledCourses());

                // Mevcut olmayan kurslar için TableView oluştur
                TableView<Course> availableTableView = new TableView<>();
                availableTableView.setPlaceholder(new Label("No available courses"));

                TableColumn<Course, String> availableNameColumn = new TableColumn<>("Course Name");
                availableNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

                TableColumn<Course, String> availableDayColumn = new TableColumn<>("Day");
                availableDayColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDay()));

                TableColumn<Course, String> availableTimeColumn = new TableColumn<>("Time");
                availableTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimeToStart()));

                TableColumn<Course, String> availableDurationColumn = new TableColumn<>("Duration");
                availableDurationColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDuration())));

                availableTableView.getColumns().addAll(availableNameColumn, availableDayColumn, availableTimeColumn, availableDurationColumn);

                // Sadece öğrencinin almadığı dersleri ekle
                List<Course> availableCourses = courses.stream()
                        .filter(course -> !selectedStudent.getEnrolledCourses().contains(course))
                        .collect(Collectors.toList());
                availableTableView.getItems().addAll(availableCourses);

                // Layout düzenlemesi
                VBox enrolledBox = new VBox(10, new Label("Enrolled Courses:"), enrolledTableView);
                VBox availableBox = new VBox(10, new Label("Available Courses:"), availableTableView);
                HBox tableLayout = new HBox(20, enrolledBox, availableBox);

                Button confirmButton = new Button("Confirm");
                Button cancelButton = new Button("Cancel");
                HBox buttonBox = new HBox(10, confirmButton, cancelButton);
                buttonBox.setAlignment(Pos.CENTER);

                VBox layout = new VBox(10, new Label("Select a course to remove and a course to add:"), tableLayout, buttonBox);
                layout.setPadding(new Insets(10));

                Scene changeCourseScene = new Scene(layout);
                Stage changeCourseStage = new Stage();
                changeCourseStage.setScene(changeCourseScene);
                changeCourseStage.setTitle("Change Course for " + selectedStudent.getFullName());
                changeCourseStage.show();

                // Confirm butonu işlevi
                confirmButton.setOnAction(eve -> {
                    Course oldCourse = enrolledTableView.getSelectionModel().getSelectedItem();
                    Course newCourse = availableTableView.getSelectionModel().getSelectedItem();

                    if (oldCourse == null || newCourse == null) {
                        showAlert("Please select both a course to remove and a course to add.");
                        return;
                    }

                    if (selectedStudent.getEnrolledCourses().stream()
                            .filter(course -> !course.equals(oldCourse)) // Kaldırılacak kurs hariç
                            .anyMatch(course -> course.isTimeConflict(newCourse))) {
                        showAlert("The new course conflicts with the student's schedule.");
                        return;
                    }

                    try {
                        // Veritabanında güncelle
                        databaseLoader.changeStudentCourse(selectedStudent.getFullName(), oldCourse.getName(), newCourse.getName());

                        // Bellekte güncelle
                        selectedStudent.getEnrolledCourses().remove(oldCourse);
                        oldCourse.getStudents().remove(selectedStudent);
                        selectedStudent.getEnrolledCourses().add(newCourse);
                        newCourse.getStudents().add(selectedStudent);
                        //Sınıf kontrolü
                        newCourse.checkAndReassignClassroom(databaseLoader, classrooms);
                        editStage.close();
                        openStudentEditStage(selectedStudent);


                        showAlert("Course changed successfully!");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Failed to change course in database.");
                    }
                    changeCourseStage.close();
                });

                // Cancel butonu işlevi
                cancelButton.setOnAction(eve ->{
                if(changeCourseStage.isShowing()){
                    changeCourseStage.close();
                }
            });


            }catch (Exception e){
                showAlert("An unexpected error occurred. Please try again");
            }
        });

        assignCourseButton.setOnAction(ev -> {
            try{

            TableView<Course> availableTableView = new TableView<>();
            availableTableView.setPlaceholder(new Label("No available courses"));

            TableColumn<Course, String> availableNameColumn = new TableColumn<>("Course Name");
            availableNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

            TableColumn<Course, String> availableDayColumn = new TableColumn<>("Day");
            availableDayColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDay()));

            TableColumn<Course, String> availableTimeColumn = new TableColumn<>("Time");
            availableTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimeToStart()));

            TableColumn<Course, String> availableDurationColumn = new TableColumn<>("Duration");
            availableDurationColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDuration())));

            availableTableView.getColumns().addAll(availableNameColumn, availableDayColumn, availableTimeColumn, availableDurationColumn);

            // Sadece öğrencinin almadığı dersleri ekle
            List<Course> availableCourses = courses.stream()
                    .filter(course -> !selectedStudent.getEnrolledCourses().contains(course))
                    .collect(Collectors.toList());
            availableTableView.getItems().addAll(availableCourses);

            // Layout düzenlemesi
            VBox availableBox = new VBox(10, new Label("Available Courses:"), availableTableView);
            Button confirmButton = new Button("Confirm");
            Button cancelButton = new Button("Cancel");
            HBox buttonBox = new HBox(10, confirmButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER);

            VBox layout = new VBox(10, new Label("Select a course to assign:"), availableBox, buttonBox);
            layout.setPadding(new Insets(10));

            Scene assignCourseScene = new Scene(layout);
            Stage assignCourseStage = new Stage();
            assignCourseStage.setScene(assignCourseScene);
            assignCourseStage.setTitle("Assign Course to " + selectedStudent.getFullName());
            assignCourseStage.show();

            // Confirm butonu işlevi
            confirmButton.setOnAction(eve -> {
                Course newCourse = availableTableView.getSelectionModel().getSelectedItem();

                if (newCourse == null) {
                    showAlert("Please select a course to assign.");
                    return;
                }


                    // Yeni kursun zaman çakışmasını kontrol et
                    if (selectedStudent.getEnrolledCourses().stream().anyMatch(course -> course.isTimeConflict(newCourse))) {
                        showAlert("The new course conflicts with the student's schedule.");
                        return;
                    }

                try {
                    // Veritabanında güncelle
                    databaseLoader.assignStudentToCourse(selectedStudent.getFullName(), newCourse.getName());
                    // Bellekte güncelle
                    selectedStudent.getEnrolledCourses().add(newCourse);
                    newCourse.getStudents().add(selectedStudent);
                    //Sınıf kontrolü
                    newCourse.checkAndReassignClassroom(databaseLoader, classrooms);
                    // Edit sayfasını yeniden başlat
                    editStage.close();
                    openStudentEditStage(selectedStudent);

                    showAlert("Course assigned successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Failed to assign course in database.");
                }
                assignCourseStage.close();
            });

            // Cancel butonu işlevi
            cancelButton.setOnAction(eve ->{
                if(assignCourseStage.isShowing()){
                    assignCourseStage.close();
                }
            });
        }catch (Exception e){
            showAlert("An unexpected error occurred. Please try again");
        }
        });

        HBox buttonBox = new HBox(10, renameButton, changeCourseButton, assignCourseButton);
        buttonBox.setAlignment(Pos.CENTER);

        editBox.getChildren().addAll(programLabel, scheduleBox, buttonBox);
    }
    private void showTeacherSchedule(Stage previousStage, String courseName, Teacher teacher) {
        previousStage.close();

        Stage scheduleStage = new Stage();
        VBox scheduleBox = new VBox(10);
        scheduleBox.setPadding(new Insets(10));

        Label teacherLabel = new Label("Schedule for " + teacher.getFullName());

        // Yeni bir VBox oluştur ve mevcut metodu çağır
        VBox scheduleTableBox = new VBox(); // Her çağrıda yeni VBox
        createScheduleTable(FXCollections.observableArrayList(teacher.getAssignedCourses()), scheduleTableBox, scheduleStage, new Scene(new VBox()));

        // Gün/Saat/Süre Seçimi
        ComboBox<String> dayComboBox = new ComboBox<>();
        dayComboBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        dayComboBox.setPromptText("Select Day");

        ComboBox<String> timeComboBox = new ComboBox<>();
        timeComboBox.getItems().addAll("8:30", "9:25", "10:20", "11:15", "12:10", "13:05", "14:00", "14:55", "15:50", "16:45", "17:40", "18:35");
        timeComboBox.setPromptText("Select Start Time");

        Spinner<Integer> durationSpinner = new Spinner<>(1, 4, 1); // Minimum 1, Maximum 4
        durationSpinner.setEditable(true);

        Button proceedButton = new Button("Proceed");

        scheduleBox.getChildren().addAll(
                teacherLabel, scheduleTableBox,
                new Label("Day:"), dayComboBox,
                new Label("Start Time:"), timeComboBox,
                new Label("Duration (hours):"), durationSpinner,
                proceedButton
        );

        Scene scheduleScene = new Scene(scheduleBox, 500, 400);
        scheduleStage.setScene(scheduleScene);
        scheduleStage.setTitle("Select Schedule for Course");
        scheduleStage.show();

        // Proceed Button Action
        proceedButton.setOnAction(ev -> {
            String day = dayComboBox.getValue();
            String startTime = timeComboBox.getValue();
            int duration = durationSpinner.getValue();

            if (day == null || startTime == null) {
                showAlert("Please select day and start time.");
                return;
            }
            String timeToStart = day + " " + startTime;
            try {
                Course tempCourse = new Course(courseName, timeToStart, duration, teacher.getFullName(), "");
                boolean hasConflict = teacher.getAssignedCourses().stream().anyMatch(tempCourse::isTimeConflict);

                if (hasConflict) {
                    showAlert("The new schedule conflicts with the teacher's program.");
                    return;
                }
            } catch (IllegalArgumentException e) {
                showAlert(e.getMessage());
                return;
            }
                showStudentSelection(scheduleStage, courseName, timeToStart, duration, teacher);

        });
    }
    private void showStudentSelection(Stage previousStage, String courseName, String timeToStart, int duration, Teacher teacher) {
        previousStage.close();

        Stage studentSelectionStage = new Stage();
        VBox selectionBox = new VBox(10);
        selectionBox.setPadding(new Insets(10));

        // Toplam ve seçilen öğrenci sayısını gösteren sayaçlar
        Label totalStudentCountLabel = new Label();
        Label selectedStudentCountLabel = new Label("Selected Students: 0");

        ComboBox<Student> studentComboBox = new ComboBox<>();
        studentComboBox.setPromptText("Select a student");

        // Toplam öğrencileri ComboBox'a ekle
        List<Student> availableStudents = students.stream()
                .filter(student -> student.getEnrolledCourses().stream().noneMatch(course ->
                        course.isTimeConflict(new Course(courseName, timeToStart, duration, teacher.getFullName(), ""))
                ))
                .toList();

        totalStudentCountLabel.setText("Total Students: " + availableStudents.size());
        studentComboBox.getItems().addAll(availableStudents);

        ListView<Student> selectedStudentListView = new ListView<>();
        ObservableList<Student> selectedStudents = FXCollections.observableArrayList();
        selectedStudentListView.setItems(selectedStudents);

        Button addButton = new Button("Add");
        Button confirmButton = new Button("Confirm");
        Button cancelButton = new Button("Cancel");

        HBox buttonBox = new HBox(10, confirmButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        addButton.setOnAction(ev -> {
            Student selectedStudent = studentComboBox.getSelectionModel().getSelectedItem();
            if (selectedStudent != null && !selectedStudents.contains(selectedStudent)) {
                selectedStudents.add(selectedStudent);
                selectedStudentCountLabel.setText("Selected Students: " + selectedStudents.size());
            } else {
                showAlert("Please select a valid student.");
            }
        });

        confirmButton.setOnAction(ev -> {
            if (selectedStudents.isEmpty()) {
                showAlert("Please select at least one student.");
                return;
            }

            // Show Classroom Selection Screen
            showClassroomSelection(studentSelectionStage, courseName, timeToStart, duration, teacher, selectedStudents);
        });

        cancelButton.setOnAction(ev -> studentSelectionStage.close());

        selectionBox.getChildren().addAll(
                totalStudentCountLabel,
                selectedStudentCountLabel,
                new Label("Select Students for the Course:"),
                studentComboBox,
                addButton,
                new Label("Selected Students:"),
                selectedStudentListView,
                buttonBox
        );

        Scene studentSelectionScene = new Scene(selectionBox, 400, 400);
        studentSelectionStage.setScene(studentSelectionScene);
        studentSelectionStage.setTitle("Select Students for Course");
        studentSelectionStage.show();
    }


    private void showClassroomSelection(Stage previousStage, String courseName, String timeToStart, int duration, Teacher teacher, List<Student> selectedStudents) {
        previousStage.close();
        Stage classroomSelectionStage = new Stage();
        VBox selectionBox = new VBox(10);
        selectionBox.setPadding(new Insets(10));

        // Toplam öğrenci sayısını göster
        Label studentCountLabel = new Label("Total Students Selected: " + selectedStudents.size());

        // Sınıf seçim listesi
        ListView<Classroom> classroomList = new ListView<>();
        classroomList.getItems().addAll(classrooms.stream().filter(classroom -> {
                            // Kapasite kontrolü
                            if (classroom.getCapacity() < selectedStudents.size()) {
                                return false;
                            }
                            // Zaman çakışması kontrolü
                            return classroom.getAssignedCourses().stream().noneMatch(course ->
                                    new Course(courseName, timeToStart, duration, teacher.getFullName(), "").isTimeConflict(course)
                            );
                        })
                        .toArray(Classroom[]::new)
        );

        classroomList.setPlaceholder(new Label("No available classrooms."));
        classroomList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Classroom classroom, boolean empty) {
                super.updateItem(classroom, empty);
                if (empty || classroom == null) {
                    setText(null);
                } else {
                    setText(classroom.getName() + " (Capacity: " + classroom.getCapacity() + ")");
                }
            }
        });

        Button confirmButton = new Button("Confirm");
        Button cancelButton = new Button("Cancel");

        HBox buttonBox = new HBox(10, confirmButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        selectionBox.getChildren().addAll(
                new Label("Select a Classroom for the Course:"),
                studentCountLabel,
                classroomList,
                buttonBox
        );
        Scene classroomSelectionScene = new Scene(selectionBox, 400, 300);
        classroomSelectionStage.setScene(classroomSelectionScene);
        classroomSelectionStage.setTitle("Select Classroom");
        classroomSelectionStage.show();

        // Confirm Button Action
        confirmButton.setOnAction(ev -> {
            Classroom selectedClassroom = classroomList.getSelectionModel().getSelectedItem();
            if (selectedClassroom == null) {
                showAlert("Please select a classroom.");
                return;
            }
            // Course ekleme işlemi
            Course newCourse = new Course(courseName, timeToStart, duration, teacher.getFullName(), "");
            newCourse.setClassroom(selectedClassroom);
            for (Student student : selectedStudents) {
                student.enrollCourse(newCourse);
                newCourse.getStudents().add(student);
            }
            selectedClassroom.getAssignedCourses().add(newCourse);
            courses.add(newCourse);
            databaseLoader.addCourse(newCourse);

            // Now the assignedCourses of teacher updating, problem solved.
            teacher.assignCourse(newCourse);

            // Kaydedilen bilgileri göster
            showAlert("Course added successfully!\n" +
                    "Classroom: " + selectedClassroom.getName() + "\n" +
                    "Students: " + selectedStudents.size());
            classroomSelectionStage.close();
        });

        // Cancel Button Action
        cancelButton.setOnAction(ev -> classroomSelectionStage.close());
    }
  public void refreshClassrooms() {
        try {
            // Clear assigned courses for all classrooms
            for (Classroom classroom : classrooms) {
                if (classroom.getAssignedCourses() != null) {
                    classroom.getAssignedCourses().clear(); // Clear assigned courses
                }
            }

            // Reassign courses to classrooms based on current data
            for (Course course : courses) {
                Classroom classroom = course.getClassroom();
                if (classroom != null) {
                    classroom.getAssignedCourses().add(course);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {

        databaseLoader.start();

        courses = databaseLoader.getCourses();
        teachers = databaseLoader.getTeachers();
        students = databaseLoader.getStudents();
        classrooms = databaseLoader.getClassrooms();

        courseManager =new CourseManager(courses, teachers, students, classrooms);
        courseManager.allocateClassrooms(databaseLoader);

        launch(args);
    }
}
