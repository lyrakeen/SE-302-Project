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

                String[] schParts = scheduledCourse.getEndTime().split(":"); //scheduled course parts
                int[] intSch = {Integer.parseInt(schParts[0]), Integer.parseInt(schParts[1])}; // scheduled course time parts converted to int
   
                String[] cParts = course.getStartTime().split(":"); // course parts
                int[] intCo = {Integer.parseInt(cParts[0]), Integer.parseInt(cParts[1])}; //  course time parts converted to int

                if (!(intCo[0] > intSch[0])) {
                    if (!(intCo[0] > intSch[0])){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addCourse(Course course) {
        schedule.add(course);
    }

    public List<Course> getAssignedCourses() {
        // `schedule` içeriğini `getAssignedCourses` adıyla döndürüyoruz.
        return new ArrayList<>(schedule);
    }
}
