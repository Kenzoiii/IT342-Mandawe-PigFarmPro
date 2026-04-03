package com.it342.g3.backend.controller;

import com.it342.g3.backend.dto.ApiResponse;
import com.it342.g3.backend.dto.CreatePenRequest;
import com.it342.g3.backend.dto.UpdatePenRequest;
import com.it342.g3.backend.model.Pig;
import com.it342.g3.backend.model.Pen;
import com.it342.g3.backend.model.User;
import com.it342.g3.backend.repository.PenRepository;
import com.it342.g3.backend.repository.PigRepository;
import com.it342.g3.backend.service.AuthService;
import com.it342.g3.backend.service.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/pens")
@CrossOrigin(origins = "*")
public class PenController {

    @Autowired
    private PenRepository penRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PigRepository pigRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping
    public ResponseEntity<?> createPen(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreatePenRequest request
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        if (request.getPenName() == null || request.getPenName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Pen name is required"));
        }

        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Capacity must be greater than zero"));
        }

        Pen pen = new Pen();
        pen.setUser(user);
        pen.setPenName(request.getPenName().trim());
        pen.setPenIdentifier(resolveIdentifier(request.getPenIdentifier()));
        pen.setCapacity(request.getCapacity());
        pen.setDescription(request.getDescription());

        if (penRepository.existsByPenIdentifier(pen.getPenIdentifier())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(false, null, "Pen identifier already exists"));
        }

        Pen savedPen = penRepository.save(pen);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", savedPen.getPenId());
        payload.put("identifier", savedPen.getPenIdentifier());
        payload.put("name", savedPen.getPenName());
        payload.put("description", savedPen.getDescription());
        payload.put("capacity", savedPen.getCapacity());
        payload.put("occupied", 0);
        payload.put("available", savedPen.getCapacity());
        payload.put("utilization", 0);
        payload.put("status", "Active");

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, payload, "Pen created"));
    }

    @PutMapping("/{penId}")
    public ResponseEntity<?> updatePen(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long penId,
            @RequestBody UpdatePenRequest request
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Pen pen = penRepository.findById(penId).orElse(null);
        if (pen == null || pen.getUser() == null || !pen.getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Pen not found"), HttpStatus.NOT_FOUND);
        }

        if (request.getPenName() == null || request.getPenName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Pen name is required"));
        }

        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Capacity must be greater than zero"));
        }

        if (request.getPenIdentifier() != null && !request.getPenIdentifier().trim().isEmpty()) {
            String requestedIdentifier = request.getPenIdentifier().trim();
            if (!requestedIdentifier.equals(pen.getPenIdentifier()) && penRepository.existsByPenIdentifier(requestedIdentifier)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false, null, "Pen identifier already exists"));
            }
            pen.setPenIdentifier(requestedIdentifier);
        }

        pen.setPenName(request.getPenName().trim());
        pen.setCapacity(request.getCapacity());
        String description = request.getDescription();
        pen.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);

        Pen savedPen = penRepository.save(pen);

        List<Pig> pigs = pigRepository.findByPenPenId(penId);
        int capacity = savedPen.getCapacity() != null ? savedPen.getCapacity() : 0;
        int occupied = pigs.size();
        int available = Math.max(capacity - occupied, 0);
        int utilization = capacity > 0 ? (int) Math.round((occupied * 100.0) / capacity) : 0;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", savedPen.getPenId());
        payload.put("identifier", savedPen.getPenIdentifier());
        payload.put("name", savedPen.getPenName());
        payload.put("description", savedPen.getDescription());
        payload.put("capacity", capacity);
        payload.put("occupied", occupied);
        payload.put("available", available);
        payload.put("utilization", utilization);
        payload.put("status", available == 0 && capacity > 0 ? "Full" : "Active");

        return ResponseEntity.ok(new ApiResponse<>(true, payload, "Pen updated"));
    }

        @GetMapping("/{penId}")
    public ResponseEntity<?> getPenDetails(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long penId
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Pen pen = penRepository.findById(penId).orElse(null);
        if (pen == null || pen.getUser() == null || !pen.getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Pen not found"), HttpStatus.NOT_FOUND);
        }

        List<Pig> pigs = pigRepository.findByPenPenId(penId);
        int capacity = pen.getCapacity() != null ? pen.getCapacity() : 0;
        int occupied = pigs.size();
        int available = Math.max(capacity - occupied, 0);
        int utilization = capacity > 0 ? (int) Math.round((occupied * 100.0) / capacity) : 0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", pen.getPenId());
        summary.put("identifier", pen.getPenIdentifier());
        summary.put("name", pen.getPenName());
        summary.put("description", pen.getDescription());
        summary.put("capacity", capacity);
        summary.put("occupied", occupied);
        summary.put("available", available);
        summary.put("status", available == 0 && capacity > 0 ? "Full" : "Active");
        summary.put("utilization", utilization);

        List<Map<String, Object>> pigCards = new ArrayList<>();
        for (Pig pig : pigs) {
            Map<String, Object> pigCard = new LinkedHashMap<>();
            pigCard.put("id", pig.getPigId());
            pigCard.put("identifier", pig.getPigIdentifier());
            pigCard.put("breed", pig.getBreed());
            pigCard.put("birthdate", pig.getBirthdate());
            pigCard.put("weight", pig.getCurrentWeight());
            pigCard.put("weightUnit", pig.getWeightUnit());
            pigCard.put("status", pig.getStatus());
            pigCard.put("gender", pig.getGender());
            pigCard.put("notes", pig.getNotes());
            pigCard.put("addedAt", pig.getAddedAt());
            pigCards.add(pigCard);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pen", summary);
        payload.put("pigs", pigCards);

        return ResponseEntity.ok(new ApiResponse<>(true, payload, "Pen details loaded"));
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private String resolveIdentifier(String requestedIdentifier) {
        if (requestedIdentifier != null && !requestedIdentifier.trim().isEmpty()) {
            return requestedIdentifier.trim();
        }
        return "PEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Long validateAndResolveUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring("Bearer ".length());
        if (!tokenProvider.validateToken(token)) {
            return null;
        }

        try {
            String prefix = "token-for-";
            if (!token.startsWith(prefix)) {
                return null;
            }
            return Long.parseLong(token.substring(prefix.length()));
        } catch (Exception ignored) {
            return null;
        }
    }
}
