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

package com.rackspace.salus.metricsgen.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("salus.gen")
@Component
@Data
public class MetricsGenProperties {

  List<String> tenants = List.of("tenant-00","tenant-01","tenant-02","tenant-03","tenant-04");

  int resourcesPerTenant = 20;

  Duration emitRate = Duration.ofSeconds(10);

  List<String> metrics = List.of("cpu", "memory", "disk");

  List<String> fields = List.of("free", "used", "current");

  Map<String, String[]> labels = defaultLabels();

  String topic = "telemetry.metrics.json";

  private static Map<String, String[]> defaultLabels() {
    final Map<String, String[]> result = new HashMap<>();
    result.put("os", new String[]{"linux", "windows", "darwin"});
    result.put("arch", new String[]{"x64", "x32"});
    result.put("env", new String[]{"prod", "stage", "test"});
    return result;
  }
}
