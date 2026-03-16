package cn.ac.sitp.infrared.ai.controller;

import cn.ac.sitp.infrared.ai.model.ChatRequest;
import cn.ac.sitp.infrared.ai.model.ChatResponse;
import cn.ac.sitp.infrared.ai.service.AiChatService;
import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.security.SessionAccountHelper;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.util.Util;
import cn.ac.sitp.infrared.web.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rest/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final HttpServletRequest request;
    private final AiChatService aiChatService;
    private final AuditLogService logService;

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest chatRequest) {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        String description = LogActionEnum.AI_CHAT.getDescription() + " " + chatRequest.getMessage();
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.AI_CHAT, description);

        ChatResponse response = aiChatService.processMessage(
                chatRequest.getMessage(),
                chatRequest.getConversationId(),
                user,
                request
        );

        Map<String, Object> contents = new HashMap<>();
        contents.put("reply", response.getReply());
        contents.put("data", response.getData());
        contents.put("actions", response.getActions());
        contents.put("conversationId", response.getConversationId());
        if (response.getTraceId() != null) {
            contents.put("traceId", response.getTraceId());
        }

        logService.saveAuditLog(ip, LogActionEnum.AI_CHAT, Util.STATUS_SUCCESS,
                new Date(), null, description, user);

        return Util.suc(contents);
    }
}
