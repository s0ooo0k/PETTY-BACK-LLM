package io.github.petty.tour.repository;

import io.github.petty.tour.entity.ContentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentInfoRepository extends JpaRepository<ContentInfo, Long> {}