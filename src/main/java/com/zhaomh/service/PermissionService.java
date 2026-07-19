package com.zhaomh.service;

import com.zhaomh.id.UserId;
import com.zhaomh.model.Role;

public interface PermissionService {
    boolean hasPermission(UserId userId, Role requiredRole);
    void setPermission(UserId userId, Role requiredRole);
    Role getRole(UserId userId);

    boolean isOwner(UserId userId);
    boolean isAdmin(UserId userId);
    boolean isNormal(UserId userId);
}
