package com.shushuk.blog.repository;

import com.shushuk.blog.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
} 