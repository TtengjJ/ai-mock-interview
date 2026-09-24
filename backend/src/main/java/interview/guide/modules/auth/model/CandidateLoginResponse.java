package interview.guide.modules.auth.model;

public record CandidateLoginResponse(String token, String realName, Long candidateId) {}
