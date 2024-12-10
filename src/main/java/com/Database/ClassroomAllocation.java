package com.Database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassroomAllocation {

    public static void main(String[] args) {
        String coursesFilePath="Courses.csv";
        String classroomFilePath="ClassroomCapacity.csv";
        String sqliteUrl="jdbc:sqlite:university.db";

        try (Connection connection = DriverManager.getConnection(sqliteUrl)) {
             createTables(connection);
             List<String[]>coursesData = readCSV(coursesFilePath);
             insertCoursesData(connection,coursesData);
             List<String[]>classroomData = readCSV(classroomFilePath);
             insertClassroomData(connection, classroomData);

             allocateClassrooms(connection);

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

        String createAllocationTable ="CREATE TABLE IF NOT EXISTS course_allocation (" +
                "course_name TEXT," +
                "classroom_name TEXT," +
                "time_to_start TEXT)";
        try(Statement statement = connection.createStatement()){
            statement.executeUpdate(createCoursesTable);
            statement.executeUpdate(createClassroomTable);
            statement.executeUpdate(createAllocationTable);
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
        String insertSQL = "INSERT INTO classroom_capacity (classroom_name,capacity) VALUES (?,?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)){
            for(String[] row: classroomData){
                preparedStatement.setString(1,row[0]);//classroom Name
                preparedStatement.setInt(2,Integer.parseInt(row[1]));//capacity
                preparedStatement.executeUpdate();
            }
        }
    }

 private static void allocateClassrooms(Connection connection) throws SQLException{
        String fetchCoursesSQL = "SELECT course_name, time_to_start, duration_in_lecture_hours, student_count FROM courses ORDER BY time_to_start";
        String fetchClassromSQL = "SELECT classrom_name, capacity FROM classroom_capcacity ORDER BY capacity DESC";

        List<String[]> allocations = new ArrayList<>();

        try(Statement statement = connection.createStatement();
        ResultSet courses = statement.executeQuery(fetchCoursesSQL)) {

        }
    }



}