package com.iMigrate.service.impl;


import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iMigrate.config.IMigrateEntityManagerConfig;
import com.iMigrate.entity.DynamicEntity;
import com.iMigrate.models.Columns;
import com.iMigrate.models.ForeignKeys;
import com.iMigrate.models.Indexes;
import com.iMigrate.models.PrimaryKeys;
import com.iMigrate.models.Tables;
import com.iMigrate.service.IMigrateMetaDataService;
import com.iMigrate.service.IMigrateEntityCreationService;

@Service
public class IMigrateMetaDataServiceImpl implements IMigrateMetaDataService {

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private IMigrateEntityCreationService IMigrateEntityCreationService;
	
	public static String entityPkgPrefix = "com.iMigrate.models.";
	
	@Autowired
	private IMigrateEntityManagerConfig IMigrateEntityManagerConfig;

	@Autowired
	com.iMigrate.repo.IMigrateSourceTargetDatabaseRepository iMigrateSourceTargetDatabaseRepository;
	
	private static Logger log = LoggerFactory.getLogger(IMigrateMetaDataServiceImpl.class);

	/**
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@Override
	public void getDatabaseTableSchemaMetadata() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		try {
			Connection connection = dataSource.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();

			//ResultSet resultSet = metaData.getTables(null, null, null, new String[]{"TABLE","VIEW", "GLOBAL TEMPORARY","LOCAL TEMPORARY", "ALIAS", "SYNONYM"});
			ResultSet resultSet = metaData.getTables(null, "dbo", null, new String[]{"TABLE"});
			Map<String, Tables> tablesMap = new LinkedHashMap<String, Tables>();

			while(resultSet.next()){
				Tables tables = Tables.builder().tableName(resultSet.getString("TABLE_NAME"))
						.tableCatalog(resultSet.getString("TABLE_CAT"))
						.tableSchema(resultSet.getString("TABLE_SCHEM"))
						.tableType(resultSet.getString("TABLE_TYPE")).build();
				if(tables.getTableName().equals("table_metadata")) {
					continue;
				}

				//System.out.println("Catalog: " + tableCatalog  + ", Schema: " + tableSchema +"Table Name: " +  tableName + " Table Type: " + tableType);

				ResultSet columnsRS = metaData.getColumns(null, null, tables.getTableName(), null);
				List<Columns> colsList= new ArrayList<>();
				tables = setColumnMetadata(tables, columnsRS, colsList);

				ResultSet primaryKeys = metaData.getPrimaryKeys(tables.getTableCatalog(), tables.getTableSchema(), tables.getTableName());
				List<PrimaryKeys> listPKs= new ArrayList<>();
				tables = setPrimaryKeysMetadata(tables, primaryKeys, listPKs);

				ResultSet foreignKeys = metaData.getImportedKeys(tables.getTableCatalog(), tables.getTableSchema(), tables.getTableName());
				List<ForeignKeys> listFKs= new ArrayList<>();
				tables = setForeignKeyMetadata(tables, foreignKeys, listFKs);

				ResultSet indexInfo = metaData.getIndexInfo(tables.getTableCatalog(), tables.getTableSchema(), tables.getTableName(), false, true);
				List<Indexes> listInds= new ArrayList<>();
				tables = setIndexexMetadata(tables, indexInfo, listInds);

				tablesMap.put(tables.getTableName(), tables);
			}

			System.out.println(tablesMap);

			for (Map.Entry<String, Tables> entry : tablesMap.entrySet()) {
				Tables tables= entry.getValue();
				if(tables.getForeignKeys().size() > 0) {
					List<Tables> parentTablesList = new ArrayList<>();
					for (ForeignKeys fks : tables.getForeignKeys()) {
						parentTablesList.add(tablesMap.get(fks.getPkTableName()));
					}
					tables.toBuilder().parentKeyTables(parentTablesList).build();
				}
			}

			List<String> doneTablesList = new LinkedList<>();
			for (Map.Entry<String, Tables> entry : tablesMap.entrySet()) {
				String key = entry.getKey();
				//Tables tables= entry.getValue();
				if(doneTablesList.contains(key)){
					continue;
				}
				//Call object creation
			}
			List<DynamicEntity> listEntities = new ArrayList<>();

			prepateEntity(tablesMap, doneTablesList, listEntities);

			IMigrateEntityManagerConfig.createEntityManger(listEntities);

			IMigrateEntityManagerConfig.persistEntities(listEntities);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * @param tables
	 * @param indexInfo
	 * @param listInds
	 * @return
	 * @throws SQLException
	 */
	private Tables setIndexexMetadata(Tables tables, ResultSet indexInfo, List<Indexes> listInds) throws SQLException {
		while(indexInfo.next()){
			Indexes idx = Indexes.builder()
					.indexName(indexInfo.getString("INDEX_NAME"))
					.type(indexInfo.getString("TYPE"))
					.columnName(indexInfo.getString("COLUMN_NAME"))
					.build();
			listInds.add(idx);
			//System.out.println("Index Name: "+indexName);
			//System.out.println("Index Type: "+type);
		}
		tables =  tables.toBuilder().indexes(listInds).build();
		return tables;
	}

	private Tables setForeignKeyMetadata(Tables tables, ResultSet foreignKeys, List<ForeignKeys> listFKs)
			throws SQLException {
		while(foreignKeys.next()){
			ForeignKeys fks= ForeignKeys.builder()
					.pkTableName(foreignKeys.getString("PKTABLE_NAME"))
					.fkTableName(foreignKeys.getString("FKTABLE_NAME"))
					.pkColumnName(foreignKeys.getString("PKCOLUMN_NAME"))
					.fkColumnName(foreignKeys.getString("FKCOLUMN_NAME")).build();
			listFKs.add(fks);

			//	System.out.println("Primary Key Table Name: " + pkTableName+", Foreign Key Table Name: " + fkTableName + "Primary Key Table Column Name: " + pkColumnName+ ", Foreign Key Table Column Name: " + fkColumnName);
		}
		tables = tables.toBuilder().foreignKeys(listFKs).build();
		return tables;
	}

	/**
	 * @param tables
	 * @param primaryKeys
	 * @param listPKs
	 * @return
	 * @throws SQLException
	 */
	private Tables setPrimaryKeysMetadata(Tables tables, ResultSet primaryKeys, List<PrimaryKeys> listPKs)
			throws SQLException {
		while(primaryKeys.next()){
			PrimaryKeys pks = PrimaryKeys.builder().primaryKeyColName(primaryKeys.getString("COLUMN_NAME"))
					.primaryKeyName(primaryKeys.getString("PK_NAME")).build();
			listPKs.add(pks);
			//System.out.println("Primary Key Column Name: " + primaryKeyColName+", Primary Key Name: " + primaryKeyName);
		}
		tables = tables.toBuilder().primaryKeys(listPKs).build();
		return tables;
	}

	/**
	 * @param tables
	 * @param columnsRS
	 * @param colsList
	 * @return
	 * @throws SQLException
	 */
	private Tables setColumnMetadata(Tables tables, ResultSet columnsRS, List<Columns> colsList) throws SQLException {
		while(columnsRS.next()){
			Columns columns = Columns.builder().columnName(columnsRS.getString("COLUMN_NAME"))
					.columnSize(columnsRS.getString("COLUMN_SIZE"))
					.columnDataType(JDBCType.valueOf(Integer.parseInt(columnsRS.getString("DATA_TYPE"))).getName())
					.isNullable(columnsRS.getString("IS_NULLABLE"))
					.isAutoIncrement(columnsRS.getString("IS_AUTOINCREMENT")).build();
			colsList.add(columns);
			//System.out.println("Column Name: " + columnName +", Column Size: " + columnSize+", Column Data Type: " + columnDataType +", Column Is Nullable: " + isNullable +", Column Is auto Increment: " + isAutoIncrement);
		}
		tables = tables.toBuilder().columns(colsList).build();
		return tables;
	}

	/**
	 * @param tablesMap
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 */
	public List<DynamicEntity> getEnitiyClassNames(Map<String, Tables> tablesMap) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException{
		List<DynamicEntity> listFullClazzName= new ArrayList<>();
		for (Map.Entry<String, Tables> entry : tablesMap.entrySet()) {
			String tableName = entry.getKey();
			DynamicEntity dynamicEntity = (DynamicEntity) Class.forName(IMigrateEntityCreationServiceImpl.entityPkgPrefix + tableName)
					.getConstructor().newInstance();
			listFullClazzName.add(dynamicEntity);
		}
		return listFullClazzName;

	}

	/**
	 * @param tablesMap
	 * @param doneTablesList
	 * @param listEntities
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void prepateEntity(Map<String, Tables> tablesMap, List<String> doneTablesList, List<DynamicEntity> listEntities) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if(tablesMap == null || tablesMap.size() == 0) {
			log.info("No tables available");
		}
		for (Map.Entry<String, Tables> entryTables : tablesMap.entrySet()) {
			Tables tables= entryTables.getValue();
			createEntity(tables, doneTablesList, listEntities);
		}
	}

	/**
	 * @param tables
	 * @param doneTablesList
	 * @param listEntities
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void createEntity(Tables tables, List<String> doneTablesList, List<DynamicEntity> listEntities) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {		
		if(doneTablesList.contains(tables.getTableName())){
			return;
		} else if(tables.getParentKeyTables()!=null && tables.getParentKeyTables().size()>0) {
			for(Tables parentTable : tables.getParentKeyTables()) {
				createEntity(parentTable, doneTablesList, listEntities);
			}
		}
		DynamicEntity entity = IMigrateEntityCreationService.getJPAEntity(tables);
		listEntities.add(entity);
		doneTablesList.add(tables.getTableName());
	}
	//Employee1
	//Employee -> Department
	//Department
}
