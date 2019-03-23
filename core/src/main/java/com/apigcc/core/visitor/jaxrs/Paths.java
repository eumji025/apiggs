package com.apigcc.core.visitor.jaxrs;

import com.apigcc.core.resolver.ast.Annotations;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * JAX RS @Path 解析工具
 */
public class Paths {

    public static final String CONTROLLER = "Path";
    public static final String RESPONSE_BODY = "Produces";

    public static final List<String> jsonTypes = Arrays.asList("*/*", "APPLICATION/JSON", "application/json");

    public static final List<String> ANNOTATIONS = Arrays.asList(CONTROLLER);

    public static boolean accept(NodeList<AnnotationExpr> nodes) {
        for (AnnotationExpr node : nodes) {
            if (CONTROLLER.equals(node.getNameAsString())) {
                return true;
            }
        }
        return false;
    }

//    public static boolean accept(AnnotationExpr n){
//        if(!ANNOTATIONS.contains(n.getNameAsString())){
//            return false;
//        }
//        return true;
//    }

    public static boolean isResponseBody(ClassOrInterfaceDeclaration n) {
        Optional<AnnotationExpr> producesOptional = n.getAnnotationByName(RESPONSE_BODY);
        Object value = Annotations.getAttr(producesOptional, "value");
        if (value == null) {
            return false;
        } else if (value instanceof List) {
            List<String> types = (List<String>) value;
            for (String type : types) {
                if (type.contains("*/*") || type.contains("APPLICATION/JSON") || type.contains("application/json")) {
                    return true;
                }
            }
        }else {
            String[] types = (String[]) value;
            for (String type : types) {
                if (type.contains("*/*") || type.contains("APPLICATION/JSON") || type.contains("application/json")) {
                    return true;
                }
            }
        }

        return false;

    }
}
