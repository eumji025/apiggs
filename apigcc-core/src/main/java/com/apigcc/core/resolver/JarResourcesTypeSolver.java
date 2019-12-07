package com.apigcc.core.resolver;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.ParserConfiguration.LanguageLevel.BLEEDING_EDGE;
import static com.github.javaparser.utils.Utils.assertNotNull;

/**
 *
 * jar包源码解析
 * @email eumji025@gmail.com
 * @author eumJi
 * @date 2019/4/4
 * @time 22:29
 */

public class JarResourcesTypeSolver implements TypeSolver {

	private static Logger logger = LoggerFactory.getLogger(JarResourcesTypeSolver.class);

	private final Map<String, CompilationUnit> parsedFiles = new ConcurrentHashMap<>();

	private volatile AtomicBoolean initFlag = new AtomicBoolean(false);

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
		//首先进行
		CompilationUnit compilationUnit = parse(name);
		if (compilationUnit != null) {
			//获取包信息
			Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
			if (packageDeclaration.isPresent()) {
				String packageName = packageDeclaration.get().getNameAsString();
				//搜索是否包含特定名称的TypeDeclaration
				Optional<TypeDeclaration<?>> astTypeDeclaration = compilationUnit.getTypes().stream().filter((t) -> (packageName + "." + t.getNameAsString()).equals(name)).findFirst();
				//如果存在则构建解析的ResolvedReferenceTypeDeclaration
				if (astTypeDeclaration.isPresent()) {
					return SymbolReference.solved(JavaParserFacade.get(this).getTypeDeclaration(astTypeDeclaration.get()));
				}
			}
		} else {
			return secondTypeResolver.tryToSolveType(name);
		}
		return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
	}
	//获取路径名
	public String fullPath(String name) {
		return scrJarPath + "/" + name.replace(".", "/") + ".java";
	}

	//解析名称
	private CompilationUnit parse(String name) {
		try {
			//判断是否初始化
			if (!initFlag.get()) {
				synchronized (this) {
					if (!initFlag.compareAndSet(false,true)) {//
						parseJarTotal();
						//initFlag.set(true);
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(name+"尝试使用jar解析器解析失败",e);
		}
		return parsedFiles.get(fullPath(name));
	}

	private void parseJarTotal() throws IOException {
		String srcJarPath = this.scrJarPath;
		//构建jar文件对象
		try (JarFile jarFile = new JarFile(srcJarPath)) {
			//遍历
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();
				//解析所有java文件
				if (jarEntry.getName().endsWith(".java")) {
					//通过JavaParser解析java文件
					Optional<CompilationUnit> compilationUnit = javaParser.parse(COMPILATION_UNIT, provider(jarFile.getInputStream(jarEntry), Charset.defaultCharset())).getResult().map(cu -> cu.setStorage(Paths.get(srcJarPath + "/" + jarEntry.getName())));
					//记录到缓存里
					compilationUnit.ifPresent(value -> parsedFiles.put(srcJarPath + "/" + jarEntry.getName(), value));
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("jarFileEntry:{}not java file,will be ignore", jarEntry.getName());
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
		} catch (IOException e) {
			throw new IllegalArgumentException("获取jar的provider失败",e);
		}
	}
}