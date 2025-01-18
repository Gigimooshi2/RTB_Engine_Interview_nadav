package com.iiq.rtbEngine.controller;

import com.iiq.rtbEngine.cache.ProfileCampaignCache;
import com.iiq.rtbEngine.comparators.CampaignsComparator;
import com.iiq.rtbEngine.db.DbManager;
import com.iiq.rtbEngine.db.models.Campaign;
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
    private ProfileCampaignCache profileCampaignCache;

    @Autowired
    private DbManager dbManager;

    private final CampaignsComparator campaignsComparator = new CampaignsComparator();

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

    /**
     * This function creates a pre-made list of all future campaigns a profile is expected to be forwarded.
     * The campaigns are picked only relative to the new attribute added to not pick past campaigns again.
     * @param profile - The profile for each the list will be made
     * @param attributeId - The new given attribute by which we'll decide which campaigns are relevant
     * @return - a list of campaigns by in the order decided by the comparator
     */
    public List<Campaign> createProfileCampaignsList(Profile profile, int attributeId){
        Set<Integer> profileAttr = profile.attributes();
        TreeSet<Campaign> campaignHeap = new TreeSet<>(campaignsComparator);

        Map<Integer, CampaignConfig> campaignConfigs = dbManager.getAllCampaignsConfigs();
        for (Map.Entry<Integer, List<Integer>> entry : dbManager.getAllCampaignAttributes().entrySet()) {
            List<Integer> campaignAttributeIds = entry.getValue();
            // Check if the campaign is relevant to the new attribute given and if they exist in the profiles attributes.
            if (campaignAttributeIds.contains(attributeId) && profileAttr.containsAll(campaignAttributeIds)) {
                CampaignConfig campaignConfig = campaignConfigs.get(entry.getKey());
                campaignHeap.add(new Campaign(entry.getKey(), campaignConfig.capacity(), campaignConfig.priority()));
            }
        }
        // Convert the heap to a simple list as we don't need more logic.
        return campaignHeap.stream().toList();
    }

    /**
     * This function adds an extra attribute to the given profile.
     * in addition to that, in order to apply caching for the bidding logic which is heavier in requests
     * we take the given attribute and pre-make all the relevant bids to the profile in advance.
     * -
     * The cache is resorted every time the profile adds a new attribute without losing past progress.
     * @param attributeId - The new attribute given to the profile.
     * @param profileId - The id of the profile.
     * @return
     * "Saved" (HTTP 200) - If the attribute addition was successful.
     * "Profile not saved" (HTTP 500) - If for some reason the profiles attribution addition failed.
     * "Attribute already exists" (HTTP 304) - If the attribute given already exists.
     */
    @GetMapping("/attribute")
    public ResponseEntity<String> attributeRequest(HttpServletRequest request, HttpServletResponse response,
                                                   @RequestParam(name = ATTRIBUTE_ID_VALUE, required = true) Integer attributeId,
                                                   @RequestParam(name = PROFILE_ID_VALUE, required = true) Integer profileId) {
        Profile profile = dbManager.getProfile(profileId);
        if (profile.attributes().contains(attributeId)) {
            return ResponseEntity.status(304).body("Attribute already exists");
        }
        boolean didProfileUpdate = dbManager.updateProfileAttribute(profileId, attributeId);
        if (didProfileUpdate) {
            // Add attribute to local profile instead of going to DB again.
            profile.attributes().add(attributeId);

            List<Campaign> profileCampaignsList = createProfileCampaignsList(profile,attributeId);
            profileCampaignCache.appendProfileCampaignsData(profile.profileID(), profileCampaignsList,campaignsComparator);

            return ResponseEntity.ok("Saved");
        } else {
            return ResponseEntity.status(500).body("Profile not saved");
        }

    }

    /**
     * This function forwards the next campaign bid to the profile,
     * All the given campaigns are pre-determined in a cache declared at {@link #attributeRequest(HttpServletRequest, HttpServletResponse, Integer, Integer)}
     * and are returned to the profile in their given order.
     * @param profileId - the id of the profile who'll be forwarded the campaign.
     * @return
     * "<The relevant campaign ID>" - assuming the profile has campaigns loaded up in cache we'll return him the next campaign by it's ID.
     * "unmatched" - if the profiles attributes never matched existing campaigns we'll return this message.
     * "capped" - if the profile has reached the end of the cache and there are no more campaigns to present we'll return this message.
     */
    @GetMapping("/bid")
    public ResponseEntity<String> bidRequest(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam(name = PROFILE_ID_VALUE, required = false) Integer profileId) {
        Integer resultCampaignId;
        if (!profileCampaignCache.checkIfAnyMatchFound(profileId)) {
            return ResponseEntity.status(404).body("unmatched");
        }
        resultCampaignId = profileCampaignCache.getNextCampaign(profileId);
        if (resultCampaignId == null) {
            return ResponseEntity.status(409).body("capped");
        }
        return ResponseEntity.ok(resultCampaignId.toString());
    }

    /**
     *
     *   GOOD LUCK !
     *   thx
     */


}
