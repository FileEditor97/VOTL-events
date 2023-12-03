package votl.events;

import java.util.Optional;

public class App {

    private static App instance;

    public final String VERSION = Optional.ofNullable(App.class.getPackage().getImplementationVersion()).map(ver -> "v"+ver).orElse("DEVELOPMENT");

    public App() {
        System.out.print(VERSION);
    }

    public static void main(String[] args) {
        instance = new App();
    }
}
