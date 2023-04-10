package com.example.emotrak.repository;

import com.example.emotrak.entity.Daily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Daily, Long> {
    @Query(value = " SELECT d.id, d.img_url "
            + "   FROM ( "
            + "         SELECT *, "
            + "                ROW_NUMBER() OVER (ORDER BY id DESC) AS rownum "
            + "           FROM daily "
            + "          WHERE emotion_id in (:emo) "
            + "            AND share = true "
            + "         ) AS d "
            + "   WHERE rownum > (:page-1) * :size "
            + "   ORDER BY d.id DESC "
            + "   LIMIT :size", nativeQuery = true)
    List<Object[]> getBoardImages(@Param("page") Long page, @Param("size") Long size, @Param("emo") List<Long> emo);

}
