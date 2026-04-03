package com.it342.g3.backend.controller;

import com.it342.g3.backend.dto.ApiResponse;
import com.it342.g3.backend.model.Feeding;
import com.it342.g3.backend.model.HealthRecord;
import com.it342.g3.backend.model.Pen;
import com.it342.g3.backend.model.Pig;
import com.it342.g3.backend.model.Sale;
import com.it342.g3.backend.model.User;
import com.it342.g3.backend.repository.FeedingRepository;
import com.it342.g3.backend.repository.HealthRecordRepository;
import com.it342.g3.backend.repository.PenRepository;
import com.it342.g3.backend.repository.PigRepository;
import com.it342.g3.backend.repository.SaleRepository;
import com.it342.g3.backend.service.AuthService;
import com.it342.g3.backend.service.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private PenRepository penRepository;

    @Autowired
    private PigRepository pigRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private FeedingRepository feedingRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(@RequestHeader(value = "Authorization", required = false) String authorization) {
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

        List<Sale> sales = pigIds.isEmpty() ? List.of() : saleRepository.findByPigPigIdIn(pigIds);
        List<HealthRecord> healthRecords = pigIds.isEmpty() ? List.of() : healthRecordRepository.findByPigPigIdIn(pigIds);
        List<Feeding> feedings = penIds.isEmpty() ? List.of() : feedingRepository.findByPenPenIdIn(penIds);

        Map<Long, Long> pigCountByPen = pigs.stream()
                .filter(pig -> pig.getPen() != null)
                .collect(Collectors.groupingBy(pig -> pig.getPen().getPenId(), Collectors.counting()));

        long pensAtCapacity = pens.stream()
                .filter(pen -> pen.getCapacity() != null && pen.getCapacity() > 0)
                .filter(pen -> pigCountByPen.getOrDefault(pen.getPenId(), 0L) >= pen.getCapacity())
                .count();

        LocalDate today = LocalDate.now();
        long healthAlerts = healthRecords.stream()
                .filter(record -> record.getNextTreatmentDate() != null)
                .filter(record -> !record.getNextTreatmentDate().isAfter(today))
                .count();

        long dueToday = healthRecords.stream()
                .filter(record -> record.getNextTreatmentDate() != null)
                .filter(record -> record.getNextTreatmentDate().isEqual(today))
                .count();

        List<Sale> pendingSales = sales.stream()
                .filter(this::isPendingSale)
                .toList();

        BigDecimal pendingSalesValue = pendingSales.stream()
                .map(Sale::getSalePrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime monthStart = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        long addedThisMonth = pigs.stream()
                .filter(pig -> pig.getAddedAt() != null)
                .filter(pig -> !pig.getAddedAt().isBefore(monthStart))
                .count();

        Map<String, Object> data = new LinkedHashMap<>();

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("fullName", user.getFullName() != null ? user.getFullName() : user.getUsername());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        data.put("profile", profile);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalPigs", pigs.size());
        metrics.put("addedThisMonth", addedThisMonth);
        metrics.put("activePens", pens.size());
        metrics.put("pensAtCapacity", pensAtCapacity);
        metrics.put("pendingSales", pendingSales.size());
        metrics.put("pendingSalesValue", pendingSalesValue.setScale(2, RoundingMode.HALF_UP));
        metrics.put("healthAlerts", healthAlerts);
        metrics.put("healthDueToday", dueToday);
        data.put("metrics", metrics);

        data.put("pens", pens.stream()
            .map(pen -> {
                long pigCount = pigCountByPen.getOrDefault(pen.getPenId(), 0L);
                int capacity = pen.getCapacity() != null ? pen.getCapacity() : 0;
                int available = Math.max(capacity - (int) pigCount, 0);
                double utilization = capacity > 0 ? ((double) pigCount / capacity) * 100.0 : 0.0;

                Map<String, Object> penCard = new LinkedHashMap<>();
                penCard.put("id", pen.getPenId());
                penCard.put("identifier", pen.getPenIdentifier());
                penCard.put("name", pen.getPenName());
                penCard.put("description", pen.getDescription());
                penCard.put("capacity", capacity);
                penCard.put("occupied", pigCount);
                penCard.put("available", available);
                penCard.put("utilization", BigDecimal.valueOf(utilization).setScale(0, RoundingMode.HALF_UP));
                penCard.put("status", available == 0 && capacity > 0 ? "Full" : "Active");
                return penCard;
            })
            .toList());

        data.put("weightTrend", buildWeeklyWeightTrend(healthRecords));
        data.put("activities", buildRecentActivities(pigs, feedings, healthRecords, sales));

        return ResponseEntity.ok(new ApiResponse<>(true, data, "Dashboard loaded"));
    }

    private List<Map<String, Object>> buildWeeklyWeightTrend(List<HealthRecord> healthRecords) {
        LocalDate currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);

        List<LocalDate> weekStarts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            weekStarts.add(currentWeekStart.minusWeeks(i));
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 0; i < weekStarts.size(); i++) {
            LocalDate weekStart = weekStarts.get(i);
            LocalDate weekEnd = weekStart.plusDays(6);

            List<BigDecimal> weekWeights = healthRecords.stream()
                    .filter(record -> record.getCheckupDate() != null)
                    .filter(record -> record.getWeight() != null)
                    .filter(record -> {
                        LocalDate checkupDate = record.getCheckupDate().toLocalDate();
                        return !checkupDate.isBefore(weekStart) && !checkupDate.isAfter(weekEnd);
                    })
                    .map(HealthRecord::getWeight)
                    .toList();

            BigDecimal average = BigDecimal.ZERO;
            if (!weekWeights.isEmpty()) {
                BigDecimal sum = weekWeights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                average = sum.divide(BigDecimal.valueOf(weekWeights.size()), 2, RoundingMode.HALF_UP);
            }

            Map<String, Object> point = new LinkedHashMap<>();
            point.put("label", "W" + (i + 1));
            point.put("value", average);
            trend.add(point);
        }

        return trend;
    }

    private List<Map<String, Object>> buildRecentActivities(
            List<Pig> pigs,
            List<Feeding> feedings,
            List<HealthRecord> healthRecords,
            List<Sale> sales
    ) {
        List<ActivityEvent> events = new ArrayList<>();

        for (Pig pig : pigs) {
            LocalDateTime occurredAt = pig.getAddedAt();
            if (occurredAt != null) {
                String pigIdentifier = pig.getPigIdentifier() != null ? pig.getPigIdentifier() : "new pig";
                events.add(new ActivityEvent(
                        occurredAt,
                        "Added pig " + pigIdentifier,
                        "blue"
                ));
            }
        }

        for (Feeding feeding : feedings) {
            LocalDateTime occurredAt = feeding.getFeedingTime() != null ? feeding.getFeedingTime() : feeding.getCreatedAt();
            if (occurredAt != null) {
                String penName = (feeding.getPen() != null && feeding.getPen().getPenName() != null)
                        ? feeding.getPen().getPenName()
                        : "a pen";
                events.add(new ActivityEvent(
                        occurredAt,
                        "Fed " + penName,
                        "green"
                ));
            }
        }

        for (HealthRecord healthRecord : healthRecords) {
            if (healthRecord.getCheckupDate() != null) {
                String pigIdentifier = (healthRecord.getPig() != null && healthRecord.getPig().getPigIdentifier() != null)
                        ? healthRecord.getPig().getPigIdentifier()
                        : "pig";
                events.add(new ActivityEvent(
                        healthRecord.getCheckupDate(),
                        "Health check · " + pigIdentifier,
                        "rose"
                ));
            }
        }

        for (Sale sale : sales) {
            LocalDateTime occurredAt = resolveSaleTimestamp(sale);
            if (occurredAt != null) {
                events.add(new ActivityEvent(
                        occurredAt,
                        "New sale recorded",
                        "gold"
                ));
            }
        }

        return events.stream()
                .sorted(Comparator.comparing(ActivityEvent::occurredAt).reversed())
                .limit(8)
                .map(event -> {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("title", event.title());
                    payload.put("timeAgo", toTimeAgo(event.occurredAt()));
                    payload.put("tone", event.tone());
                    return payload;
                })
                .toList();
    }

    private LocalDateTime resolveSaleTimestamp(Sale sale) {
        if (sale.getUpdatedAt() != null) {
            return sale.getUpdatedAt();
        }
        if (sale.getCreatedAt() != null) {
            return sale.getCreatedAt();
        }
        if (sale.getSaleDate() != null) {
            return sale.getSaleDate().atStartOfDay();
        }
        return null;
    }

    private boolean isPendingSale(Sale sale) {
        if (sale == null) {
            return false;
        }
        if (sale.getActualPickupDate() != null) {
            return false;
        }

        String status = sale.getStatus();
        if (status == null || status.isBlank()) {
            return true;
        }

        String normalizedStatus = status.trim().toLowerCase();
        return !normalizedStatus.equals("completed") && !normalizedStatus.equals("cancelled");
    }

    private String toTimeAgo(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long minutes = Math.max(duration.toMinutes(), 0);

        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        }

        long days = hours / 24;
        return days + " day" + (days == 1 ? "" : "s") + " ago";
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

    private record ActivityEvent(LocalDateTime occurredAt, String title, String tone) {}
}
