package org.liuyehcf.lombok.processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;
import org.liuyehcf.lombok.annotation.Builder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("org.liuyehcf.lombok.annotation.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BuilderProcessor extends AbstractProcessor {

    private Messager messager;
    private JavacTrees trees;
    private TreeMaker treeMaker;
    private Names names;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Builder.class);

        if (set.isEmpty()) return false;

//        messager.printMessage(Diagnostic.Kind.WARNING, set.toString());

        set.forEach(element -> {
            // 获取当前元素的JCTree
            JCTree jcTree = trees.getTree(element);

            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {

                    List<JCTree.JCMethodDecl> methodDecls = List.nil();

                    for (JCTree jTree : jcClassDecl.defs) {
                        if (isSetMethod(jTree)) {
                            methodDecls = methodDecls.prepend((JCTree.JCMethodDecl) jTree);
                        }
                    }

                    // messager.printMessage(Diagnostic.Kind.WARNING, createStaticInnerBuilderJCClassDecl(jcClassDecl, methodDecls).toString());

                    jcClassDecl.defs = jcClassDecl.defs.append(createStaticInnerBuilderJCClassDecl(jcClassDecl, methodDecls));

                    messager.printMessage(Diagnostic.Kind.WARNING, jcClassDecl.toString());
                }
            });
        });

        return true;
    }

    private boolean isSetMethod(JCTree jTree) {
        if (jTree.getKind().equals(JCTree.Kind.METHOD)) {
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jTree;
            if (jcMethodDecl.getName().toString().startsWith("set")
                    && jcMethodDecl.getParameters().size() == 1) {
                return true;
            }
        }
        return false;
    }

    private JCTree.JCClassDecl createStaticInnerBuilderJCClassDecl(JCTree.JCClassDecl jcClassDecl, List<JCTree.JCMethodDecl> methodDecls) {

        List<JCTree> jcTreeList = List.nil();

        jcTreeList = List.<JCTree>nil().appendList(cloneSetMethodJCMethodDecl(jcClassDecl, methodDecls));

        return treeMaker.ClassDef(
                treeMaker.Modifiers(Flags.PUBLIC + Flags.STATIC + Flags.FINAL), // 访问标志
                names.fromString(jcClassDecl.getSimpleName() + "Builder"), // 名字
                List.nil(), // 泛型形参列表
                null, // 继承
                List.nil(), // 接口列表
                jcTreeList); // 定义
    }

    private List<JCTree> cloneSetMethodJCMethodDecl(JCTree.JCClassDecl jcClassDecl, List<JCTree.JCMethodDecl> methodDecls) {
        List<JCTree> clonedSetMethods = List.nil();


        for (JCTree.JCMethodDecl jcMethodDecl : methodDecls) {

            ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
            //statements.appendList(jcMethodDecl.getBody().getStatements()); //todo
            statements.append(
                    treeMaker.Return(treeMaker.Ident(names.fromString("this"))));

            JCTree.JCBlock jcBlock = treeMaker.Block(0, statements.toList());

            List<JCTree.JCVariableDecl> params = List.<JCTree.JCVariableDecl>nil().prependList(jcMethodDecl.getParameters());

            clonedSetMethods = clonedSetMethods.prepend(treeMaker.MethodDef(
                    jcMethodDecl.getModifiers(), // 访问标志
                    jcMethodDecl.getName(), // 名字
                    treeMaker.Ident(names.fromString(jcClassDecl.getSimpleName().toString() + "Builder")), //返回类型
                    jcMethodDecl.getTypeParameters(), // 泛型形参列表
                    List.convert(JCTree.JCVariableDecl.class, jcMethodDecl.getTypeParameters()), // 参数列表，非常奇怪的地方，不用List.convert就报错
                    jcMethodDecl.getThrows(), // 异常列表
                    jcBlock, // 方法体
                    null // 默认值
            ));
        }

        return clonedSetMethods;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }
}
