package io.oltre_backend.user;

import java.time.LocalDate;

public class UserDTO{

    private String name;
    private LocalDate birthDate;
    private Integer age;

    public UserDTO (String name, LocalDate birthDate, Integer age){
        this.name =name;
        this.birthDate = birthDate;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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