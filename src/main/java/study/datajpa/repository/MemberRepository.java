package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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


    /**
     * Paging
     */
//    Page<Member> findByAge(int age, Pageable pageable);

    /**
     * Count 쿼리 분리하는법
     * Count Query는 기본이 left join
     * Count Query를 분리하지 않는다면 Count Query에서도 원장 쿼리에 적용된 복잡한 join문 같은 쿼리가 실행되어서 성능상에서 손해가 발생할 수 있다.
     */
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m.username) from Member m"
    )
    Page<Member> findByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);


    /**
     * Bulk성 수정 쿼리
     * @Modifying이 있어야 excute쿼리가 나가서 수정 쿼리가 실행된다.
     * clearAutomatically = true : 해당 쿼리가 나간후 clear를 자동으로 해준다.
     */
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);
}
