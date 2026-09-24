package interview.guide.modules.auth.model;

public record MeResponse(
    Long userId,
    String role,
    String realName,
    String usernameOrMobile
) {}
