package flyfish.service.impl;

import flyfish.pojo.M_Certification;
import flyfish.service.M_ResumeWordExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class M_ResumeWordExportServiceImpl implements M_ResumeWordExportService {

    public byte[] generateWordDocument(Map<String, Object> classifiedData, String userName) {
        try (XWPFDocument document = new XWPFDocument()) {
            // 设置文档属性
            setDocumentProperties(document);

            // 创建标题
            createTitle(document, userName + " - 个人荣誉简历");

            // 创建目录
            createTableOfContents(document, classifiedData);

            // 添加分类内容（带编号）
            addClassifiedContent(document, classifiedData, userName);

            // 添加页脚
            createFooter(document);

            // 转换为字节数组
            return convertToByteArray(document);

        } catch (Exception e) {
            log.error("生成Word文档失败", e);
            throw new RuntimeException("生成文档失败: " + e.getMessage());
        }
    }

    private void setDocumentProperties(XWPFDocument document) {
        document.createStyles();

        // 创建标题样式
        CTStyle titleStyle = CTStyle.Factory.newInstance();
        titleStyle.setStyleId("TitleStyle");
        CTString titleName = CTString.Factory.newInstance();
        titleName.setVal("TitleStyle");
        titleStyle.setName(titleName);
        document.getStyles().addStyle(new XWPFStyle(titleStyle));
    }

    private void createTitle(XWPFDocument document, String title) {
        // 主标题
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        titlePara.setVerticalAlignment(TextAlignment.CENTER);

        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);
        titleRun.setColor("2E74B5");
        titleRun.setBold(true);
        titleRun.setFontFamily("微软雅黑");
        titleRun.setFontSize(20);
        titleRun.addBreak();

//        // 副标题
//        XWPFParagraph subTitlePara = document.createParagraph();
//        subTitlePara.setAlignment(ParagraphAlignment.CENTER);
//
//        XWPFRun subTitleRun = subTitlePara.createRun();
//        subTitleRun.setText("个人荣誉证书汇总");
//        subTitleRun.setColor("4472C4");
//        subTitleRun.setFontFamily("微软雅黑");
//        subTitleRun.setFontSize(14);
//        subTitleRun.addBreak();
//
//        // 添加分隔线
//        addSeparator(document);
    }

    private void createTableOfContents(XWPFDocument document, Map<String, Object> classifiedData) {
        XWPFParagraph tocPara = document.createParagraph();
        tocPara.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun tocRun = tocPara.createRun();
        tocRun.setText("目录");
        tocRun.setColor("2E74B5");
        tocRun.setBold(true);
        tocRun.setFontFamily("微软雅黑");
        tocRun.setFontSize(16);
        tocRun.addBreak();

        // 生成目录项（保留原始名称，不添加编号，避免复杂化）
        generateTOCItems(document, classifiedData, 1);

        addSeparator(document);
    }

    private void generateTOCItems(XWPFDocument document, Map<String, Object> data, int level) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            XWPFParagraph itemPara = document.createParagraph();
            itemPara.setIndentationLeft(level * 200); // 缩进

            XWPFRun itemRun = itemPara.createRun();
            itemRun.setText("• " + entry.getKey());
            itemRun.setFontFamily("宋体");
            itemRun.setFontSize(12);

            if (entry.getValue() instanceof Map) {
                generateTOCItems(document, (Map<String, Object>) entry.getValue(), level + 1);
            }
        }
    }

    // 修改为调用带编号的递归方法
    private void addClassifiedContent(XWPFDocument document, Map<String, Object> classifiedData, String userName) {
        addContentSectionWithNumbering(userName, document, classifiedData, 1, "");
    }

    /**
     * 带编号的递归添加内容
     *
     * @param document     Word文档
     * @param data         当前层级的分类数据
     * @param level        当前层级（1=一级标题，2=二级标题，以此类推）
     * @param parentNumber 父级编号（数字形式，用于生成子级编号，如"1"）
     */
    private void addContentSectionWithNumbering(String userName, XWPFDocument document, Map<String, Object> data, int level, String parentNumber) {
        int subIndex = 1;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String numberPrefix;
            String childParentNumber;

            if (level == 1) {
                // 一级标题：一、二、三……
                numberPrefix = intToChineseNumber(subIndex) + "、";
                childParentNumber = String.valueOf(subIndex);
            } else {
                // 二级及以上标题：1.1, 1.2, 1.1.1 等
                numberPrefix = parentNumber + "." + subIndex;
                childParentNumber = numberPrefix;
            }

            // 添加带编号的标题
            String fullTitle = numberPrefix + " " + entry.getKey();
            addSectionTitle(document, fullTitle, level);

            Object value = entry.getValue();
            if (value instanceof Map) {
                // 递归处理子分类
                addContentSectionWithNumbering(userName, document, (Map<String, Object>) value, level + 1, childParentNumber);
            } else if (value instanceof List) {
                // 添加证书列表
                addCertificationsContent(userName, document, (List<M_Certification>) value);
            }

            subIndex++;
        }
    }

    /**
     * 将整数转换为中文数字（1-10）
     */
    private String intToChineseNumber(int num) {
        String[] chineseNumbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        if (num >= 1 && num <= 10) {
            return chineseNumbers[num - 1];
        }
        return String.valueOf(num); // 超过10返回数字本身
    }

    private void addSectionTitle(XWPFDocument document, String title, int level) {
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);

        // 根据级别设置不同样式
        switch (level) {
            case 1:
                titleRun.setColor("2E74B5");
                titleRun.setBold(true);
                titleRun.setFontSize(16);
                break;
            case 2:
                titleRun.setColor("4472C4");
                titleRun.setBold(true);
                titleRun.setFontSize(14);
                break;
            default:
                titleRun.setColor("70AD47");
                titleRun.setBold(true);
                titleRun.setFontSize(12);
        }

        titleRun.setFontFamily("微软雅黑");
        titleRun.addBreak();
    }

    private void addCertificationsContent(String userName, XWPFDocument document, List<M_Certification> certifications) {
        for (M_Certification cert : certifications) {
            addCertificationBlock(userName, document, cert);
        }
    }

    private void addCertificationBlock(String userName, XWPFDocument document, M_Certification cert) {
        // 计算所需行数

        int baseRows = 5; // 奖项名称、颁发单位、奖项等级、类别、获奖时间
        if(userName.equals("全校")) {
            baseRows = 6; // 奖项名称、获奖教师、颁发单位、奖项等级、类别、获奖时间
        }
        boolean hasSupplement = cert.getSupplement() != null && !cert.getSupplement().isEmpty();
        int totalRows = hasSupplement ? baseRows + 1 : baseRows;

        // 创建表格（动态行数，固定2列）
        XWPFTable table = document.createTable(totalRows, 2);

        // 设置表格样式（包含列宽调整）
        styleCertificationTable(table);

        // 填充内容
        fillCertificationTable(userName, table, cert);

        // 添加间距
        document.createParagraph().createRun().addBreak();
    }

    private void styleCertificationTable(XWPFTable table) {
        // 设置表格总宽度为100%（确保百分比与固定宽度兼容）
        table.setWidth("100%");
        table.setCellMargins(50, 50, 50, 50);

        for (XWPFTableRow row : table.getRows()) {
            List<XWPFTableCell> cells = row.getTableCells();
            if (cells.size() >= 2) {
                // 第一列固定宽度为1260 twip（1 twip = 1/20磅）
                // 中文字符（宋体五号）约10.5磅宽，6个字符约63磅 = 1260 twip
                cells.get(0).setWidth("1200");
                // 第二列自动填充剩余宽度
                cells.get(1).setWidth("auto");
            }
            // 设置默认背景色（标签单元格稍后会被单独覆盖）
            for (XWPFTableCell cell : cells) {
                cell.setColor("D9E2F3");
            }
        }
    }

    private void fillCertificationTable(String userName, XWPFTable table, M_Certification cert) {
        if (userName.equals("全校")) {
            //第一行增加获奖教师
            setTableRow(table, 0, "奖项名称", cert.getAwardName(), true);
            setTableRow(table, 1, "获奖教师", cert.getTeacherName(), false);

            setTableRow(table, 2, "颁发单位", cert.getOrganization(), false);
            setTableRow(table, 3, "奖项等级", cert.getAwardLevel(), false);
            //如果有备注，填充第6行
            if (cert.getSupplement() != null && !cert.getSupplement().isEmpty()) {


                setTableRow(table, 4, "备注说明", cert.getSupplement(), false);
                setTableRow(table, 5, "类别", cert.getPersonal(), false);
                setTableRow(table, 6, "获奖时间",
                        cert.getAwardTime() != null ? cert.getAwardTime().toString() : "", false);
            } else {
                setTableRow(table, 4, "类别", cert.getPersonal(), false);
                setTableRow(table, 5, "获奖时间",
                        cert.getAwardTime() != null ? cert.getAwardTime().toString() : "", false);
            }
        } else {
            // 按顺序填充各行
            setTableRow(table, 0, "奖项名称", cert.getAwardName(), true);
            setTableRow(table, 1, "颁发单位", cert.getOrganization(), false);
            setTableRow(table, 2, "奖项等级", cert.getAwardLevel(), false);


            // 如果有备注，填充第5行
            if (cert.getSupplement() != null && !cert.getSupplement().isEmpty()) {
                setTableRow(table, 3, "备注说明", cert.getSupplement(), false);
                setTableRow(table, 4, "类别", cert.getPersonal(), false);
                setTableRow(table, 5, "获奖时间",
                        cert.getAwardTime() != null ? cert.getAwardTime().toString() : "", false);
            } else {
                setTableRow(table, 3, "类别", cert.getPersonal(), false);
                setTableRow(table, 4, "获奖时间",
                        cert.getAwardTime() != null ? cert.getAwardTime().toString() : "", false);
            }
        }

    }

    private void setTableRow(XWPFTable table, int rowNum, String label, String value, boolean isTitle) {
        XWPFTableRow row = table.getRow(rowNum);

        // 标签单元格
        XWPFTableCell labelCell = row.getCell(0);
        labelCell.setText(label);
        labelCell.setColor("BDD7EE"); // 深蓝色背景

        // 值单元格：清空默认内容，重新添加带样式（如果需要）的文本
        XWPFTableCell valueCell = row.getCell(1);
        XWPFParagraph para = valueCell.getParagraphs().get(0);
        // 移除段落中已有的所有run
        for (int i = para.getRuns().size() - 1; i >= 0; i--) {
            para.removeRun(i);
        }
        XWPFRun run = para.createRun();
        run.setText(value != null ? value : "");

        if (isTitle) {
            run.setBold(true);
            run.setColor("2E74B5");
            run.setFontSize(12);
        }
    }

    private void addSeparator(XWPFDocument document) {
        XWPFParagraph separator = document.createParagraph();
        XWPFRun run = separator.createRun();
        run.setText("________________________________________________________________");
        run.setColor("BFBFBF");
        run.addBreak();
        run.addBreak();
    }

    private void createFooter(XWPFDocument document) {
        addSeparator(document);

        XWPFParagraph footer = document.createParagraph();
        footer.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun footerRun = footer.createRun();
        footerRun.setText("生成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")));
        footerRun.setColor("7F7F7F");
        footerRun.setFontSize(10);
        footerRun.setItalic(true);
    }

    private byte[] convertToByteArray(XWPFDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.write(baos);
        return baos.toByteArray();
    }
}