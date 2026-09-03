package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notice {
    private String name;
    private String subject;
    private String phone;
    private String mail;
    private boolean checkMail;
    private boolean checkPhone;
    private String classNumber;
    private String school;
}
