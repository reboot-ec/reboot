package com.reboot.organization.mapper;

import com.reboot.organization.dto.CreateOrganizationRequestDto;
import com.reboot.organization.dto.OrganizationResponseDto;
import com.reboot.organization.dto.PatchOrganizationRequestDto;
import com.reboot.organization.model.Organization;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationMapper {

    Organization toEntity(CreateOrganizationRequestDto request);

    OrganizationResponseDto toDto(Organization organization);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(PatchOrganizationRequestDto request, @MappingTarget Organization organization);
}
