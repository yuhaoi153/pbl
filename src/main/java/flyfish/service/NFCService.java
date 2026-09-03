package flyfish.service;



public interface NFCService {

    /**
     * 发送NFc表扬信息
     * @param classNumber
     * @param subject
     * @param name
     * @param situation
     */
    void sendWellNFC(String classNumber, String subject, String name, String situation);
}
