package com.Program;
import java.util.List;
import java.util.Set;

public class CourseManager {
    private List<Course> courses;
    private Set<Teacher> teachers;
    private Set<Student> students;
    private List<Classroom> classrooms;

    public CourseManager(List<Course> courses, Set<Teacher> teachers, Set<Student> students, List<Classroom> classrooms) {
        this.courses = courses;
        this.teachers = teachers;
        this.students = students;
        this.classrooms = classrooms;
    }
    public void allocateClassrooms(DatabaseLoader databaseLoader) {
        for (Course course : courses) {
            Classroom assignedClassroom = null;
            int minCapacityDifference = Integer.MAX_VALUE;  

            for (Classroom classroom : classrooms) {
                if (classroom.getCapacity() >= course.getStudents().size() && !classroom.isConflicting(course)) {
                    int capacityDifference = classroom.getCapacity() - course.getStudents().size();
                    if (capacityDifference < minCapacityDifference) {
                        minCapacityDifference = capacityDifference;
                        assignedClassroom = classroom;
                    }
                }
            }
            if (assignedClassroom != null) {
                assignedClassroom.addCourse(course);
                course.setClassroom(assignedClassroom);
                databaseLoader.updateCourseClassroom(course.getName(), assignedClassroom.getName());
            } 
        }
    }

    public List<Course> getCourses() {
        return courses;
    }

    public Set<Teacher> getTeachers() {
        return teachers;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public List<Classroom> getClassrooms() {
        return classrooms;
    }
}