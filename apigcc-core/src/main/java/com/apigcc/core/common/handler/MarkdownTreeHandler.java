package com.github.apiggs.handler;

import com.github.apiggs.Environment;
import com.github.apiggs.http.HttpMessage;
import com.github.apiggs.http.HttpRequest;
import com.github.apiggs.http.HttpResponse;
import com.github.apiggs.schema.Cell;
import com.github.apiggs.schema.Group;
import com.github.apiggs.schema.Tree;
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
    public void handle(Tree tree, Environment env) {
        builder.documentTitle(tree.getName());
        if (Objects.nonNull(tree.getVersion())) {
            builder.paragraph("version:" + tree.getVersion());
        }
        if (Objects.nonNull(tree.getDescription())) {
            builder.paragraph(tree.getDescription(), true);
        }

        for (int i = 0; i < tree.getGroups().size(); i++) {
            Group group = tree.getGroups().get(i);
            buildGroup(group, "", i + 1);
        }

        Path adoc = env.getOutPath().resolve(env.getId());
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
            builder.paragraph(message.getDescription(), true);
        }

        requestMessageBuild(message);


        responseMessageBuild(message);

    }

    private void responseMessageBuild(HttpMessage message) {
        HttpResponse response = message.getResponse();
        builder.sectionTitleLevel3("RESPONSE INFO");
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
            builder.sectionTitleLevel3("PARAMETERS");
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
        builder.sectionTitleLevel3("REQUEST INFO");
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
        builder.sectionTitleLevel3("PARAMETERS");
        table(message.getRequest().getCells());
    }

    private void table(List<Cell> cells) {
        if (CollectionUtils.isNotEmpty(cells)) {
            List<List<String>> responseTable = new ArrayList<>();
            responseTable.add(Arrays.asList("NAME", "TYPE", "DEFAULT", "DESCRIPTION"));
            responseTable.add(Arrays.asList("----", "----", "----", "----"));
            cells.forEach(parameter -> responseTable.add(parameter.toList()));
            builder.table(responseTable);
        }

    }

    public static void main(String[] args) {
        String value = "aaaaaaaaaaaaaaaa" + newLineFlag;
        System.out.println(value.substring(0, value.length() - newLineFlag.length()));
    }

}
