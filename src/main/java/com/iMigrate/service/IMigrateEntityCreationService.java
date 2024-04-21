package com.iMigrate.service;

import java.lang.reflect.InvocationTargetException;
import com.iMigrate.entity.DynamicEntity;
import com.iMigrate.models.Tables;

public interface IMigrateEntityCreationService {
	
	public DynamicEntity getJPAEntity(Tables tables)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException;

}
