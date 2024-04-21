package com.iMigrate.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Indexes {

	private String indexName;
	private String type;
	private String columnName;
}
