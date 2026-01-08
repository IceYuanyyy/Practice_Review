package com.exam.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP 工具类
 * 提供获取客户端真实IP和IP地理位置查询功能
 *
 * @author Exam System
 * @since 2025-12-27
 */
@Slf4j
public class IpUtil {

    private static final String UNKNOWN = "unknown";
    
    // IP归属地缓存，避免频繁请求API
    private static final ConcurrentHashMap<String, String> IP_LOCATION_CACHE = new ConcurrentHashMap<>();
    
    // 本地IP
    private static final String LOCAL_IP = "127.0.0.1";
    private static final String LOCAL_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端真实IP地址
     * 支持多级代理的情况
     *
     * @param request HTTP请求
     * @return 客户端IP
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        String ip = null;
        
        // 尝试从各种代理头中获取真实IP
        // 1. X-Forwarded-For: 标准的代理头，格式为 client, proxy1, proxy2, ...
        ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // 多个IP时取第一个（即真实客户端IP）
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }
        
        // 2. X-Real-IP: Nginx代理常用
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 3. Proxy-Client-IP: Apache代理
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 4. WL-Proxy-Client-IP: WebLogic代理
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 5. HTTP_CLIENT_IP
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 6. HTTP_X_FORWARDED_FOR
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 7. 如果都没有，使用 RemoteAddr
        ip = request.getRemoteAddr();
        
        // 处理IPv6本地地址
        if (LOCAL_IPV6.equals(ip)) {
            ip = LOCAL_IP;
        }
        
        return ip;
    }
    
    /**
     * 判断IP是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }
    
    /**
     * 判断是否为本地IP
     */
    public static boolean isLocalIp(String ip) {
        return LOCAL_IP.equals(ip) || LOCAL_IPV6.equals(ip) || 
               (ip != null && (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")));
    }

    /**
     * 根据IP获取地理位置信息
     * 格式：xx省xx市
     *
     * @param ip IP地址
     * @return 地理位置，如 "广东省深圳市"
     */
    public static String getIpLocation(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "未知位置";
        }
        
        // 本地IP直接返回
        if (isLocalIp(ip)) {
            return "本地访问";
        }
        
        // 从缓存获取
        String cachedLocation = IP_LOCATION_CACHE.get(ip);
        if (cachedLocation != null) {
            return cachedLocation;
        }
        
        String location = "未知位置";
        
        // 尝试多个免费API
        location = getLocationFromPconlineApi(ip);
        if (!"未知位置".equals(location)) {
            IP_LOCATION_CACHE.put(ip, location);
            return location;
        }
        
        location = getLocationFromIpApiCom(ip);
        if (!"未知位置".equals(location)) {
            IP_LOCATION_CACHE.put(ip, location);
            return location;
        }
        
        location = getLocationFromWhoisApi(ip);
        if (!"未知位置".equals(location)) {
            IP_LOCATION_CACHE.put(ip, location);
        }
        
        return location;
    }
    
    /**
     * 使用太平洋网络IP地址查询API
     * 支持国内IP地址查询，精确到省市
     */
    private static String getLocationFromPconlineApi(String ip) {
        try {
            String url = "https://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&json=true";
            String response = HttpUtil.get(url, 3000);
            
            if (response != null && !response.isEmpty()) {
                JSONObject json = JSONUtil.parseObj(response);
                String pro = json.getStr("pro", "");  // 省
                String city = json.getStr("city", ""); // 市
                
                if (!pro.isEmpty() || !city.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    if (!pro.isEmpty()) {
                        sb.append(pro);
                    }
                    if (!city.isEmpty() && !city.equals(pro)) {
                        sb.append(city);
                    }
                    String result = sb.toString();
                    if (!result.isEmpty()) {
                        log.debug("Pconline API 查询成功: ip={}, location={}", ip, result);
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Pconline API 查询失败: ip={}, error={}", ip, e.getMessage());
        }
        return "未知位置";
    }
    
    /**
     * 使用 ip-api.com 免费API
     * 支持全球IP查询
     */
    private static String getLocationFromIpApiCom(String ip) {
        try {
            String url = "http://ip-api.com/json/" + ip + "?lang=zh-CN";
            String response = HttpUtil.get(url, 3000);
            
            if (response != null && !response.isEmpty()) {
                JSONObject json = JSONUtil.parseObj(response);
                if ("success".equals(json.getStr("status"))) {
                    String regionName = json.getStr("regionName", ""); // 省
                    String city = json.getStr("city", ""); // 市
                    String country = json.getStr("country", ""); // 国家
                    
                    StringBuilder sb = new StringBuilder();
                    // 如果是中国，显示省市
                    if ("中国".equals(country) || "China".equals(country)) {
                        if (!regionName.isEmpty()) {
                            // 确保省份后缀
                            if (!regionName.endsWith("省") && !regionName.endsWith("市") && 
                                !regionName.endsWith("区") && !regionName.contains("自治")) {
                                sb.append(regionName).append("省");
                            } else {
                                sb.append(regionName);
                            }
                        }
                        if (!city.isEmpty() && !city.equals(regionName)) {
                            if (!city.endsWith("市") && !city.endsWith("区") && !city.endsWith("县")) {
                                sb.append(city).append("市");
                            } else {
                                sb.append(city);
                            }
                        }
                    } else {
                        // 国外IP显示国家+城市
                        if (!country.isEmpty()) {
                            sb.append(country);
                        }
                        if (!city.isEmpty()) {
                            sb.append(" ").append(city);
                        }
                    }
                    
                    String result = sb.toString().trim();
                    if (!result.isEmpty()) {
                        log.debug("ip-api.com 查询成功: ip={}, location={}", ip, result);
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("ip-api.com 查询失败: ip={}, error={}", ip, e.getMessage());
        }
        return "未知位置";
    }
    
    /**
     * 使用 whois.pconline.com.cn API (备用)
     */
    private static String getLocationFromWhoisApi(String ip) {
        try {
            String url = "https://whois.pconline.com.cn/ip.jsp?ip=" + ip;
            String response = HttpUtil.get(url, 3000);
            
            if (response != null && !response.isEmpty()) {
                // 响应格式类似: "广东省深圳市 电信"
                String location = response.trim();
                // 移除运营商信息
                if (location.contains(" ")) {
                    location = location.split(" ")[0];
                }
                if (!location.isEmpty() && !location.contains("错误") && !location.contains("error")) {
                    log.debug("Whois API 查询成功: ip={}, location={}", ip, location);
                    return location;
                }
            }
        } catch (Exception e) {
            log.debug("Whois API 查询失败: ip={}, error={}", ip, e.getMessage());
        }
        return "未知位置";
    }
    
    /**
     * 获取IP和位置信息的组合字符串
     * 
     * @param request HTTP请求
     * @return 格式如 "192.168.1.1 (广东省深圳市)"
     */
    public static String getIpWithLocation(HttpServletRequest request) {
        String ip = getClientIp(request);
        String location = getIpLocation(ip);
        return ip + " (" + location + ")";
    }
    
    /**
     * 清除IP位置缓存
     */
    public static void clearCache() {
        IP_LOCATION_CACHE.clear();
    }
}
