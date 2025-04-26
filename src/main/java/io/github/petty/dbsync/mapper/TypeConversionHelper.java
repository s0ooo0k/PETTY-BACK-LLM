package io.github.petty.dbsync.mapper;

import io.github.petty.dbsync.dto.DetailCommonDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@RequiredArgsConstructor // ObjectMapper 주입 위해 생성자 필요
public class TypeConversionHelper {

    private static final DateTimeFormatter API_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);



    @Named("StringToInstant")
    public Instant stringToInstant(String apiDateTime) {
        if (apiDateTime == null || apiDateTime.isBlank()) {
            return null;
        }
        try {
            LocalDateTime localDateTime;
            // 길이가 다른 경우 등 처리 (기존 로직 유지)
            if (apiDateTime.length() == 14) {
                localDateTime = LocalDateTime.parse(apiDateTime, API_DATE_TIME_FORMATTER);
            } else {
                return null;
            }

            return localDateTime.toInstant(ZoneOffset.UTC);

        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Named("StringToBigDecimal")
    public BigDecimal stringToBigDecimal(String apiDecimal) {
        if (apiDecimal == null || apiDecimal.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(apiDecimal);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO; // 변환 실패 시
        }
    }

    @Named("StringToInteger")
    public Integer stringToInteger(String apiInteger) {
        if (apiInteger == null || apiInteger.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(apiInteger);
        } catch (NumberFormatException e) {
            return null; // 변환 실패 시
        }
    }

    @Named("dtoToPoint")
    public Point dtoToPoint(DetailCommonDto dto) {
        Point point;
        double x = 0.0, y = 0.0;
        try {
            if (dto != null && dto.getMapX() != null && !dto.getMapX().isBlank() &&
                    dto.getMapY() != null && !dto.getMapY().isBlank()) {
                x = Double.parseDouble(dto.getMapX());
                y = Double.parseDouble(dto.getMapY());
            } else {
                log.warn("MapX or MapY is null/blank for contentId {}. Defaulting coordinates to (0,0).", dto != null ? dto.getContentId() : "UNKNOWN");
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse coordinates MapX='{}', MapY='{}' for contentId {}. Defaulting to (0,0). Error: {}",
                    dto.getMapX(), dto.getMapY(), dto.getContentId(), e.getMessage());
            // Keep x=0.0, y=0.0 as default
        } catch (Exception e) { // Catch broader exceptions just in case
            log.error("Unexpected error parsing coordinates for contentId {}. Defaulting to (0,0).",
                    dto != null ? dto.getContentId() : "UNKNOWN", e);
            // Keep x=0.0, y=0.0 as default
        }
        point = geometryFactory.createPoint(new Coordinate(x, y));
        point.setSRID(4326);
        return point;
    }

    @Named("StringToBooleanY")
    public Boolean stringYToBoolean(String apiFlag) {
        return "Y".equalsIgnoreCase(apiFlag);
    }


}