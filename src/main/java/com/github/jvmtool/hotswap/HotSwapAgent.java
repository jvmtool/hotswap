package com.github.jvmtool.hotswap;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class HotSwapAgent {

    public static void premain(String args, Instrumentation inst) {
        agentmain(args, inst);
    }

    public static void agentmain(String args,
                                 Instrumentation inst) {

        System.out.println("hotswap start: " + args);
        String[] cs = args.split(",");

        HotSwapTransformer dt = new HotSwapTransformer(cs[1]);
        inst.addTransformer(dt, true);
        try {
            for (Class clazz : inst.getAllLoadedClasses()) {
                if (clazz.getName().equals(cs[0])) {
                    inst.retransformClasses(clazz);
                    System.out.println("hotswap transform done .....");
                    break;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            inst.removeTransformer(dt);
        }
    }

    private static class HotSwapTransformer implements ClassFileTransformer {

        private String classFilePath;

        private HotSwapTransformer(String classFilePath) {
            this.classFilePath = classFilePath;
        }

        @Override
        public byte[] transform(ClassLoader loader,
                                String className,
                                Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain,
                                byte[] classfileBuffer) throws IllegalClassFormatException {

            try {
                System.out.println("hotswap transform class ....." + className);
                FileInputStream fis = new FileInputStream(new File(classFilePath));
                byte[] buf = new byte[1024];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                int length = fis.read(buf);
                while (length > -1) {
                    bos.write(buf, 0, length);
                    length = fis.read(buf);
                }
                return bos.toByteArray();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return classfileBuffer;
        }
    }
}
