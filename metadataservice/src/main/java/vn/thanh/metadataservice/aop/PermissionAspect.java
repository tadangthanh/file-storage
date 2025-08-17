package vn.thanh.metadataservice.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.thanh.metadataservice.annotation.RequirePermission;
import vn.thanh.metadataservice.client.PermissionServiceClient;
import vn.thanh.metadataservice.dto.ResourceType;
import vn.thanh.metadataservice.exception.AccessDeniedException;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

    private final PermissionServiceClient permissionService; // service bạn đã có sẵn (hasPermission)

    @Around("@annotation(vn.thanh.metadataservice.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);

        // Lấy action info
        ResourceType resourceType = annotation.resourceType();
        String resourceParam = annotation.resourceParam();
        int permissionBit = annotation.permissionBit();

        // Lấy request context
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        JwtAuthenticationToken auth = (JwtAuthenticationToken) request.getUserPrincipal();
        String sub = auth.getName();
        UUID userId;
        try {
            userId = UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            log.error("Token sub không phải UUID: {}", sub);
            throw new AccessDeniedException("Token không hợp lệ");
        }

        // Tìm resourceId từ args
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        Long resourceId = null;
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(resourceParam)) {
                resourceId = Long.valueOf(args[i].toString());
                break;
            }
        }

        if (resourceId == null) {
            throw new IllegalArgumentException("Missing resourceId in method arguments: " + resourceParam);
        }

        log.info("Check permission for user={} for resourceType={} and resourceId={}", userId, resourceType, resourceId);
        // Check quyền
        if (!permissionService.hasPermission(userId, resourceId, resourceType, permissionBit).getData()) {
            log.info("Access denied for user={} for resourceType={} and resourceId={}", userId, resourceType, resourceId);
            throw new AccessDeniedException("Bạn không có quyền hạn này");
        }
        log.info("Permission check passed for user={} for resourceType={} and resourceId={}", userId, resourceType, resourceId);
        // Nếu pass
        return joinPoint.proceed();
    }
}
