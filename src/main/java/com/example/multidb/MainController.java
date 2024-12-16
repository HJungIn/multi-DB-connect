package com.example.multidb;

import com.example.multidb.entity.Post;
import com.example.multidb.postgresql.entity.Storage;
import com.example.multidb.postgresql.repository.StorageRepository;
import com.example.multidb.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final PostRepository postRepository;
    private final StorageRepository storageRepository;

    @GetMapping("/posts/{id}")
    public ResponseEntity<String> getPost(@PathVariable Long id){
        return ResponseEntity.ok(postRepository.findById(id).orElseThrow(() -> new RuntimeException("없습니다")).getContent());
    }
    // 결과 : 123

    @GetMapping("/vector/{postId}")
    public ResponseEntity<List<Storage>> getStorageListByPostId(@PathVariable Long postId){
        return ResponseEntity.ok(storageRepository.findByPostId(postId));
    }
    // 결과 : [{"id":1,"data":"11","postId":2},{"id":2,"data":"22","postId":2}]

    @GetMapping("/posts/{id}/vector")
    public ResponseEntity<List<Storage>> getPostAndStorageListByPost(@PathVariable Long id){
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("없습니다"));
        List<Storage> storageList = storageRepository.findByPostId(post.getId());
        return ResponseEntity.ok(storageList);
    }
    // 결과 : [{"id":1,"data":"11","postId":2},{"id":2,"data":"22","postId":2}]
}
