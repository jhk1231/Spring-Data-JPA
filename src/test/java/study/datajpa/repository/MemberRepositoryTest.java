package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void testMember() {
        System.out.println("memberRepository.getClass() = " + memberRepository.getClass());
        Member member = new Member("memberA");
        Member saveMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(saveMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        findMember1.setUsername("member!!!!!");

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void findByUsernameAndAgeGreaterThen() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }


    @Test
    void testNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    void testQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    void findUsernameList() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> result = memberRepository.findUsernameList();
        for (String str: result) {
            System.out.println("str = " + str);
        }
    }


    @Test
    void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);
        
        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);


        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto.toString() = " + dto.toString());
        }
    }


    @Test
    void findByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> usernameList = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : usernameList) {
            System.out.println("member = " + member);
        }
    }

    @Test
    void returnTypeTest() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);


        /**
         * 컬렉션 조회시 주의사항
         * 만약 데이터가 없다면 null이 아니라 빈 컬렉션을 반환한다.
         *
         * 만약 단거 조회인데 값이 결과값이 여러개라면 NotUniqueException이 터진다.
         * 예외가 터져도 Spring에서 SpringException으로 변환해서 예외를 터트린다.
         */
        List<Member> aaa = memberRepository.findListByUsername("AAA");              //  절대 null이 아니다.
        Member aaa1 = memberRepository.findMemberByUsername("AAA");                 //  값이 없다면 null
        Optional<Member> aaa2 = memberRepository.findOptionalByUsername("AAA");     //  good 있을수도 있고 없을수도 있을 때 사용

        System.out.println("aaa2 = " + aaa);
        System.out.println("aaa2 = " + aaa1);
        System.out.println("aaa2 = " + aaa2);
    }

    @Test
    void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        /**
         * spring Data Jpa는 page가 0부터 시작
         */
        int age = 10;
        // 3,4번째 파라미터: username으로 DESC 정렬해라
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.DESC, "username");

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(6); // totalCount
        assertThat(page.getNumber()).isEqualTo(0); //현재 페이지
        assertThat(page.getTotalPages()).isEqualTo(2); // Page가 전체 몇 개인지
        assertThat(page.isFirst()).isTrue(); // 첫번째 페이지가 있는지
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는지

    }

    /**
     * Paging 결과 Entity를 DTO로 변환해서 내보내기
     */
    @Test
    void pagingResDto() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        /**
         * spring Data Jpa는 page가 0부터 시작
         */
        int age = 10;
        // 3,4번째 파라미터: username으로 DESC 정렬해라
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.DESC, "username");

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        //then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        System.out.println(" = " + toMap.getContent());


        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(6); // totalCount
        assertThat(page.getNumber()).isEqualTo(0); //현재 페이지
        assertThat(page.getTotalPages()).isEqualTo(2); // Page가 전체 몇 개인지
        assertThat(page.isFirst()).isTrue(); // 첫번째 페이지가 있는지
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는지

    }

    @Test
    void slice() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        /**
         * spring Data Jpa는 page가 0부터 시작
         */
        int age = 10;
        // 3,4번째 파라미터: username으로 DESC 정렬해라
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.DESC, "username");

        //when
        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent();
//        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
//        assertThat(page.getTotalElements()).isEqualTo(6); // totalCount
        assertThat(page.getNumber()).isEqualTo(0); //현재 페이지
//        assertThat(page.getTotalPages()).isEqualTo(2); // Page가 전체 몇 개인지
        assertThat(page.isFirst()).isTrue(); // 첫번째 페이지가 있는지
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는지

    }

    @Test
    void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        /**
         * Bluk 주의사항
         *  - bluk연산은 JPA의 영속성 컨텍스트를 무시하고 바로 DB에 쿼리를 날리기 때문에 영속성 컨텍스트는 Data가 변경된 사실을 모른다.
         *    , 때문에 Bluk 연산 후 Data를 조회하거나 했을 때 데이터 Update가 적용되지 않을수도 있다.
         *  - 해결법
         *      - bluk연산 뒤에는 영속성 컨텍스트를 모두 비우는것이 좋다.
         *
         *  - 단, 벌크만 실행되고 API가 끝나거나 트랜잭션이 종료가 된다면 크게 문제가 없을 수도 있다.
         *  - Spring Data JPA는
         *      - @Modifying에 옵션으로 clearAutomatically 옵션을 제공해준다.
         *
         *  JPA와 Mybatis를 같이 사용하면 Mybatis 쿼리를 날린 후 항상 clear()를 해주어야 한다.
         */
        int resultCount= memberRepository.bulkAgePlus(20);
        entityManager.flush();
        entityManager.clear();

        List<Member> member5 = memberRepository.findByUsername("member5");
        System.out.println("member5 = " + member5);


        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    void findMemberLazy() {
        //given
        //member1 -> temaA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        entityManager.flush();
        entityManager.clear();

//        List<Member> members = memberRepository.findAll();
        List<Member> members = memberRepository.findMemberFetchJoin();


        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }

    }

    @Test
    void entityGraph() {
        //given
        //member1 -> temaA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        entityManager.flush();
        entityManager.clear();

        List<Member> members = memberRepository.findAll();


        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }

    }
}