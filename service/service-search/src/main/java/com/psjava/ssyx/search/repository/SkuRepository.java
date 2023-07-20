package com.psjava.ssyx.search.repository;

import com.psjava.ssyx.model.search.SkuEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SkuRepository extends ElasticsearchRepository<SkuEs, Long> {
    Page<SkuEs> findByOrderByHotScoreDesc(Pageable pageable);
}
