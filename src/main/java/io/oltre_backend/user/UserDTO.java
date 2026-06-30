package io.oltre_backend.user;

import java.time.LocalDate;

public class UserDTO{

    private String username;
    private LocalDate birthDate;
    private Integer age;

    public UserDTO (String username, LocalDate birthDate, Integer age){
        this.username = username;
        this.birthDate = birthDate;
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setName(String username) {
        this.username = username;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    
}