package com.zhaomh.command;

import com.zhaomh.model.Role;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    Role value() default Role.ADMIN;
}