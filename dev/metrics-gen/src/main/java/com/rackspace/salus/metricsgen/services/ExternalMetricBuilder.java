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

import com.rackspace.monplat.protocol.AccountType;
import com.rackspace.monplat.protocol.ExternalMetric;
import com.rackspace.monplat.protocol.MonitoringSystem;
import com.rackspace.salus.metricsgen.model.GeneratedMetric;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("externalMetric")
public class ExternalMetricBuilder implements
    ProducedMetricBuilder {

  @Override
  public Object buildMetric(Instant now, GeneratedMetric generatedMetric,
                            Map<String, Long> metricFields) {
    return ExternalMetric.newBuilder()
        .setTimestamp(DateTimeFormatter.ISO_INSTANT.format(now))
        .setAccountType(AccountType.RCN)
        .setAccount(generatedMetric.getTenant())
        .setSystemMetadata(Collections.emptyMap())
        .setMonitoringSystem(MonitoringSystem.SALUS)
        .setCollectionName(generatedMetric.getName())
        .setCollectionMetadata(Collections.emptyMap())
        .setDevice(generatedMetric.getResource())
        .setDeviceMetadata(generatedMetric.getLabels())
        .setIvalues(metricFields)
        .setFvalues(Collections.emptyMap())
        .setSvalues(Collections.emptyMap())
        .setUnits(Collections.emptyMap())
        .build();
  }
}
