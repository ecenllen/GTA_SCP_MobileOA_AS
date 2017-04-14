package com.gta.db.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gta.db.annotation.Column;
import com.gta.db.annotation.Id;
import com.gta.db.annotation.Table;
import com.gta.db.helper.DBHelper;
import com.gta.db.util.DBUtils;
import com.gta.db.util.EntityBuilder;

public abstract class BaseDAO<T extends Serializable> implements IBaseDAO<T> {
	protected static final int TYPE_CREATE = 0;
	protected static final int TYPE_UPDATE = 1;
	private SQLiteDatabase db;
	private SQLiteOpenHelper mHelper;
	private Class<T> clazz = null;
	private List<Field> allFields;
	protected String tableName;
	protected String idColumnName;
	protected boolean isAutoIncrement;// 主键是否自增长
	protected Field primaryKeyField;

	public BaseDAO(SQLiteOpenHelper mHelper) {
		ini(mHelper);
	}

	@SuppressWarnings("unchecked")
	private void ini(SQLiteOpenHelper mHelper) {
		this.mHelper = mHelper;
		this.clazz = ((Class<T>) ((java.lang.reflect.ParameterizedType) super
				.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
		if (this.clazz.isAnnotationPresent(Table.class)) {
			Table table = this.clazz.getAnnotation(Table.class);
			this.tableName = table.getTableName();
		}
		this.allFields = DBHelper.joinField(clazz.getDeclaredFields(), clazz
				.getSuperclass().getDeclaredFields());
		for (Field field : allFields) {
			if (field.isAnnotationPresent(Id.class)) {
				Column column = field.getAnnotation(Column.class);
				this.idColumnName = column.name();
				this.isAutoIncrement = DBUtils.isAutoIncrement(field);
				this.primaryKeyField = field;
				break;
			}
		}
	}

	@Override
	public long insert(T entity) {
		long row_id = -1L;
		if (null == entity)
			return row_id;
		SQLiteDatabase db = null;
		try {
			db = mHelper.getReadableDatabase();
			ContentValues values = DBHelper.createContentValues(entity,
					allFields);
			db.beginTransaction();
			row_id = db.insert(tableName, null, values);
			if (row_id != -1)
				DBUtils.setPrimaryKeyFieldValue(entity, primaryKeyField, row_id);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			close(db, null);
		}
		return row_id;
	}

	@Override
	public long insert(List<T> entity) {
		long row_id = -1L;
		if (null == entity)
			return row_id;
		SQLiteDatabase db = null;
		try {
			db = mHelper.getReadableDatabase();
			db.beginTransaction();
			for (T t : entity) {
				ContentValues values = DBHelper.createContentValues(t,
						allFields);
				row_id = db.insert(tableName, null, values);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			close(db, null);
		}
		return 0;
	}

	@Override
	public long insertOrUpdata(String selection, String[] selectionArgs,
			T entity) {
		long l = -1L;
		if (null == entity)
			return l;
		SQLiteDatabase db = null;
		try {
			db = mHelper.getReadableDatabase();
			T t = isExist(selection, selectionArgs);
			if (null == t)
				l = insert(entity);
			else
				l = updateById(t);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(db, null);
		}

		return l;
	}

	@Override
	public long insertOrUpdataById(T entity) {
		long i = -1;
		if (null == entity)
			return i;
		int idColumnValues = DBUtils.getPrimaryKeyValues(entity);
		String selection = idColumnName + "=?";
		String[] selectionArgs = new String[] { String.valueOf(idColumnValues) };
		i = insertOrUpdata(selection, selectionArgs, entity);
		return i;
	}

	@Override
	public void deleteById(T entity) {
		if (null == entity)
			return;
		int idColumnValues = DBUtils.getPrimaryKeyValues(entity);
		String where = idColumnName + "=?";
		delete(where, new String[] { String.valueOf(idColumnValues) });
	}

	@Override
	public void delete(String whereClause, String[] whereArgs) {
		if (null == whereClause || null == whereClause
				|| whereClause.length() == 0)
			return;
		SQLiteDatabase db = null;
		try {
			db = mHelper.getReadableDatabase();
			db.delete(tableName, whereClause, whereArgs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(db, null);
		}

	}

	@Override
	public void deleteAll() {
		String sql = "DELETE FROM "+tableName;
		SQLiteDatabase db = null;
		try {
			db = mHelper.getReadableDatabase();
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(db, null);
		}
	}

	@Override
	public int updateById(T entity) {
		int i = -1;
		SQLiteDatabase db = null;
		if (null == entity)
			return i;
		try {
			db = mHelper.getReadableDatabase();
			int idColumnValues = DBUtils.getPrimaryKeyValues(entity);
			String where = idColumnName + "=?";
			ContentValues values = DBHelper.createContentValues(entity,
					allFields);
			i = db.update(tableName, values, where,
					new String[] { String.valueOf(idColumnValues) });

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(db, null);
		}
		return i;
	}

	@Override
	public int update(ContentValues values, String whereClause,
			String[] whereArgs) {
		int i = -1;
		SQLiteDatabase db = null;
		if (null == values || null == whereClause || null == whereClause
				|| whereClause.length() == 0)
			return i;

		try {
			db = mHelper.getReadableDatabase();
			db.update(tableName, values, whereClause, whereArgs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(db, null);
		}
		return 0;
	}

	@Override
	public T queryById(T entity) {
		if (null == entity)
			return null;
		int idColumnValues = DBUtils.getPrimaryKeyValues(entity);
		String where = idColumnName + "=?";
		List<T> result = query(where,
				new String[] { String.valueOf(idColumnValues) });
		if (result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	@Override
	public List<T> query(String selection, String[] selectionArgs) {
		return query(null, selection, selectionArgs, null, null, null, null);
	}

	@Override
	public List<T> query(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		if (null == selection || null == selectionArgs
				|| selectionArgs.length == 0)
			return null;
		Cursor cursor = null;
		SQLiteDatabase db = null;
		try {
			db = mHelper.getReadableDatabase();
			cursor = db.query(tableName, columns, selection, selectionArgs,
					groupBy, having, orderBy, limit);
			return (List<T>) EntityBuilder.buildQueryList(clazz, cursor);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(db, cursor);
		}
		return null;
	}

	@Override
	public List<T> queryAll() {
		String sql = "select * from " + tableName;
		Cursor cursor = null;
		SQLiteDatabase db = null;
		try {
			db = mHelper.getReadableDatabase();
			cursor = db.rawQuery(sql, null);
			return ((List<T>) EntityBuilder.buildQueryList(clazz, cursor));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(db, cursor);
		}
		return null;
	}

	@Override
	public T isExist(String selection, String[] selectionArgs) {
		if (null == selection || null == selectionArgs
				|| selectionArgs.length == 0)
			return null;
		Cursor cursor = null;
		SQLiteDatabase db = null;
		try {
			db = mHelper.getReadableDatabase();
			cursor = db.query(tableName, null, selection, selectionArgs, null,
					null, null);
			List<T> result = EntityBuilder.buildQueryList(clazz, cursor);
			if (result.size() > 0) {
				return result.get(0);
			}
		} catch (Exception e) {
		} finally {

			close(db, cursor);
		}

		return null;
	}

	public void close(SQLiteDatabase db, Cursor cursor) {
		if (cursor != null)
			cursor.close();
		if (db != null)
			db.close();

	}

	public SQLiteDatabase getReadableDatabase() {
		db = mHelper.getReadableDatabase();
		return db;

	}

	public SQLiteDatabase getWritableDatabase() {
		db = mHelper.getWritableDatabase();
		return db;

	}

}
