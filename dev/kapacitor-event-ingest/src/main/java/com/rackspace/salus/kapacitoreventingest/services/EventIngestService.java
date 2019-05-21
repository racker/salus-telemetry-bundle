/*
 * Copyright 2019 Rackspace US, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rackspace.salus.kapacitoreventingest.services;

import com.rackspace.salus.kapacitoreventingest.config.EventIngestProperties;
import com.rackspace.salus.kapacitoreventingest.model.KapacitorEvent;
import com.rackspace.salus.kapacitoreventingest.model.Series;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventIngestService {

  private static final String MEASUREMENT = "event";
  private final EventIngestProperties properties;
  private final InfluxDB influxDB;

  @Autowired
  public EventIngestService(EventIngestProperties properties,
                            InfluxDB influxDB) {
    this.properties = properties;
    this.influxDB = influxDB;
  }

  public String getTopic() {
    return properties.getTopic();
  }

  @KafkaListener(topics = "#{__listener.topic}")
  public void handleEvent(KapacitorEvent event) {

    if (event.getData().getSeries().isEmpty()) {
      log.warn("Discarding event with empty series: {}", event);
      return;
    }

    final Builder pointBuilder = Point.measurement(MEASUREMENT)
        .time(event.getTime().getTime(), TimeUnit.MILLISECONDS)
        .tag("id", event.getId())
        .addField("message", event.getMessage())
        .addField("details", event.getDetails())
        .addField("duration", event.getDuration())
        .addField("level", event.getLevel())
        .addField("previousLevel", event.getPreviousLevel())
        ;

    final Series series = event.getData().getSeries().get(0);
    final String measurement = series.getName();
    pointBuilder.tag("measurement", measurement);
    pointBuilder.tag(series.getTags());

    for (int c = 0; c < series.getColumns().size(); c++) {
      final String valueName = series.getColumns().get(c);
      if (!valueName.equals("time")) {
        for (int v = 0; v < series.getValues().size(); v++) {
          final Object value = series.getValues().get(v).get(c);
          if (value instanceof Number) {
            pointBuilder.addField(String.format("%s_%s_%d", measurement, valueName, v), ((Number) value));
          }
        }
      }
    }

    influxDB.write(pointBuilder.build());
  }
}
