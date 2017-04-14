/*
 * Copyright (C) 2013  WhiteCat 白猫 (www.thinkandroid.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gta.db.util;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gta.db.annotation.Column;
import com.gta.db.annotation.Id;

/**
 * @Title TASqlBuilder
 * @Package com.ta.util.db.util.sql
 * @Description 数据库的一些工具
 * @author 白猫
 * @date 2013-1-20
 * @version V1.0
 */
public class DBUtils {
	/**
	 * 通过Cursor获取一个实体数组
	 * 
	 * @param clazz
	 *            实体类型
	 * @param cursor
	 *            数据集合
	 * @return 相应实体List数组
	 */
	public static <T> List<T> getListEntity(Class<T> clazz, Cursor cursor) {
		List<T> queryList = EntityBuilder.buildQueryList(clazz, cursor);
		return queryList;
	}

	/**
	 * 检查是否是主键
	 * 
	 * @param field
	 * @return
	 */
	public static boolean isPrimaryKey(Field field) {
		return field.getAnnotation(Id.class) != null;
	}

	/**
	 * 返回主键名
	 * 
	 * @param clazz
	 *            实体类型
	 * @return
	 */
	public static String getPrimaryKeyFieldName(Class<?> clazz) {
		Field f = getPrimaryKeyField(clazz);
		if (null != f) {
			return f.getName();
		} else {
			throw new IllegalArgumentException("实体必须有一个主键Id字段");
		}
	}

	/**
	 * 返回主键值
	 * 
	 * @param <T>
	 * 
	 * @param clazz
	 *            实体类型
	 * @return
	 */
	public static <T> int getPrimaryKeyValues(T entity) {
		Field f = getPrimaryKeyField(entity.getClass());
		if (null != f) {
			f.setAccessible(true);
			try {
				return f.getInt(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return -1;
		} else {
			throw new IllegalArgumentException("实体必须有一个主键Id字段");
		}
	}

	/**
	 * 返回主键值
	 * 
	 * @param entity
	 *            实体
	 * @param f
	 *            主键field
	 * @return
	 */
	public static <T> int getPrimaryKeyValues(T entity, Field f) {
		if (null != f) {
			f.setAccessible(true);
			try {
				return f.getInt(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return -1;
		} else {
			throw new IllegalArgumentException("实体必须有一个主键Id字段");
		}
	}

	/**
	 * 返回主键字段
	 * 
	 * @param clazz
	 *            实体类型
	 * @return
	 */
	public static Field getPrimaryKeyField(Class<?> clazz) {
		Field primaryKeyField = null;
		Field[] fields = clazz.getDeclaredFields();
		if (fields != null) {

			for (Field field : fields) { // 获取ID注解
				if (field.getAnnotation(Id.class) != null) {
					primaryKeyField = field;
					break;
				}
			}
			// 没有ID注解，查找实体id或者_id属性字段
			if (primaryKeyField == null) {
				for (Field field : fields) {
					if ("_id".equals(field.getName())) {
						primaryKeyField = field;
						break;
					} else if ("id".equals(field.getName())) {
						primaryKeyField = field;
						break;
					}
				}

			}
		} else {
			throw new IllegalArgumentException("this model[" + clazz
					+ "] has no field");
		}
		return primaryKeyField;
	}

	/**
	 * 判断主键是否是自增长
	 * 
	 * @param clazz
	 * @return
	 */
	public static boolean isAutoIncrement(Field primaryKeyField) {
		// Field primaryKeyField = getPrimaryKeyField(clazz);
		if (primaryKeyField.isAnnotationPresent(Id.class)) {
			Id id = primaryKeyField.getAnnotation(Id.class);
			if (id.generator().equals("AUTOINCREMENT")) {
				return true;

			}
		}

		return false;

	}

	/**
	 * 设置主键ID的值
	 * 
	 * @param primaryKeyField
	 */
	public static void setPrimaryKeyFieldValue(Object entity,
			Field primaryKeyField, long id) {
		primaryKeyField.setAccessible(true);
		try {
			if (isAutoIncrement(primaryKeyField)
					&& (primaryKeyField.get(entity).equals(0) || primaryKeyField
							.get(entity).equals(0L))) {
				Class<?> fieldType = primaryKeyField.getType();
				if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
					primaryKeyField.setLong(entity, id);
				} else if (Integer.class.equals(fieldType)
						|| Integer.TYPE.equals(fieldType)) {
					primaryKeyField.setInt(entity, (int) id);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 是否为基本的数据类型
	 * 
	 * @param field
	 * @return
	 */
	public static boolean isBaseDateType(Field field) {
		Class<?> clazz = field.getType();
		return clazz.equals(String.class) || clazz.equals(Integer.class)
				|| clazz.equals(byte[].class) || clazz.equals(Long.class)
				|| clazz.equals(Double.class) || clazz.equals(Float.class)
				|| clazz.equals(Character.class) || clazz.equals(Short.class)
				|| clazz.equals(Boolean.class) || clazz.equals(Date.class)
				|| clazz.isPrimitive();
	}

	/**
	 * 获取字段名
	 * 
	 * @param field
	 * @return
	 */
	public static String getColumnByField(Field field) {
		Column column = field.getAnnotation(Column.class);
		if (column != null) {
			return column.name();
		}

		return null;

	}

	/**
	 * 查询自增长的最后一个ID
	 * 
	 * @param db
	 * @param tableName
	 * @return
	 */
	@SuppressWarnings("unused")
	private static long getLastAutoIncrementId(SQLiteDatabase db,
			String tableName) {
		long id = -1;
		try {
			Cursor cursor = db.rawQuery(
					"SELECT seq FROM sqlite_sequence WHERE name='" + tableName
							+ "'", null);
			if (cursor != null) {
				if (cursor.moveToNext()) {
					id = cursor.getLong(0);
				}
				cursor.close();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return id;
	}

}
