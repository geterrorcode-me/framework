import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.objectweb.asm.ClassWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Generator {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Gunakan: java Generator <input_jar> <output_dir>");
            return;
        }

        File inputFile = new File(args[0]);
        File outputDir = new File(args[1]);
        outputDir.mkdirs();

        System.out.println("📦 Menganalisis file: " + inputFile.getName());

        try (ZipFile zipFile = new ZipFile(inputFile)) {
            boolean hasDex = false;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            
            // Cek apakah ini file Android (ada .dex) atau Java biasa (ada .class)
            while (entries.hasMoreElements()) {
                if (entries.nextElement().getName().endsWith(".dex")) {
                    hasDex = true;
                    break;
                }
            }

            if (hasDex) {
                System.out.println("🤖 Mode: Android DEX detected.");
                processAsDex(inputFile, outputDir);
            } else {
                System.out.println("☕ Mode: Java Class detected.");
                processAsClass(inputFile, outputDir);
            }
        }
        System.out.println("✨ Selesai! Cek folder: " + outputDir.getName());
    }

    private static void processAsDex(File file, File outputDir) throws Exception {
        MultiDexContainer<?> container = DexFileFactory.loadDexContainer(file, Opcodes.getDefault());
        for (String entryName : container.getDexEntryNames()) {
            DexFile dexFile = container.getEntry(entryName).getDexFile();
            for (ClassDef classDef : dexFile.getClasses()) {
                String className = classDef.getType();
                if (className.startsWith("Landroid/") || className.startsWith("Lcom/android/")) {
                    // Hilangkan L di depan dan ; di belakang (Landroid/app/Activity; -> android/app/Activity)
                    generateMirror(className.substring(1, className.length() - 1), outputDir);
                }
            }
        }
    }

    private static void processAsClass(File file, File outputDir) throws Exception {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.endsWith(".class")) {
                    String className = name.replace(".class", "");
                    if (className.startsWith("android/") || className.startsWith("com/android/")) {
                        generateMirror(className, outputDir);
                    }
                }
            }
        }
    }

    private static void generateMirror(String internalName, File outputDir) throws Exception {
        // android/app/Activity -> black/android/app/BRActivity
        String simpleName = internalName.substring(internalName.lastIndexOf('/') + 1);
        String newName = "black/" + internalName.replace(simpleName, "BR" + simpleName);

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
