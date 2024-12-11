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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return fullName.equals(student.fullName); // Eşitlik tam isimlere göre belirlenir
    }

    @Override
    public int hashCode() {
        return fullName.hashCode(); // Benzersizlik tam isimlere göre belirlenir
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