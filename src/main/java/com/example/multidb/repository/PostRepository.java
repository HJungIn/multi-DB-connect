package com.example.multidb.repository;

import com.example.multidb.entity.Member;
import com.example.multidb.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
