package chatting.chat.web.kafka;


import chatting.chat.domain.chat.ChatService;
import chatting.chat.domain.data.Chatting;
import chatting.chat.domain.data.Friend;
import chatting.chat.domain.data.Room;
import chatting.chat.domain.data.User;
import chatting.chat.domain.friend.service.FriendService;
import chatting.chat.domain.room.service.RoomService;
import chatting.chat.domain.user.service.UserService;
import chatting.chat.web.kafka.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener extends KafkaTopicConst{
    private final UserService userService;
    private final KafkaTemplate<String, Object> kafkaProducerTemplate;

    // 로그인 요청
    @KafkaListener(topics = "${kafka.topic-user-change}", containerFactory = "userKafkaListenerContainerFactory")
    public void listenLogin(RequestUserChange req) {

        log.info("Receive [RequestUserChange] Message with query={}",req.getInsertOrDelete());

        if (req.getInsertOrDelete().equals("INSERT")) {
            userService.save(req.getUserId(),req.getUserName(),req.getUserStatus());

            // 신규가입자 수 추이 확인을 위한 Producer (ELK 용)
            RequestAddUserDTO user = new RequestAddUserDTO();
            user.setUserName(req.getUserName());
            user.setUserId(req.getUserId());
            sendToKafka(TOPIC_USER_ADD_REQUEST,user);

        } else if (req.getInsertOrDelete().equals("DELETE")) {
            userService.remove(req.getUserId());
        }
    }

    private void sendToKafka(String topic,Object req) {
        ListenableFuture<SendResult<String, Object>> future = kafkaProducerTemplate.send(topic, req);
        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("메세지 전송 실패={}", ex.getMessage());
            }
            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("메세지 전송 성공 topic={}, offset={}, partition={}",topic, result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            }
        });
    }


}