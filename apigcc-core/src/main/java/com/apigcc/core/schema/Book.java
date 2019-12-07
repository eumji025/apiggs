package com.apigcc.core.schema;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.TreeSet;

/**
 * 每个项目里下的一个分类，可以通过tag {@code book}进行表示
 */
@Setter
@Getter
public class Book extends Node {

    public static final String DEFAULT = "index";

    Set<Chapter> chapters = new TreeSet<>();

    public Book(String id) {
        this.id = id;
    }
}
