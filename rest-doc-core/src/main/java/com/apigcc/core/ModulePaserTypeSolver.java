package com.apigcc.core;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

import java.io.File;
import java.nio.file.Path;

/**
 * @email eumji025@gmail.com
 * @author:EumJi
 * @date: 2019/4/4
 * @time: 22:22
 */
public class ModulePaserTypeSolver extends JavaParserTypeSolver {
    private ClassLoaderTypeSolver classLoaderTypeSolver =

            new ClassLoaderTypeSolver(ModulePaserTypeSolver.class.getClassLoader());

    public ModulePaserTypeSolver(File srcDir) {
        super(srcDir);
    }

    public ModulePaserTypeSolver(String srcDir) {
        super(srcDir);
    }

    public ModulePaserTypeSolver(File srcDir, ParserConfiguration parserConfiguration) {
        super(srcDir, parserConfiguration);
    }

    public ModulePaserTypeSolver(String srcDir, ParserConfiguration parserConfiguration) {
        super(srcDir, parserConfiguration);
    }

    public ModulePaserTypeSolver(Path srcDir, ParserConfiguration parserConfiguration) {
        super(srcDir, parserConfiguration);
    }

    public ModulePaserTypeSolver(Path srcDir) {
        super(srcDir);
    }

    @Override
    public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name) {
        try {
            SymbolReference<ResolvedReferenceTypeDeclaration> reference = super.tryToSolveType(name);
            if (!reference.isSolved()) {
                reference = classLoaderTypeSolver.tryToSolveType(name);
            }
            return reference;
        } catch (Exception e) {
            return classLoaderTypeSolver.tryToSolveType(name);
        }
    }
}
