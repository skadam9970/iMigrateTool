package com.iMigrate.repo;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IMigrateSourceTargetDatabaseRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * @param tableName
	 * @return
	 */
//	public List<Object[]> fetchSourceDbTableData(String tableName) {
//		// Construct SQL query dynamically
//		String sql = "SELECT * FROM " + tableName;
//
//		// Execute query and map result
//		return jdbcTemplate.query(sql, new DynamicRowMapper());
//	}
	/**
	 * @param tableName
	 * @return
	 */
	public List<Map<String, Object>> fetchSourceDbTableData(String tableName) {
        String sql = "SELECT * FROM "+tableName;
        return jdbcTemplate.query(sql, new DynamicColumnMapper());
    }	
	
	/**
	 * @author Nishant Kansal
	 *
	 */
	public class DynamicColumnMapper implements RowMapper<Map<String, Object>> {
		@Override
		public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
			Map<String, Object> resultMap = new HashMap<>();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			// Iterate through all columns
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnName(i);
				Object columnValue = rs.getObject(i);
				// Dynamically map column values based on column name
				resultMap.put(columnName, columnValue);
			}
			return resultMap;
		}
	}
	/**
	 * @param tableName
	 * @param data
	 */
	public void insertDataToTargetDB(String tableName, Map<String, Object> data) {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
        sqlBuilder.append(tableName).append(" (");
 
        StringBuilder paramsBuilder = new StringBuilder();
        Object[] params = new Object[data.size()];
        int index = 0;
 
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String columnName = entry.getKey();
            Object value = entry.getValue();
 
            sqlBuilder.append(columnName);
            paramsBuilder.append("?");
 
            if (index < data.size() - 1) {
                sqlBuilder.append(", ");
                paramsBuilder.append(", ");
            }
 
            params[index] = value;
            index++;
        }
 
        sqlBuilder.append(") VALUES (").append(paramsBuilder).append(")");
 
        jdbcTemplate.update(sqlBuilder.toString(), params);
    }

	
	/**
	 * @author Nishant Kansal
	 *
	 */
	private static class DynamicRowMapper implements RowMapper<Object[]> {
		@Override
		public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			Object[] result = new Object[columnCount];

			for (int i = 1; i <= columnCount; i++) {
				result[i - 1] = rs.getObject(i);
			}
			return result;
		}

	}

	//	public void getTableData(String tableName) {
	//
	//		List<String> columnNames = jdbcTemplate.query("SELECT column_name FROM dbo.columns WHERE table_name = ?", new Object[]{tableName},
	//				(rs, rowNum) -> rs.getString("column_name"));
	//
	//		// Construct SQL query dynamically
	//		StringBuilder queryBuilder = new StringBuilder("SELECT ");
	//		for (int i = 0; i < columnNames.size(); i++) {
	//			queryBuilder.append(columnNames.get(i));
	//			if (i < columnNames.size() - 1) {
	//				queryBuilder.append(", ");
	//			}
	//		}
	//		queryBuilder.append(" FROM ").append(tableName);
	//
	//		// Execute query and map result
	//
	//		return jdbcTemplate.query(queryBuilder.toString(), new DynamicRowMapper());
	//
	//	}

}


