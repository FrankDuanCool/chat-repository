# Easy-Chat

项目名称：Easy-Chat

demo源作者：(https://github.com/baobaoJK/EZ-Chat)

README更新日期：2023/6/12

描述：这是基于上述链接提供的demo实现的，有群聊私聊功能，并具有端到端加密功能的一个Web聊天室。



## 开发环境

开发软件：IntelliJ IDEA Community Edition 2021.2.2（后端）， WebStorm 2023.1.2（前端）

Node 版本：18.16.0

Java 版本：JDK 1.8.0

SpringBoot 版本：2.7.12



## 内容介绍

chat-ui 是前端网页

chat-server 是后端服务


## 端到端加密实现

每个聊天室都有一个leader，每个进入聊天室的用户都会在进入时使用jsrsasign库，以RSA加密方式生成自己独有的公钥和私钥，以及用tweetnacl库生成一个EdDSA加密用的公钥私钥对，并把这两个公钥上传到服务器，服务器转发给所有用户。每当聊天室内发生人员变动，leader就会随机生成一个32位的会议密钥，并把这个密钥用每一个参与聊天的用户提供的自己的RSA公钥加密会议密钥，然后把这个加密后的会议密钥分发给对应的用户。每一个用户收到被加密过的会议密钥后，用自己的私钥解密得到会议密钥的内容并更新会议密钥。在此之后，如果成员A希望发送消息给成员B，只需要用CryptoJS库的AES方法用会议密钥加密，用EdDSA私钥给信息签名，并发送给对方。B收到加密信息后，先用自己有的对方的EdDSA公钥信息验证签名是否正确，然后用CryptoJS库的AES方法用会议密钥进行解密，得到A想发送给B的信息。在上述过程中，服务器只会保存参会者的名字，以及他们所提供的加密会议密钥用的公钥信息和签名验证用的公钥信息。这样一来，服务器无法破解除了信息来源和去向以外的任何信息，对信息的任何修改操作也会被用户发现并拒绝接受消息。通过这种方式，结合非对称加密和对称加密实现了较为安全的端到端加密。

值得一提的是，既然使用的是http，这意味着用户这边也容易被攻击。本项目的端到端实现逻辑仅提供一个思路，并不能做到绝对安全。


## 项目部署



### 前端

chat-ui

#### 安装
```
npm install
```


#### 开启服务

```
npm run serve
```



### 后端

chat-server

IDEA 打开 运行 ChatServerApplication.java



### 温馨提示

#### 1.

打开chat-server前确认chat-server和Easy-Chat-main文件夹内没有.idea文件夹，如果有就删掉先。

#### 2.

打开chat-server后 左上角文件-项目结构-模块-依赖，删掉所有带“junit”或者“api”关键字的所有依赖，然后在ChatServerApplicationTests.java中找到报错的地方alt+enter。

#### 3.

电脑没装Node.js请直接关闭这个页面去装。

#### 4.

请保证你的项目路径里没有一点中文。

#### 5.

chat-ui跑npm run serve之前记得npm install


