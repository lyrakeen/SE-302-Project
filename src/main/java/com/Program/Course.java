package com.Program;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Course {
    private String name;
    private String timeToStart;
    private int duration;
    private String lecturer;
    private List<Student> students;
    private String day;
    private String startTime;
    private String endTime;
    private Classroom classroom;
    private List<Teacher> teachers; // silinecek

    public Course(String name, String timeToStart, int duration, String lecturer, String studentsCsv) {
        this.name = name;
        this.timeToStart = timeToStart;
        this.duration = duration;
        this.lecturer = lecturer;
        this.students = parseStudents(studentsCsv);
        this.day = parseDay(timeToStart);
        this.startTime = parseStartTime(timeToStart);
        this.endTime = calculateEndTime(startTime, duration);
        this.teachers = new ArrayList<>();
    }

    private List<Student> parseStudents(String studentsCsv) {
        List<Student> studentList = new ArrayList<>();
        if (studentsCsv != null && !studentsCsv.isEmpty()) {
            String[] studentNames = studentsCsv.split(",\s*");
            for (String fullName : studentNames) {
                Student student = new Student(fullName);

                // Öğrencinin enrolledCourses listesine bu kursu ekle
                student.enrollCourse(this);
                studentList.add(student);
            }
        }
        return studentList;
    }

    private String parseDay(String timeToStart) {
        String[] parts = timeToStart.split(" "); // Example: "Monday 08:30"
        return parts.length > 0 ? parts[0] : "";
    }

    private String parseStartTime(String timeToStart) {
        String[] parts = timeToStart.split(" "); // Example: "Monday 08:30"
        return parts.length > 1 ? parts[1] : "";
    }

    private String calculateEndTime(String startTime, int durationInHours) {
        String[] starts = {"8:30","9:25","10:20","11:15","12:10","13:05", "14:00", "14:55", "15:50","16:45", "17:40", "18:35"};
        String[] ends = {"9:15","10:10","11:05","12:00","12:55","13:50","14:45","15:40","16:35","17:30","18:25","19:20" };
        int index = -1;
        // Başlangıç saatini bul
        for (int i = 0; i < starts.length; i++) {
            if (starts[i].equals(startTime)) {
                index = i;
                break;
            }
        }
        if (index == -1) {throw new IllegalArgumentException("Invalid start time: " + startTime);}
        // Bitiş saatini hesapla
        index += durationInHours - 1;
        if (index >= ends.length) {throw new IllegalArgumentException("Invalid duration: End time exceeds schedule limits.");}
        return ends[index];
    }


    public boolean isTimeConflict(Course otherCourse) {
        // Gün kontrolü
        if (!this.getDay().equalsIgnoreCase(otherCourse.getDay())) {
            return false; // Günler farklı
        }
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm"); // Tek haneli saatler için
        try {
            // Zamanları LocalTime olarak hesapla
            LocalTime thisStartTime = LocalTime.parse(this.getStartTime(), timeFormatter);
            LocalTime thisEndTime = LocalTime.parse(this.getEndTime(), timeFormatter);
            LocalTime otherStartTime = LocalTime.parse(otherCourse.getStartTime(), timeFormatter);
            LocalTime otherEndTime = LocalTime.parse(otherCourse.getEndTime(), timeFormatter);
            // Zaman çakışması kontrolü
            return !(thisEndTime.isBefore(otherStartTime) || thisStartTime.isAfter(otherEndTime));
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing time: " + e.getMessage());
            return true; // Hata durumunda varsayılan olarak çakışma olduğunu kabul et
        }
    }
    public boolean checkAndReassignClassroom(DatabaseLoader databaseLoader, List<Classroom> availableClassrooms) {
        if (this.getClassroom() == null || this.getClassroom().getCapacity() >= this.getStudents().size()) {
            return false; // No need to change classroom
        }

        // Find a suitable classroom
        Classroom newClassroom = availableClassrooms.stream()
                .filter(classroom -> classroom.getCapacity() >= this.getStudents().size())
                .filter(classroom -> classroom.getAssignedCourses().stream()
                        .noneMatch(course -> this.isTimeConflict(course)))
                .findFirst()
                .orElse(null);

        if (newClassroom != null) {
            // Update old Classroom in memory
            if (this.getClassroom() != null) {
                this.getClassroom().getAssignedCourses().remove(this);
            }

            // Update new Classroom in memory
            newClassroom.getAssignedCourses().add(this);
            this.setClassroom(newClassroom);

            // Update database
            databaseLoader.updateCourseClassroom(this.getName(), newClassroom.getName());

            // Notify user in GUI
            Platform.runLater(() -> showAlert("Classroom reassigned to: " + newClassroom.getName()));
        } else {
            Platform.runLater(() -> showAlert("No suitable classroom found for course: " + this.getName()));
        }
        return true;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeToStart() {
        return timeToStart;
    }

    public void setTimeToStart(String timeToStart) {
        this.timeToStart = timeToStart;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public List<Student> getStudents() {
        return students;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
    }

    public void removeTeacher(Teacher teacher) {
        teachers.remove(teacher);
    }

    public String getDay() {
        return day;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}