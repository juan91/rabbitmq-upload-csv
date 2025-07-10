package co.com.bancolombia.model.movement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvMovementLine {
    private String movementId;
    private String boxId;
    private String date;
    private String type;
    private String amount;
    private String currency;
    private String description;
}