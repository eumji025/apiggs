package com.apigcc.core.resolver.impl;

import com.apigcc.core.resolver.Types;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class ReferenceResolver extends Resolver {
    @Override
    public boolean accept(ResolvedType resolvedType) {
        return super.accept(resolvedType) && resolvedType.isReferenceType() && accept(resolvedType.asReferenceType().getTypeDeclaration());
    }

    /**
     * 修复因为 referenceType.getTypeParametersMap 获取到的pair的key值永远都是default值导致的问题，目前已提交pr给javaPaser
     * <p>
     * 临时适配方案
     *
     * @param types
     * @param resolvedType
     */
    @Override
    public void resolve(Types types, ResolvedType resolvedType) {
        ResolvedReferenceType referenceType = resolvedType.asReferenceType();
        ResolvedReferenceTypeDeclaration typeDeclaration = referenceType.getTypeDeclaration();
        //List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = referenceType.getTypeParametersMap();
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = new ArrayList<>();
        if (!referenceType.isRawType()) {
            List<ResolvedType> resolvedTypes = referenceType.typeParametersValues();
            for (int i = 0; i < typeDeclaration.getTypeParameters().size(); i++) {
                typeParametersMap.add(new Pair<>(typeDeclaration.getTypeParameters().get(i), resolvedTypes.get(i)));
            }
        }
        resolve(types, typeDeclaration, typeParametersMap);
    }

    public abstract boolean accept(ResolvedReferenceTypeDeclaration typeDeclaration);

    public abstract void resolve(Types types, ResolvedReferenceTypeDeclaration typeDeclaration,
                                 List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap);
}
