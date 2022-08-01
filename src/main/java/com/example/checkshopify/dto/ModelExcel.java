package com.example.checkshopify.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelExcel {

    @ExcelProperty("序号")
    private Integer index;

    @ExcelProperty("网址")
    private String url;

    @ExcelProperty("是否包含shopify")
    private String result;
}
