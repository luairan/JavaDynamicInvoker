# JavaDynamicInvoker
动态生成 JAVA 代码 并执行的例子  src/sina/Diaties.java

已测试 无内存泄漏 

Java execute string code


https://www.91r.net/ask/14617552.html


compiler.getTask(null, fileManager, null, Arrays.asList("-XDuseUnsharedTable"), null, units)



I am trying to add a javaeditor to my program to extend the program at run time. It all works fine, except when using the program extensively (I simulated 1000-10000 compiler executions). The memory usage rises and rises, it looks like there is a memory leak.

In my program, the class gets loaded, the constructor gets executed and the class gets unloaded (no remaining instance and the classLoader becomes invalid as I set the pointer to null). I analyzed the process with JConsole, the classes get unloaded when the garbage collector is executed.

I did a heapdum opened it in memory analyzer, the problem seems to be inside of java.net.FactoryURLClassLoader (in a com.sun.tools.javac.util.List Object). Since (com.sun.tools.javac) is part of the JDK and not in the JRE and the SystemToolClassLoader is an FactoryURLClassLoader Object, I would locate the leak somewhere there. The number of loaded classes in the SystemToolClassLoader rises from 1 to 521 when I execute the compiler the first time but stays the same afterwards.

So I have no idea where the leak is , is there a way to reset the SystemToolClassLoader? How could I locate the leak more precisely.

EDIT: Okay I found out it also occurs in a very very simple example. So it seems to be a part of the compilation, i don't need to load the class or instantiate it:

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;


public class Example {   

public static void main(String[] args)
{
    for (int i =0; i<10000;i++){
        try {
            System.out.println(i);
            compile();
        } catch (InstantiationException | IllegalAccessException
                | ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

public static void compile() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException
{
    File source = new File( "src\\Example.java" ); // This File
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager( null, null, null );
    Iterable<? extends JavaFileObject> units;
    units = fileManager.getJavaFileObjectsFromFiles( Arrays.asList( source ) );
    compiler.getTask( null, fileManager, null, null, null, units ).call();
    fileManager.close();
}

}
At first, I thought this was a definite memory leak; however, it is directly related to how SoftReferences work.

Oracle's JVM will only attempt to collect soft references when the heap is completely used up. It appears that it is not possible to force soft references to be collected programatically.

To identify the problem, I used three dumps on an 'unlimited' heap:

Start up the app and get the Compiler and ScriptFileManager, then do a GC-finalize-GC. Make a dump.
Load 500 'scripts'.
Do a GC-finalize-GC. Write stats. Make a dump.
Load 500 'scripts'.
Do a GC-finalize-GC. Write stats. Make a dump.
The obvious (double) instance count increase: Names (500 -> 1k), SharedNameTable (500->1k), SharedNameTable$NameImpl (in the hundreds of thousands) and [LSharedNameTable$NameImpl (500->1k).

After analyzing with EMA, it becomes apparent that SharedNameTable has a static reference to a com.sun.tools.javac.util.List which apparently SoftReferences every single SharedNameTable ever created (so one for each source file you have compiled during runtime). All the $NameImpls are the tokens your source file(s) were split to. And apparently all the tokens are never freed from the heap and accumulate to no end... Or do they?

I decided to test if this was actually the case. Knowing the difference of soft vs weak references, I decided to use a small heap (-Xms32m -Xmx32m). This way JVM is forced to either free SharedNameTables or fail with OutOfMemoryError. The results speak for themselves:

-Xmx512m -Xms512m

Total memory: 477233152
Free memory: 331507232
Used memory: 138.97506713867188 MB
Loaded scripts: 500

Total memory: 489816064
Free memory: 203307408
Used memory: 273.23594665527344 MB
Loaded scripts: 1000

The classloader/component "java.net.FactoryURLClassLoader @ 0x8a8a748" occupies 279.709.192 (98,37%) bytes.
-Xmx32m -Xms32m

Total memory: 29687808
Free memory: 25017112
Used memory: 4.454322814941406 MB
Loaded scripts: 500

Total memory: 29884416
Free memory: 24702728
Used memory: 4.941642761230469 MB
Loaded scripts: 1000

One instance of "com.sun.tools.javac.file.ZipFileIndex" loaded by "java.net.FactoryURLClassLoader @ 0x8aa4cc8" occupies 2.230.736 (47,16%) bytes. The instance is referenced by *.*.script.ScriptFileManager @ 0x8ac8230.
(This is merely a link to a JDK library.)

Script:

public class Avenger
{
    public Avenger()
    {
        JavaClassScriptCache.doNotCollect(this);
    }

    public static void main(String[] args)
    {
        // this method is called after compiling
        new Avenger();
    }
}
doNotCollect:

private static final int TO_LOAD = 1000;
private static final List<Object> _active = new ArrayList<Object>(TO_LOAD);

public static void doNotCollect(Object o)
{
    _active.add(o);
}

System.out.println("Loaded scripts: " + _active.size());
2017年08月21日03分52秒

The class definition gets unloaded when I set the classloader to null. and garbage collect. JConsole also tells me that those classes are unloaded. Total classes loaded gets back to the initial value.
That's pretty convincing evidence that this is not a classical classloader leak.

Also eclipse memory analyzer thinks it is a com.sun.tools.javac.util.List Object which takes the memory.... so it is on the heap
The next step should be to identify where the reference (or references) to that List object are. With a bit of luck, you can then look at the source code to find what the list object is used for, and whether there is some way to cause it to be cleared.

2017年08月21日03分52秒

It's not a memory leak, its like "since memory is available and there is something useful to keep, until it is really necessary to get rid of and release memory, I'll keep the compiled source for you". --Compiler says

Basically compiler tool ( the internal compiler tool being used here) keeps references to the source that was compiled, it is though, kept as soft references. Which means that garbage collector will claim the memory retained by it if the JVM will tend to go our of memory. Try to run your code with minimal heap size, you'll see the references being cleaned up.

2017年08月21日03分52秒

Java 7 introduced this bug: in an attempt to speed up compilation, they introduced the SharedNameTable, which uses soft references to avoid reallocations, but unfortunately just causes the JVM to bloat out of control, as those soft references will never be reclaimed until the JVM hits its -Xmx memory limit. Allegedly it will be fixed in Java 9. In the meantime, there's an (undocumented) compiler option to disable it: -XDuseUnsharedTable.

2017年08月21日03分52秒

As other answers have already pointed out, the problem is that the compiler keeps SoftReferences to SharedNameTable around.

Chrispy mentioned the -XDuseUnsharedTable javac option. So the last missing bit is how to enable this option when using the Java API:

compiler.getTask(null, fileManager, null, Arrays.asList("-XDuseUnsharedTable"), null, units)
2017年08月21日03分52秒