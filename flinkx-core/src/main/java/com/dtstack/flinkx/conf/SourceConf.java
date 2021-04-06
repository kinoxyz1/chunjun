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
package com.dtstack.flinkx.conf;

import com.dtstack.flinkx.constants.ConfigConstant;
import com.dtstack.flinkx.util.GsonUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Date: 2021/01/18
 * Company: www.dtstack.com
 *
 * @author tudou
 */
public class SourceConf implements Serializable {
    private static final long serialVersionUID = 1L;

    /** source插件名称 */
    private String name;
    /** source配置 */
    private Map<String, Object> parameter;
    /** table设置 */
    private TableConf table;
    /** fieldList */
    private List<FieldConf> fieldList;
    /** fieldNameList */
    private List<String> fieldNameList;
    /** fieldTypeList */
    private List<String> fieldTypeList;
    /** fieldClassList */
    private List<Class> fieldClassList;

    public List<FieldConf> getFieldList(){
        if(fieldList == null){
            List list = (List) parameter.get(ConfigConstant.KEY_COLUMN);
            fieldList = FieldConf.getFieldList(list);
            fieldNameList = new ArrayList<>(fieldList.size());
            fieldTypeList = new ArrayList<>(fieldList.size());
            fieldClassList = new ArrayList<>(fieldList.size());
            for (FieldConf field : fieldList) {
                fieldNameList.add(field.getName());
                fieldTypeList.add(field.getType());
                fieldClassList.add(field.getFieldClass());
            }
        }
        return fieldList;
    }

    public List getMetaColumn(){
        return (List) parameter.get(ConfigConstant.KEY_COLUMN);
    }

    public List<String> getFieldNameList() {
        return fieldNameList;
    }

    public List<String> getFieldTypeList() {
        return fieldTypeList;
    }

    public List<Class> getFieldClassList() {
        return fieldClassList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getParameter() {
        return parameter;
    }

    public void setParameter(Map<String, Object> parameter) {
        this.parameter = parameter;
    }

    public TableConf getTable() {
        return table;
    }

    public void setTable(TableConf table) {
        this.table = table;
    }

    /**
     * 从parameter中获取指定key的value，若无则返回defaultValue
     * @param key
     * @param defaultValue
     * @return
     */
    public int getIntVal(String key, int defaultValue) {
        Object ret = parameter.get(key);
        if(ret == null) {
            return defaultValue;
        }
        if(ret instanceof Integer) {
            return (Integer) ret;
        }
        if(ret instanceof String) {
            return Integer.parseInt((String) ret);
        }
        if(ret instanceof Long) {
            return ((Long)ret).intValue();
        }
        if(ret instanceof Float) {
            return ((Float)ret).intValue();
        }
        if(ret instanceof Double) {
            return ((Double)ret).intValue();
        }
        if(ret instanceof BigInteger) {
            return ((BigInteger)ret).intValue();
        }
        if(ret instanceof BigDecimal) {
            return ((BigDecimal)ret).intValue();
        }
        throw new RuntimeException(String.format("cant't %s from %s to int, internalMap = %s", key, ret.getClass().getName(), GsonUtil.GSON.toJson(parameter)));
    }

    public boolean getBooleanVal(String key, boolean defaultValue) {
        Object ret = parameter.get(key);
        if (ret == null) {
            return defaultValue;
        }
        if (ret instanceof Boolean) {
            return (Boolean) ret;
        }
        throw new RuntimeException(String.format("cant't %s from %s to boolean, internalMap = %s", key, ret.getClass().getName(), GsonUtil.GSON.toJson(parameter)));
    }

    /**
     * 从指定key中获取Properties配置信息
     * @param key
     * @param p
     * @return Properties
     */
    @SuppressWarnings("unchecked")
    public Properties getProperties(String key, Properties p ){
        Object ret = parameter.get(key);
        if(p == null){
            p = new Properties();
        }
        if (ret == null) {
            return p;
        }
        if(ret instanceof Map){
            Map<String, Object> map = (Map<String, Object>) ret;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                p.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
            return p;
        }else{
            throw new RuntimeException(String.format("cant't %s from %s to map, internalMap = %s", key, ret.getClass().getName(), GsonUtil.GSON.toJson(parameter)));
        }
    }


    @Override
    public String toString() {
        return "SourceConf{" +
                "name='" + name + '\'' +
                ", parameter=" + parameter +
                ", table=" + table +
                '}';
    }
}
