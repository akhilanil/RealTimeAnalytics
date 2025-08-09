package com.liftlab.models;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(setterPrefix = "with")
@ToString
public class PageViewCount {

    private String pageUrl;

    private int count;
}
