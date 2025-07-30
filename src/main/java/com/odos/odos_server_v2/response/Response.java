package com.odos.odos_server_v2.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private String message;
    private Object data;

    public Response(String msg){
        this.message = msg;
    }

    public static Response success(String msg){
        return new Response(msg);
    }
    public static Response success(String msg, Object data){
        return new Response(msg, data);
    }

    public static Response failure(String msg){
        return new Response(msg);
    }
}
