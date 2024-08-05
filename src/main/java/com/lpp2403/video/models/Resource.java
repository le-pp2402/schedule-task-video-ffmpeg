package com.lpp2403.video.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "resources")
public class Resource extends BaseModel {
    @Column(nullable = false)
    String title;
    String video;
    String thumbnail;
    String enSub;
    String viSub;
    Boolean isPrivate;
    Boolean isReady;
    @Column(columnDefinition = "LONGTEXT")
    String summarize;
}
