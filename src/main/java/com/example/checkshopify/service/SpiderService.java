package com.example.checkshopify.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface SpiderService {


    String getHttp(List<String> urlList);


    void getHttp(MultipartFile multipartFile,HttpServletResponse httpServletResponse);


}
