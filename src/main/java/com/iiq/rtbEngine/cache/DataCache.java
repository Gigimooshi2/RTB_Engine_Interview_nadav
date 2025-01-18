package com.iiq.rtbEngine.cache;

import com.iiq.rtbEngine.db.DbManager;
import com.iiq.rtbEngine.db.dao.ProfileCampaignFrequencyDao;
import com.iiq.rtbEngine.db.dao.ProfilesDao;
import com.iiq.rtbEngine.db.models.Campaign;
import com.iiq.rtbEngine.db.models.CampaignConfig;
import com.iiq.rtbEngine.db.models.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DataCache {


    private final DbManager dbManager;
    private final Map<Integer, Campaign> campaignIDtoCampaign;

    private final HashSet<Campaign> allCampaigns = new HashSet<>();

    private final Map<Integer, CampaignConfig> campaignConfig;

    public DataCache(DbManager dbManager) {
        this.dbManager = dbManager;
        this.campaignConfig = new ConcurrentHashMap<>();
        this.campaignIDtoCampaign = new ConcurrentHashMap<>();
        loadCampaignConfigurations();
        loadCampaignsData();

    }

    private void loadCampaignsData() {

        Map<Integer, List<Integer>> campaignAttributes = dbManager.getAllCampaignAttributes();
        for (Map.Entry<Integer, List<Integer>> entry : campaignAttributes.entrySet()) {
            int campaignId = entry.getKey();
            List<Integer> attributes = entry.getValue();
            CampaignConfig config = campaignConfig.get(entry.getKey());
            Campaign campaign = new Campaign(campaignId,config.capacity() , config.priority(),attributes);
            campaignIDtoCampaign.put(campaignId, campaign);
            allCampaigns.add(campaign);
        }
    }

    public Campaign getCampaignByID(int campaignID) {
        return campaignIDtoCampaign.get(campaignID);
    }

    public Set<Campaign> getAllCampaignsView(){
        return  new HashSet<>(allCampaigns);
    }


    private void loadCampaignConfigurations() {
        Map<Integer, CampaignConfig> configs = dbManager.getAllCampaignsConfigs();
        campaignConfig.putAll(configs);
    }

    public CampaignConfig getCampaignConfig(Integer campaignID){
       return  campaignConfig.get(campaignID);
    }




}
