package assignments;

abstract class SmartDevice {
    String name;
    SmartDevice(String name) {
        this.name = name;
    }
    abstract void turnOn() throws Exception;
    abstract void turnOff();
    void cleanup() {
        System.out.println("Cleaning up " + name + "...");
    }
}

class SmartLight extends SmartDevice {
    SmartLight(String name) {
        super(name);
    }
    @Override
    void turnOn() throws Exception {
        System.out.println(name + " is turning ON...");
        throw new Exception("Power surge detected while turning ON " + name);
    }
    @Override
    void turnOff() {
        System.out.println(name + " is turning OFF...");
    }
}

public class Assignment1 {
    public static void main(String[] args) {
        SmartDevice device = new SmartLight("Living Room Light");
        try {
            device.turnOn();
            device.turnOff();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            device.cleanup();
            System.out.println("Device cleanup completed.");
        }
    }
}
