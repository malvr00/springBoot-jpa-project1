package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order finOne(Long id){
        return em.find(Order.class, id);
    }

    // 동적 쿼리 방법 1
    public List<Order> findAllByStirng(OrderSearch orderSearch){

        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if(orderSearch.getOrderStatus() != null){
            if(isFirstCondition){
                jpql += " where";
                isFirstCondition = false;
            }else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        // 회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            if(isFirstCondition){
                jpql += " where";
                isFirstCondition = false;
            }else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if(orderSearch.getOrderStatus() != null){
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())){
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    // 동적 쿼리 생성 2 ( JPA Criteria ) { 권장 X }
    // 단점 : 유지보수 어려움
    // 동적 쿼리는 QueryDSL 추천
    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if(orderSearch.getOrderStatus() != null){
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        // 회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);

        return query.getResultList();

    }

    // 장점 : 재사용성 높음
    public List<Order> findAllWithMemberDelivery() {
        // fetch = lazy 무시하고 객체를 전부 다 가져옴
        return em.createQuery("select o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d", Order.class
        ).getResultList();
    }


    // 장점 : V3보다 성능 최적화가 조금은 낫다
    // 단점 : dto 의존성이 큼, 코드 지저분함
//    public List<SimpleOrderQueryDto> findorderDtos() {
//        return em.createQuery(
//                "select new jpabook.jpashop.repository.SimpleOrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
//                        " join o.member m" +
//                        " join o.delivery d", SimpleOrderQueryDto.class
//        ).getResultList();
//    }
    public List<Order> findAllWithItem() {
        // db 에 있는 distinct 랑 다름. jpa 에서는 같은 객체도 제거 해줌
        // OneToMany 사용 시 페이징 처리 불가능.
        // 이유는 데이터 뻥튀기 되기 때문에 불가능.
        // 모든 데이터를 가져오고 메모리에서 데이터를 퍼올림
        // 페이징이 필요할 경우 사용하면 안됨
        // 컬랙션 패치 조인은 하나만 써야함.
        return em.createQuery(
                        "select distinct o from Order o" +
                                " join fetch o.member m" +
                                " join fetch  o.delivery d" +
                                " join fetch o.orderItems oi" +
                                " join fetch oi.item i", Order.class)
//                .setFirstResult(1)
//                .setMaxResults(100)
                .getResultList();
    }

    // default_batch_fetch_size: 100
    // 장점
    // 쿼리 호출 수가 1 + N -> 1 + 1 로 최적화 된다.
    // 조인보다 DB 데이터 전송량이 최적화된다.
    // 페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소한다.
    // 컬렉션 페치 조인은 페이징이 불가능 하지만 이 방법은 페이징이 가능하다
    // 결론
    // ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다.
    // 따라서 ToOne 관계는 페치조인으로 쿼리 수를 줄이고 해해결하고, 나머지는 default_batch_fetch_size 로 최적화 한다.
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery("select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
                ).setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
