package com.apigcc.core.render;

import com.apigcc.core.Apigcc;
import com.apigcc.core.common.helper.FileHelper;
import com.apigcc.core.common.markup.MarkupBuilder;
import com.apigcc.core.common.markup.markdown.Markdown;
import com.apigcc.core.schema.*;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.*;

/**
 * markdown build like asciidoc
 */
@Slf4j
public class MarkDownRender implements ProjectRender {


    @Override
    public void render(Project project) {
//获取build的路径
        Path buildPath = Apigcc.getInstance().getContext().getBuildPath();
        //获取项目构建的路径
        Path projectBuildPath = buildPath.resolve(project.getId());
        //遍历
        project.getBooks().forEach((name, book) -> {
            //获取asciiDoc的构建起
            MarkupBuilder builder = MarkupBuilder.getMarkDownInstance();
            String displayName = project.getName();
            if (!Objects.equals(Book.DEFAULT, name)) {
                displayName += " - " + name;
            }
            builder.header(displayName, null);
            if (Objects.nonNull(project.getVersion())) {
                builder.paragraph("version:" + project.getVersion());
            }
            builder.paragraph(project.getDescription());
            for (Chapter chapter : book.getChapters()) {
                if (chapter.isIgnore() || chapter.getSections().isEmpty()) {
                    continue;
                }
                //标题信息
                builder.title(1, chapter.getName());
                builder.paragraph(chapter.getDescription());
                for (Section section : chapter.getSections()) {
                    if (section.isIgnore()) {
                        continue;
                    }
                    builder.title(2, section.getName());
                    builder.paragraph(section.getDescription());


                    builder.title(4, "请求参数示例");

                    builder.listing(b -> {
                        //请求url信息
                        b.textLine(section.getRequestLine());
                        //请求header
                        section.getInHeaders().values().forEach(header -> builder.textLine(header.toString()));
                        //如果存在body
                        if (section.hasRequestBody()) {
                            if (section.getInHeaders().values().size() > 0){
                                b.br();
                            }
                            b.text(section.getParameterString());
                        }
                    }, "source,HTTP");
                    builder.title(4, "请求参数描述");
                    //参数的列表构建
                    table(builder, section.getRequestRows().values());

                    builder.title(4, "响应参数示例");

                    builder.listing(b -> {
                        //构建header
                        section.getOutHeaders().values().forEach(header -> builder.textLine(header.toString()));
                        //如果有响应体，构建响应体
                        if (section.hasResponseBody()) {
                            if (section.getOutHeaders().values().size() > 0){
                                b.br();
                            }
                            b.text(section.getResponseString());
                        } else {
                            b.text("N/A");
                        }
                    }, "source,JSON");
                    builder.title(4, "响应参数描述");
                    //结果的列表构建
                    table(builder, section.getResponseRows().values());

                }
            }
            //输出markdown信息到文件
            Path markdownFile = projectBuildPath.resolve(name + Markdown.EXTENSION);
            FileHelper.write(markdownFile, builder.getContent());
            log.info("Build Markdown {}", markdownFile);
        });


    }

    private void table(MarkupBuilder builder, Collection<Row> rows) {
        if (rows.size() > 0) {
            List<List<String>> responseTable = new ArrayList<>();
            responseTable.add(Lists.newArrayList("参数名", "类型", "校验条件", "默认值", "备注"));
            rows.forEach(row -> responseTable.add(Lists.newArrayList(row.getKey(), row.getType(), row.getCondition(), row.getDef(), row.getRemark())));
            builder.table(responseTable, true, false);
        }
    }

}
