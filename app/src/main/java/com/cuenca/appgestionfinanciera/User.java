package com.cuenca.appgestionfinanciera;

public class User {
    public String id;
    public String name;
    public String email;
    public String password;

    public User() { }

    public User(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
