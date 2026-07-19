package com.zhaomh.service;

import com.zhaomh.id.UserId;
import com.zhaomh.model.Group;
import com.zhaomh.id.GroupId;

public interface GroupService {
    Group getGroup(GroupId groupId);

    void ban(GroupId groupId, UserId userId, int time);
}
