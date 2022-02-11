package dev.reviewbot2.repository;

import dev.reviewbot2.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByChatId(String chatId);
    boolean existsByLogin(String login);
    Member getMemberByChatId(String chatId);
    List<Member> getAllByReviewGroup(int reviewGroup);
}
