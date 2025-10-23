package top.gaogle.framework.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.gaogle.common.RegisterConst;
import top.gaogle.framework.util.DateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成令牌，验证等一些操作
 *
 * @author goge
 * @since 1.0.0
 */
public class Auth0TokenUtil {
    private static final Logger log = LoggerFactory.getLogger(Auth0TokenUtil.class);

    private Auth0TokenUtil() {
        throw new IllegalStateException(RegisterConst.PROHIBIT_INSTANTIATION);
    }

    private static final String AUTHENTICATION_SECRET = RegisterConst.AUTHENTICATION_SECRET + System.getenv(RegisterConst.AUTHENTICATION_SECRET);
    private static final Integer AUTHENTICATION_EXPIRATION_HOURS = RegisterConst.AUTHENTICATION_EXPIRATION_HOURS;
    public static final String AUTHENTICATION_HEADER = RegisterConst.GO_GE;
    private static final String AUTHENTICATION_ISSUER = RegisterConst.GO_GE;

    static {
        String envValue = System.getenv(RegisterConst.AUTHENTICATION_SECRET);
        if (StringUtils.isEmpty(envValue)) {
            log.error("Environment variable {} not found or empty! AUTHENTICATION_SECRET =={}", RegisterConst.AUTHENTICATION_SECRET, AUTHENTICATION_SECRET);
            throw new IllegalStateException("Startup aborted: Missing environment variable " + RegisterConst.AUTHENTICATION_SECRET);
        }
    }

    public static String generateToken(UserDetailsCustomizer userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("enterpriseId", userDetails.getEnterpriseId());
        claims.put("moduleNumMap", userDetails.getModuleNumMap());
        claims.put("roleNames", userDetails.getRoleNames());
        return generateToken(claims);
    }

    public static String generateToken(Map<String, Object> claims) {
        claims.put("sub", "username");
        claims.put(RegisterConst.IAT, DateUtil.currentSeconds()); // 发行时间，单位为毫秒
        return JWT.create().withIssuer(AUTHENTICATION_ISSUER).withPayload(claims).withExpiresAt(DateUtil.getDatePlusHour(AUTHENTICATION_EXPIRATION_HOURS)).withSubject(claims.get("username") + "").sign(Algorithm.HMAC256(AUTHENTICATION_SECRET));
    }

    public static Boolean validateToken(String token) {
        try {
            // 验证令牌并提取有效载荷
            DecodedJWT decodedJWT = getDecodedJWT(token);
            // 检查令牌是否过期
            Date expirationDate = decodedJWT.getExpiresAt();
            if (expirationDate.before(new Date())) {
                return false; // 令牌已过期
            }
            // 检查签发人
            String issuer = decodedJWT.getIssuer();
            return AUTHENTICATION_ISSUER.equals(issuer);
        } catch (JWTVerificationException e) {
            return false; // 令牌验证失败
        }
    }

    public static Boolean validateSignTime(String signTime) {
        try {
            return true;
        } catch (Exception e) {
            return false; // 验证失败
        }
    }

    public static String getUsernameFromToken(String token) {
        return getDecodedJWT(token).getSubject();
    }

    public static DecodedJWT getDecodedJWT(String token) {
        return JWT.require(Algorithm.HMAC256(AUTHENTICATION_SECRET)).withIssuer(AUTHENTICATION_ISSUER).build().verify(token);
    }

}
