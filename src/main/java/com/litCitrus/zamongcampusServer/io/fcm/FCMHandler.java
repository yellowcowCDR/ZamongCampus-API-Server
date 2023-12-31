package com.litCitrus.zamongcampusServer.io.fcm;

import com.google.firebase.messaging.*;
import com.litCitrus.zamongcampusServer.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class FCMHandler {

    private final FirebaseMessaging firebaseMessaging;
    private final String appTitle = "\uD83C\uDF4A자몽캠퍼스";
    public void sendNotification(FCMDto fcmDto, String channelId, List<User> recipients, String newTitle) {
        String title;

        if(newTitle == null) title = appTitle;
        else title = newTitle;

        List<String> recipientTokens = recipients.stream().map(user -> user.getDeviceToken()).collect(Collectors.toList());
        Notification notification = Notification
                .builder()
                .setTitle(title)
                .setBody(fcmDto.getBody())
                .build();
        AndroidNotification androidNotification = AndroidNotification.builder()
                .setChannelId(channelId)
                .build();
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setNotification(androidNotification)
                .build();
        if(!recipientTokens.isEmpty()){
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(recipientTokens)
                    .setNotification(notification)
                    .putAllData(fcmDto.getData())
                    .setAndroidConfig(androidConfig)
                    .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                    .build();
            BatchResponse response = null;
            try {
                response = firebaseMessaging.sendMulticast(message);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
            System.out.println(response.getSuccessCount());
        }
    }

    public void sendNotificationOne(FCMDto fcmDto, String channelId, User recipient, String newTitle) {
        String title;

        if(newTitle == null) title = appTitle;
        else title = newTitle;

        String recipientToken = recipient.getDeviceToken();
        Notification notification = Notification
                .builder()
                .setTitle(title)
                .setBody(fcmDto.getBody())
                .build();
        AndroidNotification androidNotification = AndroidNotification.builder()
                .setChannelId(channelId)
                .build();
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setNotification(androidNotification)
                .build();
        if(!recipientToken.isEmpty()){
            Message message = Message
                .builder()
                .setToken(recipientToken)
                .setNotification(notification)
                .putAllData(fcmDto.getData())
                .setAndroidConfig(androidConfig)
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .build();

            String response = null;
            try {
                response = firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
            System.out.println(response);
        }
    }
}
