package com.splitforlater.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseEvent {
    public enum Action {
        CREATED, UPDATED, DELETED
    }

    private UUID groupId;
    private UUID expenseId;
    private Action action;

    // Optional: Include the full expense details if you want,
    // but since your consumer fetches full history, just IDs are enough!
    private ExpenseDto expenseDetails;
}
