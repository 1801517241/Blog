package com.shushuk.blog.service;

import com.shushuk.blog.model.User;
import com.shushuk.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final String UPLOAD_DIR = "uploads/avatars/";

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }
    
    public void registerUser(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }
        
        // 检查邮箱是否已存在
        if (existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        // 如果昵称为空，使用用户名作为昵称
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(user.getUsername());
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // 保存用户
        userRepository.save(user);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public void updateProfile(String username, String nickname, String email, 
                            String password, MultipartFile avatar, String bio) {
        User user = findByUsername(username);
        
        // 更新昵称
        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname);
        }
        
        // 更新邮箱
        if (StringUtils.hasText(email) && !email.equals(user.getEmail())) {
            if (existsByEmail(email)) {
                throw new RuntimeException("该邮箱已被使用");
            }
            user.setEmail(email);
        }
        
        // 更新密码
        if (StringUtils.hasText(password)) {
            user.setPassword(passwordEncoder.encode(password));
        }
        
        // 更新个人简介
        user.setBio(bio);
        
        // 更新头像
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String avatarPath = saveAvatar(avatar);
                
                // 删除旧头像
                if (user.getAvatar() != null) {
                    deleteOldAvatar(user.getAvatar());
                }
                
                user.setAvatar(avatarPath);
            } catch (IOException e) {
                throw new RuntimeException("头像上传失败", e);
            }
        }
        
        userRepository.save(user);
    }
    
    private String saveAvatar(MultipartFile file) throws IOException {
        // 确保上传目录存在
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名
        String filename = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(filename);
        
        // 保存文件
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return filename;
    }
    
    private void deleteOldAvatar(String avatarPath) {
        try {
            Path path = Paths.get(UPLOAD_DIR + avatarPath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 记录错误但不抛出异常
            e.printStackTrace();
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotPos = filename.lastIndexOf(".");
        if (lastDotPos == -1) return "";
        return filename.substring(lastDotPos);
    }
    
    public String updateAvatar(MultipartFile file) throws IOException {
        // 确保上传目录存在
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名
        String filename = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(filename);
        
        // 保存文件
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 获取当前用户并更新头像
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = findByUsername(auth.getName());
        
        // 删除旧头像
        if (user.getAvatar() != null && !user.getAvatar().equals("/images/default-avatar.png")) {
            try {
                Path oldAvatarPath = Paths.get(UPLOAD_DIR, user.getAvatar());
                Files.deleteIfExists(oldAvatarPath);
            } catch (IOException e) {
                // 记录错误但不中断流程
                e.printStackTrace();
            }
        }
        
        // 更新用户头像路径
        user.setAvatar(filename);
        userRepository.save(user);
        
        return filename;
    }
}