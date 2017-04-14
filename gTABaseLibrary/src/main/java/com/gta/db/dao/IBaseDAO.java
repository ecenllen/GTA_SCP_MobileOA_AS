package com.gta.db.dao;

import java.io.Serializable;
import java.util.List;

import android.content.ContentValues;

/**
 * DB操作类，实体操作根据实体的主键id判断
 * 
 * @author qinghua.liu
 * 
 * @param <T>
 */
public interface IBaseDAO<T extends Serializable> {
	/**
	 * 插入数据实体
	 * 
	 * @param entity
	 * @return 返回插入的行号id
	 */
	long insert(T entity);

	/**
	 * 插入实体集合
	 * 
	 * @param entity
	 * @return 返回最后行号id
	 */
	long insert(List<T> entity);

	/**
	 * 插入或者更新（实体存在则插入）
	 * 
	 * @param selection
	 * @param selectionArgs
	 * @param entity
	 * @return 返回操作成功的id
	 */
	long insertOrUpdata(String selection, String[] selectionArgs, T entity);

	/**
	 * 插入或者更新（实体存在则插入）
	 * 
	 * @param entity
	 * @return
	 */
	long insertOrUpdataById(T entity);

	/**
	 * 删除实体对象，根据实体主键id
	 * 
	 * @param entity
	 */
	void deleteById(T entity);

	void delete(String whereClause, String[] whereArgs);
	
	void deleteAll();

	/**
	 * 更新实体对象，根据实体主键id
	 * 
	 * @param entity
	 * @return
	 */
	int updateById(T entity);

	int update(ContentValues values, String whereClause, String[] whereArgs);

	/**
	 * 查询实体对象，根据实体主键id
	 * 
	 * @param entity
	 * @return
	 */
	T queryById(T entity);

	List<T> query(String[] columns, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy, String limit);

	List<T> query(String selection, String[] selectionArgs);

	/**
	 * 查询实体集合
	 * 
	 * @return
	 */
	List<T> queryAll();

	T isExist(String selection, String[] selectionArgs);
}
