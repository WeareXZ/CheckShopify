package com.example.checkshopify.controller;

import com.example.checkshopify.dto.SpiderReq;
import com.example.checkshopify.service.SpiderService;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
public class SpriderController {

    @Autowired
    private SpiderService spiderService;

    @RequestMapping("/test")
    public String checkShopify(@RequestBody SpiderReq spiderReq) {
        Asserts.notNull(spiderReq, "parameter is null!");
        Asserts.notNull(spiderReq.getUrlList(), "url list is null!");
        return spiderService.getHttp(spiderReq.getUrlList());
    }

    @RequestMapping("/file")
    public void checkShopify(@RequestParam("file") MultipartFile multipartFile, HttpServletResponse httpServletResponse) {
        spiderService.getHttp(multipartFile, httpServletResponse);
    }

}
