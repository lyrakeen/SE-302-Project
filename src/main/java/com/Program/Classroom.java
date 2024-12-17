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
            if (scheduledCourse.getDay().equals(course.getDay())) {

                int[] scheduledStart = parseTime(scheduledCourse.getStartTime());
                int[] scheduledEnd = parseTime(scheduledCourse.getEndTime());

                int[] courseStart = parseTime(course.getStartTime());
                int[] courseEnd = parseTime(course.getEndTime());

                if (isTimeConflict(scheduledStart, scheduledEnd, courseStart, courseEnd)) {
                    return true; 
                }
            }
        }
        return false;
    }
    
    private int[] parseTime(String time) {
        String[] parts = time.split(":");
        return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) };
    }
    
    private boolean isTimeConflict(int[] start1, int[] end1, int[] start2, int[] end2) {
        return !(end1[0] < start2[0] || (end1[0] == start2[0] && end1[1] <= start2[1])) &&
               !(end2[0] < start1[0] || (end2[0] == start1[0] && end2[1] <= start1[1]));
    }

    public void addCourse(Course course) {
        schedule.add(course);
    }

    public List<Course> getAssignedCourses() {
        // `schedule` içeriğini `getAssignedCourses` adıyla döndürüyoruz.
        return new ArrayList<>(schedule);
    }
}
