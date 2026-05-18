package com.it342.g3.backend.controller;

import com.it342.g3.backend.dto.ApiResponse;
import com.it342.g3.backend.dto.CreateMortalityRecordRequest;
import com.it342.g3.backend.model.MortalityRecord;
import com.it342.g3.backend.model.Pen;
import com.it342.g3.backend.model.Pig;
import com.it342.g3.backend.model.User;
import com.it342.g3.backend.repository.MortalityRecordRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/mortality")
@CrossOrigin(origins = "*")
public class MortalityRecordController {

    @Autowired
    private MortalityRecordRepository mortalityRecordRepository;

    @Autowired
    private PigRepository pigRepository;

    @Autowired
    private PenRepository penRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenProvider tokenProvider;

    @GetMapping
    public ResponseEntity<?> getMortalityRecords(@RequestHeader(value = "Authorization", required = false) String authorization) {
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

        List<MortalityRecord> records = pigIds.isEmpty()
                ? List.of()
                : mortalityRecordRepository.findByPigPigIdIn(pigIds);

        List<Map<String, Object>> payload = records.stream()
                .sorted(Comparator.comparing(this::resolveRecordTimestamp).reversed())
                .map(this::mapRecord)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, payload, "Mortality records loaded"));
    }

    @PostMapping
    public ResponseEntity<?> createMortalityRecord(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateMortalityRecordRequest request
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Pig pig = resolvePig(request.getPigId(), request.getPigIdentifier());
        if (pig == null || pig.getPen() == null || pig.getPen().getUser() == null
                || !pig.getPen().getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Pig not found"), HttpStatus.NOT_FOUND);
        }

        if (mortalityRecordRepository.findByPigPigId(pig.getPigId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, null, "Mortality record already exists for this pig"));
        }

        LocalDate dateOfDeath = request.getDateOfDeath() != null ? request.getDateOfDeath() : LocalDate.now();
        if (dateOfDeath.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Date of death cannot be in the future"));
        }

        Integer ageAtDeath = request.getAgeAtDeath();
        if (ageAtDeath != null && ageAtDeath < 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Age at death cannot be negative"));
        }

        BigDecimal weightAtDeath = request.getWeightAtDeath();
        if (weightAtDeath != null && weightAtDeath.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Weight at death cannot be negative"));
        }

        MortalityRecord record = new MortalityRecord();
        record.setPig(pig);
        record.setRecordedBy(user);
        record.setDateOfDeath(dateOfDeath);
        record.setAgeAtDeath(resolveAgeAtDeath(pig, dateOfDeath, ageAtDeath));
        record.setCauseOfDeath(trimToNull(request.getCauseOfDeath()));
        record.setWeightAtDeath(weightAtDeath);
        record.setSymptoms(trimToNull(request.getSymptoms()));
        record.setActionsTaken(trimToNull(request.getActionsTaken()));
        record.setNotes(trimToNull(request.getNotes()));

        MortalityRecord saved = mortalityRecordRepository.save(record);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, mapRecord(saved), "Mortality record recorded"));
    }

    private Pig resolvePig(Long pigId, String pigIdentifier) {
        if (pigId != null) {
            return pigRepository.findById(pigId).orElse(null);
        }

        String identifier = trimToNull(pigIdentifier);
        if (identifier == null) {
            return null;
        }

        return pigRepository.findByPigIdentifier(identifier).orElse(null);
    }

    private Integer resolveAgeAtDeath(Pig pig, LocalDate dateOfDeath, Integer requestedAge) {
        if (requestedAge != null) {
            return requestedAge;
        }
        if (pig == null || pig.getBirthdate() == null || dateOfDeath == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(pig.getBirthdate(), dateOfDeath);
        if (days < 0) {
            return null;
        }
        if (days > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) days;
    }

    private Map<String, Object> mapRecord(MortalityRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", record.getMortalityRecordId());

        Pig pig = record.getPig();
        payload.put("pigId", pig != null ? pig.getPigId() : null);
        payload.put("pigIdentifier", pig != null ? pig.getPigIdentifier() : null);

        payload.put("dateOfDeath", record.getDateOfDeath());
        payload.put("ageAtDeath", record.getAgeAtDeath());
        payload.put("causeOfDeath", record.getCauseOfDeath());
        payload.put("weightAtDeath", record.getWeightAtDeath());
        payload.put("symptoms", record.getSymptoms());
        payload.put("actionsTaken", record.getActionsTaken());
        payload.put("notes", record.getNotes());
        payload.put("recordedAt", record.getRecordedAt());

        User recordedBy = record.getRecordedBy();
        String recordedName = recordedBy != null
                ? (recordedBy.getFullName() != null ? recordedBy.getFullName() : recordedBy.getUsername())
                : null;
        payload.put("recordedBy", recordedName);
        payload.put("recordedById", recordedBy != null ? recordedBy.getId() : null);

        return payload;
    }

    private LocalDateTime resolveRecordTimestamp(MortalityRecord record) {
        if (record.getRecordedAt() != null) {
            return record.getRecordedAt();
        }
        if (record.getDateOfDeath() != null) {
            return record.getDateOfDeath().atStartOfDay();
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
