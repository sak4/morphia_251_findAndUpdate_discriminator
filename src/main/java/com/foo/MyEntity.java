package com.foo;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

import java.util.Objects;
import java.util.UUID;

@Entity
public class MyEntity {
    @Id
    private String id = UUID.randomUUID().toString();
    private String name;
    private int value;
    private String nickname;

    public String getId() {
        return id;
    }

    public MyEntity setId(final String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public MyEntity setName(final String name) {
        this.name = name;
        return this;
    }

    public int getValue() {
        return value;
    }

    public MyEntity setValue(final int value) {
        this.value = value;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public MyEntity setNickname(final String nickname) {
        this.nickname = nickname;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof MyEntity)) return false;
        final MyEntity myEntity = (MyEntity) o;
        return getValue() == myEntity.getValue() && Objects.equals(getId(), myEntity.getId()) && Objects.equals(getName(), myEntity.getName()) && Objects.equals(getNickname(), myEntity.getNickname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getValue(), getNickname());
    }

    @Override
    public String toString() {
        return "MyEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
