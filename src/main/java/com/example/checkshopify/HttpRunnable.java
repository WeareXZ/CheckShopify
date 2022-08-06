package com.example.checkshopify;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.checkshopify.dto.ModelExcel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class HttpRunnable implements Runnable{

    private InputStream inputStream;

    @Override
    public void run() {
        List<ModelExcel> list = new ArrayList<>();
        ExcelReader excelReader = null;
        CloseableHttpClient httpClient = getHttpClient();
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
                                }else {
                                    modelExcel.setResult("否");
                                    list.add(modelExcel);
                                }
                            }
                        } catch (Exception e) {
                            modelExcel.setResult("网址访问出错!");
                            list.add(modelExcel);
                        }
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    JOptionPane.showMessageDialog(null, "导入成功!");
                }
            }).build();
            excelReader.readAll();
            LocalDateTime now = LocalDateTime.now();
            String format = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            File file = new File(format+"-result.xls");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            export(fileOutputStream, ModelExcel.class, "表格1", list);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            excelReader.finish();
        }
    }

    public CloseableHttpClient getHttpClient() {
        //创建HttpClient对象
        SSLContext sslContext = null;
        try {
            sslContext = SSLContextBuilder.create().useProtocol(SSLConnectionSocketFactory.SSL).loadTrustMaterial((x, y) -> true).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
        return HttpClientBuilder.create().setDefaultRequestConfig(config).setSSLContext(sslContext).setSSLHostnameVerifier((x, y) -> true).build();
    }

    public void export(FileOutputStream fileOutputStream, Class head, String sheetName, List data) {
        ExcelWriter excelWriter = null;
        try {
            // 创建ExcelWriter对象
            excelWriter = EasyExcel.write(fileOutputStream, head).build();
            // 创建Sheet对象
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
            // 向Excel中写入数据
            excelWriter.write(data, writeSheet);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            excelWriter.finish();
        }
    }
}
