package com.it342.g3.backend.controller;

import com.it342.g3.backend.dto.ApiResponse;
import com.it342.g3.backend.dto.CreateSaleRequest;
import com.it342.g3.backend.dto.UpdateSaleRequest;
import com.it342.g3.backend.model.Pen;
import com.it342.g3.backend.model.Pig;
import com.it342.g3.backend.model.Sale;
import com.it342.g3.backend.model.User;
import com.it342.g3.backend.repository.PenRepository;
import com.it342.g3.backend.repository.PigRepository;
import com.it342.g3.backend.repository.SaleRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/sales")
@CrossOrigin(origins = "*")
public class SaleController {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private PigRepository pigRepository;

    @Autowired
    private PenRepository penRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenProvider tokenProvider;

    @GetMapping
    public ResponseEntity<?> getSales(@RequestHeader(value = "Authorization", required = false) String authorization) {
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

        List<Map<String, Object>> payload = sales.stream()
                .sorted(Comparator.comparing(this::resolveSaleTimestamp).reversed())
                .map(this::mapSale)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, payload, "Sales loaded"));
    }

    @PostMapping
    public ResponseEntity<?> createSale(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateSaleRequest request
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

        if (saleRepository.findByPigPigId(pig.getPigId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, null, "Sale already exists for this pig"));
        }

        String buyerName = trimToNull(request.getBuyerName());
        if (buyerName == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Buyer name is required"));
        }

        BigDecimal salePrice = request.getSalePrice();
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Sale price must be greater than zero"));
        }

        Sale sale = new Sale();
        sale.setPig(pig);
        sale.setBuyerName(buyerName);
        sale.setBuyerContact(trimToNull(request.getBuyerContact()));
        sale.setSalePrice(salePrice);
        sale.setSaleDate(request.getSaleDate() != null ? request.getSaleDate() : LocalDate.now());
        sale.setExpectedPickupDate(request.getExpectedPickupDate());
        sale.setActualPickupDate(request.getActualPickupDate());
        sale.setStatus(resolveStatus(request.getStatus(), request.getActualPickupDate()));
        sale.setPaymentStatus(resolvePaymentStatus(request.getPaymentStatus()));
        sale.setNotes(trimToNull(request.getNotes()));

        Sale saved = saleRepository.save(sale);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, mapSale(saved), "Sale recorded"));
    }

    @PutMapping("/{saleId}")
    public ResponseEntity<?> updateSale(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long saleId,
            @RequestBody UpdateSaleRequest request
    ) {
        Long userId = validateAndResolveUserId(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        Sale sale = saleRepository.findById(saleId).orElse(null);
        if (sale == null || sale.getPig() == null || sale.getPig().getPen() == null
                || sale.getPig().getPen().getUser() == null
                || !sale.getPig().getPen().getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Sale not found"), HttpStatus.NOT_FOUND);
        }

        if (request.getBuyerName() != null) {
            sale.setBuyerName(trimToNull(request.getBuyerName()));
        }

        if (request.getBuyerContact() != null) {
            sale.setBuyerContact(trimToNull(request.getBuyerContact()));
        }

        if (request.getSalePrice() != null) {
            if (request.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Sale price must be greater than zero"));
            }
            sale.setSalePrice(request.getSalePrice());
        }

        if (request.getSaleDate() != null) {
            sale.setSaleDate(request.getSaleDate());
        }

        if (request.getExpectedPickupDate() != null) {
            sale.setExpectedPickupDate(request.getExpectedPickupDate());
        }

        if (request.getActualPickupDate() != null) {
            sale.setActualPickupDate(request.getActualPickupDate());
        }

        if (request.getStatus() != null) {
            sale.setStatus(trimToNull(request.getStatus()));
        } else if (request.getActualPickupDate() != null) {
            sale.setStatus(resolveStatus(null, request.getActualPickupDate()));
        }

        if (request.getPaymentStatus() != null) {
            sale.setPaymentStatus(resolvePaymentStatus(request.getPaymentStatus()));
        }

        if (request.getNotes() != null) {
            sale.setNotes(trimToNull(request.getNotes()));
        }

        Sale saved = saleRepository.save(sale);
        return ResponseEntity.ok(new ApiResponse<>(true, mapSale(saved), "Sale updated"));
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

    private Map<String, Object> mapSale(Sale sale) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", sale.getSaleId());

        Pig pig = sale.getPig();
        payload.put("pigId", pig != null ? pig.getPigId() : null);
        payload.put("pigIdentifier", pig != null ? pig.getPigIdentifier() : null);

        payload.put("buyerName", sale.getBuyerName());
        payload.put("buyerContact", sale.getBuyerContact());
        payload.put("salePrice", sale.getSalePrice());
        payload.put("saleDate", sale.getSaleDate());
        payload.put("expectedPickupDate", sale.getExpectedPickupDate());
        payload.put("actualPickupDate", sale.getActualPickupDate());
        payload.put("status", resolveStatus(sale.getStatus(), sale.getActualPickupDate()));
        payload.put("paymentStatus", resolvePaymentStatus(sale.getPaymentStatus()));
        payload.put("notes", sale.getNotes());
        payload.put("createdAt", sale.getCreatedAt());
        payload.put("updatedAt", sale.getUpdatedAt());

        return payload;
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
        return LocalDateTime.MIN;
    }

    private String resolveStatus(String status, LocalDate actualPickupDate) {
        String trimmed = trimToNull(status);
        if (trimmed != null) {
            return trimmed;
        }
        return actualPickupDate != null ? "Sold" : "Pending";
    }

    private String resolvePaymentStatus(String status) {
        String trimmed = trimToNull(status);
        return trimmed != null ? trimmed : "Unpaid";
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
