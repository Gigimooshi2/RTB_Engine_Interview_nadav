package com.iiq.rtbEngine.db.models;

import java.util.List;

public record Campaign(int campaignId, List<Integer> attributes , int capacity , int priority ) {}
