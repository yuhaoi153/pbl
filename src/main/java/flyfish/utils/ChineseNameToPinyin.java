package flyfish.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class ChineseNameToPinyin {
    // 定义一个多音字的自定义拼音映射表
    private static final Map<Character, String> customPinyinMap = new HashMap<>();

    static {
        customPinyinMap.put('曾', "zeng");
        customPinyinMap.put('柏', "bai");
        // 可以继续添加其他多音字
    }

    public static String convertToPinyin(String chineseName) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE); // 小写格式
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); // 不带声调

        StringBuilder pinyinName = new StringBuilder();

        for (char character : chineseName.toCharArray()) {
            try {
                // 如果字符在自定义拼音映射表中，使用指定的拼音
                if (customPinyinMap.containsKey(character)) {
                    pinyinName.append(customPinyinMap.get(character));
                } else if (Character.toString(character).matches("[\\u4e00-\\u9fa5]")) {
                    // 如果字符是中文字符，使用pinyin4j的拼音转换
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(character, format);
                    if (pinyinArray != null) {
                        pinyinName.append(pinyinArray[0]); // 使用第一个拼音
                    }
                } else {
                    // 非中文字符直接添加
                    pinyinName.append(character);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return pinyinName.toString();
    }

    public  List<String> convertNamesToPinyin(List<String> chineseNames) {
        return chineseNames.stream()
                .map(ChineseNameToPinyin::convertToPinyin)
                .collect(Collectors.toList());
    }


}
