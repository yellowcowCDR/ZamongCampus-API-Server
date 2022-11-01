package com.litCitrus.zamongcampusServer.dto.post;

import com.litCitrus.zamongcampusServer.domain.post.Post;
import com.litCitrus.zamongcampusServer.domain.post.PostComment;
import com.litCitrus.zamongcampusServer.domain.post.PostLike;
import com.litCitrus.zamongcampusServer.domain.post.PostParticipant;
import com.litCitrus.zamongcampusServer.domain.user.CollegeCode;
import com.litCitrus.zamongcampusServer.domain.user.User;
import com.litCitrus.zamongcampusServer.domain.user.UserPicture;
import lombok.Getter;

import javax.xml.stream.events.Comment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PostDtoRes {

    /// PostDetail
    @Getter
    public static class ResWithComment extends Res{
        final private List<PostCommentDtoRes.Res> comments;

        /// 1. 부모 댓글만 dto로 변환 (filter)
        /// 만약 부모댓글이 삭제되었는데, 자식댓글이 남았을 때는?
        /// 일단 삭제된 댓글이라도 들고 간다.
        /// 근데 만약 삭제된 댓글 + (자식도 없거나 || 자식들이 다 삭제된거면)
        /// 안 들고 가도록 (isDeleted인 값이 true 아닌 것들의, 즉 존재하는 값이 없을때(noneMatch))
        public ResWithComment(Post post, List<PostParticipant> postParticipants){
            super(post);
            this.comments = post.getComments().stream()
                    .filter(postComment -> postComment.getParent() == null)
//                    .filter(postComment -> !((postComment.isDeleted() && (postComment.getChildren().isEmpty() || postComment.getChildren().stream().noneMatch(child -> child.isDeleted() != true)))))
                    .map(postComment -> new PostCommentDtoRes.Res(postComment, postParticipants)).collect(Collectors.toList());

        }
    }

    @Getter
    public static class Res{
        private final long id;
        private final String loginId;
        private final String userNickname;
        private final CollegeCode writerCollegeCode;
        private final String writerProfileImageUrl;
        private final String body;
        private final LocalDateTime createdAt;
        private final List<String> imageUrls;
        private final int likedCount;
        private int commentCount;
        private final int viewCount;
        private final List<String> postCategoryCodes;

        public Res(Post post){
            this.id = post.getId();
            this.loginId = post.getUser().getLoginId();
            this.userNickname = post.getUser().getNickname();
            this.writerCollegeCode = post.getUser().getCollegeCode();
            List<UserPicture> userProfileImages = post.getUser().getPictures();
            this.writerProfileImageUrl = (userProfileImages==null || userProfileImages.size()<=0)? "":userProfileImages.get(0).getStored_file_path();
            this.body = post.getBody();
            this.createdAt = post.getCreatedAt();
            this.imageUrls = post.getPictures().stream().map(postPicture -> postPicture.getStored_file_path()).collect(Collectors.toList());
            this.likedCount = post.getLikeCount();
            // 1. 부모고 삭제된 상태고 자식도 없으면 count에서 제외, 2. 부모고 삭제된 상태고, 자식들도 다 삭제된 상태면 부모+자식 count에서 제외
            // 일단 이 경우는 나중에 생각. 적용 x (2022.5.20) 위에 filter도 주석
            this.commentCount = post.getCommentCount();
            this.viewCount = post.getViewCount();
            this.postCategoryCodes = post.getPostCategories().stream().map(postCategory -> postCategory.getPostCategoryCode().name()).collect(Collectors.toList());
        }
    }
}
