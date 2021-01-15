/*
 * Copyright 2021 Rackspace US, Inc.
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

import com.google.protobuf.Timestamp;
import com.rackspace.monplat.protocol.Metric;
import com.rackspace.monplat.protocol.UniversalMetricFrame;
import com.rackspace.salus.metricsgen.model.GeneratedMetric;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("umf")
public class UmfMetricBuilder implements
    ProducedMetricBuilder {

  @Override
  public Object buildMetric(Instant now, GeneratedMetric generatedMetric,
                            Map<String, Long> metricFields) {
    return UniversalMetricFrame.newBuilder()
        .setAccountType(UniversalMetricFrame.AccountType.CLOUD)
        .setTenantId(generatedMetric.getTenant())
        .setMonitoringSystem(UniversalMetricFrame.MonitoringSystem.SALUS)
        .addAllMetrics(metricFields.entrySet().stream()
            .map(entry -> Metric.newBuilder()
                .setTimestamp(Timestamp.newBuilder()
                    .setSeconds(now.getEpochSecond())
                    .setNanos(now.getNano())
                    .build())
                .setGroup(generatedMetric.getName())
                .setName(entry.getKey())
                .setInt(entry.getValue())
                .build())
            .collect(Collectors.toList()))
        .build();
  }
}
