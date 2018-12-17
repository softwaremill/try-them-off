package com.softwaremill.try_them_off;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true)
@RequiredArgsConstructor
class GeoCoordinates {

  private final String city;
  private final String latitude;
  private final String longitude;

}
