package com.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ClassroomAllocation {

    public static void main(String[] args) {
        String coursesFilePath="Courses.csv";
        String classroomFilePath="ClassroomCapacity.csv";
        String sqliteUrl="jdbc:sqlite:university.db";

        try (Connection connection = DriverManager.getConnection(sqliteUrl)) {
             createTables(connection);
             List<String[]>coursesData = readCSV(coursesFilePath);
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

        String createAllocationTable ="CREATE TABLE IF NOT EXITS course_allocation (" +
                "course_name TEXT," +
                "classroom_name TEXT," +
                "time_to_start TEXT)";
        try(Statement statement = connection.createStatement()){
            statement.executeUpdate(createCoursesTable);
            statement.executeUpdate(createClassroomTable);
            statement.executeUpdate(createAllocationTable);
        }
}
}