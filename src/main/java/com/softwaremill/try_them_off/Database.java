package com.softwaremill.try_them_off;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Database {

    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    void start() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
                        InvocationTargetException, InstantiationException {
        Class.forName(DRIVER).getDeclaredConstructor().newInstance();
        Try.withResources(
          () -> DriverManager.getConnection("jdbc:h2:mem:measurements;DB_CLOSE_DELAY=-1")
        )
           .of(this::createMeasurementsTable)
           .onFailure(exc -> log.error("Error creating table", exc))
           .get();
    }

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:measurements");
    }

    private String createMeasurementsTable(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();

        String sql = "CREATE TABLE IF NOT EXISTS MEASUREMENTS " +
                     "(id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY," +
                     " read_date TIMESTAMP, " +
                     " city VARCHAR(255), " +
                     " pm1 DECIMAL, " +
                     " pm25 DECIMAL, " +
                     " pm10 DECIMAL, " +
                     " pressure DECIMAL, " +
                     " humidity DECIMAL, " +
                     " temperature DECIMAL, " +
                     " PRIMARY KEY ( id ))";

        stmt.executeUpdate(sql);

        return "created";
    }

}
