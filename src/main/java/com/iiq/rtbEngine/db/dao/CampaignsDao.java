package com.iiq.rtbEngine.db.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class CampaignsDao {
	@Autowired
	private H2DB h2Db;

	private static final Log logger = LogFactory.getLog(CampaignsDao.class);
	private static final String CAMPAIGNS_TABLE_NAME = "campaigns";
	private static final String ATTRIBUTE_ID_COLUMN = "attribute_id";
	private static final String CAMPAIGN_ID_COLUMN = "campaign_id";
	private static final String CREATE_CAMPAIGNS_TABLE = "CREATE TABLE "+CAMPAIGNS_TABLE_NAME+ 
			"("+CAMPAIGN_ID_COLUMN  +" INTEGER not NULL, " + 
			    ATTRIBUTE_ID_COLUMN +" INTEGER, " +  
			" PRIMARY KEY ( "+CAMPAIGN_ID_COLUMN+ ", "+ATTRIBUTE_ID_COLUMN+"))";  
	private static final String UPDATE_STATMENT = "INSERT INTO "+CAMPAIGNS_TABLE_NAME+" VALUES (%s, %s)";
	private static final String SELECT_CAMPAIGN_STATEMENT = "SELECT * FROM "+CAMPAIGNS_TABLE_NAME+" where "+CAMPAIGN_ID_COLUMN+" = %s";
	private static final String SELECT_ALL_CAMPAIGNS_ATTRIBUTES_STATEMENT = "SELECT * FROM "+CAMPAIGNS_TABLE_NAME;
	
	public void createTable() {
		try {
			h2Db.executeUpdate(CREATE_CAMPAIGNS_TABLE);
		} catch (SQLException e) {
			System.out.println("Error while trying to create table "+CAMPAIGNS_TABLE_NAME);
			logger.error("",e);
		}
	}
	
	public void updateTable(String campaignId, String attribute) {
		try {
			h2Db.executeUpdate(String.format(UPDATE_STATMENT, campaignId, attribute));
		} catch (SQLException e) {
			logger.error("Error while trying to update table "+CAMPAIGNS_TABLE_NAME+" with campaignId="+campaignId+" capacity"+attribute,e);
		}
	}
	
	public Set<Integer> getCampaignAttributes(int campaignId) {

		Set<Integer> attributes = new HashSet<>();
		try {
			List<Map<String, String>> result = h2Db.executeQuery(String.format(SELECT_CAMPAIGN_STATEMENT, campaignId+""), ATTRIBUTE_ID_COLUMN);
			if(result == null)
				return attributes;
		
			for(Map<String, String> row : result) {
				Integer attributeId = Integer.parseInt(row.get(ATTRIBUTE_ID_COLUMN));
				attributes.add(attributeId);
			}
		} catch (Exception e) {
			logger.error("Error while trying to get campaigns attributes from table for campaign "+campaignId,e);
		}
		
		return attributes;
	}

	public Map<Integer, List<Integer>> getAllCampaignAttributes() {
		Map<Integer, List<Integer>> campaignToAttributesMap = new HashMap<>();
		try {
			List<Map<String, String>> result = h2Db.executeQuery(SELECT_ALL_CAMPAIGNS_ATTRIBUTES_STATEMENT, ATTRIBUTE_ID_COLUMN, CAMPAIGN_ID_COLUMN);
			if(result == null)
				return campaignToAttributesMap;
			
			for(Map<String, String> row : result) {
				Integer campaignId = Integer.parseInt(row.get(CAMPAIGN_ID_COLUMN));
				Integer attributeId = Integer.parseInt(row.get(ATTRIBUTE_ID_COLUMN));
                List<Integer> campaignAttributes = campaignToAttributesMap.computeIfAbsent(campaignId, k -> new ArrayList<>());
                campaignAttributes.add(attributeId);
			}
		} catch (Exception e) {
			logger.error("Error while trying to get all campaigns attributes from table",e);
		}
		
		return campaignToAttributesMap;
	}
}
