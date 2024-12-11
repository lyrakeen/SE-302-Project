package com.Program;
import java.util.List;

public class CourseManager {
    private List<Course> courses;
    private List<Teacher> teachers;
    private List<Student> students;
    private List<Classroom> classrooms;

    public CourseManager(List<Course> courses, List<Teacher> teachers, List<Student> students, List<Classroom> classrooms) {
        this.courses = courses;
        this.teachers = teachers;
        this.students = students;
        this.classrooms = classrooms;
    }

    public void allocateClassrooms() {
        for (Course course : courses) {
            for (Classroom classroom : classrooms) {
                if (classroom.getCapacity() >= course.getStudents().size() && !classroom.isConflicting(course)) {
                    classroom.addCourse(course);
                    course.setClassroom(classroom);
                    break;
                }
            }
        }
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    public void editCourse(Course oldCourse, Course newCourse) {
        int index = courses.indexOf(oldCourse);
        if (index >= 0) {
            courses.set(index, newCourse);
        }
    }

    public List<Course> getCourses() {
        return courses;
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public List<Student> getStudents() {
        return students;
    }

    public List<Classroom> getClassrooms() {
        return classrooms;
    }
}