// src/main/java/com/m95/market95/repository/FavStockRepository.java
package com.m95.market95.repository;

import com.m95.market95.model.FavStock;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FavStockRepository extends MongoRepository<FavStock, String> {
  List<FavStock> findByUserToken(String userToken);
}
