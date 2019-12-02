package com.apigcc.core.handler;

import com.apigcc.core.Options;
import com.apigcc.core.common.Cell;
import com.apigcc.core.http.HttpMessage;
import com.apigcc.core.http.HttpRequest;
import com.apigcc.core.http.HttpResponse;
import com.apigcc.core.schema.Appendix;
import com.apigcc.core.schema.Group;
import com.apigcc.core.schema.Tree;
import io.github.swagger2markup.markup.builder.internal.markdown.MarkdownBuilder;
import org.apache.commons.collections.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @description: markdown文件生成的handler
 * @email eumji025@gmail.com
 * @author: EumJi
 * @date: 2018-10-27
 */
public class MarkdownTreeHandler implements TreeHandler {
    private static final String newLineFlag = System.getProperty("line.separator");
    MarkdownBuilder builder = new MarkdownBuilder();

    @Override
    public void handle(Tree tree, Options options) {
        builder.documentTitle(tree.getName());
        if (Objects.nonNull(tree.getVersion())) {
            builder.paragraph("version:" + tree.getVersion());
        }
        if (Objects.nonNull(tree.getDescription())) {
            builder.paragraph(tree.getDescription(), true);
        }

        for (int i = 0; i < tree.getBucket().getGroups().size(); i++) {
            Group group = tree.getBucket().getGroups().get(i);
            buildGroup(group, "", i + 1);
        }

        if (!tree.getAppendices().

                isEmpty()) {
            builder.sectionTitleLevel1("附录");
            int i = 1;
            for (Appendix appendix :
                    tree.getAppendices()) {
                if (!appendix.isEmpty()) {
                    builder.sectionTitleLevel2(i++ + "." + appendix.getName());
                    appendix(appendix.getCells());
                }
            }
        }

        Path adoc = options.getOutPath().resolve(options.getId());
        builder.writeToFile(adoc, StandardCharsets.UTF_8);
    }


    private void buildGroup(Group group, String prefix, int num) {
        builder.sectionTitleLevel1(prefix + num + " " + group.getName());
        if (Objects.nonNull(group.getDescription())) {
            builder.paragraph(group.getDescription(), true);
        }
        for (int i = 0; i < group.getNodes().size(); i++) {
            HttpMessage httpMessage = group.getNodes().get(i);
            buildHttpMessage(httpMessage, prefix + num + ".", i + 1);
        }
    }

    private void buildHttpMessage(HttpMessage message, String prefix, int num) {
        builder.sectionTitleLevel2(prefix + num + " " + message.getName());
        if (Objects.nonNull(message.getDescription())) {
            builder.paragraph(documentConvert(message.getDescription()), true);
        }

        requestMessageBuild(message);


        responseMessageBuild(message);

    }

    private void responseMessageBuild(HttpMessage message) {
        HttpResponse response = message.getResponse();
        builder.sectionTitleLevel3("响应信息示例");
        if (!response.isEmpty()) {
            builder.listingBlock(((Supplier<String>) () -> {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(message.getVersion()).append(" ").append(response.getStatus());
                if (!response.getHeaders().isEmpty()) {
                    stringBuilder.append(newLineFlag);
                    response.getHeaders().forEach((k, v) -> stringBuilder.append(k).append(": ").append(v).append(newLineFlag));
                    if (response.hasBody()) {
                        stringBuilder.append(response.bodyString());
                    }
                } else if (response.hasBody()) {
                    stringBuilder.append(newLineFlag);
                    stringBuilder.append(response.bodyString());
                }

                String value = stringBuilder.toString();
                return value.endsWith(newLineFlag) ? value.substring(0, value.length() - newLineFlag.length()) : value;
            }).get(), "response");
            builder.sectionTitleLevel3("响应参数详情");
            table(response.getCells());
        }
    }

    /**
     * 构建response的message
     *
     * @param message
     */
    private void requestMessageBuild(HttpMessage message) {
        HttpRequest request = message.getRequest();
        builder.sectionTitleLevel3("请求参数示例");
        builder.listingBlock(((Supplier<String>) () -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (String uri : request.getUris()) {
                stringBuilder.append(request.getMethod()).append(" ").append(uri).append(request.queryString()).append(" ").append(message.getVersion()).append(newLineFlag);
            }
            if (!request.getHeaders().isEmpty()) {
                request.getHeaders().forEach((k, v) -> stringBuilder.append(k).append(": ").append(v).append(newLineFlag));

            }
            if (request.hasBody()) {
                stringBuilder.append(request.bodyString());
            }
            String value = stringBuilder.toString();
            return value.endsWith(newLineFlag) ? value.substring(0, value.length() - newLineFlag.length()) : value;
        }).get(), "request");
        builder.sectionTitleLevel3("请求参数详情");
        table(message.getRequest().getCells());
    }

    private void table(List<Cell<String>> cells) {
        if (CollectionUtils.isNotEmpty(cells)) {
            List<List<String>> responseTable = new ArrayList<>();
            responseTable.add(Arrays.asList("属性名", "类型","校验", "默认值", "描述"));
            responseTable.add(Arrays.asList("----", "----", "----", "----", "----"));
            cells.forEach(parameter -> responseTable.add(parameter.toList()));
            builder.table(responseTable);
        }

    }

    private void appendix(List<Cell<String>> cells) {
        if (cells.size() > 0) {
            List<List<String>> responseTable = new ArrayList<>();
            List<String> titles = cells.get(0).toList();
            responseTable.add(titles);
            List<String> titleSplits = new ArrayList<>(titles.size());
            for (String ignored :
                    titles) {
                titleSplits.add("----");
            }
            responseTable.add(titleSplits);
            cells.forEach(parameter -> responseTable.add(parameter.toList()));
            builder.table(responseTable);
        }
    }

    /**
     * 格式转换
     *
     * @param message
     * @return
     */
    public static String documentConvert(String message) {
        String targetMessage = message.replace("<pre>", "```java\n");
        targetMessage = targetMessage.replace("</pre>", "\n```");
        return targetMessage;
    }

}
