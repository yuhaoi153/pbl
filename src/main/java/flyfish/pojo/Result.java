package flyfish.pojo;

public class Result<T> {
    //先定义几个属性
    private Integer code;
    private String message;
    private Object data;

    //声明了一个无参构造和一个有参构造
    public Result(){
    }
    public  Result(Integer code,String message, Object data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }


//如果是全部数据都有的情况是什么样的
    public static Result success(Object data){
        return new Result(1, "success", data);
    }
    //如果没有数据是什么样子
    public static Result success(){
        return new Result(1,"success",null);
    }
    //如果不成功是什么样的？
    public static Result error(Object data){
        return new Result(0,"failed", data);
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
