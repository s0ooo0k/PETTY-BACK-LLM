package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class TourSummaryDto {
    private Long contentId;
    private String title;
    private String addr1;
    private Integer contentTypeId;
    private String firstImage; // Thumbnail
    private String cat1;

    // Optional: Add distance for location-based search results
    private Double distanceMeters;
}
