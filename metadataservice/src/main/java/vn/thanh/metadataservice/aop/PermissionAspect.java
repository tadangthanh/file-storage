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
import vn.thanh.metadataservice.service.ICategoryService;
import vn.thanh.metadataservice.service.IFileService;
import vn.thanh.metadataservice.utils.AuthUtils;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

    private final PermissionServiceClient permissionService; // service bạn đã có sẵn (hasPermission)
    private final IFileService fileService;
    private final ICategoryService categoryService;
    @Around("@annotation(vn.thanh.metadataservice.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);

        // Lấy action info
        ResourceType resourceType = annotation.resourceType();
        String resourceParam = annotation.resourceParam();
        int permissionBit = annotation.permissionBit();
        // Tìm resourceId từ args
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        Long resourceId = null;
        for (int i = 0; i < paramNames.length; i++) {
            if (resourceParam.equals(paramNames[i])) {
                if (args[i] instanceof Long id) {
                    resourceId = id;
                } else {
                    throw new IllegalArgumentException("Parameter " + resourceParam + " is not of type Long");
                }
                break;
            }
        }
        if (resourceId == null) {
            throw new IllegalArgumentException("Missing resourceId in method arguments: " + resourceParam);
        }
        // Lấy request context
        UUID userId = AuthUtils.getUserId();
        // check xem có phải chủ sở hữu tài liệu không, nếu phải thì cho qua, nếu k thì kiểm tra permission
        boolean isOwner = switch (resourceType) {
            case DOCUMENT -> fileService.checkUserIsOwner(userId, resourceId);
            case CATEGORY -> categoryService.checkUserIdOwner(userId, resourceId);
            default -> false;
        };
        if (isOwner) {
            return joinPoint.proceed();
        }


        log.info("Checking permission: user={}, type={}, resourceId={}", userId, resourceType, resourceId);

        if (!permissionService.hasPermission(userId, resourceId, resourceType, permissionBit).getData()) {
            log.warn("Access denied: user={}, type={}, resourceId={}", userId, resourceType, resourceId);
            throw new AccessDeniedException("Bạn không có quyền hạn này");
        }

        log.info("Access granted: user={}, type={}, resourceId={}", userId, resourceType, resourceId);

        // Nếu pass
        return joinPoint.proceed();
    }
}
