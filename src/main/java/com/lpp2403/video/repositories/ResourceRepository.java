package com.lpp2403.video.repositories;

import com.lpp2403.video.models.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends BaseRepository<Resource, Integer> {
    Optional<Resource> findById(Integer id);
    List<Resource> findAll();
    List<Resource> findByIsReadyFalse();
}
