# Code Levels

While typical code generators work with code at two levels (generator level
and generated code level), the Wurbelizer operates at three levels:

1. **wurblet level**: holds the code for the generator itself
2. **output level**: the generated code
3. **source level**: the application's source code.

The source code of the wurblet itself contains a mix of the first two levels.
The third level is parsed and modified by the wurbler. 
The wurbler analyzes the source files and locates comment-, Java- and generated code sections. 
The comments, which are ignored by the Java compiler, are parsed
for wurblet anchors, variables and so-called here-documents. 
Then the wurblets are applied and the generated code inserted or replaced within
the guarded blocks of the source file.
The wurbler repeats these steps until there is no more change (generated code could
contain new wurblet anchors!) and finally writes back the
source file to the filesystem (if there was any change at all).
