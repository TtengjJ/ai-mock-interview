package interview.guide.common.constant;

/**
 * 通用常量定义
 */
public final class CommonConstants {

  private CommonConstants() {}

  public static final class StatusCode {
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int SERVER_ERROR = 500;

    private StatusCode() {}
  }

  public static final class Pagination {
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private Pagination() {}
  }

  public static final class Auth {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_KEY_PREFIX = "auth:token:";
    public static final String ATTR_USER_CONTEXT = "AUTH_USER_CONTEXT";

    private Auth() {}
  }
}
