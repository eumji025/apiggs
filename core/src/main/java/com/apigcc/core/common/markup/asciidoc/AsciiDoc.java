package com.apigcc.core.common.markup.asciidoc;

public enum AsciiDoc implements CharSequence {
    EXTENSION(".adoc"),
    /**
     * 各种关键字
     */
    HEADER("= "),
    TABLE("|==="),
    TABLE_CELL("|"),
    TITLE("="),
    EMPHASIZED("_"),
    STRONG("*"),
    MONOSPACED("+"),
    QUOTED("`"),
    DOUBLE_QUOTED("``"),
    UNQUOTED("#"),
    LIST_FLAG("1. "),
    LIST_FLAG_LETTER("a. "),
    LIST_FLAG_LETTER_UPPER("A. "),
    LISTING("----"),
    LITERAL("...."),
    SIDEBAR("****"),
    COMMENT("////"),
    PASSTHROUGH("++++"),
    QUOTE("____"),
    EXAMPLE("===="),
    NOTE("NOTE"),
    TIP("TIP"),
    IMPORTANT("IMPORTANT"),
    WARNING("WARNING"),
    CAUTION("CAUTION"),
    PAGEBREAKS("<<<"),
    HARDBREAKS("[%hardbreaks]"),
    WHITESPACE(" "),
    BR("\r\n"),
    NEW_LINE("\r\n\r\n"),
    HBR(" +"),
    /**
     * 文档属性
     */
    TOC(":toc:"),
    LEFT("left"),
    TOC_LEVEL(":toclevels:"),
    TOC_TITLE(":toc-title:"),
    DOCTYPE(":doctype:"),
    BOOK("book"),
    SOURCE_HIGHLIGHTER(":source-highlighter:"),
    PRETTIFY("prettify"),
    HIGHLIGHTJS("highlightjs"),
    CODERAY("coderay"),
    /**
     * 文字样式
     */
    STYLE_BIG("big"),
    STYLE_SMALL("small"),
    STYLE_UNDERLINE("underline"),
    STYLE_OVERLINE("overline"),
    STYLE_LINE_THROUGH("line-through"),
    ;

    private final String markup;

    AsciiDoc(final String markup) {
        this.markup = markup;
    }

    @Override
    public int length() {
        return markup.length();
    }

    @Override
    public char charAt(int index) {
        return markup.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return markup.subSequence(start, end);
    }

    @Override
    public String toString() {
        return markup;
    }

    public static CharSequence attr(AsciiDoc key, Object value){
        return key.toString() + " " + String.valueOf(value);
    }

}
