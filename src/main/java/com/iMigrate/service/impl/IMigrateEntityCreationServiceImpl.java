package com.iMigrate.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iMigrate.entity.DynamicEntity;
import com.iMigrate.models.Columns;
import com.iMigrate.models.ForeignKeys;
import com.iMigrate.models.Indexes;
import com.iMigrate.models.PrimaryKeys;
import com.iMigrate.models.Tables;
import com.iMigrate.service.IMigrateEntityCreationService;
import com.iMigrate.util.IMigrateUtils;

import jakarta.persistence.FetchType;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

/**
 * 
 * @author
 *
 */
@Service
public class IMigrateEntityCreationServiceImpl implements IMigrateEntityCreationService {

	public static String entityPkgPrefix = "com.iMigrate.models.";

	@Autowired
	private IMigrateUtils iMigrateUtils;

	@Autowired
	com.iMigrate.repo.IMigrateSourceTargetDatabaseRepository iMigrateSourceTargetDatabaseRepository;


	/**
	 * @param tables
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@Override
	public DynamicEntity getJPAEntity(Tables tables)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		createJPAEntity(tables, entityPkgPrefix + tables.getTableName(), tables.getColumns());
		DynamicEntity entity = (DynamicEntity) Class.forName(entityPkgPrefix + tables.getTableName()).getConstructor()
				.newInstance();

		List<Map<String, Object>> object = iMigrateSourceTargetDatabaseRepository.fetchSourceDbTableData(tables.getTableName());
		Map<String, ForeignKeys> listPks = null;
		if(tables.getForeignKeys()!=null && tables.getForeignKeys().size()>0) {
			listPks = tables.getForeignKeys().stream()
					.collect(Collectors.toMap(obj->obj.getFkColumnName(), Function.identity()));
					
		}
		for (Map<String, Object> row : object) {
			for (Columns col : tables.getColumns()) {
				if(listPks!=null && listPks.size()>0 && listPks.containsKey(col.getColumnName())) {
					ForeignKeys fkDet = listPks.get(col.getColumnName());
					DynamicEntity fkEntity = (DynamicEntity) Class.forName(entityPkgPrefix + fkDet.getPkTableName()).getConstructor()
							.newInstance();
					entity.set(fkDet.getPkTableName(), fkEntity);
				}else {
					entity.set(col.getColumnName(),row.get(col.getColumnName()));	
				}
				
			}
		}
		return entity;
	}

	/**
	 * @param tables
	 * @param className
	 * @param columns
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?> createJPAEntity(Tables tables, String className, List<Columns> columns)
			throws ClassNotFoundException {
		Map<String, Columns> mapColumns = columns.stream().collect(Collectors.toMap(Columns::getColumnName, Function.identity()));
		Map<String, Class<?>> fields = iMigrateUtils.mapColumnDataType(columns);

		Builder<DynamicEntity> builder;		
		//@Table(indexes = { @Index(name = "IDX_MYIDX1", columnList = "id,name,surname") })
		List<Indexes> listIndx = tables.getIndexes();
		if(listIndx!= null && listIndx.size()>0) {
			String indexes = listIndx.stream().filter(obj->obj.getIndexName()!=null).map(obj -> obj.getColumnName()).collect(Collectors.joining(","));			
			builder = getTableMetadataWithIndexed(tables, indexes);
		} else {
			builder = getTableMetaData(tables);
		}
		Map<String, PrimaryKeys> pkMap = tables.getPrimaryKeys().stream()
				.collect(Collectors.toMap(PrimaryKeys::getPrimaryKeyColName, Function.identity()));

		Map<String, ForeignKeys> fkColsMap = tables.getForeignKeys().stream()
				.collect(Collectors.toMap(ForeignKeys::getFkColumnName, Function.identity()));

		for (Map.Entry<String, Class<?>> e : fields.entrySet()) {
			if (pkMap.containsKey(e.getKey())) {
				continue;
			} else if (fkColsMap.containsKey(e.getKey())) {
				ForeignKeys fks = fkColsMap.get(e.getKey());
				// @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH, optional =
				// false)
				// @JoinColumn(name="TCONC_CONTRACT_ID_SK", referencedColumnName =
				// "pkColumnName", nullable=false, updatable = false)
				// private Department department;
				Class clazz = Class.forName(entityPkgPrefix + fks.getPkTableName());
				builder = builder.defineField(fks.getPkTableName(), clazz).annotateField(
						AnnotationDescription.Builder.ofType(jakarta.persistence.JoinColumn.class)
								.define("name", fks.getFkColumnName())
								.define("nullable", false)
								.define("referencedColumnName", fks.getPkColumnName()).build(),

						AnnotationDescription.Builder.ofType(jakarta.persistence.ManyToOne.class)
						.define("fetch", FetchType.EAGER)
						// .define("cascade", CascadeType.REFRESH)
						.define("optional", false).build());
			} else {
				builder = builder.defineField(e.getKey(), e.getValue())
						.annotateField(AnnotationDescription.Builder.ofType(jakarta.persistence.Column.class)
																
								.build());
			}
		}
//		.define("nullable", 
//				(mapColumns.containsKey(e.getKey()) && mapColumns.get(e.getKey()).getIsNullable().equals("YES"))? true: false)
		if (tables.getPrimaryKeys().size() > 0) {
			PrimaryKeys pks = tables.getPrimaryKeys().get(0);
			Class clazz = fields.get(pks.getPrimaryKeyColName());
			builder = builder.defineField(pks.getPrimaryKeyColName(), clazz).annotateField(
					AnnotationDescription.Builder.ofType(jakarta.persistence.Id.class).build(),
					AnnotationDescription.Builder.ofType(jakarta.persistence.Column.class).build(),
					AnnotationDescription.Builder.ofType(jakarta.persistence.GeneratedValue.class).build());
		}
		// Load the entity
		Unloaded<?> generatedClass = builder.name(className).make();
		generatedClass.load(IMigrateEntityCreationServiceImpl.class.getClassLoader(),
				ClassLoadingStrategy.Default.INJECTION);

		try {
			Class<?> cls = Class.forName(className);
			return cls;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Builder<DynamicEntity> getTableMetadataWithIndexed(Tables tables, String indexes) {
		//		AnnotationDescription idxAn = AnnotationDescription.Builder.ofType(jakarta.persistence.Index.class)
		//		.define("columnList", indexes).build();
		var builder = new ByteBuddy().subclass(DynamicEntity.class).annotateType(
				AnnotationDescription.Builder.ofType(jakarta.persistence.Entity.class).build(),
				AnnotationDescription.Builder.ofType(jakarta.persistence.Table.class)
				.define("name", tables.getTableName())
				.defineAnnotationArray("indexes", jakarta.persistence.Index.class, new jakarta.persistence.Index[] {
						new jakarta.persistence.Index() {
							@Override
							public String name() {
								return tables.getIndexes().get(1).getIndexName();
							}

							@Override
							public Class<? extends Annotation> annotationType() {
								// TODO Auto-generated method stub
								return jakarta.persistence.Index.class;
							}

							@Override
							public String columnList() {
								// TODO Auto-generated method stub
								return indexes;
							}

							@Override
							public boolean unique() {
								// TODO Auto-generated method stub
								return false;
							}
						}
				})
				.build());
		return builder;

		//@Table(indexes = @Index(columnList = "firstName"))
	}


	private Builder<DynamicEntity> getTableMetaData(Tables tables) {
		Builder<DynamicEntity> builder = new ByteBuddy().subclass(DynamicEntity.class).annotateType(
				AnnotationDescription.Builder.ofType(jakarta.persistence.Entity.class).build(),
				AnnotationDescription.Builder.ofType(jakarta.persistence.Table.class)
				.define("name", tables.getTableName()).build());
		return builder;
	}
}
