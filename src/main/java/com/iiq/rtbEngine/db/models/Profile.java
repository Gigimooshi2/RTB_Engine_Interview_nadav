package com.iiq.rtbEngine.db.models;

import java.util.Set;

public record Profile(int profileID , Set<Integer> attributes) {
}
