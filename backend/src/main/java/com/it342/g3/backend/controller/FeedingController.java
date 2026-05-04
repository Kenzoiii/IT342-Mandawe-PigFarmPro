package com.it342.g3.backend.controller;

import com.it342.g3.backend.dto.ApiResponse;
import com.it342.g3.backend.dto.CreateFeedingRequest;
import com.it342.g3.backend.dto.UpdateFeedingRequest;
import com.it342.g3.backend.model.Feeding;
import com.it342.g3.backend.model.Pen;
import com.it342.g3.backend.model.User;
import com.it342.g3.backend.repository.FeedingRepository;
import com.it342.g3.backend.repository.PenRepository;
import com.it342.g3.backend.service.AuthService;
import com.it342.g3.backend.service.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/feeding")
@CrossOrigin(origins = "*")
public class FeedingController {

    @Autowired
    private FeedingRepository feedingRepository;

    @Autowired
    private PenRepository penRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenProvider tokenProvider;

    @GetMapping
    public ResponseEntity<?> getFeedings(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        List<Pen> pens = penRepository.findByUserId(userId);
        List<Long> penIds = pens.stream().map(Pen::getPenId).toList();
        List<Feeding> feedings = penIds.isEmpty() ? List.of() : feedingRepository.findByPenPenIdIn(penIds);

        List<Map<String, Object>> payload = feedings.stream()
                .sorted(Comparator.comparing(this::resolveFeedingTimestamp).reversed())
                .map(this::mapFeeding)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, payload, "Feeding history loaded"));
    }

    @PostMapping
    public ResponseEntity<?> createFeeding(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateFeedingRequest request
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Long penId = request.getPenId();
        if (penId == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Pen is required"));
        }

        String feedType = trimToNull(request.getFeedType());
        if (feedType == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Feed type is required"));
        }

        BigDecimal quantity = request.getQuantity();
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Quantity must be greater than zero"));
        }

        BigDecimal cost = request.getCost();
        if (cost != null && cost.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Cost must be zero or greater"));
        }

        Pen pen = penRepository.findById(penId).orElse(null);
        if (pen == null || pen.getUser() == null || !pen.getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Pen not found"), HttpStatus.NOT_FOUND);
        }

        Feeding feeding = new Feeding();
        feeding.setPen(pen);
        feeding.setRecordedBy(user);
        feeding.setFeedType(feedType);
        feeding.setQuantity(quantity);
        feeding.setUnit(resolveUnit(request.getUnit()));
        feeding.setCost(cost);
        feeding.setFeedingTime(request.getFeedingTime() != null ? request.getFeedingTime() : LocalDateTime.now());
        feeding.setNotes(trimToNull(request.getNotes()));

        Feeding saved = feedingRepository.save(feeding);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, mapFeeding(saved), "Feeding recorded"));
    }

    @PutMapping("/{feedingId}")
    public ResponseEntity<?> updateFeeding(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long feedingId,
            @RequestBody UpdateFeedingRequest request
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Feeding feeding = feedingRepository.findById(feedingId).orElse(null);
        if (feeding == null || feeding.getPen() == null || feeding.getPen().getUser() == null
                || !feeding.getPen().getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Feeding record not found"), HttpStatus.NOT_FOUND);
        }

        Long requestedPenId = request.getPenId();
        if (requestedPenId != null) {
            Pen pen = penRepository.findById(requestedPenId).orElse(null);
            if (pen == null || pen.getUser() == null || !pen.getUser().getId().equals(userId)) {
                return new ResponseEntity<>(new ApiResponse<>(false, null, "Pen not found"), HttpStatus.NOT_FOUND);
            }
            feeding.setPen(pen);
        }

        if (request.getFeedType() != null) {
            feeding.setFeedType(trimToNull(request.getFeedType()));
        }

        if (request.getQuantity() != null) {
            if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Quantity must be greater than zero"));
            }
            feeding.setQuantity(request.getQuantity());
        }

        if (request.getUnit() != null) {
            feeding.setUnit(resolveUnit(request.getUnit()));
        }

        if (request.getCost() != null) {
            if (request.getCost().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Cost must be zero or greater"));
            }
            feeding.setCost(request.getCost());
        }

        if (request.getFeedingTime() != null) {
            feeding.setFeedingTime(request.getFeedingTime());
        }

        if (request.getNotes() != null) {
            feeding.setNotes(trimToNull(request.getNotes()));
        }

        Feeding saved = feedingRepository.save(feeding);
        return ResponseEntity.ok(new ApiResponse<>(true, mapFeeding(saved), "Feeding updated"));
    }

    @DeleteMapping("/{feedingId}")
    public ResponseEntity<?> deleteFeeding(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long feedingId
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Feeding feeding = feedingRepository.findById(feedingId).orElse(null);
        if (feeding == null || feeding.getPen() == null || feeding.getPen().getUser() == null
                || !feeding.getPen().getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Feeding record not found"), HttpStatus.NOT_FOUND);
        }

        feedingRepository.delete(feeding);
        return ResponseEntity.ok(new ApiResponse<>(true, Map.of("id", feedingId), "Feeding deleted"));
    }

    private Map<String, Object> mapFeeding(Feeding feeding) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", feeding.getFeedingId());

        Pen pen = feeding.getPen();
        payload.put("penId", pen != null ? pen.getPenId() : null);
        payload.put("penName", pen != null ? pen.getPenName() : null);
        payload.put("penIdentifier", pen != null ? pen.getPenIdentifier() : null);

        payload.put("feedType", feeding.getFeedType());
        payload.put("quantity", feeding.getQuantity());
        payload.put("unit", feeding.getUnit());
        payload.put("cost", feeding.getCost());
        payload.put("feedingTime", feeding.getFeedingTime());
        payload.put("notes", feeding.getNotes());
        payload.put("createdAt", feeding.getCreatedAt());

        User recordedBy = feeding.getRecordedBy();
        String recordedName = recordedBy != null
                ? (recordedBy.getFullName() != null ? recordedBy.getFullName() : recordedBy.getUsername())
                : null;
        payload.put("recordedBy", recordedName);
        payload.put("recordedById", recordedBy != null ? recordedBy.getId() : null);

        return payload;
    }

    private LocalDateTime resolveFeedingTimestamp(Feeding feeding) {
        if (feeding.getFeedingTime() != null) {
            return feeding.getFeedingTime();
        }
        return feeding.getCreatedAt() != null ? feeding.getCreatedAt() : LocalDateTime.MIN;
    }

    private String resolveUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return "kg";
        }
        return unit.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long validateAndResolveUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring("Bearer ".length());
        if (!tokenProvider.validateToken(token)) {
            return null;
        }

        return parseUserIdFromToken(token);
    }

    private Long parseUserIdFromToken(String token) {
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
