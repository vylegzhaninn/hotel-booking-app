package io.github.vylegzhaninn.bookingservice.mapper;

import io.github.vylegzhaninn.bookingservice.model.Room;
import io.github.vylegzhaninn.bookingservice.model.RoomResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomResponse toResponse(Room room);

    List<RoomResponse> toResponseList(List<Room> rooms);
}
