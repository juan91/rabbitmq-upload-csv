package co.com.bancolombia.model.movement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CsvMovementLine {
    private String movementId;
    private String boxId;
    private String date;
    private String type;
    private String amount;
    private String currency;
    private String description;
}