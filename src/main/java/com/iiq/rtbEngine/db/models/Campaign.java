package com.iiq.rtbEngine.db.models;

import java.util.List;

public class Campaign {
    public int campaignId, capacity , priority;
    public List<Integer> attributes;

    public Campaign(int campaignId, int capacity, int priority) {
        this.campaignId = campaignId;
        this.capacity = capacity;
        this.priority = priority;
    }

    public Campaign(int campaignId, int capacity, int priority, List<Integer> attributes) {
        this.campaignId = campaignId;
        this.capacity = capacity;
        this.priority = priority;
        this.attributes = attributes;
    }
}
