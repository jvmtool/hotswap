package com.github.jvmtool.hotswap;


import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class Main {

    public static void main(String[] args) throws Exception {

        if(args.length > 3){
            System.out.println("usage: \n" +
                    "java -cp <jdk_path>/lib/tools.jar:./hotswap.jar com.github.jvmtool.hotswap.Main <pid> <path_to_class_file>" +
                    "or\n" +
                    "java -cp <jdk_path>/lib/tools.jar:./hotswap.jar com.github.jvmtool.hotswap.Main <pid> <path_to_class_file> <class full name>");
            return;
        }
        //get where am I
        String jarFilePath = HotSwapAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        System.out.println("use agent jar: " + jarFilePath);

        String pid = args[0];
        String classFilePath = args[1];
        String className;
        if(args.length == 3) {
            className = args[2];// ClassFileParser.getClassName(classFilePath);
        }
        else {
            className = ClassFileParser.getClassName(classFilePath);
        }

        System.out.println("use pid: " + pid);
        System.out.println("hotwap class: " + className);
        System.out.println("class file: " + classFilePath);

        // Find the target JVM pid
        VirtualMachineDescriptor targetJvm = null;
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            if (descriptor.id().equals(pid)) {
                targetJvm = descriptor;
                break;
            }
        }

        if (targetJvm != null) {
            try {
                VirtualMachine vm = VirtualMachine.attach(targetJvm);
                vm.loadAgent(jarFilePath, String.format("%s,%s", className, classFilePath));
                vm.detach();
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else {
            System.out.println(String.format("process<%s> is not found", pid));
        }
    }
}
