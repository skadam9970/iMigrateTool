package com.iMigrate.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ForeignKeys {

	private String pkTableName;
	private String fkTableName;
	private String pkColumnName;
	private String fkColumnName;
	
}
