package com.iMigrate.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * @author Kansal Nishant
 *
 */
@AllArgsConstructor
@Builder(toBuilder=true)
public class DynamicEntity {

	public void set(String key, Object value) {
		try {
			java.lang.reflect.Field field = this.getClass().getDeclaredField(key);
			field.setAccessible(true);
			field.set(this, value);
			field.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object get(String key) {
		try {
			java.lang.reflect.Field field = this.getClass().getDeclaredField(key);
			field.setAccessible(true);
			Object value = field.get(this);
			field.setAccessible(false);
			return value;
		} catch (Exception e) {
		}
		return null;
	}
}
