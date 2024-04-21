package com.iMigrate.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Configuration;
import com.iMigrate.entity.DynamicEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

/**
 * @author Kansal Nishant
 *
 */
@Configuration
public class IMigrateEntityManagerConfig {
	
	public static EntityManager em = null;
	
	public void createEntityManger(List<DynamicEntity> fullClassNames) {
		if(em == null) {
			em = getEntityManagerFactory(fullClassNames);	
			//callEmCalls();
		}
	}

	/**
	 * 
	 */
	public void callEmCalls() {
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
	}
	
	public void persistEntities(List<DynamicEntity> listEntities) {
		em.getTransaction().begin();
		em.flush();
		for (DynamicEntity dynamicEntity : listEntities) {
			em.merge(dynamicEntity);
		}em.getTransaction().commit();
	}

	/**
	 * @param entities
	 * @return
	 */
	public EntityManager getEntityManagerFactory(Object... entities) {
		Map<String, Object> properties = new HashMap<>();
		properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
		properties.put("hibernate.connection.username", "postgres");
		properties.put("hibernate.connection.password", "Root123$");
		properties.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/iMigratedb");
		properties.put("hibernate.hbm2ddl.auto", "create");
		properties.put("hibernate.show_sql", true);
		EntityManagerFactory entityManagerFactory = new HibernatePersistenceProvider()
				.createContainerEntityManagerFactory(dynamicJpa(entities), properties);
		return entityManagerFactory.createEntityManager();

	}

	/**
	 * @param entities
	 * @return
	 */
	private static PersistenceUnitInfo dynamicJpa(Object... entities) {
		return new PersistenceUnitInfo() {
			@Override
			public String getPersistenceUnitName() {
				return "iMigrate-dynamic-jpa";
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<String> getManagedClassNames() {
				List<String> list = new ArrayList<>();
				for (Object entity : entities) {
					if(entity.getClass().getName().equals("java.util.ArrayList")) {
						ArrayList<Object> listArr = (ArrayList<Object>)entity;
						for (Object object : listArr) {
							list.add(object.getClass().getName());
						}
					}else {
						list.add(entity.getClass().getName());
					}
				}
				return list;
			}

			@Override
			public String getPersistenceProviderClassName() {
				return "org.hibernate.jpa.HibernatePersistenceProvider";
			}

			@Override
			public PersistenceUnitTransactionType getTransactionType() {
				return PersistenceUnitTransactionType.RESOURCE_LOCAL;
			}

			@Override
			public DataSource getJtaDataSource() {
				return null;
			}

			@Override
			public DataSource getNonJtaDataSource() {
				return null;
			}

			@Override
			public List<String> getMappingFileNames() {
				return Collections.emptyList();
			}

			@Override
			public List<URL> getJarFileUrls() {
				try {
					return Collections.list(this.getClass().getClassLoader().getResources(""));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public URL getPersistenceUnitRootUrl() {
				return null;
			}

			@Override
			public boolean excludeUnlistedClasses() {
				return false;
			}

			@Override
			public SharedCacheMode getSharedCacheMode() {
				return null;
			}

			@Override
			public ValidationMode getValidationMode() {
				return null;
			}

			@Override
			public Properties getProperties() {
				return new Properties();
			}

			@Override
			public String getPersistenceXMLSchemaVersion() {
				return null;
			}

			@Override
			public ClassLoader getClassLoader() {
				return null;
			}

			@Override
			public void addTransformer(ClassTransformer transformer) {

			}

			@Override
			public ClassLoader getNewTempClassLoader() {
				return null;
			}
		};
	}

}
