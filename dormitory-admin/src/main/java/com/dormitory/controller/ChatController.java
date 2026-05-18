package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.common.Result;
import com.dormitory.entity.ChatMessage;
import com.dormitory.entity.ChatSession;
import com.dormitory.entity.Student;
import com.dormitory.mapper.ChatMessageMapper;
import com.dormitory.mapper.ChatSessionMapper;
import com.dormitory.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ChatController - 智能客服控制器，提供聊天会话管理、智能问答和人工客服接入功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final StudentMapper studentMapper;

    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;

    public ChatController(ChatSessionMapper chatSessionMapper,
                          ChatMessageMapper chatMessageMapper,
                          StudentMapper studentMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.studentMapper = studentMapper;
    }

    /**
     * 创建新的聊天会话
     * @param studentId 学生ID
     * @param chatType 聊天类型（1：智能客服，2：人工客服）
     * @return 创建成功的会话对象
     */
    @PostMapping("/session")
    public Result<ChatSession> createSession(@RequestParam Long studentId, @RequestParam Integer chatType) {
        ChatSession session = new ChatSession();
        session.setStudentId(studentId);
        session.setChatType(chatType);
        session.setStatus(0);
        session.setCreateTime(LocalDateTime.now());
        
        chatSessionMapper.insert(session);
        
        return Result.success(session);
    }

    /**
     * 获取指定学生的所有聊天会话列表
     * @param studentId 学生ID
     * @return 包含会话信息的列表
     */
    @GetMapping("/sessions/{studentId}")
    public Result<List<Map<String, Object>>> getSessions(@PathVariable Long studentId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getStudentId, studentId);
        wrapper.orderByDesc(ChatSession::getCreateTime);
        
        List<ChatSession> sessions = chatSessionMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatSession session : sessions) {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", session.getId());
            map.put("chatType", session.getChatType());
            map.put("status", session.getStatus());
            map.put("createTime", session.getCreateTime());
            
            if (session.getChatType() == 2) {
                map.put("typeText", "人工客服");
            } else {
                map.put("typeText", "智能客服");
            }
            
            LambdaQueryWrapper<ChatMessage> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(ChatMessage::getSessionId, session.getId());
            msgWrapper.orderByDesc(ChatMessage::getCreateTime);
            msgWrapper.last("LIMIT 1");
            ChatMessage lastMsg = chatMessageMapper.selectOne(msgWrapper);
            if (lastMsg != null) {
                map.put("lastMessage", lastMsg.getMessageContent());
            }
            
            result.add(map);
        }
        
        return Result.success(result);
    }

    /**
     * 获取指定会话的所有聊天消息
     * @param sessionId 会话ID
     * @return 消息列表（按时间正序）
     */
    @GetMapping("/messages/{sessionId}")
    public Result<List<ChatMessage>> getMessages(@PathVariable Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        wrapper.orderByAsc(ChatMessage::getCreateTime);
        
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        return Result.success(messages);
    }

    /**
     * 发送聊天消息，如果是智能客服会话则调用AI生成回复
     * @param sessionId 会话ID
     * @param senderType 发送者类型（1：学生，2：AI，3：管理员）
     * @param content 消息内容
     * @param senderId 发送者ID（可选，管理员发送时必填）
     * @return 发送成功的消息对象
     */
    @PostMapping("/message")
    public Result<ChatMessage> sendMessage(@RequestParam Long sessionId,
                                          @RequestParam Integer senderType,
                                          @RequestParam String content,
                                          @RequestParam(required = false) Long senderId) {
        if (senderId == null && senderType == 3) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            senderId = (Long) authentication.getPrincipal();
        }
        
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderType(senderType);
        message.setSenderId(senderId);
        message.setMessageContent(content);
        message.setCreateTime(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
        
        ChatSession session = chatSessionMapper.selectById(sessionId);
        
        if (session != null && session.getChatType() == 1) {
            String aiResponse = getAIResponse(content);
            
            ChatMessage aiMessage = new ChatMessage();
            aiMessage.setSessionId(sessionId);
            aiMessage.setSenderType(2);
            aiMessage.setSenderId(0L);
            aiMessage.setMessageContent(aiResponse);
            aiMessage.setCreateTime(LocalDateTime.now());
            chatMessageMapper.insert(aiMessage);
        }
        
        return Result.success(message);
    }

    /**
     * 获取所有人工客服会话列表（管理员接口）
     * @return 人工客服会话列表
     */
    @GetMapping("/admin/sessions")
    public Result<List<Map<String, Object>>> getAdminSessions() {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getChatType, 2);
        wrapper.orderByDesc(ChatSession::getCreateTime);
        
        List<ChatSession> sessions = chatSessionMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatSession session : sessions) {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", session.getId());
            map.put("studentId", session.getStudentId());
            map.put("status", session.getStatus());
            map.put("createTime", session.getCreateTime());
            
            Student student = studentMapper.selectById(session.getStudentId());
            if (student != null) {
                map.put("studentName", student.getName());
                map.put("studentNumber", student.getStudentNumber());
            }
            
            LambdaQueryWrapper<ChatMessage> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(ChatMessage::getSessionId, session.getId());
            msgWrapper.orderByDesc(ChatMessage::getCreateTime);
            msgWrapper.last("LIMIT 1");
            ChatMessage lastMsg = chatMessageMapper.selectOne(msgWrapper);
            if (lastMsg != null) {
                map.put("lastMessage", lastMsg.getMessageContent());
            }
            
            result.add(map);
        }
        
        return Result.success(result);
    }

    /**
     * 管理员接听人工客服会话
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @PostMapping("/admin/accept")
    public Result<Void> acceptSession(@RequestParam Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = (Long) authentication.getPrincipal();
        
        ChatSession session = chatSessionMapper.selectById(sessionId);
        
        if (session != null) {
            session.setAdminId(adminId);
            session.setStatus(1);
            chatSessionMapper.updateById(session);
        }
        
        return Result.success();
    }

    /**
     * 关闭聊天会话
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @PostMapping("/admin/close")
    public Result<Void> closeSession(@RequestParam Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        
        if (session != null) {
            session.setStatus(2);
            session.setEndTime(LocalDateTime.now());
            chatSessionMapper.updateById(session);
        }
        
        return Result.success();
    }

    /**
     * 调用DeepSeek API获取AI智能回复
     * @param question 用户问题
     * @return AI生成的回复内容
     */
    private String getAIResponse(String question) {
        if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            return "AI客服暂时不可用，请稍后再试或选择人工客服。";
        }
        
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .build();
            
            String requestBody = """
                {
                    "model": "deepseek-chat",
                    "messages": [
                        {"role": "system", "content": "你是用户的AI助手，请用中文友好地回答用户的问题。"},
                        {"role": "user", "content": "%s"}
                    ],
                    "temperature": 0.7
                }
                """.formatted(question);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.deepseek.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + deepseekApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                
                int choicesStart = body.indexOf("\"choices\":[");
                if (choicesStart != -1) {
                    int msgStart = body.indexOf("\"message\":{", choicesStart);
                    if (msgStart != -1) {
                        int contentStart = body.indexOf("\"content\":\"", msgStart);
                        if (contentStart != -1) {
                            contentStart += 10;
                            String afterContent = body.substring(contentStart);
                            int contentEnd = -1;
                            for (int i = 1; i < afterContent.length(); i++) {
                                if (afterContent.charAt(i) == '"') {
                                    contentEnd = contentStart + i;
                                    break;
                                }
                            }
                            if (contentEnd > contentStart) {
                                return body.substring(contentStart, contentEnd)
                                        .replace("?", "")
                                        .replace("\\n", "\n")
                                        .replace("\\t", "\t")
                                        .replace("\\r", "\r")
                                        .replace("\\\"", "\"")
                                        .replace("**", "")
                                        .replace("*", "")
                                        .replace("#", "")
                                        .replace("---", "")
                                        .trim();
                            }
                        }
                    }
                }
            } else {
                return "AI回答生成失败，请选择人工客服。";
            }
        } catch (Exception e) {
            return "AI服务暂时不可用，请选择人工客服。";
        }
        return "AI回答生成失败，请选择人工客服。";
    }
}
