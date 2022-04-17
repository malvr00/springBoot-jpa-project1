package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import jpabook.jpashop.repository.order.simplequery.SimpleOrderQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne ( ManyToOne, OneToOne )
 * Order 조회
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    // ==================== 주문조회 =================== //
    //== 1 ==//
    // Entity return 하게 되면 성능 이슈가 발생. Entity 상에 사용하지 않는 객체들도 추가로 쿼리 발생 ( ex -> orderItem 도 출력 )
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByStirng(new OrderSearch());
        return all;
    }

    //== 2 ==//
    // 성능문제 N + 1 발생
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2(){
        List<Order> orders = orderRepository.findAllByStirng(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    //== 3 ==//
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3(){
       List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    //== 4 ==//
    @GetMapping("/api/v4/simple-orders")
    public List<SimpleOrderQueryDto> orderV4(){
        return orderSimpleQueryRepository.findorderDtos();
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
