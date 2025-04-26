package io.github.petty.tour.repository;

import io.github.petty.tour.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> { // Entity is Area, ID type is Integer
}

