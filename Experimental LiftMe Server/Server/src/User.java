/**
 * Created by Francois on 2016/03/24.
 */
public class User {

    private String name;
    private String surname;
    private Integer userID;
    private String password;
    private String email;
    private String contactNum;
    private Integer availableAsDrive;
    private Integer numberOfPassengers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNum() {
        return contactNum;
    }

    public void setContactNum(String contactNum) {
        this.contactNum = contactNum;
    }

    public Integer getAvailableAsDrive() {
        return availableAsDrive;
    }

    public void setAvailableAsDrive(Integer availableAsDrive) {
        this.availableAsDrive = availableAsDrive;
    }

    public Integer getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void setNumberOfPassengers(Integer numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }
}
