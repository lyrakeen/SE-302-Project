package com.Program;

import java.util.ArrayList;
import java.util.List;
public class Classroom {
    private String name;
    private int capacity;
    private List<Course> schedule;

    public Classroom(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.schedule = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<Course> getSchedule() {
        return schedule;
    }

    public boolean isConflicting(Course course) {
        for (Course scheduledCourse : schedule) {
            if (scheduledCourse.getDay().equals(course.getDay()) &&
                    !(scheduledCourse.getEndTime().compareTo(course.getStartTime()) <= 0 ||
                            scheduledCourse.getStartTime().compareTo(course.getEndTime()) >= 0)) {
                return true;
            }
        }
        return false;
    }

    public void addCourse(Course course) {
        schedule.add(course);
    }
}
