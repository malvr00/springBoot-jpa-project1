package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item){
        if(item.getId() == null){
            em.persist(item);
        }else {
            // 머지 방법 ( 준영속 상태 )
            // item 키 값이 존재하지 않으면 find 해서 영속성 상태를 만듦
            // 장점 : 단순할 경우 편함.
            // 단점 : 전체 다 변경이 되고 원하는 부분은 변경이 어려움. 객체에 값이 빠져 있으면 DB에 null 값이 저장되게 됨
            // 대도록이면 사용하면 안됨. 정말 단순할 경우에만 사용
            em.merge(item);
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

}
