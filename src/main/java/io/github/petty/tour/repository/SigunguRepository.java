package io.github.petty.tour.repository;


import io.github.petty.tour.entity.Sigungu;
import io.github.petty.tour.entity.SigunguId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SigunguRepository extends JpaRepository<Sigungu, SigunguId> { // Entity is Sigungu, ID type is SigunguId

    List<Sigungu> findByAreaCode(Integer areaCode);
}