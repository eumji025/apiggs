package com.apigcc.core;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 优先使用scan
 * scan不存在的情况下才使用ignore（提高效率，防止加载过多不需要的包）
 * @email eumji025@gmail.com
 * @author:EumJi
 * @date: 2019/3/23
 * @time: 11:33
 */
public class DynamicTypeSolver implements TypeSolver {
    private TypeSolver parent;

    private List<String> ignorePackages;

    private List<String> scanPackage;

    Map<String, JarTypeSolver> jarTypeSolverMap = new ConcurrentHashMap<>();

    public DynamicTypeSolver() {
        ignorePackages = Arrays.asList("org.spring","java.");
    }

    public DynamicTypeSolver(List<String> ignorePackages) {
        this.ignorePackages = ignorePackages;
    }

    public void setIgnorePackages(List<String> ignorePackages) {
        this.ignorePackages = ignorePackages;
    }

    public void addIgnorePackage(String... packageName) {
        for (String name : packageName) {
            ignorePackages.add(name);
        }
    }

    public void setScanPackage(List<String> scanPackage) {
        this.scanPackage = scanPackage;
    }

    public void  addScanPackage(String... packageName){
        for (String name : packageName) {
            scanPackage.add(name);
        }
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
            System.out.println(classPath);

            //忽略的包
            for (String ignorePackage : ignorePackages) {
                if (name.startsWith(ignorePackage)){
                    return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
                }
            }

            String newPath = classPath.substring(0,classPath.lastIndexOf("."));
            File file = new File(newPath + "-sources.jar");
            if (file.exists()){
                jarTypeSolverMap.put(classPath,new JarTypeSolver(file.getPath()));
            }else {
                jarTypeSolverMap.put(classPath,new JarTypeSolver(classPath));
            }

            JarTypeSolver jarTypeSolver = jarTypeSolverMap.get(classPath);
            return jarTypeSolver.tryToSolveType(name);

        }catch (Exception e){
            System.out.println("解析失败,name is :" + name);
        }
        return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
    }
}
