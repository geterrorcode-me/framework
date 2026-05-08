import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes as ASMOpcodes;
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

        DexFile dexFile = DexFileFactory.loadDexFile(inputFile, Opcodes.getDefault());

        for (ClassDef classDef : dexFile.getClasses()) {
            String className = classDef.getType();
            
            // Filter hanya kelas Android
            if (className.startsWith("Landroid/") || className.startsWith("Lcom/android/")) {
                generateMirror(classDef, outputDir);
            }
        }
    }

    private static void generateMirror(ClassDef classDef, File outputDir) throws Exception {
        String originalName = classDef.getType().substring(1, classDef.getType().length() - 1);
        String simpleName = originalName.substring(originalName.lastIndexOf('/') + 1);
        
        // Ubah android/app/ActivityThread -> black/android/app/BRActivityThread
        String newName = "black/" + originalName.replace(simpleName, "BR" + simpleName);

        ClassWriter cw = new ClassWriter(0);
        cw.visit(ASMOpcodes.V1_8, ASMOpcodes.ACC_PUBLIC, newName, null, "java/lang/Object", null);

        // Tambahkan Field TYPE sebagai referensi class asli
        cw.visitField(ASMOpcodes.ACC_PUBLIC | ASMOpcodes.ACC_STATIC, "TYPE", "Ljava/lang/Class;", null, null).visitEnd();

        cw.visitEnd();

        File outFile = new File(outputDir, newName + ".class");
        outFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(cw.toByteArray());
        }
    }
}
