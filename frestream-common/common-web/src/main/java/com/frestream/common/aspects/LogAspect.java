package com.frestream.common.aspects;

import com.frestream.common.model.logs.LogDTO;
import com.frestream.common.annotations.Log;
import com.frestream.core.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Autowired
    private Environment env;

    /**
     * 切点为使用 @Log 注解标注的方法
     */
    @Pointcut("@annotation(com.frestream.common.annotations.Log)")
    public void pointcut() { }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        LogDTO log = handleLog(joinPoint);
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        log.setExecuteTime(System.currentTimeMillis() - start)
                .setResult(JsonUtil.toJson(proceed));

        return proceed;
    }

    @AfterThrowing(value = "pointcut()", throwing = "ex")
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {

        LogDTO log = handleLog(joinPoint)
                .setErrorMsg(ex.getMessage());
    }

    private LogDTO handleLog(JoinPoint joinPoint) {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = requestAttributes.getRequest();
        String url = request.getRequestURI();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logOperate = method.getAnnotation(Log.class);

        // 请求的类名、方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        // 请求的参数
        List<Object> argList = new ArrayList<>();
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            // 跳过一些特殊的参数类型
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile || arg instanceof BindResult) {
                continue;
            }
            argList.add(arg);
        }

        // 日志对象
        return new LogDTO()
                .setApplicationName(env.getProperty("spring.application.name"))
                .setParam(JsonUtil.toJson(argList))
                .setMethodName(className + "#" + methodName)
                .setUrl(url)
                .setIp(getRemoteHost(request))
                .setDesc(logOperate.desc());
    }

    /**
     * 获取目标主机的ip
     */
    private String getRemoteHost(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
