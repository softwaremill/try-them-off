package com.softwaremill.try_them_off;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class MeasurementsRepository {

  private static final String INSERT_STMT =
    "INSERT INTO MEASUREMENTS (READ_DATE, CITY, PM1, PM25, PM10, PRESSURE, HUMIDITY, TEMPERATURE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

  private final Database database;

  Try<Void> save(Measurements measurement) {
    return Try.run(() -> {
        try (final Connection connection = database.getConnection()) {
          insertData(measurement, connection);
        }
      }
    );
  }

  private void insertData(Measurements measurement, Connection connection)
    throws SQLException {
    try (final var preparedStatement = connection.prepareStatement(INSERT_STMT);) {
      preparedStatement.setTimestamp(1, Timestamp.from(measurement.timestamp()));
      preparedStatement.setString(2, measurement.city());
      preparedStatement.setDouble(3, measurement.pm1());
      preparedStatement.setDouble(4, measurement.pm25());
      preparedStatement.setDouble(5, measurement.pm10());
      preparedStatement.setDouble(6, measurement.pressure());
      preparedStatement.setDouble(7, measurement.humidity());
      preparedStatement.setDouble(8, measurement.temperature());
      preparedStatement.execute();
    }
  }

}
