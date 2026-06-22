package com.ivf.companion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String senderId;
    private String senderName;
    private String content;
    private String timestamp;
}
