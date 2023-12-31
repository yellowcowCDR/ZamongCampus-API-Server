package com.litCitrus.zamongcampusServer.service.chat;

import com.litCitrus.zamongcampusServer.domain.chat.ChatRoom;
import com.litCitrus.zamongcampusServer.domain.history.WorkHistoryType;
import com.litCitrus.zamongcampusServer.domain.user.BlockedUser;
import com.litCitrus.zamongcampusServer.domain.user.ModifiedChatInfo;
import com.litCitrus.zamongcampusServer.domain.user.User;
import com.litCitrus.zamongcampusServer.dto.chat.ChatMessageDtoReq;
import com.litCitrus.zamongcampusServer.dto.chat.ChatMessageDtoRes;
import com.litCitrus.zamongcampusServer.exception.chat.ChatRoomNotFoundException;
import com.litCitrus.zamongcampusServer.exception.user.UserNotFoundException;
import com.litCitrus.zamongcampusServer.io.dynamodb.model.ChatMessage;
import com.litCitrus.zamongcampusServer.io.dynamodb.service.DynamoDBHandler;
import com.litCitrus.zamongcampusServer.io.fcm.FCMDto;
import com.litCitrus.zamongcampusServer.io.fcm.FCMHandler;
import com.litCitrus.zamongcampusServer.repository.chat.ChatRoomRepository;
import com.litCitrus.zamongcampusServer.repository.user.ModifiedChatInfoRepository;
import com.litCitrus.zamongcampusServer.repository.user.UserRepository;
import com.litCitrus.zamongcampusServer.security.jwt.TokenProvider;
import com.litCitrus.zamongcampusServer.service.history.WorkHistoryService;
import com.litCitrus.zamongcampusServer.service.user.BlockedUserService;
import com.litCitrus.zamongcampusServer.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final DynamoDBHandler dynamoDBHandler;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ModifiedChatInfoRepository modifiedChatInfoRepository;
    private final TokenProvider tokenProvider;
    private final FCMHandler fcmHandler;

    private final BlockedUserService blockedUserService;

    private final WorkHistoryService workHistoryService;

    @Transactional
    public void sendMessage(ChatMessageDtoReq messageDto, String token){
        /* Dynamo Db에 저장 + 메시지를 채팅방(roomId)에게 Stomp으로 전송한다 */
        //User 검증
        Authentication authentication = tokenProvider.getAuthentication(token.substring(7));
        User user = SecurityUtil.getCurrentUsername(authentication).flatMap(userRepository::findOneWithAuthoritiesByLoginId).orElseThrow(UserNotFoundException::new);

        // 채팅 메세지 객체생성
        final String currentTime = LocalDateTime.now().toString();
        ChatMessageDtoRes.MessageDto messageDtoRes = new ChatMessageDtoRes.MessageDto(messageDto.getType(), user.getLoginId(), messageDto.getText(), currentTime);
        ChatMessageDtoRes.RealTimeMessageBundle roomIdMessageBundleDto = ChatMessageDtoRes.RealTimeMessageBundle.builder()
                .type(ModifiedChatInfo.MemberStatus.TALK)
                .roomId(messageDto.getRoomId())
                .messageDto(messageDtoRes).build();

        ChatRoom chatRoom = chatRoomRepository.findByRoomId(messageDto.getRoomId()).orElseThrow(ChatRoomNotFoundException::new);
        /* 1. 채팅 메시지를 채팅방에 소속된 사용자에게 전송 (roomId에 메세지 publish) */
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.getRoomId(), roomIdMessageBundleDto);
        if(chatRoom.getType().equals("single")){
            /// TODO: 만약 multi방이면 알림 전송 x. (추후 single,multi,voice 이렇게 변경해서 voice를 안 보내도록 변경해야함)
            // 현재는 multi면 dynamo 저장도 fcm도 전송 안하도록 구현 (voice는 실시간 기반이기에)
            // 첫 채팅메세지 전송 시, 상대방에게 알림전송

            /* 2. 채팅 메시지 디비에 저장 */
            dynamoDBHandler.putMessage(messageDto, user.getLoginId(), currentTime);
            /* 3. fcm(알림) 전송 */
            List<String> chatRoomTitleAndImage = chatRoom.getCounterpartChatRoomTitleAndImage(user.getLoginId());
            FCMDto fcmDto = new FCMDto(messageDto.getText(),
                    new HashMap<String,String>(){{
                        put("navigate","/chatDetail");
                        put("roomId", chatRoom.getRoomId());
                        put("title", user.getNickname());
                        put("imageUrl", user.getPictures().isEmpty() ? "" : user.getPictures().get(0).getStored_file_path());
                        put("type", chatRoom.getType());

                    }});
            List<User> recipientsExceptMe  = chatRoomRepository.findByRoomId(messageDto.getRoomId()).orElseThrow(ChatRoomNotFoundException::new)
                    .getUsers().stream().filter(recipient -> !recipient.getLoginId().equals(user.getLoginId())).collect(Collectors.toList());
            recipientsExceptMe = recipientsExceptMe.stream().filter((recipientExceptMe)->!blockedUserService.isBlockedUser(recipientExceptMe.getLoginId(), user.getLoginId())).collect(Collectors.toList());
            if(recipientsExceptMe!=null && recipientsExceptMe.size()>0) {
                fcmHandler.sendNotification(fcmDto, "fcm_message_channel", recipientsExceptMe, user.getNickname());
            }
        }
        //이력 저장
        workHistoryService.saveWorkHistory(WorkHistoryType.WorkType.WRITE, WorkHistoryType.FunctionType.MESSAGE);
    }

    // READ: GET MESSAGE
    @Transactional
    public ChatMessageDtoRes.ChatBundle getChatMessageDynamo(String createdAfter, User user){
        /* 1. 참여한 모든 방 찾기 */
        //User user = SecurityUtil.getCurrentUsername().flatMap(userRepository::findOneWithAuthoritiesByLoginId).orElseThrow(UserNotFoundException::new);
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByParticipants_User(user);

        List<User> blockedUserList = blockedUserService.getBlockedUserList().stream().map(BlockedUser::getBlockedUser).collect(Collectors.toList());

        chatRooms = chatRooms.stream().filter(chatRoom ->{
            List<User> chatMemberList = chatRoom.getUsers();

            for(User member : chatMemberList){
                if(blockedUserList.contains(member))
                    return false;
            }

            return true;
        }).collect(Collectors.toList());

        /* 2. 각 채팅 roomId 기준으로 DynamoDB에서 메시지 가져오고 dto로 변환 */
        List<ChatMessageDtoRes.RoomMessageBundle> roomMessages
                = chatRooms.stream()
                .map(chatRoom ->makeRoomMessageBundle(chatRoom, createdAfter))
                .filter(chatMessage -> chatMessage!=null)
                .collect(Collectors.toList());

        /* 3. ModifiedChatInfo에 대한 정보를 MySQL에서 가져오고 dto로 변환 */
        List<ChatMessageDtoRes.ModifiedInfo> modifiedInfos
                = user.getModifiedChatInfos().stream().map(modifiedChatInfo ->
                new ChatMessageDtoRes.ModifiedInfo(modifiedChatInfo, user))
                .collect(Collectors.toList());

        // Member info 내용을 MySQL에서 삭제
        //TODO: 반드시 주석 변경해둘 것
        modifiedChatInfoRepository.deleteAll(user.getModifiedChatInfos());

        return new ChatMessageDtoRes.ChatBundle(roomMessages, modifiedInfos);

    }

    private ChatMessageDtoRes.RoomMessageBundle makeRoomMessageBundle(ChatRoom chatRoom, String createdAfter){
        PageIterable<ChatMessage> chatMessagesByRoomId =
                dynamoDBHandler.getMessages(chatRoom.getRoomId(), createdAfter);
        if(chatMessagesByRoomId.items().stream().count() == 0){
            return null;
        }else{
            return new ChatMessageDtoRes.RoomMessageBundle(chatRoom.getRoomId(), chatMessagesByRoomId);
        }
    }
}
