package chatting.chat.domain.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class Chatting {
    @Id
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID")
    private Room room;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User sendUser;
    @Column(name = "MESSAGE")
    private String message;
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public Chatting(String id, Room room, User sendUser, String message, LocalDateTime createdAt) {
        this.id = id;
        this.room = room;
        this.sendUser = sendUser;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Chatting() {

    }
}
