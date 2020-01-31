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
import com.rackspace.salus.metricsgen.model.Metric;
import com.rackspace.salus.metricsgen.model.Resource;
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
  private boolean running;
  private List<Resource> resources = new ArrayList<>();

  @Autowired
  public Generator(MetricsGenProperties properties, Random rand,
                   KafkaTemplate<String, ExternalMetric> kafkaTemplate) {
    this.properties = properties;
    this.rand = rand;
    this.kafkaTemplate = kafkaTemplate;

  }

  @Override
  public void start() {

    final int minPeriod = (int) ((properties.getEmitRate().toMillis()/1000) * 2);

    for (String tenant : properties.getTenants()) {

      for (int r = 0; r < properties.getResourcesPerTenant(); r++) {
        final String resourceId = String.format("resource-%03d", r);

        final Resource resource = new Resource()
            .setTenant(tenant)
            .setId(resourceId);

        properties.getLabels()
            .forEach((key, values) -> resource.getLabels().put(
                key,
                values[rand.nextInt(values.length)]
            ));

        for (String metricName : properties.getMetrics()) {
          final Metric metric = new Metric();

          for (String fieldName : properties.getFields()) {
            metric.getFields().put(
                fieldName,
                new Field(
                    rand.nextInt(MAX_OFFSET),
                    Math.max(minPeriod, rand.nextInt(MAX_PERIOD)),
                    rand.nextInt(MAX_AMPLITUDE)
                )
            );
          }

          resource.getMetrics().put(metricName, metric);
        }

        resources.add(resource);
      }
    }

    log.info("Starting");
    for (Resource resource : resources) {
      log.info("{}", resource);
    }

    running = true;
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
    log.info("Generating");

    final Instant now = Instant.now();
    final long epochSecond = now.getEpochSecond();
    final String timestamp = DateTimeFormatter.ISO_INSTANT.format(now);

    for (Resource resource : resources) {

      resource.getMetrics().forEach((metricName, metricSpec) -> {
        final ExternalMetric metric = ExternalMetric.newBuilder()
            .setAccountType(AccountType.RCN)
            .setAccount(resource.getTenant())
            .setMonitoringSystem(MonitoringSystem.SALUS)
            .setCollectionName(metricName)
            .setCollectionMetadata(Collections.emptyMap())
            .setDevice(resource.getId())
            .setDeviceMetadata(Collections.emptyMap())
            .setTimestamp(timestamp)
            .setSystemMetadata(resource.getLabels())
            .setIvalues(fillMetricFields(metricSpec, epochSecond))
            .setFvalues(Collections.emptyMap())
            .setSvalues(Collections.emptyMap())
            .setUnits(Collections.emptyMap())
            .build();

        kafkaTemplate.send(
            properties.getTopic(),
            String.join(":", resource.getTenant(), resource.getId()),
            metric
            );
      });

    }
  }

  private Map<String, Long> fillMetricFields(Metric metricSpec, long now) {
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
