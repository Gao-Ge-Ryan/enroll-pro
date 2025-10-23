package top.gaogle.pojo.dto;

public class SignInInfoDTO {
    private String photo;
    private String idNumber;
    private String admissionTicketNumber;
    private String name;
    private String seatNumber;

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getAdmissionTicketNumber() {
        return admissionTicketNumber;
    }

    public void setAdmissionTicketNumber(String admissionTicketNumber) {
        this.admissionTicketNumber = admissionTicketNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
