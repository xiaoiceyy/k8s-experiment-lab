package com.ly.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ly.dto.LayerDto;
import com.ly.mapper.UserMapper;
import com.ly.model.User;

import lombok.extern.slf4j.Slf4j;

/*
阿良教育 www.aliangedu.cn
*/
@Controller
@Slf4j
public class UserController {
    @Autowired
    private UserMapper userMapper;

    @RequestMapping("/queryUserListPage")
    public String queryuserListPage(ModelMap model){
        log.info("请求访问用户列表页面");
        return "userList";
    }

    @RequestMapping("/addUserPage")
    public String addUserPage(ModelMap model){
        log.info("请求访问添加用户页面");
        return "addUser";
    }
    
    @RequestMapping("/queryUserList")
    @ResponseBody
    public Object queryUserList(HttpServletRequest request, HttpServletResponse response,Integer page,Integer limit,String name){
        log.info("请求查询用户列表");
        List<User> userList=userMapper.queryuserList(name);
        return new LayerDto<User>(0, "返回成功", 10,userList); 
    }
    
    @PostMapping("/addUser")
    @ResponseBody
    public String addUser(HttpServletRequest request, HttpServletResponse response,User user){
        if(null==user){
            log.info("添加失败，用户对象为空！");
            return "fail";
        }
        log.info(new Date()+"请求添加一个用户,名字={},年龄={},性别={}",user.getName(),user.getAge(),user.getSex());
        int result=userMapper.insert(user.getName(), user.getAge(), user.getSex());
        if(result==0){
            log.info("添加失败，数据库插入用户返回空！");
            return "fail";
        }
        log.info("添加'{}'用户成功！",user.getName());
        return "ok";
    }
}

