package interview.guide.modules.auth.model;

import jakarta.validation.constraints.NotBlank;

public record CandidateLoginRequest(
    @NotBlank(message = "手机号不能为空") String mobile,
    @NotBlank(message = "密码不能为空") String password
) {}
