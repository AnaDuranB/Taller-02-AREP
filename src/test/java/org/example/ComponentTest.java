package org.example;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ComponentTest {

    @Test
    public void testComponentCreation() {
        Component component = new Component("CPU", "Procesador", 300.0);
        assertEquals("CPU", component.getName());
        assertEquals("Procesador", component.getType());
        assertEquals(300.0, component.getPrice(), 0.001);
    }
    @Test
    public void testToString() {
        Component component = new Component("GPU", "Tarjeta Gráfica", 500.0);
        String expected = "{\"name\":\"GPU\", \"type\":\"Tarjeta Gráfica\", \"price\":500.0}";
        assertEquals(expected, component.toString());
    }
}
