package sina;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * Created by luairan on 2017/1/28.
 */
public class Diaties {

    public static void main(String[] args) throws Exception {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        JavaFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));

        for (int i = 0; i < 500000000; i++) {
            //Thread.sleep(500);
            String source = "package sina;\n"
                + "\n"
                + "/**\n"
                + " * Created by luairan on 2017/8/20.\n"
                + " *\n"
                + " * @author luairan\n"
                + " * @date 2017/08/20\n"
                + " */\n"
                + "public class Tessa"+i+" {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"ssssssssssss\");\n"
                + "    }\n"
                + "}\n";
            JavaSourceFromString sourceObject = new JavaSourceFromString("sina.Tessa"+i, source);
            Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(sourceObject);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, Arrays.asList("-XDuseUnsharedTable"), null, fileObjects);
            boolean result = task.call();
            if (result) {
                System.out.println("编译成功。");
                //   ClassLoader loader = Thread.currentThread().getContextClassLoader();
                try {
                    ClassLoader loader = fileManager.getClassLoader(null);
                    Class<?> clazz = loader.loadClass("sina.Tessa"+i);
                    Method method = clazz.getMethod("main", new Class<?>[] {String[].class});
                    Object value = method.invoke(null, new Object[] {null});
                    System.out.println(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
