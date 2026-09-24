package interview.guide.modules.auth.service;

import interview.guide.common.constant.CommonConstants;
import interview.guide.modules.auth.model.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class AuthContextHolder {

  private AuthContextHolder() {}

  public static AuthSession current() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      return null;
    }
    HttpServletRequest request = attrs.getRequest();
    Object ctx = request.getAttribute(CommonConstants.Auth.ATTR_USER_CONTEXT);
    return ctx instanceof AuthSession session ? session : null;
  }

  public static Long currentCandidateId() {
    AuthSession session = current();
    return session == null ? null : session.userId();
  }
}
