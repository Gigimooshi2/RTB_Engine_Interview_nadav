package com.iiq.rtbEngine.db.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.iiq.rtbEngine.db.H2DB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfilesDao {
	@Autowired
	private H2DB h2Db;

	private final Log logger = LogFactory.getLog(ProfilesDao.class);
	private static final String PROFILES_TABLE_NAME = "profiles";
	private static final String ATTRIBUTE_ID_COLUMN = "attribute_id";
	private static final String PROFILE_ID_COLUMN = "profile_id";
	private static final String CREATE_PROFILES_TABLE = "CREATE TABLE "+PROFILES_TABLE_NAME+ 
			"("+PROFILE_ID_COLUMN+" INTEGER not NULL, " + 
			ATTRIBUTE_ID_COLUMN + " INTEGER, " +  
			" PRIMARY KEY ( "+PROFILE_ID_COLUMN+ ", "+ATTRIBUTE_ID_COLUMN+"))";  
	private static final String UPDATE_STATEMENT = "MERGE INTO "+PROFILES_TABLE_NAME+" KEY("+PROFILE_ID_COLUMN+", "+ATTRIBUTE_ID_COLUMN+") VALUES (%s, %s)";
	private static final String SELECT_STATEMENT = "SELECT * FROM "+PROFILES_TABLE_NAME+" where "+PROFILE_ID_COLUMN+" = %s";
	
	public void createTable() {
		try {
			h2Db.executeUpdate(CREATE_PROFILES_TABLE);
		} catch (Exception e) {
			logger.error("Error while trying to create table "+PROFILES_TABLE_NAME,e);
		}
	}
	
	public boolean updateTable(String profileId, String attributeId) {
		boolean updateFlag = false;
		try {
			int rowsUpdated = h2Db.executeUpdate(String.format(UPDATE_STATEMENT, profileId, attributeId));
			if (rowsUpdated > 0){
				updateFlag = true;
				logger.info("Profile ID "+profileId+" updated it's attribute to " +attributeId);
			}
		} catch (Exception e) {
			logger.error("Error while trying to update table "+PROFILES_TABLE_NAME+" with profileId="+profileId+" attributeId"+attributeId,e);
		}
		return updateFlag;
	}

	public List<Map<String, String>> getProfile(int profileId) {
		List<Map<String, String>> result = List.of();
		try {
			result = h2Db.executeQuery(String.format(SELECT_STATEMENT, profileId+""));
		} catch (Exception e) {
			logger.error("Got exception on : getProfile" , e );
		}

		return result;
	}
	
	public Set<Integer> getProfileAttributes(int profileId) {

		Set<Integer> attributes = new HashSet<>();
		try {
			List<Map<String, String>> result = h2Db.executeQuery(String.format(SELECT_STATEMENT, profileId+""), ATTRIBUTE_ID_COLUMN);
			if(result == null)
				return attributes;
		
			for(Map<String, String> row : result) {
				Integer attributeId = Integer.parseInt(row.get(ATTRIBUTE_ID_COLUMN));
				attributes.add(attributeId);
			}
		} catch (Exception e) {
			logger.error("Got exception on : getProfileAttributes" , e );
		}
		
		return attributes;
	}
}
