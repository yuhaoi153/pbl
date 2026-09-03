package flyfish.handler;


import flyfish.exception.BaseException;
import flyfish.pojo.Result;
import io.micrometer.core.ipc.http.HttpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public ResponseEntity<Object> exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("msg",ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

//    /**
//     * 处理SQL语句异常—— 键唯一
//     * @param ex
//     * @return
//     */
//    @ExceptionHandler
//    //重载方法
//    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
//        //Duplicate entry 'lisi' for key 'employee.idx_username'
//        String message = ex.getMessage();
//        if(message.contains("Duplicate entry")){
//        String[] split = message.split(" ");
//        String username = split[2];
//        String msg = username + MessageConstant.ALREADY_EXISTS;
//        return Result.error(msg);
//        }else {
//            return Result.error(MessageConstant.UNKNOWN_ERROR);
//        }
//
//    }
}
