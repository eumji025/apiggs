package com.apigcc.core.schema;


import com.apigcc.core.Context;
import com.apigcc.core.common.helper.CommentHelper;
import com.apigcc.core.common.helper.StringHelper;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.javadoc.Javadoc;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
public class Appendix {
    //标题
    private String title;
    //描述
    private String description;

    List<Cell<String>> cells = new LinkedList<>();


    public void accept(Comment comment) {
        if (!comment.isJavadocComment()) {
            setNameAndDescription(comment.getContent());
            return;
        }
        Javadoc javadoc = comment.asJavadocComment().parse();
        setNameAndDescription(CommentHelper.getDescription(javadoc.getDescription()));
    }



    public void setNameAndDescription(String content) {
        String[] arr = content.split("(\\r\\n)|(\\r)|(\\n)+", 2);
        if (arr.length >= 1 && StringHelper.nonBlank(arr[0])) {
            title = arr[0];
        }
        if (arr.length >= 2 && StringHelper.nonBlank(arr[1])) {
            description = arr[1];
        }
    }
}
