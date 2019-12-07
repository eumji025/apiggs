package com.apigcc.core.common.helper;

import com.apigcc.core.schema.Appendix;
import com.apigcc.core.schema.Cell;
import com.apigcc.core.schema.Node;
import com.apigcc.core.util.CollectionUtils;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 附录
 */
@Setter
@Getter
public class AppendixHelper extends Node {

    public static String NEW_LINE = System.getProperty("line.separator");

    @Nullable
    public static Appendix parse(JavadocComment n) {
        if (!n.getCommentedNode().isPresent()) {
            return null;
        }
        final com.github.javaparser.ast.Node node = n.getCommentedNode().get();
        if (!(node instanceof BodyDeclaration)) {
            return null;
        }
        final BodyDeclaration bodyDeclaration = (BodyDeclaration) node;
        if (!bodyDeclaration.isEnumDeclaration() && !bodyDeclaration.isClassOrInterfaceDeclaration()) {
            return null;
        }
        Appendix appendix = new Appendix();
        if (bodyDeclaration.isEnumDeclaration()) {
            parseEnum(appendix.getCells(), bodyDeclaration.asEnumDeclaration());
            appendix.getCells().addAll(parseValue(bodyDeclaration.asEnumDeclaration()));
        } else if (bodyDeclaration.isClassOrInterfaceDeclaration()) {
            //appendix.getCells().addAll(Fields.getConstants(bodyDeclaration.asClassOrInterfaceDeclaration()));
        }
        if (node instanceof NodeWithSimpleName) {
            //appendix.setName(((NodeWithSimpleName) node).getNameAsString());
        }
        //appendix.setDescription(node.getComment().get().getContent());
        node.getComment().ifPresent(appendix::accept);

        return appendix;
    }

    private static Collection<? extends Cell<String>> parseValue(EnumDeclaration enumDeclaration) {
        ArrayList<Cell<String>> cells = new ArrayList<>();
        for (EnumConstantDeclaration entry : enumDeclaration.getEntries()) {
            Cell<String> cell = new Cell<>();
            //entry.
            cell.add(entry.getNameAsString());
            entry.getArguments().forEach(arg -> cell.add(arg.toString()));
            cells.add(cell);
        }
        return cells;
    }

    /**
     * 枚举header解析
     *
     * @param cells
     * @param enumDeclaration
     */
    private static void parseEnum(List<Cell<String>> cells, EnumDeclaration enumDeclaration) {

        NodeList<BodyDeclaration<?>> entries = enumDeclaration.getMembers();
        Cell<String> header = new Cell<>();
        cells.add(header);
        header.add("枚举值");
        if (CollectionUtils.isEmpty(entries)) {
            return;
        }
        Map<String, String> parameterDesc = new HashMap<>();
        for (BodyDeclaration entry : entries) {

            if (entry instanceof FieldDeclaration) {
                FieldDeclaration fieldDeclaration = (FieldDeclaration) entry;
                //获取内容
                String content = fieldDeclaration.getComment().
                        orElse(new JavadocComment(fieldDeclaration.getVariable(0).getNameAsString())).getContent();
                //进行切割
                String desc = content.split("NEW_LINE")[0].trim();
                //去掉*号
                if (desc.startsWith("*")){
                    desc = desc.substring(1).trim();
                }
                parameterDesc.put(fieldDeclaration.getVariable(0).getNameAsString(), desc);

            }
        }


        for (BodyDeclaration<?> entry : entries) {
            if (entry instanceof ConstructorDeclaration) {
                ConstructorDeclaration constructor = (ConstructorDeclaration) entry;
                NodeList<Parameter> parameters = constructor.getParameters();
                for (Parameter parameter : parameters) {
                    String parameterName = parameter.getNameAsString();
                    String desc = parameterDesc.getOrDefault(parameterName, parameterName);
                    header.add(desc);
                }
                break;

            }
        }
    }

}
