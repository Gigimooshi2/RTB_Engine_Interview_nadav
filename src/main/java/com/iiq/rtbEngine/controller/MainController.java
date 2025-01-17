package com.iiq.rtbEngine.controller;

import com.iiq.rtbEngine.cache.DataCache;
import com.iiq.rtbEngine.db.DbManager;
import com.iiq.rtbEngine.db.models.CampaignConfig;
import com.iiq.rtbEngine.db.models.Profile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@RestController
public class MainController {

    @Autowired
    private DataCache cache;

    @Autowired
    private DbManager dbManager;

    private static final String ATTRIBUTE_ID_VALUE = "atid";
    private static final String PROFILE_ID_VALUE = "pid";

    private enum UrlParam {
        ATTRIBUTE_ID(ATTRIBUTE_ID_VALUE),
        PROFILE_ID(PROFILE_ID_VALUE),
        ;

        private final String value;

        private UrlParam(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    @GetMapping("/attribute")
    public ResponseEntity<String> attributeRequest(HttpServletRequest request, HttpServletResponse response,
                                                   @RequestParam(name = ATTRIBUTE_ID_VALUE, required = true) Integer attributeId,
                                                   @RequestParam(name = PROFILE_ID_VALUE, required = true) Integer profileId) {
        boolean didProfileUpdate = dbManager.updateProfileAttribute(profileId, attributeId);

        if (didProfileUpdate) {
            return ResponseEntity.ok("Saved");
        } else {
            return ResponseEntity.status(500).body("Profile not saved");
        }

    }
    HashMap<Integer, Map<Integer, Integer>> campaignProfileCapacities = new HashMap<>();
    @GetMapping("/bid")
    public ResponseEntity<String> bidRequest(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam(name = PROFILE_ID_VALUE, required = false) Integer profileId) {
        Profile profile = dbManager.getProfile(profileId);
        Set<Integer> profileAttr = profile.attributes();
        Map<Integer, CampaignConfig> campaignConfigs = dbManager.getAllCampaignsConfigs();
        Integer selectedCampaign = -1;
        System.out.println(profileAttr);
        boolean foundValidCamp = false;
        boolean foundValidCapacity = false;
        for (Map.Entry<Integer, List<Integer>> entry : dbManager.getAllCampaignAttributes().entrySet()) {
            Integer campaignId = entry.getKey();
            List<Integer> attributeIds = entry.getValue();
            System.out.println(attributeIds);
            if (profileAttr.containsAll(attributeIds)) {
                foundValidCamp = true;
                // Capacities block
                campaignProfileCapacities.putIfAbsent(campaignId, new HashMap<>());
                campaignProfileCapacities.get(campaignId).putIfAbsent(profileId, 0);
                if (campaignConfigs.get(campaignId).capacity() <= campaignProfileCapacities.get(campaignId).get(profileId)) {
                    continue;
                }
                foundValidCapacity = true;

                if (selectedCampaign == -1) {
                    selectedCampaign = campaignId;
                    System.out.println("selectedCamp= " + selectedCampaign);
                    continue;
                }
                int selectedCampaignPriority = campaignConfigs.get(selectedCampaign).priority();
                int currentCampaignPriority = campaignConfigs.get(campaignId).priority();
                if (currentCampaignPriority > selectedCampaignPriority ||
                        (currentCampaignPriority == selectedCampaignPriority && campaignId < selectedCampaign)) {
                    {
                        System.out.println("old camp:" + selectedCampaign);
                        System.out.println("old camp prio:" + selectedCampaignPriority);
                        System.out.println("new camp:" + campaignId);
                        System.out.println("new camp prio:" + currentCampaignPriority);
                    }
                    selectedCampaign = campaignId;
                }
            } else
                System.out.println("not valid");
        }
        if(!foundValidCamp){
            return ResponseEntity.status(404).body("unmatched");
        }
        if (!foundValidCapacity) {
            return ResponseEntity.status(409).body("capped");
        }

        campaignProfileCapacities.get(selectedCampaign)
                .put(profileId, campaignProfileCapacities.get(selectedCampaign).get(profileId) + 1);

        return ResponseEntity.ok(selectedCampaign.toString());

    }

    /**
     *
     *   GOOD LUCK !
     *
     */


}
