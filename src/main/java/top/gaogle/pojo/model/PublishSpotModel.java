package top.gaogle.pojo.model;

import top.gaogle.pojo.domain.PublishSpot;

public class PublishSpotModel extends PublishSpot {

    private Integer roomCount;
    private Integer seatCount;

    public Integer getRoomCount() {
        return roomCount;
    }
    public void setRoomCount(Integer roomCount) {
        this.roomCount = roomCount;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(Integer seatCount) {
        this.seatCount = seatCount;
    }
}
