package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    // open-in-view( OSIV ): false 로 주게 되면 트랜젝션 안에서 영속성이 완전이 클리어 되기 때문에 컨트롤러와 뷰 템플릿에서 사용할 수 없다.
    // 그래서 별도로 proxy 관리하는 파일을 분리해서 관리한다.
    //  화면이나 API에 맞춘 서비스 ( 주로 읽기 전용 트렌잭션 사용 ) - ( 읽기, 쓰기 - 수정 분리? { 핵심 비즈니스 } )
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }

    @Getter
    static class OrderDto{

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //        private List<OrderItem> orderItems; // dto 안에서도 entity 가 존재하면 안됨
        private List<OrderItemDto> orderItems;

        public OrderDto(Order o) {
            this.orderId = o.getId();
            this.name = o.getMember().getName();
            this.orderDate = o.getOrderDate();
            this.orderStatus = o.getStatus();
            this.address = o.getDelivery().getAddress();

            this.orderItems = o.getOrderItems().stream()
                    .map(orderItems -> new OrderItemDto(orderItems))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;    // 상품 명
        private int orderPrice;     // 주문 가격
        private int count;          // 주문 수량

        public OrderItemDto(OrderItem orderItem){
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getItem().getPrice();
            this.count = orderItem.getCount();
        }
    }
}
