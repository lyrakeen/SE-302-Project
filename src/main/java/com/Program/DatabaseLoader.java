package com.Program;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DatabaseLoader {
    private List<Course> courses = new ArrayList<>();
    private Set<Student> students = new HashSet<>();
    private Set<Teacher> teachers = new HashSet<>();

    private List<Classroom> classrooms = new ArrayList<>();
    public  void start() {
        String coursesFilePath="Courses.csv";
        String classroomFilePath="ClassroomCapacity.csv";
        String sqliteUrl="jdbc:sqlite:university.db";

        try (Connection connection = DriverManager.getConnection(sqliteUrl)) {
            //Tabloları Oluşturur
             createTables(connection);

             //CSV dosyalarını okuyup veri tabanına aktarır
            List<String[]>coursesData = readCSV(coursesFilePath);
             insertCoursesData(connection,coursesData);
             List<String[]>classroomData = readCSV(classroomFilePath);
             insertClassroomData(connection, classroomData);


            // 3. Veritabanından verileri yükle ve model sınıflarına aktar
            loadCourses(connection);
            loadClassrooms(connection);

            // Öğrencileri ve öğretmenleri doldur
            loadStudents();
            loadTeachers();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection connection) throws SQLException {

        String createCoursesTable = "CREATE TABLE IF NOT EXISTS courses (" +
                "course_name TEXT," +
                "time_to_start TEXT," +
                "duration_in_lecture_hours INTEGER," +
                "lecturer TEXT," +
                "students TEXT," +
                "student_count INTEGER)";

        String createClassroomTable = "CREATE TABLE IF NOT EXISTS classroom_capacity (" +
                "classroom_name TEXT," +
                "capacity INTEGER)";


        try(Statement statement = connection.createStatement()){
            statement.executeUpdate(createCoursesTable);
            statement.executeUpdate(createClassroomTable);
        }
}

    private static List<String[]>readCSV(String filePath){
     List<String[]> data = new ArrayList<>();
     try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
         String line;
         br.readLine();//Skip the first(header)line
         while((line=br.readLine())!=null){
             data.add(line.split(";"));
         }
     }catch (IOException e){
         throw new RuntimeException("Error reading file: " + filePath, e);
     }
     return data;
    }

    private static void insertCoursesData(Connection connection, List<String[]> coursesData) throws SQLException{
        String checkSQL = "SELECT COUNT(*) FROM courses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSQL)) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Courses table already populated. Skipping insertion.");
                return; // Eğer tablo doluysa veri ekleme
            }
        }
        String insertSQL = "INSERT INTO courses (course_name, time_to_start, duration_in_lecture_hours, lecturer, students, student_count) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
        for(String[] row: coursesData){
            String students = String.join(",", Arrays.copyOfRange(row,4,row.length));
            preparedStatement.setString(1,row[0]);//course name
            preparedStatement.setString(2,row[1]);// time to start
            preparedStatement.setInt(3, Integer.parseInt(row[2])); //duration
            preparedStatement.setString(4,row[3]);//lecturer
            preparedStatement.setString(5,students);//student names
            preparedStatement.setInt(6,row.length-4);//student count but I will update for the blank spaces later
            preparedStatement.executeUpdate();
        }
    }
}

    private  static void insertClassroomData(Connection connection, List<String[]> classroomData) throws  SQLException{
        String checkSQL = "SELECT COUNT(*) FROM classroom_capacity";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSQL)) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Classroom table already populated. Skipping insertion.");
                return; // Eğer tablo doluysa veri ekleme
            }
        }
        String insertSQL = "INSERT INTO classroom_capacity (classroom_name,capacity) VALUES (?,?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)){
            for(String[] row: classroomData){
                preparedStatement.setString(1,row[0]);//classroom Name
                preparedStatement.setInt(2,Integer.parseInt(row[1]));//capacity
                preparedStatement.executeUpdate();
            }
        }
    }

    private void loadCourses(Connection connection) throws SQLException {
        String fetchCoursesSQL = "SELECT * FROM courses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(fetchCoursesSQL)) {
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                String timeToStart = rs.getString("time_to_start");
                int duration = rs.getInt("duration_in_lecture_hours");
                String lecturer = rs.getString("lecturer");
                String students = rs.getString("students");

                // Duplicate kontrolü
                boolean alreadyExists = courses.stream()
                        .anyMatch(course -> course.getName().equals(courseName) &&
                                course.getTimeToStart().equals(timeToStart) &&
                                course.getLecturer().equals(lecturer));
                if (!alreadyExists) {
                    Course course = new Course(courseName, timeToStart, duration, lecturer, students);
                    courses.add(course);
                }
            }
        }
    }
    private void loadClassrooms(Connection connection) throws SQLException {
        String fetchClassroomsSQL = "SELECT * FROM classroom_capacity";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(fetchClassroomsSQL)) {
            while (rs.next()) {
                String classroomName = rs.getString("classroom_name");
                int capacity = rs.getInt("capacity");

                // Duplicate kontrolü
                boolean alreadyExists = classrooms.stream()
                        .anyMatch(classroom -> classroom.getName().equals(classroomName));
                if (!alreadyExists) {
                    Classroom classroom = new Classroom(classroomName, capacity);
                    classrooms.add(classroom);
                }
            }
        }
    }

    private void loadStudents() {
        for (Course course : courses) {
            for (Student student : course.getStudents()) {
                if (students.add(student)) { // Eğer yeni bir öğrenciyse ekle
                    student.enrollCourse(course);
                } else {
                    // Zaten varsa, öğrencinin kurs listesine ekle
                    for (Student existingStudent : students) {
                        if (existingStudent.equals(student)) {
                            existingStudent.enrollCourse(course);
                            break;
                        }
                    }
                }
            }
        }
    }
    private void loadTeachers() {
        for (Course course : courses) {
            Teacher teacher = new Teacher(course.getLecturer());
            if (teachers.add(teacher)) { // Eğer yeni bir öğretmense ekle
                teacher.assignCourse(course);
            } else {
                // Zaten varsa, öğretmenin kurs listesine ekle
                for (Teacher existingTeacher : teachers) {
                    if (existingTeacher.equals(teacher)) {
                        existingTeacher.assignCourse(course);
                        break;
                    }
                }
            }
        }
    }

    // Getters
    public List<Course> getCourses() { return courses; }
    public Set<Teacher> getTeachers() { return teachers; }
    public Set<Student> getStudents() { return students; }
    public List<Classroom> getClassrooms() { return classrooms; }
}