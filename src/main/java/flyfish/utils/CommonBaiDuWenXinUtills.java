package flyfish.utils;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.model.chat.ChatResponse;
import flyfish.properties.BaiDuWenXinProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class CommonBaiDuWenXinUtills {
    @Autowired
    private BaiDuWenXinProperties baiDuWenXinProperties;



    public  String getJsonFeedback(String message, String prompt)  {
        Qianfan qianfan = new Qianfan(
                baiDuWenXinProperties.getSdkAccessKey(),
                baiDuWenXinProperties.getSdkSecretKey()
        );

        ChatResponse response = qianfan.chatCompletion()
                .model("ERNIE-4.0-Turbo-8K") // 使用model指定预置模型
                 .endpoint("completions_pro") // 也可以使用endpoint指定任意模型 (二选一)
                .addMessage("user", prompt) // 添加用户消息
                .temperature(0.7) // 自定义超参数
                .execute(); // 发起请求
        log.info("response:{}",response.getResult());
        return response.getResult();
    }


}
