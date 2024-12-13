package com.shushuk.blog.controller;

import com.shushuk.blog.model.User;
import com.shushuk.blog.model.Article;
import com.shushuk.blog.service.UserService;
import com.shushuk.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ArticleService articleService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/list";
    }

    @GetMapping("/{username}")
    public String showUserProfile(@PathVariable String username, Model model) {
        try {
            User user = userService.findByUsername(username);
            model.addAttribute("user", user);
            model.addAttribute("articles", articleService.findByAuthorId(user.getId()));
            return "users/profile";
        } catch (RuntimeException e) {
            model.addAttribute("error", "用户不存在");
            return "redirect:/users";
        }
    }

    @GetMapping("/{username}/articles")
    public String showUserArticles(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        model.addAttribute("articles", articleService.findByAuthorId(user.getId()));
        return "users/articles";
    }
} 