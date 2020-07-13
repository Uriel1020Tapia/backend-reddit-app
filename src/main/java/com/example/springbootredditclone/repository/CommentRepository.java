package com.example.springbootredditclone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springbootredditclone.model.Comment;
import com.example.springbootredditclone.model.Post;
import com.example.springbootredditclone.model.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);

    List<Comment> findAllByUser(User user);
}