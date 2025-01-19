package com.iiq.rtbEngine.cache;

import com.iiq.rtbEngine.db.models.Campaign;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProfileCampaignCache {

    private final Map<Integer, LinkedList<Campaign>> profileCampaigns;
    private final Map<Integer, Boolean> matchFound;

    public ProfileCampaignCache() {
        this.matchFound = new ConcurrentHashMap<>();
        this.profileCampaigns = new ConcurrentHashMap<>();
    }

    /**
     * In order to know if no match was ever found or if the cache is just empty we need an indicator flag
     * for each profile to be declared in advanced.
     * @param profileId - the id of the profile we're interested to know if it ever got a match.
     * @return boolean value if a match was ever found for this profile.
     */
    public boolean checkIfAnyMatchFound(int profileId) {
        return matchFound.get(profileId);
    }

    /**
     * This function sets the pre-made list of campaigns which will be forwarded to the client during the bidding.
     * @param profileId - The id of the profile whose cache will be set.
     * @param campaigns - the list of the given campaigns.
     */
    public void loadProfileCampaignsData(int profileId, List<Campaign> campaigns) {
        if (campaigns.isEmpty()) {
            this.matchFound.put(profileId, false);
        }
        this.matchFound.put(profileId, true);
        profileCampaigns.put(profileId, new LinkedList<>(campaigns));
    }

    /**
     * This function appends to the existing cache add new campaigns and resorts them by the rules of the given comparator.
     * if no campaign exists in cache, the function calls {@link #loadProfileCampaignsData}
     * @param profileId - The id of the profile whose cache will be changed.
     * @param campaigns - the list of the new given campaigns.
     * @param comparator - the comparator which will declare the rule set of the merge
     */
    public void appendProfileCampaignsData(int profileId, List<Campaign> campaigns, Comparator<Campaign> comparator) {
        LinkedList<Campaign> currentCampaigns = profileCampaigns.get(profileId);
        if (currentCampaigns == null || currentCampaigns.isEmpty()){
            loadProfileCampaignsData(profileId,campaigns);
            return;
        }
        profileCampaigns.put(profileId,
                new LinkedList<>(
                        Stream.concat(campaigns.stream(),currentCampaigns.stream())
                                .sorted(comparator)
                                .collect(Collectors.toList())));
    }

    /**
     * This function returns the next campaign to the client,
     * In addition, by using the {@link Campaign#capacity} we decide how many times profile will be forwarded
     * the ad before we move on to the next Campaign.
     *
     * @param profileId - The id of the profile who we will reference the cache to.
     * @return The ID of the next relevant campaign.
     */
    public Integer getNextCampaign(int profileId) {
        LinkedList<Campaign> campaigns = profileCampaigns.get(profileId);

        if (campaigns == null || campaigns.isEmpty()) {
            return null; // No more campaigns for this profile
        }

        Campaign currentCampaign = campaigns.peekFirst();
        if (currentCampaign != null && currentCampaign.capacity <= 1) {
            return campaigns.pollFirst().campaignId;
        }
        campaigns.peekFirst().capacity--;

        assert currentCampaign != null;
        return currentCampaign.campaignId; // Return the current campaign without removing it
    }

}