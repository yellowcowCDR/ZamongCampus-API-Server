package com.litCitrus.zamongcampusServer.dto.voiceRoom;

import com.litCitrus.zamongcampusServer.domain.post.Post;
import com.litCitrus.zamongcampusServer.domain.user.User;
import com.litCitrus.zamongcampusServer.domain.voiceRoom.VoiceRoom;
import com.litCitrus.zamongcampusServer.dto.chat.SystemMessageDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VoiceRoomDtoRes {

    @Getter
    public static class DetailRes{
        private final boolean isFull;
        private VoiceRoomAndTokenInfo voiceRoomAndTokenInfo;
        private List<SystemMessageDto.MemberInfo> memberInfos;

        public DetailRes(VoiceRoom voiceRoom, String token, int uid){
            /// uid 현재는 user_id, 나중에 변경 필요.
            this.isFull = false;
            this.voiceRoomAndTokenInfo = new VoiceRoomAndTokenInfo(voiceRoom, token, uid);
            this.memberInfos = voiceRoom.getChatRoom().getUsers().stream()
                    .map(member -> new SystemMessageDto.MemberInfo(member.getId(),
                            member.getLoginId(), member.getNickname(), member.getPictures().get(0).getStored_file_path())).collect(Collectors.toList());
        }

        public DetailRes(VoiceRoom voiceRoom){
            this.isFull = true;
            this.voiceRoomAndTokenInfo = new VoiceRoomAndTokenInfo(voiceRoom);
        }
    }

    @Getter
    public static class Res{
        private final Long id;
        private final String title;
        private final List<String> userImageUrls;
        public Res(VoiceRoom voiceRoom){
            this.title = voiceRoom.getTitle();
            this.id = voiceRoom.getId();
            this.userImageUrls = voiceRoom.getChatRoom().getUsers().stream().map(user -> user.getPictures().get(0).getStored_file_path()).collect(Collectors.toList());
        }
    }

    @Getter
    public static class VoiceRoomAndTokenInfo{
        private final long id;
        private String title;
        private String roomId;
        private String token;
        private int uid;
        private String ownerLoginId;

        public VoiceRoomAndTokenInfo(VoiceRoom voiceRoom, String token, int uid){
            this.id = voiceRoom.getId();
            this.title = voiceRoom.getTitle();
            this.roomId = voiceRoom.getChatRoom().getRoomId();
            this.token = token;
            this.uid = uid;
            this.ownerLoginId = voiceRoom.getOwner().getLoginId();
        }
        public VoiceRoomAndTokenInfo(VoiceRoom voiceRoom){
            this.id = voiceRoom.getId();
        }
    }

    @Getter
    public static class UpdateMemberInfo {
        private final String type;
        private final String loginId;
        private final String nickname;
        private String imageUrl;

        public UpdateMemberInfo(User user, String type){
            this.type = type;
            this.loginId = user.getLoginId();
            this.nickname = user.getNickname();
            this.imageUrl = user.getPictures().get(0).getStored_file_path();
        }
    }
}
