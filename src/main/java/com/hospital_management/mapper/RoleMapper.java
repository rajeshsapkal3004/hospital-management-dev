package com.hospital_management.mapper;




import com.hospital_management.dtos.RoleDto;
import com.hospital_management.models.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {

    @Mapping(target = "permissions", source = "permissions")
    RoleDto toDto(Role role);

    Set<RoleDto> toDtoSet(Set<Role> roles);

    List<RoleDto> toDtoList(List<Role> roles);
}
