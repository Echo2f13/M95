// src/main/java/com/m95/market95/model/FavStock.java
package com.m95.market95.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "fav_stocks")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@CompoundIndexes({
  // ensure each user can only favorite a given symbol once
  @CompoundIndex(name = "user_stock_unique", def = "{'userToken':1,'favStockName':1}", unique = true)
})
public class FavStock {
  @Id
  private String id;

  @Indexed
  private String userToken;        // foreign key to your JWT user sub or id

  private String favStockName;     // e.g. "AAPL"

  private boolean bought;          // has user marked it as purchased?

  private LocalDate boughtDate;    // when they bought

  private BigDecimal boughtPrice;  // cost basis

  private BigDecimal stopLoss;     // optional stop‐loss

  private BigDecimal targetPrice;  // optional take‐profit

  private Integer quantity;        // number of shares

  private String notes;            // free‐form user notes

  @CreatedDate
  private Instant createdAt;

  @LastModifiedDate
  private Instant updatedAt;
}
