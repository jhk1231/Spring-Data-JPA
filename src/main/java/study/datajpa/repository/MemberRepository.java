package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 보통 조건이 2개 이하일 때만 사용
    // By 뒤에 조건이 없다면 전체조회
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /**
     * JPQL의 :username 등의 파라미터에 값을 입력하려면 꼭 @Param 어노테이션으로 정의해야 한다.
     * @Query 어노테이션을 생략해도 사용 가능
     * Spring Data JPA는 Type클래스.메소드명 을 먼저 탐색한다.
     */
//    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();


    /**
     * DTO 조회
     * new Operation 문법이 필요
     */
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /**
     * IN 절 사용법
     */
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    /**
     * Spring Data JPA의 다양한 반환 타입들
     */
    List<Member> findListByUsername(String username);
    Member findMemberByUsername(String username);
    Optional<Member> findOptionalByUsername(String username);



}
