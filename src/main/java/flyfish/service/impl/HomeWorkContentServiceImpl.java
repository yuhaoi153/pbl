package flyfish.service.impl;


import flyfish.mapper.HomeWorkContentMapper;
import flyfish.mapper.M_GradeYearMapper;
import flyfish.mapper.StudentInfoMapper;
import flyfish.pojo.DTO.DeleteContetnDTO;
import flyfish.pojo.DTO.HomeWorkContentDTO;

import flyfish.service.HomeWorkContentService;
import flyfish.utils.AliyunAudioRecognitionUtil;

import flyfish.utils.BaiWenXinUtills;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;



@Slf4j
@Service
public class HomeWorkContentServiceImpl implements HomeWorkContentService {
    @Autowired
    private HomeWorkContentMapper homeWorkContentMapper;
    @Autowired
    private M_GradeYearMapper m_GradeYearMapper;



    /**
     * 新增作业类型
     * @param homeWorkContentDTO
     * @return
     */

    public String addcontent(HomeWorkContentDTO homeWorkContentDTO) {
        if(homeWorkContentDTO !=null){
            List<String> querycontent = homeWorkContentMapper.queryContent(homeWorkContentDTO);
            if (querycontent != null && querycontent.size()>0){

            if (homeWorkContentDTO.getContent().equals(querycontent.get(0))){
                return "作业内容已存在";
            }}

            String grade = homeWorkContentDTO.getClassNumber().substring(0,1)+"年级";
            Integer year = m_GradeYearMapper.getYearByGrade(grade);
            homeWorkContentDTO.setYear(year);


            homeWorkContentDTO.setClassNumber(turnChineseClassToNumber(homeWorkContentDTO.getClassNumber()));




            homeWorkContentMapper.addContent(homeWorkContentDTO);
            return "成功新增作业类型";
        }

        return null;
    }

    /**
     * 查询所有的作业类型
     * @param homeWorkContentDTO
     * @return
     */
    @Override
    public List<String> queryContent(HomeWorkContentDTO homeWorkContentDTO) {
        List<String> homeWorkContentDTOS = homeWorkContentMapper.queryContent(homeWorkContentDTO);


        return homeWorkContentDTOS;
    }

    /**
     * 批量删除作业类型
     * @param deleteContetnDTO
     * @return
     */
    @Override
    public String deleteContent(DeleteContetnDTO deleteContetnDTO) {

        List<String> contentList = List.of(deleteContetnDTO.getContent());
        deleteContetnDTO.setContentList(contentList);
        homeWorkContentMapper.batchDelete(deleteContetnDTO);
        String feedbackdelete = "";
        for(String content :deleteContetnDTO.getContentList()){
            feedbackdelete += content + "、" ;
        }
        feedbackdelete += "已删除";
        return feedbackdelete;
    }







    /**
     * 把中文的班级转换成数字
     * @param classNumber
     * @return
     */
    private String turnChineseClassToNumber(String classNumber) {
        if(classNumber != null && !classNumber.isEmpty()) {
            if(classNumber.contains("一")) {
                classNumber = classNumber.replace("一","1");
            }
            if(classNumber.contains("二")) {
                classNumber = classNumber.replace("二","2");
            }
            if(classNumber.contains("三")) {
                classNumber = classNumber.replace("三","3");
            }
            if(classNumber.contains("四")) {
                classNumber = classNumber.replace("四","4");
            }
            if(classNumber.contains("五")) {
                classNumber = classNumber.replace("五","5");
            }
            if(classNumber.contains("六")) {
                classNumber = classNumber.replace("六","6");
            }
            if(classNumber.contains("七")) {
                classNumber = classNumber.replace("七","7");
            }
            if(classNumber.contains("八")) {
                classNumber = classNumber.replace("八","8");
            }
            if(classNumber.contains("九")) {
                classNumber = classNumber.replace("九","9");
            }
        }
        //把(和)替换掉
        if(classNumber != null && !classNumber.isEmpty()) {
            if (classNumber.contains("(")) {
                classNumber = classNumber.replace("(", "");
            }
            if (classNumber.contains(")")) {
                classNumber = classNumber.replace(")", "");
            }
        }
        //把班替换掉
        if(classNumber != null && !classNumber.isEmpty()) {
            if (classNumber.contains("班")) {
                classNumber = classNumber.replace("班", "");
            }
        }
        return classNumber;
    }
}
