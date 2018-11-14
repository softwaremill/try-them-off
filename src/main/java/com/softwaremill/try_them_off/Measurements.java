package com.softwaremill.try_them_off;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(fluent = true)
class Measurements {

    private final Instant timestamp;
    private final String city;
    private final double pm1;
    private final double pm25;
    private final double pm10;
    private final double pressure;
    private final double humidity;
    private final double temperature;

}
