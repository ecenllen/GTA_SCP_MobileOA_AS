package com.gta.db.helper;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.gta.db.annotation.Column;
import com.gta.db.annotation.Id;
import com.gta.db.annotation.Table;

public class DBHelper {
	public static <T> void createTableByclass(SQLiteDatabase db,
			Class<?>[] clazzs) {
		for (Class<?> clazz : clazzs) {
			createTable(db, clazz);
		}
	}

	public static <T> void createTable(SQLiteDatabase db, Class<T> clazz) {
		String tableName = "";
		if (clazz.isAnnotationPresent(Table.class)) {
			Table table = clazz.getAnnotation(Table.class);
			tableName = table.getTableName();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(" CREATE TABLE IF NOT EXISTS ").append(tableName).append("(");
		List<Field> fields = joinField(clazz.getDeclaredFields(), clazz
				.getSuperclass().getDeclaredFields());
		for (Field field : fields) {
			if (!field.isAnnotationPresent(Column.class)) {
				continue;
			}
			Column column = field.getAnnotation(Column.class);
			String columnType = "";
			if (column.type().equals("")) {
				columnType = getColumnType(field.getType());
			} else {
				columnType = column.type();
			}
			if (column.name().equalsIgnoreCase("Id")) {
				sb.append("_id" + "  " + columnType);
			} else {
				sb.append(column.name() + "  " + columnType);
			}
			if (column.getLength() != 0) {
				sb.append(" (" + column.getLength() + ") ");
			}
			if (field.isAnnotationPresent(Id.class)) {
				Id id = field.getAnnotation(Id.class);
				if (id.generator().equals("AUTOINCREMENT")) {
					sb.append(" PRIMARY KEY AUTOINCREMENT");
				} else {
					sb.append(" PRIMARY KEY");
				}
			}
			sb.append(", ");
		}

		sb.delete(sb.length() - 2, sb.length() - 1);
		sb.append(");");
		// Log.i("info", sb.toString());
		db.execSQL(sb.toString());
	}

	public static List<Field> joinField(Field[] fields1, Field[] fields2) {
		Map<String, Field> map = new LinkedHashMap<String, Field>();
		for (Field field : fields1) {
			if (!field.isAnnotationPresent(Column.class)) {
				continue;
			}
			Column column = field.getAnnotation(Column.class);
			map.put(column.name(), field);
		}
		for (Field field : fields2) {
			if (!field.isAnnotationPresent(Column.class)) {
				continue;
			}
			Column column = field.getAnnotation(Column.class);
			map.put(column.name(), field);
		}
		List<Field> list = new ArrayList<Field>();
		for (String key : map.keySet()) {
			Field value = map.get(key);
			if (value.isAnnotationPresent(Id.class)) {
				list.add(0, value);
			} else {
				list.add(value);
			}
		}
		return list;
	}

	public static String getColumnType(Class<?> fieldType) {
		if (String.class.equals(fieldType)) {
			return "TEXT";
		}
		if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
			return "INTEGER";
		}
		if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
			return "REAL";
		}
		if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
			return "INTEGER";
		}
		if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
			return "INTEGER";
		}
		if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
			return "REAL";
		}
		if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
			return "TEXT";
		}
		if (Byte.class.equals(fieldType) || Byte.TYPE.equals(fieldType)) {
			return "INTEGER";
		}
		if (Blob.class.equals(fieldType)) {
			return "BLOB";
		}
		if (Date.class.equals(fieldType)) {
			return "INTEGER";
		}

		return "TEXT";

	}

	@SuppressLint("SimpleDateFormat")
	public static <T> ContentValues createContentValues(T entity,
			List<Field> allFields) throws IllegalArgumentException,
			IllegalAccessException {
		ContentValues values = new ContentValues();
		for (Field field : allFields) {
			if (!field.isAnnotationPresent(Column.class)) {
				continue;
			}
			if (field.isAnnotationPresent(Id.class)) {
				Id id = field.getAnnotation(Id.class);
				if (id.generator().equals("AUTOINCREMENT")) {

					continue;
				}
			}
			Column column = field.getAnnotation(Column.class);
			field.setAccessible(true);
			Class<?> fieldType = field.getType();
			Object fieldValue = field.get(entity);

			if (fieldValue == null) {
				// continue;
			}
			if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {
				values.put(column.name(), (Integer) fieldValue);
			} else if (String.class == fieldType) {
				values.put(column.name(), (String) fieldValue);
			} else if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {
				values.put(column.name(), (Long) fieldValue);
			} else if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {
				values.put(column.name(), (Float) fieldValue);
			} else if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {
				values.put(column.name(), (Short) fieldValue);
			} else if ((Double.TYPE == fieldType)
					|| (Double.class == fieldType)) {
				values.put(column.name(), (Double) fieldValue);
			} else if (byte[].class == fieldType) {
				if (fieldValue == null)
					values.put(column.name(), (byte[]) null);
				else
					values.put(column.name(), (byte[]) fieldValue);
			} else if (Date.class == fieldType) {
				if (fieldValue == null)
					values.put(column.name(), (Integer) null);
				else
					values.put(column.name(), ((Date) fieldValue).getTime());
			} else if ((Boolean.TYPE == fieldType || Boolean.class == fieldType)) {
				values.put(column.name(), fieldValue.toString());
			} else if ((Byte.TYPE == fieldType || byte.class == fieldType)) {
				values.put(column.name(), (Byte) fieldValue);
			} else if ((Character.TYPE == fieldType || char.class == fieldType)) {
				values.put(column.name(), String.valueOf(fieldValue));
			} else {
				values.put(column.name(), fieldValue.toString());
			}
		}
		return values;

	}

}
