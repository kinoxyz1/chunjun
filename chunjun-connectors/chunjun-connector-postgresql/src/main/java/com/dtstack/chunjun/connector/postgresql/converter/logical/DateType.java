package com.dtstack.chunjun.connector.postgresql.converter.logical;

import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.LogicalTypeRoot;
import org.apache.flink.table.types.logical.LogicalTypeVisitor;

import org.postgresql.core.Oid;

import java.io.Reader;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DateType extends PgCustomType {

    private static final Set<String> INPUT_CONVERSION =
            conversionSet(Date.class.getName(), Date.class.getName());

    private static final Class<?> DEFAULT_CONVERSION = Date.class;

    private static final Set<String> OUTPUT_CONVERSION = conversionSet(Reader.class.getName());

    public DateType(boolean isNullable, LogicalTypeRoot typeRoot, boolean isArray) {
        super(isNullable, typeRoot, isArray, Oid.DATE_ARRAY);
    }

    @Override
    public String asSerializableString() {
        return "PG-DATE";
    }

    @Override
    public boolean supportsInputConversion(Class<?> clazz) {
        return INPUT_CONVERSION.contains(clazz.getName());
    }

    @Override
    public boolean supportsOutputConversion(Class<?> clazz) {
        return OUTPUT_CONVERSION.contains(clazz.getName());
    }

    @Override
    public Class<?> getDefaultConversion() {
        return DEFAULT_CONVERSION;
    }

    @Override
    public List<LogicalType> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public <R> R accept(LogicalTypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public LogicalType copy(boolean isNullable) {
        return new DateType(isNullable, getTypeRoot(), isArray());
    }
}
