package com.gta.scpoa.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gta.scpoa.entity.TabAttachInfo;
import com.gta.scpoa.entity.TableInfor;

/**
 * 表单解析的工具类
 * 
 * @author xiaojie.cai
 */
public class ExplainTableUtil {
	
	/**
	 * 获取中总表的数据
	 * @param jsonString
	 * @return
	 */
	public static List<TableInfor> getAllTable(String jsonString){
//		List<TableInfor> allLists = getAllMainInfor(jsonString);
		List<TableInfor> allLists = getMainTable(jsonString);
		if(allLists.size() == 0 ) return allLists;
		/*获取子表的所有的表名的ID*/
		List<String> childTableTitleIds = getClildTableId(jsonString);
		int size = childTableTitleIds.size();
		if(size == 0) return allLists;
		/*根据表的名字的ID获取  表的名字  和表的结构*/
		for(int i = 0 ; i <size ; i ++){
			String id  = childTableTitleIds.get(i);
//			TableInfor titleTableInfor = getChildTableTittle(id, jsonString);
			List<TableInfor> tempLists = getChildStructure(id, jsonString);
			tempLists = getChildrenValue(id, tempLists, jsonString);
			/*将表名和表的结构先后加入到总表中*/
			if(tempLists.size() == 0) continue;
//			allLists.add(titleTableInfor);
			allLists.addAll(tempLists);
		}
		return allLists;
	}
	
	
	/**
	 * 获取主表单的结构 
	 * 
	 **/
	private static List<TableInfor> getMainStructure(String jsonString){
		List<TableInfor> listTableInfors = new ArrayList<TableInfor>();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = JSON.parseObject(jsonString);
			if (jsonObject != null) {
				if (jsonObject.containsKey("structure")) {
					JSONObject structureJsonObject = jsonObject
							.getJSONObject("structure");
					if (structureJsonObject.containsKey("main")) {
						JSONArray mainArray = structureJsonObject
								.getJSONArray("main");
						for(int i = 0 ; i <mainArray.size() ; i ++){
							TableInfor tableInfor = new TableInfor();
							JSONObject tempObject = mainArray.getJSONObject(i);
							if (tempObject.containsKey("name")
									&& tempObject
											.containsKey("type")
									&& tempObject
											.containsKey("key")) {
								
								tableInfor.setKey(tempObject.getString("name")); // 字段
								tableInfor.setType(tempObject.getIntValue("type")); // 类型
								tableInfor.setKeyId(tempObject.getString("key"));
								listTableInfors.add(tableInfor);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			listTableInfors.clear();
		}
		return listTableInfors;
	}
	
	/**
	 * 
	 * 根据结构获取值
	 * 
	 **/
	private static List<TableInfor> getMainTable(String jsonString){
		/*先获取表结构的数据*/
		List<TableInfor> listTableInfors = getMainStructure(jsonString);
		if(listTableInfors.size() == 0) return listTableInfors;
		List<TableInfor> tempTableInfors = new ArrayList<TableInfor>();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = JSON.parseObject(jsonString);
			if (jsonObject != null) {
				if (jsonObject.containsKey("data")) {
					JSONObject dataJsonObject = jsonObject
							.getJSONObject("data");
					if (dataJsonObject.containsKey("main")) {
						JSONObject mainJsonObject = dataJsonObject
								.getJSONObject("main");
						Set<String> jsonKeyString = mainJsonObject.keySet();
						for(TableInfor tableInfor:listTableInfors){
							String keyId = tableInfor.getKeyId();
							for (Iterator<String> iterator = jsonKeyString
									.iterator(); iterator.hasNext();) {
								String name = (String) iterator.next();
								if(name.equals(keyId)){
									String valueString = mainJsonObject.getString(keyId);
									tableInfor.setValue(valueString);
									tempTableInfors.add(tableInfor);
									break;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			tempTableInfors.clear();
			return tempTableInfors;
		}
		return tempTableInfors;
	}
	
	
	/**
	 * 
	 * 获取子表的结构
	 * 
	 */
	private static List<TableInfor> getChildStructure(String id,String jsonString){
		List<TableInfor> listTableInfors = new ArrayList<TableInfor>();
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = JSON.parseObject(jsonString);
			if (jsonObject != null) {
				if (jsonObject.containsKey("structure")) {
					JSONObject structureJsonObject = jsonObject
							.getJSONObject("structure");
					if(structureJsonObject.containsKey("sub")){
						JSONArray subJsonArray = structureJsonObject.getJSONArray("sub");
							for(int i = 0 ; i < subJsonArray.size() ; i ++){
								JSONObject childJsonObject = subJsonArray.getJSONObject(i);
								if(childJsonObject.containsKey("key")){
									if(childJsonObject.getString("key").equals(id)){
										if(childJsonObject.containsKey("field")){
											JSONArray fieldJsonArray = childJsonObject.getJSONArray("field");
											if(fieldJsonArray.size() == 0){
												listTableInfors.clear();
												return listTableInfors;
											}
											for(int j = 0 ; j < fieldJsonArray.size() ;  j ++){
												JSONObject tempObject = fieldJsonArray.getJSONObject(j);
												TableInfor tableInfor = new TableInfor();
													if (tempObject.containsKey("name")
															&& tempObject
																	.containsKey("type")
															&& tempObject
																	.containsKey("key")) {
														
														tableInfor.setKey(tempObject.getString("name")); // 字段
														tableInfor.setType(tempObject.getIntValue("type")); // 类型
														tableInfor.setKeyId(tempObject.getString("key"));
														listTableInfors.add(tableInfor);
													}
											}
										}
										break;
									}
								}
							}
					}
				}
			}
		} catch (Exception e) {
			listTableInfors.clear();
			return listTableInfors;
		}
		
		return listTableInfors;
	}
	
	
	/**
	 * 
	 * 获取子表的字段的值   根据子表的结构  和  子表的名字的ID
	 * 
	 */
	private static List<TableInfor> getChildrenValue(String id,List<TableInfor> structureList,String jsonString){
		
		List<TableInfor> listTableInfors = new ArrayList<TableInfor>();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = JSON.parseObject(jsonString);
			if (jsonObject != null) {
				if (jsonObject.containsKey("data")) {
					JSONObject dataJsonObject = jsonObject
							.getJSONObject("data");
					if (dataJsonObject.containsKey("sub")) {
						JSONArray subJsonArray = dataJsonObject.getJSONArray("sub");
						for(int i = 0 ; i < subJsonArray.size(); i ++){
							JSONObject childJsonObject = subJsonArray.getJSONObject(i);
							if(childJsonObject.containsKey("tableName")){
								/*获取到表名的ID*/
								String tableID = childJsonObject.getString("tableName");
								if(tableID.equals(id)){
									TableInfor titleTableInfor = getChildTableTittle(id, jsonString);
									if(childJsonObject.containsKey("dataList")){
										JSONArray dataListJsonArray = childJsonObject.getJSONArray("dataList");
										for(int j=0 ; j < dataListJsonArray.size() ; j ++){
											/*添加表单头*/
											boolean canAdd = false;
											TableInfor titleTempTableInfor = new TableInfor();
											titleTempTableInfor.setKey(titleTableInfor.getKey());
											titleTempTableInfor.setKeyId(titleTableInfor.getKeyId());
											titleTempTableInfor.setTitle(titleTableInfor.isTitle());
											titleTempTableInfor.setType(titleTableInfor.getType());
											titleTempTableInfor.setValue(titleTableInfor.getValue());
											listTableInfors.add(titleTableInfor);
											/********************************/
											JSONObject nameIdJObject = dataListJsonArray.getJSONObject(j);
											for(TableInfor tableInfor : structureList){
												TableInfor tableInfor2 = new TableInfor();
												tableInfor2.setKey(tableInfor.getKey());
												tableInfor2.setKeyId(tableInfor.getKeyId());
												tableInfor2.setTitle(tableInfor.isTitle());
												tableInfor2.setType(tableInfor.getType());
												String keyID = tableInfor.getKeyId();
												if(nameIdJObject.containsKey(keyID)){
													tableInfor2.setValue(nameIdJObject.getString(keyID));
													listTableInfors.add(tableInfor2);
													canAdd = true;
												}
											}
											/*去掉表单头*/
											if(!canAdd){
												listTableInfors.remove(listTableInfors.size()-1);
												canAdd = false;
											}
											
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			listTableInfors.clear();
			return listTableInfors;
		}
		return listTableInfors;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	

	
	/**
	 * 获取子表的名字的ID
	 * @param jsonString
	 * @return
	 */
	private static List<String> getClildTableId(String jsonString){
		List<String> childTableIdList = new ArrayList<String>();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = JSON.parseObject(jsonString);
			if (jsonObject != null) {
				if (jsonObject.containsKey("data")) {
					JSONObject dataJsonObject = jsonObject
							.getJSONObject("data");
					if (dataJsonObject.containsKey("sub")) {
						JSONArray subJsonArray = dataJsonObject.getJSONArray("sub");
						for(int i = 0 ; i < subJsonArray.size(); i ++){
							JSONObject childJsonObject = subJsonArray.getJSONObject(i);
							if(childJsonObject.containsKey("tableName")){
								childTableIdList.add(childJsonObject.getString("tableName"));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			childTableIdList.clear();
			return childTableIdList;
		}
		return childTableIdList;
	}
	
	/**
	 * 根据子表名的ID获取子表名
	 * @param childTableIds
	 * @param jsonString
	 * @return
	 */
	private static TableInfor getChildTableTittle(String id,String jsonString){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = JSON.parseObject(jsonString);
			if (jsonObject != null) {
				if (jsonObject.containsKey("structure")) {
					JSONObject structureJsonObject = jsonObject
							.getJSONObject("structure");
					if(structureJsonObject.containsKey("sub")){
						JSONArray subJsonArray = structureJsonObject.getJSONArray("sub");
							for(int i = 0 ; i < subJsonArray.size() ; i ++){
								JSONObject childJsonObject = subJsonArray.getJSONObject(i);
								if(childJsonObject.containsKey("key")){
									if(childJsonObject.getString("key").equals(id)){
										if(childJsonObject.containsKey("name")){
											TableInfor tableInfor = new TableInfor();
											tableInfor.setValue(childJsonObject.getString("name"));
											tableInfor.setTitle(true);
											return tableInfor;
										}
									}
								}
							}
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	
	
	
	
	/**
	 * 附件的解析
	 */
	public static List<TabAttachInfo> getTabFJ(String jsonArrayString){
		List<TabAttachInfo> list = new ArrayList<TabAttachInfo>();
		try {
			JSONArray jsonArray = JSON.parseArray(jsonArrayString);
			for(int i = 0 ; i < jsonArray.size() ; i ++){
				TabAttachInfo tabAttachInfo = new TabAttachInfo();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if(jsonObject.containsKey("id")){
					tabAttachInfo.setId(jsonObject.getString("id"));
				}
				
				if(jsonObject.containsKey("name")){
					tabAttachInfo.setName(jsonObject.getString("name"));
				}
				
				if(jsonObject.containsKey("size")){
					tabAttachInfo.setSize(jsonObject.getLong("size"));
				}
				
				list.add(tabAttachInfo);
			}
		} catch (Exception e) {
			list.clear();
		}
		return list;
	}
	
	
	/*获取正文附件*/
	public static List<TabAttachInfo> getTabZWFJ(String jsonArrayString){
		List<TabAttachInfo> list = new ArrayList<TabAttachInfo>();
		try {
			JSONObject jsonObject = (JSONObject)JSON.parse(jsonArrayString);
				TabAttachInfo tabAttachInfo = new TabAttachInfo();
				String id  = "";
				if(jsonObject.containsKey("id")){
					id = jsonObject.getString("id");
					tabAttachInfo.setId(id);
					tabAttachInfo.setName(id+".doc");
				}
				if(jsonObject.containsKey("size")){
					tabAttachInfo.setSize(jsonObject.getLong("size"));
				}
				list.add(tabAttachInfo);
		} catch (Exception e) {
			list.clear();
		}
		return list;
	}
}

