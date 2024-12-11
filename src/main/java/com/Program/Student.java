package com.Program;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private String fullName;
    private List<Course> enrolledCourses;

    public Student(String fullName) {
        this.fullName = fullName;
        this.enrolledCourses = new ArrayList<>();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void enrollCourse(Course course) {
        enrolledCourses.add(course);
    }

    public void dropCourse(Course course) {
        enrolledCourses.remove(course);
    }
}