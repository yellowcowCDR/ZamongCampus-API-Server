package com.litCitrus.zamongcampusServer.repository.post;

import com.litCitrus.zamongcampusServer.domain.post.Post;
import com.litCitrus.zamongcampusServer.domain.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends PagingAndSortingRepository<Post, Long> {

    List<Post> findAllByLikedUsers_UserAndDeletedFalse(User user);
    List<Post> findAllByBookMarkUsers_UserAndDeletedFalse(User user);
    List<Post> findAllByBookMarkUsers_UserAndDeletedFalse(User user, Pageable page);
    Post findByIdAndDeletedFalse(Long id);
    // 인기순: likeCount > viewCount > createDesc
    List<Post> findAllByDeletedFalseOrderByLikeCountDescViewCountDescCreatedAtDesc(Pageable page);

}
