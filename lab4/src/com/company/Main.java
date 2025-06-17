import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.image.*;
import javax.imageio.ImageIO;

public class Main {
    static volatile boolean cancelled = false;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java Main <path> [/sub] /s <scale>|/r|/c <copyPath>");
            return;
        }

        String rootPath = args[0];
        boolean recursive = false;
        int index = 1;

        if (args[1].equalsIgnoreCase("/sub")) {
            recursive = true;
            index++;
        }

        if (index >= args.length) {
            System.out.println("Missing action flag");
            return;
        }

        String action = args[index++];
        double scale = 0;
        String copyPath = null;

        switch (action.toLowerCase()) {
            case "/s":
                if (index >= args.length) {
                    System.out.println("Missing scale factor");
                    return;
                }
                try {
                    scale = Double.parseDouble(args[index++]);
                    if (scale <= 0) {
                        System.out.println("Scale factor must be positive");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid scale factor");
                    return;
                }
                break;
            case "/c":
                if (index >= args.length) {
                    System.out.println("Missing copy target path");
                    return;
                }
                copyPath = args[index++];
                File cp = new File(copyPath);
                if (!cp.exists() || !cp.isDirectory()) {
                    System.out.println("Copy target path must exist and be a directory");
                    return;
                }
                break;
            case "/r":
                // no extra param
                break;
            default:
                System.out.println("Unknown action flag");
                return;
        }

        Path root = Paths.get(rootPath);
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            System.out.println("Invalid root path");
            return;
        }

        System.out.println("Press ESC to cancel...");

        // Запускаем поток для чтения ESC
        Thread cancelThread = new Thread(() -> {
            try {
                while (true) {
                    int ch = System.in.read();
                    if (ch == 27) { // ESC ASCII
                        cancelled = true;
                        System.out.println("\nCancellation requested.");
                        break;
                    }
                }
            } catch (IOException ignored) {}
        });
        cancelThread.setDaemon(true);
        cancelThread.start();

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            walkAndProcess(root, recursive, action, scale, copyPath, pool);
        } finally {
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.MINUTES);
        }

        if (!cancelled) {
            System.out.println("Processing completed.");
        } else {
            System.out.println("Processing stopped by user.");
        }
    }

    static void walkAndProcess(Path dir, boolean recursive, String action, double scale, String copyPath, ExecutorService pool) throws IOException {
        if (cancelled) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (cancelled) return;

                if (Files.isDirectory(entry)) {
                    if (recursive) {
                        walkAndProcess(entry, true, action, scale, copyPath, pool);
                    }
                } else if (isImageFile(entry)) {
                    pool.submit(() -> processFile(entry, action, scale, copyPath));
                }
            }
        }
    }

    static boolean isImageFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".gif");
    }

    static void processFile(Path file, String action, double scale, String copyPath) {
        if (cancelled) return;

        try {
            switch (action.toLowerCase()) {
                case "/s":
                    stretchImage(file, scale);
                    break;
                case "/r":
                    deleteFile(file);
                    break;
                case "/c":
                    copyFile(file, copyPath);
                    break;
            }
        } catch (Exception e) {
            System.out.println("Error processing " + file + ": " + e.getMessage());
        }
    }

    static void stretchImage(Path file, double scale) throws IOException {
        // Пример: читаем, масштабируем и перезаписываем изображение
        BufferedImage img = ImageIO.read(file.toFile());
        if (img == null) {
            System.out.println("Not an image: " + file);
            return;
        }
        int w = (int)(img.getWidth() * scale);
        int h = (int)(img.getHeight() * scale);
        BufferedImage resized = new BufferedImage(w, h, img.getType());
        resized.getGraphics().drawImage(img, 0, 0, w, h, null);

        ImageIO.write(resized, getExtension(file), file.toFile());
        System.out.println("Stretched " + file);
    }

    static void deleteFile(Path file) throws IOException {
        Files.delete(file);
        System.out.println("Deleted " + file);
    }

    static void copyFile(Path file, String targetDir) throws IOException {
        Path target = Paths.get(targetDir).resolve(file.getFileName());
        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Copied " + file + " to " + target);
    }

    static String getExtension(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot == -1) return "jpg"; // default
        return name.substring(dot + 1).toLowerCase();
    }
}
