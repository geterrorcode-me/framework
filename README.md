# 🛡️ Android Mirror Generator (Framework & Services)

![Build Status](https://img.shields.io/badge/Build-GitHub_Actions-blue?style=for-the-badge&logo=github)
![Chipset](https://img.shields.io/badge/Powered_By-Dimensity_8300_Ultra-orange?style=for-the-badge)
![Library](https://img.shields.io/badge/Core-ASM_|_Javassist-red?style=for-the-badge)


**Mirror Generator** adalah alat otomatisasi tingkat tinggi untuk mengekstraksi dan membangun kelas "Mirror" dari `framework.jar` dan `services.jar` Android. Proyek ini memfasilitasi akses ke `@hide` API dan internal sistem Android dengan mengubah struktur paket menjadi prefix `black.android.*`.

---

## 🚀 Fitur Utama

* **Automatic Dex Parsing**: Menggunakan `DexLib2` untuk membedah file `.dex` langsung dari framework sistem.
* **Bytecode Remapping**: Mengonversi `android.*` menjadi `black.android.*` secara otomatis menggunakan **ASM** (Low-Level Manipulation).
* **Reflection Bridge**: Menghasilkan field statis untuk setiap method internal agar bisa diakses secara instan via reflection.
* **Zero-Cost Runtime**: Menghasilkan kelas mirror yang ringan tanpa membebani memori saat dieksekusi.
* **CI/CD Ready**: Terintegrasi penuh dengan GitHub Actions untuk build otomatis skala besar.

---

## 🛠️ Arsitektur Output

Generator ini akan mentransformasi struktur internal kelas Android seperti contoh berikut:

| Original Class | Generated Mirror Class |
| :--- | :--- |
| `android.app.ActivityThread` | `black.android.app.BRActivityThread` |
| `android.os.ServiceManager` | `black.android.os.BRServiceManager` |
| `android.content.pm.PackageParser` | `black.android.content.pm.BRPackageParser` |

### Contoh Struktur Hasil:
```java
package black.android.app;

public class BRActivityThread {
    public static final Class<?> TYPE;
    public static Method currentActivityThread;
    public static Method getProcessName;
    
    static {
        // Otomatis terisi melalui logic static initializer generator
    }
}```

​---

​<p align="center">
Developed with ❤️ for the Android Community
</p>
