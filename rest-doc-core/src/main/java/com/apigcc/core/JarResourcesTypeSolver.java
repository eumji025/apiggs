package com.apigcc.core;

import com.apigcc.core.exception.TypeResolverException;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Provider;
import com.github.javaparser.StreamProvider;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.ParserConfiguration.LanguageLevel.BLEEDING_EDGE;
import static com.github.javaparser.utils.Utils.assertNotNull;

/**
 * @email eumji025@gmail.com
 * @author:EumJi
 * @date: 2019/4/4
 * @time: 22:29
 */

public class JarResourcesTypeSolver implements TypeSolver {

	private static Logger logger = LoggerFactory.getLogger(JarResourcesTypeSolver.class);

	private final Map<String, CompilationUnit> parsedFiles = new ConcurrentHashMap<>();

	private Boolean initFlag = false;

	private TypeSolver parent;

	private JavaParser javaParser;

	private String scrJarPath;

	private TypeSolver secondTypeResolver = new ReflectionTypeSolver();

	public JarResourcesTypeSolver(String scrJarPath) {
		this.scrJarPath = scrJarPath;
		ParserConfiguration parserConfiguration = new ParserConfiguration().setLanguageLevel(BLEEDING_EDGE);
		javaParser = new JavaParser(parserConfiguration);
	}

	@Override
	public TypeSolver getParent() {
		return parent;
	}

	@Override
	public void setParent(TypeSolver typeSolver) {
		parent = typeSolver;
	}

	@Override
	public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name) {
		CompilationUnit compilationUnit = parse(name);
		if (compilationUnit != null) {

			Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
			if (packageDeclaration.isPresent()) {
				String packageName = packageDeclaration.get().getNameAsString();
				Optional<TypeDeclaration<?>> astTypeDeclaration = compilationUnit.getTypes().stream().filter((t) -> (packageName + "." + t.getNameAsString()).equals(name)).findFirst();
				//Optional<com.github.javaparser.ast.body.TypeDeclaration<?>>astTypeDeclaration=Navigator.findType(compilationUnit,fullPath(name));
				if (astTypeDeclaration.isPresent()) {
					return SymbolReference.solved(JavaParserFacade.get(this).getTypeDeclaration(astTypeDeclaration.get()));
				}
			}
		} else {
			return secondTypeResolver.tryToSolveType(name);
		}
		return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
	}

	public String fullPath(String name) {
		return scrJarPath + "/" + name.replace(".", "/") + ".java";
	}

	private CompilationUnit parse(String name) {
		try {
			if (!initFlag) {
				synchronized (this) {
					if (!initFlag) {//½âÎöjar°ü
						parseJarTotal();
						initFlag = true;
					}
				}
			}
		} catch (Exception e) {
			throw new TypeResolverException(name+"尝试使用jar解析器解析失败",e);
		}
		return parsedFiles.get(fullPath(name));
	}

	private void parseJarTotal() throws IOException {
		String srcJarPath = this.scrJarPath;
		try (JarFile jarFile = new JarFile(srcJarPath)) {
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();
				if (jarEntry.getName().endsWith(".java")) {
					Optional<CompilationUnit> compilationUnit = javaParser.parse(COMPILATION_UNIT, provider(jarFile.getInputStream(jarEntry), Charset.defaultCharset())).getResult().map(cu -> cu.setStorage(Paths.get(srcJarPath + "/" + jarEntry.getName())));
					compilationUnit.ifPresent(value -> parsedFiles.put(srcJarPath + "/" + jarEntry.getName(), value));
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("jarFileEntry:{}notjavafile,willbeignore", jarEntry.getName());
					}
				}
			}
		}

	}

	public static Provider provider(InputStream input, Charset encoding) {
		assertNotNull(input);
		assertNotNull(encoding);
		try {
			return new StreamProvider(input, encoding.name());
		} catch (IOException e) {//TheonlyonethatisthrownisUnsupportedCharacterEncodingException,//andthat'safundamentalproblem,soruntimeexception.
			throw new TypeResolverException("获取jar的provider失败",e);
		}
	}
}
