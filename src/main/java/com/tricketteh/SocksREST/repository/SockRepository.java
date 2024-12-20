package com.tricketteh.SocksREST.repository;

import com.tricketteh.SocksREST.entity.Sock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SockRepository extends JpaRepository<Sock, Long> {

    @Query("""
            SELECT s FROM Sock s
            WHERE (:color IS NULL OR s.color = :color)
            AND (:operation IS NULL OR
                 (:operation = 'moreThan' AND s.cottonPart > :cottonPart) OR
                 (:operation = 'lessThan' AND s.cottonPart < :cottonPart) OR
                 (:operation = 'equal' AND s.cottonPart = :cottonPart))
            AND (:cottonPartMin IS NULL OR s.cottonPart >= :cottonPartMin)
            AND (:cottonPartMax IS NULL OR s.cottonPart <= :cottonPartMax)
            ORDER BY
            CASE WHEN :sortBy = 'color' THEN s.color END ASC,
            CASE WHEN :sortBy = 'cottonPart' THEN s.cottonPart END ASC
            """)
    List<Sock> findFilteredAndSorted(
            @Param("color") String color,
            @Param("operation") String operation,
            @Param("cottonPart") Double cottonPart,
            @Param("cottonPartMin") Double cottonPartMin,
            @Param("cottonPartMax") Double cottonPartMax,
            @Param("sortBy") String sortBy
    );

    @Query("SELECT s.id FROM Sock s WHERE s.color = :color AND s.cottonPart = :cottonPart AND s.quantity = :quantity")
    Optional<Long> findIdByFields(String color, Double cottonPart, Integer quantity);
}
