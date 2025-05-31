package com.saveetha.LeaveManagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    private final Map<String, List<String>> notificationMap = new ConcurrentHashMap<>();

    public void sendNotification(String receiverEmpId, Map<String, Object> payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(payload);

            // Send jsonMessage to the intended receiver (log, queue, DB, WebSocket, etc.)
            System.out.println("Sending JSON Notification to " + receiverEmpId + ": " + jsonMessage);

            // Your existing logic to actually deliver the message
            // For example: save to DB, send through WebSocket, etc.

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert notification to JSON", e);
        }
    }


    public List<String> getNotifications(String empId) {
        return notificationMap.getOrDefault(empId, Collections.emptyList());
    }

    public void clearNotifications(String empId) {
        notificationMap.remove(empId);
    }


}
