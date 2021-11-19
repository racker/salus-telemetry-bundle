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

package com.rackspace.monplat.metricconsumer;

import com.rackspace.monplat.protocol.ExternalMetric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalMetricConsumer {

  private final AppProperties appProperties;

  @Autowired
  public ExternalMetricConsumer(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  public String[] getTopics() {
    return appProperties.getTopics();
  }

  @KafkaListener(topics = "#{__listener.topics}")
  public void consumeExternalMetric(ExternalMetric metric) {
    log.info("Consumed metric: {}", metric);
  }

  @KafkaListener(topics = "#{__listener.topics}")
  public void consumeRawStringMessages(String message) {
    log.info("Consumed raw message: {}", message);
  }
}
