package io.github.petty.tour.repository;

import io.github.petty.tour.entity.ContentImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentImageRepository extends JpaRepository<ContentImage, Long> {}
