package com.litCitrus.zamongcampusServer.domain.post;


import com.litCitrus.zamongcampusServer.domain.BaseEntity;
import com.litCitrus.zamongcampusServer.domain.user.User;
import com.litCitrus.zamongcampusServer.dto.post.PostDtoReq;
import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE post SET deleted = true WHERE id=?")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String body;

    private int likeCount;
    private int commentCount;
    private int viewCount;

    @OneToMany(mappedBy = "post")
    private List<PostParticipant> postParticipants;

    @OneToMany(mappedBy = "post")
    private List<PostPicture> pictures;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<PostLike> likedUsers;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<PostBookMark> bookMarkUsers;

    @OneToMany(mappedBy = "post")
    private List<PostComment> comments;

    @ManyToMany
    private List<PostCategory> postCategories;

    @Builder.Default
    @NotNull
    private boolean exposed = Boolean.TRUE;

    @Builder.Default
    @NotNull
    private boolean deleted = Boolean.FALSE;

    public static Post createPost(User user, PostDtoReq.Create postDto, List<PostCategory> postCategories) {
        /// ** postCategory는 ManyToMany이기에 여기서 선언.
        final Post post = Post.builder()
                .user(user)
                .body(postDto.getBody())
                .postCategories(postCategories)
                .likeCount(0)
                .commentCount(0)
                .build();
        return post;
    }

    public void updateMyPost(PostDtoReq.Update postDto) {
        this.body = postDto.getBody();
    }

    public void plusLikeCnt(){
        likeCount ++;
    }
    public void minusLikeCnt(){
        likeCount --;
    }
    public void plusCommentCnt(){
        commentCount ++;
    }
    // 아래 minus는 안 쓸수도. (댓글을 따로 삭제 안하기 때문)
    public void minusCommentCnt(){
        commentCount --;
    }
    public void plusViewCnt(){
        viewCount ++;
    }

    public void changeExposed(Boolean value){ this.exposed = value; }
}

