package com.softwaremill.try_them_off;

import java.io.FileNotFoundException;

import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Application {

  public static void main(String[] args) {
    final var database = new Database();
    final var geoCoordinatesReader = new GeoCoordinatesReader();
    final var measurementsRepository = new MeasurementsRepository(database);
    final var airlyService = new AirlyService();

    Try
      .run(database::start)
      .onSuccess(ignore -> log.info("Database started successfully"))
      .onFailure(exc -> log.error("Cannot start the database", exc))
      .andThen(
        () -> fetchMeasurements(geoCoordinatesReader, airlyService, measurementsRepository)
      );
  }

  private static void fetchMeasurements(GeoCoordinatesReader geoCoordinatesReader,
                                        AirlyService airlyService,
                                        MeasurementsRepository measurementsRepository) {
    readCoordinates(geoCoordinatesReader)
      .map(airlyService::getMeasurement)
      .map(result -> {
          result
            .onSuccess(m -> log.info("Measurements fetched: {}", m))
            .onFailure(exc -> log.error("Reading measurements failed", exc));
          return result;
        }
      )
      .flatMap(t -> t)
      .map(m -> save(measurementsRepository, m)
      );
  }

  private static List<GeoCoordinates> readCoordinates(GeoCoordinatesReader geoCoordinatesReader) {
    return Try.of(
      () -> geoCoordinatesReader.fromCsvFile("./src/main/resources/cities.csv")
    )
      .onSuccess(coords -> log.info("Coordinates read: {}", coords))
      .onFailure(exc -> log.error("Cannot read coordinates from a file", exc))
      .recover(FileNotFoundException.class, (exc) -> provideBackupCoordinates())
      .getOrElse(List.empty());
  }

  private static List<GeoCoordinates> provideBackupCoordinates() {
    log.warn("Calling for backup data");
    return List.of(
      GeoCoordinates.builder()
        .city("Warszawa")
        .latitude("52.25")
        .longitude("21")
        .build()
    );
  }

  private static Try<Void> save(MeasurementsRepository measurementsRepository, Measurements m) {
    return measurementsRepository
      .save(m)
      .onSuccess(ignore -> log.info("Measurement {} saved successfully", m))
      .onFailure(exc -> log.error("Error during saving measurement into database", exc));
  }

}
