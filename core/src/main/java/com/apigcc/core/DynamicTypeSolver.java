package com.apigcc.core;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 优先使用scan
 * scan不存在的情况下才使用ignore（提高效率，防止加载过多不需要的包）
 *
 * @email eumji025@gmail.com
 * @author:EumJi
 * @date: 2019/3/23
 * @time: 11:33
 */
public class DynamicTypeSolver implements TypeSolver {
    private static Logger logger = LoggerFactory.getLogger(DynamicTypeSolver.class);
    private TypeSolver parent;
    private List<String> ignorePackages = Arrays.asList("org.spring", "java.", "javax");
    private List<String> scanPackage = Arrays.asList("com.eumji", "com.sf.framework.domain", "com.demo.domain", "org.jruby.ir");
    Map<String, TypeSolver> typeSolverMap = new ConcurrentHashMap<>();

    public DynamicTypeSolver() {
        this(Arrays.asList("org.spring", "java."));
    }

    public DynamicTypeSolver(List<String> ignorePackages) {
        this.ignorePackages.addAll(ignorePackages);
    }

    public void setIgnorePackages(List<String> ignorePackages) {
        this.ignorePackages.addAll(ignorePackages);
    }

    public void addIgnorePackage(String... packageName) {
        Collections.addAll(ignorePackages, packageName);
    }

    public void setScanPackage(List<String> scanPackage) {
        this.scanPackage.addAll(scanPackage);
    }

    public void addScanPackage(String... packageName) {
        Collections.addAll(scanPackage, packageName);
    }

    @Override
    public TypeSolver getParent() {
        return parent;
    }

    @Override
    public void setParent(TypeSolver parent) {
        this.parent = parent;
    }

    @Override
    public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name) {
        try {
            Class<?> aClass = Class.forName(name);
            String classPath = aClass.getProtectionDomain().getCodeSource().getLocation().getPath();
            TypeSolver typeSolver = typeSolverMap.get(classPath);
            if (typeSolver != null) {
                return typeSolver.tryToSolveType(name);
            }
            if (logger.isDebugEnabled()){
                logger.debug("类型：{}对应的classpath为：{}",name,classPath);
            }
            //如果是源码编译的target/下依赖的类，则尝试获取源码解析
            if (classPath.endsWith("target/classes/")) {
                String newPath = classPath.replace("target/classes/", "src/main/java");
                File file = new File(newPath);
                if (file.exists()) {
                    typeSolverMap.put(classPath, typeSolver = new ModulePaserTypeSolver(file.getAbsolutePath()));
                } else {
                    typeSolverMap.put(classPath, typeSolver = new ClassLoaderTypeSolver(this.getClass().getClassLoader()));
                }
                return typeSolver.tryToSolveType(name);

            }
            //处于忽略的包里直接结束
            for (String ignorePackage : ignorePackages) {
                if (name.startsWith(ignorePackage)) {
                    return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
                }
            }

            for (String packageName : scanPackage) {
                if (name.startsWith(packageName)) {//是支持处理的包
                    String newPath = classPath.substring(0, classPath.lastIndexOf("."));
                    File file = new File(newPath + "-sources.jar");
                    if (file.exists()) {//自定义jar包处理方式
                        typeSolverMap.put(classPath, typeSolver = new JarResourcesTypeSolver(file.getAbsolutePath()));
                    } else {
                        typeSolverMap.put(classPath, typeSolver = new ClassLoaderTypeSolver(this.getClass().getClassLoader()));
                    }
                    return typeSolver.tryToSolveType(name);
                }
            }
        } catch (Exception e) {
            //护理失败
        }
        return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
    }
}
