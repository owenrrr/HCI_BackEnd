package com.example.demo.controller;

import com.example.demo.bl.Common;
import com.example.demo.util.AnalysisJSON;
import com.example.demo.vo.IOKG;
import com.example.demo.vo.ResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/inputKG")
    @ApiImplicitParams(@ApiImplicitParam(name = "IOKG", value = "input knowledge graph", required = true, dataType = "IOKG"))
    @ApiOperation(value = "新建图谱", notes = "新建图谱")
    ResponseVO createKG(@RequestBody IOKG IOKG){
        try{
            analysisJSON.createKG(IOKG);
        }catch(Exception e){
            return ResponseVO.buildFailure(e.getMessage());
        }
        return ResponseVO.buildSuccess("success");
    }

    @PostMapping("/addKG")
    @ApiImplicitParams(@ApiImplicitParam(name = "IOKG", value = "input knowledge graph", required = true, dataType = "IOKG"))
    @ApiOperation(value = "添加图谱", notes = "添加图谱")
    ResponseVO addKG(@RequestBody IOKG IOKG){
        try{
            analysisJSON.addKG(IOKG);
        }catch(Exception e){
            return ResponseVO.buildFailure(e.getMessage());
        }
        return ResponseVO.buildSuccess("success");
    }

    @GetMapping("/getKG")
    @ApiImplicitParam(value = "获取知识图谱")
    @ApiOperation(value = "获取图谱", notes = "获取图谱")
    ResponseVO getKG(){
        IOKG result = common.getKG();
        if (result == null || result.getEdges().length == 0 || result.getNodes().length == 0){
            return ResponseVO.buildFailure("failed");
        }
        return ResponseVO.buildSuccess(result);
    }
}
