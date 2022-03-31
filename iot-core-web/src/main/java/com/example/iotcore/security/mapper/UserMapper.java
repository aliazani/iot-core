package com.example.iotcore.security.mapper;

import com.example.iotcore.security.domain.User;
import com.example.iotcore.security.dto.AdminUserDTO;
import com.example.iotcore.security.dto.UserDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

/**
 * Mapper for the entity {@link User} and its DTO called {@link UserDTO}.
 * Mapper for the entity {@link User} and its DTO called {@link AdminUserDTO}.
 * <p>
 */
@Mapper(componentModel = "spring", uses = AuthorityMapper.class)
public interface UserMapper {
    // From: User - To: UserDTO -> Only Id
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toUserDTOId(User user);

    // From: User - To: UserDTO -> Id and Login
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "login", source = "login")
    @Mapping(target = "id", source = "id")
    UserDTO toUserDTO(User user);

    // From User - To: AdminUserDTO
    @Mapping(source = "authorities", target = "authorities", qualifiedByName = "authoritiesToString")
    AdminUserDTO toAdminUserDTO(User user);

    @Mapping(source = "authorities", target = "authorities", qualifiedByName = "authoritiesToString")
    List<AdminUserDTO> toAdminUserDTOList(List<User> users);

    @Mapping(source = "authorities", target = "authorities", qualifiedByName = "authoritiesToString")
    Set<AdminUserDTO> toAdminUserDTOSet(Set<User> users);

    // From: AdminUserDTO - To: User
    @BeforeMapping
    default void convertToLowerCase(AdminUserDTO adminUserDTO) {
        adminUserDTO.setLogin(adminUserDTO.getLogin().toLowerCase());
        if (adminUserDTO.getEmail() != null)
            adminUserDTO.setEmail(adminUserDTO.getEmail().toLowerCase());
    }

    @Mapping(source = "login", target = "login")
    @Mapping(target = "authorities", ignore = true)
    User fromAdminUserDTOToUserEntity(AdminUserDTO adminUserDTO);

    @Mapping(target = "authorities", ignore = true)
    List<User> fromAdminUserDTOsToUserEntities(List<AdminUserDTO> adminUserDTOs);
}
