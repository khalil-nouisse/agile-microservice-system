package com.example.sprint_service.feign;

import com.example.sprint_service.feign.dto.WorkItemClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "workitem-service")
public interface WorkItemClient {

    @GetMapping("/api/workitems/{workItemId}")
    WorkItemClientResponse getWorkItem(@PathVariable UUID workItemId);
}
