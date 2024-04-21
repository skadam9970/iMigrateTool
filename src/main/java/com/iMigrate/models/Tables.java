package com.iMigrate.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Tables {
	private String tableName;
	private String tableCatalog; 
	private String tableSchema;
	private String tableType;
	private List<Columns> columns;
	private List<PrimaryKeys> primaryKeys;
	private List<ForeignKeys> foreignKeys;
	private List<Indexes> indexes;
	private List<Tables> parentKeyTables;
}
