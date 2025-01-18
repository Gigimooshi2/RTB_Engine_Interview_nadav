package com.iiq.rtbEngine.db.dao;

import com.iiq.rtbEngine.db.H2DB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Deprecated
@Component
public class ProfileCampaignFrequencyDao {
    @Autowired
    private H2DB h2Db;
    private static final Log logger = LogFactory.getLog(ProfileCampaignFrequencyDao.class);
    private static final String PROFILE_CAMPAIGN_FREQUENCY_TABLE_NAME = "profile_campaign_frequency";
    private static final String PROFILE_ID_COLUMN = "profile_id";
    private static final String CAMPAIGN_ID_COLUMN = "campaign_id";
    private static final String FREQUENCY_COLUMN = "frequency";
    private static final String CREATE_PROFILE_CAMPAIGN_FREQUENCY_TABLE = "CREATE TABLE " + PROFILE_CAMPAIGN_FREQUENCY_TABLE_NAME +
            "(" + PROFILE_ID_COLUMN + " INTEGER not NULL, " +
            " " + CAMPAIGN_ID_COLUMN + " INTEGER not NULL, " +
            " " + FREQUENCY_COLUMN + " INTEGER, " +
            " PRIMARY KEY (" + PROFILE_ID_COLUMN + ", " + CAMPAIGN_ID_COLUMN + "))";
    private static final String INSERT_STATEMENT =
            "MERGE INTO " + PROFILE_CAMPAIGN_FREQUENCY_TABLE_NAME + " AS target " +
                    "USING (VALUES (%d, %d, %d)) AS source (" + PROFILE_ID_COLUMN + ", " + CAMPAIGN_ID_COLUMN + ", " + FREQUENCY_COLUMN + ") " +
                    "ON (target." + PROFILE_ID_COLUMN + " = source." + PROFILE_ID_COLUMN + " " +
                    "    AND target." + CAMPAIGN_ID_COLUMN + " = source." + CAMPAIGN_ID_COLUMN + ") " +
                    "WHEN MATCHED THEN " +
                    "    UPDATE SET " + FREQUENCY_COLUMN + " = source." + FREQUENCY_COLUMN + " " +
                    "WHEN NOT MATCHED THEN " +
                    "    INSERT (" + PROFILE_ID_COLUMN + ", " + CAMPAIGN_ID_COLUMN + ", " + FREQUENCY_COLUMN + ") " +
                    "    VALUES (source." + PROFILE_ID_COLUMN + ", source." + CAMPAIGN_ID_COLUMN + ", source." + FREQUENCY_COLUMN + ");";

    private static final String SELECT_FREQUENCY_STATEMENT = "SELECT " + FREQUENCY_COLUMN + " FROM " +
            PROFILE_CAMPAIGN_FREQUENCY_TABLE_NAME + " WHERE " + PROFILE_ID_COLUMN + " = %d AND " +
            CAMPAIGN_ID_COLUMN + " = %d";

    public void createTable() {
        try {
            h2Db.executeUpdate(CREATE_PROFILE_CAMPAIGN_FREQUENCY_TABLE);
            logger.info("Init capacity table");
        } catch (SQLException e) {
            logger.error("Error while trying to create table " + PROFILE_CAMPAIGN_FREQUENCY_TABLE_NAME, e);
        }
    }

    public void insertFrequency(int profileId, int campaignId, int frequency) {
        try {
            h2Db.executeUpdate(String.format(INSERT_STATEMENT, profileId, campaignId, frequency));
        } catch (SQLException e) {
            logger.error("Error while trying to insert capacity into table " + PROFILE_CAMPAIGN_FREQUENCY_TABLE_NAME, e);
        }
    }

    public Integer getFrequency(int profileId, int campaignId) {
        try {
            List<Map<String, String>> result = h2Db.executeQuery(String.format(SELECT_FREQUENCY_STATEMENT, profileId, campaignId), FREQUENCY_COLUMN);
            if (result == null || result.isEmpty()) {
                return null;
            }
            return Integer.parseInt(result.get(0).get(FREQUENCY_COLUMN));
        } catch (Exception e) {
            logger.error("Error while trying to retrieve capacity from table " + PROFILE_CAMPAIGN_FREQUENCY_TABLE_NAME, e);
        }
        return null;
    }
}
