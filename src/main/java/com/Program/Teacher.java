package com.Program;

import java.util.ArrayList;
import java.util.List;

public class Teacher {
    private String fullName;
    private List<Course> assignedCourses;

    public Teacher(String fullName) {
        this.fullName = fullName;
        this.assignedCourses = new ArrayList<>();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Course> getAssignedCourses() {
        return assignedCourses;
    }

    public void assignCourse(Course course) {
        assignedCourses.add(course);
    }

    public void removeCourse(Course course) {
        assignedCourses.remove(course);
    }
}