package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Data;


    @Data
    @Entity
    @Table(name = "users")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String email;
      @Column(name = "password_hash")  // ← BU SATIRI EKLEYİN
    private String passwordHash;
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType;

    public enum RoleType{
        admin, corporate, individual
    }
}
