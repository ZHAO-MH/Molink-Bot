package com.zhaomh.service;

import com.zhaomh.id.GroupId;
import com.zhaomh.id.UserId;
import com.zhaomh.model.GroupMember;

public interface GroupMemberService {

    GroupMember getGroupMemberInfo(GroupId groupId, UserId userId);
}
