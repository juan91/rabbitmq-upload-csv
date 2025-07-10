package co.com.bancolombia.model.movement;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Movement {
    private String movementId;
    private String boxId;
    private LocalDateTime date;
    private MovementType type; // INCOME o EXPENSE
    private BigDecimal amount;
    private String currency;
    private String description;

}
