package com.java.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * IpUtils
 *
 * @author Giles.wu
 * @date 2016/10/11 11:28
 */
public class IpUtils {

    static String localIp = null;

    /**
     * 获取请求的客户端的IP地址
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");//X-Forwarded-For  x-forwarded-for
        /*if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }*/
        if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1")  || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                ipAddress = getLocalNetWorkIp();
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    /**
     * 获取本机的网络IP
     */
    public static String getLocalNetWorkIp() {
        if (localIp != null) {
            return localIp;
        }
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (netInterfaces.hasMoreElements()) {// 遍历所有的网卡
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                if (ni.isLoopback() || ni.isVirtual()) {// 如果是回环和虚拟网络地址的话继续
                    continue;
                }
                Enumeration<InetAddress> addresss = ni.getInetAddresses();
                while (addresss.hasMoreElements()) {
                    InetAddress address = addresss.nextElement();
                    if (address instanceof Inet4Address) {// 这里暂时只获取ipv4地址
                        ip = address;
                        break;
                    }
                }
                if (ip != null) {
                    break;
                }
            }
            if (ip != null) {
                localIp = ip.getHostAddress();
            } else {
                localIp = "127.0.0.1";
            }
        } catch (Exception e) {
            localIp = "127.0.0.1";
        }
        return localIp;
    }

    /**
     * 客户端上送的ip无效时，设置一个固定ip
     * @param tempIp
     * @return
     */
    public static String getClientIp(String tempIp){
        if(StringUtils.isEmpty(tempIp) || "error".equals(tempIp) || !isIpValid(tempIp)){
            tempIp = "192.168.6.57";
        }
        return tempIp;
    }

    /** 校验Ip地址是否合法
     * @param addr
     * @return
     */
    public static boolean isIpValid(String addr){
        try{
            String[] ipStr = new String[4];
            int[] ipb = new int[4];
            StringTokenizer tokenizer = new StringTokenizer(addr, ".");
            int len = tokenizer.countTokens();

            if (len != 4){
                return false;
            }
            int i = 0;
            while (tokenizer.hasMoreTokens()){
                ipStr[i] = tokenizer.nextToken();
                ipb[i] = (new Integer(ipStr[i])).intValue();

                if (ipb[i] < 0 || ipb[i] > 255){
                    return false;
                }
                i++;
            }
            if (ipb[0] > 0){
                return true;
            }
        }
        catch (Exception e){}
        return false;
    }

    public static void main(String[] args) {
        System.out.println(IpUtils.getClientIp("127.0.0.1"));
    }

}
