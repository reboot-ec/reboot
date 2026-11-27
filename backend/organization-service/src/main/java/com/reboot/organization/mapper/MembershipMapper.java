package com.reboot.organization.mapper;

import com.reboot.organization.dto.MembershipResponseDto;
import com.reboot.organization.model.OrganizationMembership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MembershipMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    MembershipResponseDto toDto(OrganizationMembership membership);
}
