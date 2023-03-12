package io.github.zerobyteword.easyvalidator.beans;

import lombok.Data;

import java.util.Map;

@Data
public class SourceBean {
    private String name;
    private String age;
    private OtherBean otherBean;
    private String sex;
    private Boolean test;
    private Map<String, String> map;
}
