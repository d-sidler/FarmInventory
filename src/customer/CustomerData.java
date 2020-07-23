package customer;

import javafx.beans.property.*;

public class CustomerData {

    private IntegerProperty id;
    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty email;


    public CustomerData(int id, String firstName, String lastName, String email) {
        this.id = new SimpleIntegerProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName  = new SimpleStringProperty(lastName);
        this.email     = new SimpleStringProperty(email);
    }

    public IntegerProperty id() {
        return this.id;
    }

    public StringProperty firstName() {
        return this.firstName;
    }
    public String getFirstName() {return this.firstName.get();}

    public StringProperty lastName() {
        return this.lastName;
    }
    public String getLastName() {return this.lastName.get();}

    public StringProperty email() {
        return this.email;
    }
    public String getEmail() {return this.email.get();}

    @Override
    public String toString() {
        return this.firstName.getValue() + " " + this.lastName.getValue();
    }


}
