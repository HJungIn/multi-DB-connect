package com.example.multidb.postgresql.repository;

import com.example.multidb.postgresql.entity.Storage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findByPostId(Long id);
}
