package co.com.bancolombia.mongo.data;

import co.com.bancolombia.model.movement.BoxStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document("Boxes")
public class BoxData {

    @Id
    private String id;
    private String name;
    private BoxStatus status;
    private BigDecimal openingAmount;
    private BigDecimal closingAmount;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal currentBalance;
    private boolean deleted;
    private String userDeletedBox;
    private LocalDateTime dateUpdate;

}