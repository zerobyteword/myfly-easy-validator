package io.github.zerobyteword.easyvalidator.beans;

import lombok.Data;

import java.util.Map;

@Data
public class TargetBean {
    private String name2;
    private Integer age2;
    private OtherBean test4;
    private String sex;
    private String test;
    private Map<String, String> map;
}
