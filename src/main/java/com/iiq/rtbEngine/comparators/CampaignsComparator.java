package com.iiq.rtbEngine.comparators;

import com.iiq.rtbEngine.db.models.Campaign;

import java.util.Comparator;

public class CampaignsComparator implements Comparator<Campaign> {
    /**
     * The role of this specific comparator is to compare to campaigns by the given rule set:
     * 1. if a campaign has a higher priority it wins.
     * 2. if both campaigns are equal in their priority, the lowest id wins
     * @param o1 the first campaign to be compared.
     * @param o2 the second campaign to be compared.
     * @return the resolve of which campaign is at a higher priority.
     */
    @Override
    public int compare(Campaign o1, Campaign o2) {
        int compareRes = Integer.compare(o2.priority, o1.priority);
        if (compareRes == 0){
            return Integer.compare(o1.campaignId,o2.campaignId);
        }
        return compareRes;
    }
}
