package com.shushuk.blog.controller;

import com.shushuk.blog.model.Article;
import com.shushuk.blog.model.User;
import com.shushuk.blog.service.ArticleService;
import com.shushuk.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    private final UserService userService;
    
    @GetMapping("/articles/write")
    public String showWriteForm(Model model) {
        model.addAttribute("article", new Article());
        return "articles/write";
    }
    
    @PostMapping("/articles")
    public String createArticle(@ModelAttribute Article article, 
                              Authentication authentication) {
        String username = authentication.getName();
        User author = userService.findByUsername(username);
        
        article.setAuthor(author);
        articleService.save(article);
        
        return "redirect:/articles/" + article.getId();
    }
    
    @GetMapping("/articles/{id}")
    public String showArticle(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id);
        model.addAttribute("article", article);
        return "articles/show";
    }
    
    @GetMapping("/articles/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id);
        model.addAttribute("article", article);
        return "articles/edit";
    }
    
    @PostMapping("/articles/{id}")
    public String updateArticle(@PathVariable Long id, 
                              @ModelAttribute Article article) {
        article.setId(id);
        articleService.save(article);
        return "redirect:/articles/" + id;
    }
    
    @DeleteMapping("/articles/{id}")
    public String deleteArticle(@PathVariable Long id) {
        articleService.deleteById(id);
        return "redirect:/";
    }
} 