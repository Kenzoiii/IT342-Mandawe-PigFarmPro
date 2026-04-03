package com.it342.g3.backend.controller;

import com.it342.g3.backend.dto.ApiResponse;
import com.it342.g3.backend.dto.CreatePigRequest;
import com.it342.g3.backend.dto.UpdatePigRequest;
import com.it342.g3.backend.model.Pen;
import com.it342.g3.backend.model.Pig;
import com.it342.g3.backend.model.User;
import com.it342.g3.backend.repository.PenRepository;
import com.it342.g3.backend.repository.PigRepository;
import com.it342.g3.backend.service.AuthService;
import com.it342.g3.backend.service.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class PigController {

    @Autowired
    private PenRepository penRepository;

    @Autowired
    private PigRepository pigRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping("/pens/{penId}/pigs")
    public ResponseEntity<?> createPig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long penId,
            @RequestBody CreatePigRequest request
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

        String identifier = resolveIdentifier(request.getPigIdentifier());
        if (pigRepository.existsByPigIdentifier(identifier)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, null, "Pig identifier already exists"));
        }

        Pig pig = new Pig();
        pig.setPen(pen);
        pig.setPigIdentifier(identifier);
        pig.setBreed(trimToNull(request.getBreed()));
        pig.setBirthdate(request.getBirthdate());
        pig.setCurrentWeight(request.getCurrentWeight());
        pig.setWeightUnit(resolveWeightUnit(request.getWeightUnit(), request.getCurrentWeight()));
        pig.setGender(trimToNull(request.getGender()));
        pig.setStatus(resolveStatus(request.getStatus()));
        pig.setNotes(trimToNull(request.getNotes()));

        Pig savedPig = pigRepository.save(pig);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, mapPig(savedPig), "Pig created"));
    }

    @PutMapping("/pigs/{pigId}")
    public ResponseEntity<?> updatePig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long pigId,
            @RequestBody UpdatePigRequest request
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Pig pig = pigRepository.findById(pigId).orElse(null);
        if (pig == null || pig.getPen() == null || pig.getPen().getUser() == null
                || !pig.getPen().getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Pig not found"), HttpStatus.NOT_FOUND);
        }

        if (request.getPigIdentifier() != null && !request.getPigIdentifier().trim().isEmpty()) {
            String requestedIdentifier = request.getPigIdentifier().trim();
            if (!requestedIdentifier.equals(pig.getPigIdentifier()) && pigRepository.existsByPigIdentifier(requestedIdentifier)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false, null, "Pig identifier already exists"));
            }
            pig.setPigIdentifier(requestedIdentifier);
        }

        if (request.getBreed() != null) {
            pig.setBreed(trimToNull(request.getBreed()));
        }

        if (request.getBirthdate() != null) {
            pig.setBirthdate(request.getBirthdate());
        }

        if (request.getCurrentWeight() != null) {
            pig.setCurrentWeight(request.getCurrentWeight());
        }

        if (request.getWeightUnit() != null) {
            pig.setWeightUnit(trimToNull(request.getWeightUnit()));
        }

        if (request.getGender() != null) {
            pig.setGender(trimToNull(request.getGender()));
        }

        if (request.getStatus() != null) {
            pig.setStatus(resolveStatus(request.getStatus()));
        }

        if (request.getNotes() != null) {
            pig.setNotes(trimToNull(request.getNotes()));
        }

        Pig savedPig = pigRepository.save(pig);
        return ResponseEntity.ok(new ApiResponse<>(true, mapPig(savedPig), "Pig updated"));
    }

    @DeleteMapping("/pigs/{pigId}")
    public ResponseEntity<?> deletePig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long pigId
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Pig pig = pigRepository.findById(pigId).orElse(null);
        if (pig == null || pig.getPen() == null || pig.getPen().getUser() == null
                || !pig.getPen().getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Pig not found"), HttpStatus.NOT_FOUND);
        }

        pigRepository.delete(pig);
        return ResponseEntity.ok(new ApiResponse<>(true, Map.of("id", pigId), "Pig deleted"));
    }

    private Map<String, Object> mapPig(Pig pig) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", pig.getPigId());
        payload.put("identifier", pig.getPigIdentifier());
        payload.put("breed", pig.getBreed());
        payload.put("birthdate", pig.getBirthdate());
        payload.put("weight", pig.getCurrentWeight());
        payload.put("weightUnit", pig.getWeightUnit());
        payload.put("gender", pig.getGender());
        payload.put("status", pig.getStatus());
        payload.put("notes", pig.getNotes());
        return payload;
    }

    private String resolveIdentifier(String requestedIdentifier) {
        if (requestedIdentifier != null && !requestedIdentifier.trim().isEmpty()) {
            return requestedIdentifier.trim();
        }
        String identifier;
        int attempts = 0;
        do {
            identifier = "PIG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            attempts++;
        } while (pigRepository.existsByPigIdentifier(identifier) && attempts < 5);
        return identifier;
    }

    private String resolveStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Active";
        }
        return status.trim();
    }

    private String resolveWeightUnit(String weightUnit, BigDecimal weight) {
        if (weightUnit != null && !weightUnit.trim().isEmpty()) {
            return weightUnit.trim();
        }
        return weight != null ? "kg" : null;
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
