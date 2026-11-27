package com.reboot.identity.mapper;

import com.reboot.identity.dto.ProfilePatchRequestDto;
import com.reboot.identity.dto.RegisterRequestDto;
import com.reboot.identity.dto.UserResponseDto;
import com.reboot.identity.model.Role;
import com.reboot.identity.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface IdentityUserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "authProvider", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toNewUser(RegisterRequestDto request);

    @Mapping(target = "roles", source = "roles")
    UserResponseDto toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "authProvider", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProfile(ProfilePatchRequestDto request, @MappingTarget User user);

    default List<String> mapRoles(Set<Role> roles) {
        return roles == null ? List.of() : roles.stream()
                .map(Role::getName)
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
