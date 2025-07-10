package co.com.bancolombia.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BoxMovementsUploadedEvent {
    private String boxId;
    private int totalLines;
    private int successCount;
    private int failedCount;
    private LocalDateTime timestamp;
    private String uploadedBy;
}