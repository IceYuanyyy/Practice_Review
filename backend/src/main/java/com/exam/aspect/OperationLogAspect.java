package com.exam.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.annotation.OperationLog;
import com.exam.entity.User;
import com.exam.entity.UserOperationLog;
import com.exam.mapper.UserOperationLogMapper;
import com.exam.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 自动记录带有 @OperationLog 注解的方法执行情况
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private UserOperationLogMapper operationLogMapper;

    @Autowired
    private UserService userService;

    /**
     * 切入点：所有带有 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.exam.annotation.OperationLog)")
    public void operationLogPointcut() {}

    /**
     * 环绕通知：记录操作日志
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        
        String operationType = annotation.type();
        String operationDesc = annotation.desc();
        
        // 获取用户信息
        Long userId = null;
        String username = "unknown";
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
                User user = userService.getById(userId);
                if (user != null) {
                    username = user.getUsername();
                }
            }
        } catch (Exception e) {
            log.debug("获取用户信息失败: {}", e.getMessage());
        }
        
        // 获取请求信息
        String requestIp = getClientIp();
        String requestMethod = "";
        String requestUrl = "";
        String requestParams = "";
        
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                requestIp = getClientIp(request);
                requestMethod = request.getMethod();
                requestUrl = request.getRequestURI();
                // 简化参数记录，避免敏感信息
                requestParams = joinPoint.getArgs().length > 0 ? "[" + joinPoint.getArgs().length + " params]" : "";
            }
        } catch (Exception e) {
            log.debug("获取请求信息失败: {}", e.getMessage());
        }
        
        // 执行目标方法
        Object result = null;
        int operationStatus = 1; // 1: 成功, 0: 失败
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            operationStatus = 0;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            
            // 记录操作日志
            try {
                UserOperationLog opLog = new UserOperationLog();
                opLog.setUserId(userId);
                opLog.setUsername(username);
                opLog.setOperationType(operationType);
                opLog.setOperationDesc(operationDesc);
                opLog.setRequestMethod(requestMethod);
                opLog.setRequestUrl(requestUrl);
                opLog.setRequestParams(requestParams);
                opLog.setRequestIp(requestIp);
                opLog.setOperationStatus(operationStatus);
                opLog.setErrorMessage(errorMessage);
                opLog.setCostTime(costTime);
                opLog.setOperationTime(LocalDateTime.now());
                
                operationLogMapper.insert(opLog);
                log.debug("操作日志记录成功: type={}, user={}, cost={}ms", operationType, username, costTime);
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }
        
        return result;
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return getClientIp(attributes.getRequest());
            }
        } catch (Exception e) {
            log.debug("获取IP失败: {}", e.getMessage());
        }
        return "unknown";
    }
    
    /**
     * 从请求中获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个IP时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
