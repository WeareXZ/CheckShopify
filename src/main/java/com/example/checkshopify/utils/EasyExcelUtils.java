package com.example.checkshopify.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * @Description: excel导出
 * @author: heyz
 * @Date: 2022/7/19 21:36
 */
public class EasyExcelUtils {


    /**
     * 导出
     *
     * @param fileName  输出流
     * @param head      类型
     * @param sheetName 表格名
     * @param data      数据集合
     */
    public static void export(HttpServletResponse httpServletResponse, String fileName, Class head, String sheetName, List data) {
        ExcelWriter excelWriter = null;
        try {
            fileName = fileName + ".xls";
            httpServletResponse.setContentType("application/vnd.ms-excel");
            httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            OutputStream out = httpServletResponse.getOutputStream();
            // 创建ExcelWriter对象
            excelWriter = EasyExcel.write(out, head).build();
            // 创建Sheet对象
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
            // 向Excel中写入数据
            excelWriter.write(data, writeSheet);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            excelWriter.finish();
        }
    }

}
