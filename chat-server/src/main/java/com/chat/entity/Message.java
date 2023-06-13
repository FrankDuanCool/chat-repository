package com.chat.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author KSaMar
 * @version 1.0
 * 信息实体类
 */
@Data
public class Message {

    private String name;
    private String time;
    private String msg;
    private String to;
    private String rsapk;
    private String dsapk;
    private String sign;
    private String type;
    private String key;
    private String sendto;

    private Map<String,String> dsamap;

    private Map<String,String> rsamap;

    private List<String> userlist;

    private int upd;
}
