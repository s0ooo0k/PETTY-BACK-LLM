package io.github.petty.tour.repository;

import io.github.petty.tour.entity.PetTourInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetTourInfoRepository extends JpaRepository<PetTourInfo, Long> {}