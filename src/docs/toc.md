# Table of Contents

The complete Wurbelizer documentation, in suggested reading order. Each entry
links to the document and its main sections. See also the [Index](index.md) for
an alphabetical list of terms.

[← Back to README](../../README.md)

---

### 0. [Quickstart](quickstart.md)

A 5-minute tutorial that builds a small DTO-generating wurblet and weaves it into
a source file.

- [Prerequisites](quickstart.md#prerequisites)
- [1. The first wurblet](quickstart.md#1-the-first-wurblet)
  - [The model](quickstart.md#the-model)
  - [The wurblet base class](quickstart.md#the-wurblet-base-class)
  - [The template](quickstart.md#the-template)
- [2. Generate code](quickstart.md#2-generate-code)
  - [The source file](quickstart.md#the-source-file)
  - [Run it](quickstart.md#run-it)
- [Further reading](quickstart.md#further-reading)

### 1. [Code Levels](code-levels.md)

The three code levels — wurblet, output and source — that distinguish the
Wurbelizer from two-level generators.

### 2. [Build Tool Integration](integration.md)

How to run the wurbiler and wurbler from common build tools.

- [Maven Integration](maven.md)
- [Ant Integration](ant.md)
  - [Wurbile](ant.md#wurbile)
  - [Wurbel](ant.md#wurbel)
- [Console Integration](console.md)

### 3. [Wurblet Level Syntax](wurblet-syntax.md)

The `@`-based extensions used inside `.wrbl` files.

- [Level Switching](wurblet-syntax.md#level-switching)
  - [Code Switching](wurblet-syntax.md#code-switching)
  - [Value Extraction](wurblet-syntax.md#value-extraction)
- [Wurbiler Directives](wurblet-syntax.md#wurbiler-directives)
  - [include](wurblet-syntax.md#include) ·
    [args](wurblet-syntax.md#args) ·
    [to](wurblet-syntax.md#to) ·
    [package](wurblet-syntax.md#package) ·
    [import](wurblet-syntax.md#import) ·
    [extends](wurblet-syntax.md#extends) ·
    [implements](wurblet-syntax.md#implements) ·
    [indent](wurblet-syntax.md#indent) ·
    [phase](wurblet-syntax.md#phase) ·
    [comment](wurblet-syntax.md#comment) ·
    [code](wurblet-syntax.md#code)

### 4. [Source Level Syntax](source-syntax.md)

The constructs the wurbler reads from application source comments.

- [Wurblet Anchor](source-syntax.md#wurblet-anchor)
  - [Unnamed Anchor](source-syntax.md#unnamed-anchor)
  - [Conditional Anchor](source-syntax.md#conditional-anchor)
  - [Extra Indentation](source-syntax.md#extra-indentation)
  - [Editor Fold](source-syntax.md#editor-fold)
- [Here Documents](source-syntax.md#here-documents)
- [Variables](source-syntax.md#variables)

### 5. [Guard Types](guard-types.md)

The pluggable patterns (`netbeans`, `coffeebytes`, `visualstudio`, custom) that
delimit guarded blocks.

### 6. [Writing Wurblets](writing-wurblets.md)

Design guidance and the Wurbelizer API for building your own wurblets.

- [Wurbelizer API](writing-wurblets.md#wurbelizer-api)
  - [Wurbler Properties](writing-wurblets.md#wurbler-properties)
- [Skeleton extending AbstractJavaWurblet](writing-wurblets.md#skeleton-extending-abstractjavawurblet)
- [Errorhandling](writing-wurblets.md#errorhandling)
- [Logging](writing-wurblets.md#logging)
- [Examples](writing-wurblets.md#examples)

---

**Navigation:** [Table of Contents](toc.md) | [Index](index.md) | [Start reading →](quickstart.md)
