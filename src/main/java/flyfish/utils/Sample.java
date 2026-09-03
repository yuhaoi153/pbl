package flyfish.utils;

import okhttp3.*;

import java.io.IOException;

class Sample {

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public static void main(String[] args) throws IOException {
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token?client_id=zJE4PoferCzKq9feyEU8w0ge&client_secret=QWVm7NdI48UYd9gHS1yb7drfrcP3lMS8&grant_type=client_credentials")
                .method("GET", null) // 对于GET请求，method方法的第二个参数可以是null
                .addHeader("Accept", "application/json") // 通常不需要为GET请求添加Content-Type头，因为GET请求不包含请求体
                .build();

        Response response = HTTP_CLIENT.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        System.out.println(response.body().string());
    }
}