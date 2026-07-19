package com.zhaomh.service;

import com.zhaomh.model.User;
import com.zhaomh.id.UserId;

public interface UserService {
    User getUser(UserId userId);
}
