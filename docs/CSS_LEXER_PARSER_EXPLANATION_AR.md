# شرح تنفيذ CSS Lexer وParser

## 1. ما الذي بنيناه؟

بنينا مسار تحليل CSS من ثلاث خطوات:

1. يقرأ `CssLexer` الأحرف ويحوّلها إلى Tokens، مثل `IDENT` و`NUMBER` و`LBRACE`.
2. يقرأ `CssParser` هذه الـ Tokens ويتحقق من ترتيبها وفق قواعد CSS المدعومة.
3. يشغّل `CssParserDemo` المرحلتين ويجمع الأخطاء ويطبع موقع كل خطأ.

ملفات Java الموجودة في `target/generated-sources/antlr4` مولّدة آليًا بواسطة
ANTLR. مصدر الحقيقة الذي نعدله يدويًا هو ملفات `.g4`، وليس الملفات المولّدة.

## 2. لماذا عدّلنا Lexer؟

أضفنا Tokens للمحددات المركبة، ومنها `>`, `+`, `~`، ولـ attribute selectors،
ومنها `[`, `]`, `=`, `^=`, `$=`, `*=`, `~=`, و`|=`.

استخدمنا Token واحدًا باسم `HASH_VALUE` لكل من `#main` و`#333abc`. الشكل
اللغوي للحالتين واحد، بينما يحدد Parser المعنى من السياق: داخل selector تكون
ID، وداخل value تكون لونًا أو قيمة hash.

أضفنا `IMPORTANT` كـ Token مستقل حتى يقبل Parser `!important` تحديدًا ولا يقبل
أي كلمة عشوائية بعد `!`.

أهم قرار هو عدم استخدام `-> skip` مع `WS` و`COMMENT`. لو حذف Lexer المسافات
فلن يستطيع Parser التفريق بين:

- `div.product`: عنصر `div` يحمل class اسمها `product`.
- `div .product`: عنصر class `product` موجود داخل `div`.

لذلك نحافظ على المسافة كـ Token ويقرر Parser متى تكون مجرد تنسيق ومتى تكون
descendant combinator. أما `CssLexerDemo` فيخفيها عند الطباعة فقط حتى تبقى
قائمة Tokens واضحة.

## 3. ماذا يدعم Parser؟

قاعدة البداية هي `stylesheet`. وهي تشترط تحليل الملف كاملًا حتى `EOF`، لذلك
لا يعتبر الملف صحيحًا إذا حلّل Parser بدايته وترك جزءًا غير مفهوم في النهاية.

يدعم Parser:

- type selectors مثل `p` وuniversal selector وهو `*`.
- class وID مثل `.card` و`#main`.
- selectors مركبة مثل `button.primary:hover`.
- descendant وchild وadjacent/general sibling combinators.
- attribute selectors مثل `input[type="text"]` و`a[href^="https"]`.
- pseudo selectors مثل `:hover` و`::before` و`:not(.disabled)`.
- الخصائص العادية وcustom properties مثل `--main-color`.
- الأرقام والوحدات والنسب والألوان والنصوص والقيم المتعددة.
- الدوال مثل `rgb(...)` و`calc(...)` و`var(...)`.
- `!important`.

صممنا `declarationList` بحيث تكون `;` إلزامية بين تصريحين، لكنها اختيارية بعد
آخر تصريح قبل `}`. لذلك هذا صحيح:

```css
.card {
    color: red;
    width: 20px
}
```

أما وجود تصريحين دون `;` بينهما فينتج syntax error.

النسخة الحالية subset عملي وليست تطبيقًا كاملًا لمواصفة CSS. لا تدعم حاليًا
at-rules مثل `@media` و`@keyframes`، وهذا قرار نطاق وليس خطأ في التنفيذ.

## 4. كيف يعمل CssParserDemo؟

يستقبل البرنامج مسار ملف واحد. يزيل Error Listeners الافتراضية من ANTLR
ويربط Listener خاصًا مع Lexer وآخر مع Parser. بهذه الطريقة نجمع الأخطاء بدل
أن يطبع ANTLR رسائل متفرقة لا يمكن للبرنامج تقييمها.

شكل الخطأ هو:

```text
file.css:2:11: parser error: missing ':' at 'red'
```

معاني exit codes:

- `0`: اكتمل التحليل دون أخطاء.
- `1`: الملف قُرئ، لكنه يحتوي خطأ Lexer أو Parser.
- `2`: استخدام البرنامج غير صحيح أو الملف غير موجود/غير قابل للقراءة.

نفصل أخطاء Lexer عن Parser لأن الرمز غير المعروف، مثل `@` في النطاق الحالي،
يفشل أثناء تكوين Tokens، بينما نسيان `:` أو `}` يفشل عند التحقق من ترتيبها.

## 5. البناء والتشغيل

توليد ملفات ANTLR وترجمة المشروع:

```powershell
mvn clean compile
```

تجهيز مكتبات التشغيل ثم تجربة ملف صحيح:

```powershell
mvn dependency:copy-dependencies "-DoutputDirectory=target/dependency"
java -cp "target/classes;target/dependency/*" CssParserDemo src/test/resources/css/valid/basic.css
$LASTEXITCODE
```

تجربة ملف خاطئ:

```powershell
java -cp "target/classes;target/dependency/*" CssParserDemo src/test/resources/css/invalid/missing-colon.css
$LASTEXITCODE
```

نستخدم Java process مستقلًا بدل الاعتماد على `exec:java` عند فحص exit code؛
لأن `exec:java` يشغّل الكلاس داخل عملية Maven، بينما هنا نريد كود خروج البرنامج
نفسه بصورة مباشرة.

## 6. ملفات التحقق

`valid/basic.css` يثبت القواعد والتصريحات الأساسية. أما
`valid/core-features.css` فيغطي combinators وattributes وpseudo selectors
والمتغيرات والدوال والألوان و`!important`.

مجلد `invalid` يحتوي حالات مستقلة لـ:

- نسيان `:`.
- نسيان `}`.
- selector غير مكتمل.
- function غير مكتملة.
- نسيان `;` بين تصريحين.
- رمز غير مدعوم يسبب Lexer error.

فصل الحالات يجعل سبب الفشل واضحًا، بدل وضع أخطاء كثيرة في ملف واحد.

## 7. أسئلة متوقعة من المشرف

**ما الفرق بين Lexer وParser؟**

Lexer يقسم الأحرف إلى Tokens، بينما Parser يتحقق من أن ترتيب هذه Tokens يطابق
القواعد النحوية. قدرة Lexer على قراءة الكلمات لا تعني أن جملة CSS صحيحة.

**لماذا لم نحذف whitespace؟**

لأن whitespace قد يكون له معنى داخل selector، فهو يمثل descendant combinator.
يحذفه برنامج عرض Tokens بصريًا فقط، لكنه يبقى متاحًا للـ Parser.

**لماذا CssParser.java غير موجود في src/main/java؟**

لأنه ملف مولّد من `CssParser.g4` أثناء Maven build، ويوضع في
`target/generated-sources/antlr4`. لا نرفعه إلى Git ولا نعدله يدويًا.

**كيف نعرف أن Parser حلّل الملف كله؟**

قاعدة `stylesheet` تنتهي بـ `EOF`. إذا بقيت Tokens غير محللة فسيظهر خطأ.

**كيف نميّز الخطأ اللغوي عن النحوي؟**

الرمز الذي لا يستطيع Lexer تحويله إلى Token ينتج `lexer error`. أما Token
المعروف الموجود في ترتيب خاطئ، مثل property دون `:`, فينتج `parser error`.

**هل هذا يدعم CSS كاملًا؟**

لا. يدعم النواة الشائعة المتفق عليها. Media queries وkeyframes وat-rules مرحلة
لاحقة ويمكن إضافتها بقواعد مستقلة دون تغيير فكرة مسار التحليل.
