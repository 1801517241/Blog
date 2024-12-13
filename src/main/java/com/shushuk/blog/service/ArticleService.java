package com.shushuk.blog.service;

import com.shushuk.blog.model.Article;
import com.shushuk.blog.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    
    private final ArticleRepository articleRepository;
    
    public Article save(Article article) {
        if (article.getId() == null) {
            article.setCreatedAt(LocalDateTime.now());
        }
        article.setUpdatedAt(LocalDateTime.now());
        return articleRepository.save(article);
    }
    
    public Article findById(Long id) {
        return articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("文章不存在"));
    }
    
    public List<Article> findAll() {
        return articleRepository.findAll();
    }
    
    public List<Article> findByAuthorId(Long authorId) {
        return articleRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }
    
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }
} 