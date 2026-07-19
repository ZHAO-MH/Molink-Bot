package com.zhaomh.id;

import lombok.Getter;

@Getter
public class GroupMemberId {
    private final GroupId groupId;
    private final UserId userId;

    private GroupMemberId(GroupId groupId, UserId userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public static GroupMemberId of(GroupId groupId, UserId userId) {
        return new GroupMemberId(groupId, userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMemberId)) return false;
        return groupId == ((GroupMemberId) o).groupId && userId == ((GroupMemberId) o).userId;
    }
    @Override
    public int hashCode() { return toString().hashCode(); }
    @Override
    public String toString() { return groupId + ":" + userId; }
}

