package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

// 값 타입은 기본적으로 값이 변경이 되면 안됨.
// 생성 할 때만 값이 세팅 되는 방식이 좋음
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() {}

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

}
