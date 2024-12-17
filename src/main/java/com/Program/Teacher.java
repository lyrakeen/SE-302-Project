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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return fullName.equals(teacher.fullName); // Eşitlik tam isme göre belirlenir
    }

    @Override
    public int hashCode() {
        return fullName.hashCode(); // Benzersizlik tam isme göre belirlenir
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
    @Override
    public String toString() {
        return this.fullName;
    }

}