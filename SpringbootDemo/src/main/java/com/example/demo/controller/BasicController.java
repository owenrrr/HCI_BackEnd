package com.example.demo.controller;

import com.example.demo.bl.Common;
import com.example.demo.bl.Iteration2Mod;
import com.example.demo.po.User;
import com.example.demo.util.AnalysisJSON;
import com.example.demo.vo.IOKG;
import com.example.demo.vo.ProjectVO;
import com.example.demo.vo.ResponseVO;
import com.example.demo.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @Author: Owen
 * @Date: 2021/6/6 23:27
 * @Description:
 */
@RestController
@RequestMapping("/api/basic")
@Api(value = "测试接口", tags="基础知识图谱接口")
public class BasicController {

    @Autowired
    AnalysisJSON analysisJSON;
    @Autowired
    Common common;
    @Autowired
    Iteration2Mod iteration2Mod;

    @PostMapping("/inputKG")
    @ApiImplicitParams(@ApiImplicitParam(name = "IOKG", value = "input knowledge graph", required = true, dataType = "IOKG"))
    @ApiOperation(value = "编辑图谱", notes = "编辑图谱")
    ResponseVO createKG(@RequestBody IOKG IOKG){
        try{
            analysisJSON.createKG(IOKG);
        }catch(Exception e){
            return ResponseVO.buildFailure(e.getMessage());
        }
        return ResponseVO.buildSuccess("success");
    }

//    @PostMapping("/addKG")
//    @ApiImplicitParams(@ApiImplicitParam(name = "IOKG", value = "input knowledge graph", required = true, dataType = "IOKG"))
//    @ApiOperation(value = "添加图谱", notes = "添加图谱")
//    ResponseVO addKG(@RequestBody IOKG IOKG){
//        try{
//            analysisJSON.addKG(IOKG);
//        }catch(Exception e){
//            return ResponseVO.buildFailure(e.getMessage());
//        }
//        return ResponseVO.buildSuccess("success");
//    }

    @GetMapping("/getKG")
    @ApiImplicitParam(value = "获取知识图谱")
    @ApiOperation(value = "获取图谱", notes = "获取图谱")
    ResponseVO getKG(@RequestParam("pid") Integer pid){
        IOKG result = common.getKG(pid);
        if (result == null){
            return ResponseVO.buildFailure("获取图谱失败");
        }
        return ResponseVO.buildSuccess(result);
    }

    @PostMapping("/createUser")
    @ApiImplicitParam(value = "创建用户")
    @ApiOperation(value = "创建用户", notes = "创建用户")
    ResponseVO createUser(@RequestBody UserVO userVO){
        int user_id = common.createUser(userVO.getMail(), userVO.getPassword());
        if (user_id <= 0) {
            return ResponseVO.buildFailure("创建失败");
        }
        return ResponseVO.buildSuccess(user_id);
    }

    @GetMapping("/getUser")
    @ApiImplicitParam(value = "获得用户信息")
    @ApiOperation(value = "获取用户", notes = "获取用户")
    ResponseVO getUser(@RequestParam String mail){
        User user= common.getUser(mail);
        if (user == null) {
            return ResponseVO.buildFailure("用户不存在");
        }
        return ResponseVO.buildSuccess(user);
    }
    @PostMapping("/checkUser")
    @ApiImplicitParam(value = "登录")
    @ApiOperation(value = "用户登录", notes = "用户登录")
    ResponseVO checkUser(@RequestBody UserVO userVO){
        if(userVO == null){
            return ResponseVO.buildFailure("用户参数不正确");
        }
        User user= common.getUser(userVO.getMail());
        if (user == null) {
            return ResponseVO.buildFailure("用户不存在");
        }else if(userVO.getPassword() == null || user.getPassword()==null){
            return ResponseVO.buildFailure("用户不存在");
        }else {
            if(userVO.getPassword().equals(user.getPassword())){
                return ResponseVO.buildSuccess(user);
            }
            return ResponseVO.buildFailure("密码错误");
        }
    }

    @PostMapping("/createProject")
    @ApiImplicitParam(value = "创建项目")
    @ApiOperation(value = "创建项目", notes = "创建项目")
    ResponseVO createProject(@RequestBody ProjectVO projectVO){
        int project_id = common.createProject(projectVO.getUid(), projectVO.getName());
        if (project_id <= 0) {
            return ResponseVO.buildFailure("创建失败");
        }
        return ResponseVO.buildSuccess(project_id);
    }

    @GetMapping("/getUserProjects")
    @ApiImplicitParam(value = "获取用户所有项目")
    ResponseVO getUserProjects(@RequestParam("uid") Integer uid){
        HashMap<Integer, String> resultMap = iteration2Mod.getUserProjectList(uid);
        if (resultMap != null) {
            return ResponseVO.buildSuccess(resultMap);
        }
        return ResponseVO.buildFailure("该用户没有项目");
    }

    @GetMapping("/getUserInfo")
    @ApiImplicitParam(value = "获取用户所有项目")
    ResponseVO getUserDetail(@RequestParam("uid") Integer uid){
        UserVO result = iteration2Mod.getUserInfo(uid);
        if (result != null) {
            return ResponseVO.buildSuccess(result);
        }
        return ResponseVO.buildFailure("无匹配用户");
    }

    @GetMapping("/removeProject")
    ResponseVO removeProject(@RequestParam("pid") Integer pid) {
        int result = iteration2Mod.removeProject(pid);
        if (result == 1) {
            return ResponseVO.buildSuccess("成功删除项目");
        }
        return ResponseVO.buildFailure("删除项目失败");
    }

}
