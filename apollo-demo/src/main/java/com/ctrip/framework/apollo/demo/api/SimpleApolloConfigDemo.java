/*
 * Copyright 2022 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.demo.api;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.google.common.base.Charsets;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class SimpleApolloConfigDemo {
  private static final Logger logger = LoggerFactory.getLogger(SimpleApolloConfigDemo.class);
  private String DEFAULT_VALUE = "undefined";

  private Map<String, Config> nameSpaceConfig = new HashMap<>();
  public SimpleApolloConfigDemo(String [] appIds) {
    for (int i = 0; i < appIds.length; i++) {
      ConfigChangeListener changeListener = new ConfigChangeListener() {
        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
          logger.info("Changes for namespace {}", changeEvent.getNamespace());
          for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            logger.info("Change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
                    change.getPropertyName(), change.getOldValue(), change.getNewValue(),
                    change.getChangeType());
          }
        }
      };
      String nameSpace = appIds[i];
      Config config = ConfigService.getAppConfig(nameSpace);
      config.addChangeListener(changeListener);
      nameSpaceConfig.put(nameSpace, config);
    }
  }

  private String getConfig(String nameSpace,String key) {
    Config config = nameSpaceConfig.get(nameSpace);
    String result = config.getProperty(key, DEFAULT_VALUE);
    logger.info(String.format("Loading key : %s with value: %s", key, result));
    return result;
  }

  public static void main(String[] args) throws IOException {
    String cluster = System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY);
    String cluster1 = System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY);

    for (int i = 0; i < args.length; i++) {
      System.out.println(args[i]);
    }
    String[] split = args[0].split(",");
    SimpleApolloConfigDemo apolloConfigDemo = new SimpleApolloConfigDemo(split);
    System.out.println(
        "Apollo Config Demo. Please input key to get the value. Input quit to exit.");
    while (true) {
      System.out.print("> ");
      String input = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8)).readLine();
      if (input == null || input.length() == 0) {
        continue;
      }
      input = input.trim();
      if (input.equalsIgnoreCase("quit")) {
        System.exit(0);
      }
      String[] values = input.split(":");
      if (values.length != 2) {
        System.out.println("Please input NameSpace+key to get the value. Input quit to exit.:例如[dpc_global_config:D_DATETIME_BEFORE_DAY]");
        continue;
      }
      apolloConfigDemo.getConfig(values[0],values[1]);
    }
  }
}
