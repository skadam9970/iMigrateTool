package com.iMigrate.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.iMigrate.models.Columns;

@Component
public class IMigrateUtils {

	/**
	 * Mapping source database column type to destination database column type
	 * 
	 * @param colList
	 * @return
	 */
	public Map<String, Class<?>> mapColumnDataType(List<Columns> colList) {
		Map<String, Class<?>> fields = new HashMap<>();
		for (Columns columns : colList) {
			if (columns.getColumnDataType().equals("BIGINT")) {
				fields.put(columns.getColumnName(), Long.class);
			} else if (columns.getColumnDataType().equals("NCHAR")) {
				fields.put(columns.getColumnName(), String.class);
			} else if (columns.getColumnDataType().equals("NUMERIC")) {
				fields.put(columns.getColumnName(), BigDecimal.class);
			} else if (columns.getColumnDataType().equals("DATE")) {
				fields.put(columns.getColumnName(), Date.class);
			} else {
				fields.put(columns.getColumnName(), String.class);
			}
		}
		return fields;
	}

	/**
	 * @param columns
	 * @return
	 */
	public Class<?> mapSingleColumnDataType(Columns columns) {
		if (columns.getColumnDataType().equals("BIGINT")) {
			return Long.class;
		} else if (columns.getColumnDataType().equals("NCHAR")) {
			return String.class;
		} else if (columns.getColumnDataType().equals("NUMERIC")) {
			return Integer.class;
		} else if (columns.getColumnDataType().equals("DATE")) {
			return Date.class;
		} else {
			return String.class;
		}
		
	}

}
