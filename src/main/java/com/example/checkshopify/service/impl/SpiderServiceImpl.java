package com.example.checkshopify.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.checkshopify.config.HttpClientPool;
import com.example.checkshopify.dto.ModelExcel;
import com.example.checkshopify.service.SpiderService;
import com.example.checkshopify.utils.EasyExcelUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class SpiderServiceImpl implements SpiderService {


    @Autowired
    private HttpClientPool httpClientPool;

    @Override
    public String getHttp(List<String> urlList) {
        StringBuilder stringBuilder = new StringBuilder();
        CloseableHttpClient httpClient = httpClientPool.getHttpClient();
        //创建HttpGet请求
        urlList.forEach(s -> {
            if (StringUtils.isNotBlank(s)) {
                log.info("网址:{}", s);
                String trim = s.trim();
                HttpGet httpGet = new HttpGet(trim);
                //使用HttpClient发起请求
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    //判断响应状态码是否为200
                    if (response.getStatusLine().getStatusCode() == 200) {
                        //如果为200表示请求成功，获取返回数据
                        String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                        if (content.contains("shopify")) {
                            stringBuilder.append("网址:" + s + " 包含shopify").append("/n");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("解析错误!");
                }
            }
        });
        return stringBuilder.toString();
    }

    @Override
    public void getHttp(@RequestParam("file") MultipartFile multipartFile, HttpServletResponse httpServletResponse) {
        String originalFilename = multipartFile.getOriginalFilename();
        int beginIndex = originalFilename.lastIndexOf(".");
        String suffix = originalFilename.substring(beginIndex);
        if (!".xls".equals(suffix) && !".xlsx".equals(suffix)) {
            return;
        }
        List<ModelExcel> list = new ArrayList<>();
        ExcelReader excelReader = null;
        CloseableHttpClient httpClient = httpClientPool.getHttpClient();
        try {
            // 创建ExcelReader对象
            excelReader = EasyExcel.read(multipartFile.getInputStream(), ModelExcel.class, new AnalysisEventListener<ModelExcel>() {
                @Override
                public void invoke(ModelExcel modelExcel, AnalysisContext analysisContext) {
                    if (Objects.isNull(modelExcel)) {
                        return;
                    }
                    String s = modelExcel.getUrl();
                    if (StringUtils.isNotBlank(s)) {
                        log.info("网址:{}", s);
                        String trim = s.trim();
                        if (!s.startsWith("http") && !s.startsWith("https")) {
                            modelExcel.setResult("需要http或htpps开头");
                            list.add(modelExcel);
                            return;
                        }
                        HttpGet httpGet = new HttpGet(trim);
                        //使用HttpClient发起请求
                        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                            //判断响应状态码是否为200
                            if (response.getStatusLine().getStatusCode() == 200) {
                                //如果为200表示请求成功，获取返回数据
                                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                                if (content.contains("shopify")) {
                                    modelExcel.setResult("是");
                                    list.add(modelExcel);
                                }
                            }
                        } catch (Exception e) {
                            modelExcel.setResult("网址访问出错!");
                            list.add(modelExcel);
                            e.printStackTrace();
                            log.info("解析错误!");
                        }
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    log.info("导入完成!");
                }
            }).build();
            excelReader.readAll();
            EasyExcelUtils.export(httpServletResponse, "result", ModelExcel.class, "表格1", list);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            excelReader.finish();
        }
    }

    @Override
    public void getHttp(InputStream inputStream, HttpServletResponse httpServletResponse) {
        List<ModelExcel> list = new ArrayList<>();
        ExcelReader excelReader = null;
        CloseableHttpClient httpClient = httpClientPool.getHttpClient();
        try {
            // 创建ExcelReader对象
            excelReader = EasyExcel.read(inputStream, ModelExcel.class, new AnalysisEventListener<ModelExcel>() {
                @Override
                public void invoke(ModelExcel modelExcel, AnalysisContext analysisContext) {
                    if (Objects.isNull(modelExcel)) {
                        return;
                    }
                    String s = modelExcel.getUrl();
                    if (StringUtils.isNotBlank(s)) {
                        log.info("网址:{}", s);
                        String trim = s.trim();
                        if (!s.startsWith("http") && !s.startsWith("https")) {
                            modelExcel.setResult("需要http或htpps开头");
                            list.add(modelExcel);
                            return;
                        }
                        HttpGet httpGet = new HttpGet(trim);
                        //使用HttpClient发起请求
                        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                            //判断响应状态码是否为200
                            if (response.getStatusLine().getStatusCode() == 200) {
                                //如果为200表示请求成功，获取返回数据
                                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                                if (content.contains("shopify")) {
                                    modelExcel.setResult("是");
                                    list.add(modelExcel);
                                }
                            }
                        } catch (Exception e) {
                            modelExcel.setResult("网址访问出错!");
                            list.add(modelExcel);
                            e.printStackTrace();
                            log.info("解析错误!");
                        }
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    log.info("导入完成!");
                }
            }).build();
            excelReader.readAll();
            EasyExcelUtils.export(httpServletResponse, "result", ModelExcel.class, "表格1", list);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            excelReader.finish();
        }
    }
}
