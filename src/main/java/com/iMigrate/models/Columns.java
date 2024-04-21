package com.iMigrate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Columns {
	private String columnName;
	private String columnSize;
	private String columnDataType;
	private String isNullable;
	private String isAutoIncrement;
}
