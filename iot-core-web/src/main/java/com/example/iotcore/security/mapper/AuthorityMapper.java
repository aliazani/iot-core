package com.example.iotcore.security.mapper;


import com.example.iotcore.security.domain.Authority;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthorityMapper {
    @Named("authorityToString")
    default String authorityToString(Authority authority) {
        return (authority == null) ? null : authority.getName();
    }

    @Named("authoritiesToString")
    default Set<String> authoritiesToString(Set<Authority> authorities) {
        return authorities
                .stream()
                .map(this::authorityToString)
                .collect(Collectors.toSet());
    }
}

