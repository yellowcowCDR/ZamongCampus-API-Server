package com.litCitrus.zamongcampusServer.domain.user;

import lombok.*;
import javax.persistence.*;
/// setter 삭제 필요.
@Entity
@Getter
@Setter
@Builder
@Table(name = "authority")
@AllArgsConstructor
@NoArgsConstructor
public class Authority {

    @Id
    @Column(name = "authority_name", length = 50)
    private String authorityName;
}
