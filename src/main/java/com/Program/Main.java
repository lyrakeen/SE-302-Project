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
        System.out.println("\nTeachers and their courses:");
        for (Teacher teacher : databaseLoader.getTeachers()) {
            System.out.println("Teacher: " + teacher.getFullName());
            for (Course course : teacher.getAssignedCourses()) {
                System.out.println("  Course: " + course.getName());
            }
        }

        System.out.println("\nStudents and their courses:");
        for (Student student : databaseLoader.getStudents()) {
            System.out.println("Student: " + student.getFullName());
            for (Course course : student.getEnrolledCourses()) {
                System.out.println("  Course: " + course.getName());
            }
        }

    }
}
