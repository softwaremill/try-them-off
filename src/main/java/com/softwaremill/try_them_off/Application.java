package com.softwaremill.try_them_off;

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
      .andThenTry(
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
      .filter(Try::isSuccess)
      .map(Try::get)
      .map(m -> measurementsRepository
        .save(m)
        .onSuccess(ignore -> log.info("Measurement {} saved successfully", m))
        .onFailure(exc -> log.error("Error during saving measurement into database", exc))
      );
  }

  private static List<GeoCoordinates> readCoordinates(GeoCoordinatesReader geoCoordinatesReader) {
    return Try.of(
      () -> geoCoordinatesReader.fromCsvFile("./src/main/resources/cities.csv")
    )
      .onFailure(exc -> log.error("Cannot read coordinates from a file", exc))
      .onSuccess(coords -> log.info("Coordinates read: {}", coords))
      .getOrElse(List.empty());
  }

}
