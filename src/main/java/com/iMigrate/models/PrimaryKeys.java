package com.iMigrate.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class PrimaryKeys {
	private String primaryKeyColName;
	private String primaryKeyName;
}
