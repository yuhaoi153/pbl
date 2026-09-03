package flyfish.service;

public interface AIService {
    /**
     * 调用AI接口
     * @param classNumber
     * @param subject
     * @param message
     */
    String aiPerform(String classNumber, String subject, String message, String school,Integer year);

    /**
     * 调用AI接口分组
     * @param classNumber
     * @param subject
     * @param message
     */
    String group(String classNumber, String subject, String message, String school);

    /**
     * 调用AI接口小组记录
     * @param classNumber
     * @param subject
     * @param message
     */
    String groupPerform(String classNumber, String subject, String message, String school,Integer year);

    String quickPerform(String classNumber, String subject, String message, String school,Integer year);
}
