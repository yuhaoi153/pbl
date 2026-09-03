package flyfish.service.impl;

import flyfish.mapper.ClassTeacherMapper;
import flyfish.pojo.ClassTeacher;
import flyfish.service.ClassTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassTeacherServiceImpl implements ClassTeacherService {

    @Autowired
    private ClassTeacherMapper classTeacherMapper;

    /**
     * 通过年级查询教师信息
     * @param grade1
     * @return
     */
    public Map<String,List> getByGrade(String grade1) {
        List<ClassTeacher> classTeacherList = classTeacherMapper.getByGrade(grade1);
        HashMap<String, List> firstlayer = new HashMap<>();
        classTeacherList.forEach(classTeacher ->{
            //将语数英教师存在临时列表中
            ArrayList<String> tempTeacher = new ArrayList<>();
            //用于存储value：teacher集合的
            ArrayList<Map> teacherMap = new ArrayList<>();
            tempTeacher.add(classTeacher.getChineseTeacher());
            tempTeacher.add(classTeacher.getMathTeacher());
            tempTeacher.add(classTeacher.getEnglishTeacher());
            for (Integer i =0 ; i<3; i++){
                HashMap<Object, Object> secondlayer = new HashMap<>();
                secondlayer.put("value",tempTeacher.get(i));
                teacherMap.add(secondlayer);
            }
            firstlayer.put(classTeacher.getClassName(),teacherMap);
        });
        return firstlayer;
    }
}
