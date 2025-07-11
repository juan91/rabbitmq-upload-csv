package co.com.bancolombia.mongo.data;

import co.com.bancolombia.model.movement.BoxStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document("LogsMovements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResultData {

    @Id
    private String id;
    private String boxId;
    private int total;
    private int success;
    private int failed;
    private List<ErrorLine> errors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorLine {
        private int lineNumber;
        private String reason;
        private String rawLine;
    }
}