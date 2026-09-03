package flyfish.utils;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClassNameChangeUtills {


    // 中文数字与阿拉伯数字的映射（只处理一到六）
    private static final Map<String, Integer> CHINESE_TO_NUM = new HashMap<>();
    private static final String[] NUM_TO_CHINESE = {"", "一", "二", "三", "四", "五", "六"};

    static {
        CHINESE_TO_NUM.put("一", 1);
        CHINESE_TO_NUM.put("二", 2);
        CHINESE_TO_NUM.put("三", 3);
        CHINESE_TO_NUM.put("四", 4);
        CHINESE_TO_NUM.put("五", 5);
        CHINESE_TO_NUM.put("六", 6);
    }

    /**
     * 将“一(8)班”格式转换为“18”
     * @param longName 如："一(8)班"、"二(12)班"
     * @return 短格式，如："18"、"212"；若格式不匹配则返回 null
     */
    public String formatToNumber(String longName) {
        if (longName == null || longName.isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile("^([一二三四五六])\\((\\d+)\\)班$");
        Matcher matcher = pattern.matcher(longName);
        if (!matcher.matches()) {
            return null; // 格式错误
        }
        String gradeChinese = matcher.group(1);
        String classNum = matcher.group(2);
        Integer gradeNum = CHINESE_TO_NUM.get(gradeChinese);
        if (gradeNum == null) {
            return null;
        }
        return gradeNum + classNum;
    }

    /**
     * 将“18”格式转换为“一(8)班”
     * @param shortName 如："18"、"212"（第一位数字为年级1-6，后续为班级号）
     * @return 长格式，如："一(8)班"、"二(12)班"；若格式不匹配则返回 null
     */
    public String formatToChinese(String shortName) {
        if (shortName == null || shortName.isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile("^([1-6])(\\d+)$");
        Matcher matcher = pattern.matcher(shortName);
        if (!matcher.matches()) {
            return null; // 格式错误
        }
        int gradeNum = Integer.parseInt(matcher.group(1));
        String classNum = matcher.group(2);
        if (gradeNum < 1 || gradeNum > 6) {
            return null;
        }
        String gradeChinese = NUM_TO_CHINESE[gradeNum];
        return gradeChinese + "(" + classNum + ")班";
    }
}
