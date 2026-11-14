parser grammar HtmlParser;

options {
    tokenVocab = HtmlLexer;
}

// =====================================
// 1. HTML Document
// =====================================

document
    : DOCTYPE? content* EOF
    ;

// =====================================
// 2. Document Content
// =====================================

content
    : element
    | TEXT
    | COMMENT
    ;

// =====================================
// 3. HTML Elements
// =====================================

element
    : normalElement
    | selfClosingElement
    | voidElement
    ;

// =====================================
// 4. Normal Element
// Example: <p>Hello</p>
// =====================================

normalElement
    : openTag content* closeTag
    ;

// =====================================
// 5. Opening Tag
// Example: <div class="box">
// =====================================

openTag
    : TAG_OPEN tagName attribute* TAG_CLOSE
    ;

// =====================================
// 6. Closing Tag
// Example: </div>
// =====================================

closeTag
    : TAG_OPEN TAG_CLOSE_OPEN tagName TAG_CLOSE
    ;

// =====================================
// 7. Self Closing Element
// Example: <br/>
// =====================================

selfClosingElement
    : TAG_OPEN tagName attribute* SELF_CLOSE
    ;

// =====================================
// 8. Void Element (Elements without closing tag like <input disabled>)
// =====================================

voidElement
    : TAG_OPEN VOID_TAG_NAME attribute* TAG_CLOSE
    ;

// =====================================
// 9. Tag Name Rule (Accepts both regular and void tag names)
// =====================================

tagName
    : NAME
    | VOID_TAG_NAME
    ;

// =====================================
// 10. HTML Attributes
// =====================================

attribute
    : NAME (EQUALS attributeValue)?
    ;

// =====================================
// 11. Attribute Values
// =====================================

attributeValue
    : DOUBLE_QUOTED_STRING
    | SINGLE_QUOTED_STRING
    | UNQUOTED_VALUE
    ;
