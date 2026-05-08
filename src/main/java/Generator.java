import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes; // Ini Opcodes milik DexLib2
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.objectweb.asm.ClassWriter;
// Hapus baris import Opcodes ASM yang bermasalah tadi

import java.io.File;
import java.io.FileOutputStream;

public class Generator {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Gunakan: java Generator <input_jar> <output_dir>");
            return;
        }

        File inputFile = new File(args[0]);
        File outputDir = new File(args[1]);
        outputDir.mkdirs();

        // Menggunakan Opcodes.getDefault() dari DexLib2
        DexFile dexFile = DexFileFactory.loadDexFile(inputFile, Opcodes.getDefault());

        for (ClassDef classDef : dexFile.getClasses()) {
            String className = classDef.getType();
            
            if (className.startsWith("Landroid/") || className.startsWith("Lcom/android/")) {
                generateMirror(classDef, outputDir);
            }
        }
    }

    private static void generateMirror(ClassDef classDef, File outputDir) throws Exception {
        String originalName = classDef.getType().substring(1, classDef.getType().length() - 1);
        String simpleName = originalName.substring(originalName.lastIndexOf('/') + 1);
        String newName = "black/" + originalName.replace(simpleName, "BR" + simpleName);

        ClassWriter cw = new ClassWriter(0);
        
        // Di sini kita panggil org.objectweb.asm.Opcodes secara langsung tanpa import alias
        cw.visit(org.objectweb.asm.Opcodes.V1_8, 
                 org.objectweb.asm.Opcodes.ACC_PUBLIC, 
                 newName, null, "java/lang/Object", null);

        cw.visitField(org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_STATIC, 
                      "TYPE", "Ljava/lang/Class;", null, null).visitEnd();

        cw.visitEnd();

        File outFile = new File(outputDir, newName + ".class");
        outFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(cw.toByteArray());
        }
    }
}
