package com.lakecat.web.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


/**
 * @ClassName EmailUtil
 */
@Component
public class EmailUtils {

    @Value("${spring.mail.from}")
    private String from;


    @Autowired
    private JavaMailSender mailSender;


    public void sendMessage(String receiver, String topic, String content) {
        // 创建一个邮件对象
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from); // 设置发送发
        msg.setTo(receiver); // 设置接收方
        msg.setSubject(topic); // 设置邮件主题
        msg.setText(content); // 设置邮件内容
        // 发送邮件
        mailSender.send(msg);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }


}

