package com.zhaomh.service.impl;

import com.zhaomh.Main;
import com.zhaomh.config.BotConfig;
import com.zhaomh.id.UserId;
import com.zhaomh.model.Role;
import com.zhaomh.service.PermissionService;

public class PermissionServiceImpl implements PermissionService {
    private final BotConfig config;

    public PermissionServiceImpl(BotConfig botConfig) {
        config = botConfig;
    }
    @Override
    public boolean hasPermission(UserId userId, Role requiredRole) {
        return switch (requiredRole) {
            case OWNER -> isOwner(userId);
            case ADMIN -> isAdmin(userId) || isOwner(userId);
            case NORMAL -> true;
        };
    }

    @Override
    public void setPermission(UserId userId, Role requiredRole) {
        switch (requiredRole) {
            case OWNER -> {
                config.getPermissions().removeAdmin(userId);
                config.getPermissions().setOwner(userId);
            }
            case ADMIN -> {
                if (!isOwner(userId))
                    config.getPermissions().addAdmin(userId);
            }
            case NORMAL -> {
                if (!isOwner(userId))
                    config.getPermissions().removeAdmin(userId);
            }
        }
    }

    @Override
    public Role getRole(UserId userId) {
        if (isOwner(userId)) {
            return Role.OWNER;
        } else if (isAdmin(userId)) {
            return Role.ADMIN;
        }
        return Role.NORMAL;
    }


    @Override
    public boolean isOwner(UserId userId) {
        return config.getOwner() != null && config.getOwner().equals(userId);
    }

    @Override
    public boolean isAdmin(UserId userId) {
        return config.getAdmins().contains(userId);
    }

    @Override
    public boolean isNormal(UserId userId) {
        return !isOwner(userId) && !isAdmin(userId);
    }
}
