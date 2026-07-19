package com.zhaomh.model;


import com.google.gson.annotations.SerializedName;
import com.zhaomh.id.GroupId;
import com.zhaomh.id.Identifiable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Group implements Identifiable<GroupId> {
    @SerializedName("group_id")
    private final GroupId id;

    @SerializedName("nickname")
    @Builder.Default
    private String nickname = null;

    public Group(long id, String nickname) {
        this(GroupId.of(id), nickname);
    }
    public Group(GroupId id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
}
