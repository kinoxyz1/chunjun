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

package com.dtstack.flinkx.connector.clickhouse.sink;

import com.dtstack.flinkx.connector.clickhouse.converter.ClickhouseRawTypeConverter;
import com.dtstack.flinkx.connector.clickhouse.util.ClickhouseUtil;
import com.dtstack.flinkx.connector.jdbc.sink.JdbcOutputFormat;
import com.dtstack.flinkx.util.TableUtil;

import org.apache.flink.table.types.logical.RowType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @program: flinkx
 * @author: xiuzhu
 * @create: 2021/05/10
 */
public class ClickhouseOutputFormat extends JdbcOutputFormat {

    @Override
    protected void openInternal(int taskNumber, int numTasks) {
        super.openInternal(taskNumber, numTasks);
        RowType rowType =
                TableUtil.createRowType(column, columnType, ClickhouseRawTypeConverter::apply);
        setRowConverter(jdbcDialect.getColumnConverter(rowType));
    }

    @Override
    protected Connection getConnection() {
        try {
            return ClickhouseUtil.getConnection(
                    jdbcConf.getJdbcUrl(), jdbcConf.getUsername(), jdbcConf.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
