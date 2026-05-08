import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.objectweb.asm.ClassWriter;
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

        System.out.println("📦 Memproses: " + inputFile.getName());

        // Gunakan raw type atau wildcard yang lebih fleksibel untuk menghindari CAP#1 error
        MultiDexContainer<?> container = DexFileFactory.loadDexContainer(inputFile, Opcodes.getDefault());

        for (String entryName : container.getDexEntryNames()) {
            System.out.println("🔍 Membedah entry: " + entryName);
            DexFile dexFile = container.getEntry(entryName).getDexFile();

            for (ClassDef classDef : dexFile.getClasses()) {
                String className = classDef.getType();
                // Filter framework
                if (className.startsWith("Landroid/") || className.startsWith("Lcom/android/internal/")) {
                    generateMirror(classDef, outputDir);
                }
            }
        }
        System.out.println("✨ Selesai!");
    }

    private static void generateMirror(ClassDef classDef, File outputDir) throws Exception {
        String originalName = classDef.getType().substring(1, classDef.getType().length() - 1);
        String simpleName = originalName.substring(originalName.lastIndexOf('/') + 1);
        String newName = "black/" + originalName.replace(simpleName, "BR" + simpleName);

        ClassWriter cw = new ClassWriter(0);
        cw.visit(org.objectweb.asm.Opcodes.V1_8, org.objectweb.asm.Opcodes.ACC_PUBLIC, newName, null, "java/lang/Object", null);
        cw.visitField(org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_STATIC, "TYPE", "Ljava/lang/Class;", null, null).visitEnd();
        cw.visitEnd();

        File outFile = new File(outputDir, newName + ".class");
        outFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(cw.toByteArray());
        }
    }
}
