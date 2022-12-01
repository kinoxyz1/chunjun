/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.chunjun.environment;

import com.dtstack.chunjun.enums.ClusterMode;
import com.dtstack.chunjun.options.Options;
import com.dtstack.chunjun.util.PropertiesUtil;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.config.TableConfigOptions;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class EnvFactory {

    /**
     * 创建StreamExecutionEnvironment
     *
     * @param options
     * @return
     */
    public static StreamExecutionEnvironment createStreamExecutionEnvironment(Options options) {
        Configuration flinkConf = new Configuration();
        Configuration cfg = Configuration.fromMap(PropertiesUtil.confToMap(options.getConfProp()));
        registerPluginIntoEnv(cfg, Lists.newArrayList());
        if (StringUtils.isNotEmpty(options.getFlinkConfDir())) {
            flinkConf = GlobalConfiguration.loadConfiguration(options.getFlinkConfDir());
        }
        StreamExecutionEnvironment env;
        if (StringUtils.equalsIgnoreCase(ClusterMode.local.name(), options.getMode())) {
            flinkConf.addAll(cfg);
            env = new MyLocalStreamEnvironment(flinkConf);
        } else {
            env = StreamExecutionEnvironment.getExecutionEnvironment(cfg);
        }
        env.getConfig().disableClosureCleaner();
        env.getConfig().setGlobalJobParameters(cfg);
        return env;
    }

    public static List<URL> parsePluginPath(Options options) {
        List<URL> pluginPaths = Lists.newArrayList();

        String distDir = options.getChunjunDistDir();

        return pluginPaths;
    }

    public static void registerPluginIntoEnv(Configuration configuration, List<URL> pluginPaths) {
        List<String> jars = configuration.get(PipelineOptions.JARS);
        if (CollectionUtils.isEmpty(jars)) {
            jars = Lists.newArrayList();
        }
        jars.addAll(pluginPaths.stream().map(URL::toString).collect(Collectors.toList()));
        configuration.set(
                PipelineOptions.JARS, jars.stream().distinct().collect(Collectors.toList()));
        List<String> classpath = configuration.get(PipelineOptions.CLASSPATHS);
        if (classpath == null) {
            classpath = new ArrayList<>();
        }
        classpath.addAll(pluginPaths.stream().map(URL::toString).collect(Collectors.toList()));
        configuration.set(
                PipelineOptions.CLASSPATHS,
                classpath.stream().distinct().collect(Collectors.toList()));
    }

    public static StreamTableEnvironment createStreamTableEnvironment(
            StreamExecutionEnvironment env, Properties properties, String jobName) {
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);
        Configuration configuration = tEnv.getConfig().getConfiguration();
        // Iceberg need this config setting up true.
        configuration.setBoolean(
                TableConfigOptions.TABLE_DYNAMIC_TABLE_OPTIONS_ENABLED.key(), true);
        setTableConfig(tEnv, properties, jobName);
        setTableConfigForPython(tEnv, properties);
        return tEnv;
    }

    private static void setTableConfig(
            StreamTableEnvironment tEnv, Properties properties, String jobName) {
        Configuration configuration = tEnv.getConfig().getConfiguration();
        properties.entrySet().stream()
                .filter(e -> e.getKey().toString().toLowerCase().startsWith("table."))
                .forEach(
                        e ->
                                configuration.setString(
                                        e.getKey().toString().toLowerCase(),
                                        e.getValue().toString().toLowerCase()));

        configuration.setString(PipelineOptions.NAME, jobName);
    }

    private static void setTableConfigForPython(
            StreamTableEnvironment tEnv, Properties properties) {
        Configuration configuration = tEnv.getConfig().getConfiguration();
        properties.entrySet().stream()
                .filter(e -> e.getKey().toString().toLowerCase().startsWith("python."))
                .forEach(
                        e ->
                                configuration.setString(
                                        e.getKey().toString(), e.getValue().toString()));
    }
}
