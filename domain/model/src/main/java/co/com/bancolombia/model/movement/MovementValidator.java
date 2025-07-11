package co.com.bancolombia.model.movement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

public class MovementValidator {

    public static Movement validateAndMap(CsvMovementLine line, Set<String> movementIds, String expectedBoxId) {

        if (!expectedBoxId.equals(line.getBoxId())) {
            throw new IllegalArgumentException("boxId mismatch: " + line.getBoxId());
        }

        if (line.getMovementId() == null || line.getMovementId().isBlank() || line.getMovementId().equalsIgnoreCase("null")) {
            throw new IllegalArgumentException("movementId is required");
        }

        if (!movementIds.add(line.getMovementId())) {
            throw new IllegalArgumentException("movementId is duplicated: " + line.getMovementId());
        }

        LocalDateTime date;
        try {
            date = LocalDateTime.parse(line.getDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + line.getDate());
        }

        MovementType
                type;
        try {
            type = MovementType.valueOf(line.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type: " + line.getType());
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(line.getAmount());
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: " + line.getAmount());
        }

        if (!List.of("COP", "USD").contains(line.getCurrency())) {
            throw new IllegalArgumentException("Unsupported currency: " + line.getCurrency());
        }

        if (line.getDescription() == null || line.getDescription().isBlank() || line.getDescription().length() < 5) {
            throw new IllegalArgumentException("Description is required OR description must be valid");
        }

        return Movement.builder()
                .movementId(line.getMovementId())
                .boxId(line.getBoxId())
                .date(date)
                .type(type)
                .amount(amount)
                .currency(line.getCurrency())
                .description(line.getDescription())
                .build();
    }
}