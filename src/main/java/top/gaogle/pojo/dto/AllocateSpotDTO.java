package top.gaogle.pojo.dto;

public class AllocateSpotDTO {
    private String admissionTicketNumber;
    private String spotId;
    private String spot;
    private String spotAddress;

    private Integer roomNumber;

    private Integer seatNumber;

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public String getSpot() {
        return spot;
    }

    public void setSpot(String spot) {
        this.spot = spot;
    }

    public String getSpotAddress() {
        return spotAddress;
    }

    public void setSpotAddress(String spotAddress) {
        this.spotAddress = spotAddress;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getAdmissionTicketNumber() {
        return admissionTicketNumber;
    }

    public void setAdmissionTicketNumber(String admissionTicketNumber) {
        this.admissionTicketNumber = admissionTicketNumber;
    }
}
