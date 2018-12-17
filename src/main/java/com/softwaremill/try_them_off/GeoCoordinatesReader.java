package com.softwaremill.try_them_off;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.vavr.collection.List;
import io.vavr.collection.Stream;

class GeoCoordinatesReader {

  List<GeoCoordinates> fromCsvFile(String path) throws IOException {
    return Stream.ofAll(lines(path))
      .filter(line -> !line.startsWith("#"))
      .map(line -> line.split(","))
      .map(this::asGeoCoordinates)
      .toList();
  }

  private Iterable<String> lines(String fileName) throws IOException {
    return Files.readAllLines(Paths.get(fileName));
  }

  private GeoCoordinates asGeoCoordinates(String[] geoData) {
    return GeoCoordinates.builder().city(geoData[0]).latitude(geoData[1]).longitude(geoData[2])
      .build();
  }

}
