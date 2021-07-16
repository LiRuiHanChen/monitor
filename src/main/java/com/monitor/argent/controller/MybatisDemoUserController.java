package com.monitor.argent.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monitor.argent.entity.MybatisDemoUser;
import com.monitor.argent.dao.MybatisDemoUserMapper;

@RestController
@RequestMapping("/usermybatis")
public class MybatisDemoUserController {

    @Autowired
    private MybatisDemoUserMapper mybatisDemoUserMapper;

    // http://127.0.0.1:8080/usermybatis/findAll
    @RequestMapping("/findAll")
    public List<MybatisDemoUser> findAll(){
        return mybatisDemoUserMapper.findAll();
    }

}