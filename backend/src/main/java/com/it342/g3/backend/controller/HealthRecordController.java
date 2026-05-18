package com.it342.g3.backend.controller;

import com.it342.g3.backend.dto.ApiResponse;
import com.it342.g3.backend.dto.CreateHealthRecordRequest;
import com.it342.g3.backend.model.HealthRecord;
import com.it342.g3.backend.model.Pen;
import com.it342.g3.backend.model.Pig;
import com.it342.g3.backend.model.User;
import com.it342.g3.backend.repository.HealthRecordRepository;
import com.it342.g3.backend.repository.PenRepository;
import com.it342.g3.backend.repository.PigRepository;
import com.it342.g3.backend.service.AuthService;
import com.it342.g3.backend.service.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/api/user/health-records")
@CrossOrigin(origins = "*")
public class HealthRecordController {

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private PigRepository pigRepository;

    @Autowired
    private PenRepository penRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenProvider tokenProvider;

    @GetMapping
    public ResponseEntity<?> getHealthRecords(@RequestHeader(value = "Authorization", required = false) String authorization) {
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
        List<Pig> pigs = penIds.isEmpty() ? List.of() : pigRepository.findByPenPenIdIn(penIds);
        List<Long> pigIds = pigs.stream().map(Pig::getPigId).toList();

        List<HealthRecord> healthRecords = pigIds.isEmpty()
                ? List.of()
                : healthRecordRepository.findByPigPigIdIn(pigIds);

        List<Map<String, Object>> payload = healthRecords.stream()
                .sorted(Comparator.comparing(this::resolveRecordTimestamp).reversed())
                .map(this::mapRecord)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, payload, "Health records loaded"));
    }

    @PostMapping
    public ResponseEntity<?> createHealthRecord(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateHealthRecordRequest request
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Pig pig = resolvePig(request);
        if (pig == null || pig.getPen() == null || pig.getPen().getUser() == null
                || !pig.getPen().getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Pig not found"), HttpStatus.NOT_FOUND);
        }

        if (request.getWeight() != null && request.getWeight().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Weight cannot be negative"));
        }

        if (request.getTemperature() != null && request.getTemperature().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Temperature cannot be negative"));
        }

        HealthRecord record = new HealthRecord();
        record.setPig(pig);
        record.setRecordedBy(user);
        record.setWeight(request.getWeight());
        record.setHealthCondition(trimToNull(request.getHealthCondition()));
        record.setTemperature(request.getTemperature());
        record.setTreatmentGiven(trimToNull(request.getTreatmentGiven()));
        record.setMedicationUsed(trimToNull(request.getMedicationUsed()));
        record.setNextTreatmentDate(request.getNextTreatmentDate());
        record.setNextTreatmentType(trimToNull(request.getNextTreatmentType()));
        record.setCheckupDate(request.getCheckupDate() != null ? request.getCheckupDate() : LocalDateTime.now());
        record.setNotes(trimToNull(request.getNotes()));

        HealthRecord saved = healthRecordRepository.save(record);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, mapRecord(saved), "Health record created"));
    }

    private Pig resolvePig(CreateHealthRecordRequest request) {
        if (request.getPigId() != null) {
            return pigRepository.findById(request.getPigId()).orElse(null);
        }

        String identifier = trimToNull(request.getPigIdentifier());
        if (identifier == null) {
            return null;
        }

        return pigRepository.findByPigIdentifier(identifier).orElse(null);
    }

    private Map<String, Object> mapRecord(HealthRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", record.getHealthRecordId());

        Pig pig = record.getPig();
        payload.put("pigId", pig != null ? pig.getPigId() : null);
        payload.put("pigIdentifier", pig != null ? pig.getPigIdentifier() : null);

        Pen pen = pig != null ? pig.getPen() : null;
        payload.put("penId", pen != null ? pen.getPenId() : null);
        payload.put("penName", pen != null ? pen.getPenName() : null);

        payload.put("weight", record.getWeight());
        payload.put("healthCondition", record.getHealthCondition());
        payload.put("temperature", record.getTemperature());
        payload.put("treatmentGiven", record.getTreatmentGiven());
        payload.put("medicationUsed", record.getMedicationUsed());
        payload.put("nextTreatmentDate", record.getNextTreatmentDate());
        payload.put("nextTreatmentType", record.getNextTreatmentType());
        payload.put("checkupDate", record.getCheckupDate());
        payload.put("notes", record.getNotes());
        payload.put("createdAt", record.getCreatedAt());

        User recordedBy = record.getRecordedBy();
        String recordedName = recordedBy != null
                ? (recordedBy.getFullName() != null ? recordedBy.getFullName() : recordedBy.getUsername())
                : null;
        payload.put("recordedBy", recordedName);
        payload.put("recordedById", recordedBy != null ? recordedBy.getId() : null);

        return payload;
    }

    private LocalDateTime resolveRecordTimestamp(HealthRecord record) {
        if (record.getCheckupDate() != null) {
            return record.getCheckupDate();
        }
        if (record.getCreatedAt() != null) {
            return record.getCreatedAt();
        }
        if (record.getNextTreatmentDate() != null) {
            return record.getNextTreatmentDate().atStartOfDay();
        }
        return LocalDateTime.MIN;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long validateAndResolveUserId(String authorization) {
        return tokenProvider.resolveUserIdFromAuthorization(authorization);
    }
}
