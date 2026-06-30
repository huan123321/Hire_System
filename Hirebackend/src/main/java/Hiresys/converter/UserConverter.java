package Hiresys.converter;

import Hiresys.dto.UserDTO;
import Hiresys.entity.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户对象转换器
 * 负责 Entity 和 DTO 之间的转换
 */
public class UserConverter {

    /**
     * Entity 转 DTO（单个对象）
     * 
     * @param user 用户实体
     * @return 用户 DTO，如果 user 为 null 则返回 null
     */
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        // 基础信息
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setNickname(user.getNickname());
        dto.setRealName(user.getRealName());
        dto.setAvatar(user.getAvatar());
        
        // 个人信息
        dto.setGender(user.getGender());
        dto.setBirthday(user.getBirthday());
        dto.setBio(user.getBio());
        dto.setLocation(user.getLocation());
        
        // 状态信息
        dto.setStatus(user.getStatus());
        dto.setRole(user.getRole());
        dto.setLastLoginTime(user.getLastLoginTime());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        
        // HR 专属信息
        dto.setCompanyId(user.getCompanyId());
        dto.setCompanyName(user.getCompanyName());
        
        // ❌ 注意：以下敏感字段不会复制到 DTO
        // - password (密码)
        // - lastLoginIp (最后登录IP)
        // - isDeleted (删除标志)
        
        return dto;
    }

    /**
     * Entity 列表转 DTO 列表
     * 
     * @param users 用户实体列表
     * @return 用户 DTO 列表
     */
    public static List<UserDTO> toDTOList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        
        return users.stream()
                   .map(UserConverter::toDTO)
                   .collect(Collectors.toList());
    }

    /**
     * DTO 转 Entity（用于更新操作）
     * 
     * @param dto 用户 DTO
     * @return 用户实体
     */
    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname());
        user.setRealName(dto.getRealName());
        user.setAvatar(dto.getAvatar());
        user.setGender(dto.getGender());
        user.setBirthday(dto.getBirthday());
        user.setBio(dto.getBio());
        user.setLocation(dto.getLocation());
        user.setStatus(dto.getStatus());
        user.setRole(dto.getRole());
        user.setCompanyId(dto.getCompanyId());
        
        return user;
    }
}
