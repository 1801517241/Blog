package com.shushuk.blog.controller;

import com.shushuk.blog.model.User;
import com.shushuk.blog.service.UserService;
import com.shushuk.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ArticleService articleService;

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        model.addAttribute("user", user);
        model.addAttribute("articles", articleService.findByAuthorId(user.getId()));
        return "profile/index";
    }

    @GetMapping("/settings")
    public String showSettings(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        model.addAttribute("user", user);
        return "profile/settings";
    }

    @PostMapping("/settings")
    public String updateProfile(Authentication authentication,
                              @RequestParam(required = false) String nickname,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String password,
                              @RequestParam(required = false) MultipartFile avatar,
                              @RequestParam(required = false) String bio,
                              RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            userService.updateProfile(username, nickname, email, password, avatar, bio);
            redirectAttributes.addFlashAttribute("success", "个人资料更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/profile/settings";
    }

    @PostMapping("/settings/avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile file, 
                              RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("请选择要上传的文件");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new IllegalArgumentException("只支持 JPG 和 PNG 格式的图片");
            }

            // 检查文件大小
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                throw new IllegalArgumentException("文件大小不能超过 5MB");
            }

            String avatarUrl = userService.updateAvatar(file);
            redirectAttributes.addFlashAttribute("success", "头像更新成功");
            return "redirect:/profile/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/settings";
        }
    }
} 