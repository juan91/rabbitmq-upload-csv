package co.com.bancolombia.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UploadResult {
    private int total;
    private String boxId;
    private int success;
    private int failed;
    private List<ErrorLine> errors;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class ErrorLine {
        private int lineNumber;
        private String reason;
        private String rawLine;
    }
}