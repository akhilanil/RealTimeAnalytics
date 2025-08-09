package com.liftlab.models;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(setterPrefix = "with")
@ToString
public class UserDetails {

    private String userId;

    private int sessionCount;
}
