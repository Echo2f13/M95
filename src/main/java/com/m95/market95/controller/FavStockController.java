// src/main/java/com/m95/market95/controller/FavStockController.java
package com.m95.market95.controller;

import com.m95.market95.model.FavStock;
import com.m95.market95.repository.FavStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/fav-stocks")
@RequiredArgsConstructor
public class FavStockController {
    private final FavStockRepository favRepo;

    // 1) List all favorites for the logged-in user
    @GetMapping
    public ResponseEntity<List<FavStock>> listFavorites(Principal principal) {
        List<FavStock> favs = favRepo.findByUserToken(principal.getName());
        return ResponseEntity.ok(favs);
    }

    // 2) Add a new favorite
    @PostMapping
    public ResponseEntity<FavStock> addFavorite(
        Principal principal,
        @RequestBody FavStock payload
    ) {
        // Ensure a new ID and bind to the authenticated user
        payload.setId(null);
        payload.setUserToken(principal.getName());

        // If not bought, clear purchase details
        if (!payload.isBought()) {
            payload.setBoughtDate(null);
            payload.setBoughtPrice(null);
            payload.setQuantity(null);
            payload.setStopLoss(null);
            payload.setTargetPrice(null);
            payload.setNotes(null);
        }

        FavStock saved = favRepo.save(payload);
        return ResponseEntity.ok(saved);
    }

    // 3) Update an existing favorite (e.g. mark as bought, set stop-loss, etc.)
    @PutMapping("/{id}")
    public ResponseEntity<FavStock> updateFavorite(
        Principal principal,
        @PathVariable String id,
        @RequestBody FavStock payload
    ) {
        return favRepo.findById(id)
            // ensure the owner matches
            .filter(f -> f.getUserToken().equals(principal.getName()))
            .map(existing -> {
                // copy only the updatable fields
                existing.setBought(payload.isBought());
                existing.setBoughtDate(payload.getBoughtDate());
                existing.setBoughtPrice(payload.getBoughtPrice());
                existing.setStopLoss(payload.getStopLoss());
                existing.setTargetPrice(payload.getTargetPrice());
                existing.setQuantity(payload.getQuantity());
                existing.setNotes(payload.getNotes());
                FavStock updated = favRepo.save(existing);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // 4) Remove a favorite
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(
        Principal principal,
        @PathVariable String id
    ) {
        return favRepo.findById(id)
            .filter(f -> f.getUserToken().equals(principal.getName()))
            .map(fav -> {
                favRepo.delete(fav);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
