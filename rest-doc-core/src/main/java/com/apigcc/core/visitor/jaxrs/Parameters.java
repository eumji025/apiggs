package com.apigcc.core.visitor.jaxrs;

import com.apigcc.core.common.Cell;
import com.apigcc.core.common.loging.Logger;
import com.apigcc.core.common.loging.LoggerFactory;
import com.apigcc.core.resolver.TypeResolvers;
import com.apigcc.core.resolver.Types;
import com.apigcc.core.resolver.ast.Annotations;
import com.apigcc.core.resolver.ast.Comments;
import com.apigcc.core.resolver.ast.Defaults;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Spring Mvc 参数类型解析
 */
@Setter
@Getter
public class Parameters {

    static Logger log = LoggerFactory.getLogger(Parameters.class);

    //public static final String REQUEST_BODY = null;
    public static final String REQUEST_PARAM = "QueryParam";
    public static final String REQUEST_HEADER = "HeaderParam";
    public static final String PATH_VARIABLE = "PathParam";
    public static final String FORM_VARIABLE = "FormParam";

    public static final String MVC_MODEL = "MODEL";

    public static final Set<String> MVCS = Sets.newHashSet(MVC_MODEL);

    /**
     * 是否基本类型(包括string)
     */
    boolean primitive;
    /**
     * 是否路径字段
     */
    boolean pathVariable;
    /**
     * 是否请求体接收字段
     */
    boolean requestBody;
    /**
     * 是否是header
     */
    boolean header;
    /**
     * 是否文件字段
     */
    boolean file;
    /**
     * 是否spring mvc 保留字段
     */
    boolean mvc;

    String name;
    String type;
    Object value;
    String description;
    List<Cell<String>> cells = new ArrayList<>();

    public static Parameters of(Parameter expr) {
        Parameters parameters = new Parameters();

        //TODO file mvc ?

        if (expr.isAnnotationPresent(PATH_VARIABLE)) {
            parameters.setPathVariable(true);
            parameters.resolvePath(expr);
        } else if(expr.isAnnotationPresent(REQUEST_HEADER)){
            parameters.setHeader(true);
            parameters.resolveHeader(expr);
        } else {
//            if (expr.isAnnotationPresent(REQUEST_BODY)) {
//                parameters.setRequestBody(true);
//            }
            parameters.setRequestBody(true);
            parameters.tryResolve(expr);
        }


        return parameters;
    }

    private void resolvePath(Parameter expr){
        Cell<String> cell = new Cell<>(expr.getNameAsString(), expr.getTypeAsString(),"",
                String.valueOf(Defaults.get(expr.getTypeAsString())), Comments.getCommentFromMethod(expr));
        cell.setEnable(false);
        cells.add(cell);
    }
    private void resolveHeader(Parameter expr){
        name = expr.getNameAsString();
        type = expr.getTypeAsString();
        value = Defaults.get(type);
        //解析RequestParam 获取字段名和默认值
        Object valueAttr = Annotations.getAttr(expr.getAnnotationByName(REQUEST_HEADER), "value");
        Object defaultValueAttr = Annotations.getAttr(expr.getAnnotationByName(REQUEST_HEADER), "defaultValue");
        if (valueAttr != null) {
            name = String.valueOf(valueAttr);
        }
        if (defaultValueAttr != null) {
            value = defaultValueAttr;
        }
        Cell<String> cell = new Cell<>(name, type, "", String.valueOf(value), Comments.getCommentFromMethod(expr));
        cell.setEnable(false);
        cells.add(cell);
    }

    private void tryResolve(Parameter expr) {
        try {
            ResolvedParameterDeclaration parameterDeclaration = expr.resolve();
            Types astResolvedType = TypeResolvers.of(parameterDeclaration.getType());
            if(!astResolvedType.isResolved()){
                return;
            }
            setPrimitive(astResolvedType.isPrimitive());
            setValue(astResolvedType.getValue());

            if (astResolvedType.isPrimitive()) {

                String name = expr.getNameAsString();
                String value = String.valueOf(astResolvedType.getValue());
                //解析RequestParam 获取字段名和默认值
                Object valueAttr = Annotations.getAttr(expr.getAnnotationByName(REQUEST_PARAM), "value");
                if (valueAttr != null) {
                    name = String.valueOf(valueAttr);
                }
                Object defaultValueAttr = Annotations.getAttr(expr.getAnnotationByName(REQUEST_PARAM), "defaultValue");
                if (defaultValueAttr != null) {
                    value = String.valueOf(defaultValueAttr);
                }
                cells.add(new Cell<>(name, astResolvedType.getName(), "", value, Comments.getCommentFromMethod(expr)));
            }
            cells.addAll(astResolvedType.getCells());

        } catch (Exception e) {
            log.debug("parameters parse fail:{}",expr.toString());
        }
    }

}
