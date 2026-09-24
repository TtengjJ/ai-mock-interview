package interview.guide.infrastructure.file;

import org.springframework.stereotype.Service;

@Service
public class TextCleaningService {

  public String clean(String raw) {
    if (raw == null) {
      return "";
    }
    return raw
        .replace("\u0000", "")
        .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
        .replaceAll("\\n{3,}", "\n\n")
        .trim();
  }
}
