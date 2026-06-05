package com.sky.websocket;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();
    /** 记录每个 (sid → empId) 的映射，用于日志追踪 */
    private static final ConcurrentHashMap<String, Long> empIdMap = new ConcurrentHashMap<>();
    private static JwtProperties jwtProperties;

    /**
     * Spring 通过此 setter 注入 JwtProperties（@ServerEndpoint 的实例由容器创建，
     * 不能直接 @Autowired，通过静态字段共享给所有 WebSocket 实例）
     */
    @Autowired
    public void setJwtProperties(JwtProperties jwtProperties) {
        WebSocketServer.jwtProperties = jwtProperties;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {

        try {
//            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), sid);
//            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            sessionMap.put(sid, session);
//            empIdMap.put(sid, empId);
//            log.info("管理员 {} (sid={}) 建立 WebSocket 连接", empId, sid);
            log.info(" (sid={}) 建立 WebSocket 连接",  sid);
        } catch (Exception e) {
            log.warn("WebSocket 认证失败，拒绝连接: {}", e.getMessage());
            try {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.CANNOT_ACCEPT, "Unauthorized"));
            } catch (IOException ex) {
                log.error("关闭未授权连接时异常", ex);
            }
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到来自管理员 {} 的消息: {}", empIdMap.get(sid), message);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        Long empId = empIdMap.remove(sid);
        sessionMap.remove(sid);
        if (empId != null) {
            log.info("管理员 {} (sid={}) 断开连接", empId, sid);
        } else {
            log.info("客户端 {} 断开连接", sid);
        }
    }

    /**
     * 群发消息给所有客户端
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                } else {
                    sessionMap.values().remove(session);
                }
            } catch (IOException e) {
                log.error("发送消息给客户端失败", e);
                sessionMap.values().remove(session);
            }
        }
    }

    /**
     * 向指定客户端发送消息
     */
    public void sendToClient(String sid, String message) {
        Session session = sessionMap.get(sid);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("发送消息给客户端 {} 失败", sid, e);
                sessionMap.remove(sid);
                empIdMap.remove(sid);
            }
        }
    }

    /**
     * 获取当前在线连接数
     */
    public int getOnlineCount() {
        return sessionMap.size();
    }
}
