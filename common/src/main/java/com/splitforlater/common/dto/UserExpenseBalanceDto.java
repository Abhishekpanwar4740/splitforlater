package com.splitforlater.common.dto;

import java.io.Serializable;
import java.util.UUID;

public class UserExpenseBalanceDto implements Serializable {
    private UUID userId;
    private UUID groupId;
    private Long balanceAmount;
}
