package it.ttf.invernizzi.salting.persistence.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    @Column(length = 100, unique = true, nullable = false)
    private String username;
    @NonNull
    @Column(length = 100, nullable = false)
    private String email;
    @NonNull
    @Column(length = 128, nullable = false)
    private String token;
}
