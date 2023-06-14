package com.chat.component;

import com.alibaba.fastjson.JSON;
import com.chat.entity.*;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KSaMar
 * @version 1.0
 * WebSocket 服务
 */
@Component
@ServerEndpoint("/socket/{username}")
public class WebSocketServer {

    /**
     * 存储对象 map
     */
    public static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    public static final Map<String, String> RSAPKMap = new ConcurrentHashMap<>();
    public static final Map<String, String> DSAPKMap = new ConcurrentHashMap<>();

    /***
     * WebSocket 建立连接事件
     * 1.把登录的用户存到 sessionMap 中
     * 2.发送给所有人当前登录人员信息
     * 3.更新每个人的RSA和DSA表
     * 4.提醒leader更新密钥
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        // 搜索名称是否存在
        boolean isExist = sessionMap.containsKey(username);
        if (!isExist) {
            System.out.println(username + "加入了聊天室");
            sessionMap.put(username, session);
            sendAllMessage(setUserList());
            showUserList();
//            try {
//                System.out.println(list);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    /**
     * WebSocket 关闭连接事件
     * 1.把登出的用户从 sessionMap 中剃除
     * 2.发送给所有人当前登录人员信息
     * 3.更新每个人的RSA和DSA表
     * 4.提醒leader更新密钥
     */
    @OnClose
    public void onClose(@PathParam("username") String username) {
        if (username != null) {
            System.out.println(username + "退出了聊天室");
            sessionMap.remove(username);
            RSAPKMap.remove(username);
            DSAPKMap.remove(username);
            sendAllMessage(setUserList());
            sendAllMessage(setDSA());
            sendAllMessage(setRSA());
            if (sessionMap.entrySet().stream().findFirst().isPresent()){
                String leader = sessionMap.entrySet().stream().findFirst().get().getKey();
                try {
                    sessionMap.get(leader).getBasicRemote().sendText(JSON.toJSONString(setupdate()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            showUserList();
            showDSAList();
            showRSAList();
        }
    }

    /**
     * WebSocket 接受信息事件
     * 接收处理客户端发来的数据
     * @param message 信息
     */
    @OnMessage
    public void onMessage(String message) {
            Message msg = JSON.parseObject(message, Message.class);
            String smsg = JSON.toJSONString(msg);
            sendAllMessage(msg);
//            try {
//                System.out.println(smsg);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
    }

    /**
     * WebSocket 错误事件
     * @param session 用户 Session
     * @param error 错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 显示在线用户
     */
    private void showUserList() {
        System.out.println("------------------------------------------");
        System.out.println("当前在线用户");
        System.out.println("------------------------------------------");
        for (String username : sessionMap.keySet()) {
            System.out.println(username);
        }
        System.out.println("------------------------------------------");
        System.out.println();
    }

    /**
     * 显示RSA表
     */
    private void showRSAList() {
        System.out.println("------------------------------------------");
        System.out.println("RSA");
        System.out.println("------------------------------------------");
        for (String username : RSAPKMap.keySet()) {
            System.out.println(username + RSAPKMap.get(username));
        }
        System.out.println("------------------------------------------");
        System.out.println();
    }

    /**
     * 显示DSA表
     */
    private void showDSAList() {
        System.out.println("------------------------------------------");
        System.out.println("DSA");
        System.out.println("------------------------------------------");
        for (String username : DSAPKMap.keySet()) {
            System.out.println(username + DSAPKMap.get(username));
        }
        System.out.println("------------------------------------------");
        System.out.println();
    }

    /**
     * 设置接收消息的用户列表
     * @return 用户列表
     */
    private Message setUserList(){
        ArrayList<String> list = new ArrayList<>(sessionMap.keySet());
        Message userList = new Message();
        userList.setUserlist(list);
        return userList;
    }

    /**
     * 设置RSA表
     * @return RSA表
     */
    private Message setRSA(){
        Map<String,String> tmp = new HashMap<>();
        for (String username : RSAPKMap.keySet()) {
            tmp.put(username,RSAPKMap.get(username));
        }
        Message rsamap = new Message();
        rsamap.setRsamap(tmp);
        return rsamap;
    }

    /**
     * 设置DSA表
     * @return DSA表
     */
    private Message setDSA(){
        Map<String,String> tmp = new HashMap<>();
        for (String username : DSAPKMap.keySet()) {
            tmp.put(username,DSAPKMap.get(username));
        }
        Message dsamap = new Message();
        dsamap.setDsamap(tmp);
        return dsamap;
    }

    /**
     * 生成提醒包（提醒leader更新）
     * @return 带有提醒意味的包
     */
    private Message setupdate(){
        Message upd = new Message();
        upd.setUpd(1);
        return upd;
    }

    /**
     * 发送消息到所有用户种
     * @param message 消息
     */
    private void sendAllMessage(Message message) {
        try {
//            System.out.println("msg+"+ message);
            if (message.getSendto() != null){//更新会议密钥
                String sendto = message.getSendto();
//                System.out.println(sendto);
                sessionMap.get(sendto).getBasicRemote().sendText(JSON.toJSONString(message));
            }else {
                if(message.getSign() != null){//message是发送消息的操作
                    String sourcename = message.getName();
                    List<String> getto = message.getTo();
//                    System.out.println(getto);
                    if (getto.size()==0){//群发
                        message.setReceiver("All Users");
                        for (Session session : sessionMap.values()) {
                            session.getBasicRemote().sendText(JSON.toJSONString(message));
                        }
                    }else {
                        for (String s : getto) {
                            message.setReceiver(s);
                            if (s.equals(sourcename)) {//给自己发
                                sessionMap.get(s).getBasicRemote().sendText(JSON.toJSONString(message));
                            } else {//一对一
                                sessionMap.get(s).getBasicRemote().sendText(JSON.toJSONString(message));
                                sessionMap.get(sourcename).getBasicRemote().sendText(JSON.toJSONString(message));
                            }
                        }
                    }
                }else {
                    if (message.getType()!=null){//新用户加入
//                        System.out.println(message);
                        String dsapk = message.getDsapk();
                        dsapk = dsapk.replace("\\","");
                        String name = message.getName();
                        String rsapk = message.getRsapk();
                        rsapk = rsapk.replace("\\r\\n","\r\n");
                        RSAPKMap.put(name, rsapk);
                        DSAPKMap.put(name, dsapk);
//                        System.out.println(setDSA());
//                        System.out.println(setRSA());
                        for (Session session : sessionMap.values()) {
                            session.getBasicRemote().sendText(JSON.toJSONString(setDSA()));
                            session.getBasicRemote().sendText(JSON.toJSONString(setRSA()));
                        }
                        if (sessionMap.entrySet().stream().findFirst().isPresent()){
                            String leader = sessionMap.entrySet().stream().findFirst().get().getKey();
                            sessionMap.get(leader).getBasicRemote().sendText(JSON.toJSONString(setupdate()));
                        }
                        showRSAList();
                        showDSAList();
                        return;
                    }
                    for (Session session : sessionMap.values()) {
                        session.getBasicRemote().sendText(JSON.toJSONString(message));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
