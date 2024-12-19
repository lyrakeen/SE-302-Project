package com.Program;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.control.*;
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

public class Main extends Application {

    static List<Course> courses;
    static Set<Teacher> teachers;
    static Set<Student> students;
    static List<Classroom> classrooms;
    static DatabaseLoader databaseLoader = new DatabaseLoader();
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

        courseItem.setOnAction(e -> {managingCourses();});
        teacherItem.setOnAction(e -> managingTeachers());
        studentItem.setOnAction(e -> managingStudents());

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
                "-fx-font-family: 'Caveat'; "+
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

        manualItem.setOnAction(e -> {showManual();
        });

        table.setOnMouseClicked(event -> {
            displayInfo(table.getSelectionModel().getSelectedItem());
        });

        firstStage.setTitle("Sketchuler");
        firstStage.setScene(scene);
        firstStage.show();
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

        // Add Course Button
        addCourseButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Course");
            dialog.setHeaderText("Enter new course name");
            dialog.setContentText("Course Name:");

            dialog.showAndWait().ifPresent(courseName -> {
                if (!courseName.trim().isEmpty()) {
                    Course newCourse = new Course(courseName, "08:30", 1, "Unknown", ""); // Default Course Data
                    courses.add(newCourse);
                    courseLists.getItems().add(newCourse);
                    showAlert("Course added successfully!");
                } else {
                    showAlert("Course name cannot be empty!");
                }
            });
        });

        // Edit Course Button
        editC.setOnAction(e -> {
            ObservableList<Course> selectedCourses = courseLists.getSelectionModel().getSelectedItems();
            if (selectedCourses.size() > 1) {
                showAlert("You can only edit 1 course at once!");
                return;
            }
            Course selectedCourse = courseLists.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                TextInputDialog dialog = new TextInputDialog(selectedCourse.getName());
                dialog.setTitle("Edit Course");
                dialog.setHeaderText("Edit course name");
                dialog.setContentText("New Name:");

                dialog.showAndWait().ifPresent(newName -> {
                    if (!newName.trim().isEmpty()) {
                        selectedCourse.setName(newName);
                        courseLists.refresh();
                        showAlert("Course edited successfully!");
                    } else {
                        showAlert("Course name cannot be empty!");
                    }
                });
            } else {
                showAlert("Please select a course to edit.");
            }
        });


        courseLists.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        // Delete Course Button
        deleteC.setOnAction(e -> {
            ObservableList<Course> selectedCourses = courseLists.getSelectionModel().getSelectedItems();
            if (!selectedCourses.isEmpty()) {
                for(Course c : selectedCourses){
                    try {
                        databaseLoader.deleteCourse(c.getName());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
               // courses.removeAll(selectedCourses); //Bunu zaten databaseLoader'ın deleteCourse methodu yapıyor.
                courseLists.getItems().removeAll(selectedCourses);
                showAlert("Selected courses deleted successfully!");
            } else {
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
                                studentLists.getItems().add(new Student(studentName));
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
            ObservableList<Student> selectedStudents = studentLists.getSelectionModel().getSelectedItems();
            if (selectedStudents.size() > 1) {
                showAlert("You can only edit 1 student at once!");
                return;
            }
            Student selectedStudent = studentLists.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                TextInputDialog dialog = new TextInputDialog(selectedStudent.getFullName());
                dialog.setTitle("Edit Student");
                dialog.setHeaderText("Edit student name");
                dialog.setContentText("New Name:");

                dialog.showAndWait().ifPresent(newName -> {
                    if (!newName.trim().isEmpty()) {
                        selectedStudent.setFullName(newName);
                        studentLists.refresh();
                        showAlert("Student edited successfully!");
                    }
                });
            } else {
                showAlert("Please select a student to edit.");
            }
        });


        studentLists.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        deleteS.setOnAction(e -> {
            ObservableList<Student> selectedStudents = studentLists.getSelectionModel().getSelectedItems();
            if (!selectedStudents.isEmpty()) {
                for(Student s : selectedStudents){
                    try {
                        databaseLoader.deleteStudent(s.getFullName());
                    }catch(SQLException ex){
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

        Button editT = new Button("Edit");
        Button deleteT = new Button("Delete");

        forButtons.getChildren().addAll(addTeacherButton, editT, deleteT);

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

        editT.setOnAction(e -> {
            ObservableList<Teacher> selectedTeachers = teacherLists.getSelectionModel().getSelectedItems();
            if (selectedTeachers.size() > 1) {
                showAlert("You can only edit 1 teacher at once!");
                return;
            }
            Teacher selectedTeacher = teacherLists.getSelectionModel().getSelectedItem();
            if (selectedTeacher != null) {
                TextInputDialog dialog = new TextInputDialog(selectedTeacher.getFullName());
                dialog.setTitle("Edit Teacher");
                dialog.setHeaderText("Edit teacher name");
                dialog.setContentText("New Name:");

                dialog.showAndWait().ifPresent(newName -> {
                    if (!newName.trim().isEmpty()) {
                        selectedTeacher.setFullName(newName);
                        teacherLists.refresh();
                        showAlert("Teacher edited successfully!");
                    }
                });
            } else {
                showAlert("Please select a teacher to edit.");
            }
        });

        teacherLists.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        deleteT.setOnAction(e -> {
            ObservableList<Teacher> selectedTeachers = teacherLists.getSelectionModel().getSelectedItems();
            if (!selectedTeachers.isEmpty()) {
                teachers.removeAll(selectedTeachers);
                teacherLists.getItems().removeAll(selectedTeachers);
                showAlert("Selected teachers deleted successfully!");
            } else {
                showAlert("Please select at least one teacher to delete.");
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
            } catch (Exception e){}

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
            sixth.getChildren().addAll(studentCountLabel,studentCount);
            allIn.getChildren().addAll(sixth, fifth, fourth, third, second, first);
            nextBox.getChildren().addAll(allIn, studentList);
            infoRoot.getChildren().addAll(nextBox);
            infoStage.setScene(infoScene);
            infoStage.show();
        } if (selected instanceof Student) {
            infoRoot.getChildren().clear();
            Student student = (Student) selected;
            createScheduleTable(FXCollections.observableArrayList(student.getEnrolledCourses()), infoRoot, infoStage, infoScene);
            Label forStudent = new Label(student.getFullName() + "'s Weekly Program");
            infoRoot.getChildren().add(0,forStudent); // BURAYA TASARIM YAPILACAK 
        } if (selected instanceof Teacher) {
            infoRoot.getChildren().clear();
            Teacher teacher = (Teacher) selected;
            createScheduleTable(FXCollections.observableArrayList(teacher.getAssignedCourses()), infoRoot, infoStage, infoScene);
            Label forTeacher = new Label(teacher.getFullName() + "'s Weekly Program");
            infoRoot.getChildren().add(0,forTeacher); // BURAYA TASARIM YAPILACAK 
        } if (selected instanceof Classroom) {
            infoRoot.getChildren().clear();
            Classroom classroom = (Classroom) selected;
            createScheduleTable(FXCollections.observableArrayList(classroom.getAssignedCourses()), infoRoot, infoStage, infoScene);
            Label forClass = new Label(classroom.getName() + "'s Weekly Program  Capacity : " + classroom.getCapacity());
            infoRoot.getChildren().add(0,forClass); // BURAYA TASARIM YAPILACAK 
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

    public static void main(String[] args) {

        databaseLoader.start();

        courses = databaseLoader.getCourses();
        teachers = databaseLoader.getTeachers();
        students = databaseLoader.getStudents();
        classrooms = databaseLoader.getClassrooms();

        CourseManager courseManager = new CourseManager(courses, teachers, students, classrooms);
        courseManager.allocateClassrooms(databaseLoader);

        launch(args);
    }
}
