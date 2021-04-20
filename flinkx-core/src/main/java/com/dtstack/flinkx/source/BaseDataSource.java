/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.flinkx.source;

import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.util.Preconditions;

import com.dtstack.flinkx.conf.FlinkxCommonConf;
import com.dtstack.flinkx.conf.SpeedConf;
import com.dtstack.flinkx.conf.FlinkXConf;
import com.dtstack.flinkx.streaming.api.functions.source.DtInputFormatSourceFunction;
import com.dtstack.flinkx.util.PropertiesUtil;
import com.dtstack.flinkx.util.TableUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.Collections;

/**
 * Abstract specification of Reader Plugin
 *
 * Company: www.dtstack.com
 * @author huyifan.zju@163.com
 */
public abstract class BaseDataSource {

    protected StreamExecutionEnvironment env;
    protected FlinkXConf flinkXConf;
    protected TypeInformation<RowData> typeInformation;

    protected BaseDataSource(FlinkXConf flinkXConf, StreamExecutionEnvironment env) {
        this.env = env;
        this.flinkXConf = flinkXConf;

        if(flinkXConf.getTransformer() == null || StringUtils.isBlank(flinkXConf.getTransformer().getTransformSql())){
            typeInformation = TableUtil.getTypeInformation(Collections.emptyList());
        }else{
            typeInformation = TableUtil.getTypeInformation(flinkXConf.getReader().getFieldList());
        }
    }

    /**
     * Build the read data flow object
     *
     * @return DataStream
     */
    public abstract DataStream<RowData> readData();

    @SuppressWarnings("unchecked")
    protected DataStream<RowData> createInput(InputFormat inputFormat, String sourceName) {
        Preconditions.checkNotNull(sourceName);
        Preconditions.checkNotNull(inputFormat);
//        TypeInformation typeInfo = TypeExtractor.getInputFormatTypes(inputFormat);
        DtInputFormatSourceFunction function = new DtInputFormatSourceFunction(inputFormat, typeInformation);
        return env.addSource(function, sourceName, typeInformation);
    }

    protected DataStream<RowData> createInput(RichParallelSourceFunction<RowData> function, String sourceName) {
        Preconditions.checkNotNull(sourceName);
        return env.addSource(function, sourceName, typeInformation);
    }

    protected DataStream<RowData> createInput(InputFormat inputFormat) {
        return createInput(inputFormat, this.getClass().getSimpleName().toLowerCase());
    }

    /**
     * 初始化FlinkxCommonConf
     * @param flinkxCommonConf
     */
    public void initFlinkxCommonConf(FlinkxCommonConf flinkxCommonConf){
        PropertiesUtil.initFlinkxCommonConf(flinkxCommonConf, this.flinkXConf);
        flinkxCommonConf.setCheckFormat(this.flinkXConf.getReader().getBooleanVal("check", true));
        SpeedConf speed = this.flinkXConf.getSpeed();
        flinkxCommonConf.setParallelism(speed.getReaderChannel() == -1 ? speed.getChannel() : speed.getReaderChannel());
    }

    public abstract LogicalType getLogicalType() throws SQLException;
}
