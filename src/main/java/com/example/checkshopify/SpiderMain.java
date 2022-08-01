package com.example.checkshopify;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.checkshopify.dto.ModelExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SpiderMain extends JFrame implements ActionListener {

    private JButton select;
    private JButton downTemplate;
    private JButton btnOK;
    private JTextField textField;
    private JPanel panel;
    private JFileChooser fc = new JFileChooser();


    public SpiderMain() {
        // 建立一个面板
        panel = new JPanel();
        // 把面板添加到框架
        this.getContentPane().add(panel);
        // 把一个文本按钮添加到面板
        panel.add(new JLabel("请选择模版文件"));
        textField = new JTextField(10);
        // 把一个文本框添加到面板
        panel.add(textField);
        select = new JButton("浏览");
        // 把一个浏览按钮添加到面板
        panel.add(select);
        select.addActionListener(this);
        btnOK = new JButton("确定");
        // 把一个确定按钮添加到面板
        panel.add(btnOK);
        btnOK.addActionListener(this);
        downTemplate = new JButton("下载模版");
        // 把一个下载模版按钮添加到面板
        panel.add(downTemplate);
        downTemplate.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
    // 当按下选择按钮，打开一个文件选择，文本框显示文件路径
        if (e.getSource() == select) {
            int intRetVal = fc.showOpenDialog(this);
            if (intRetVal == JFileChooser.APPROVE_OPTION) {
                textField.setText(fc.getSelectedFile().getPath());
            }
        } else if (e.getSource() == btnOK) {
            // 当按下确定按钮，生成一个新框架，框架里面有一个文本域，显示打开文件的内容
            JFrame f = new JFrame();
            f.setSize(400, 400);
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            String extensionName = getExtensionName(textField.getText());
            if (!"xls".equals(extensionName) && !"xlsx".equals(extensionName)) {
                JOptionPane.showMessageDialog(null, "请选择xlsx/xls格式的文件!");
            }
            try {
                File selectedFile = fc.getSelectedFile();
                InputStream inputStream =  new FileInputStream(selectedFile);
                getHttp(inputStream);
                JOptionPane.showMessageDialog(null, "请耐心等待,完成后文件将在该文件路径下生成,请勿关闭程序!");
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "解析出错，请校对模板后重试!");
            }
        }else if(e.getSource() == downTemplate){
            downTemplate();
        }
    }

    /**
     * @param filename
     * @return
     * @throws
     * @Description：获取文件后缀名
     */
    private String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public void getHttp(InputStream inputStream) {
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

    public void downTemplate(){
        ExcelReader excelReader = null;
        try {
            File file = new File("模版.xls");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            export(fileOutputStream, ModelExcel.class, "表格1", null);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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


    public static void main(String[] args) {
        SpiderMain frame = new SpiderMain();
        frame.setSize(400, 100);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
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
