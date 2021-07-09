package vulkanguide;

public class Main {

    public static void main(String[] args) {
        final VulkanEngine engine = new VulkanEngine();

        engine.init();

        engine.run();

        engine.cleanup();
    }
}
