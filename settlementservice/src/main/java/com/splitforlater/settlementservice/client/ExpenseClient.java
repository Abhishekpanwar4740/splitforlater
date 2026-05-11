package com.splitforlater.settlementservice.client;

import com.splitforlater.settlementservice.dto.ExpenseResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Service
@FeignClient(name = "expense-service", url = "${EXPENSE_SERVICE_URL:http://localhost:8083}")
public interface ExpenseClient {

    @GetMapping("/api/expenses/group/{groupId}")
    List<ExpenseResponseDto> getExpensesByGroup(@PathVariable("groupId") UUID groupId);
}
