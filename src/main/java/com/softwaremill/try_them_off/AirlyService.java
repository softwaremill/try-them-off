package com.softwaremill.try_them_off;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class AirlyService {

    private static final TypeReference<List<Measurement>> MEASUREMENTS_LIST_REFERENCE = new TypeReference<>() {};

    private final HttpRequest.Builder airlyRequestBuilder = HttpRequest.newBuilder()
                                                                       .GET()
                                                                       .header("Accept",
                                                                               "application/json")
                                                                       .header("apikey",
                                                                               "----------------------------");

    private final HttpResponse.BodyHandler<String> bodyHandler = responseInfo -> HttpResponse.BodySubscribers
      .ofString(UTF_8);

    private final ObjectMapper mapper = new ObjectMapper()
      .findAndRegisterModules()
      .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Try<Measurements> getMeasurement(GeoCoordinates coordinates) {
        return Try.of(() -> intoAirlyUri(coordinates.latitude(), coordinates.longitude()))
          .flatMap(this::call)
          .map(responseBody -> new RawMeasurements(coordinates.city(), responseBody))
          .mapTry(this::parse);
    }

    private URI intoAirlyUri(String latitude, String longitude) {
        return URI.create(
          String.format(
            "https://airapi.airly.eu/v2/measurements/nearest?lat=%s&lng=%s&maxDistanceKM=5",
            latitude, longitude)
        );
    }

    private Try<String> call(URI airlyUri) {
        HttpRequest req = airlyRequestBuilder.uri(airlyUri).build();
        return Try
          .of(() -> newHttpClient().send(req, bodyHandler))
          .map(HttpResponse::body);
    }

    private Measurements parse(RawMeasurements raw) throws IOException {
        final JsonNode jsonNode = mapper.readTree(raw.getRawData()).get("current");
        final Instant timestamp = Instant.parse(jsonNode.get("tillDateTime").textValue());
        final List<Measurement> measurements = mapper.convertValue(jsonNode.get("values"), MEASUREMENTS_LIST_REFERENCE);

        final Function1<String, Double> mapped = measurements
          .map(m -> Tuple.of(m.getName(), m.getValue()))
          .toMap(t -> t)
          .withDefaultValue(0.0);

        return new Measurements(timestamp, raw.getCity(),
                                mapped.apply("PM1"),
                                mapped.apply("PM25"),
                                mapped.apply("PM10"),
                                mapped.apply("PRESSURE"),
                                mapped.apply("HUMIDITY"),
                                mapped.apply("TEMPERATURE")
        );
    }

    @Value
    private class RawMeasurements {

        private final String city;
        private final String rawData;

    }

    @Data
    private class Measurement {

        private String name;
        private double value;

    }
}
