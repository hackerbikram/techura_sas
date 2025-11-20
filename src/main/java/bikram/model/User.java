package bikram.model;

import bikram.util.IdGenerator;
import bikram.util.barcode.BarcodeGenerator;
import com.google.zxing.BarcodeFormat;
import org.bridj.ann.Constructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;


public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
    private String email;
    private String password;
    private Role role;
    private Date joined_date;
    private double salaryPerMonth;


    public User() {}
    public User(String firstName, String lastName, String address,
                String phoneNumber, String email, String password, Role role,double salaryPerMonth) {
        this.id = IdGenerator.idGenerate("USR", 4);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.role = role;
        this.joined_date = Date.valueOf(LocalDate.now());
        this.salaryPerMonth = salaryPerMonth;
        BarcodeGenerator.generateBarcode(this.id,this.getFullName(), BarcodeFormat.CODE_128,300,100);
    }
    public User(String id,String firstName, String lastName, String address,
                String phoneNumber, String email, String password, Role role,Date joined_date,double salaryPerMonth) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.role = role;
        this.joined_date=joined_date;
        this.salaryPerMonth=salaryPerMonth;
    }
    // ✅ Computed getter (not stored)

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public double getSalaryPerMonth() {
        return salaryPerMonth;
    }

    public void setSalaryPerMonth(double salaryPerMonth) {
        this.salaryPerMonth = salaryPerMonth;
    }

    public void setJoined_date(Date joined_date) {
        this.joined_date=joined_date;
    }

    public Date getJoined_date() {
        return joined_date;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getRoleStr(){return role.toString();}


}
