package co.com.bancolombia.mongo.data;

import co.com.bancolombia.model.movement.MovementType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("Movements")
public class MovementData {

    @Id
    private String id;
    private String movementId;
    private String boxId;
    private LocalDateTime date;
    private MovementType type; // INCOME o EXPENSE
    private BigDecimal amount;
    private String currency;
    private String description;
}