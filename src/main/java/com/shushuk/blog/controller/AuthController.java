package com.shushuk.blog.controller;

import com.shushuk.blog.model.User;
import com.shushuk.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.web.WebAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String showLoginForm(Model model, 
                              @RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "logout", required = false) String logout,
                              HttpSession session) {
        
        if (error != null) {
            String errorMessage = extractErrorMessage(session);
            model.addAttribute("error", errorMessage);
        }

        if (logout != null) {
            model.addAttribute("message", "您已成功退出登录");
        }

        return "auth/login";
    }

    private String extractErrorMessage(HttpSession session) {
        Exception exception = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (exception != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            return exception.getMessage();
        }
        return "用户名或密码错误";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "注册成功！请登录。");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
} 