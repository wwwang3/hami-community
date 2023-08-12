package top.wang3.hami.mail.model;

public class Address {

    private String address;
    private String personal;

    public Address(String address) {
        this.address = address;
    }

    public Address(String address, String personal) {
        this.address = address;
        this.personal = personal;
    }

    public String getAddress() {
        return address;
    }

    public Address setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getPersonal() {
        return personal;
    }

    public Address setPersonal(String personal) {
        this.personal = personal;
        return this;
    }
}
