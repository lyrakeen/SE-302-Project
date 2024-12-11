package com.Program;
import java.util.List;
import java.util.Set;

public class Main {
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

    }
}
