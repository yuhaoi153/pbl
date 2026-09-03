package flyfish.service;

public interface DirectMailService {
    public void sendMail(String toEmail, String subject, String content) throws Exception;
}
