package com.example.text2sql.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 项目成员关系实体类
 */
@Entity
@Table(name = "project_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private String role;
    
    @Column(name = "join_date")
    private LocalDate joinDate;
}
