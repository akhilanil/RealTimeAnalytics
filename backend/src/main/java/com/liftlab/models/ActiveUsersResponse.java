package com.liftlab.models;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder(setterPrefix = "with")
@ToString
public class ActiveUsersResponse {

    private List<UserDetails> userDetails;

}
