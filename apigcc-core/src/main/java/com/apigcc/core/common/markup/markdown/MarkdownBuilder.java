package com.apigcc.core.common.markup.markdown;

import com.apigcc.core.common.Assert;
import com.apigcc.core.common.markup.MarkupBuilder;
import com.google.common.base.Strings;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;

import static com.apigcc.core.common.markup.markdown.Markdown.*;

public class MarkdownBuilder implements MarkupBuilder {

    public static final int MAX_TITLE = 6;

    private StringBuilder content = new StringBuilder();

    @Override
    public MarkupBuilder header(String text, CharSequence... attrs) {
        Assert.notBlank(text, "header must not be blank");
        content.append(HEADER);
        content.append(nobr(text.trim()));
        br();
        return this;
    }

    @Override
    public MarkupBuilder title(int level, String text) {
        Assert.notBlank(text, "header must not be blank");
        Assert.between(level, 1, MAX_TITLE, "title level can not be " + level);
        br();
        content.append(Strings.repeat(TITLE.toString(), level + 1)).append(WHITESPACE)
                .append(nobr(text.trim()));
        br();
        return this;
    }

    @Override
    public MarkupBuilder text(String text) {
        if (Assert.isBlank(text)) {
            return this;
        }
        content.append(text.trim());
        return this;
    }

    @Override
    public MarkupBuilder textLine(String text) {
        if (Assert.isBlank(text)) {
            return this;
        }
        text(nobr(text));
        br();
        return this;
    }

    @Override
    public MarkupBuilder paragraph(String text, CharSequence... attrs) {
        if (Assert.isBlank(text)) {
            return this;
        }
        text(text);
        newLine();
        return this;
    }

    @Override
    public MarkupBuilder note(String text) {
        content.append(QUOTE);
        paragraph(text);
        return this;
    }

    @Override
    public MarkupBuilder tip(String text) {
        content.append(QUOTE);
        paragraph(text);
        return this;
    }

    @Override
    public MarkupBuilder important(String text) {
        content.append(QUOTE);
        paragraph(text);
        return this;
    }

    @Override
    public MarkupBuilder warning(String text) {
        content.append(QUOTE);
        paragraph(text);
        return this;
    }

    @Override
    public MarkupBuilder caution(String text) {
        content.append(QUOTE);
        paragraph(text);
        return this;
    }

    @Override
    public MarkupBuilder block(Consumer<MarkupBuilder> consumer, CharSequence flag, CharSequence... attrs) {
        content.append(flag);
        br();
        consumer.accept(this);
        br();
        content.append(flag);
        newLine();
        return this;

    }

    @Override
    public MarkupBuilder listing(Consumer<MarkupBuilder> consumer, CharSequence... attrs) {
        return block(consumer, LISTING, attrs);
    }

    @Override
    public MarkupBuilder literal(Consumer<MarkupBuilder> consumer, CharSequence... attrs) {
        return block(consumer, LISTING, attrs);
    }

    @Override
    public MarkupBuilder sidebar(Consumer<MarkupBuilder> consumer, CharSequence... attrs) {
        return block(consumer, LISTING, attrs);
    }

    @Override
    public MarkupBuilder comment(Consumer<MarkupBuilder> consumer, CharSequence... attrs) {
        return block(consumer, LISTING, attrs);
    }

    @Override
    public MarkupBuilder passthrough(Consumer<MarkupBuilder> consumer, CharSequence... attrs) {
        return block(consumer, LISTING, attrs);
    }

    @Override
    public MarkupBuilder quote(Consumer<MarkupBuilder> consumer, CharSequence... attrs) {
        return block(consumer, LISTING, attrs);
    }

    @Override
    public MarkupBuilder example(Consumer<MarkupBuilder> consumer, CharSequence... attrs) {
        return block(consumer, LISTING, attrs);
    }

    @Override
    public MarkupBuilder list(String text) {
        return list(text, LIST_FLAG);
    }

    @Override
    public MarkupBuilder list(String text, CharSequence flag) {
        if (!Assert.isBlank(text)) {
            content.append(flag).append(nobr(text));
        }
        return this;
    }

    @Override
    public MarkupBuilder url(String text, String url) {
        if (!Assert.isBlank(text) && !Assert.isBlank(url)) {
            content.append("[").append(nobr(text)).append("](").append(url).append(")");
            br();
        }
        return this;
    }

    @Override
    public MarkupBuilder image(String text, String url) {
        if (!Assert.isBlank(text) && !Assert.isBlank(url)) {
            text("!");
            url(text, url);
        }
        return this;
    }

    @Override
    public MarkupBuilder table(List<List<String>> data) {
        return table(data, true, false);
    }

    @Override
    public MarkupBuilder table(List<List<String>> data, boolean header, boolean footer) {

        for (int i = 0; i < data.size(); i++) {
            content.append(TABLE_CELL);
            for (int j = 0; j < data.get(i).size(); j++) {
                String value = data.get(i).get(j);
                if(value!=null){
                    content.append(data.get(i).get(j).replace(TABLE_CELL, "\\" + TABLE_CELL));
                }else{
                    content.append(" ");
                }
                content.append(TABLE_CELL);
            }
            br();
            if(i==0 && header){
                content.append(TABLE_CELL);
                for (int j = 0; j < data.get(i).size(); j++) {
                    content.append(TABLE_Header);
                }
                br();
            }
            if(i==data.size()-2 && footer){
                content.append(TABLE_CELL);
                for (int j = 0; j < data.get(i).size(); j++) {
                    content.append(TABLE_Header);
                }
                br();
            }
        }
        newLine();
        return this;
    }


    public MarkupBuilder style(CharSequence flag, String text, CharSequence... textStyle) {
        if (Assert.isBlank(text)) {
            return this;
        }
        content.append(flag);
        text(text);
        content.append(flag);
        return this;

    }

    @Override
    public MarkupBuilder emphasized(String text, CharSequence... textStyle) {
        return style(EMPHASIZED, text, textStyle);
    }

    @Override
    public MarkupBuilder strong(String text, CharSequence... textStyle) {
        return style(STRONG, text, textStyle);
    }

    @Override
    public MarkupBuilder monospaced(String text, CharSequence... textStyle) {
        return style(MONOSPACED, text, textStyle);
    }

    @Override
    public MarkupBuilder quoted(String text, CharSequence... textStyle) {
        return style(QUOTE, text, textStyle);
    }

    @Override
    public MarkupBuilder doubleQuoted(String text, CharSequence... textStyle) {
        return style(DOUBLE_QUOTED, text, textStyle);
    }

    @Override
    public MarkupBuilder unquoted(String text, CharSequence... textStyle) {
        return style(UNQUOTED, text, textStyle);
    }

    @Override
    public MarkupBuilder br() {
        content.append(BR);
        return this;
    }

    @Override
    public MarkupBuilder hbr() {
        content.append(HBR);
        return this;
    }

    @Override
    public MarkupBuilder newLine() {
        content.append(NEW_LINE);
        return this;
    }

    @Override
    public MarkupBuilder pageBreak() {
        content.append(PAGEBREAKS);
        br();
        return this;
    }

    @Override
    public String getContent() {
        return content.toString();
    }

    @Override
    public void clean() {
        content = new StringBuilder();
    }

    String nobr(String content) {
        if (Assert.isBlank(content)) {
            return content;
        }
        return content.replaceAll(BR.toString(),
                Matcher.quoteReplacement(WHITESPACE.toString()));
    }

}
