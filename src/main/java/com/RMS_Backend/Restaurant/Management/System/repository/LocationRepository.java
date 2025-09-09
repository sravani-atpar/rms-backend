package com.RMS_Backend.Restaurant.Management.System.repository;
import com.RMS_Backend.Restaurant.Management.System.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository  extends JpaRepository<Location, Long>  {
//    List<Location> findByLevelConfigId(Long levelConfigId);
//    Optional<Location> findByCode(String code);
//    List<Location> findByLevelConfigIdAndParentId(Long levelConfigId, Long parentId);
//
//    List<Location> getLocationsByCodeIsIn(List<String> locationsLgdCodes);
//
//    List<Location> getLocationsByIdIsIn(List<Long> locationIds);


}
