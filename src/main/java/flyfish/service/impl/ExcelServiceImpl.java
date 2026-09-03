package flyfish.service.impl;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.TextAlignment;
import flyfish.constant.FeedBackConstant;
import flyfish.exception.FileException;
import flyfish.mapper.AccumulateScoreMapper;
import flyfish.mapper.ExcelMapper;
import flyfish.mapper.FeedBackMapper;
import flyfish.mapper.StudentInfoMapper;
import flyfish.pojo.DTO.StudentDTO;
import flyfish.service.ExcelService;
import flyfish.utils.ChineseNameToPinyin;
import flyfish.utils.QRCodeImageUtills;
import org.apache.poi.ss.usermodel.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.itextpdf.layout.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    private ExcelMapper excelMapper;
    @Autowired
    private StudentInfoMapper studentInfoMapper;
    @Autowired
    private AccumulateScoreMapper accumulateScoreMapper;
    @Autowired
    private AccumulateScoreServiceImpl accumulateScoreService;
    @Autowired
    private ChineseNameToPinyin chineseNameToPinyin;
    @Autowired
    private FeedBackMapper feedBackMapper;

    private static final String FONT = "STSong-Light";

    /**
     * 生成学生二维码
     * @param file
     * @return
     */
    @Override
    public ResponseEntity<byte[]> geneCode(MultipartFile file, String username, String school) throws Exception {
        // 将文件内容存储到数据库中
        List<StudentDTO> students = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            List<String> nameList = new ArrayList<>();
            List<String> studentnumberList = new ArrayList<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    org.apache.poi.ss.usermodel.Cell numberCell = row.getCell(1);
                    if ("学号".equals(numberCell.getStringCellValue())) {
                        continue;
                    } else {
                        throw new FileException("上传的文件标题有问题");
                    }
                }

                // 跳过标题行
                org.apache.poi.ss.usermodel.Cell nameCell = row.getCell(0);
                org.apache.poi.ss.usermodel.Cell numberCell = row.getCell(1);

                String name = nameCell != null ? nameCell.getStringCellValue() : null;
                String number = null;
                if (numberCell.getCellType() == CellType.NUMERIC) {
                    double numericValue = numberCell.getNumericCellValue();
                    int intValue = (int) numericValue; // 将double转换为int
                    number = Integer.toString(intValue); // 将int转换为String
                } else if (numberCell == null) {
                } else {
                    throw new FileException("文件学号不是数字");
                }

                if (name != null && number != null) {
                    nameList.add(name);
                    studentnumberList.add(number);
                }
            }

            if(nameList.size() == 0){
                throw new FileException("上传的文件内容为空");
            }else {
                List<String> pinyinList = chineseNameToPinyin.convertNamesToPinyin(nameList);
                for (int i = 0; i < nameList.size(); i++) {
                    StudentDTO student = new StudentDTO();
                    student.setName(nameList.get(i));
                    student.setStudentNumber(studentnumberList.get(i));
                    student.setPinyin(pinyinList.get(i));
                    students.add(student);
                }


                // 先将班级原有的二维码数据删除掉，避免出现重复的情况
                excelMapper.batchDelete(username,school);
                excelMapper.addStudent(students, username,school);}

        } catch (IOException e) {
            throw new RuntimeException(e);
        }





        // 生成二维码
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Table table = new Table(4); // Assuming 4 QR codes per row
        int count = 0;

        // 加载支持中文的字体
        PdfFont font = PdfFontFactory.createFont("https://webtry.oss-cn-shenzhen.aliyuncs.com/FangZhengHeiTiJianTi-1.ttf", "Identity-H", true);

        for (StudentDTO student : students) {
            String name = student.getName();
            String info = student.getStudentNumber() + "/" + username + "--";

            // 创建单元格
            Cell cell = new Cell();

            // 创建段落，设置字体并居中对齐
            Paragraph nameParagraph = new Paragraph(name).setFont(font);
            nameParagraph.setTextAlignment(TextAlignment.CENTER); // 设置文本居中对齐

            Paragraph numberParagraph = new Paragraph(student.getStudentNumber()).setFont(font);
            numberParagraph.setTextAlignment(TextAlignment.CENTER); // 设置文本居中对齐

            // 将段落添加到单元格中
            cell.add(nameParagraph);
            cell.add(numberParagraph);

            QRCodeImageUtills qrtuill = new QRCodeImageUtills();
            byte[] qrCode = qrtuill.generateQRCodeImage(info);
            Image qrImage = new Image(ImageDataFactory.create(qrCode));

            cell.add(qrImage);
            table.addCell(cell);

            count++;

            if (count % 16 == 0) {
                document.add(table);
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                table = new Table(4); // Reset table for next page
            }
        }

        if (count % 16 != 0) {
            document.add(table);
        }

        document.close();

        List<String> nameList = new ArrayList<>();
        for(StudentDTO student : students){
            nameList.add(student.getName());
        }
        //同时更新积分表
        accumulateScoreService.getNameClass(username,nameList,"语文",school);
        accumulateScoreService.getNameClass(username,nameList,"数学",school);
        accumulateScoreService.getNameClass(username,nameList,"英语",school);

        //同时插入反馈常量表
        String subject = "语文";
        Integer collectedNumber = FeedBackConstant.collectedNumber;
        Integer praiseNumber = FeedBackConstant.praiseNumber;
        Integer uncompletedNumber = FeedBackConstant.uncompletedNumber;
        Integer warningNumber = FeedBackConstant.warningNumber;
//        Integer feedbackHour = FeedBackConstant.feedbackHour;
        feedBackMapper.deleteConstant(username,subject,school);
        feedBackMapper.addConsant(username,subject,collectedNumber,praiseNumber,uncompletedNumber,warningNumber,school);
        subject = "数学";
        feedBackMapper.deleteConstant(username,subject,school);
        feedBackMapper.addConsant(username,subject,collectedNumber,praiseNumber,uncompletedNumber,warningNumber,school);
        subject = "英语";
        feedBackMapper.deleteConstant(username,subject,school);
        feedBackMapper.addConsant(username,subject,collectedNumber,praiseNumber,uncompletedNumber,warningNumber,school);



        // Check the size of the ByteArrayOutputStream
        byte[] pdfBytes = byteArrayOutputStream.toByteArray();


        // 返回pdf到前端
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.pdf");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * 检查该班级是否已经有学生信息存在
     * @param classNumber
     * @return
     */
    @Override
    public String checkExcel(String classNumber,String school) {
        String name = excelMapper.checkExcel(classNumber,school);
        if (name != null) {
            return "1";
        } else {
            return "0";
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadQR(String username,String school) throws Exception {
        List<StudentDTO> students = excelMapper.getByUserName(username,school);
        if(students != null){
            // 生成二维码
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(byteArrayOutputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Table table = new Table(4); // Assuming 4 QR codes per row
            int count = 0;

            // 加载支持中文的字体
            PdfFont font = PdfFontFactory.createFont("https://webtry.oss-cn-shenzhen.aliyuncs.com/FangZhengHeiTiJianTi-1.ttf", "Identity-H", true);

            for (StudentDTO student : students) {
                String name = student.getName();
                String info = student.getStudentNumber() + "/" + username + "--";

                // 创建单元格
                Cell cell = new Cell();

                // 创建段落，设置字体并居中对齐
                Paragraph nameParagraph = new Paragraph(name).setFont(font);
                nameParagraph.setTextAlignment(TextAlignment.CENTER); // 设置文本居中对齐

                Paragraph numberParagraph = new Paragraph(student.getStudentNumber()).setFont(font);
                numberParagraph.setTextAlignment(TextAlignment.CENTER); // 设置文本居中对齐

                // 将段落添加到单元格中
                cell.add(nameParagraph);
                cell.add(numberParagraph);

                QRCodeImageUtills qrtuill = new QRCodeImageUtills();
                byte[] qrCode = qrtuill.generateQRCodeImage(info);
                Image qrImage = new Image(ImageDataFactory.create(qrCode));

                cell.add(qrImage);
                table.addCell(cell);

                count++;

                if (count % 16 == 0) {
                    document.add(table);
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                    table = new Table(4); // Reset table for next page
                }
            }

            if (count % 16 != 0) {
                document.add(table);
            }

            document.close();


            // Check the size of the ByteArrayOutputStream
            byte[] pdfBytes = byteArrayOutputStream.toByteArray();


            // 返回pdf到前端
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        }else {
            return null;
        }

    }
}
