package com.example.springbootredditclone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springbootredditclone.model.Post;
import com.example.springbootredditclone.model.User;
import com.example.springbootredditclone.model.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findTopByPostAndUserOrderByVoteIdDesc(Post post, User currentUser);
}