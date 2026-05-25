package uz.uptimehub.booking.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uptimehub.booking.dto.booking.BookingCreateRequest;
import uz.uptimehub.booking.dto.booking.BookingDto;
import uz.uptimehub.booking.jpa.entity.Booking;
import uz.uptimehub.resource.dto.resource.ResourceDto;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "resourceId", source = "resourceId")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    @Mapping(target = "status", expression = "java(uz.uptimehub.booking.dto.booking.Status.PENDING)")
    @Mapping(target = "organizationId", expression = "java(resource.getOrganizationId())")
    @Mapping(target = "resourceId", expression = "java(resource.getId())")
    @Mapping(target = "userId", expression = "java(userId)")
    Booking toEntity(BookingCreateRequest request, ResourceDto resource, UUID userId);

    BookingDto toDto(Booking booking);
}
