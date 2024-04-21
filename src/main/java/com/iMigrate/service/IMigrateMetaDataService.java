package com.iMigrate.service;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Kansal Nishant
 *
 */
public interface IMigrateMetaDataService {

	public void getDatabaseTableSchemaMetadata() throws InstantiationException, IllegalAccessException, 
	ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;
	
	

}
