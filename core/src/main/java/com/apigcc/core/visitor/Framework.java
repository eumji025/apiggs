package com.apigcc.core.visitor;

import com.apigcc.core.visitor.jaxrs.JaxrsVisitor;
import com.apigcc.core.visitor.jfinal.JFinalVisitor;
import com.apigcc.core.visitor.springmvc.SpringVisitor;
import com.github.javaparser.ast.CompilationUnit;

import java.util.Arrays;
import java.util.List;

/**
 * 判断当前项目使用了什么框架
 */
public enum Framework {

    SPRINGMVC(new SpringVisitor()),
    JFINAL(new JFinalVisitor()),
    JAXRS(new JaxrsVisitor()),
    ;

    private NodeVisitor visitor;

    Framework(NodeVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * 根据解析的内容选择合适的框架
     * @param cu
     * @return
     */
    public static NodeVisitor chooseVisitor(CompilationUnit cu) {
        for (Framework framework : values()) {
            if(framework.getVisitor().accept(cu)){
                return framework.getVisitor();
            }
        }
        return Framework.SPRINGMVC.getVisitor();
        //throw new IllegalStateException("can not find any framework in project, only support "+ Arrays.toString(values()));
    }

    public NodeVisitor getVisitor() {
        return visitor;
    }

    public static Framework current;

    /**
     * 获取当前代码环境所使用的框架信息
     * @param cus
     * @return
     */
    public static Framework getCurrent(List<CompilationUnit> cus){
        if(current == null){
            current = findoutFramework(cus);
        }
        return current;
    }

    /**
     * 解析环境，找到对应的框架
     * @param results
     * @return
     */
    public static Framework findoutFramework(List<CompilationUnit> results){
        for (CompilationUnit cu : results) {
            for (Framework framework : values()) {
                if(framework.getVisitor().accept(cu)){
                    return framework;
                }
            }
        }
        throw new IllegalStateException("can not find any framework in project, only support "+ Arrays.toString(values()));
    }

}
