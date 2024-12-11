package com.Program;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Initialize the DatabaseLoader
        DatabaseLoader databaseLoader = new DatabaseLoader();
        databaseLoader.start();

        // Retrieve data from the loader
        List<Course> courses = databaseLoader.getCourses();
        List<Teacher> teachers = databaseLoader.getTeachers();
        List<Student> students = databaseLoader.getStudents();
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
        System.out.println("\nTeachers:");
        for (Teacher teacher : teachers) {
            System.out.println("Teacher: " + teacher.getFullName());
        }
        // Display each student's enrolled courses
        System.out.println("\nStudents and their enrolled courses:");
        for (Student student : students) {
            System.out.println("Student: " + student.getFullName());
            for (Course course : student.getEnrolledCourses()) {
                System.out.println("  Enrolled in: " + course.getName() + " on " + course.getDay() + " at " + course.getStartTime());
            }
        }
    }
}
