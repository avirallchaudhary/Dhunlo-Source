import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class GenerateBrandingAssets {
    private static final Color NAVY_BG = new Color(0x01, 0x02, 0x14);

    public static void main(String[] args) throws Exception {
        File rootDir = new File("C:/Users/acer/Downloads/Dhunlo-Workspace");
        File masterFile = new File(rootDir, "branding/dhunlo-logo-master.png");
        if (!masterFile.exists()) {
            throw new RuntimeException("Master logo file does not exist: " + masterFile.getAbsolutePath());
        }

        BufferedImage master = ImageIO.read(masterFile);
        System.out.println("Loaded master logo: " + master.getWidth() + "x" + master.getHeight());

        // 1. Generate 512x512 logo.png (with safe circular margin: master drawn at 410x410 centered in 512x512)
        BufferedImage logo512 = createPaddedLogo(master, 512, 410);

        // Save to shared composeResources
        File sharedLogo = new File(rootDir, "shared/src/commonMain/composeResources/drawable/logo.png");
        savePng(logo512, sharedLogo);
        System.out.println("Saved: " + sharedLogo.getAbsolutePath());

        // Save to desktopApp resources
        File desktopLogo = new File(rootDir, "desktopApp/src/desktopMain/resources/logo.png");
        savePng(logo512, desktopLogo);
        System.out.println("Saved: " + desktopLogo.getAbsolutePath());

        File desktopLogoDark = new File(rootDir, "desktopApp/src/desktopMain/resources/logo_dark.png");
        savePng(logo512, desktopLogoDark);
        System.out.println("Saved: " + desktopLogoDark.getAbsolutePath());

        // Save to assets and fastlane
        File playstoreLogo = new File(rootDir, "assets/ic_launcher-playstore.png");
        savePng(logo512, playstoreLogo);
        System.out.println("Saved: " + playstoreLogo.getAbsolutePath());

        File fastlaneLogo = new File(rootDir, "fastlane/metadata/android/en-US/images/icon.png");
        savePng(logo512, fastlaneLogo);
        System.out.println("Saved: " + fastlaneLogo.getAbsolutePath());

        // 2. Generate multi-size desktopApp logo.ico (16, 32, 48, 64, 128, 256)
        File desktopIco = new File(rootDir, "desktopApp/src/desktopMain/resources/logo.ico");
        int[] icoSizes = {256, 128, 64, 48, 32, 16};
        writeIco(logo512, icoSizes, desktopIco);
        System.out.println("Saved: " + desktopIco.getAbsolutePath());

        // 3. Generate desktopApp logo.icns
        File desktopIcns = new File(rootDir, "desktopApp/src/desktopMain/resources/logo.icns");
        writeIcns(master, desktopIcns);
        System.out.println("Saved: " + desktopIcns.getAbsolutePath());

        // 4. Generate Android TV Banner 320x180
        BufferedImage banner = new BufferedImage(320, 180, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gBanner = banner.createGraphics();
        setHighQuality(gBanner);
        gBanner.setColor(NAVY_BG);
        gBanner.fillRect(0, 0, 320, 180);
        // Draw master logo at 144x144 centered
        int bannerLogoSize = 144;
        int bannerX = (320 - bannerLogoSize) / 2;
        int bannerY = (180 - bannerLogoSize) / 2;
        gBanner.drawImage(master, bannerX, bannerY, bannerLogoSize, bannerLogoSize, null);
        gBanner.dispose();

        File tvBanner = new File(rootDir, "androidApp/src/main/res/mipmap-xhdpi/tv_banner.png");
        savePng(banner, tvBanner);
        System.out.println("Saved: " + tvBanner.getAbsolutePath());

        // 5. Generate Android Adaptive Foreground variants (symbol safely inside 66dp-72dp mask)
        // Master scaled by 240/1254 = 0.1914 on 432x432 canvas
        int[] adaptiveCanvasSizes = {108, 162, 216, 324, 432};
        String[] densityNames = {"mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};

        for (int i = 0; i < adaptiveCanvasSizes.length; i++) {
            int canvas = adaptiveCanvasSizes[i];
            int masterDrawSize = (int) Math.round(canvas * (240.0 / 432.0));
            BufferedImage fg = createPaddedLogo(master, canvas, masterDrawSize);

            File dir = new File(rootDir, "androidApp/src/main/res/mipmap-" + densityNames[i]);
            dir.mkdirs();
            File fgFile = new File(dir, "ic_launcher_foreground.png");
            savePng(fg, fgFile);
            System.out.println("Saved: " + fgFile.getAbsolutePath());
        }

        // Also save nodpi fallback
        File nodpiFg = new File(rootDir, "androidApp/src/main/res/drawable-nodpi/ic_launcher_foreground.png");
        nodpiFg.getParentFile().mkdirs();
        BufferedImage fg432 = createPaddedLogo(master, 432, 240);
        savePng(fg432, nodpiFg);
        System.out.println("Saved: " + nodpiFg.getAbsolutePath());

        // 6. Generate Android Legacy Launcher Icons (ic_launcher.png)
        int[] legacySizes = {48, 72, 96, 144, 192};
        for (int i = 0; i < legacySizes.length; i++) {
            int size = legacySizes[i];
            int masterDrawSize = (int) Math.round(size * (410.0 / 512.0));
            BufferedImage legacy = createPaddedLogo(master, size, masterDrawSize);

            File dir = new File(rootDir, "androidApp/src/main/res/mipmap-" + densityNames[i]);
            dir.mkdirs();
            File legacyFile = new File(dir, "ic_launcher.png");
            savePng(legacy, legacyFile);
            System.out.println("Saved: " + legacyFile.getAbsolutePath());
        }

        System.out.println("All branding assets generated successfully!");
    }

    private static BufferedImage createPaddedLogo(BufferedImage master, int canvasSize, int drawSize) {
        BufferedImage result = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        setHighQuality(g);
        g.setColor(NAVY_BG);
        g.fillRect(0, 0, canvasSize, canvasSize);

        int offset = (canvasSize - drawSize) / 2;
        g.drawImage(master, offset, offset, drawSize, drawSize, null);
        g.dispose();
        return result;
    }

    private static void setHighQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    private static void savePng(BufferedImage img, File target) throws IOException {
        target.getParentFile().mkdirs();
        ImageIO.write(img, "PNG", target);
    }

    private static void writeIco(BufferedImage source, int[] sizes, File outputFile) throws IOException {
        List<byte[]> pngBytesList = new ArrayList<>();
        for (int size : sizes) {
            BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resized.createGraphics();
            setHighQuality(g);
            g.drawImage(source, 0, 0, size, size, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "PNG", baos);
            pngBytesList.add(baos.toByteArray());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        // ICONDIR header
        out.writeShort(Short.reverseBytes((short) 0)); // reserved
        out.writeShort(Short.reverseBytes((short) 1)); // type 1 = ICO
        out.writeShort(Short.reverseBytes((short) sizes.length)); // count

        int offset = 6 + (16 * sizes.length);
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            byte[] data = pngBytesList.get(i);

            out.writeByte(size == 256 ? 0 : size); // width
            out.writeByte(size == 256 ? 0 : size); // height
            out.writeByte(0); // colors
            out.writeByte(0); // reserved
            out.writeShort(Short.reverseBytes((short) 1)); // planes
            out.writeShort(Short.reverseBytes((short) 32)); // bpp
            out.writeInt(Integer.reverseBytes(data.length)); // size in bytes
            out.writeInt(Integer.reverseBytes(offset)); // file offset
            offset += data.length;
        }

        for (byte[] data : pngBytesList) {
            out.write(data);
        }
        out.flush();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(baos.toByteArray());
        }
    }

    private static void writeIcns(BufferedImage master, File outputFile) throws IOException {
        // Build ICNS elements with PNG data
        // Standard chunk types:
        // ic07 (128x128), ic08 (256x256), ic09 (512x512), ic10 (1024x1024),
        // ic11 (16x16@2x/32x32), ic12 (32x32@2x/64x64), ic13 (128x128@2x/256x256), ic14 (256x256@2x/512x512)
        record IcnsEntry(String tag, int size) {}
        List<IcnsEntry> entries = List.of(
            new IcnsEntry("ic11", 32),
            new IcnsEntry("ic12", 64),
            new IcnsEntry("ic07", 128),
            new IcnsEntry("ic08", 256),
            new IcnsEntry("ic13", 256),
            new IcnsEntry("ic09", 512),
            new IcnsEntry("ic14", 512),
            new IcnsEntry("ic10", 1024)
        );

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        for (IcnsEntry entry : entries) {
            int canvas = entry.size;
            int drawSize = (int) Math.round(canvas * (410.0 / 512.0));
            BufferedImage img = createPaddedLogo(master, canvas, drawSize);

            ByteArrayOutputStream pngBaos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", pngBaos);
            byte[] pngData = pngBaos.toByteArray();

            // Write 4-byte ASCII tag
            payload.write(entry.tag.getBytes(StandardCharsets.US_ASCII));
            // Write 4-byte big-endian chunk length (header 8 bytes + data length)
            int chunkLen = 8 + pngData.length;
            payload.write((chunkLen >>> 24) & 0xFF);
            payload.write((chunkLen >>> 16) & 0xFF);
            payload.write((chunkLen >>> 8) & 0xFF);
            payload.write(chunkLen & 0xFF);
            payload.write(pngData);
        }

        byte[] payloadBytes = payload.toByteArray();
        ByteArrayOutputStream icnsFile = new ByteArrayOutputStream();
        // Magic 'icns'
        icnsFile.write("icns".getBytes(StandardCharsets.US_ASCII));
        // Total file length (8 byte header + payload)
        int totalLen = 8 + payloadBytes.length;
        icnsFile.write((totalLen >>> 24) & 0xFF);
        icnsFile.write((totalLen >>> 16) & 0xFF);
        icnsFile.write((totalLen >>> 8) & 0xFF);
        icnsFile.write(totalLen & 0xFF);
        icnsFile.write(payloadBytes);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(icnsFile.toByteArray());
        }
    }
}
