/*
 * Copyright 2020 Rackspace US, Inc.
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

package com.rackspace.salus.metricsgen.services;

import com.rackspace.monplat.protocol.AccountType;
import com.rackspace.monplat.protocol.ExternalMetric;
import com.rackspace.monplat.protocol.MonitoringSystem;
import com.rackspace.salus.metricsgen.config.MetricsGenProperties;
import com.rackspace.salus.metricsgen.model.Field;
import com.rackspace.salus.metricsgen.model.GeneratedMetric;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Generator implements SmartLifecycle {

  private static final int MAX_OFFSET = 10;
  private static final int MAX_PERIOD = 120;
  private static final int MAX_AMPLITUDE = 100;

  private final MetricsGenProperties properties;
  private final Random rand;
  private final KafkaTemplate<String, ExternalMetric> kafkaTemplate;
  private final int minPeriod;
  private boolean running;
  private List<GeneratedMetric> generatedMetrics = new ArrayList<>();

  @Autowired
  public Generator(MetricsGenProperties properties, Random rand,
                   KafkaTemplate<String, ExternalMetric> kafkaTemplate) {
    this.properties = properties;
    this.rand = rand;
    this.kafkaTemplate = kafkaTemplate;
    minPeriod = (int) ((properties.getEmitRate().toMillis()/1000) * 2);

  }

  @Override
  public void start() {

    properties.getTenants().forEach((tenantId, tenant) -> {
      tenant.getResources().forEach(resourceId -> {
        tenant.getMetrics().forEach((metricName, metric) -> {
          generatedMetrics.add(
              new GeneratedMetric()
                  .setName(metricName)
                  .setTenant(tenantId)
                  .setResource(resourceId)
                  .setLabels(pickLabels(tenant.getLabels()))
                  .setFields(pickFields(metric.getFields()))
          );
        });
      });
    });

    log.info("Starting");
    for (GeneratedMetric generatedMetric : generatedMetrics) {
      log.info("{}", generatedMetric);
    }

    running = true;
  }

  private Map<String, Field> pickFields(List<String> fields) {
    final HashMap<String, Field> picked = new HashMap<>();

    for (String fieldName : fields) {
      picked.put(
          fieldName,
          new Field(
              rand.nextInt(MAX_OFFSET),
              Math.max(minPeriod, rand.nextInt(MAX_PERIOD)),
              rand.nextInt(MAX_AMPLITUDE)
          )
      );
    }

    return picked;
  }

  private Map<String, String> pickLabels(Map<String, String[]> labels) {
    final HashMap<String, String> picked = new HashMap<>();

    labels
        .forEach((key, values) -> picked.put(
            key,
            values[rand.nextInt(values.length)]
        ));

    return picked;
  }

  @Override
  public void stop() {
    log.info("Stopping");
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Scheduled(fixedRateString = "#{metricsGenProperties.emitRate}")
  public void generate() {
    log.info("Generating {} metrics", generatedMetrics.size());

    final Instant now = Instant.now();
    final long epochSecond = now.getEpochSecond();
    final String timestamp = DateTimeFormatter.ISO_INSTANT.format(now);

    for (GeneratedMetric generatedMetric : generatedMetrics) {

        final ExternalMetric metric = ExternalMetric.newBuilder()
            .setTimestamp(timestamp)
            .setAccountType(AccountType.RCN)
            .setAccount(generatedMetric.getTenant())
            .setSystemMetadata(Collections.emptyMap())
            .setMonitoringSystem(MonitoringSystem.SALUS)
            .setCollectionName(generatedMetric.getName())
            .setCollectionMetadata(Collections.emptyMap())
            .setDevice(generatedMetric.getResource())
            .setDeviceMetadata(generatedMetric.getLabels())
            .setIvalues(fillMetricFields(generatedMetric, epochSecond))
            .setFvalues(Collections.emptyMap())
            .setSvalues(Collections.emptyMap())
            .setUnits(Collections.emptyMap())
            .build();

        log.trace("Sending {}", metric);
        kafkaTemplate.send(
            properties.getTopic(),
            String.join(",", generatedMetric.getTenant(), generatedMetric.getResource()),
            metric
            );
    }
  }

  private Map<String, Long> fillMetricFields(GeneratedMetric metricSpec, long now) {
    final Map<String, Long> fields = new HashMap<>();

    metricSpec.getFields().forEach((fieldName, field) -> {
      final double value = field.getAmplitude() +
          field.getAmplitude() *
              Math.sin(
                  (2.0 * Math.PI * (now + field.getOffset()))
                      / field.getPeriod()
              );

      fields.put(fieldName, (long) value);
    });
    return fields;
  }

}
