package com.example.kyc.common.dto;



import com.example.kyc.handler.StatusCode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Message {
    public static final String DEFAULT_RESPONSE = "Request processed successfully";
    private int statusCode;
    private String message;
    private Object data;

    public Message(StatusCode statusCode, Object data) {
        this.statusCode = statusCode.getStatusCode();
        this.message = statusCode.getMessage();
        this.data = data;
    }

    public Message(StatusCode statusCode) {
        this.statusCode = statusCode.getStatusCode();
        this.message = statusCode.getMessage();
        this.data = DEFAULT_RESPONSE;
    }
}
