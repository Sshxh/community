package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;


    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    public User findUserById(int id) {
        return userMapper.selectById(id);
    }


    //用户注册的数据库集合操作
    //返回的是带有提示msg的集合
//    public Map<String, Object> register(User user) {
//        Map<String, Object> map = new HashMap<>();
//
//        // =========所有可能的空值处理
//
//        if (user == null) {
//            throw new IllegalArgumentException("参数不能为空!");
//        }
//        if (StringUtils.isBlank(user.getUsername())) {
//            map.put("usernameMsg", "账号不能为空!");
//            return map;
//        }
//        if (StringUtils.isBlank(user.getPassword())) {
//            map.put("passwordMsg", "密码不能为空!");
//            return map;
//        }
//        if (StringUtils.isBlank(user.getEmail())) {
//            map.put("emailMsg", "邮箱不能为空!");
//            return map;
//        }
//
//        //=========重复注册问题的处理
//
//
//        // 验证账号
//        User u = userMapper.selectByName(user.getUsername());
//        if (u != null) {
//            map.put("usernameMsg", "该账号已存在!");
//            return map;
//        }
//        // 验证邮箱
//        u = userMapper.selectByEmail(user.getEmail());
//        if (u != null) {
//            map.put("emailMsg", "该邮箱已被注册!");
//            return map;
//        }
//
//        // =========没有上述的问题的话 进行注册用户
//
//
//        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
//        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
//        user.setType(0);
//        user.setStatus(0);
//        //随机数
//        user.setActivationCode(CommunityUtil.generateUUID());
//        //随机头像
//        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
//        //创建时间
//        user.setCreateTime(new Date());
//
//
//        //=========添加注册的用户
//        userMapper.insertUser(user);
//
//
//
//
//        // =========激活邮件
//
//
//        Context context = new Context();
//        //带上刚注册的email
//        context.setVariable("email", user.getEmail());
//        // http://localhost:8023/activation/101/code
//        String url = domain + ("/".equals(contextPath) ? "" : contextPath) + "/activation/" + user.getId() + "/" + user.getActivationCode();
//
////        String url = domain + CommunityUtil.contextPathJudge(contextPath) + "/activation/" + user.getId() + "/" + user.getActivationCode();
//        context.setVariable("url", url);
//        String content = templateEngine.process("/mail/activation", context);
//
//
//
//        //=========发送邮箱
//        mailClient.sendMail(user.getEmail(), "激活账号", content);
//
//        return map;
//    }
//
//    //=========激活是否正确的方法
//    public int activation(int userId,String code){
//        User user = userMapper.selectById(userId);
//        if(user.getStatus()==1){
//            return ACTIVATION_REPEAT;
//        }
//        //下面就是用户的激活码和现在的激活码是一致的情况下 说明我们的用户正确被激活
//        else if(user.getActivationCode().equals(code)){
//            userMapper.updateStatus(userId,1);
//            return ACTIVATION_SUCCESS;
//        }else{
//            return ACTIVATION_FAILURE;
//        }
//
//    }
//

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }
        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8023/activation/101/code
        String url = domain + ("/".equals(contextPath) ? "" : contextPath) + "/activation/" + user.getId() + "/" + user.getActivationCode();
//        String url = domain + CommunityUtil.contextPathJudge(contextPath) + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }


    public Integer activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        //如果已经正确注册的话 激活状态status已经是1了 没必要再激活了
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            //如果正确注册的话 我们会将其激活状态status改成1
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }
}
