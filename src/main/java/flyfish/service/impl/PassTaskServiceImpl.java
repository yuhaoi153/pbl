package flyfish.service.impl;

import flyfish.mapper.PassTaskMapper;
import flyfish.pojo.DTO.DeleteContetnDTO;
import flyfish.pojo.DTO.HomeWorkContentDTO;
import flyfish.service.PassTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassTaskServiceImpl implements PassTaskService {
    @Autowired
    private PassTaskMapper passTaskMapper;

    /**
     * 新增过关任务类型
     * @param homeWorkContentDTO
     * @return
     */
    @Override
    public String addContent(HomeWorkContentDTO homeWorkContentDTO) {
        if(homeWorkContentDTO !=null){
            List<String> querycontent = passTaskMapper.queryContent(homeWorkContentDTO);
            if (querycontent != null && querycontent.size()>0){

                if (homeWorkContentDTO.getContent().equals(querycontent.get(0))){
                    return "作业内容已存在";
                }}


            if(homeWorkContentDTO.getImageurl().size() == 0){
                homeWorkContentDTO.setSupplementary(null) ;
            }else {
                String supplementary = String.join("、", homeWorkContentDTO.getImageurl());
                homeWorkContentDTO.setSupplementary(supplementary);
            }
            passTaskMapper.addContent(homeWorkContentDTO);
            return "成功新增作业类型";
        }

        return null;

    }

    /**
     * 查询过关任务
     * @param homeWorkContentDTO
     * @return
     */
    @Override
    public List<String> queryContent(HomeWorkContentDTO homeWorkContentDTO) {
        List<String> passtasklists = passTaskMapper.queryContent(homeWorkContentDTO);

        return passtasklists;
    }

    /**
     * 获取images的列表
     * @param classNumber
     * @param content
     * @param subject
     * @return
     */
    @Override
    public List<String> getImages(String classNumber, String content, String subject) {
        String imagesString = passTaskMapper.getImages(classNumber,content,subject);
        List<String> images= new ArrayList<>();
        if(imagesString !=null){
        images = Arrays.stream(imagesString.split("、"))
                .collect(Collectors.toList());}

        return images;
    }

    /**
     * 删除任务类型
     * @param deleteContetnDTO
     * @return
     */
    @Override
    public String deleteContent(DeleteContetnDTO deleteContetnDTO) {

        passTaskMapper.batchDelete(deleteContetnDTO);
        String feedbackdelete = "";
        for(String content :deleteContetnDTO.getContentList()){
            feedbackdelete += content + "、" ;
        }
        feedbackdelete += "已删除";
        return feedbackdelete;

    }
}
