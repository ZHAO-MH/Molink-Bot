package com.zhaomh.model;

import lombok.Getter;

@Getter
public enum Role {
    NORMAL(0),
    ADMIN(5),
    OWNER(10);

    private final int level;
    Role(int level) {
        this.level = level;
    }

    public boolean isAtLeast(Role other) {
        return this.level >= other.level;
    }

    public static Role fromLevel(int level) {
        for (Role role : Role.values()) {
            if (role.level == level) {
                return role;
            }
        }
        return NORMAL;
    }
}