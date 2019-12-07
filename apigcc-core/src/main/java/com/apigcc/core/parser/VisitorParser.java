package com.apigcc.core.parser;

import com.apigcc.core.common.helper.AppendixHelper;
import com.apigcc.core.common.helper.OptionalHelper;
import com.apigcc.core.schema.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Objects;
import java.util.Optional;

public class VisitorParser extends VoidVisitorAdapter<Node> {

    private ParserStrategy parserStrategy;

    public void setParserStrategy(ParserStrategy parserStrategy) {
        this.parserStrategy = parserStrategy;
    }

    @Override
    public void visit(final ClassOrInterfaceDeclaration n, final Node arg) {

        if (arg instanceof Project && parserStrategy.accept(n)) {
            Project project = (Project) arg;
            Chapter chapter = new Chapter();
            n.getFullyQualifiedName().ifPresent(chapter::setId);
            chapter.setName(n.getNameAsString());
            n.getComment().ifPresent(chapter::accept);

            OptionalHelper.any(chapter.getTag("book"),chapter.getTag("group"))
                    .ifPresent(tag -> chapter.setBookName(tag.getContent()));

            parserStrategy.visit(n, chapter);
            project.addChapter(chapter);
            super.visit(n, chapter);
        }
    }

    @Override
    public void visit(final MethodDeclaration n, final Node arg) {
        if (arg instanceof Chapter && parserStrategy.accept(n)) {
            Chapter chapter = (Chapter) arg;
            Section section = new Section();
            section.setId(n.getNameAsString());
            section.setName(n.getNameAsString());
            section.setIndex(chapter.getSections().size());
            n.getComment().ifPresent(section::accept);

            parserStrategy.visit(n, chapter, section);
            chapter.getSections().add(section);
            super.visit(n, section);
        }
    }


    @Override
    public void visit(JavadocComment n, Node arg) {
        super.visit(n, arg);
        Optional<com.github.javaparser.ast.Node> node = n.getCommentedNode();
        if (!node.isPresent()) {
            return;
        }
        if (node.get() instanceof EnumDeclaration) {
            Appendix parse = AppendixHelper.parse(n);

            Project project = (Project) arg;
            project.addAppendix(parse);
        }
//        if (arg instanceof Tree) {
//            Tree tree = (Tree) arg;
//            Comments javadoc = Comments.of(n);
//            //解析部分自定义标签
//            for (Tag tag : javadoc.getTags()) {
//                if (Objects.equals(tag.getName(), Tags.readme.name())) {
//                    tree.setReadme(tag.getContent());
//                }
//                if (Objects.equals(tag.getName(), Tags.title.name())) {
//                    tree.setName(tag.getContent());
//                }
//                if (Objects.equals(tag.getName(), Tags.description.name())) {
//                    tree.setDescription(tag.getContent());
//                }
//                if (Objects.equals(tag.getName(), Tags.code.name())) {
//                    Appendix appendix = Appendix.parse(n);
//                    if(appendix!=null){
//                        tree.getAppendices().add(appendix);
//                    }
//                }
//            }
//        }

    }

}
